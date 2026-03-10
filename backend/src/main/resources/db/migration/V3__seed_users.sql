-- =============================================================
-- V3: Seed default users
-- =============================================================

-- Password: admin123  (BCrypt hash with strength 12)
INSERT INTO users (id, username, email, password_hash, role) VALUES
    (uuid_generate_v4(), 'admin', 'admin@medicalml.local',
     '$2a$12$7Ki.rZJoIp3opcD6gXnhLuDwMm.JURrXaZ3jc1gqsbAL6KJZT9ENq',
     'ADMIN');

-- Password: doctor123
INSERT INTO users (id, username, email, password_hash, role) VALUES
    (uuid_generate_v4(), 'dr_smith', 'drsmith@medicalml.local',
     '$2a$12$rn/zKhir2oKDT83EMjTyqOtqycgx6sUzqG1ECQUic9YT./4ShxzEm',
     'DOCTOR');

-- Password: analyst123
INSERT INTO users (id, username, email, password_hash, role) VALUES
    (uuid_generate_v4(), 'analyst1', 'analyst@medicalml.local',
     '$2a$12$06L/JDBIc1tkZ/G249HO3eEQe0yP3rm08kXKOwmYv/kKjp.xUE.Tm',
     'ANALYST');
