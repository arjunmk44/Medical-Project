<p align="center">
  <img src="https://img.icons8.com/3d-fluency/94/medical-doctor.png" width="80" alt="MedicalML Logo"/>
</p>

<h1 align="center">🏥 MedicalML Platform</h1>

<p align="center">
  <strong>AI-Powered Predictive Analytics for Latent Health Intelligence from Routine Medical Checkups</strong>
</p>

<p align="center">
  <a href="#-quick-start"><img src="https://img.shields.io/badge/Quick_Start-5_min_setup-blue?style=for-the-badge" alt="Quick Start"/></a>
  <a href="#-demo"><img src="https://img.shields.io/badge/Demo-Live_Preview-success?style=for-the-badge" alt="Demo"/></a>
  <a href="#-contributing"><img src="https://img.shields.io/badge/PRs-Welcome-brightgreen?style=for-the-badge" alt="PRs Welcome"/></a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/build-passing-brightgreen?logo=github-actions&logoColor=white" alt="Build Status"/>
  <img src="https://img.shields.io/badge/license-MIT-blue" alt="License"/>
  <img src="https://img.shields.io/badge/java-17-orange?logo=openjdk&logoColor=white" alt="Java 17"/>
  <img src="https://img.shields.io/badge/python-3.11-blue?logo=python&logoColor=white" alt="Python 3.11"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.2.3-6DB33F?logo=spring-boot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=white" alt="React"/>
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white" alt="Docker"/>
</p>

---

## 📖 Description

**MedicalML** is a full-stack medical intelligence platform that transforms routine health checkup data into actionable predictive insights. It implements an **8-layer ML pipeline** inspired by published clinical research ([PMC7028517 — Wang et al.](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC7028517/)), combining unsupervised pattern discovery, supervised risk prediction, and advanced interpretability to surface latent health conditions early.

By integrating **Spring Boot microservices**, a **FastAPI ML engine**, and a **React analytics dashboard**, MedicalML enables clinicians to upload patient records and receive AI-driven risk assessments with explainable insights — all from a single deploy command.

---

## ✨ Key Features

| Feature | Description |
|---------|-------------|
| 🔬 **8-Layer ML Pipeline** | End-to-end pipeline from data ingestion through risk scoring with SHAP-based explainability |
| 🧠 **Research-Grade Models** | Implements LDA topic modeling and Poisson Deviation Models from published clinical literature |
| 📊 **Interactive Dashboards** | Real-time cluster maps, radar charts, SHAP waterfalls, and Kaplan-Meier survival curves |
| 🔐 **Enterprise Security** | JWT RS256 authentication with role-based access control (Admin, Doctor, Analyst) |
| 📁 **Multi-Format Ingestion** | Supports CSV, Excel, JSON, HL7, and FHIR clinical data formats |
| ⚡ **One-Click Deploy** | Full Docker Compose orchestration — up and running in under 5 minutes |

---

## 🛠️ Tech Stack

<table>
  <tr>
    <td align="center" width="96"><img src="https://img.icons8.com/color/48/java-coffee-cup-logo.png" width="40"/><br><b>Java 17</b></td>
    <td align="center" width="96"><img src="https://img.icons8.com/color/48/spring-logo.png" width="40"/><br><b>Spring Boot</b></td>
    <td align="center" width="96"><img src="https://img.icons8.com/color/48/python.png" width="40"/><br><b>Python</b></td>
    <td align="center" width="96"><img src="https://img.icons8.com/color/48/react-native.png" width="40"/><br><b>React 18</b></td>
    <td align="center" width="96"><img src="https://img.icons8.com/color/48/postgreesql.png" width="40"/><br><b>PostgreSQL</b></td>
    <td align="center" width="96"><img src="https://img.icons8.com/color/48/docker.png" width="40"/><br><b>Docker</b></td>
  </tr>
</table>

