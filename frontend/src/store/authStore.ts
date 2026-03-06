/**
 * authStore.ts — Authentication state management using Zustand.
 *
 * Manages JWT authentication state with localStorage persistence:
 *   - token: JWT access token (sent with every API request).
 *   - username: Currently logged-in user's display name.
 *   - role: User role ("USER" or "ADMIN") for UI permission checks.
 *   - isAuthenticated: Derived boolean for route protection.
 *
 * login(): Saves credentials to both Zustand state and localStorage.
 * logout(): Clears credentials from both Zustand state and localStorage.
 *
 * State persists across page reloads via localStorage.
 * The API client (client.ts) reads the token from this store for every request.
 */

import { create } from 'zustand'

interface AuthState {
    token: string | null
    username: string | null
    role: string | null
    isAuthenticated: boolean
    login: (token: string, username: string, role: string) => void
    logout: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
    token: localStorage.getItem('token'),
    username: localStorage.getItem('username'),
    role: localStorage.getItem('role'),
    isAuthenticated: !!localStorage.getItem('token'),
    login: (token, username, role) => {
        localStorage.setItem('token', token)
        localStorage.setItem('username', username)
        localStorage.setItem('role', role)
        set({ token, username, role, isAuthenticated: true })
    },
    logout: () => {
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('role')
        set({ token: null, username: null, role: null, isAuthenticated: false })
    },
}))
