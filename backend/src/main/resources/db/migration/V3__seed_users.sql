-- =============================================================
-- V3: Seed default users
-- =============================================================

-- Password: admin123  (BCrypt hash with strength 12)
INSERT INTO users (id, username, email, password_hash, role) VALUES
    (uuid_generate_v4(), 'admin', 'admin@medicalml.local',
     '$2a$12$LJ3m4ks5GXsBKQ8RZmDMEeGUH6FPQqz3hB0vJi0kG4rX8nKz0YwJa',
     'ADMIN');

-- Password: doctor123
INSERT INTO users (id, username, email, password_hash, role) VALUES
    (uuid_generate_v4(), 'dr_smith', 'drsmith@medicalml.local',
     '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
     'DOCTOR');

-- Password: analyst123
INSERT INTO users (id, username, email, password_hash, role) VALUES
    (uuid_generate_v4(), 'analyst1', 'analyst@medicalml.local',
     '$2a$12$dHVP2V7EAm5wQfGz8kMGxuY8s7C4e0cE.0w3NvJH8nY/Xl3xaQ7Gu',
     'ANALYST');
