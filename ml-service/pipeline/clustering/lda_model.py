"""
LDA (Latent Dirichlet Allocation) for Patient Subgroup Discovery — Ref-2.

From PMC7028517 (Wang et al.):
    LDA is a probabilistic topic model originally designed for text analysis.
    Here it is repurposed for patient health profiling by treating:
        - Each patient as a "document"
        - Biomarker categorical buckets as "words"
        - Latent health conditions as "topics"

LDA Generative Process (Ref-2 Section 3.1):
    For each of K disease topics:
        Choose ϕ_k ~ Dirichlet(β)         — topic-to-word distribution
    For each of M patients:
        Choose θ_m ~ Dirichlet(α)          — patient-to-topic distribution
        For each of N biomarker buckets:
            Choose z_{m,n} ~ Multinomial(θ_m)   — topic assignment
            Choose w_{m,n} ~ Multinomial(ϕ_z)   — observed bucket

Example topic interpretation:
    Topic 0: cardiovascular_normal, metabolic_normal → Healthy pattern
    Topic 1: metabolic_prediabetic, bmi_overweight → Early metabolic risk
    Topic 2: liver_significant_elevation, kidney_moderate_impairment → Organ dysfunction
    Topic 3: cardiovascular_elevated, lifestyle_high_risk → Multiple risk factors

In production, Gensim's LdaModel would be used on the full patient corpus.
For single-sample inference, topic distributions are simulated.
"""

import numpy as np
from typing import Dict, Any, List
import structlog

logger = structlog.get_logger()

# ─── Biomarker Bucket Vocabulary ──────────────────────────────────────────────
# These categorical buckets serve as the "vocabulary" in the LDA analogy.
# Continuous biomarker values are discretized into clinically meaningful categories.

BIOMARKER_BUCKETS = [
    "cardiovascular_normal", "cardiovascular_borderline", "cardiovascular_elevated",
    "metabolic_normal", "metabolic_prediabetic", "metabolic_diabetic",
    "liver_normal", "liver_mild_elevation", "liver_significant_elevation",
    "kidney_normal", "kidney_mild_impairment", "kidney_moderate_impairment",
    "bmi_underweight", "bmi_normal", "bmi_overweight", "bmi_obese",
    "lifestyle_healthy", "lifestyle_moderate_risk", "lifestyle_high_risk",
]


def _biomarkers_to_buckets(features: np.ndarray, feature_names: List[str]) -> List[str]:
    """
    Convert continuous biomarker values into categorical buckets.

    Each biomarker domain maps to a category based on thresholds:
        - Cardiovascular: systolic_bp → normal / borderline / elevated
        - Metabolic: (glucose + hba1c)/2 → normal / prediabetic / diabetic
        - Liver: (ALT + AST)/2 → normal / mild / significant elevation
        - Kidney: creatinine - eGFR → normal / mild / moderate impairment
        - BMI: direct mapping → underweight / normal / overweight / obese
        - Lifestyle: smoking - activity → healthy / moderate risk / high risk

    Since features are Z-score normalized, thresholds are in standard deviation units.

    Args:
        features: Normalized feature array.
        feature_names: Names of features matching the array.

    Returns:
        List of bucket label strings (one per biomarker domain).
    """
    feature_map = dict(zip(feature_names, features))
    buckets = []

    # Cardiovascular risk — based on systolic blood pressure Z-score
    sbp = feature_map.get("systolic_bp", 0)
    if sbp < 0:
        buckets.append("cardiovascular_normal")
    elif sbp < 0.5:
        buckets.append("cardiovascular_borderline")
    else:
        buckets.append("cardiovascular_elevated")

    # Metabolic risk — average of glucose and HbA1c Z-scores
    glucose = feature_map.get("glucose", 0)
    hba1c = feature_map.get("hba1c", 0)
    metabolic_avg = (glucose + hba1c) / 2
    if metabolic_avg < 0:
        buckets.append("metabolic_normal")
    elif metabolic_avg < 0.5:
        buckets.append("metabolic_prediabetic")
    else:
        buckets.append("metabolic_diabetic")

    # Liver function — average of ALT and AST Z-scores
    alt = feature_map.get("alt", 0)
    ast = feature_map.get("ast", 0)
    liver_avg = (alt + ast) / 2
    if liver_avg < 0:
        buckets.append("liver_normal")
    elif liver_avg < 0.5:
        buckets.append("liver_mild_elevation")
    else:
        buckets.append("liver_significant_elevation")

    # Kidney function — creatinine elevated + eGFR low = worse
    creatinine = feature_map.get("creatinine", 0)
    egfr = feature_map.get("egfr", 0)
    kidney_score = creatinine - egfr  # Inverted: high creat + low eGFR = bad
    if kidney_score < 0:
        buckets.append("kidney_normal")
    elif kidney_score < 0.5:
        buckets.append("kidney_mild_impairment")
    else:
        buckets.append("kidney_moderate_impairment")

    # BMI category
    bmi = feature_map.get("bmi", 0) if "bmi" in feature_map else feature_map.get("bmi_calc", 0)
    if bmi < -0.5:
        buckets.append("bmi_underweight")
    elif bmi < 0:
        buckets.append("bmi_normal")
    elif bmi < 0.5:
        buckets.append("bmi_overweight")
    else:
        buckets.append("bmi_obese")

    # Lifestyle risk — smoking (risk factor) minus activity (protective factor)
    smoking = feature_map.get("smoking_encoded", 0)
    activity = feature_map.get("activity_encoded", 0.5)
    lifestyle_risk = smoking - activity
    if lifestyle_risk < -0.2:
        buckets.append("lifestyle_healthy")
    elif lifestyle_risk < 0.3:
        buckets.append("lifestyle_moderate_risk")
    else:
        buckets.append("lifestyle_high_risk")

    return buckets


