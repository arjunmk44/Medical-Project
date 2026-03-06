"""
Medical ML Platform — Python ML Microservice (FastAPI).

This is the main application file for the Machine Learning microservice that
provides health analytics for the Medical ML Platform. It runs as a standalone
service (default: port 8001) and is called by the Java backend via HTTP.

Architecture (Ref-1 Six-Layer Pipeline):
    Layer 1: Data Ingestion       — Handled by Java backend (IngestService)
    Layer 2: Preprocessing        — pipeline/preprocessing.py
    Layer 3: Dimensionality       — pipeline/dimensionality.py (PCA + Autoencoder)
    Layer 4: Clustering           — pipeline/clustering/ (K-Means, Hierarchical, DBSCAN, GMM, LDA, PDM)
             Supervised           — pipeline/supervised/ensemble.py (RF, XGBoost, LightGBM, SVM, LogReg)
    Layer 5: Interpretability     — pipeline/interpretability/ (SHAP, Permutation Importance)
    Layer 6: Risk Scoring         — pipeline/risk_scoring.py

Endpoints:
    GET  /health         — Health check
    GET  /ml/model-info  — Model metadata and pipeline info
    POST /ml/analyze     — Full ML analysis pipeline
    POST /ml/retrain     — Trigger model retraining

Dependencies:
    FastAPI, Uvicorn, Pydantic, NumPy, Scikit-learn, Structlog
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import Optional, Dict, Any
import numpy as np
import structlog
import uvicorn

# Import all pipeline modules — each handles one layer of the ML pipeline
from pipeline.preprocessing import preprocess_patient_data
from pipeline.dimensionality import run_dimensionality_reduction
from pipeline.clustering.kmeans_cluster import run_kmeans
from pipeline.clustering.hierarchical import run_hierarchical
from pipeline.clustering.dbscan_cluster import run_dbscan
from pipeline.clustering.gmm import run_gmm
from pipeline.clustering.lda_model import run_lda
from pipeline.clustering.pdm_model import run_pdm
from pipeline.supervised.ensemble import run_ensemble_prediction
from pipeline.interpretability.shap_explainer import compute_shap_values
from pipeline.interpretability.permutation_imp import compute_permutation_importance
from pipeline.risk_scoring import compute_risk_score
from pipeline.survival_analysis import generate_survival_curves

# Configure structured logging for consistent JSON-formatted log output
logger = structlog.get_logger()

# ─── FastAPI Application Setup ────────────────────────────────────────────────

app = FastAPI(
    title="Medical ML Service",
    description="Machine Learning microservice for patient health analysis",
    version="1.0.0",
)

# Enable CORS to allow the Java backend (port 8080) and frontend (port 3000) to call this service
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],           # Allow all origins (restrict in production)
    allow_credentials=True,
    allow_methods=["*"],           # Allow all HTTP methods
    allow_headers=["*"],           # Allow all headers
)


# ─── Request/Response Models ──────────────────────────────────────────────────

class PatientData(BaseModel):
    """
    Pydantic model for the patient data request body.

    All biomarker fields are optional — patients may not have every test performed.
    Default values are set to population averages where applicable.

    Fields:
        patient_id / record_id: Identifiers from the Java backend.
        age, sex: Demographics used for age/sex correction in PDM model.
        systolic_bp ... sleep_hours: Individual biomarker measurements.
    """
    patient_id: str = ""
    record_id: str = ""
    age: Optional[int] = None
    sex: Optional[str] = None

    # Cardiovascular biomarkers
    systolic_bp: Optional[float] = Field(default=120, description="Systolic blood pressure in mmHg")
    diastolic_bp: Optional[float] = Field(default=80, description="Diastolic blood pressure in mmHg")
    heart_rate: Optional[float] = Field(default=72, description="Resting heart rate in bpm")

    # Metabolic panel
    glucose: Optional[float] = Field(default=100, description="Fasting blood glucose in mg/dL")
    hba1c: Optional[float] = Field(default=5.5, description="Glycated hemoglobin percentage")
    total_cholesterol: Optional[float] = Field(default=200, description="Total cholesterol in mg/dL")
    ldl: Optional[float] = Field(default=100, description="LDL cholesterol in mg/dL")
    hdl: Optional[float] = Field(default=50, description="HDL cholesterol in mg/dL")
    triglycerides: Optional[float] = Field(default=150, description="Triglycerides in mg/dL")

    # Liver function
    alt: Optional[float] = Field(default=25, description="Alanine aminotransferase in U/L")
    ast: Optional[float] = Field(default=25, description="Aspartate aminotransferase in U/L")
    alp: Optional[float] = Field(default=70, description="Alkaline phosphatase in U/L")

    # Kidney function
    creatinine: Optional[float] = Field(default=1.0, description="Serum creatinine in mg/dL")
    bun: Optional[float] = Field(default=15, description="Blood urea nitrogen in mg/dL")
    egfr: Optional[float] = Field(default=90, description="Estimated GFR in mL/min/1.73m²")

    # Anthropometrics
    height_cm: Optional[float] = Field(default=170, description="Height in centimeters")
    weight_kg: Optional[float] = Field(default=70, description="Weight in kilograms")
    bmi: Optional[float] = Field(default=24, description="Body Mass Index")
    waist_cm: Optional[float] = Field(default=85, description="Waist circumference in cm")
    hip_cm: Optional[float] = Field(default=95, description="Hip circumference in cm")

    # Lifestyle factors
    smoking_status: Optional[str] = Field(default="NEVER", description="NEVER, FORMER, or CURRENT")
    alcohol_units: Optional[float] = Field(default=2, description="Weekly alcohol units")
    activity_level: Optional[str] = Field(default="MODERATE", description="Activity level")
    sleep_hours: Optional[float] = Field(default=7, description="Average sleep hours per day")


# ─── Endpoints ────────────────────────────────────────────────────────────────

@app.get("/health")
async def health_check():
    """
    Health check endpoint.
    Returns a simple status message to confirm the service is running.
    Used by Docker/Kubernetes health probes and the backend's connectivity check.
    """
    return {"status": "healthy", "service": "ml-service"}


@app.get("/ml/model-info")
async def model_info():
    """
    Model metadata endpoint.
    Returns information about the ML pipeline, models used, and references.
    Used by the VisualizationController for the survival analysis fallback.
    """
    return {
        "models": {
            "supervised": ["random_forest", "xgboost", "lightgbm", "svm", "logistic_regression"],
            "clustering": ["kmeans", "hierarchical", "dbscan", "gmm"],
            "topic_models": ["lda", "pdm"],
            "dimensionality": ["pca", "autoencoder"],
            "interpretability": ["shap", "permutation_importance"],
        },
        "references": {
            "ref_1": "Custom Six-Layer Pipeline",
            "ref_2": "PMC7028517 (Wang et al.) — LDA + Poisson-Dirichlet Model",
        },
        "version": "1.0.0",
    }


@app.post("/ml/analyze")
async def analyze_patient(data: PatientData):
    """
    Full ML analysis pipeline — the main endpoint called by the Java backend.

    Executes all 5 remaining pipeline layers in sequence:

    1. PREPROCESSING (Layer 2):
       - Converts raw biomarker values into a normalized feature vector.
       - Imputes missing values, detects outliers, encodes categoricals.
       - Engineers derived features (ratios, composite scores).

    2. DIMENSIONALITY REDUCTION (Layer 3):
       - Applies PCA for linear projection.
       - Applies autoencoder for non-linear embedding.
       - Reduces the feature space for downstream clustering.

    3. CLUSTERING + SUPERVISED PREDICTION (Layer 4):
       - K-Means, Hierarchical, DBSCAN, GMM: traditional clustering.
       - LDA: Topic modeling treating patients as "documents" (Ref-2).
       - PDM: Poisson-Dirichlet Model with age/sex correction (Ref-2).
       - Ensemble: Soft-voting across RF, XGBoost, LightGBM, SVM, LogReg.

    4. INTERPRETABILITY (Layer 5):
       - SHAP: Per-feature contribution to the prediction.
       - Permutation Importance: Feature importance with bootstrap CIs.

    5. RISK SCORING (Layer 6):
       - Combines all ML outputs into a single risk score (0.0–1.0).
       - Assigns a health label (Healthy / Monitor Closely / At Risk).
       - Generates early warning alerts for elevated values.

    Args:
        data: PatientData model with all biomarker and demographic fields.

    Returns:
        Comprehensive dict with results from every pipeline layer,
        plus the final health_label, risk_score, and warning alerts.
    """
    try:
        logger.info("analyze_start", patient_id=data.patient_id, record_id=data.record_id)

        # ─── Layer 2: Preprocessing ───────────────────────────────────────
        # Convert the Pydantic model to a dict and send to the preprocessing pipeline
        raw_data = data.model_dump()
        preprocessing_result = preprocess_patient_data(raw_data)
        features = preprocessing_result["features"]           # Normalized numpy array
        feature_names = preprocessing_result["feature_names"]  # Corresponding feature names

        # ─── Layer 3: Dimensionality Reduction ────────────────────────────
        # PCA + autoencoder to create compact representations
        dim_result = run_dimensionality_reduction(features)

        # ─── Layer 4a: Clustering (Unsupervised) ─────────────────────────
        # Run multiple clustering algorithms for robustness
        kmeans_result = run_kmeans(features)                               # K-Means with heuristic cluster assignment
        hierarchical_result = run_hierarchical(features)                   # Agglomerative (Ward linkage)
        dbscan_result = run_dbscan(features)                               # Density-based (DBSCAN)
        gmm_result = run_gmm(features)                                     # Gaussian Mixture Models
        lda_result = run_lda(features, feature_names)                      # LDA topic modeling (Ref-2)
        pdm_result = run_pdm(features, feature_names, age=data.age, sex=data.sex)  # PDM with age/sex correction (Ref-2)

        # ─── Layer 4b: Supervised Prediction ──────────────────────────────
        # Ensemble of 5 classifiers for health label prediction
        supervised_result = run_ensemble_prediction(features, feature_names)

        # ─── Layer 5: Interpretability ────────────────────────────────────
        # Explain which features drive the predictions
        shap_result = compute_shap_values(features, feature_names)
        perm_result = compute_permutation_importance(features, feature_names)

        # ─── Layer 6: Risk Scoring ────────────────────────────────────────
        # Combine all ML outputs into a final risk score and health label
        risk_result = compute_risk_score(
            supervised_result=supervised_result,
            clustering_result=kmeans_result,
            features=features,
            feature_names=feature_names,
            pca_result=dim_result,
        )

        # ─── Survival Analysis (Ref-2) ───────────────────────────────────
        # Generate Kaplan-Meier survival curves for this patient's subgroup
        survival_result = generate_survival_curves(
            cluster_id=kmeans_result["cluster_id"],
            age=data.age,
            sex=data.sex,
        )

        logger.info("analyze_complete",
                     patient_id=data.patient_id,
                     health_label=risk_result["health_label"],
                     risk_score=risk_result["risk_score"])

        # ─── Assemble Final Response ──────────────────────────────────────
        # Combines results from all pipeline layers into a single JSON response
        return {
            # Top-level results (stored in MlAnalysisResult by the backend)
            "health_label": risk_result["health_label"],
            "risk_score": risk_result["risk_score"],
            "cluster_id": kmeans_result["cluster_id"],
            "cluster_label": kmeans_result["cluster_label"],
            "warnings": risk_result.get("warnings", []),

            # Detailed per-layer results
            "preprocessing": preprocessing_result.get("summary", {}),
            "dimensionality_reduction": dim_result,
            "clustering": {
                "kmeans": kmeans_result,
                "hierarchical": hierarchical_result,
                "dbscan": dbscan_result,
                "gmm": gmm_result,
            },
            "topic_models": {
                "lda": lda_result,
                "pdm": pdm_result,
            },
            "supervised": supervised_result,
            "interpretability": {
                "shap": shap_result,
                "permutation_importance": perm_result,
            },
            "risk_scoring": risk_result,
            "survival_analysis": survival_result,

            # Data passed back for storage in the database
            "lda_topic_distribution": lda_result.get("topic_distribution"),
            "pdm_topic_distribution": pdm_result.get("topic_distribution"),
            "pca_components": dim_result.get("pca", {}),
            "shap_values": shap_result.get("feature_contributions", []),
        }

    except Exception as e:
        logger.error("analyze_failed", error=str(e), patient_id=data.patient_id)
        raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")


@app.post("/ml/retrain")
async def retrain():
    """
    Trigger model retraining on the full dataset.

    Currently a stub for local development. In production, this endpoint would:
    1. Pull all patient data from the database.
    2. Split into train/validation/test sets.
    3. Retrain all 5 supervised models + re-fit clustering.
    4. Evaluate and save model artifacts to /app/models/.
    5. Update model versioning metadata.

    Returns:
        Status message indicating retraining has been initiated.
    """
    logger.info("retrain_initiated")
    return {"status": "retraining_started", "message": "Model retraining initiated (stub for local dev)"}


# ─── Application Startup ─────────────────────────────────────────────────────

if __name__ == "__main__":
    """
    Start the FastAPI server with Uvicorn.

    - host="0.0.0.0": Listen on all network interfaces (required for Docker).
    - port=8001: Default port for the ML service (Java backend connects here).
    """
    uvicorn.run(app, host="0.0.0.0", port=8001)