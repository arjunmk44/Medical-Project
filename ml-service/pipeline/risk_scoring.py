"""
Risk Scoring Engine — Ref-1 Layer 6.

Combines outputs from all upstream ML layers into a final risk assessment:
    - Supervised model predictions (ensemble health label + confidence)
    - Clustering results (cluster ID indicates health group)
    - Feature deviations from clinical norms
    - PCA variance (how "typical" the patient's pattern is)

Produces:
    1. risk_score: Float 0.0 (healthy) to 1.0 (critical) — weighted combination.
    2. health_label: "Healthy", "Monitor Closely", or "At Risk".
    3. warnings: List of early warning alerts when biomarkers exceed thresholds.

Weight configuration (adjustable):
    - Supervised prediction:  40% (most reliable, trained on labeled data)
    - Cluster assignment:     20% (validates against unsupervised groupings)
    - Feature deviation:      25% (raw biomarker abnormality signal)
    - PCA distance:           15% (how unusual the overall pattern is)
"""

import numpy as np
from typing import Dict, Any, List, Optional
import structlog

logger = structlog.get_logger()

# ─── Risk Score Weights ───────────────────────────────────────────────────────
# These weights determine how much each ML component contributes to the final score.
# They should sum to 1.0.

SUPERVISED_WEIGHT = 0.40    # Weight for ensemble prediction confidence
CLUSTER_WEIGHT = 0.20      # Weight for cluster-based risk
FEATURE_WEIGHT = 0.25      # Weight for raw feature deviation from norms
PCA_WEIGHT = 0.15          # Weight for PCA-based pattern abnormality

# ─── Warning Thresholds ──────────────────────────────────────────────────────
# Biomarker thresholds that trigger early warning alerts.
# Values are Z-scores — i.e., how many standard deviations above/below normal.

WARNING_THRESHOLDS = {
    "glucose": {"high": 1.5, "critical": 2.5, "name": "Blood Glucose"},
    "hba1c": {"high": 1.5, "critical": 2.5, "name": "HbA1c"},
    "systolic_bp": {"high": 1.5, "critical": 2.5, "name": "Systolic Blood Pressure"},
    "ldl": {"high": 1.5, "critical": 2.5, "name": "LDL Cholesterol"},
    "creatinine": {"high": 1.5, "critical": 2.5, "name": "Serum Creatinine"},
    "alt": {"high": 1.5, "critical": 2.5, "name": "ALT (Liver Enzyme)"},
    "bmi": {"high": 1.5, "critical": 2.5, "name": "BMI"},
}


def _compute_supervised_risk(supervised_result: Dict[str, Any]) -> float:
    """
    Extract risk score from supervised ensemble prediction.

    Uses the predicted class probabilities to compute a risk value:
        - P("Healthy") contributes 0 risk
        - P("Monitor Closely") contributes 0.5 risk
        - P("At Risk") contributes 1.0 risk

    The weighted sum gives a continuous risk score [0, 1].

    Args:
        supervised_result: Output from ensemble.run_ensemble_prediction().

    Returns:
        Float risk score from supervised models (0.0 to 1.0).
    """
    ensemble = supervised_result.get("ensemble", {})
    probs = ensemble.get("class_probabilities", {})
    risk = (
        probs.get("Healthy", 0.5) * 0.0 +            # Healthy → no risk
        probs.get("Monitor Closely", 0.3) * 0.5 +     # Monitor → moderate risk
        probs.get("At Risk", 0.2) * 1.0                # At Risk → full risk
    )
    return float(risk)


def _compute_cluster_risk(clustering_result: Dict[str, Any]) -> float:
    """
    Map cluster assignment to a risk score.

    Cluster → Risk mapping:
        0 (Healthy):          0.1
        1 (Monitor Closely):  0.4
        2 (At Risk):          0.7
        3 (High Risk):        0.9

    Args:
        clustering_result: Output from kmeans.run_kmeans().

    Returns:
        Float risk score from cluster assignment (0.1 to 0.9).
    """
    cluster_risk_map = {0: 0.1, 1: 0.4, 2: 0.7, 3: 0.9}
    cluster_id = clustering_result.get("cluster_id", 1)
    return cluster_risk_map.get(cluster_id, 0.5)


def _compute_feature_risk(features: np.ndarray, feature_names: List[str]) -> float:
    """
    Compute risk from raw feature deviations.

    Measures how far the patient's features are from the population average
    (since features are Z-score normalized, deviations >2 are clinically significant).

    Uses the mean absolute Z-score across all features. Higher mean deviation
    indicates a less typical (potentially riskier) biomarker profile.

    Sigmoid transformation squashes the value to [0, 1]:
        sigmoid(x) = 1 / (1 + exp(-k * (x - midpoint)))

    Args:
        features: Normalized feature array.
        feature_names: Feature names (not used in current implementation).

    Returns:
        Float risk score from feature deviations (0.0 to 1.0).
    """
    mean_deviation = np.mean(np.abs(features))
    # Sigmoid with midpoint at 1.0 deviation, steepness 2.0
    risk = 1 / (1 + np.exp(-2 * (mean_deviation - 1.0)))
    return float(risk)


def _compute_pca_risk(pca_result: Dict[str, Any]) -> float:
    """
    Compute risk from PCA component magnitudes.

    Patients with extreme PCA projections (far from the center of the PCA space)
    have unusual biomarker patterns — this may indicate health abnormalities.

    Uses the L2 norm of the first few PCA components as a distance metric.

    Args:
        pca_result: Output from dimensionality.run_dimensionality_reduction().

    Returns:
        Float risk score from PCA distance (0.0 to 1.0).
    """
    pca = pca_result.get("pca", {})
    components = pca.get("components", [0.0])
    pca_magnitude = np.linalg.norm(components[:5])   # Use first 5 components
    # Sigmoid with midpoint at magnitude 3.0
    risk = 1 / (1 + np.exp(-1.5 * (pca_magnitude - 3.0)))
    return float(risk)


