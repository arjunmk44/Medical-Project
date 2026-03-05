-- =============================================================
-- V2: Create indexes for performance
-- =============================================================

-- Patients
CREATE INDEX idx_patients_mrn ON patients(mrn);
CREATE INDEX idx_patients_name ON patients(last_name, first_name);
CREATE INDEX idx_patients_not_deleted ON patients(id) WHERE is_deleted = FALSE;

-- Checkup Records
CREATE INDEX idx_checkup_patient ON checkup_records(patient_id);
CREATE INDEX idx_checkup_date ON checkup_records(record_date DESC);
CREATE INDEX idx_checkup_status ON checkup_records(status);

-- Biomarkers
CREATE INDEX idx_biomarkers_record ON biomarkers(record_id);

-- ML Results
CREATE INDEX idx_ml_results_record ON ml_analysis_results(record_id);
CREATE INDEX idx_ml_results_model ON ml_analysis_results(model_name);
CREATE INDEX idx_ml_results_label ON ml_analysis_results(health_label);

-- Latent Indices
CREATE INDEX idx_latent_record ON latent_indices(record_id);

-- Early Warnings
CREATE INDEX idx_warnings_patient ON early_warnings(patient_id);
CREATE INDEX idx_warnings_severity ON early_warnings(severity);
CREATE INDEX idx_warnings_unacked ON early_warnings(patient_id) WHERE acknowledged = FALSE;

-- Audit Log
CREATE INDEX idx_audit_user ON audit_log(user_id);
CREATE INDEX idx_audit_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_time ON audit_log(logged_at DESC);
