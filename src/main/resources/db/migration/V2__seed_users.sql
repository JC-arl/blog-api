-- =========================
-- ADMIN USERS (10)
-- =========================
INSERT INTO users (email, password_hash, nickname, role, status, provider, provider_id)
VALUES
    ('admin1@club.com',  '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Ew5z9z6pG9v7K4l1kK9iK', '관리자1', 'ROLE_ADMIN', 'ACTIVE', 'LOCAL', 'admin1'),
    ('admin2@club.com',  '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Ew5z9z6pG9v7K4l1kK9iK', '관리자2', 'ROLE_ADMIN', 'ACTIVE', 'LOCAL', 'admin2'),
    ('admin3@club.com',  '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Ew5z9z6pG9v7K4l1kK9iK', '관리자3', 'ROLE_ADMIN', 'ACTIVE', 'LOCAL', 'admin3'),
    ('admin4@club.com',  '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Ew5z9z6pG9v7K4l1kK9iK', '관리자4', 'ROLE_ADMIN', 'ACTIVE', 'LOCAL', 'admin4'),
    ('admin5@club.com',  '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Ew5z9z6pG9v7K4l1kK9iK', '관리자5', 'ROLE_ADMIN', 'ACTIVE', 'LOCAL', 'admin5'),
    ('admin6@club.com',  '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Ew5z9z6pG9v7K4l1kK9iK', '관리자6', 'ROLE_ADMIN', 'ACTIVE', 'LOCAL', 'admin6'),
    ('admin7@club.com',  '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Ew5z9z6pG9v7K4l1kK9iK', '관리자7', 'ROLE_ADMIN', 'ACTIVE', 'LOCAL', 'admin7'),
    ('admin8@club.com',  '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Ew5z9z6pG9v7K4l1kK9iK', '관리자8', 'ROLE_ADMIN', 'ACTIVE', 'LOCAL', 'admin8'),
    ('admin9@club.com',  '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Ew5z9z6pG9v7K4l1kK9iK', '관리자9', 'ROLE_ADMIN', 'ACTIVE', 'LOCAL', 'admin9'),
    ('admin10@club.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Ew5z9z6pG9v7K4l1kK9iK', '관리자10','ROLE_ADMIN', 'ACTIVE', 'LOCAL', 'admin10');

-- =========================
-- NORMAL categories
-- =========================
INSERT INTO categories (name) VALUES
                                  ('공지'),
                                  ('자유'),
                                  ('스터디'),
                                  ('프로젝트'),
                                  ('질문'),
                                  ('후기'),
                                  ('행사'),
                                  ('모집'),
                                  ('자료'),
                                  ('기타');

INSERT INTO posts (user_id, category_id, title, content)
VALUES
    (2, 1, '첫 번째 게시글', '내용입니다'),
    (3, 2, '스터디 모집합니다', '같이 공부해요'),
    (4, 3, '프로젝트 후기', '정말 힘들었습니다');

INSERT INTO comments (post_id, user_id, content)
VALUES
    (1, 3, '좋은 글이네요'),
    (1, 4, '동의합니다'),
    (2, 5, '참여하고 싶어요'),
    (3, 6, '감사합니다');

INSERT INTO likes (post_id, user_id)
VALUES
    (1, 2),
    (1, 3),
    (2, 4),
    (2, 5),
    (3, 6);

