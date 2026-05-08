/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        display: ['"Noto Serif SC"', '"Source Han Serif SC"', 'serif'],
        body: ['"LXGW WenKai"', '"Noto Sans SC"', 'sans-serif'],
      },
      colors: {
        ink: '#1f2933',
        paper: '#f6f0e6',
        moss: '#4f6f52',
        clay: '#b45f43',
        wheat: '#e6c88f',
      },
    },
  },
  plugins: [],
};
