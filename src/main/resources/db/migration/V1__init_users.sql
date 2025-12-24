CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,

                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255),

                       nickname VARCHAR(50) NOT NULL,

                       role VARCHAR(20) NOT NULL COMMENT 'ROLE_USER, ROLE_ADMIN',
                       status VARCHAR(20) NOT NULL COMMENT 'ACTIVE, SUSPENDED',

                       provider VARCHAR(20) NOT NULL COMMENT 'LOCAL, GOOGLE, FIREBASE',
                       provider_id VARCHAR(255),

                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                       INDEX idx_users_email (email),
                       INDEX idx_users_provider (provider, provider_id)
);
CREATE TABLE categories (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(50) NOT NULL UNIQUE,
                            created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE posts (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,

                       user_id BIGINT NOT NULL,
                       category_id BIGINT NOT NULL,

                       title VARCHAR(100) NOT NULL,
                       content TEXT NOT NULL,

                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                       CONSTRAINT fk_posts_user
                           FOREIGN KEY (user_id) REFERENCES users(id),
                       CONSTRAINT fk_posts_category
                           FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE INDEX idx_posts_user ON posts(user_id);
CREATE INDEX idx_posts_category ON posts(category_id);

CREATE TABLE comments (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,

                          post_id BIGINT NOT NULL,
                          user_id BIGINT NOT NULL,

                          content VARCHAR(500) NOT NULL,

                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_comments_post
                              FOREIGN KEY (post_id) REFERENCES posts(id),
                          CONSTRAINT fk_comments_user
                              FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_comments_post ON comments(post_id);

CREATE TABLE likes (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,

                       post_id BIGINT NOT NULL,
                       user_id BIGINT NOT NULL,

                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_likes_post
                           FOREIGN KEY (post_id) REFERENCES posts(id),
                       CONSTRAINT fk_likes_user
                           FOREIGN KEY (user_id) REFERENCES users(id),

                       CONSTRAINT uk_likes UNIQUE (post_id, user_id)
);
