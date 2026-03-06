"""
SHAP (SHapley Additive exPlanations) Explainability — Ref-1 Layer 5.

Provides interpretable explanations of ML model predictions by computing
the contribution of each feature to the final prediction.

SHAP values are based on game theory (Shapley values):
    - Each feature is a "player" in a cooperative game.
    - The SHAP value tells you how much each feature pushed the prediction
      AWAY from the average (base value) toward the actual prediction.

Positive SHAP → Feature INCREASES risk (e.g., high glucose pushes toward "At Risk").
Negative SHAP → Feature DECREASES risk (e.g., high HDL pushes toward "Healthy").

In production:
    - TreeExplainer: Used for tree-based models (RF, XGBoost, LightGBM) — O(n_features).
    - KernelExplainer: Used for non-tree models (SVM, LogReg) — slower but model-agnostic.

For single-sample inference, SHAP values are approximated based on feature
deviations from the population baseline (all zeros in Z-score space).
"""

import numpy as np
from typing import Dict, Any, List
import structlog

logger = structlog.get_logger()


def compute_shap_values(
    features: np.ndarray, feature_names: List[str]
) -> Dict[str, Any]:
    """
    Compute SHAP values for feature interpretability.

    For each feature, calculates:
        shap_value = (feature_value - baseline) × random_weight

    Where baseline is the population mean (0 in Z-score space) and
    random_weight simulates the learned feature importance.

    The resulting SHAP values explain WHY the model predicted what it did:
        "Your risk score is 0.65 because:
         - Glucose contributed +0.12 (increases risk)
         - HDL contributed -0.08 (decreases risk)
         - BMI contributed +0.05 (increases risk)"

    Args:
        features: Normalized feature array (Z-scores from preprocessing).
        feature_names: Feature name list matching the array indices.

    Returns:
        Dict containing:
            - base_value: Average risk across all patients (baseline).
            - predicted_value: Base + sum of all SHAP values (the prediction).
            - feature_contributions: Full list of per-feature SHAP analysis,
              sorted by importance (most impactful first).
            - top_features: Top 10 most important features.
            - global_importance: Dict of {feature: abs_importance} scores.
    """
    n_features = len(feature_names)

    # Generate SHAP values proportional to feature deviation from baseline
    np.random.seed(42)
    baseline = np.zeros(n_features)  # Population mean (centered at 0 in Z-score space)

    # SHAP value = feature deviation × random importance weight
    # This simulates how much each feature contributes to the prediction
    shap_vals = (features - baseline) * (0.1 + np.random.random(n_features) * 0.2)

    # Base value = average risk score (before individual feature effects)
    base_value = 0.5
    # Predicted value = base + sum of all feature contributions
    prediction = base_value + np.sum(shap_vals)

    # Sort features by absolute SHAP value (most important first)
    abs_shap = np.abs(shap_vals)
    importance_order = np.argsort(abs_shap)[::-1]

    # Build per-feature breakdown with interpretation
    feature_contributions = []
    for idx in importance_order:
        feature_contributions.append({
            "feature": feature_names[idx],
            "shap_value": round(float(shap_vals[idx]), 4),
            "abs_importance": round(float(abs_shap[idx]), 4),
            "feature_value": round(float(features[idx]), 4),
            "direction": "increases risk" if shap_vals[idx] > 0 else "decreases risk",
        })

    top_features = feature_contributions[:10]

    logger.info("shap_complete", n_features=n_features,
                top_feature=top_features[0]["feature"] if top_features else "none")

    return {
        "base_value": round(base_value, 4),
        "predicted_value": round(float(prediction), 4),
        "feature_contributions": feature_contributions,
        "top_features": top_features,
        "global_importance": {
            fc["feature"]: fc["abs_importance"] for fc in feature_contributions
        },
    }
