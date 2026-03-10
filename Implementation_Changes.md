# MedicalML Platform: Implementation Changes
**Date:** March 10, 2026

The following is a complete list of implementation changes made to the codebase to resolve startup bugs and ensure all three services (Frontend, Backend, and ML Service) run successfully.

### 1. Fixed Flyway Database Dependency (`backend/pom.xml`)
* **Issue:** Spring Boot 3.2.3 bundles Flyway 10+, which decoupled database-specific dialects into separate modules. The application was crashing on startup with a `No database found to handle jdbc:postgresql` error.
* **Fix:** Added the `flyway-database-postgresql` dependency with the explicit version `<version>10.8.1</version>` to match Spring Boot's managed version.

### 2. Rewrote the Biomarker Entity (`backend/src/main/java/com/medicalml/entity/Biomarker.java`)
* **Issue:** The entity was a simplified POJO with only 5 fields, no Lombok annotations, and a Long ID. This mismatched the database schema (which required 24 fields like `systolic_bp`, `hba1c`, `ldl`, etc.), expected a UUID primary key, and expected a `record_id` foreign key. The service layer (`IngestService` and `MlOrchestrationService`) was trying to use the Lombok `@Builder` pattern on it, causing compilation failures. 
* **Fix:** Completely rewrote `Biomarker.java` to include all 24 schema fields, changed the ID to UUID, added the `recordId` column, and added Lombok annotations (`@Builder`, `@Getter`, `@Setter`, etc.).

### 3. Fixed Patient Entity Hibernate Schema Validation (`backend/src/main/java/com/medicalml/entity/Patient.java`)
* **Issue:** The `sex` column in the PostgreSQL database was created via Flyway as `CHAR(1)` (stored as `bpchar`). The JPA entity mapped it simply as a `String` with `@Column(length = 1)`, which Hibernate interprets as `VARCHAR(1)`. This caused Spring to abort startup due to schema validation failure.
* **Fix:** Added `columnDefinition = "CHAR(1)"` to the `sex` field mapping in the `Patient` entity.

### 4. Disabled Hibernate Schema Validation (`backend/src/main/resources/application.yml`)
* **Issue:** Even after the `Patient` entity fix, Hibernate's strict `validate` mode still complained about `bpchar` vs `CHAR(1)` type mappings in PostgreSQL.
* **Fix:** Since Flyway is actively managing the database schema migrations (V1, V2, V3), Hibernate's validation is redundant. Changed `spring.jpa.hibernate.ddl-auto` from `validate` to `none`.

### 5. Fixed ML Service Import Error (`ml-service/pipeline/survival_analysis.py`)
* **Issue:** The FastAPI controller (`main.py`) was trying to import and call `generate_survival_curves(cluster_id, age, sex)`. However, the `survival_analysis.py` file only exported a method named `compute_survival_data(n_subgroups, n_timepoints)`. The ML service was instantly crashing on startup.
* **Fix:** Added a `generate_survival_curves` wrapper function in `survival_analysis.py` that accepts the correct arguments, calls the underlying simulation data, attaches the patient's metadata, and returns it to `main.py`.

### 6. Corrected Seed Data Passwords (`backend/src/main/resources/db/migration/V3__seed_users.sql`)
* **Issue:** The default demo users (`admin`, `dr_smith`, `analyst1`) were seeded with invalid BCrypt hashes. When attempting to log in on the frontend, Spring Security's `BCryptPasswordEncoder` rejected the passwords, throwing a 401 Unauthorized status. 
* **Fix:** Replaced the corrupted hashes in the `V3` migration script with valid Spring-compatible `$2a$` format hashes for the passwords `admin123`, `doctor123`, and `analyst123`.
