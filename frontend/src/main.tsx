/**
 * main.tsx — Application entry point.
 *
 * Sets up:
 *   1. React Query (TanStack Query) for server state management.
 *      - staleTime: 30s — cached data is considered fresh for 30 seconds.
 *      - retry: 1 — failed queries retry once before showing an error.
 *   2. React.StrictMode — enables development-only checks for deprecated patterns.
 *   3. Mounts the App component into the #root DOM element.
 *
 * React Query is used for all API calls (fetching patients, ML results, etc.)
 * and provides automatic caching, refetching, and loading states.
 */

import React from 'react'
import ReactDOM from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import App from './App'
import './index.css'

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 30000,
            retry: 1,
        },
    },
})

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <QueryClientProvider client={queryClient}>
            <App />
        </QueryClientProvider>
    </React.StrictMode>,
)
