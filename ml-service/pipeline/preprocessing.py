"""
Data Preprocessing Pipeline — Ref-1 Layer 2.

Transforms raw patient biomarker data into a clean, normalized feature vector
suitable for ML model consumption. This is the first ML processing step after
data ingestion.

Pipeline steps:
    1. Feature Extraction  — Selects numeric biomarker fields from the raw data.
    2. Missing Value Imputation — Fills nulls with population-average defaults.
    3. Outlier Detection    — Caps extreme values using IQR-based Winsorization.
    4. Categorical Encoding — Converts smoking_status and activity_level to numeric.
    5. Feature Engineering  — Creates derived features (ratios, composite scores).
    6. Normalization        — Applies Z-score standardization (mean=0, std=1).

Input:  Raw patient data dict (from the FastAPI request body).
Output: Dict with 'features' (numpy array), 'feature_names' (list), and 'summary' (metadata).
"""

import numpy as np
from typing import Dict, Any, List
import structlog

logger = structlog.get_logger()

# ─── Clinical Reference Ranges ───────────────────────────────────────────────
# Default (population average) values used to impute missing biomarker measurements.
# These represent the midpoint of normal clinical ranges for each biomarker.

DEFAULTS = {
    "systolic_bp": 120, "diastolic_bp": 80, "heart_rate": 72,
    "glucose": 100, "hba1c": 5.5,
    "total_cholesterol": 200, "ldl": 100, "hdl": 50, "triglycerides": 150,
    "alt": 25, "ast": 25, "alp": 70,
    "creatinine": 1.0, "bun": 15, "egfr": 90,
    "height_cm": 170, "weight_kg": 70, "bmi": 24,
    "waist_cm": 85, "hip_cm": 95,
    "alcohol_units": 2, "sleep_hours": 7,
}

# ─── Outlier Detection Thresholds ─────────────────────────────────────────────
# Clinical bounds for Winsorization: values outside these ranges are capped
# to prevent extreme outliers from skewing the model's predictions.

CLINICAL_BOUNDS = {
    "systolic_bp": (70, 250),       # mmHg — range: severe hypotension to hypertensive crisis
    "diastolic_bp": (40, 150),      # mmHg
    "heart_rate": (30, 220),        # bpm — range: bradycardia to maximum predicted HR
    "glucose": (40, 600),           # mg/dL — range: severe hypoglycemia to diabetic emergency
    "hba1c": (3.0, 15.0),          # % — range: normal to severe uncontrolled diabetes
    "total_cholesterol": (80, 500), # mg/dL
    "ldl": (20, 400),              # mg/dL
    "hdl": (10, 120),              # mg/dL
    "triglycerides": (30, 1000),    # mg/dL
    "alt": (5, 500),               # U/L — range: normal to severe liver damage
    "ast": (5, 500),               # U/L
    "alp": (20, 500),              # U/L
    "creatinine": (0.2, 15.0),     # mg/dL — range: normal to severe kidney failure
    "bun": (2, 100),               # mg/dL
    "egfr": (5, 150),              # mL/min/1.73m²
    "bmi": (12, 65),               # kg/m² — range: severe underweight to extreme obesity
}

# ─── Normalization Parameters ─────────────────────────────────────────────────
# Pre-computed population mean and standard deviation for Z-score normalization.
# In production, these would be computed from the training dataset.

NORM_PARAMS = {
    "systolic_bp": {"mean": 125, "std": 18},
    "diastolic_bp": {"mean": 78, "std": 12},
    "heart_rate": {"mean": 75, "std": 12},
    "glucose": {"mean": 105, "std": 30},
    "hba1c": {"mean": 5.8, "std": 1.2},
    "total_cholesterol": {"mean": 210, "std": 40},
    "ldl": {"mean": 110, "std": 35},
    "hdl": {"mean": 52, "std": 15},
    "triglycerides": {"mean": 155, "std": 70},
    "alt": {"mean": 28, "std": 15},
    "ast": {"mean": 27, "std": 12},
    "alp": {"mean": 75, "std": 25},
    "creatinine": {"mean": 1.05, "std": 0.3},
    "bun": {"mean": 16, "std": 6},
    "egfr": {"mean": 88, "std": 20},
    "height_cm": {"mean": 170, "std": 10},
    "weight_kg": {"mean": 75, "std": 15},
    "bmi": {"mean": 25.5, "std": 5},
    "waist_cm": {"mean": 88, "std": 12},
    "hip_cm": {"mean": 98, "std": 10},
    "alcohol_units": {"mean": 5, "std": 6},
    "sleep_hours": {"mean": 7, "std": 1.5},
}


