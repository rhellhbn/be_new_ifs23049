-- Buat database (jalankan terpisah jika belum ada)
-- CREATE DATABASE db_perpus_ifs23049;

-- Ekstensi UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabel users
CREATE TABLE IF NOT EXISTS users (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(100)  NOT NULL,
    username    VARCHAR(50)   NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    photo       VARCHAR(255)  NULL,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Tabel refresh_tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id       UUID         NOT NULL,
    refresh_token TEXT         NOT NULL,
    auth_token    TEXT         NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Tabel books
CREATE TABLE IF NOT EXISTS books (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID          NOT NULL,
    title       VARCHAR(255)  NOT NULL,
    author      VARCHAR(255)  NOT NULL,
    description TEXT          NOT NULL,
    genre       VARCHAR(100)  NOT NULL DEFAULT 'Umum',
    isbn        VARCHAR(50)   NULL,
    publisher   VARCHAR(255)  NULL,
    year        INTEGER       NULL,
    is_read     BOOLEAN       NOT NULL DEFAULT FALSE,
    cover       TEXT          NULL,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);
