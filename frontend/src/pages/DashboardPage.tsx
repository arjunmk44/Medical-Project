import { useQuery } from '@tanstack/react-query'
import { Users, AlertTriangle, Activity, TrendingUp } from 'lucide-react'
import {
    ScatterChart, Scatter, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
    BarChart, Bar, Cell
} from 'recharts'
import api from '../api/client'

const COLORS = ['#14B8A6', '#3B82F6', '#F59E0B', '#EF4444']

const mockClusterData = [
    { x: 1.2, y: 0.8, cluster: 0 }, { x: 0.8, y: 1.1, cluster: 0 }, { x: 1.5, y: 0.5, cluster: 0 },
    { x: -0.5, y: 1.5, cluster: 1 }, { x: -0.2, y: 1.8, cluster: 1 }, { x: -0.8, y: 1.2, cluster: 1 },
    { x: 2.1, y: -1.2, cluster: 2 }, { x: 1.8, y: -0.9, cluster: 2 }, { x: 2.5, y: -1.5, cluster: 2 },
    { x: -1.8, y: -0.9, cluster: 3 }, { x: -2.1, y: -1.2, cluster: 3 },
]

const mockRiskDist = [
    { label: 'Healthy', count: 65, color: '#14B8A6' },
    { label: 'Monitor', count: 30, color: '#F59E0B' },
    { label: 'At Risk', count: 25, color: '#EF4444' },
]

function KpiCard({ icon: Icon, label, value, color }: {
    icon: React.ElementType; label: string; value: string | number; color: string
}) {
    return (
        <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100 animate-slide-up">
            <div className="flex items-center justify-between mb-3">
                <div className={`p-2 rounded-lg ${color}`}>
                    <Icon className="w-5 h-5 text-white" />
                </div>
            </div>
            <p className="text-2xl font-bold text-primary">{value}</p>
            <p className="text-sm text-gray-500 mt-1">{label}</p>
        </div>
    )
}

export default function DashboardPage() {
    return (
        <div>
            <div className="mb-8">
                <h1 className="text-2xl font-bold text-primary">Dashboard</h1>
                <p className="text-sm text-gray-500 mt-1">Population health overview and analytics</p>
            </div>

            {/* KPI Strip */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
                <KpiCard icon={Users} label="Total Patients" value={120} color="bg-accent" />
                <KpiCard icon={AlertTriangle} label="At-Risk Count" value={25} color="bg-danger" />
                <KpiCard icon={Activity} label="Avg Risk Score" value="0.42" color="bg-info" />
                <KpiCard icon={TrendingUp} label="Active Alerts" value={8} color="bg-warning" />
            </div>

            {/* Charts Row */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
                {/* Cluster Map */}
                <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                    <h3 className="text-sm font-semibold text-primary mb-4">Population Cluster Map (PCA)</h3>
                    <ResponsiveContainer width="100%" height={300}>
                        <ScatterChart>
                            <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                            <XAxis type="number" dataKey="x" name="PC1" stroke="#94a3b8" tick={{ fontSize: 12 }} />
                            <YAxis type="number" dataKey="y" name="PC2" stroke="#94a3b8" tick={{ fontSize: 12 }} />
                            <Tooltip
                                content={({ payload }) => {
                                    if (payload && payload.length > 0) {
                                        const labels = ['Healthy', 'Monitor', 'At Risk', 'High Risk']
                                        const d = payload[0].payload
                                        return (
                                            <div className="bg-white px-3 py-2 rounded-lg shadow-lg border text-xs">
                                                <p className="font-medium">{labels[d.cluster]}</p>
                                                <p className="text-gray-500">PC1: {d.x}, PC2: {d.y}</p>
                                            </div>
                                        )
                                    }
                                    return null
                                }}
                            />
                            <Scatter data={mockClusterData} fill="#14B8A6">
                                {mockClusterData.map((entry, i) => (
                                    <Cell key={i} fill={COLORS[entry.cluster]} />
                                ))}
                            </Scatter>
                        </ScatterChart>
                    </ResponsiveContainer>
                    <div className="flex gap-4 mt-3 justify-center">
                        {['Healthy', 'Monitor', 'At Risk', 'High Risk'].map((l, i) => (
                            <div key={l} className="flex items-center gap-1.5 text-xs text-gray-500">
                                <div className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: COLORS[i] }} />
                                {l}
                            </div>
                        ))}
                    </div>
                </div>

                {/* Risk Distribution */}
                <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                    <h3 className="text-sm font-semibold text-primary mb-4">Risk Distribution</h3>
                    <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={mockRiskDist}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                            <XAxis dataKey="label" stroke="#94a3b8" tick={{ fontSize: 12 }} />
                            <YAxis stroke="#94a3b8" tick={{ fontSize: 12 }} />
                            <Tooltip />
                            <Bar dataKey="count" radius={[6, 6, 0, 0]}>
                                {mockRiskDist.map((entry, i) => (
                                    <Cell key={i} fill={entry.color} />
                                ))}
                            </Bar>
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            </div>

            {/* Alert Panel */}
            <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                <h3 className="text-sm font-semibold text-primary mb-4">Recent Alerts</h3>
                <div className="space-y-3">
                    {[
                        { severity: 'HIGH', msg: 'Patient MRN-A001: Glucose critically elevated', time: '2 hours ago' },
                        { severity: 'MEDIUM', msg: 'Patient MRN-A005: LDL above threshold', time: '5 hours ago' },
                        { severity: 'HIGH', msg: 'Patient MRN-A012: Multi-system risk detected', time: '1 day ago' },
                    ].map((alert, i) => (
                        <div key={i} className={`flex items-start gap-3 p-3 rounded-lg ${alert.severity === 'HIGH' ? 'bg-danger/5 border border-danger/10' : 'bg-warning/5 border border-warning/10'
                            }`}>
                            <AlertTriangle className={`w-4 h-4 mt-0.5 flex-shrink-0 ${alert.severity === 'HIGH' ? 'text-danger' : 'text-warning'
                                }`} />
                            <div>
                                <p className="text-sm text-primary">{alert.msg}</p>
                                <p className="text-xs text-gray-400 mt-1">{alert.time}</p>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    )
}
