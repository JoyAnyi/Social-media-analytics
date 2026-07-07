/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#111827',
        muted: '#64748b',
        line: '#dbe3ea',
        canvas: '#f6f8fb',
        brand: '#0f9f9a',
        coral: '#ef6f6c',
        amber: '#d99920',
      },
      boxShadow: {
        panel: '0 14px 40px rgba(15, 23, 42, 0.08)',
      },
    },
  },
  plugins: [],
};
