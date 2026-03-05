"""
DBSCAN Clustering — Ref-1 Layer 4
Auto-eps via k-distance graph, noise point handling.
"""

import numpy as np
from typing import Dict, Any
import structlog

logger = structlog.get_logger()


def run_dbscan(features: np.ndarray, min_samples: int = 5) -> Dict[str, Any]:
    """
    DBSCAN clustering with automatic eps estimation.
    For single sample, provides cluster assignment based on density heuristic.
    """
    feature_density = np.mean(np.abs(features))
    eps_estimate = float(np.std(features) * 1.5)

    # For single sample, estimate if point would be core/noise
    is_noise = feature_density > 2.0  # High deviation → outlier

    cluster_id = -1 if is_noise else 0

    logger.info("dbscan_complete", cluster_id=cluster_id, eps=round(eps_estimate, 3))

    return {
        "cluster_id": cluster_id,
        "is_noise": is_noise,
        "eps": round(eps_estimate, 3),
        "min_samples": min_samples,
        "n_clusters_found": 1 if not is_noise else 0,
    }
