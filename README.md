# Сервис кодов OTP  

Сервис для создания и применения кодов One Time Password (OTP) для подтверждения выполнения тех или иных операций.  
Код OTP отправляется пользователю по Email, в виде SMS (SMPP эмулятор), в Telegram бот или сохраняется в файл.  
Сервис разработан на базе простого HTTP-сервера Java.

**Для демонстрации работы сервиса** изменение пароля пользователя подтверждается с помощью кода OTP

---
## Реализованные функции сервиса (API-запросы)  
- регистрация пользователей с разделением ролей (пользователь, администратор);
- аутентификация пользователей с применением JWT-токенов;
- функции администратора:
  - установка времени действия и количество символов кода OTP; 
  - получение списка пользователей (без администратора);
  - удаление учётной записи пользователя и удаление кодов OTP пользователя в БД;
- функции пользователя:
  - получить список доступных операций;
  - выполнить операцию с подтверждением выполнения по коду OTP;
  - отправка кодов OTP по следующим каналам:
    - по электронной почте;
    - в виде короткого сообщения SMS (на эмулятор SMPPsim);
    - в телеграм бота;
    - сохранение кода в файл;
- проверка времени действия кодов OTP с периодичностью; `XXXXXXXXXXXXXXXXXXXXXXXX`
- логирование в консоль сервиса результатов выполнения запросов к сервису с помощью SLF4J/Logback;
- ограничение на регистрацию только одного пользователя с ролью администратора.

## Поддерживаемые операции
| Короткий № | Имя операции    | Описание                      |
|:----------:|:----------------|:------------------------------|
|   `101`    | UpdatePassword  | Изменение пароля пользователя |
|   `102`    | SendReport      | Отправка отчёта               |
|   `103`    | MakeTransfer    | Выполнение перевода           |

Параметры операции передаются в формате JSON:  

| Имя операции    | Параметр     | Комментарий    |
|:----------------|:-------------|:---------------|
| UpdatePassword  | `password`   | новый пароль   |
|   SendReport    | `reportType` | тип отчёта     |
|  MakeTransfer   | `amount`     | сумма перевода |

Передача параметра операции в запросе обязательна.  

Валидация кода OTP выполняется для всех операций.  

Операция `UpdatePassword` полностью реализована и выполняет обновление пароля пользователя в БД после подтверждения кодом OTP.  
Выполнение операций `SendReport` и `MakeTransfer` эмулируется задержкой потока на 2 секунды.  
В целях иллюстрации работы сервиса операция `SendReport` всегда завершается успешно,  
и операция `MakeTransfer` никогда не завершается успешно.  

## API-запросы
### Публичные маршруты:
- **/register** регистрация нового пользователя
- **/login** вход в сервис  

### Маршруты для пользователей (роль USER):
- **/operations** получение списка операций
- **/operation/perform** выполнение операции
- **/operation/confirm** подтверждение выполнения операции

### Маршруты для администратора (роль ADMIN)
- **/admin/config** установка параметров кодов OTP
- **/admin/users** получение списка зарегистрированных пользователей
- **/admin/users/2** удаление пользователя с id = 2  

В заголовке непубличного запросах передаётся токен авторизации `Authorization: Bearer TOKEN`  

| Метод  | Маршрут            | Параметры JSON                  |
|:-------|:-------------------|:--------------------------------|
| POST   | /register          | username, password, email, role |
| POST   | /login             | username, password              |
| GET    | /operations        | -                               |
| POST   | /operation/perform | operationNumber, channel        |
| PATCH  | /operation/confirm | code, [параметры операции]      |
| PATCH  | /admin/config      | length, ttlSeconds              |
| GET    | /admin/users       | -                               |
| DELETE | /admin/users/[id]  | -                               |


