export interface UserResponse {
  id: string;
  email: string;
  username: string;
  displayName: string;
  roles: string[];
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number;
  user: UserResponse;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload extends LoginPayload {
  username: string;
  displayName: string;
}

export interface MetricPoint {
  label: string;
  value: number;
}

export interface SentimentBreakdown {
  positive: number;
  neutral: number;
  negative: number;
  averageScore: number;
}

export interface LatestPostView {
  externalId: string;
  platform: string;
  authorUsername: string;
  authorDisplayName: string;
  content: string;
  language: string;
  sentiment: 'POSITIVE' | 'NEUTRAL' | 'NEGATIVE';
  publishedAt: string;
}

export interface SystemHealthView {
  database: string;
  kafka: string;
  redis: string;
  elasticsearch: string;
  cpuUsage: number;
  usedMemoryBytes: number;
  maxMemoryBytes: number;
}

export interface DashboardSummary {
  totalPosts: number;
  postsToday: number;
  postsPerMinute: number;
  postsPerSecond: number;
  sentiment: SentimentBreakdown;
  topHashtags: MetricPoint[];
  topKeywords: MetricPoint[];
  activePlatforms: MetricPoint[];
  topUsers: MetricPoint[];
  latestPosts: LatestPostView[];
  systemHealth: SystemHealthView;
  generatedAt: string;
}

export interface RealtimeMessage<T = unknown> {
  channel: 'dashboard-updates' | 'new-post' | 'new-alert' | 'analytics-update' | 'system-health' | 'notifications';
  payload: T;
  timestamp: string;
}

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '';

async function request<T>(path: string, init: RequestInit): Promise<T> {
  const token = localStorage.getItem('socialanalytics.accessToken');
  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init.headers,
    },
  });
  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`);
  }
  return response.json() as Promise<T>;
}

export async function login(payload: LoginPayload): Promise<AuthResponse> {
  return request<AuthResponse>('/api/v1/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export async function register(payload: RegisterPayload): Promise<AuthResponse> {
  return request<AuthResponse>('/api/v1/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export async function currentUser(): Promise<UserResponse> {
  return request<UserResponse>('/api/v1/users/me', {
    method: 'GET',
  });
}

export async function dashboardSummary(): Promise<DashboardSummary> {
  return request<DashboardSummary>('/api/v1/dashboard/summary', {
    method: 'GET',
  });
}
