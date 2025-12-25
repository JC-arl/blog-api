-- =========================
-- ADMIN USERS (10)
-- =========================
INSERT INTO users (email, password_hash, nickname, role, status, provider, provider_id)
VALUES
    ('admin@club.com',  '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Ew5z9z6pG9v7K4l1kK9iK', '관리자1', 'ROLE_ADMIN', 'ACTIVE', 'LOCAL', 'admin1'),
