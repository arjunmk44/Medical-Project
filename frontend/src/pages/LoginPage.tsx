import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { Activity, Eye, EyeOff } from 'lucide-react'
import api from '../api/client'

export default function LoginPage() {
    const [username, setUsername] = useState('')
    const [password, setPassword] = useState('')
    const [showPassword, setShowPassword] = useState(false)
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)
    const login = useAuthStore((s) => s.login)
    const navigate = useNavigate()

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        setError('')
        setLoading(true)

        try {
            const res = await api.post('/auth/login', { username, password })
            const { accessToken, username: user, role } = res.data.data
            login(accessToken, user, role)
            navigate('/dashboard')
        } catch (err: unknown) {
            setError('Invalid username or password')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="min-h-screen flex">
            {/* Left: Branding */}
            <div className="hidden lg:flex lg:w-1/2 bg-primary relative overflow-hidden items-center justify-center">
                {/* ECG SVG Animation */}
                <svg className="absolute inset-0 w-full h-full opacity-10" viewBox="0 0 1200 400">
                    <path
                        className="ecg-line"
                        d="M0,200 L200,200 L230,200 L250,100 L270,300 L290,150 L310,250 L330,200 L500,200 L530,200 L550,80 L570,320 L590,130 L610,270 L630,200 L800,200 L830,200 L850,90 L870,310 L890,140 L910,260 L930,200 L1200,200"
                        fill="none"
                        stroke="#14B8A6"
                        strokeWidth="3"
                    />
                </svg>

                <div className="relative z-10 text-center px-12">
                    <Activity className="w-16 h-16 text-accent mx-auto mb-6" />
                    <h1 className="text-4xl font-bold text-white mb-4">MedicalML</h1>
                    <p className="text-lg text-white/60">
                        Latent Health Intelligence Platform
                    </p>
                    <p className="text-sm text-white/40 mt-2">
                        ML-driven insights from routine checkup data
                    </p>
                </div>
            </div>

            {/* Right: Login Form */}
            <div className="flex-1 flex items-center justify-center p-8 bg-clinical">
                <div className="w-full max-w-md animate-slide-up">
                    <div className="lg:hidden flex items-center gap-2 mb-8">
                        <Activity className="w-8 h-8 text-accent" />
                        <span className="text-2xl font-bold text-primary">MedicalML</span>
                    </div>

                    <h2 className="text-2xl font-bold text-primary mb-1">Welcome back</h2>
                    <p className="text-sm text-gray-500 mb-8">Sign in to access the health intelligence platform</p>

                    {error && (
                        <div className="bg-danger/10 border border-danger/20 text-danger px-4 py-3 rounded-lg mb-6 text-sm">
                            {error}
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-5">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1.5">Username</label>
                            <input
                                id="login-username"
                                type="text"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                className="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:ring-2 focus:ring-accent/50 focus:border-accent outline-none transition-all"
                                placeholder="Enter username"
                                required
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1.5">Password</label>
                            <div className="relative">
                                <input
                                    id="login-password"
                                    type={showPassword ? 'text' : 'password'}
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    className="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:ring-2 focus:ring-accent/50 focus:border-accent outline-none transition-all pr-10"
                                    placeholder="Enter password"
                                    required
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                                >
                                    {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                                </button>
                            </div>
                        </div>

                        <button
                            id="login-submit"
                            type="submit"
                            disabled={loading}
                            className="w-full bg-accent hover:bg-accent-dark text-white py-2.5 rounded-lg font-medium transition-all duration-200 disabled:opacity-50 shadow-lg shadow-accent/25 hover:shadow-accent/40"
                        >
                            {loading ? (
                                <span className="flex items-center justify-center gap-2">
                                    <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                                    Signing in...
                                </span>
                            ) : (
                                'Sign In'
                            )}
                        </button>
                    </form>

                    <div className="mt-8 p-4 bg-gray-50 rounded-lg">
                        <p className="text-xs text-gray-500 font-medium mb-2">Demo Credentials</p>
                        <p className="text-xs text-gray-400">Admin: admin / admin123</p>
                    </div>
                </div>
            </div>
        </div>
    )
}
