-- =============================================================
-- V1: Create all tables for Medical ML Platform
-- =============================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- -----------------------------------------------
-- Users (Clinical Staff)
-- -----------------------------------------------
CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username    VARCHAR(100) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role        VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'DOCTOR', 'ANALYST')),
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- -----------------------------------------------
-- Patients
-- -----------------------------------------------
CREATE TABLE patients (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mrn         VARCHAR(50) NOT NULL UNIQUE,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    age         INT,
    sex         CHAR(1) CHECK (sex IN ('M', 'F', 'O')),
    date_of_birth DATE,
    is_deleted  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- -----------------------------------------------
-- Checkup Records (raw uploads)
-- -----------------------------------------------
CREATE TABLE checkup_records (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id      UUID NOT NULL REFERENCES patients(id),
    record_date     DATE NOT NULL,
    source_format   VARCHAR(20) NOT NULL CHECK (source_format IN ('CSV', 'EXCEL', 'JSON', 'HL7', 'FHIR')),
    raw_json        JSONB,
    status          VARCHAR(20) NOT NULL DEFAULT 'UPLOADED' CHECK (status IN ('UPLOADED', 'PARSED', 'ANALYZED', 'ERROR')),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- -----------------------------------------------
-- Biomarkers (extracted structured data)
-- -----------------------------------------------
CREATE TABLE biomarkers (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    record_id       UUID NOT NULL REFERENCES checkup_records(id),
    -- Vital & Cardiovascular
    systolic_bp     DECIMAL(6,2),
    diastolic_bp    DECIMAL(6,2),
    heart_rate      DECIMAL(6,2),
    -- Metabolic Panel
    glucose         DECIMAL(8,2),
    hba1c           DECIMAL(5,2),
    total_cholesterol DECIMAL(8,2),
    ldl             DECIMAL(8,2),
    hdl             DECIMAL(8,2),
    triglycerides   DECIMAL(8,2),
    -- Organ Function (Liver)
    alt             DECIMAL(8,2),
    ast             DECIMAL(8,2),
    alp             DECIMAL(8,2),
    -- Organ Function (Kidney)
    creatinine      DECIMAL(6,3),
    bun             DECIMAL(8,2),
    egfr            DECIMAL(8,2),
    -- Anthropometric
    height_cm       DECIMAL(6,2),
    weight_kg       DECIMAL(6,2),
    bmi             DECIMAL(5,2),
    waist_cm        DECIMAL(6,2),
    hip_cm          DECIMAL(6,2),
    waist_hip_ratio DECIMAL(4,3),
    skinfold_mm     DECIMAL(6,2),
    -- Lifestyle
    smoking_status      VARCHAR(20) CHECK (smoking_status IN ('NEVER', 'FORMER', 'CURRENT')),
    alcohol_units       DECIMAL(5,1),
    activity_level      VARCHAR(20) CHECK (activity_level IN ('SEDENTARY', 'LIGHT', 'MODERATE', 'ACTIVE', 'VERY_ACTIVE')),
    sleep_hours         DECIMAL(4,1),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- -----------------------------------------------
-- ML Analysis Results
-- -----------------------------------------------
CREATE TABLE ml_analysis_results (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    record_id               UUID NOT NULL REFERENCES checkup_records(id),
    model_name              VARCHAR(100) NOT NULL,
    health_label            VARCHAR(50),
    risk_score              DECIMAL(5,4),
    cluster_id              INT,
    cluster_label           VARCHAR(100),
    lda_topic_distribution  JSONB,
    pdm_topic_distribution  JSONB,
    pca_components          JSONB,
    shap_values             JSONB,
    analyzed_at             TIMESTAMP NOT NULL DEFAULT NOW()
);

-- -----------------------------------------------
-- Latent Health Indices
-- -----------------------------------------------
CREATE TABLE latent_indices (
    id                          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    record_id                   UUID NOT NULL REFERENCES checkup_records(id),
    pca_index                   JSONB,
    autoencoder_embedding       JSONB,
    metabolic_composite         DECIMAL(8,4),
    cardiovascular_composite    DECIMAL(8,4),
    organ_function_composite    DECIMAL(8,4),
    created_at                  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- -----------------------------------------------
-- Early Warnings
-- -----------------------------------------------
CREATE TABLE early_warnings (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id      UUID NOT NULL REFERENCES patients(id),
    record_id       UUID REFERENCES checkup_records(id),
    warning_type    VARCHAR(100) NOT NULL,
    severity        VARCHAR(20) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    message         TEXT NOT NULL,
    recommendation  TEXT,
    triggered_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    acknowledged    BOOLEAN NOT NULL DEFAULT FALSE,
    acknowledged_at TIMESTAMP
);

-- -----------------------------------------------
-- Audit Log
-- -----------------------------------------------
CREATE TABLE audit_log (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID REFERENCES users(id),
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id   UUID,
    details     JSONB,
    logged_at   TIMESTAMP NOT NULL DEFAULT NOW()
);
