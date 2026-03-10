"""
Survival Analysis — Ref-2 (PMC7028517, Section 3.3.2).

Generates synthetic Kaplan-Meier survival curves for patient subgroups
discovered by the PDM (Poisson Dirichlet Model). Used to validate whether
the identified subgroups have meaningfully different health outcomes.

Key outputs:
    - Kaplan-Meier curves: Survival probability over time for each subgroup.
    - Log-rank test: Statistical test for significant differences between curves.
    - ECI (Elixhauser Comorbidity Index): Summary comorbidity score per subgroup.
    - Demographics: Age and sex distribution per subgroup.

In production, real survival data (time-to-event, censoring) would be
pulled from the patient database. Currently uses synthetic/mock data.
"""

import numpy as np
from typing import Dict, Any, List, Optional
import structlog

logger = structlog.get_logger()


def compute_survival_data(
    n_subgroups: int = 4,
    n_timepoints: int = 60,
) -> Dict[str, Any]:
    """
    Generate Kaplan-Meier survival data for PDM patient subgroups.
    Ref-2 Section 3.3.2: survival analysis for subgroup validation.

    Returns:
        Survival curves, log-rank p-values, subgroup demographics, ECI scores.
    """
    np.random.seed(42)

    subgroup_labels = [
        "Low Risk (Healthy)",
        "Moderate Risk (Metabolic)",
        "High Risk (Cardiovascular)",
        "Very High Risk (Multi-System)",
    ]

    # Base hazard rates per subgroup (lower = better survival)
    base_hazards = [0.005, 0.012, 0.025, 0.045]

    survival_curves = []
    for sg in range(n_subgroups):
        # Generate Kaplan-Meier curve
        survival_prob = 1.0
        timepoints = []
        for t in range(n_timepoints):
            # Hazard with some random variation
            h = base_hazards[sg] * (1 + 0.2 * np.random.randn())
            h = max(0.001, h)
            survival_prob *= (1 - h)
            survival_prob = max(0.0, survival_prob)
            timepoints.append({
                "month": t + 1,
                "survival_probability": round(survival_prob, 4),
                "n_at_risk": max(10, int(100 - t * (sg + 1) * 0.3)),
                "n_events": int(np.random.poisson(h * 100)),
            })

        survival_curves.append({
            "subgroup_id": sg,
            "subgroup_label": subgroup_labels[sg],
            "curve": timepoints,
            "median_survival_months": next(
                (tp["month"] for tp in timepoints if tp["survival_probability"] < 0.5),
                n_timepoints,
            ),
        })

    # Log-rank test p-values between subgroups (Ref-2)
    log_rank_results = []
    for i in range(n_subgroups):
        for j in range(i + 1, n_subgroups):
            # Lower p-value for more different subgroups
            p_value = max(0.0001, 0.05 / (abs(base_hazards[j] - base_hazards[i]) * 100))
            log_rank_results.append({
                "group_a": subgroup_labels[i],
                "group_b": subgroup_labels[j],
                "chi_squared": round(float(-2 * np.log(p_value)), 2),
                "p_value": round(p_value, 6),
                "significant": p_value < 0.05,
            })

    # Elixhauser Comorbidity Index per subgroup (Ref-2 Section 3.3.2)
    eci_scores = []
    for sg in range(n_subgroups):
        mean_eci = sg * 2.5 + np.random.random() * 1.5
        eci_scores.append({
            "subgroup_id": sg,
            "subgroup_label": subgroup_labels[sg],
            "mean_eci": round(mean_eci, 2),
            "median_eci": round(mean_eci - 0.5, 2),
            "std_eci": round(1.0 + sg * 0.5, 2),
        })

    # Demographics per subgroup
    demographics = []
    for sg in range(n_subgroups):
        demographics.append({
            "subgroup_id": sg,
            "subgroup_label": subgroup_labels[sg],
            "mean_age": round(45 + sg * 7 + np.random.random() * 3, 1),
            "pct_male": round(0.45 + sg * 0.05 + np.random.random() * 0.05, 2),
            "n_patients": int(100 - sg * 15 + np.random.randint(-5, 5)),
        })

    logger.info("survival_analysis_complete", n_subgroups=n_subgroups)

    return {
        "survival_curves": survival_curves,
        "log_rank_tests": log_rank_results,
        "eci_scores": eci_scores,
        "demographics": demographics,
        "n_subgroups": n_subgroups,
        "n_timepoints": n_timepoints,
    }


def generate_survival_curves(
    cluster_id: int = 0,
    age: Optional[int] = None,
    sex: Optional[str] = None,
) -> Dict[str, Any]:
    """
    Wrapper around compute_survival_data matching the signature expected by main.py.

    Args:
        cluster_id: The patient's assigned cluster from K-Means.
        age: Patient age (used for context in the response).
        sex: Patient sex (used for context in the response).

    Returns:
        Survival analysis results with the patient's cluster highlighted.
    """
    result = compute_survival_data()
    result["patient_cluster_id"] = cluster_id
    result["patient_age"] = age
    result["patient_sex"] = sex
    return result
