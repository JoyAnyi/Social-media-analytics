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
  systemHealth: SystemHealthView | null;
  generatedAt: string;
}

export interface RealtimeMessage<T = unknown> {
  channel: 'dashboard-updates' | 'new-post' | 'new-alert' | 'analytics-update' | 'system-health' | 'notifications';
  payload: T;
  timestamp: string;
}

export interface GeneratePostsPayload {
  count: number;
  speed: 'SLOW' | 'MEDIUM' | 'FAST';
  topic: string;
}

export interface GeneratedPostsResponse {
  posts: Array<{
    externalId: string;
    platform: string;
    authorUsername: string;
    authorDisplayName: string;
    content: string;
    hashtags: string[];
    mentions: string[];
    language: string;
    publishedAt: string;
  }>;
}

interface ApiErrorResponse {
  message?: string;
  validationErrors?: Record<string, string>;
}

const accessTokenKey = 'socialanalytics.accessToken';
const refreshTokenKey = 'socialanalytics.refreshToken';
const userKey = 'socialanalytics.user';
let refreshInFlight: Promise<boolean> | null = null;

export class ApiClientError extends Error {
  readonly status: number;
  readonly validationErrors: Record<string, string>;

  constructor(status: number, message: string, validationErrors: Record<string, string> = {}) {
    super(message);
    this.name = 'ApiClientError';
    this.status = status;
    this.validationErrors = validationErrors;
  }
}

export const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? defaultApiBaseUrl();

async function request<T>(path: string, init: RequestInit): Promise<T> {
  const response = await fetchWithAuthRetry(path, init, true);
  if (!response.ok) {
    throw await apiErrorFromResponse(response);
  }
  return response.json() as Promise<T>;
}

async function requestBlob(path: string, init: RequestInit): Promise<Blob> {
  const response = await fetchWithAuthRetry(path, init, false);
  if (!response.ok) {
    throw await apiErrorFromResponse(response);
  }
  return response.blob();
}

async function fetchWithAuthRetry(path: string, init: RequestInit, jsonRequest: boolean): Promise<Response> {
  const response = await authenticatedFetch(path, init, jsonRequest);
  if (response.status !== 401 || isAuthPath(path)) {
    return response;
  }
  const refreshed = await refreshSession();
  if (!refreshed) {
    return response;
  }
  return authenticatedFetch(path, init, jsonRequest);
}

async function authenticatedFetch(path: string, init: RequestInit, jsonRequest: boolean): Promise<Response> {
  const token = localStorage.getItem(accessTokenKey);
  try {
    return await fetch(`${apiBaseUrl}${path}`, {
      ...init,
      headers: {
        ...(jsonRequest ? { 'Content-Type': 'application/json' } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...init.headers,
      },
    });
  } catch {
    throw new ApiClientError(0, 'Cannot reach the backend. Confirm Spring Boot is running on http://localhost:8080.');
  }
}

async function refreshSession(): Promise<boolean> {
  const refreshToken = localStorage.getItem(refreshTokenKey);
  if (!refreshToken) {
    return false;
  }
  if (refreshInFlight) {
    return refreshInFlight;
  }
  const refreshRequest = refreshAccessToken(refreshToken);
  refreshInFlight = refreshRequest;
  try {
    return await refreshRequest;
  } finally {
    if (refreshInFlight === refreshRequest) {
      refreshInFlight = null;
    }
  }
}

async function refreshAccessToken(refreshToken: string): Promise<boolean> {
  try {
    const response = await fetch(`${apiBaseUrl}/api/v1/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    });
    if (!response.ok) {
      return false;
    }
    const auth = await response.json() as AuthResponse;
    localStorage.setItem(accessTokenKey, auth.accessToken);
    localStorage.setItem(refreshTokenKey, auth.refreshToken);
    localStorage.setItem(userKey, JSON.stringify(auth.user));
    return true;
  } catch {
    return false;
  }
}

function isAuthPath(path: string) {
  return path.startsWith('/api/v1/auth/');
}

async function apiErrorFromResponse(response: Response) {
  const fallback = `Request failed with status ${response.status}`;
  try {
    const body = await response.json() as ApiErrorResponse;
    return new ApiClientError(response.status, body.message ?? fallback, body.validationErrors ?? {});
  } catch {
    return new ApiClientError(response.status, fallback);
  }
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

export async function generatePosts(payload: GeneratePostsPayload): Promise<GeneratedPostsResponse> {
  return request<GeneratedPostsResponse>('/api/v1/feed/simulator/posts', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export async function exportCsvReport(): Promise<Blob> {
  return requestBlob('/api/v1/reports/csv', {
    method: 'POST',
  });
}

export async function exportPdfReport(): Promise<Blob> {
  return requestBlob('/api/v1/reports/pdf', {
    method: 'POST',
  });
}

function defaultApiBaseUrl() {
  if (typeof window === 'undefined') {
    return 'http://localhost:8080';
  }
  const localHosts = new Set(['localhost', '127.0.0.1', '::1', '']);
  if (localHosts.has(window.location.hostname)) {
    return 'http://localhost:8080';
  }
  return `${window.location.protocol}//${window.location.hostname}:8080`;
}