def _impute_missing(data: Dict[str, Any]) -> Dict[str, float]:
    """
    Step 2: Impute missing values with population-average defaults.

    For each expected biomarker field:
        - If the value is present and not None, use it as-is (converted to float).
        - If missing or None, substitute the default from DEFAULTS.

    Args:
        data: Raw patient data dict from the API request.

    Returns:
        Dict of {feature_name: numeric_value} with no null values.
    """
    imputed = {}
    for feature, default in DEFAULTS.items():
        value = data.get(feature)
        if value is not None:
            try:
                imputed[feature] = float(value)
            except (ValueError, TypeError):
                # If the value can't be converted to float, use the default
                imputed[feature] = float(default)
        else:
            imputed[feature] = float(default)
    return imputed


def _cap_outliers(values: Dict[str, float]) -> Dict[str, float]:
    """
    Step 3: Cap extreme outlier values using Winsorization.

    Clamps each biomarker value to its clinical bounds defined in CLINICAL_BOUNDS.
    Values below the lower bound are raised to the minimum; values above the
    upper bound are lowered to the maximum.

    This prevents physiologically impossible or extreme values from distorting
    downstream model predictions.

    Args:
        values: Dict of {feature_name: numeric_value} (after imputation).

    Returns:
        Dict with outlier values capped to clinical bounds.
    """
    capped = {}
    for feature, value in values.items():
        if feature in CLINICAL_BOUNDS:
            low, high = CLINICAL_BOUNDS[feature]
            capped[feature] = max(low, min(high, value))
        else:
            capped[feature] = value
    return capped


def _encode_categoricals(data: Dict[str, Any]) -> Dict[str, float]:
    """
    Step 4: Convert categorical features to numeric values.

    Encoding schemes:
        smoking_status: NEVER=0, FORMER=0.5, CURRENT=1.0
            Higher values indicate higher cardiovascular risk.

        activity_level: SEDENTARY=0, LIGHT=0.25, MODERATE=0.5, ACTIVE=0.75, VERY_ACTIVE=1.0
            Higher values indicate more physical activity (protective factor).

    Args:
        data: Raw patient data dict containing categorical fields.

    Returns:
        Dict with encoded categorical features as numeric values.
    """
    # Smoking status encoding — higher = more risk
    smoking_map = {"NEVER": 0.0, "FORMER": 0.5, "CURRENT": 1.0}
    smoking = data.get("smoking_status", "NEVER")
    smoking_encoded = smoking_map.get(str(smoking).upper(), 0.0)

    # Activity level encoding — higher = more active (protective)
    activity_map = {"SEDENTARY": 0.0, "LIGHT": 0.25, "MODERATE": 0.5, "ACTIVE": 0.75, "VERY_ACTIVE": 1.0}
    activity = data.get("activity_level", "MODERATE")
    activity_encoded = activity_map.get(str(activity).upper(), 0.5)

    return {
        "smoking_encoded": smoking_encoded,
        "activity_encoded": activity_encoded,
    }


