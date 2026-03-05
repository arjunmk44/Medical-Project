import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell,
    LineChart, Line, Legend
} from 'recharts'

const COLORS = ['#14B8A6', '#3B82F6', '#F59E0B', '#EF4444']

const featureImportance = [
    { feature: 'Glucose', importance: 0.18 },
    { feature: 'HbA1c', importance: 0.15 },
    { feature: 'Systolic BP', importance: 0.13 },
    { feature: 'LDL', importance: 0.11 },
    { feature: 'BMI', importance: 0.10 },
    { feature: 'Cholesterol', importance: 0.09 },
    { feature: 'Creatinine', importance: 0.07 },
    { feature: 'Triglycerides', importance: 0.06 },
    { feature: 'Heart Rate', importance: 0.05 },
    { feature: 'eGFR', importance: 0.04 },
]

const heatmapLabels = ['SystBP', 'DiastBP', 'Glucose', 'HbA1c', 'LDL', 'HDL', 'BMI', 'Creat']
const heatmapMatrix = [
    [1.0, 0.8, 0.3, 0.2, 0.15, -0.1, 0.25, 0.1],
    [0.8, 1.0, 0.25, 0.18, 0.12, -0.08, 0.2, 0.08],
    [0.3, 0.25, 1.0, 0.85, 0.4, -0.3, 0.35, 0.2],
    [0.2, 0.18, 0.85, 1.0, 0.35, -0.25, 0.3, 0.18],
    [0.15, 0.12, 0.4, 0.35, 1.0, -0.6, 0.3, 0.15],
    [-0.1, -0.08, -0.3, -0.25, -0.6, 1.0, -0.2, -0.1],
    [0.25, 0.2, 0.35, 0.3, 0.3, -0.2, 1.0, 0.12],
    [0.1, 0.08, 0.2, 0.18, 0.15, -0.1, 0.12, 1.0],
]

// Kaplan-Meier survival data per PDM subgroup (Ref-2)
const survivalData = Array.from({ length: 24 }, (_, i) => ({
    month: i + 1,
    'Low Risk': Math.max(0, 1 - 0.005 * i - Math.random() * 0.01),
    'Moderate Risk': Math.max(0, 1 - 0.012 * i - Math.random() * 0.015),
    'High Risk': Math.max(0, 1 - 0.025 * i - Math.random() * 0.02),
    'Very High Risk': Math.max(0, 1 - 0.045 * i - Math.random() * 0.025),
}))

const modelMetrics = [
    { model: 'Random Forest', accuracy: 0.87, f1: 0.86, auc: 0.93 },
    { model: 'XGBoost', accuracy: 0.89, f1: 0.88, auc: 0.95 },
    { model: 'LightGBM', accuracy: 0.88, f1: 0.87, auc: 0.94 },
    { model: 'SVM', accuracy: 0.83, f1: 0.82, auc: 0.90 },
    { model: 'Logistic Reg', accuracy: 0.81, f1: 0.80, auc: 0.88 },
]

function getCorrelationColor(value: number): string {
    if (value > 0.5) return '#14B8A6'
    if (value > 0.2) return '#5EEAD4'
    if (value > -0.2) return '#f1f5f9'
    if (value > -0.5) return '#FCA5A5'
    return '#EF4444'
}