| Layer | Technologies |
|-------|-------------|
| **Backend** | Spring Boot 3.2.3, Spring Security, Spring Data JPA, Flyway, Lombok, OpenPDF, SpringDoc OpenAPI |
| **ML Engine** | FastAPI, scikit-learn, XGBoost, LightGBM, SHAP, PyTorch, Gensim, Lifelines |
| **Frontend** | React 18, TypeScript, Vite 5, Tailwind CSS, Recharts, Zustand, TanStack Query |
| **Infrastructure** | Docker Compose, Nginx (reverse proxy), PostgreSQL 15, Maven |

---

## 🎬 Demo

<p align="center">
  <img src="https://via.placeholder.com/800x450/1a1a2e/e94560?text=📊+Dashboard+—+Patient+Analytics+%26+Risk+Scores" alt="Dashboard - Patient Analytics & Risk Scores" width="80%"/>
</p>

<p align="center"><em>Real-time patient analytics dashboard with risk scoring and trend visualization</em></p>

<details>
<summary>📸 More Screenshots</summary>

<br>

| Login & Auth | Patient Profile | ML Analysis |
|:---:|:---:|:---:|
| <img src="https://via.placeholder.com/350x200/16213e/0f3460?text=🔐+Secure+Login" alt="Login Page" width="100%"/> | <img src="https://via.placeholder.com/350x200/16213e/0f3460?text=👤+Patient+Profile" alt="Patient Profile" width="100%"/> | <img src="https://via.placeholder.com/350x200/16213e/0f3460?text=🧠+ML+Results" alt="ML Analysis Results" width="100%"/> |

| Upload & Ingest | SHAP Explanations | Cluster Visualization |
|:---:|:---:|:---:|
| <img src="https://via.placeholder.com/350x200/1a1a2e/e94560?text=📁+Data+Upload" alt="Data Upload" width="100%"/> | <img src="https://via.placeholder.com/350x200/1a1a2e/e94560?text=📈+SHAP+Waterfall" alt="SHAP Explanations" width="100%"/> | <img src="https://via.placeholder.com/350x200/1a1a2e/e94560?text=🗺️+Cluster+Map" alt="Cluster Visualization" width="100%"/> |

</details>

---

## 🚀 Quick Start

### Prerequisites