def _generate_warnings(
    features: np.ndarray,
    feature_names: List[str],
    risk_score: float,
) -> List[Dict[str, Any]]:
    """
    Generate early warning alerts for clinicians.

    Two types of warnings:
    1. Risk-level warnings: Triggered by overall risk score thresholds.
       - risk_score > 0.7 → HIGH severity warning
       - risk_score > 0.5 → MEDIUM severity warning

    2. Biomarker-specific warnings: Triggered when individual biomarker Z-scores
       exceed the thresholds defined in WARNING_THRESHOLDS.
       - |z| > critical (2.5) → HIGH severity
       - |z| > high (1.5)     → MEDIUM severity

    Each warning includes a type, severity, human-readable message,
    and a clinical recommendation.

    Args:
        features: Normalized feature array (Z-scores).
        feature_names: Feature names matching the array indices.
        risk_score: Final composite risk score.

    Returns:
        List of warning dicts, each with: type, severity, message, recommendation.
    """
    warnings = []

    # Overall risk score warnings
    if risk_score > 0.7:
        warnings.append({
            "type": "HIGH_RISK_SCORE",
            "severity": "HIGH",
            "message": f"Patient has a high overall risk score of {risk_score:.2f}",
            "recommendation": "Immediate clinical review recommended. Consider comprehensive health assessment.",
        })
    elif risk_score > 0.5:
        warnings.append({
            "type": "MODERATE_RISK",
            "severity": "MEDIUM",
            "message": f"Patient has an elevated risk score of {risk_score:.2f}",
            "recommendation": "Schedule follow-up appointment. Monitor key biomarkers.",
        })

    # Per-biomarker warnings — check each tracked biomarker's Z-score
    feature_map = dict(zip(feature_names, features))
    for feature, thresholds in WARNING_THRESHOLDS.items():
        if feature in feature_map:
            z_score = abs(feature_map[feature])
            if z_score > thresholds["critical"]:
                warnings.append({
                    "type": f"BIOMARKER_CRITICAL_{feature.upper()}",
                    "severity": "HIGH",
                    "message": f"{thresholds['name']} is critically abnormal (Z-score: {z_score:.1f})",
                    "recommendation": f"Urgent: Verify {thresholds['name']} measurement and consider immediate intervention.",
                })
            elif z_score > thresholds["high"]:
                warnings.append({
                    "type": f"BIOMARKER_DEVIATION_{feature.upper()}",
                    "severity": "MEDIUM",
                    "message": f"{thresholds['name']} is significantly elevated (Z-score: {z_score:.1f})",
                    "recommendation": f"Monitor {thresholds['name']} closely. Schedule follow-up test.",
                })

    return warnings


def compute_risk_score(
    supervised_result: Dict[str, Any],
    clustering_result: Dict[str, Any],
    features: np.ndarray,
    feature_names: List[str],
    pca_result: Dict[str, Any],
) -> Dict[str, Any]:
    """
    Main risk scoring function — combines all ML outputs into a final assessment.

    Computes a weighted composite risk score:
        final = (supervised × 0.40) + (cluster × 0.20) + (features × 0.25) + (pca × 0.15)

    Then maps the score to a health label:
        < 0.3  → "Healthy"
        < 0.6  → "Monitor Closely"
        ≥ 0.6  → "At Risk"

    Args:
        supervised_result: Ensemble prediction output (health label probabilities).
        clustering_result: K-Means cluster assignment.
        features: Normalized feature vector.
        feature_names: Feature name list.
        pca_result: PCA dimensionality reduction output.

    Returns:
        Dict containing:
            - risk_score: Final composite score (0.0 to 1.0).
            - health_label: "Healthy", "Monitor Closely", or "At Risk".
            - component_scores: Breakdown of each component's contribution.
            - warnings: List of generated early warning alerts.
    """
    # Compute individual risk components
    supervised_risk = _compute_supervised_risk(supervised_result)
    cluster_risk = _compute_cluster_risk(clustering_result)
    feature_risk = _compute_feature_risk(features, feature_names)
    pca_risk = _compute_pca_risk(pca_result)

    # Weighted combination
    risk_score = (
        supervised_risk * SUPERVISED_WEIGHT +
        cluster_risk * CLUSTER_WEIGHT +
        feature_risk * FEATURE_WEIGHT +
        pca_risk * PCA_WEIGHT
    )

    # Clamp to [0, 1]
    risk_score = max(0.0, min(1.0, risk_score))

    # Map score to health label
    if risk_score < 0.3:
        health_label = "Healthy"
    elif risk_score < 0.6:
        health_label = "Monitor Closely"
    else:
        health_label = "At Risk"

    # Generate early warning alerts
    warnings = _generate_warnings(features, feature_names, risk_score)

    logger.info("risk_score_complete",
                risk_score=round(risk_score, 4),
                health_label=health_label,
                n_warnings=len(warnings))

    return {
        "risk_score": round(risk_score, 4),
        "health_label": health_label,
        "component_scores": {
            "supervised": round(supervised_risk, 4),
            "cluster": round(cluster_risk, 4),
            "feature_deviation": round(feature_risk, 4),
            "pca_distance": round(pca_risk, 4),
        },
        "weights": {
            "supervised": SUPERVISED_WEIGHT,
            "cluster": CLUSTER_WEIGHT,
            "feature_deviation": FEATURE_WEIGHT,
            "pca_distance": PCA_WEIGHT,
        },
        "warnings": warnings,
    }
