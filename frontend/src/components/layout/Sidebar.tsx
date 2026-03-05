import { NavLink, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'
import {
    LayoutDashboard, Users, Upload, BarChart3, LogOut, Activity, Menu, X
} from 'lucide-react'
import { useState } from 'react'

const navItems = [
    { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/patients', icon: Users, label: 'Patients' },
    { to: '/upload', icon: Upload, label: 'Upload & Analyze' },
    { to: '/analytics', icon: BarChart3, label: 'Analytics' },
]

export default function Sidebar() {
    const [collapsed, setCollapsed] = useState(false)
    const logout = useAuthStore((s) => s.logout)
    const username = useAuthStore((s) => s.username)
    const role = useAuthStore((s) => s.role)
    const navigate = useNavigate()

    const handleLogout = () => {
        logout()
        navigate('/login')
    }

    return (
        <aside className={`${collapsed ? 'w-16' : 'w-64'} bg-primary text-white flex flex-col transition-all duration-300 ease-out`}>
            {/* Header */}
            <div className="flex items-center justify-between p-4 border-b border-white/10">
                {!collapsed && (
                    <div className="flex items-center gap-2">
                        <Activity className="w-6 h-6 text-accent" />
                        <span className="font-semibold text-sm">MedicalML</span>
                    </div>
                )}
                <button onClick={() => setCollapsed(!collapsed)} className="p-1 hover:bg-white/10 rounded">
                    {collapsed ? <Menu className="w-5 h-5" /> : <X className="w-5 h-5" />}
                </button>
            </div>

            {/* Navigation */}
            <nav className="flex-1 py-4">
                {navItems.map((item) => (
                    <NavLink
                        key={item.to}
                        to={item.to}
                        className={({ isActive }) =>
                            `flex items-center gap-3 px-4 py-3 mx-2 rounded-lg transition-all duration-200 ${isActive
                                ? 'bg-accent/20 text-accent'
                                : 'text-white/70 hover:bg-white/5 hover:text-white'
                            }`
                        }
                    >
                        <item.icon className="w-5 h-5 flex-shrink-0" />
                        {!collapsed && <span className="text-sm font-medium">{item.label}</span>}
                    </NavLink>
                ))}
            </nav>

            {/* User Info */}
            <div className="border-t border-white/10 p-4">
                {!collapsed && (
                    <div className="mb-3">
                        <p className="text-sm font-medium">{username}</p>
                        <p className="text-xs text-white/50">{role}</p>
                    </div>
                )}
                <button
                    onClick={handleLogout}
                    className="flex items-center gap-2 text-white/60 hover:text-white text-sm w-full"
                >
                    <LogOut className="w-4 h-4" />
                    {!collapsed && 'Logout'}
                </button>
            </div>
        </aside>
    )
}
