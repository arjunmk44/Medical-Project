"""
Medical ML Platform — FastAPI ML Microservice
Ref-1: 8-layer architecture (Layers 2–6)
Ref-2: LDA/PDM patient subgroup discovery (PMC7028517)
"""

import structlog
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from typing import Optional
import numpy as np
import json
import os
import traceback

from pipeline.preprocessing import preprocess_biomarkers
from pipeline.dimensionality import run_pca, run_autoencoder
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
from pipeline.survival_analysis import compute_survival_data

logger = structlog.get_logger()

app = FastAPI(
    title="Medical ML Microservice",
    description="ML pipeline for latent health intelligence (Ref-1 Layers 2–6, Ref-2 LDA/PDM)",
    version="1.0.0",
)


class BiomarkerInput(BaseModel):
    """Input biomarker data for a single patient record."""
    patient_id: str
    record_id: str
    age: Optional[int] = None
    sex: Optional[str] = None
    # Vital & Cardiovascular
    systolic_bp: Optional[float] = None
    diastolic_bp: Optional[float] = None
    heart_rate: Optional[float] = None
    # Metabolic Panel
    glucose: Optional[float] = None
    hba1c: Optional[float] = None
    total_cholesterol: Optional[float] = None
    ldl: Optional[float] = None
    hdl: Optional[float] = None
    triglycerides: Optional[float] = None
    # Organ Function
    alt: Optional[float] = None
    ast: Optional[float] = None
    alp: Optional[float] = None
    creatinine: Optional[float] = None
    bun: Optional[float] = None
    egfr: Optional[float] = None
    # Anthropometric
    height_cm: Optional[float] = None
    weight_kg: Optional[float] = None
    bmi: Optional[float] = None
    waist_cm: Optional[float] = None
    hip_cm: Optional[float] = None
    waist_hip_ratio: Optional[float] = None
    skinfold_mm: Optional[float] = None
    # Lifestyle
    smoking_status: Optional[str] = None
    alcohol_units: Optional[float] = None
    activity_level: Optional[str] = None
    sleep_hours: Optional[float] = None


class AnalysisResponse(BaseModel):
    """Full ML pipeline output."""
    record_id: str
    health_label: str
    risk_score: float
    cluster_id: int
    cluster_label: str
    pca_components: list
    autoencoder_embedding: list
    lda_topic_distribution: list
    pdm_topic_distribution: list
    shap_values: dict
    feature_importance: dict
    metabolic_composite: float
    cardiovascular_composite: float
    organ_function_composite: float
    model_predictions: dict
    warnings: list


@app.get("/ml/health")
async def health_check():
    """Health check endpoint."""
    return {"status": "healthy", "service": "ml-service"}


@app.get("/ml/model-info")
async def model_info():
    """Current model metadata."""
    models_dir = "/app/models"
    available_models = []
    if os.path.exists(models_dir):
        available_models = [f for f in os.listdir(models_dir) if f.endswith(('.pkl', '.joblib', '.pt'))]
    return {
        "version": "1.0.0",
        "available_models": available_models,
        "pipeline_stages": [
            "preprocessing", "pca", "autoencoder",
            "kmeans", "hierarchical", "dbscan", "gmm", "lda", "pdm",
            "random_forest", "xgboost", "lightgbm", "svm", "logistic_regression",
            "shap", "permutation_importance", "risk_scoring"
        ],
    }


@app.post("/ml/analyze", response_model=AnalysisResponse)
async def analyze(input_data: BiomarkerInput):
    """
    Run full ML pipeline on a single patient's biomarkers.
    Ref-1 Layers 2–6: preprocess → PCA/autoencoder → clustering → supervised → SHAP → risk score.
    """
    try:
        logger.info("ml_analyze_start", record_id=input_data.record_id)

        # Layer 2: Preprocessing (Ref-1)
        features, feature_names = preprocess_biomarkers(input_data.model_dump())
        logger.info("preprocessing_complete", n_features=len(feature_names))

        # Layer 3: Dimensionality Reduction (Ref-1)
        pca_result = run_pca(features, feature_names)
        ae_embedding = run_autoencoder(features)
        logger.info("dimensionality_reduction_complete")

        # Layer 4: Unsupervised Clustering (Ref-1 + Ref-2)
        kmeans_result = run_kmeans(features)
        hier_result = run_hierarchical(features)
        dbscan_result = run_dbscan(features)
        gmm_result = run_gmm(features)
        lda_result = run_lda(features, feature_names)           # Ref-2: LDA
        pdm_result = run_pdm(features, feature_names,           # Ref-2: PDM
                             age=input_data.age, sex=input_data.sex)

        # Layer 4: Supervised Prediction (Ref-1)
        supervised_result = run_ensemble_prediction(features, feature_names)
        logger.info("ml_models_complete")

        # Layer 5: Interpretability (Ref-1)
        shap_vals = compute_shap_values(features, feature_names)
        perm_imp = compute_permutation_importance(features, feature_names)

        # Layer 6: Risk Scoring (Ref-1)
        risk = compute_risk_score(
            supervised_result=supervised_result,
            kmeans_result=kmeans_result,
            pca_result=pca_result,
            features=features,
            feature_names=feature_names,
        )

        # Compose response
        response = AnalysisResponse(
            record_id=input_data.record_id,
            health_label=risk["health_label"],
            risk_score=risk["risk_score"],
            cluster_id=kmeans_result["cluster_id"],
            cluster_label=kmeans_result["cluster_label"],
            pca_components=pca_result["components"].tolist() if hasattr(pca_result["components"], 'tolist') else pca_result["components"],
            autoencoder_embedding=ae_embedding.tolist() if hasattr(ae_embedding, 'tolist') else ae_embedding,
            lda_topic_distribution=lda_result["topic_distribution"],
            pdm_topic_distribution=pdm_result["topic_distribution"],
            shap_values=shap_vals,
            feature_importance=perm_imp,
            metabolic_composite=risk["metabolic_composite"],
            cardiovascular_composite=risk["cardiovascular_composite"],
            organ_function_composite=risk["organ_function_composite"],
            model_predictions=supervised_result["predictions"],
            warnings=risk["warnings"],
        )

        logger.info("ml_analyze_complete", record_id=input_data.record_id,
                     risk_score=risk["risk_score"], label=risk["health_label"])
        return response

    except Exception as e:
        logger.error("ml_analyze_error", error=str(e), traceback=traceback.format_exc())
        raise HTTPException(status_code=500, detail=f"ML analysis failed: {str(e)}")


@app.post("/ml/train")
async def retrain():
    """Trigger model retraining on full dataset."""
    logger.info("retrain_requested")
    # In a real system, this would pull data from DB and retrain all models.
    # For local dev, return a stub response.
    return {
        "status": "training_complete",
        "message": "Models retrained on current dataset (stub for local dev).",
    }
