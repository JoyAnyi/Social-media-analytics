import { useEffect, useMemo, useState } from 'react';
import {
  ApiClientError,
  apiBaseUrl,
  dashboardSummary,
  exportCsvReport,
  exportPdfReport,
  generatePosts,
  type DashboardSummary,
  type LatestPostView,
  type RealtimeMessage,
  type UserResponse,
} from '../../api/client';
import { Button } from '../../components/Button';
import { Icon } from '../../components/Icon';
import { readAccessToken } from '../auth/authStorage';
import { fallbackSummary } from './dashboardData';
import { SentimentChart } from './SentimentChart';

interface DashboardProps {
  user: UserResponse | null;
  onLogout: () => void;
}

type ActiveSection = 'dashboard' | 'live' | 'search' | 'alerts' | 'reports' | 'admin';

type NavItem = { id: ActiveSection; label: string; icon: 'analytics' | 'bell' | 'hash' | 'search' | 'shield' | 'trend' };

const standardNavItems: NavItem[] = [
  { id: 'dashboard', label: 'Dashboard', icon: 'analytics' },
  { id: 'live', label: 'Live Feed', icon: 'trend' },
  { id: 'search', label: 'Search', icon: 'search' },
  { id: 'alerts', label: 'Alerts', icon: 'bell' },
  { id: 'reports', label: 'Reports', icon: 'hash' },
];
const adminNavItem: NavItem = { id: 'admin', label: 'Admin', icon: 'shield' };

