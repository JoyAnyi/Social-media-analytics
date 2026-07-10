import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { App } from './App';

describe('App', () => {
  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
    localStorage.clear();
  });

  it('renders the authentication entry screen', () => {
    localStorage.clear();
    render(<App />);

    expect(screen.getByText('Social Analytics')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('shows backend validation messages during registration', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(new Response(JSON.stringify({
      message: 'Validation failed',
      validationErrors: {
        password: 'must include uppercase, lowercase, number, and special character',
      },
    }), {
      status: 400,
      headers: { 'Content-Type': 'application/json' },
    }));

    render(<App />);

    fireEvent.click(screen.getByRole('button', { name: /register/i }));
    fireEvent.click(screen.getByRole('button', { name: /create account/i }));

    await waitFor(() => {
      expect(screen.getByText(/password: must include uppercase/i)).toBeInTheDocument();
    });
  });
});
