import { useEffect, useMemo, useState } from 'react';
import { dashboardSummary, type DashboardSummary, type LatestPostView, type RealtimeMessage, type UserResponse } from '../../api/client';
import { Button } from '../../components/Button';
import { Icon } from '../../components/Icon';
import { readAccessToken } from '../auth/authStorage';
import { fallbackSummary } from './dashboardData';
import { SentimentChart } from './SentimentChart';

interface DashboardProps {
  user: UserResponse | null;
  onLogout: () => void;
}

export function Dashboard({ user, onLogout }: DashboardProps) {
  const [summary, setSummary] = useState<DashboardSummary>(fallbackSummary);
  const [connection, setConnection] = useState<'connecting' | 'live' | 'offline'>('connecting');
  const positivePercent = useMemo(() => percentage(summary.sentiment.positive, sentimentTotal(summary)), [summary]);
  const neutralPercent = useMemo(() => percentage(summary.sentiment.neutral, sentimentTotal(summary)), [summary]);
  const negativePercent = useMemo(() => percentage(summary.sentiment.negative, sentimentTotal(summary)), [summary]);

  useEffect(() => {
    let cancelled = false;
    dashboardSummary()
      .then((data) => {
        if (!cancelled) {
          setSummary(data);
        }
      })
      .catch(() => setConnection('offline'));
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
        if (message.channel === 'dashboard-updates' || message.channel === 'analytics-update') {
          dashboardSummary().then(setSummary).catch(() => setConnection('offline'));
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
          {[
            ['Dashboard', 'analytics'],
            ['Search', 'search'],
            ['Alerts', 'bell'],
            ['Reports', 'hash'],
            ['Admin', 'shield'],
          ].map(([label, iconName]) => (
            <a
              className={`flex items-center gap-3 rounded-md px-3 py-2.5 ${label === 'Dashboard' ? 'bg-teal-50 text-brand' : 'text-muted hover:bg-slate-50 hover:text-ink'}`}
              href="#"
              key={label as string}
            >
              <Icon name={iconName as 'analytics' | 'bell' | 'hash' | 'search' | 'shield'} size={17} />
              {label as string}
            </a>
          ))}
        </nav>
      </aside>

      <main className="lg:pl-64">
        <header className="sticky top-0 z-10 flex h-16 items-center justify-between border-b border-line bg-white/95 px-5 backdrop-blur">
          <div>
            <h1 className="text-lg font-bold">Dashboard</h1>
            <p className="text-xs font-medium text-muted">Live operating view</p>
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

        <div className="grid gap-5 p-5 xl:grid-cols-[1fr_360px]">
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
                </div>
              </section>
            </div>

            <div className="grid gap-5 lg:grid-cols-3">
              <MetricList title="Top keywords" items={summary.topKeywords} />
              <MetricList title="Active platforms" items={summary.activePlatforms} />
              <MetricList title="System health" items={[
                { label: 'Database', value: summary.systemHealth.database },
                { label: 'Kafka', value: summary.systemHealth.kafka },
                { label: 'Redis', value: summary.systemHealth.redis },
                { label: 'Elasticsearch', value: summary.systemHealth.elasticsearch },
              ]} />
            </div>

            <section className="rounded-lg border border-line bg-white shadow-panel">
              <div className="border-b border-line px-5 py-4">
                <h2 className="text-base font-semibold">Latest posts</h2>
              </div>
              <div className="divide-y divide-line">
                {summary.latestPosts.map((post) => (
                  <article className="grid gap-3 px-5 py-4 md:grid-cols-[110px_150px_1fr_90px]" key={post.externalId}>
                    <span className="text-sm font-bold">{post.platform}</span>
                    <span className="text-sm text-muted">@{post.authorUsername}</span>
                    <p className="text-sm text-ink">{post.content}</p>
                    <span className="text-sm font-semibold text-brand">{formatSentiment(post)}</span>
                  </article>
                ))}
              </div>
            </section>
          </section>
        </div>
      </main>
    </div>
  );
}

function Metric({ label, value, delta, intent = 'normal' }: { label: string; value: string; delta: string; intent?: 'normal' | 'warning' }) {
  return (
    <section className="rounded-lg border border-line bg-white p-5 shadow-panel">
      <p className="text-sm font-semibold text-muted">{label}</p>
      <div className="mt-3 flex items-end justify-between">
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

function dashboardSocketUrl(token: string) {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  return `${protocol}//${window.location.host}/ws/dashboard?token=${encodeURIComponent(token)}`;
}
