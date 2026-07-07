import type { DashboardSummary } from '../../api/client';

export const fallbackSummary: DashboardSummary = {
  totalPosts: 0,
  postsToday: 0,
  postsPerMinute: 0,
  postsPerSecond: 0,
  sentiment: {
    positive: 0,
    neutral: 0,
    negative: 0,
    averageScore: 0,
  },
  topHashtags: [],
  topKeywords: [],
  activePlatforms: [],
  topUsers: [],
  latestPosts: [],
  systemHealth: {
    database: 'UNKNOWN',
    kafka: 'UNKNOWN',
    redis: 'UNKNOWN',
    elasticsearch: 'UNKNOWN',
    cpuUsage: 0,
    usedMemoryBytes: 0,
    maxMemoryBytes: 0,
  },
  generatedAt: new Date(0).toISOString(),
};
