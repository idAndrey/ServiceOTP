-- Схема базы данных для сервиса кодов OTP

-- Пользователи
CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL CHECK (email ~* '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$'),
    role          VARCHAR(10) NOT NULL CHECK (role IN ('USER','ADMIN'))
);
-- Частичный уникальный индекс
-- для существования единственной записи с установленной ролью `ADMIN`
CREATE UNIQUE INDEX only_one_admin ON users (role) WHERE role = 'ADMIN';


-- Установка параметров кодов OTP (содержит только одну запись)
CREATE TABLE IF NOT EXISTS config (
    id          BIGSERIAL PRIMARY KEY,
    length      INT NOT NULL CHECK (length > 0),
    ttl_seconds INT NOT NULL CHECK (ttl_seconds > 0)
);
-- Частичный индекс с константой
-- для существования единственной записи в таблице
CREATE UNIQUE INDEX config_single_row ON config ((1));


-- Коды OTP для подтверждения операции (operation_number) пользователя (user_id)
CREATE TABLE IF NOT EXISTS codes (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    operation_number    VARCHAR(100) REFERENCES operations(number),
    code                VARCHAR(20) NOT NULL,
    status              VARCHAR(10) NOT NULL CHECK (status IN ('ACTIVE','USED','EXPIRED')),
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Операции и их числовой номер number
CREATE TABLE IF NOT EXISTS operations (
    id           BIGSERIAL PRIMARY KEY,
    number       INT NOT NULL UNIQUE,
    name         VARCHAR(100) NOT NULL,
    description  VARCHAR(500),
);