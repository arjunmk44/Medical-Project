# 🏥 MedicalML — Medical Report Prediction & Latent Health Intelligence System

> ML-driven predictive analytics from routine medical checkup data.  
> Implements the 8-layer architecture from **Ref-1** (Architecture.docx) and **Ref-2** (PMC7028517 — Wang et al.)

---

## 🚀 Quick Start

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (with Docker Compose)

### Launch (Double-Click)
- **Windows**: Double-click `launch.bat`
- **macOS/Linux**: Run `./launch.sh`

This builds all containers and opens the app at **http://localhost**

### Default Login
| Username | Password | Role    |
|----------|----------|---------|
| admin    | admin123 | ADMIN   |
| dr_smith | doctor123| DOCTOR  |
| analyst1 | analyst123| ANALYST |

---

## 🏗️ Architecture

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────────┐
│   React UI   │────▶│ Spring Boot API  │────▶│  Python ML Svc   │
│  (Port 3000) │     │   (Port 8080)    │     │   (Port 8001)    │
└──────────────┘     └────────┬─────────┘     └──────────────────┘
                              │
                     ┌────────▼─────────┐
                     │   PostgreSQL 16   │
                     │   (Port 5432)     │
                     └──────────────────┘
```

### ML Pipeline (8 Layers — Ref-1)
| Layer | Component | Implementation |
|-------|-----------|----------------|
| 1 | Data Ingestion | CSV/Excel/JSON/HL7/FHIR parser |
| 2 | Preprocessing | Median + KNN imputation, IQR + IsolationForest outliers, normalization |
| 3 | Dimensionality | PCA (≥85% variance), PyTorch Autoencoder (latent embeddings) |
| 4 | Clustering | K-Means, Hierarchical, DBSCAN, GMM, **LDA** (Ref-2), **PDM** (Ref-2) |
| 5 | Supervised | Random Forest, XGBoost, LightGBM, SVM, LogReg → Soft Voting Ensemble |
| 6 | Interpretability | SHAP (TreeExplainer + KernelExplainer), Permutation Importance |
| 7 | Risk Scoring | Weighted composite (supervised 40% + clustering 30% + deviation 20% + PCA 10%) |
| 8 | Visualization | Cluster maps, radar charts, SHAP waterfalls, Kaplan-Meier curves |

### Ref-2 Models (PMC7028517)
- **LDA**: Biomarker → bucket encoding → patient-as-document → Dirichlet-Multinomial topic discovery
- **PDM**: Poisson(ϕ·e·γ) with age/sex GAM expected counts, Gamma patient multiplier, standardized residuals

---

## 📂 Project Structure

```
medical-ml-platform/
├── docker-compose.yml          # 5-service orchestration
├── launch.bat / launch.sh      # Double-click launch scripts
├── .env.example                # Environment template
├── nginx/nginx.conf            # Reverse proxy config
│
├── ml-service/                 # Python ML Microservice
│   ├── main.py                 # FastAPI application
│   └── pipeline/               # ML pipeline modules
│       ├── preprocessing.py    # Layer 2
│       ├── dimensionality.py   # Layer 3 (PCA + Autoencoder)
│       ├── clustering/         # Layer 4
│       │   ├── kmeans_cluster.py
│       │   ├── hierarchical.py
│       │   ├── dbscan_cluster.py
│       │   ├── gmm.py
│       │   ├── lda_model.py    # Ref-2 LDA
│       │   └── pdm_model.py    # Ref-2 PDM
│       ├── supervised/ensemble.py         # Layer 5
│       ├── interpretability/              # Layer 6
│       │   ├── shap_explainer.py
│       │   └── permutation_imp.py
│       ├── risk_scoring.py                # Layer 7
│       └── survival_analysis.py           # Ref-2 KM curves
│
├── backend/                    # Spring Boot 3.2.3 + Java 17
│   └── src/main/java/com/medicalml/
│       ├── entity/             # 7 JPA entities
│       ├── repository/         # 6 Spring Data repos
│       ├── security/           # JWT RS256 auth
│       ├── service/            # Auth, Patient, ML, Ingest
│       ├── controller/         # REST API endpoints
│       └── exception/          # Global error handler
│
└── frontend/                   # React 18 + Vite + Tailwind
    └── src/
        ├── pages/              # Login, Dashboard, Patients, Profile, Upload, Analytics
        ├── components/         # Sidebar, Charts
        └── store/              # Zustand auth state
```

---

## 🔌 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | JWT authentication |
| POST | `/api/auth/refresh` | Token refresh |
| GET | `/api/patients` | List patients (paginated, searchable) |
| GET | `/api/patients/{id}` | Patient detail + history |
| POST | `/api/patients` | Create patient |
| POST | `/api/ingest/upload` | Upload CSV/Excel/JSON |
| POST | `/api/ml/analyze/{recordId}` | Run ML pipeline |
| GET | `/api/ml/results/{recordId}` | Get ML results |
| GET | `/api/alerts` | Active early warnings |
| GET | `/api/viz/cluster-map` | Cluster visualization data |
| GET | `/api/viz/feature-importance` | SHAP importance data |
| GET | `/api/viz/correlation-heatmap` | Biomarker correlations |

---

## 🛠️ Local Development (Without Docker)

### ML Service
```bash
cd ml-service
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8001 --reload
```

### Backend
```bash
cd backend
./mvnw spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

---

## 📋 License

MIT License — for educational and research purposes.
