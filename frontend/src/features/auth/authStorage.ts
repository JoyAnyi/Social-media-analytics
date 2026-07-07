import type { AuthResponse, UserResponse } from '../../api/client';

const accessKey = 'socialanalytics.accessToken';
const refreshKey = 'socialanalytics.refreshToken';
const userKey = 'socialanalytics.user';

export function persistSession(auth: AuthResponse) {
  localStorage.setItem(accessKey, auth.accessToken);
  localStorage.setItem(refreshKey, auth.refreshToken);
  localStorage.setItem(userKey, JSON.stringify(auth.user));
}

export function readStoredUser(): UserResponse | null {
  const raw = localStorage.getItem(userKey);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as UserResponse;
  } catch {
    return null;
  }
}

export function readAccessToken(): string | null {
  return localStorage.getItem(accessKey);
}

export function clearSession() {
  localStorage.removeItem(accessKey);
  localStorage.removeItem(refreshKey);
  localStorage.removeItem(userKey);
}
