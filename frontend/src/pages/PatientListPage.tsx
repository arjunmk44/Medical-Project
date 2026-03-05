import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { Search, Plus, ChevronRight } from 'lucide-react'
import api from '../api/client'

interface Patient {
    id: string
    mrn: string
    firstName: string
    lastName: string
    age: number
    sex: string
    latestRiskLabel: string | null
    latestRiskScore: number | null
}

const mockPatients: Patient[] = [
    { id: '1', mrn: 'MRN-A001', firstName: 'John', lastName: 'Smith', age: 58, sex: 'M', latestRiskLabel: 'At Risk', latestRiskScore: 0.72 },
    { id: '2', mrn: 'MRN-A002', firstName: 'Sarah', lastName: 'Johnson', age: 45, sex: 'F', latestRiskLabel: 'Healthy', latestRiskScore: 0.18 },
    { id: '3', mrn: 'MRN-A003', firstName: 'Michael', lastName: 'Williams', age: 67, sex: 'M', latestRiskLabel: 'Monitor Closely', latestRiskScore: 0.51 },
    { id: '4', mrn: 'MRN-A004', firstName: 'Emily', lastName: 'Brown', age: 34, sex: 'F', latestRiskLabel: 'Healthy', latestRiskScore: 0.12 },
    { id: '5', mrn: 'MRN-A005', firstName: 'Robert', lastName: 'Davis', age: 72, sex: 'M', latestRiskLabel: 'At Risk', latestRiskScore: 0.81 },
]

function RiskBadge({ label }: { label: string | null }) {
    if (!label) return <span className="text-xs text-gray-400">—</span>
    const colors: Record<string, string> = {
        'Healthy': 'bg-accent/10 text-accent',
        'Monitor Closely': 'bg-warning/10 text-warning',
        'At Risk': 'bg-danger/10 text-danger',
    }
    return (
        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${colors[label] || 'bg-gray-100 text-gray-600'}`}>
            <span className={`w-1.5 h-1.5 rounded-full mr-1.5 ${label === 'Healthy' ? 'bg-accent' : label === 'At Risk' ? 'bg-danger' : 'bg-warning'
                }`} />
            {label}
        </span>
    )
}

export default function PatientListPage() {
    const [search, setSearch] = useState('')
    const navigate = useNavigate()

    const filtered = mockPatients.filter(p =>
        `${p.firstName} ${p.lastName} ${p.mrn}`.toLowerCase().includes(search.toLowerCase())
    )

    return (
        <div>
            <div className="flex items-center justify-between mb-6">
                <div>
                    <h1 className="text-2xl font-bold text-primary">Patients</h1>
                    <p className="text-sm text-gray-500 mt-1">{mockPatients.length} registered patients</p>
                </div>
                <button className="flex items-center gap-2 bg-accent text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-accent-dark transition-all shadow-sm">
                    <Plus className="w-4 h-4" /> Add Patient
                </button>
            </div>

            {/* Search */}
            <div className="relative mb-6">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                <input
                    id="patient-search"
                    type="text"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    placeholder="Search by name or MRN..."
                    className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-lg focus:ring-2 focus:ring-accent/50 focus:border-accent outline-none text-sm"
                />
            </div>

            {/* Table */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                <table className="w-full">
                    <thead>
                        <tr className="bg-gray-50/80">
                            <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Patient</th>
                            <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">MRN</th>
                            <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Age / Sex</th>
                            <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Risk Status</th>
                            <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Score</th>
                            <th className="px-6 py-3"></th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-50">
                        {filtered.map((patient) => (
                            <tr
                                key={patient.id}
                                onClick={() => navigate(`/patients/${patient.id}`)}
                                className="hover:bg-gray-50/50 cursor-pointer transition-colors"
                            >
                                <td className="px-6 py-4">
                                    <p className="text-sm font-medium text-primary">{patient.firstName} {patient.lastName}</p>
                                </td>
                                <td className="px-6 py-4 text-sm text-gray-500 font-mono">{patient.mrn}</td>
                                <td className="px-6 py-4 text-sm text-gray-500">{patient.age} / {patient.sex}</td>
                                <td className="px-6 py-4"><RiskBadge label={patient.latestRiskLabel} /></td>
                                <td className="px-6 py-4 text-sm font-serif font-semibold text-primary">
                                    {patient.latestRiskScore != null ? patient.latestRiskScore.toFixed(2) : '—'}
                                </td>
                                <td className="px-6 py-4"><ChevronRight className="w-4 h-4 text-gray-300" /></td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    )
}