## Настройка и запуск
Для работы сервиса необходимы:
  - Java Runtime Environment (Java 23)
  - PostgreSQL 17
  - Maven 4.0.0
  - эмулятор SMPPsim 3.0.2 (скачайте по [ссылке](https://github.com/delhee/SMPPSim/releases/tag/3.0.0))

Создайте базу данных `service_otp`  

Создайте таблицы `users`, `codes`, `config`, используя SQL-скрипты в папке `scr\main\db\` в файлах:
- schemaDB.sql
- initialData.sql

В таблице config установлен частичный индекс с константой для обеспечения в таблице единственной записи.  
В таблице users установлен частичный уникальный индекс для обеспечения в таблице единственной записи с установленной ролью `ADMIN`  

Установите настройки в конфигурационных файлах сервиса в папке `src/main/resources`:
- `application.properties` - параметры базы данных
- `email.properties` - SMTP сервер
- `sms.properties` - эмулятор отправки SMS-сообщения SMPP
- `telegram.properties` - токен телеграм бота и ваш chatId
- `logback.xml` - настройки формата вывода логов

Для отправки кода OTP в телеграм-бот используйте заданный по умолчанию @newserviceotpcode_0203_bot или создайте свой бот.  
Чтобы получать коды OTP в телеграм-бот начните с ним диалог, выполнив команду /start.  
Для получения своего chatId воспользуйтесь ботом @getmyid_bot.

Клонируйте репозиторий:

`XXXXXXXXXXXXXXXXXXXXXXXX`
```bash
git clone https://github.com/idAndrey/ServiceOTP.git  
cd ServiceOTP
```

Соберите проект и запустите приложение:

```bash
mvn clean package
java -jar target/service-otp-1.0-SNAPSHOT.jar
```

## Пример использования сервиса

### Регистрация пользователя

```bash
curl.exe -X POST http://localhost:8080/register -H "Content-Type: application/json" -d '{\"username\":\"user1\",\"password\":\"password123\",\"role\":\"USER\"}'
```

### Вход (получение токена)

```bash
curl.exe -X POST http://localhost:8080/login -H "Content-Type: application/json" -d '{\"username\":\"user1\",\"password\":\"password123\"}'
```
### Просмотр доступных операций
```bash
curl.exe -X GET http://localhost:8080/operations -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_TOKEN"
```

### Выполнение операции 101 UpdatePassword с отправкой кода OTP в Телеграм-бот

```bash
curl.exe -X POST http://localhost:8080/operation/perform -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_TOKEN" -d '{\"operationNumber\":101,\"channel\":\"TELEGRAM\"}'
```

### Подтверждение кодом OTP выполнения операции 101 UpdatePassword

```bash
curl.exe -X PATCH http://localhost:8080/operation/confirm -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_TOKEN" -d '{\"code\":\"443878\",\"passsword\":\"password456\"}'
```

Пароль для пользователя `user1` будет изменён на `password456` 

### Действия администратора
- **Вход пользователя с ролью ADMIN**  
  Получаем токен администратора ADMIN_TOKEN
```bash
curl.exe -X POST http://localhost:8080/login -H "Content-Type: application/json" -d '{\"username\":\"admin\",\"password\":\"admin\"}'
```
- **Изменение параметров OTP**
```bash
curl.exe -X PATCH http://localhost:8080/admin/config -H "Content-Type: application/json" -H "Authorization: Bearer ADMIN_TOKEN" -d '{\"length\":6,\"ttlSeconds\":300}'
```
- **Просмотр зарегистрированных пользователей**  
  (за исключением пользователей с ролью ADMIN)
```bash
curl.exe -X GET http://localhost:8080/admin/users -H "Authorization: Bearer ADMIN_TOKEN"
```
- **Удаление пользователя**  
  удаление пользователя с id = 2 (добавить в конец к маршруту /admin/users/[id] )
```bash
curl.exe -X DELETE http://localhost:8080/admin/users/2 -H "Authorization: Bearer ADMIN_TOKEN"
```

---


## Поддержка

Если у вас возникли вопросы по работе сервиса или сложности при его запуске, создайте
[обсуждение](https://github.com/idAndrey/issues/new/choose) в данном репозитории или свяжитесь с разработчиком по электронной почте <a href="mailto:mail@example.com"><ermakov.andrey@ya.ru></a> или телеграм [Андрей Ермаков](https://t.me/pc022979700).  


