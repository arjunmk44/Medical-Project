import { useParams } from 'react-router-dom'
import {
    RadarChart, Radar, PolarGrid, PolarAngleAxis, PolarRadiusAxis,
    LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
    BarChart, Bar, Cell
} from 'recharts'
import { AlertTriangle, ArrowLeft } from 'lucide-react'
import { useNavigate } from 'react-router-dom'

const COLORS = ['#14B8A6', '#3B82F6', '#F59E0B', '#EF4444']

// Mock data for patient profile
const mockBiomarkerRadar = [
    { category: 'Cardiovascular', value: 72, fullMark: 100 },
    { category: 'Metabolic', value: 55, fullMark: 100 },
    { category: 'Liver', value: 88, fullMark: 100 },
    { category: 'Kidney', value: 92, fullMark: 100 },
    { category: 'Anthropometric', value: 45, fullMark: 100 },
    { category: 'Lifestyle', value: 60, fullMark: 100 },
]

const mockRiskTrend = [
    { date: 'Jan', score: 0.35 }, { date: 'Mar', score: 0.42 },
    { date: 'May', score: 0.38 }, { date: 'Jul', score: 0.51 },
    { date: 'Sep', score: 0.55 }, { date: 'Nov', score: 0.72 },
]

const mockLdaTopics = [
    { topic: 'Cardiovascular', probability: 0.35 },
    { topic: 'Metabolic', probability: 0.28 },
    { topic: 'Healthy Aging', probability: 0.22 },
    { topic: 'Mixed Risk', probability: 0.15 },
]

const mockPdmTopics = [
    { topic: 'CV Excess Risk', probability: 0.42 },
    { topic: 'Metabolic Excess', probability: 0.25 },
    { topic: 'Baseline', probability: 0.20 },
    { topic: 'Organ Stress', probability: 0.13 },
]

const mockShapFeatures = [
    { feature: 'Glucose', value: 0.15, direction: 'risk' },
    { feature: 'HbA1c', value: 0.12, direction: 'risk' },
    { feature: 'Systolic BP', value: 0.10, direction: 'risk' },
    { feature: 'HDL', value: -0.08, direction: 'protect' },
    { feature: 'BMI', value: 0.07, direction: 'risk' },
    { feature: 'Activity', value: -0.06, direction: 'protect' },
    { feature: 'LDL', value: 0.05, direction: 'risk' },
    { feature: 'Sleep', value: -0.04, direction: 'protect' },
]

function RiskGauge({ score }: { score: number }) {
    const angle = score * 180
    const color = score < 0.3 ? '#14B8A6' : score < 0.6 ? '#F59E0B' : '#EF4444'
    const label = score < 0.3 ? 'Healthy' : score < 0.6 ? 'Monitor' : 'At Risk'

    return (
        <div className="flex flex-col items-center">
            <svg width="160" height="90" viewBox="0 0 160 90">
                {/* Background arc */}
                <path d="M 10 80 A 70 70 0 0 1 150 80" fill="none" stroke="#e2e8f0" strokeWidth="12" strokeLinecap="round" />
                {/* Filled arc */}
                <path
                    d="M 10 80 A 70 70 0 0 1 150 80"
                    fill="none"
                    stroke={color}
                    strokeWidth="12"
                    strokeLinecap="round"
                    strokeDasharray={`${angle / 180 * 220} 220`}
                    className="transition-all duration-1000 ease-out"
                />
            </svg>
            <p className="text-3xl font-bold mt-[-10px]" style={{ color }}>{(score * 100).toFixed(0)}</p>
            <p className="text-sm text-gray-500">{label}</p>
        </div>
    )
}

