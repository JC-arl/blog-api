-- =========================
-- ADMIN USERS (1)
-- =========================
INSERT INTO users (email, password_hash, nickname, role, status, provider, provider_id)
VALUES
    ('admin@blog.com',  '$2a$10$yFNjUlpLZ3d.IBGJoCtHqeGEDUBmE7tW4B0SGtClR8OD1RxNLUr6q', '관리자', 'ROLE_ADMIN', 'ACTIVE', 'LOCAL', 'admin');
