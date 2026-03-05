import { useState, useCallback } from 'react'
import { useDropzone } from 'react-dropzone'
import { Upload, FileText, CheckCircle, Loader, AlertCircle } from 'lucide-react'
import api from '../api/client'

type StepStatus = 'pending' | 'active' | 'complete' | 'error'

const STEPS = ['Upload', 'Parse', 'Preprocess', 'ML Analysis', 'Results']

export default function UploadAnalysisPage() {
    const [currentStep, setCurrentStep] = useState(0)
    const [file, setFile] = useState<File | null>(null)
    const [status, setStatus] = useState<'idle' | 'uploading' | 'processing' | 'complete' | 'error'>('idle')
    const [result, setResult] = useState<Record<string, unknown> | null>(null)
    const [error, setError] = useState('')

    const onDrop = useCallback((acceptedFiles: File[]) => {
        if (acceptedFiles.length > 0) {
            setFile(acceptedFiles[0])
            setStatus('idle')
            setError('')
        }
    }, [])

    const { getRootProps, getInputProps, isDragActive } = useDropzone({
        onDrop,
        accept: {
            'text/csv': ['.csv'],
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': ['.xlsx'],
            'application/json': ['.json'],
        },
        maxFiles: 1,
    })

    const handleAnalyze = async () => {
        if (!file) return
        setStatus('uploading')
        setCurrentStep(1)
        setError('')

        try {
            const formData = new FormData()
            formData.append('file', file)

            setCurrentStep(1) // Parse
            const uploadRes = await api.post('/ingest/upload', formData, {
                headers: { 'Content-Type': 'multipart/form-data' },
            })

            setCurrentStep(2) // Preprocess
            const recordIds = uploadRes.data?.data?.recordIds

            if (recordIds && recordIds.length > 0) {
                setCurrentStep(3) // ML Analysis
                setStatus('processing')

                const mlRes = await api.post(`/ml/analyze/${recordIds[0]}`)
                setResult(mlRes.data?.data || mlRes.data)

                setCurrentStep(4) // Results
                setStatus('complete')
            } else {
                setCurrentStep(4)
                setStatus('complete')
                setResult(uploadRes.data?.data || {})
            }
        } catch (err: unknown) {
            setStatus('error')
            setError('Analysis failed. Please check the file format and try again.')
        }
    }

    return (
        <div>
            <div className="mb-6">
                <h1 className="text-2xl font-bold text-primary">Upload & Analyze</h1>
                <p className="text-sm text-gray-500 mt-1">Upload checkup data files for ML analysis</p>
            </div>

            {/* Step Progress */}
            <div className="flex items-center justify-between mb-8 max-w-2xl mx-auto">
                {STEPS.map((step, i) => {
                    let stepStatus: StepStatus = 'pending'
                    if (i < currentStep) stepStatus = 'complete'
                    else if (i === currentStep && status !== 'idle') stepStatus = 'active'
                    if (status === 'error' && i === currentStep) stepStatus = 'error'

                    return (
                        <div key={step} className="flex items-center">
                            <div className="flex flex-col items-center">
                                <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-medium transition-all ${stepStatus === 'complete' ? 'bg-accent text-white' :
                                        stepStatus === 'active' ? 'bg-accent/20 text-accent border-2 border-accent' :
                                            stepStatus === 'error' ? 'bg-danger/20 text-danger border-2 border-danger' :
                                                'bg-gray-100 text-gray-400'
                                    }`}>
                                    {stepStatus === 'complete' ? <CheckCircle className="w-4 h-4" /> :
                                        stepStatus === 'active' ? <Loader className="w-4 h-4 animate-spin" /> :
                                            stepStatus === 'error' ? <AlertCircle className="w-4 h-4" /> :
                                                i + 1}
                                </div>
                                <span className="text-xs text-gray-500 mt-1.5">{step}</span>
                            </div>
                            {i < STEPS.length - 1 && (
                                <div className={`w-12 h-0.5 mx-1 ${i < currentStep ? 'bg-accent' : 'bg-gray-200'}`} />
                            )}
                        </div>
                    )
                })}
            </div>

            {/* Dropzone */}
            <div className="max-w-2xl mx-auto mb-6">
                <div
                    {...getRootProps()}
                    className={`border-2 border-dashed rounded-xl p-12 text-center transition-all cursor-pointer ${isDragActive ? 'border-accent bg-accent/5' :
                            file ? 'border-accent/50 bg-accent/5' :
                                'border-gray-200 hover:border-accent/40 hover:bg-gray-50'
                        }`}
                >
                    <input {...getInputProps()} />
                    <Upload className={`w-10 h-10 mx-auto mb-3 ${file ? 'text-accent' : 'text-gray-300'}`} />
                    {file ? (
                        <div>
                            <p className="text-sm font-medium text-primary">{file.name}</p>
                            <p className="text-xs text-gray-400 mt-1">{(file.size / 1024).toFixed(1)} KB</p>
                        </div>
                    ) : (
                        <div>
                            <p className="text-sm text-gray-600">Drag & drop a file here, or click to browse</p>
                            <p className="text-xs text-gray-400 mt-1">Supports CSV, Excel (.xlsx), JSON</p>
                        </div>
                    )}
                </div>
            </div>

            {/* Analyze Button */}
            {file && status !== 'complete' && (
                <div className="flex justify-center mb-6">
                    <button
                        id="analyze-button"
                        onClick={handleAnalyze}
                        disabled={status === 'uploading' || status === 'processing'}
                        className="bg-accent hover:bg-accent-dark text-white px-8 py-2.5 rounded-lg font-medium transition-all disabled:opacity-50 shadow-lg shadow-accent/25"
                    >
                        {status === 'uploading' || status === 'processing' ? (
                            <span className="flex items-center gap-2">
                                <Loader className="w-4 h-4 animate-spin" /> Processing...
                            </span>
                        ) : 'Run Analysis'}
                    </button>
                </div>
            )}

            {/* Error */}
            {error && (
                <div className="max-w-2xl mx-auto bg-danger/10 border border-danger/20 text-danger px-4 py-3 rounded-lg text-sm mb-6">
                    {error}
                </div>
            )}

            {/* Results */}
            {status === 'complete' && result && (
                <div className="max-w-2xl mx-auto bg-white rounded-xl p-6 shadow-sm border border-gray-100 animate-slide-up">
                    <div className="flex items-center gap-2 mb-4">
                        <CheckCircle className="w-5 h-5 text-accent" />
                        <h3 className="text-sm font-semibold text-primary">Analysis Complete</h3>
                    </div>
                    <pre className="text-xs bg-gray-50 p-4 rounded-lg overflow-auto max-h-64 font-mono text-gray-600">
                        {JSON.stringify(result, null, 2)}
                    </pre>
                </div>
            )}
        </div>
    )
}
