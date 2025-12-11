-- ==========================================
-- V1 - Tabelas principais do sistema base
-- users + follows
-- ==========================================

-- ==========================
-- TABELA: users
-- ==========================
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       bio TEXT,
                       profile_image_url TEXT,
                       created_at TIMESTAMP DEFAULT NOW()
);

-- Índices recomendados
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);


-- ==========================
-- TABELA: follows
-- ==========================
CREATE TABLE follows (
                         follower_id BIGINT NOT NULL,
                         followed_id BIGINT NOT NULL,
                         created_at TIMESTAMP DEFAULT NOW(),

                         CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id)
                             REFERENCES users(id) ON DELETE CASCADE,

                         CONSTRAINT fk_follows_followed FOREIGN KEY (followed_id)
                             REFERENCES users(id) ON DELETE CASCADE,

                         CONSTRAINT pk_follows PRIMARY KEY (follower_id, followed_id)
);

-- Índices para melhorar performance
CREATE INDEX idx_follows_follower ON follows(follower_id);
CREATE INDEX idx_follows_followed ON follows(followed_id);