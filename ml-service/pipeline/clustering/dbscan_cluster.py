"""
DBSCAN Clustering — Ref-1 Layer 4.

Density-Based Spatial Clustering of Applications with Noise.
Unlike K-Means, DBSCAN does not require specifying the number of clusters
upfront. Instead, it discovers clusters based on data density.

Key parameters:
    - eps: Neighborhood radius — points within this distance are considered neighbors.
    - min_samples: Minimum points to form a dense region (core point threshold).

Advantages for medical data:
    - Handles non-spherical cluster shapes.
    - Identifies noise/outlier points (patients that don't fit any cluster).
    - Does not require knowing the number of clusters in advance.

For single-sample inference, we estimate whether the patient would be
classified as a core point or noise based on their feature density.
"""

import numpy as np
from typing import Dict, Any
import structlog

logger = structlog.get_logger()


def run_dbscan(features: np.ndarray, min_samples: int = 5) -> Dict[str, Any]:
    """
    DBSCAN clustering with automatic eps estimation.

    For single-sample inference:
        - Estimates eps from the feature standard deviation.
        - Classifies the patient as noise if their mean absolute feature value
          exceeds 2.0 standard deviations (indicating an outlier patient).

    Cluster ID semantics:
        - 0: Core point (belongs to a dense cluster)
        - -1: Noise point (outlier, doesn't fit any cluster)

    Args:
        features: Normalized 1D numpy array of patient features.
        min_samples: Minimum neighbors to be a core point (default: 5).

    Returns:
        Dict containing:
            - cluster_id: 0 (core) or -1 (noise).
            - is_noise: Boolean indicating outlier status.
            - eps: Estimated neighborhood radius.
            - min_samples: Configuration parameter.
            - n_clusters_found: 1 if core, 0 if noise.
    """
    # Mean absolute feature value as density proxy
    feature_density = np.mean(np.abs(features))

    # Estimated eps from feature spread
    eps_estimate = float(np.std(features) * 1.5)

    # High deviation signals an outlier (noise point)
    is_noise = feature_density > 2.0

    cluster_id = -1 if is_noise else 0

    logger.info("dbscan_complete", cluster_id=cluster_id, eps=round(eps_estimate, 3))

    return {
        "cluster_id": cluster_id,
        "is_noise": is_noise,
        "eps": round(eps_estimate, 3),
        "min_samples": min_samples,
        "n_clusters_found": 1 if not is_noise else 0,
    }
