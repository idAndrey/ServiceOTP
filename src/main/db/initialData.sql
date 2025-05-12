-- Параметры кодов OTP
INSERT INTO config (length, ttl_seconds)
VALUES (6, 60)
ON CONFLICT DO NOTHING;

-- Администратор (для тестирования)
-- пароль по умолчанию `admin`
INSERT INTO users (username, password_hash, role)
VALUES (
    'admin',
    '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918',
    'admin@mail.ru',
    'ADMIN'
) ON CONFLICT DO NOTHING;

INSERT INTO operations (number, name, description)
VALUES
    (101, 'UpdatePassword', 'Изменение пароля пользователя'),
    (102, 'SendReport', 'Отправка отчёта'),
    (103, 'MakeTransfer', 'Выполнение перевода');
