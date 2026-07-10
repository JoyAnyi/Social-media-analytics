import { useEffect, useMemo, useState } from 'react';
import { ApiClientError, currentUser, type AuthResponse, type UserResponse } from '../api/client';
import { Dashboard } from '../features/dashboard/Dashboard';
import { AuthPanel } from '../features/auth/AuthPanel';
import { clearSession, persistSession, persistUser, readAccessToken, readStoredUser } from '../features/auth/authStorage';

export function App() {
  const storedUser = useMemo(() => readStoredUser(), []);
  const [user, setUser] = useState<UserResponse | null>(storedUser);

  useEffect(() => {
    if (!readAccessToken()) {
      return undefined;
    }
    let cancelled = false;
    currentUser()
      .then((refreshedUser) => {
        if (!cancelled) {
          persistUser(refreshedUser);
          setUser(refreshedUser);
        }
      })
      .catch((caughtError) => {
        if (!cancelled && caughtError instanceof ApiClientError && caughtError.status === 401) {
          clearSession();
          setUser(null);
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  function handleAuthenticated(response: AuthResponse) {
    persistSession(response);
    setUser(response.user);
  }

  function handleLogout() {
    clearSession();
    setUser(null);
  }

  if (user) {
    return <Dashboard user={user} onLogout={handleLogout} />;
  }

  return (
    <div className="min-h-screen bg-canvas px-4 py-6 text-ink md:px-8">
      <div className="mx-auto grid min-h-[calc(100vh-3rem)] max-w-6xl items-center gap-8 lg:grid-cols-[1fr_420px]">
        <section>
          <div className="mb-8 flex items-center gap-3">
            <div className="h-10 w-10 rounded-md bg-brand" />
            <div>
              <p className="text-lg font-bold">Social Analytics</p>
              <p className="text-sm text-muted">Real-time social media intelligence</p>
            </div>
          </div>
          <div className="max-w-2xl">
            <h1 className="text-4xl font-bold leading-tight text-ink md:text-5xl">
              Real-time analytics for every social signal.
            </h1>
            <p className="mt-5 text-lg leading-8 text-muted">
              Monitor live post volume, sentiment, trending hashtags, and alert activity from one secure operations dashboard.
            </p>
          </div>
        </section>
        <AuthPanel onAuthenticated={handleAuthenticated} />
      </div>
    </div>
  );
}