export default function AnalyticsPage() {
    return (
        <div>
            <div className="mb-6">
                <h1 className="text-2xl font-bold text-primary">Analytics</h1>
                <p className="text-sm text-gray-500 mt-1">Population-level ML analysis and model performance</p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
                {/* Global Feature Importance (SHAP) */}
                <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                    <h3 className="text-sm font-semibold text-primary mb-4">Global Feature Importance (SHAP)</h3>
                    <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={featureImportance} layout="vertical">
                            <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                            <XAxis type="number" stroke="#94a3b8" tick={{ fontSize: 11 }} />
                            <YAxis type="category" dataKey="feature" width={100} stroke="#94a3b8" tick={{ fontSize: 11 }} />
                            <Tooltip />
                            <Bar dataKey="importance" fill="#14B8A6" radius={[0, 4, 4, 0]} />
                        </BarChart>
                    </ResponsiveContainer>
                </div>

                {/* Correlation Heatmap */}
                <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                    <h3 className="text-sm font-semibold text-primary mb-4">Biomarker Correlation Heatmap</h3>
                    <div className="overflow-auto">
                        <table className="mx-auto">
                            <thead>
                                <tr>
                                    <th className="w-12"></th>
                                    {heatmapLabels.map(l => (
                                        <th key={l} className="text-xs text-gray-500 p-1 w-10 text-center" style={{ writingMode: 'vertical-rl' }}>{l}</th>
                                    ))}
                                </tr>
                            </thead>
                            <tbody>
                                {heatmapMatrix.map((row, i) => (
                                    <tr key={i}>
                                        <td className="text-xs text-gray-500 pr-2 text-right">{heatmapLabels[i]}</td>
                                        {row.map((val, j) => (
                                            <td key={j} className="p-0.5">
                                                <div
                                                    className="w-9 h-9 rounded flex items-center justify-center text-[10px] font-mono"
                                                    style={{ backgroundColor: getCorrelationColor(val), color: Math.abs(val) > 0.4 ? '#fff' : '#64748b' }}
                                                    title={`${heatmapLabels[i]} × ${heatmapLabels[j]}: ${val.toFixed(2)}`}
                                                >
                                                    {val.toFixed(1)}
                                                </div>
                                            </td>
                                        ))}
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            {/* Kaplan-Meier Survival Curves (Ref-2) */}
            <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100 mb-6">
                <h3 className="text-sm font-semibold text-primary mb-1">Kaplan-Meier Survival Curves — PDM Subgroups</h3>
                <p className="text-xs text-gray-400 mb-4">Ref-2 (PMC7028517): Survival analysis validating PDM patient stratification</p>
                <ResponsiveContainer width="100%" height={320}>
                    <LineChart data={survivalData}>
                        <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                        <XAxis dataKey="month" label={{ value: 'Months', position: 'insideBottom', offset: -5, fontSize: 11 }} stroke="#94a3b8" tick={{ fontSize: 11 }} />
                        <YAxis label={{ value: 'Survival Probability', angle: -90, position: 'insideLeft', fontSize: 11 }} domain={[0, 1]} stroke="#94a3b8" tick={{ fontSize: 11 }} />
                        <Tooltip />
                        <Legend wrapperStyle={{ fontSize: 11 }} />
                        <Line type="stepAfter" dataKey="Low Risk" stroke={COLORS[0]} strokeWidth={2} dot={false} />
                        <Line type="stepAfter" dataKey="Moderate Risk" stroke={COLORS[1]} strokeWidth={2} dot={false} />
                        <Line type="stepAfter" dataKey="High Risk" stroke={COLORS[2]} strokeWidth={2} dot={false} />
                        <Line type="stepAfter" dataKey="Very High Risk" stroke={COLORS[3]} strokeWidth={2} dot={false} />
                    </LineChart>
                </ResponsiveContainer>
            </div>

            {/* Model Performance */}
            <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                <h3 className="text-sm font-semibold text-primary mb-4">Model Performance Comparison</h3>
                <div className="overflow-auto">
                    <table className="w-full">
                        <thead>
                            <tr className="bg-gray-50/80">
                                <th className="px-4 py-2.5 text-left text-xs font-semibold text-gray-500">Model</th>
                                <th className="px-4 py-2.5 text-center text-xs font-semibold text-gray-500">Accuracy</th>
                                <th className="px-4 py-2.5 text-center text-xs font-semibold text-gray-500">F1 (weighted)</th>
                                <th className="px-4 py-2.5 text-center text-xs font-semibold text-gray-500">ROC-AUC</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-50">
                            {modelMetrics.map((m, i) => (
                                <tr key={i} className="hover:bg-gray-50/50">
                                    <td className="px-4 py-3 text-sm font-medium text-primary">{m.model}</td>
                                    <td className="px-4 py-3 text-sm text-center font-serif">{(m.accuracy * 100).toFixed(1)}%</td>
                                    <td className="px-4 py-3 text-sm text-center font-serif">{(m.f1 * 100).toFixed(1)}%</td>
                                    <td className="px-4 py-3 text-sm text-center">
                                        <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${m.auc > 0.93 ? 'bg-accent/10 text-accent' : m.auc > 0.90 ? 'bg-info/10 text-info' : 'bg-warning/10 text-warning'
                                            }`}>
                                            {(m.auc * 100).toFixed(1)}%
                                        </span>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    )
}
