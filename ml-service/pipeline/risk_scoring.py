"""
Risk Scoring Engine — Ref-1 Layer 6
Combines all ML outputs into personalized risk scores.
Latent health indices, cluster labels, early deviation detection.
"""

import numpy as np
from typing import Dict, Any, List
import structlog

logger = structlog.get_logger()

# Clinical thresholds for early warning generation
WARNING_THRESHOLDS = {
    "systolic_bp": {"high": 140, "critical": 180, "unit": "mmHg", "label": "Hypertension"},
    "glucose": {"high": 126, "critical": 200, "unit": "mg/dL", "label": "Hyperglycemia"},
    "hba1c": {"high": 6.5, "critical": 9.0, "unit": "%", "label": "Diabetes Risk"},
    "ldl": {"high": 160, "critical": 190, "unit": "mg/dL", "label": "High LDL Cholesterol"},
    "triglycerides": {"high": 200, "critical": 500, "unit": "mg/dL", "label": "Hypertriglyceridemia"},
    "creatinine": {"high": 1.3, "critical": 4.0, "unit": "mg/dL", "label": "Kidney Impairment"},
    "alt": {"high": 56, "critical": 200, "unit": "U/L", "label": "Liver Enzyme Elevation"},
    "bmi": {"high": 30, "critical": 40, "unit": "kg/m²", "label": "Obesity"},
}


def _compute_composite_scores(
    features: np.ndarray, feature_names: List[str]
) -> Dict[str, float]:
    """Compute composite domain scores (Ref-1 Layer 6)."""
    feature_map = dict(zip(feature_names, features))

    # Metabolic composite (glucose, hba1c, cholesterol, ldl, hdl, triglycerides)
    metabolic_features = ["glucose", "hba1c", "total_cholesterol", "ldl", "hdl", "triglycerides"]
    metabolic_vals = [abs(feature_map.get(f, 0)) for f in metabolic_features]
    metabolic_composite = float(np.mean(metabolic_vals)) if metabolic_vals else 0.0

    # Cardiovascular composite (bp, heart rate)
    cv_features = ["systolic_bp", "diastolic_bp", "heart_rate"]
    cv_vals = [abs(feature_map.get(f, 0)) for f in cv_features]
    cardiovascular_composite = float(np.mean(cv_vals)) if cv_vals else 0.0

    # Organ function composite (liver + kidney)
    organ_features = ["alt", "ast", "alp", "creatinine", "bun", "egfr"]
    organ_vals = [abs(feature_map.get(f, 0)) for f in organ_features]
    organ_function_composite = float(np.mean(organ_vals)) if organ_vals else 0.0

    return {
        "metabolic_composite": round(metabolic_composite, 4),
        "cardiovascular_composite": round(cardiovascular_composite, 4),
        "organ_function_composite": round(organ_function_composite, 4),
    }


def _generate_warnings(
    features: np.ndarray, feature_names: List[str], risk_score: float
) -> List[Dict[str, Any]]:
    """Generate early warning alerts based on clinical thresholds (Ref-1 Layer 8)."""
    warnings = []
    feature_map = dict(zip(feature_names, features))

    # High risk score warning
    if risk_score > 0.7:
        warnings.append({
            "type": "HIGH_RISK_SCORE",
            "severity": "HIGH",
            "message": f"Overall risk score is elevated ({round(risk_score, 3)}). "
                       "Multiple health indicators suggest increased health risk.",
            "recommendation": "Schedule comprehensive follow-up evaluation. "
                            "Review cardiovascular, metabolic, and organ function markers.",
        })
    elif risk_score > 0.5:
        warnings.append({
            "type": "MODERATE_RISK",
            "severity": "MEDIUM",
            "message": f"Risk score is moderately elevated ({round(risk_score, 3)}).",
            "recommendation": "Consider lifestyle modifications and follow-up in 3 months.",
        })

    # Deviation-based warnings (normalized features >1 std from mean)
    for feat in feature_names:
        val = feature_map.get(feat, 0)
        if abs(val) > 1.5:
            severity = "HIGH" if abs(val) > 2.0 else "MEDIUM"
            direction = "elevated" if val > 0 else "below normal"
            warnings.append({
                "type": f"BIOMARKER_DEVIATION_{feat.upper()}",
                "severity": severity,
                "message": f"{feat.replace('_', ' ').title()} is significantly {direction} "
                          f"(deviation: {round(val, 2)} SD from population mean).",
                "recommendation": f"Investigate {feat.replace('_', ' ')} levels. "
                                "Consider targeted diagnostic testing.",
            })

    return warnings[:10]  # Cap at 10 warnings


def compute_risk_score(
    supervised_result: Dict[str, Any],
    kmeans_result: Dict[str, Any],
    pca_result: Dict[str, Any],
    features: np.ndarray,
    feature_names: List[str],
) -> Dict[str, Any]:
    """
    Compute final risk score combining all ML outputs.
    Ref-1 Layer 6: latent indices + cluster labels + predictive risk.

    Weights:
    - Supervised ensemble: 40%
    - Clustering consensus: 30%
    - Feature deviation magnitude: 20%
    - PCA latent indices: 10%
    """
    # Supervised component
    ensemble_probs = supervised_result["ensemble"]["class_probabilities"]
    supervised_risk = (
        ensemble_probs.get("Monitor Closely", 0) * 0.5
        + ensemble_probs.get("At Risk", 0) * 1.0
    )

    # Clustering component
    cluster_risk_map = {0: 0.1, 1: 0.4, 2: 0.7, 3: 0.95}
    cluster_risk = cluster_risk_map.get(kmeans_result["cluster_id"], 0.5)

    # Feature deviation component
    feature_deviation = min(float(np.mean(np.abs(features))) / 2.0, 1.0)

    # PCA component
    pca_magnitude = min(float(np.linalg.norm(pca_result["components"])) / 5.0, 1.0)

    # Weighted combination
    risk_score = (
        0.40 * supervised_risk
        + 0.30 * cluster_risk
        + 0.20 * feature_deviation
        + 0.10 * pca_magnitude
    )
    risk_score = max(0.0, min(1.0, risk_score))

    # Assign label
    if risk_score < 0.3:
        health_label = "Healthy"
    elif risk_score < 0.6:
        health_label = "Monitor Closely"
    else:
        health_label = "At Risk"

    # Composite scores
    composites = _compute_composite_scores(features, feature_names)

    # Warnings
    warnings = _generate_warnings(features, feature_names, risk_score)

    logger.info("risk_score_computed", risk_score=round(risk_score, 4),
                health_label=health_label, n_warnings=len(warnings))

    return {
        "risk_score": round(risk_score, 4),
        "health_label": health_label,
        "component_scores": {
            "supervised": round(supervised_risk, 4),
            "clustering": round(cluster_risk, 4),
            "feature_deviation": round(feature_deviation, 4),
            "pca_latent": round(pca_magnitude, 4),
        },
        "warnings": warnings,
        **composites,
    }
