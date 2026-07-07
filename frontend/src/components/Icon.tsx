type IconName = 'analytics' | 'bell' | 'hash' | 'lock' | 'mail' | 'search' | 'shield' | 'trend' | 'user';

interface IconProps {
  name: IconName;
  size?: number;
  className?: string;
}

const paths: Record<IconName, string> = {
  analytics: 'M4 19V5M4 19h16M8 15l3-4 3 2 4-7',
  bell: 'M6 16h12l-1.2-2V10a4.8 4.8 0 0 0-9.6 0v4L6 16Zm5 3h2',
  hash: 'M8 4 6 20M16 4l-2 16M4 9h16M3 15h16',
  lock: 'M7 10V8a5 5 0 0 1 10 0v2M6 10h12v10H6V10Zm6 4v3',
  mail: 'M4 6h16v12H4V6Zm0 1 8 6 8-6',
  search: 'm15 15 5 5M4 11a7 7 0 1 0 14 0A7 7 0 0 0 4 11Z',
  shield: 'M12 3 5 6v6c0 4 3 7 7 9 4-2 7-5 7-9V6l-7-3Z',
  trend: 'M4 16 9 11l4 4 7-9M15 6h5v5',
  user: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8ZM4 20a8 8 0 0 1 16 0',
};

export function Icon({ name, size = 18, className = '' }: IconProps) {
  return (
    <svg
      aria-hidden
      className={className}
      fill="none"
      height={size}
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="2"
      viewBox="0 0 24 24"
      width={size}
    >
      <path d={paths[name]} />
    </svg>
  );
}
