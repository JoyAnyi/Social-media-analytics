import type { ButtonHTMLAttributes, ReactNode } from 'react';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost';
  icon?: ReactNode;
}

const variants = {
  primary: 'bg-brand text-white border-brand hover:bg-teal-700',
  secondary: 'bg-white text-ink border-line hover:bg-slate-50',
  ghost: 'bg-transparent text-muted border-transparent hover:bg-slate-100 hover:text-ink',
};

export function Button({ className = '', variant = 'primary', icon, children, ...props }: ButtonProps) {
  return (
    <button
      className={`focus-ring inline-flex h-10 items-center justify-center gap-2 rounded-md border px-4 text-sm font-semibold transition ${variants[variant]} ${className}`}
      {...props}
    >
      {icon}
      {children}
    </button>
  );
}
