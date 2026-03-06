/**
 * App.tsx — Root application component for the Medical ML Platform frontend.
 *
 * Responsibilities:
 *   1. Sets up client-side routing with React Router (BrowserRouter).
 *   2. Defines all page routes with authentication guards.
 *   3. Wraps authenticated pages in a consistent layout (Sidebar + main content).
 *
 * Route structure:
 *   /login       — Public login page (no auth required).
 *   /dashboard   — Protected main dashboard with population stats.
 *   /patients    — Protected patient list with search/pagination.
 *   /patients/:id — Protected individual patient profile.
 *   /upload      — Protected file upload for data ingestion.
 *   /analytics   — Protected analytics/visualization page.
 *   /            — Redirects to /dashboard.
 *
 * ProtectedRoute: Checks auth state from Zustand store; redirects to /login if not authenticated.
 * AppLayout: Provides the sidebar navigation + main content area with responsive design.
 */

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './store/authStore'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import PatientListPage from './pages/PatientListPage'
import PatientProfilePage from './pages/PatientProfilePage'
import UploadAnalysisPage from './pages/UploadAnalysisPage'
import AnalyticsPage from './pages/AnalyticsPage'
import Sidebar from './components/layout/Sidebar'

function ProtectedRoute({ children }: { children: React.ReactNode }) {
    const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
    return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />
}

function AppLayout({ children }: { children: React.ReactNode }) {
    return (
        <div className="flex h-screen bg-clinical">
            <Sidebar />
            <main className="flex-1 overflow-auto">
                <div className="p-6 max-w-[1600px] mx-auto animate-fade-in">
                    {children}
                </div>
            </main>
        </div>
    )
}

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/dashboard" element={
                    <ProtectedRoute><AppLayout><DashboardPage /></AppLayout></ProtectedRoute>
                } />
                <Route path="/patients" element={
                    <ProtectedRoute><AppLayout><PatientListPage /></AppLayout></ProtectedRoute>
                } />
                <Route path="/patients/:id" element={
                    <ProtectedRoute><AppLayout><PatientProfilePage /></AppLayout></ProtectedRoute>
                } />
                <Route path="/upload" element={
                    <ProtectedRoute><AppLayout><UploadAnalysisPage /></AppLayout></ProtectedRoute>
                } />
                <Route path="/analytics" element={
                    <ProtectedRoute><AppLayout><AnalyticsPage /></AppLayout></ProtectedRoute>
                } />
                <Route path="/" element={<Navigate to="/dashboard" replace />} />
            </Routes>
        </BrowserRouter>
    )
}