def _engineer_features(values: Dict[str, float], categoricals: Dict[str, float]) -> Dict[str, float]:
    """
    Step 5: Create derived/engineered features from existing biomarkers.

    Engineered features capture clinically meaningful ratios and composite scores:
        - bmi_calc: BMI calculated from height and weight (fills in if BMI is missing).
        - waist_hip_ratio: Waist-to-hip ratio (body fat distribution indicator).
        - cholesterol_ratio: Total cholesterol / HDL ratio (cardiovascular risk marker).
        - mean_arterial_pressure: MAP = DBP + (SBP - DBP) / 3 (perfusion pressure).
        - metabolic_composite: Average of glucose and HbA1c Z-scores (metabolic risk).
        - cardiovascular_composite: Average of SBP and DBP Z-scores (CV risk).
        - organ_composite: Average of ALT and creatinine Z-scores (organ function risk).

    Args:
        values: Dict of numeric biomarker values (after imputation and outlier capping).
        categoricals: Dict of encoded categorical features.

    Returns:
        Dict containing all engineered feature values.
    """
    engineered = {}

    # BMI calculated from height/weight (as a cross-check for the reported BMI)
    height_m = values.get("height_cm", 170) / 100
    weight = values.get("weight_kg", 70)
    engineered["bmi_calc"] = weight / (height_m ** 2) if height_m > 0 else 24

    # Waist-to-hip ratio — indicator of central obesity
    waist = values.get("waist_cm", 85)
    hip = values.get("hip_cm", 95)
    engineered["waist_hip_ratio"] = waist / hip if hip > 0 else 0.9

    # Total cholesterol to HDL ratio — predicts cardiovascular disease risk
    tc = values.get("total_cholesterol", 200)
    hdl = values.get("hdl", 50)
    engineered["cholesterol_ratio"] = tc / hdl if hdl > 0 else 4.0

    # Mean Arterial Pressure — clinically important for organ perfusion
    sbp = values.get("systolic_bp", 120)
    dbp = values.get("diastolic_bp", 80)
    engineered["mean_arterial_pressure"] = dbp + (sbp - dbp) / 3

    # Composite domain scores (simple averages of normalized values)
    # These provide quick summary metrics for metabolic, CV, and organ health
    glucose_z = (values.get("glucose", 100) - 105) / 30
    hba1c_z = (values.get("hba1c", 5.5) - 5.8) / 1.2
    engineered["metabolic_composite"] = (glucose_z + hba1c_z) / 2

    sbp_z = (sbp - 125) / 18
    dbp_z = (dbp - 78) / 12
    engineered["cardiovascular_composite"] = (sbp_z + dbp_z) / 2

    alt_z = (values.get("alt", 25) - 28) / 15
    creat_z = (values.get("creatinine", 1.0) - 1.05) / 0.3
    engineered["organ_composite"] = (alt_z + creat_z) / 2

    return engineered


def _normalize(values: Dict[str, float]) -> np.ndarray:
    """
    Step 6: Apply Z-score normalization to all features.

    Z-score formula: z = (x - mean) / std

    This transforms each feature to have approximately zero mean and unit variance,
    which is required for distance-based algorithms (K-Means, SVM, PCA) and
    improves convergence for gradient-based methods (neural networks).

    Features not in NORM_PARAMS (e.g., engineered features) are left as-is.

    Args:
        values: Dict of all feature values (original + engineered).

    Returns:
        Numpy array of normalized feature values.
    """
    normalized = []
    for feature, value in values.items():
        if feature in NORM_PARAMS:
            mean = NORM_PARAMS[feature]["mean"]
            std = NORM_PARAMS[feature]["std"]
            normalized.append((value - mean) / std if std > 0 else 0.0)
        else:
            normalized.append(value)
    return np.array(normalized, dtype=np.float64)


def preprocess_patient_data(raw_data: Dict[str, Any]) -> Dict[str, Any]:
    """
    Main preprocessing function — orchestrates the full preprocessing pipeline.

    Called by main.py's analyze endpoint. Executes all 5 preprocessing steps
    in sequence and returns the clean feature vector ready for ML models.

    Steps:
        1. Impute missing values with population defaults.
        2. Cap outliers using clinical bounds.
        3. Encode categorical features (smoking, activity) to numeric.
        4. Engineer derived features (ratios, composites).
        5. Normalize all features via Z-score standardization.

    Args:
        raw_data: Raw patient data dict from the API request body.

    Returns:
        Dict containing:
            - 'features': Normalized numpy array of all features.
            - 'feature_names': List of feature name strings (matching array order).
            - 'summary': Metadata dict with preprocessing stats.
    """
    logger.info("preprocessing_start")

    # Step 1: Fill missing values with defaults
    imputed = _impute_missing(raw_data)

    # Step 2: Cap outlier values
    capped = _cap_outliers(imputed)

    # Step 3: Encode categorical variables
    categoricals = _encode_categoricals(raw_data)

    # Step 4: Create derived features
    engineered = _engineer_features(capped, categoricals)

    # Combine all features into a single dict (order matters — matches feature_names)
    all_features = {**capped, **categoricals, **engineered}
    feature_names = list(all_features.keys())

    # Step 5: Normalize features
    features = _normalize(all_features)

    logger.info("preprocessing_complete", n_features=len(features),
                n_original=len(capped), n_engineered=len(engineered))

    return {
        "features": features,
        "feature_names": feature_names,
        "summary": {
            "n_features": len(features),
            "n_original_features": len(capped),
            "n_engineered_features": len(engineered),
            "n_categorical_features": len(categoricals),
            "imputed_count": sum(1 for k in DEFAULTS if raw_data.get(k) is None),
            "outliers_capped": sum(1 for k, v in imputed.items()
                                   if k in CLINICAL_BOUNDS and (v < CLINICAL_BOUNDS[k][0] or v > CLINICAL_BOUNDS[k][1])),
        },
    }