| Requirement | Version |
|-------------|---------|
| [Docker Desktop](https://www.docker.com/products/docker-desktop/) | Latest (with Docker Compose) |
| Git | 2.x+ |

### 5-Step Setup

```bash
# 1. Clone the repository
git clone https://github.com/your-username/medical-ml-platform.git
cd medical-ml-platform

# 2. Copy environment config
cp .env.example .env

# 3. (Optional) Customize environment variables
#    Edit .env to set your own DB credentials and JWT keys

# 4. Build and launch all services
docker compose up --build -d

# 5. Open the app
#    Navigate to http://localhost in your browser
```

> **That's it!** 🎉 The platform launches 5 containers (PostgreSQL, ML Service, Backend, Frontend, Nginx) and seeds default accounts automatically.

### Default Credentials

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | 🔴 ADMIN |
| `dr_smith` | `doctor123` | 🟢 DOCTOR |
| `analyst1` | `analyst123` | 🔵 ANALYST |

---

## 🏗️ Architecture

```
                    ┌─────────────────────────────────────────────────┐
                    │              Nginx Reverse Proxy (:80)          │
                    └───────────┬──────────────────┬──────────────────┘
                                │                  │
                    ┌───────────▼──────┐ ┌─────────▼────────┐
                    │   React + Vite   │ │ Spring Boot API  │
                    │   Frontend       │ │   Backend        │
                    │   (:3000)        │ │   (:8080)        │
                    └──────────────────┘ └────────┬─────────┘
                                                  │
                              ┌────────────────────┼────────────────────┐
                              │                    │                    │
                    ┌─────────▼────────┐ ┌─────────▼────────┐          │
                    │  FastAPI ML Svc  │ │  PostgreSQL 15   │          │
                    │  Python Engine   │ │  Data Store      │          │
                    │  (:8001)         │ │  (:5432)         │          │
                    └──────────────────┘ └──────────────────┘          │
                                                                       │
                              ┌─────────────────────────────────────────┘
                              │  JWT RS256 Auth + RBAC
                              └─────────────────────────────────────────
```

### ML Pipeline — 8 Layers

| Layer | Component | Implementation |
|:-----:|-----------|----------------|
| **1** | 📥 Data Ingestion | CSV, Excel, JSON, HL7, FHIR parsers with schema validation |
| **2** | 🧹 Preprocessing | Median + KNN imputation · IQR + Isolation Forest outlier detection · Normalization |
| **3** | 📐 Dimensionality Reduction | PCA (≥85% variance) · PyTorch Autoencoder (latent embeddings) |
| **4** | 🔮 Clustering | K-Means · Hierarchical · DBSCAN · GMM · **LDA** · **PDM** *(research models)* |
| **5** | 🎯 Supervised Learning | Random Forest · XGBoost · LightGBM · SVM · LogReg → **Soft Voting Ensemble** |
| **6** | 🔍 Interpretability | SHAP (TreeExplainer + KernelExplainer) · Permutation Importance |
| **7** | ⚠️ Risk Scoring | Weighted composite: Supervised 40% + Clustering 30% + Deviation 20% + PCA 10% |
| **8** | 📊 Visualization | Cluster maps · Radar charts · SHAP waterfalls · Kaplan-Meier survival curves |

<details>
<summary>📄 Research Model Details (PMC7028517)</summary>

- **LDA (Latent Dirichlet Allocation):** Biomarker → bucket encoding → patient-as-document → Dirichlet-Multinomial topic discovery for latent health pattern identification
- **PDM (Poisson Deviation Model):** Poisson(ϕ·e·γ) with age/sex GAM expected counts, Gamma patient multiplier, and standardized residuals for anomaly detection

</details>

---

## 📂 Project Structure

```
medical-ml-platform/
│
├── 🐳 docker-compose.yml           # 5-service orchestration
├── 🚀 launch.bat / launch.sh       # One-click launch scripts
├── ⚙️  .env.example                 # Environment variable template
├── 🌐 nginx/nginx.conf             # Reverse proxy configuration
├── 📄 sample-data/                  # Sample CSV for quick testing
│
├── 🐍 ml-service/                   # ═══ Python ML Microservice ═══
│   ├── main.py                      # FastAPI app + /analyze endpoint
│   ├── requirements.txt             # Python dependencies
│   ├── Dockerfile
│   └── pipeline/
│       ├── preprocessing.py         # Layer 2: Imputation, outliers, normalization
│       ├── dimensionality.py        # Layer 3: PCA + PyTorch Autoencoder
│       ├── clustering/              # Layer 4: Unsupervised pattern discovery
│       │   ├── kmeans_cluster.py
│       │   ├── hierarchical.py
│       │   ├── dbscan_cluster.py
│       │   ├── gmm.py
│       │   ├── lda_model.py         # Research: Latent Dirichlet Allocation
│       │   └── pdm_model.py         # Research: Poisson Deviation Model
│       ├── supervised/
│       │   └── ensemble.py          # Layer 5: Soft Voting Ensemble
│       ├── interpretability/
│       │   ├── shap_explainer.py    # Layer 6: SHAP explanations
│       │   └── permutation_imp.py   # Layer 6: Permutation importance
│       ├── risk_scoring.py          # Layer 7: Composite risk scores
│       └── survival_analysis.py     # Kaplan-Meier survival curves
│
├── ☕ backend/                       # ═══ Spring Boot 3.2.3 + Java 17 ═══
│   ├── pom.xml                      # Maven dependencies
│   ├── Dockerfile
│   └── src/main/java/com/medicalml/
│       ├── entity/                  # 7 JPA entities (Patient, Record, etc.)
│       ├── repository/              # 6 Spring Data JPA repositories
│       ├── security/                # JWT RS256 auth + RBAC filters
│       ├── service/                 # Auth, Patient, ML, Ingest services
│       ├── controller/             # REST API controllers
│       └── exception/              # Global exception handler
│
└── ⚛️  frontend/                     # ═══ React 18 + TypeScript + Vite ═══
    ├── package.json
    ├── Dockerfile
    ├── vite.config.ts
    └── src/
        ├── App.tsx                  # Router + layout
        ├── pages/                   # Login, Dashboard, Patients, Upload, Analytics
        ├── api/client.ts            # Axios HTTP client
        └── store/authStore.ts       # Zustand auth state management
```

---

## 🔌 API Endpoints

> Full interactive API docs available at **`/swagger-ui.html`** when the backend is running.

### 🔐 Authentication

| Method | Endpoint | Description | Auth |
|:------:|----------|-------------|:----:|
| `POST` | `/api/auth/login` | Authenticate & receive JWT token pair | ❌ |
| `POST` | `/api/auth/refresh` | Refresh access token | 🔑 |

### 👥 Patient Management

| Method | Endpoint | Description | Auth |
|:------:|----------|-------------|:----:|
| `GET` | `/api/patients` | List patients (paginated, searchable) | 🔑 |
| `GET` | `/api/patients/{id}` | Patient detail + medical history | 🔑 |
| `POST` | `/api/patients` | Create new patient record | 🔑 |

### 🧠 ML Analysis

| Method | Endpoint | Description | Auth |
|:------:|----------|-------------|:----:|
| `POST` | `/api/ingest/upload` | Upload CSV/Excel/JSON data files | 🔑 |
| `POST` | `/api/ml/analyze/{recordId}` | Run full 8-layer ML pipeline | 🔑 |
| `GET` | `/api/ml/results/{recordId}` | Retrieve ML analysis results | 🔑 |
| `GET` | `/api/alerts` | Active early warning alerts | 🔑 |

### 📊 Visualization

| Method | Endpoint | Description | Auth |
|:------:|----------|-------------|:----:|
| `GET` | `/api/viz/cluster-map` | Patient cluster visualization data | 🔑 |
| `GET` | `/api/viz/feature-importance` | SHAP feature importance rankings | 🔑 |
| `GET` | `/api/viz/correlation-heatmap` | Biomarker correlation matrix | 🔑 |

> 🔑 = Requires JWT Bearer token &nbsp;&nbsp; ❌ = Public

---

## 🛠️ Local Development (Without Docker)

<details>
<summary><b>🐍 ML Service</b></summary>

```bash
cd ml-service
python -m venv .venv
source .venv/bin/activate    # Windows: .venv\Scripts\activate
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8001 --reload
```
</details>

<details>
<summary><b>☕ Spring Boot Backend</b></summary>

```bash
cd backend
# Ensure PostgreSQL is running on localhost:5432
./mvnw spring-boot:run       # Windows: mvnw.cmd spring-boot:run
```
</details>

<details>
<summary><b>⚛️ React Frontend</b></summary>

```bash
cd frontend
npm install
npm run dev
# Opens at http://localhost:5173
```
</details>

---

## 🤝 Contributing

Contributions are welcome! Here's how to get started:

1. **Fork** the repository
2. **Create** a feature branch
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Commit** your changes with clear messages
   ```bash
   git commit -m "feat: add new clustering algorithm"
   ```
4. **Push** to your branch
   ```bash
   git push origin feature/your-feature-name
   ```
5. **Open** a Pull Request

### Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready, stable releases |
| `develop` | Integration branch for features |
| `feature/*` | Individual feature development |
| `fix/*` | Bug fixes |

### Commit Convention

We follow [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` — New features
- `fix:` — Bug fixes
- `docs:` — Documentation changes
- `refactor:` — Code refactoring
- `test:` — Adding or updating tests

---

## 📋 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

Free for educational - and research purposes.

---

## 👤 Author & Contact

<table>
  <tr>
    <td align="center">
      <strong>Achyuth</strong><br>
      <a href="https://github.com/your-username">
        <img src="https://img.shields.io/badge/GitHub-@your--username-181717?style=flat-square&logo=github" alt="GitHub"/>
      </a>
      <br>
      <a href="mailto:your.email@example.com">
        <img src="https://img.shields.io/badge/Email-Contact_Me-D14836?style=flat-square&logo=gmail&logoColor=white" alt="Email"/>
      </a>
    </td>
  </tr>
</table>

---

<p align="center">
  <sub>Built with ❤️ using Java, Python, React, and way too much coffee ☕</sub>
</p>

<p align="center">
  <a href="#-medicalml-platform">⬆ Back to Top</a>
</p>
