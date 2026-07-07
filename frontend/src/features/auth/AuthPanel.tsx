import { useState } from 'react';
import { login, register, type AuthResponse } from '../../api/client';
import { Button } from '../../components/Button';
import { Icon } from '../../components/Icon';

interface AuthPanelProps {
  onAuthenticated: (response: AuthResponse) => void;
}

export function AuthPanel({ onAuthenticated }: AuthPanelProps) {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [email, setEmail] = useState('analyst@example.com');
  const [password, setPassword] = useState('correct horse battery');
  const [username, setUsername] = useState('analyst');
  const [displayName, setDisplayName] = useState('Analytics Lead');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit() {
    setIsSubmitting(true);
    setError(null);
    try {
      const response =
        mode === 'login'
          ? await login({ email, password })
          : await register({ email, password, username, displayName });
      onAuthenticated(response);
    } catch {
      setError('Authentication failed. Check the backend is running and credentials are valid.');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section className="rounded-lg border border-line bg-white p-5 shadow-panel">
      <div className="mb-5 flex items-center justify-between">
        <div>
          <h2 className="text-lg font-semibold text-ink">Sign in</h2>
          <p className="mt-1 text-sm text-muted">Access live social intelligence.</p>
        </div>
        <div className="rounded-md bg-teal-50 p-2 text-brand">
          <Icon name="lock" size={20} />
        </div>
      </div>

      <div className="mb-4 grid grid-cols-2 rounded-md bg-slate-100 p-1 text-sm font-semibold">
        <button
          className={`rounded px-3 py-2 ${mode === 'login' ? 'bg-white text-ink shadow-sm' : 'text-muted'}`}
          onClick={() => setMode('login')}
          type="button"
        >
          Login
        </button>
        <button
          className={`rounded px-3 py-2 ${mode === 'register' ? 'bg-white text-ink shadow-sm' : 'text-muted'}`}
          onClick={() => setMode('register')}
          type="button"
        >
          Register
        </button>
      </div>

      <form
        className="space-y-4"
        onSubmit={(event) => {
          event.preventDefault();
          void submit();
        }}
      >
        <label className="block text-sm font-medium text-ink">
          Email
          <span className="mt-1 flex items-center rounded-md border border-line bg-white px-3">
            <Icon className="text-muted" name="mail" size={16} />
            <input
              className="h-11 w-full border-0 px-3 text-sm outline-none"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              type="email"
              autoComplete="email"
            />
          </span>
        </label>

        {mode === 'register' && (
          <div className="grid gap-4 sm:grid-cols-2">
            <label className="block text-sm font-medium text-ink">
              Username
              <input
                className="mt-1 h-11 w-full rounded-md border border-line px-3 text-sm outline-none focus:border-brand"
                value={username}
                onChange={(event) => setUsername(event.target.value)}
              />
            </label>
            <label className="block text-sm font-medium text-ink">
              Display name
              <input
                className="mt-1 h-11 w-full rounded-md border border-line px-3 text-sm outline-none focus:border-brand"
                value={displayName}
                onChange={(event) => setDisplayName(event.target.value)}
              />
            </label>
          </div>
        )}

        <label className="block text-sm font-medium text-ink">
          Password
          <input
            className="mt-1 h-11 w-full rounded-md border border-line px-3 text-sm outline-none focus:border-brand"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            type="password"
            autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
          />
        </label>

        {error && (
          <p className="rounded-md border border-coral/30 bg-red-50 px-3 py-2 text-sm text-red-700">
            {error}
          </p>
        )}

        <Button className="w-full" disabled={isSubmitting} icon={<Icon name="user" size={16} />}>
          {isSubmitting ? 'Working...' : mode === 'login' ? 'Sign in' : 'Create account'}
        </Button>
      </form>
    </section>
  );
}