export default function PatientProfilePage() {
    const { id } = useParams()
    const navigate = useNavigate()

    return (
        <div>
            {/* Header */}
            <div className="flex items-center gap-4 mb-6">
                <button onClick={() => navigate('/patients')} className="p-2 hover:bg-gray-100 rounded-lg">
                    <ArrowLeft className="w-5 h-5 text-gray-500" />
                </button>
                <div className="flex-1">
                    <h1 className="text-2xl font-bold text-primary">John Smith</h1>
                    <p className="text-sm text-gray-500">MRN: MRN-A001 · Age: 58 · Male</p>
                </div>
                <RiskGauge score={0.72} />
            </div>

            {/* Charts Grid */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
                {/* Biomarker Radar */}
                <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                    <h3 className="text-sm font-semibold text-primary mb-4">Biomarker Overview</h3>
                    <ResponsiveContainer width="100%" height={280}>
                        <RadarChart data={mockBiomarkerRadar}>
                            <PolarGrid stroke="#e2e8f0" />
                            <PolarAngleAxis dataKey="category" tick={{ fontSize: 11, fill: '#64748b' }} />
                            <PolarRadiusAxis angle={30} domain={[0, 100]} tick={{ fontSize: 10 }} />
                            <Radar dataKey="value" stroke="#14B8A6" fill="#14B8A6" fillOpacity={0.2} strokeWidth={2} />
                        </RadarChart>
                    </ResponsiveContainer>
                </div>

                {/* Risk Trend */}
                <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                    <h3 className="text-sm font-semibold text-primary mb-4">Risk Score Trend</h3>
                    <ResponsiveContainer width="100%" height={280}>
                        <LineChart data={mockRiskTrend}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                            <XAxis dataKey="date" stroke="#94a3b8" tick={{ fontSize: 12 }} />
                            <YAxis domain={[0, 1]} stroke="#94a3b8" tick={{ fontSize: 12 }} />
                            <Tooltip />
                            <Line type="monotone" dataKey="score" stroke="#EF4444" strokeWidth={2} dot={{ fill: '#EF4444', r: 4 }} />
                        </LineChart>
                    </ResponsiveContainer>
                </div>
            </div>

            {/* LDA vs PDM Topic Distribution */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
                <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                    <h3 className="text-sm font-semibold text-primary mb-1">LDA Topic Distribution</h3>
                    <p className="text-xs text-gray-400 mb-4">Ref-2: Standard Dirichlet-Multinomial clustering</p>
                    <ResponsiveContainer width="100%" height={200}>
                        <BarChart data={mockLdaTopics} layout="vertical">
                            <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                            <XAxis type="number" domain={[0, 0.5]} stroke="#94a3b8" tick={{ fontSize: 11 }} />
                            <YAxis type="category" dataKey="topic" width={120} stroke="#94a3b8" tick={{ fontSize: 11 }} />
                            <Tooltip />
                            <Bar dataKey="probability" radius={[0, 4, 4, 0]}>
                                {mockLdaTopics.map((_, i) => <Cell key={i} fill={COLORS[i]} />)}
                            </Bar>
                        </BarChart>
                    </ResponsiveContainer>
                </div>

                <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                    <h3 className="text-sm font-semibold text-primary mb-1">PDM Topic Distribution</h3>
                    <p className="text-xs text-gray-400 mb-4">Ref-2: Poisson-corrected (age/sex adjusted)</p>
                    <ResponsiveContainer width="100%" height={200}>
                        <BarChart data={mockPdmTopics} layout="vertical">
                            <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                            <XAxis type="number" domain={[0, 0.5]} stroke="#94a3b8" tick={{ fontSize: 11 }} />
                            <YAxis type="category" dataKey="topic" width={120} stroke="#94a3b8" tick={{ fontSize: 11 }} />
                            <Tooltip />
                            <Bar dataKey="probability" radius={[0, 4, 4, 0]}>
                                {mockPdmTopics.map((_, i) => <Cell key={i} fill={COLORS[i]} />)}
                            </Bar>
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            </div>

            {/* SHAP Waterfall */}
            <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100 mb-6">
                <h3 className="text-sm font-semibold text-primary mb-1">SHAP Feature Contributions</h3>
                <p className="text-xs text-gray-400 mb-4">Individual prediction explanation — what drives this patient's risk score</p>
                <div className="space-y-2">
                    {mockShapFeatures.map((f, i) => (
                        <div key={i} className="flex items-center gap-3">
                            <span className="text-xs text-gray-600 w-24 text-right">{f.feature}</span>
                            <div className="flex-1 h-6 bg-gray-50 rounded relative">
                                <div
                                    className={`h-full rounded transition-all duration-500 ${f.direction === 'risk' ? 'bg-danger/30' : 'bg-accent/30'
                                        }`}
                                    style={{
                                        width: `${Math.abs(f.value) * 500}%`,
                                        marginLeft: f.direction === 'protect' ? `${50 - Math.abs(f.value) * 250}%` : '50%',
                                    }}
                                />
                            </div>
                            <span className={`text-xs font-mono w-12 ${f.direction === 'risk' ? 'text-danger' : 'text-accent'}`}>
                                {f.value > 0 ? '+' : ''}{f.value.toFixed(2)}
                            </span>
                        </div>
                    ))}
                </div>
                <div className="flex justify-center gap-6 mt-4 text-xs text-gray-500">
                    <div className="flex items-center gap-1.5"><div className="w-3 h-3 rounded bg-danger/30" /> Increases risk</div>
                    <div className="flex items-center gap-1.5"><div className="w-3 h-3 rounded bg-accent/30" /> Decreases risk</div>
                </div>
            </div>

            {/* Warnings */}
            <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                <h3 className="text-sm font-semibold text-primary mb-4">Early Warnings</h3>
                <div className="space-y-3">
                    <div className="flex items-start gap-3 p-3 rounded-lg bg-danger/5 border border-danger/10">
                        <AlertTriangle className="w-4 h-4 text-danger mt-0.5" />
                        <div>
                            <p className="text-sm font-medium text-primary">Glucose critically elevated</p>
                            <p className="text-xs text-gray-500 mt-1">Fasting glucose 2.1 SD above population mean. Schedule comprehensive metabolic evaluation.</p>
                        </div>
                    </div>
                    <div className="flex items-start gap-3 p-3 rounded-lg bg-warning/5 border border-warning/10">
                        <AlertTriangle className="w-4 h-4 text-warning mt-0.5" />
                        <div>
                            <p className="text-sm font-medium text-primary">LDL above threshold</p>
                            <p className="text-xs text-gray-500 mt-1">Consider statin therapy evaluation and lifestyle modification counseling.</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}
