/**
 * client.ts — Axios HTTP client for API communication.
 *
 * Creates a pre-configured Axios instance with:
 *   1. baseURL: '/api' — all requests are relative to /api (proxied by Vite to the backend).
 *   2. Request interceptor: Attaches the JWT access token from the auth store
 *      to every outgoing request's Authorization header.
 *   3. Response interceptor: On 401 Unauthorized, automatically logs the user out
 *      (clears tokens from localStorage) and redirects to the login page.
 *
 * Usage: import api from './client'; api.get('/patients');
 */

import axios from 'axios'
import { useAuthStore } from '../store/authStore'

const api = axios.create({
    baseURL: '/api',
    headers: { 'Content-Type': 'application/json' },
})

// Attach JWT token to every request
api.interceptors.request.use((config) => {
    const token = useAuthStore.getState().token
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

// Handle 401 responses
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            useAuthStore.getState().logout()
            window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)

export default api
