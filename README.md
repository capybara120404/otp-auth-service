# OTP Auth Service

Приложение для генерации временных одноразовых кодов, реализует функционал регистрации, аутентификации, генерации и валидации кодов подтверждения с рассылкой по различным каналам.

## Стек
* **Язык**: Java 17
* **Сборка**: Maven
* **База данных**: PostgreSQL 18
* **Взаимодействие с БД**: JDBC
* **HTTP Сервер**: Встроенный сервер `com.sun.net.httpserver`
* **Авторизация**: Bearer Token
* **Логирование**: SLF4J + Logback
* **Рассылка уведомлений**:
  * **Email**: Jakarta Mail
  * **SMS**: `jsmpp` для интеграции с эмулятором SMPPsim
  * **Telegram**: Стандартный HTTP Client Java
  * **File**: Сохранение кодов локально в текстовый файл `otp_codes.txt`

## Структура
1. Слой **API**: хэндлеры и фильтры запросов.
2. Слой **Application**: сервисы с логикой.
3. Слой **Domain**: модели для описания данных.
4. Слой **Infrastructure**: репозитории для БД, интеграции с внешними сервисами, конфигурации.

Реализована фоновая задача, которая раз в минуту проверяет активные OTP-коды и переводит просроченные в статус `EXPIRED`, также работает задача очистки истекших токенов.


## Установка и запуск

### 1. Подготовка окружения
У вас должны быть установлены:
* JDK 17
* Maven 3.8+
* PostgreSQL 18

### 2. Настройка конфигурационных файлов
Перед запуском необходимо настроить каналы связи, для этого отредактируйте следующие файлы в папке `src/main/resources/`:

**email.properties**
```properties
email.username=email
email.password=password
email.from=email
mail.smtp.host=host
mail.smtp.port=0000
mail.smtp.auth=true
mail.smtp.starttls.enable=true
```

**sms.properties**
```properties
smpp.host=host
smpp.port=0000
smpp.system_id=system_id
smpp.password=password
smpp.system_type=type
smpp.source_addr=source
```

**telegram.properties**
```properties
telegram.bot_token=token
telegram.chat_id=chat_id
```

Нужно настроить подключение к базе данных и время жизни и длину кода, нужно создать файл `.env` в корне проекта:
```env
DATABASE_URL=jdbc:postgresql://localhost:5432/otp_service_db
DATABASE_USER=postgres
DATABASE_PASSWORD=postgres
OTP_DEFAULT_LIFE_TIME=300
OTP_DEFAULT_LENGTH=6
```

### 3. Сборка и запуск
Соберите проект с помощью Maven:
```bash
mvn clean package
```
Запустите класс `otp.auth.service.Main` через вашу IDE или скомпилированный JAR-файл. Сервер будет запущен на порту **8080**.

---

## API-эндпоинты и доступ

Все защищенные эндпоинты требуют передачи заголовка: `Authorization: Bearer <ваш_токен>`

### Публичные (без токена)

* `POST /api/register`
  * **Описание**: Регистрация нового пользователя.
  * **Тело (JSON)**: `{"username": "user", "password": "123", "role": "USER"}` (Роли: `USER` или `ADMIN`. Администратор может быть только один).

* `POST /api/login`
  * **Описание**: Вход и получение токена.
  * **Тело (JSON)**: `{"username": "user", "password": "123"}`
  * **Ответ**: Возвращает токен для дальнейшей аутентификации.

### Пользовательские

* `POST /api/otp/generate`
  * **Описание**: Сгенерировать и отправить код по выбранному каналу.
  * **Тело (JSON)**: `{"userId": 1, "channel": "TELEGRAM", "destination": "Название или ID"}`
  * **Поддерживаемые каналы (`channel`)**: `EMAIL`, `SMS`, `TELEGRAM`, `FILE`.

* `POST /api/otp/verify`
  * **Описание**: Проверка введенного кода.
  * **Тело (JSON)**: `{"userId": 1, "code": "123456"}`
  * **Ответ**: 200 если верно, 400 если код неверный или просрочен (`EXPIRED`).

### Административные

* `GET /api/admin/users`
  * **Описание**: Получить список всех пользователей, кроме администраторов.

* `DELETE /api/admin/users/{id}`
  * **Описание**: Удалить пользователя и все привязанные к нему OTP-коды.

* `PUT /api/admin/config`
  * **Описание**: Изменить глобальные настройки OTP (время жизни и длину).
  * **Тело (JSON)**: `{"lifeTimeSeconds": 600, "codeLength": 8}`

---

## Как протестировать

В корне проекта есть подготовленный файл конфигурации запросов `requests.http`.

Порядок базового сценария тестирования:
1. Установить и запустить локальный PostgreSQL.
2. Запустить приложение.
3. Отправить запрос на `POST /api/register` для создания обычного юзера.
4. Отправить `POST /api/login`, чтобы получить Token.
5. Выполнить запрос на `POST /api/otp/generate` передав полученный токен в Headers (`Authorization: Bearer <TOKEN>`) и выбрав `channel: "FILE"`.
6. Посмотреть сгенерированный код в появившемся файле `otp_codes.txt`.
7. Вызвать `POST /api/otp/verify` с этим кодом, чтобы проверить успешную валидацию.
8. Для админа, нужно зарегистрировать юзера с `role: "ADMIN"`, залогиниться под ним и попробовать отправить запрос `GET /api/admin/users`.
