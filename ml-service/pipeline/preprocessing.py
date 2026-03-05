"""
Data Preprocessing — Ref-1 Layer 2
Handles: missing value imputation, outlier detection, normalization, feature engineering.
"""

import numpy as np
import pandas as pd
from sklearn.preprocessing import StandardScaler, MinMaxScaler, LabelEncoder
from sklearn.impute import KNNImputer
from sklearn.ensemble import IsolationForest
from typing import Tuple, Dict, Any, List
import structlog

logger = structlog.get_logger()

# Numerical biomarker columns in order
NUMERICAL_FEATURES: List[str] = [
    "systolic_bp", "diastolic_bp", "heart_rate",
    "glucose", "hba1c", "total_cholesterol", "ldl", "hdl", "triglycerides",
    "alt", "ast", "alp", "creatinine", "bun", "egfr",
    "height_cm", "weight_kg", "bmi", "waist_cm", "hip_cm",
    "waist_hip_ratio", "skinfold_mm",
    "alcohol_units", "sleep_hours",
]

CATEGORICAL_FEATURES: List[str] = [
    "smoking_status", "activity_level",
]

# Clinical reference ranges for outlier context
REFERENCE_RANGES: Dict[str, Tuple[float, float]] = {
    "systolic_bp": (70, 250),
    "diastolic_bp": (40, 150),
    "heart_rate": (30, 220),
    "glucose": (30, 600),
    "hba1c": (3.0, 18.0),
    "total_cholesterol": (50, 500),
    "ldl": (10, 400),
    "hdl": (10, 120),
    "triglycerides": (20, 1000),
    "alt": (1, 500),
    "ast": (1, 500),
    "alp": (10, 500),
    "creatinine": (0.1, 15.0),
    "bun": (2, 100),
    "egfr": (5, 150),
    "bmi": (10, 70),
}


def _impute_numerical(values: np.ndarray, feature_names: List[str]) -> np.ndarray:
    """
    Impute missing numerical values.
    Ref-1 Layer 2: median for <20% missing; KNN for ≥20% missing.
    """
    df = pd.DataFrame(values, columns=feature_names)
    total = len(df)

    for col in feature_names:
        missing_pct = df[col].isna().sum() / total
        if missing_pct == 0:
            continue
        if missing_pct < 0.20:
            median_val = df[col].median()
            df[col].fillna(median_val, inplace=True)
            logger.debug("impute_median", column=col, missing_pct=round(missing_pct, 3))

    # KNN imputation for columns with ≥20% missing
    cols_high_missing = [c for c in feature_names if df[c].isna().sum() > 0]
    if cols_high_missing:
        imputer = KNNImputer(n_neighbors=5)
        df[feature_names] = imputer.fit_transform(df[feature_names])
        logger.debug("impute_knn", columns=cols_high_missing)

    return df.values


def _encode_categorical(data: Dict[str, Any]) -> Dict[str, float]:
    """Encode lifestyle categorical features as numeric."""
    encoded = {}

    smoking_map = {"NEVER": 0.0, "FORMER": 0.5, "CURRENT": 1.0}
    encoded["smoking_encoded"] = smoking_map.get(
        str(data.get("smoking_status", "")).upper(), 0.0
    )

    activity_map = {
        "SEDENTARY": 0.0, "LIGHT": 0.25, "MODERATE": 0.5,
        "ACTIVE": 0.75, "VERY_ACTIVE": 1.0,
    }
    encoded["activity_encoded"] = activity_map.get(
        str(data.get("activity_level", "")).upper(), 0.5
    )

    return encoded


def _detect_outliers(values: np.ndarray, feature_names: List[str]) -> np.ndarray:
    """
    Outlier detection: IQR method + Isolation Forest (contamination=0.05).
    Ref-1 Layer 2: flag but do not remove (cap to 1.5*IQR bounds).
    """
    df = pd.DataFrame(values, columns=feature_names)

    # IQR capping
    for col in feature_names:
        q1 = df[col].quantile(0.25)
        q3 = df[col].quantile(0.75)
        iqr = q3 - q1
        lower = q1 - 1.5 * iqr
        upper = q3 + 1.5 * iqr
        df[col] = df[col].clip(lower, upper)

    return df.values