export function Dashboard({ user, onLogout }: DashboardProps) {
  const [summary, setSummary] = useState<DashboardSummary>(fallbackSummary);
  const [connection, setConnection] = useState<'connecting' | 'live' | 'offline'>('connecting');
  const [activeSection, setActiveSection] = useState<ActiveSection>('dashboard');
  const [simulatorTopic, setSimulatorTopic] = useState('ProductLaunch');
  const [simulatorCount, setSimulatorCount] = useState(10);
  const [simulatorStatus, setSimulatorStatus] = useState<string | null>(null);
  const [isGenerating, setIsGenerating] = useState(false);
  const [reportStatus, setReportStatus] = useState<string | null>(null);
  const [isExportingReport, setIsExportingReport] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const isAdmin = user?.roles.includes('ROLE_ADMIN') ?? false;
  const navItems = useMemo(() => isAdmin ? [...standardNavItems, adminNavItem] : standardNavItems, [isAdmin]);
  const activeNavItem = navItems.find((item) => item.id === activeSection) ?? navItems[0];
  const positivePercent = useMemo(() => percentage(summary.sentiment.positive, sentimentTotal(summary)), [summary]);
  const neutralPercent = useMemo(() => percentage(summary.sentiment.neutral, sentimentTotal(summary)), [summary]);
  const negativePercent = useMemo(() => percentage(summary.sentiment.negative, sentimentTotal(summary)), [summary]);
  const filteredPosts = useMemo(() => filterPosts(summary.latestPosts, searchTerm), [summary.latestPosts, searchTerm]);

  async function refreshSummary() {
    const data = await dashboardSummary();
    setSummary(data);
  }

  async function refreshSummaryWithRetry(attempts = 4) {
    for (let attempt = 0; attempt < attempts; attempt += 1) {
      try {
        await refreshSummary();
      } catch (caughtError) {
        if (!handleRequestError(caughtError, onLogout)) {
          setConnection('offline');
        }
      }
      if (attempt < attempts - 1) {
        await delay(700);
      }
    }
  }

  async function runSimulator() {
    setIsGenerating(true);
    setSimulatorStatus(null);
    const count = normalizeSimulatorCount(simulatorCount);
    setSimulatorCount(count);
    try {
      const startingTotal = summary.totalPosts;
      const response = await generatePosts({
        count,
        speed: count >= 25 ? 'FAST' : count <= 5 ? 'SLOW' : 'MEDIUM',
        topic: simulatorTopic,
      });
      const processed = await waitForProcessedPosts(startingTotal, response.posts.length);
      if (processed) {
        setSimulatorStatus(`Processed ${response.posts.length} simulated posts. Dashboard and live feed are current.`);
      } else {
        setSimulatorStatus(`Published ${response.posts.length} posts, but processing has not updated the dashboard yet. Check Kafka/backend logs.`);
      }
    } catch (caughtError) {
      if (handleRequestError(caughtError, onLogout)) {
        return;
      }
      setSimulatorStatus(simulatorErrorMessage(caughtError));
    } finally {
      setIsGenerating(false);
    }
  }

  async function waitForProcessedPosts(startingTotal: number, publishedCount: number) {
    const expectedTotal = startingTotal + publishedCount;
    for (let attempt = 0; attempt < 22; attempt += 1) {
      try {
        const data = await dashboardSummary();
        setSummary(data);
        if (data.totalPosts >= expectedTotal) {
          return true;
        }
      } catch (caughtError) {
        if (handleRequestError(caughtError, onLogout)) {
          return false;
        }
      }
      await delay(700);
    }
    return false;
  }

  async function downloadReport(format: 'csv' | 'pdf') {
    setIsExportingReport(true);
    setReportStatus(null);
    try {
      const blob = format === 'csv' ? await exportCsvReport() : await exportPdfReport();
      downloadBlob(blob, `social-analytics-report.${format}`);
      setReportStatus(`${format.toUpperCase()} report downloaded.`);
    } catch (caughtError) {
      if (handleRequestError(caughtError, onLogout)) {
        return;
      }
      setReportStatus(caughtError instanceof ApiClientError ? caughtError.message : 'Report export failed.');
    } finally {
      setIsExportingReport(false);
    }
  }

  function updateSimulatorCount(value: number) {
    setSimulatorCount(normalizeSimulatorCount(value));
  }

  useEffect(() => {
    if (!isAdmin && activeSection === 'admin') {
      setActiveSection('dashboard');
    }
  }, [activeSection, isAdmin]);

  useEffect(() => {
    let cancelled = false;
    dashboardSummary()
      .then((data) => {
        if (!cancelled) {
          setSummary(data);
          setConnection((current) => current === 'connecting' ? 'offline' : current);
        }
      })
      .catch((caughtError) => {
        if (!handleRequestError(caughtError, onLogout)) {
          setConnection('offline');
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    const token = readAccessToken();
    if (!token) {
      setConnection('offline');
      return undefined;
    }
    const accessToken = token;
    let closed = false;
    let retry: number | undefined;
    let socket: WebSocket | undefined;

    function connect() {
      setConnection('connecting');
      socket = new WebSocket(dashboardSocketUrl(accessToken));
      socket.onopen = () => setConnection('live');
      socket.onclose = () => {
        if (!closed) {
          setConnection('offline');
          retry = window.setTimeout(connect, 2500);
        }
      };
      socket.onerror = () => setConnection('offline');
      socket.onmessage = (event) => {
        const message = JSON.parse(event.data) as RealtimeMessage;
        if (message.channel === 'dashboard-updates' || message.channel === 'analytics-update' || message.channel === 'new-post') {
          dashboardSummary().then(setSummary).catch((caughtError) => {
            if (!handleRequestError(caughtError, onLogout)) {
              setConnection('offline');
            }
          });
        }
      };
    }

    connect();
    return () => {
      closed = true;
      if (retry) {
        window.clearTimeout(retry);
      }
      socket?.close();
    };
  }, []);

  return (
    <div className="min-h-screen bg-canvas text-ink">
      <aside className="fixed inset-y-0 left-0 hidden w-64 border-r border-line bg-white px-4 py-5 lg:block">
        <div className="mb-8 flex items-center gap-3">
          <div className="grid h-10 w-10 place-items-center rounded-md bg-brand text-white">
            <Icon name="trend" size={21} />
          </div>
          <div>
            <p className="text-base font-bold">Social Analytics</p>
            <p className="text-xs font-medium text-muted">Real-time intelligence</p>
          </div>
        </div>
        <nav className="space-y-1 text-sm font-semibold">
          {navItems.map((item) => (
            <button
              className={`flex w-full items-center gap-3 rounded-md px-3 py-2.5 text-left ${item.id === activeSection ? 'bg-teal-50 text-brand' : 'text-muted hover:bg-slate-50 hover:text-ink'}`}
              key={item.id}
              onClick={() => setActiveSection(item.id)}
              type="button"
            >
              <Icon name={item.icon} size={17} />
              {item.label}
            </button>
          ))}
        </nav>
      </aside>

      <main className="lg:pl-64">
        <header className="sticky top-0 z-10 flex h-16 items-center justify-between border-b border-line bg-white/95 px-5 backdrop-blur">
          <div>
            <h1 className="text-lg font-bold">{activeNavItem.label}</h1>
            <p className="text-xs font-medium text-muted">
              {activeSection === 'dashboard' ? 'Live operating view' : 'Operational workspace'}
            </p>
          </div>
          <div className="flex items-center gap-3">
            <div className="hidden text-right sm:block">
              <p className="text-sm font-semibold">{user?.displayName ?? 'Guest analyst'}</p>
              <p className="text-xs text-muted">{user?.email ?? 'Not signed in'}</p>
            </div>
            <span className={`rounded-md px-2.5 py-1 text-xs font-bold ${connection === 'live' ? 'bg-teal-50 text-brand' : connection === 'connecting' ? 'bg-amber-50 text-amber' : 'bg-rose-50 text-coral'}`}>
              {connection === 'live' ? 'Live' : connection === 'connecting' ? 'Connecting' : 'Offline'}
            </span>
            <Button variant="secondary" onClick={onLogout}>Logout</Button>
          </div>
        </header>

        <div className="p-5">
          {activeSection === 'dashboard' && (
            <DashboardOverview
              negativePercent={negativePercent}
              neutralPercent={neutralPercent}
              onRunSimulator={runSimulator}
              positivePercent={positivePercent}
              simulatorCount={simulatorCount}
              simulatorStatus={simulatorStatus}
              simulatorTopic={simulatorTopic}
              summary={summary}
              isAdmin={isAdmin}
              isGenerating={isGenerating}
              setSimulatorCount={updateSimulatorCount}
              setSimulatorTopic={setSimulatorTopic}
            />
          )}
          {activeSection === 'live' && (
            <LiveFeed
              onRunSimulator={runSimulator}
              posts={summary.latestPosts}
              simulatorStatus={simulatorStatus}
              isGenerating={isGenerating}
            />
          )}
          {activeSection === 'search' && (
            <SearchSection posts={filteredPosts} searchTerm={searchTerm} setSearchTerm={setSearchTerm} />
          )}
          {activeSection === 'alerts' && (
            <StatusSection
              title="Alerts"
              description="Alert notifications will appear here as rule evaluation is connected."
              items={[
                { label: 'Negative sentiment', value: `${negativePercent}%` },
                { label: 'Latest posts monitored', value: summary.latestPosts.length },
                { label: 'WebSocket status', value: connection.toUpperCase() },
              ]}
            />
          )}
          {activeSection === 'reports' && (
            <ReportsSection
              isExporting={isExportingReport}
              onExportCsv={() => downloadReport('csv')}
              onExportPdf={() => downloadReport('pdf')}
              status={reportStatus}
              summary={summary}
            />
          )}
          {activeSection === 'admin' && isAdmin && (
            <StatusSection
              title="System Health"
              description="Standalone mode intentionally disables Docker-only services unless you start them."
              items={systemHealthItems(summary)}
            />
          )}
        </div>
      </main>
    </div>
  );
}

function DashboardOverview({
  summary,
  positivePercent,
  neutralPercent,
  negativePercent,
  simulatorTopic,
  simulatorCount,
  simulatorStatus,
  isGenerating,
  isAdmin,
  setSimulatorTopic,
  setSimulatorCount,
  onRunSimulator,
}: {
  summary: DashboardSummary;
  positivePercent: number;
  neutralPercent: number;
  negativePercent: number;
  simulatorTopic: string;
  simulatorCount: number;
  simulatorStatus: string | null;
  isGenerating: boolean;
  isAdmin: boolean;
  setSimulatorTopic: (value: string) => void;
  setSimulatorCount: (value: number) => void;
  onRunSimulator: () => void;
}) {
  return (
    <div className="grid gap-5 xl:grid-cols-[1fr_360px]">
      <section className="space-y-5">
        <div className="grid gap-4 md:grid-cols-3">
          <Metric label="Total posts" value={formatNumber(summary.totalPosts)} delta={`${summary.postsToday} today`} />
          <Metric label="Posts / min" value={formatNumber(summary.postsPerMinute)} delta={`${summary.postsPerSecond}/sec`} />
          <Metric label="Positive" value={`${positivePercent}%`} delta={`${neutralPercent}% neutral`} />
        </div>

        <div className="grid gap-5 xl:grid-cols-[330px_1fr]">
          <section className="rounded-lg border border-line bg-white p-5 shadow-panel">
            <h2 className="text-base font-semibold">Sentiment</h2>
            <p className="mt-1 text-xs font-semibold text-muted">{negativePercent}% negative</p>
            <div className="mx-auto mt-4 max-w-56">
              <SentimentChart sentiment={summary.sentiment} />
            </div>
          </section>

          <section className="rounded-lg border border-line bg-white p-5 shadow-panel">
            <h2 className="text-base font-semibold">Trending hashtags</h2>
            <div className="mt-4 flex flex-wrap gap-2">
              {summary.topHashtags.map((tag) => (
                <span className="rounded-md border border-teal-100 bg-teal-50 px-3 py-2 text-sm font-semibold text-teal-800" key={tag.label}>
                  #{tag.label} <span className="text-teal-600">{tag.value}</span>
                </span>
              ))}
              {summary.topHashtags.length === 0 && <p className="text-sm text-muted">Run the simulator to create hashtag activity.</p>}
            </div>
          </section>
        </div>

        <div className="grid gap-5 lg:grid-cols-3">
          <MetricList title="Top keywords" items={summary.topKeywords} />
          <MetricList title="Active platforms" items={summary.activePlatforms} />
          {isAdmin && <MetricList title="System health" items={systemHealthItems(summary)} />}
        </div>

        <PostsTable posts={summary.latestPosts} title="Latest posts" />
      </section>

      <aside className="space-y-5">
        <SimulatorPanel
          count={simulatorCount}
          isGenerating={isGenerating}
          onCountChange={setSimulatorCount}
          onRun={onRunSimulator}
          onTopicChange={setSimulatorTopic}
          status={simulatorStatus}
          topic={simulatorTopic}
        />
        <MetricList title="Most active users" items={summary.topUsers} />
      </aside>
    </div>
  );
}

function ReportsSection({
  summary,
  status,
  isExporting,
  onExportCsv,
  onExportPdf,
}: {
  summary: DashboardSummary;
  status: string | null;
  isExporting: boolean;
  onExportCsv: () => void;
  onExportPdf: () => void;
}) {
  return (
    <section className="rounded-lg border border-line bg-white p-5 shadow-panel">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h2 className="text-base font-semibold">Reports</h2>
          <p className="mt-1 text-sm text-muted">Export the current analytics snapshot for review.</p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button disabled={isExporting} icon={<Icon name="hash" size={16} />} onClick={onExportCsv}>
            CSV
          </Button>
          <Button disabled={isExporting} icon={<Icon name="hash" size={16} />} onClick={onExportPdf} variant="secondary">
            PDF
          </Button>
        </div>
      </div>
      {status && <p className="mt-4 rounded-md border border-line bg-slate-50 px-3 py-2 text-sm font-medium text-muted">{status}</p>}
      <div className="mt-5 grid gap-4 md:grid-cols-3">
        <Metric label="Total posts" value={formatNumber(summary.totalPosts)} delta="included" />
        <Metric label="Generated at" value={new Date(summary.generatedAt).toLocaleTimeString()} delta="snapshot" />
        <Metric label="Top hashtag" value={summary.topHashtags[0]?.label ?? 'None'} delta="current" />
      </div>
    </section>
  );
}

function SimulatorPanel({
  topic,
  count,
  status,
  isGenerating,
  onTopicChange,
  onCountChange,
  onRun,
}: {
  topic: string;
  count: number;
  status: string | null;
  isGenerating: boolean;
  onTopicChange: (value: string) => void;
  onCountChange: (value: number) => void;
  onRun: () => void;
}) {
  return (
    <section className="rounded-lg border border-line bg-white p-5 shadow-panel">
      <h2 className="text-base font-semibold">Post simulator</h2>
      <div className="mt-4 space-y-4">
        <label className="block text-sm font-semibold text-ink">
          Topic
          <input
            className="mt-1 h-10 w-full rounded-md border border-line px-3 text-sm outline-none focus:border-brand"
            maxLength={80}
            onChange={(event) => onTopicChange(event.target.value)}
            value={topic}
          />
        </label>
        <label className="block text-sm font-semibold text-ink">
          Batch size
          <input
            className="mt-1 h-10 w-full rounded-md border border-line px-3 text-sm outline-none focus:border-brand"
            max={50}
            min={1}
            onChange={(event) => onCountChange(Number(event.target.value))}
            type="number"
            value={count}
          />
        </label>
        <Button className="w-full" disabled={isGenerating} icon={<Icon name="trend" size={16} />} onClick={onRun}>
          {isGenerating ? 'Generating...' : 'Generate posts'}
        </Button>
        {status && <p className="rounded-md border border-line bg-slate-50 px-3 py-2 text-sm font-medium text-muted">{status}</p>}
      </div>
    </section>
  );
}

function LiveFeed({ posts, simulatorStatus, isGenerating, onRunSimulator }: {
  posts: LatestPostView[];
  simulatorStatus: string | null;
  isGenerating: boolean;
  onRunSimulator: () => void;
}) {
  return (
    <div className="space-y-5">
      <section className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-line bg-white p-5 shadow-panel">
        <div>
          <h2 className="text-base font-semibold">Incoming posts</h2>
          <p className="mt-1 text-sm text-muted">Generate simulated traffic and watch the newest processed posts appear.</p>
        </div>
        <Button disabled={isGenerating} icon={<Icon name="trend" size={16} />} onClick={onRunSimulator}>
          {isGenerating ? 'Generating...' : 'Generate sample traffic'}
        </Button>
        {simulatorStatus && <p className="basis-full text-sm font-medium text-muted">{simulatorStatus}</p>}
      </section>
      <PostsTable posts={posts} title="Live activity feed" />
    </div>
  );
}

function SearchSection({ posts, searchTerm, setSearchTerm }: {
  posts: LatestPostView[];
  searchTerm: string;
  setSearchTerm: (value: string) => void;
}) {
  return (
    <div className="space-y-5">
      <section className="rounded-lg border border-line bg-white p-5 shadow-panel">
        <h2 className="text-base font-semibold">Search posts</h2>
        <input
          className="mt-4 h-11 w-full rounded-md border border-line px-3 text-sm outline-none focus:border-brand"
          onChange={(event) => setSearchTerm(event.target.value)}
          placeholder="Search by platform, author, content, sentiment, or language"
          value={searchTerm}
        />
      </section>
      <PostsTable posts={posts} title="Search results" />
    </div>
  );
}

function StatusSection({ title, description, items }: { title: string; description: string; items: Array<{ label: string; value: number | string }> }) {
  return (
    <section className="rounded-lg border border-line bg-white p-5 shadow-panel">
      <h2 className="text-base font-semibold">{title}</h2>
      <p className="mt-1 text-sm text-muted">{description}</p>
      <div className="mt-5 grid gap-4 md:grid-cols-3">
        {items.map((item) => (
          <Metric key={item.label} label={item.label} value={String(item.value)} delta="current" />
        ))}
      </div>
    </section>
  );
}

function PostsTable({ title, posts }: { title: string; posts: LatestPostView[] }) {
  return (
    <section className="rounded-lg border border-line bg-white shadow-panel">
      <div className="border-b border-line px-5 py-4">
        <h2 className="text-base font-semibold">{title}</h2>
      </div>
      <div className="divide-y divide-line">
        {posts.map((post) => (
          <article className="grid gap-3 px-5 py-4 md:grid-cols-[110px_150px_1fr_90px]" key={post.externalId}>
            <span className="text-sm font-bold">{post.platform}</span>
            <span className="text-sm text-muted">@{post.authorUsername}</span>
            <p className="text-sm text-ink">{post.content}</p>
            <span className="text-sm font-semibold text-brand">{formatSentiment(post)}</span>
          </article>
        ))}
        {posts.length === 0 && <p className="px-5 py-4 text-sm text-muted">No posts yet.</p>}
      </div>
    </section>
  );
}

function Metric({ label, value, delta, intent = 'normal' }: { label: string; value: string; delta: string; intent?: 'normal' | 'warning' }) {
  return (
    <section className="rounded-lg border border-line bg-white p-5 shadow-panel">
      <p className="text-sm font-semibold text-muted">{label}</p>
      <div className="mt-3 flex items-end justify-between gap-3">
        <strong className="text-3xl font-bold tracking-normal">{value}</strong>
        <span className={`text-sm font-bold ${intent === 'warning' ? 'text-amber' : 'text-brand'}`}>{delta}</span>
      </div>
    </section>
  );
}

function MetricList({ title, items }: { title: string; items: Array<{ label: string; value: number | string }> }) {
  return (
    <section className="rounded-lg border border-line bg-white p-5 shadow-panel">
      <h2 className="text-base font-semibold">{title}</h2>
      <div className="mt-4 space-y-3">
        {items.map((item) => (
          <div className="flex items-center justify-between gap-3 text-sm" key={item.label}>
            <span className="truncate font-semibold text-muted">{item.label}</span>
            <strong>{item.value}</strong>
          </div>
        ))}
        {items.length === 0 && <p className="text-sm text-muted">No data yet.</p>}
      </div>
    </section>
  );
}

function sentimentTotal(summary: DashboardSummary) {
  return summary.sentiment.positive + summary.sentiment.neutral + summary.sentiment.negative;
}

function percentage(value: number, total: number) {
  return total === 0 ? 0 : Math.round((value / total) * 100);
}

function formatNumber(value: number) {
  return new Intl.NumberFormat().format(value);
}

function formatSentiment(post: LatestPostView) {
  return post.sentiment.charAt(0) + post.sentiment.slice(1).toLowerCase();
}

function filterPosts(posts: LatestPostView[], searchTerm: string) {
  const query = searchTerm.trim().toLowerCase();
  if (!query) {
    return posts;
  }
  return posts.filter((post) => [
    post.platform,
    post.authorUsername,
    post.authorDisplayName,
    post.content,
    post.language,
    post.sentiment,
  ].some((value) => value.toLowerCase().includes(query)));
}

function simulatorErrorMessage(caughtError: unknown) {
  if (caughtError instanceof ApiClientError) {
    const validationMessages = Object.entries(caughtError.validationErrors)
      .map(([field, message]) => `${field}: ${message}`);
    if (validationMessages.length > 0) {
      return validationMessages.join('. ');
    }
    return caughtError.message;
  }
  return 'Simulator request failed. Confirm the backend is running and you are logged in.';
}

function systemHealthItems(summary: DashboardSummary) {
  const health = summary.systemHealth;
  if (!health) {
    return [];
  }
  return [
    { label: 'Database', value: health.database },
    { label: 'Kafka', value: health.kafka },
    { label: 'Redis', value: health.redis },
    { label: 'Elasticsearch', value: health.elasticsearch },
  ];
}

function downloadBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

function normalizeSimulatorCount(value: number) {
  if (!Number.isFinite(value)) {
    return 1;
  }
  return Math.min(50, Math.max(1, Math.trunc(value)));
}

function handleRequestError(caughtError: unknown, onLogout: () => void) {
  if (caughtError instanceof ApiClientError && caughtError.status === 401) {
    onLogout();
    return true;
  }
  return false;
}

function delay(milliseconds: number) {
  return new Promise((resolve) => {
    window.setTimeout(resolve, milliseconds);
  });
}

function dashboardSocketUrl(token: string) {
  const configuredBase = import.meta.env.VITE_WS_BASE_URL as string | undefined;
  if (configuredBase) {
    return `${configuredBase}/ws/dashboard?token=${encodeURIComponent(token)}`;
  }
  const base = apiBaseUrl || `${window.location.protocol}//${window.location.host}`;
  const url = new URL(base, window.location.origin);
  url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:';
  url.pathname = '/ws/dashboard';
  url.search = `token=${encodeURIComponent(token)}`;
  return url.toString();
}
