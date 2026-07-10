import { afterEach, describe, expect, it, vi } from 'vitest';
import { exportCsvReport } from './client';

describe('api client authentication retry', () => {
  afterEach(() => {
    vi.restoreAllMocks();
    localStorage.clear();
  });

  it('refreshes an expired access token before retrying a report download', async () => {
    localStorage.setItem('socialanalytics.accessToken', 'expired-access-token');
    localStorage.setItem('socialanalytics.refreshToken', 'valid-refresh-token');
    const refreshedAuth = {
      accessToken: 'fresh-access-token',
      refreshToken: 'rotated-refresh-token',
      expiresInSeconds: 900,
      user: {
        id: 'user-1',
        email: 'analyst@example.com',
        username: 'analyst',
        displayName: 'Analytics Lead',
        roles: ['ROLE_USER'],
        createdAt: new Date(0).toISOString(),
        updatedAt: new Date(0).toISOString(),
      },
    };
    const fetchMock = vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(new Response(JSON.stringify({ message: 'Authentication is required' }), {
        status: 401,
        headers: { 'Content-Type': 'application/json' },
      }))
      .mockResolvedValueOnce(new Response(JSON.stringify(refreshedAuth), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }))
      .mockResolvedValueOnce(new Response('section,label,value\nsummary,totalPosts,1\n', {
        status: 200,
        headers: { 'Content-Type': 'text/csv' },
      }));

    const blob = await exportCsvReport();

    expect(blob.type).toContain('text/csv');
    expect(localStorage.getItem('socialanalytics.accessToken')).toBe('fresh-access-token');
    expect(localStorage.getItem('socialanalytics.refreshToken')).toBe('rotated-refresh-token');
    expect(fetchMock).toHaveBeenCalledTimes(3);
    expect(fetchMock.mock.calls[2]?.[1]?.headers).toMatchObject({
      Authorization: 'Bearer fresh-access-token',
    });
  });
});