def run_lda(features: np.ndarray, feature_names: List[str],
            n_topics: int = 4, alpha: float = 0.1, eta: float = 0.01) -> Dict[str, Any]:
    """
    LDA-based patient topic modeling (Ref-2).

    Discovers latent health themes in the patient's biomarker profile.
    Each topic represents a pattern of co-occurring biomarker abnormalities.

    Process:
        1. Convert biomarkers to categorical buckets (the "document").
        2. Generate a topic distribution using Dirichlet sampling.
        3. Adjust probabilities based on how many risk-related buckets are present.
        4. Simulate per-topic word distributions (which buckets each topic covers).

    Args:
        features: Normalized feature array.
        feature_names: Feature name list.
        n_topics: Number of latent disease topics (K) — default 4.
        alpha: Dirichlet prior for document-topic distribution (sparsity control).
        eta: Dirichlet prior for topic-word distribution.

    Returns:
        Dict containing:
            - topic_distribution: Per-patient topic probabilities θ_m (list of K floats).
            - dominant_topic: Index of the highest-probability topic.
            - topic_compositions: Top 3 buckets per topic with probabilities.
            - coherence_score: Simulated topic coherence (0.35-0.65).
            - patient_buckets: The categorical buckets assigned to this patient.
            - n_topics: Number of topics used.
    """
    # Step 1: Convert biomarkers to bucket representation (the "document")
    buckets = _biomarkers_to_buckets(features, feature_names)

    # Step 2: Simulate LDA topic distribution via Dirichlet sampling
    np.random.seed(hash(tuple(buckets)) % 2**31)
    raw_dist = np.random.dirichlet(np.ones(n_topics) * alpha)

    # Step 3: Adjust based on how many buckets indicate risk
    risk_buckets = [b for b in buckets if "elevated" in b or "diabetic" in b
                    or "impairment" in b or "obese" in b or "high_risk" in b]
    risk_ratio = len(risk_buckets) / max(len(buckets), 1)

    # Boost risk-related topics proportionally to risk ratio
    raw_dist[n_topics - 1] += risk_ratio * 0.3    # Last topic = highest risk
    raw_dist[0] += (1 - risk_ratio) * 0.3          # First topic = healthy
    topic_distribution = (raw_dist / raw_dist.sum()).tolist()

    # Step 4: Simulate per-topic word distributions (top 3 buckets per topic)
    topic_compositions = []
    for k in range(n_topics):
        np.random.seed(k * 42)
        topic_words = np.random.dirichlet(np.ones(len(BIOMARKER_BUCKETS)) * eta)
        top_indices = np.argsort(topic_words)[-3:]
        topic_compositions.append({
            "topic_id": k,
            "top_buckets": [
                {"bucket": BIOMARKER_BUCKETS[i], "probability": round(float(topic_words[i]), 4)}
                for i in top_indices
            ],
        })

    coherence_score = round(0.35 + np.random.random() * 0.3, 3)

    logger.info("lda_complete", n_topics=n_topics, coherence=coherence_score,
                dominant_topic=int(np.argmax(topic_distribution)))

    return {
        "topic_distribution": topic_distribution,
        "dominant_topic": int(np.argmax(topic_distribution)),
        "topic_compositions": topic_compositions,
        "coherence_score": coherence_score,
        "patient_buckets": buckets,
        "n_topics": n_topics,
    }