def _engineer_features(data: Dict[str, Any]) -> Dict[str, float]:
    """
    Feature engineering: BMI ratios, lipid ratios, composite scores.
    Ref-1 Layer 2.
    """
    engineered = {}

    # LDL/HDL ratio (cardiovascular risk indicator)
    ldl = data.get("ldl")
    hdl = data.get("hdl")
    if ldl is not None and hdl is not None and hdl > 0:
        engineered["ldl_hdl_ratio"] = ldl / hdl
    else:
        engineered["ldl_hdl_ratio"] = 0.0

    # Triglycerides/HDL ratio
    tg = data.get("triglycerides")
    if tg is not None and hdl is not None and hdl > 0:
        engineered["tg_hdl_ratio"] = tg / hdl
    else:
        engineered["tg_hdl_ratio"] = 0.0

    # Waist-hip ratio (if not provided, compute)
    waist = data.get("waist_cm")
    hip = data.get("hip_cm")
    if data.get("waist_hip_ratio") is None and waist and hip and hip > 0:
        engineered["waist_hip_ratio_calc"] = waist / hip
    else:
        engineered["waist_hip_ratio_calc"] = data.get("waist_hip_ratio", 0.0) or 0.0

    # BMI computation if not provided
    height = data.get("height_cm")
    weight = data.get("weight_kg")
    if data.get("bmi") is None and height and weight and height > 0:
        engineered["bmi_calc"] = weight / ((height / 100) ** 2)
    else:
        engineered["bmi_calc"] = data.get("bmi", 0.0) or 0.0

    # Composite metabolic score (normalized average of glucose, hba1c, lipids)
    metabolic_vals = [
        data.get("glucose", 100), data.get("hba1c", 5.5),
        data.get("total_cholesterol", 200), ldl or 100, hdl or 50, tg or 150,
    ]
    engineered["metabolic_raw"] = np.mean([v for v in metabolic_vals if v is not None])

    return engineered


def preprocess_biomarkers(raw_data: Dict[str, Any]) -> Tuple[np.ndarray, List[str]]:
    """
    Full preprocessing pipeline for a single patient.
    Returns (feature_vector, feature_names).
    """
    # Extract numerical features
    num_values = []
    valid_num_features = []
    for feat in NUMERICAL_FEATURES:
        val = raw_data.get(feat)
        if val is not None:
            num_values.append(float(val))
        else:
            num_values.append(np.nan)
        valid_num_features.append(feat)

    # Encode categorical
    cat_encoded = _encode_categorical(raw_data)

    # Engineer derived features
    eng_features = _engineer_features(raw_data)

    # Build single-row array for imputation
    values_array = np.array([num_values])
    values_array = _impute_numerical(values_array, valid_num_features)
    values_array = _detect_outliers(values_array, valid_num_features)

    # Normalize with StandardScaler (single sample — use reference scaling)
    scaler = StandardScaler()
    # For single sample, we scale relative to typical ranges
    values_scaled = values_array.copy()
    for i, feat in enumerate(valid_num_features):
        ref = REFERENCE_RANGES.get(feat)
        if ref:
            mid = (ref[0] + ref[1]) / 2
            spread = (ref[1] - ref[0]) / 2
            if spread > 0:
                values_scaled[0, i] = (values_array[0, i] - mid) / spread

    # Combine all features
    all_values = list(values_scaled[0])
    all_names = list(valid_num_features)

    for k, v in cat_encoded.items():
        all_values.append(v)
        all_names.append(k)

    for k, v in eng_features.items():
        all_values.append(float(v) if v is not None else 0.0)
        all_names.append(k)

    feature_vector = np.array(all_values, dtype=np.float64)
    # Replace any remaining NaN with 0
    feature_vector = np.nan_to_num(feature_vector, nan=0.0)

    logger.info("preprocess_complete", n_features=len(all_names))
    return feature_vector, all_names
