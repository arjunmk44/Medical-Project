/** @type {import('tailwindcss').Config} */
export default {
    content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
    theme: {
        extend: {
            colors: {
                primary: { DEFAULT: '#0A1628', light: '#1A2A44', dark: '#050D18' },
                clinical: { DEFAULT: '#F8FAFC', warm: '#F1F5F9' },
                accent: { DEFAULT: '#14B8A6', light: '#5EEAD4', dark: '#0F766E' },
                warning: { DEFAULT: '#F59E0B', light: '#FCD34D' },
                danger: { DEFAULT: '#EF4444', light: '#FCA5A5' },
                info: { DEFAULT: '#3B82F6', light: '#93C5FD' },
            },
            fontFamily: {
                sans: ['DM Sans', 'system-ui', 'sans-serif'],
                serif: ['Source Serif 4', 'Georgia', 'serif'],
            },
            animation: {
                'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
                'fade-in': 'fadeIn 0.3s ease-out',
                'slide-up': 'slideUp 0.3s ease-out',
            },
            keyframes: {
                fadeIn: { '0%': { opacity: '0' }, '100%': { opacity: '1' } },
                slideUp: { '0%': { opacity: '0', transform: 'translateY(10px)' }, '100%': { opacity: '1', transform: 'translateY(0)' } },
            },
        },
    },
    plugins: [],
}
