# Travel Day Planner

REST API для планирования дня в городе. Пользователь вводит адрес, временной интервал и темп прогулки — система автоматически геокодирует адрес, подбирает подходящие места поблизости и строит оптимальное расписание на день с учётом времени в пути между точками.

---

## 1. Название и назначение

Проект реализован как три независимых микросервиса:

| Сервис | Порт | Роль |
|--------|------|------|
| `location-context-service` | 8081 | Точка входа. Геокодирует адрес, рассчитывает радиус пешеходной доступности через изохроны, оркестрирует вызовы к двум другим сервисам, возвращает клиенту готовый маршрут |
| `places-service` | 8082 | Ищет достопримечательности вблизи заданных координат через OpenTripMap API, фильтрует по категориям, рассчитывает время посещения, сортирует по рейтингу |
| `planner-service` | 8083 | Принимает список мест, фильтрует их через AI (DeepSeek), строит оптимальное расписание с учётом времени в пути и графика работы мест |

Клиент отправляет **один запрос** в `location-context-service` и получает полный маршрут на день.

---

## 2. Архитектура и зависимости

### Технологии

- **Java 21**
- **Spring Boot 3.x**
- **Gradle** (Groovy DSL)
- **JUnit 5** + **Mockito**
- **Docker** + **Docker Compose**
- **Checkstyle** (Sun Style) — статический анализ кода
- **Spotless** (google-java-format) — автоформатирование кода

### Взаимодействие между сервисами

```
Клиент
  └─► location-context-service :8081
            ├─► places-service :8082      (поиск мест по координатам)
            └─► planner-service :8083     (построение расписания)
```

`location-context-service` вызывает `places-service` и `planner-service` по HTTP.
Остальные сервисы не обращаются друг к другу напрямую.

### Внешние сервисы

| Сервис | Используется в | Назначение |
|--------|---------------|------------|
| [OpenTripMap API](https://dev.opentripmap.org/product) | places-service | Поиск мест по координатам и категориям |
| [LocationIQ API](https://locationiq.com) | location-context-service | Геокодинг адреса в координаты |
| [OpenRouteService Isochrones](https://openrouteservice.org) | location-context-service | Расчёт радиуса пешеходной доступности |
| [DeepSeek API](https://api.deepseek.com) | planner-service | AI-фильтрация мест перед построением маршрута |

---

## 3. Способы запуска

### Запуск через Docker Compose (рекомендуется)

Создайте файл `.env` в корне репозитория:

```env
OPENTRIPMAP_API_KEY=ваш_ключ
GEOCODING_API_KEY=ваш_ключ
ISOCHRONE_API_KEY=ваш_ключ
DEEPSEEK_API_KEY=ваш_ключ
```

Затем запустите:

```bash
docker-compose up --build
```

Docker Compose автоматически подхватит переменные из `.env`. `DEEPSEEK_API_KEY` необязателен — без него AI-фильтрация просто пропускается.

Остановить:
```bash
docker-compose down
```

### Локальный запуск без Docker

Каждый сервис запускается отдельно из своей папки:

**places-service:**
```bash
cd places-service
OPENTRIPMAP_API_KEY=ваш_ключ ./gradlew bootRun
```

**planner-service:**
```bash
cd planner-service
DEEPSEEK_API_KEY=ваш_ключ ./gradlew bootRun
```

**location-context-service:**
```bash
cd location-context-service
GEOCODING_API_KEY=ваш_ключ \
ISOCHRONE_API_KEY=ваш_ключ \
./gradlew bootRun
```

### Переменные окружения

| Переменная | Сервис | Обязательная | Описание |
|-----------|--------|------|----------|
| `OPENTRIPMAP_API_KEY` | places-service | да | API-ключ OpenTripMap. [Получить ключ](https://dev.opentripmap.org/product) · [Документация](https://dev.opentripmap.org/docs) |
| `GEOCODING_API_KEY` | location-context-service | да | API-ключ LocationIQ. [Получить ключ](https://locationiq.com/register) · [Документация](https://locationiq.com/docs) |
| `ISOCHRONE_API_KEY` | location-context-service | да | API-ключ OpenRouteService. [Получить ключ](https://openrouteservice.org/dev/#/signup) · [Документация](https://openrouteservice.org/dev/#/api-docs/v2/isochrones) |
| `DEEPSEEK_API_KEY` | planner-service | нет | API-ключ DeepSeek для AI-фильтрации. [Получить ключ](https://platform.deepseek.com/api_keys) · [Документация](https://platform.deepseek.com/docs) |

---

## 4. API документация

Swagger UI доступен при запущенном сервисе:

- `location-context-service`: http://localhost:8081/swagger-ui/index.html
- `places-service`: http://localhost:8082/swagger-ui/index.html
- `planner-service`: http://localhost:8083/swagger-ui/index.html

---

### `POST /api/v1/context/analyze` — location-context-service

Основной эндпоинт. Принимает адрес и параметры прогулки, возвращает полный маршрут.

**Запрос:**
```json
{
  "location": "Арбат, Москва",
  "startTime": "10:00",
  "endTime": "16:00",
  "pace": "MEDIUM",
  "categories": ["museum", "park", "cafe"]
}
```

| Поле | Тип | Описание |
|------|-----|----------|
| `location` | string | Адрес или название места |
| `startTime` | string | Время начала прогулки (HH:mm) |
| `endTime` | string | Время конца прогулки (HH:mm) |
| `pace` | string | Темп: `SLOW`, `MEDIUM`, `FAST` — влияет на радиус поиска |
| `categories` | array | Категории мест: `museum`, `park`, `cafe`, `gallery`, `restaurant` |

**Ответ `200 OK`:**
```json
{
  "data": {
    "resolvedLocation": "Арбат, Москва",
    "latitude": 55.752,
    "longitude": 37.592,
    "radiusMeters": 3000,
    "availableHours": 6.0,
    "startTime": "10:00",
    "endTime": "16:00",
    "pace": "MEDIUM",
    "places": [
      {
        "placeId": "otm_museum_abc123",
        "name": "Третьяковская галерея",
        "category": "museum",
        "latitude": 55.7415,
        "longitude": 37.6208,
        "estimatedHours": 2.5,
        "rating": 4.8,
        "address": "Лаврушинский переулок, 10",
        "openingHoursText": "Mo-Su 10:00-18:00",
        "scheduleUnknown": false
      }
    ],
    "plan": {
      "items": [
        {
          "placeId": "otm_museum_abc123",
          "placeName": "Третьяковская галерея",
          "arrivalTime": "10:18",
          "departureTime": "12:48",
          "category": "museum",
          "travelTimeMinutes": 18
        }
      ],
      "totalPlaces": 1,
      "totalHours": 2.5,
      "warnings": []
    }
  }
}
```

---

### `POST /api/v1/places/search` — places-service

Поиск мест рядом с координатами.

**Запрос:**
```json
{
  "latitude": 55.7520,
  "longitude": 37.5921,
  "radiusMeters": 3000,
  "availableHours": 6.0,
  "categories": ["museum", "park"]
}
```

| Поле | Тип | Описание |
|------|-----|----------|
| `latitude` | number | Широта центра поиска |
| `longitude` | number | Долгота центра поиска |
| `radiusMeters` | integer | Радиус поиска в метрах (больше 0) |
| `availableHours` | number | Доступное время в часах — влияет на оценку времени посещения |
| `categories` | array | Категории мест: `museum`, `park`, `cafe`, `gallery`, `restaurant`. Если не указаны — используются все |

**Ответ `200 OK`:**
```json
{
  "data": {
    "places": [
      {
        "placeId": "otm_museum_abc123",
        "name": "Третьяковская галерея",
        "category": "museum",
        "latitude": 55.7415,
        "longitude": 37.6208,
        "estimatedHours": 2.5,
        "rating": 4.8,
        "address": "Лаврушинский переулок, 10",
        "description": "Знаменитый музей русского искусства",
        "openingHoursText": "Mo-Su 10:00-18:00",
        "scheduleUnknown": false
      }
    ],
    "totalFound": 1
  }
}
```

---

### `POST /api/v1/plan/build` — planner-service

Строит расписание из списка мест.

**Запрос:**
```json
{
  "startTime": "10:00",
  "endTime": "18:00",
  "startLatitude": 55.7558,
  "startLongitude": 37.6173,
  "places": [
    {
      "placeId": "otm_museum_001",
      "name": "Третьяковская галерея",
      "category": "museum",
      "latitude": 55.7415,
      "longitude": 37.6208,
      "estimatedHours": 2.5,
      "rating": 4.8,
      "openingHoursText": "Mo-Su 10:00-18:00",
      "scheduleUnknown": false
    }
  ]
}
```

| Поле | Тип | Описание |
|------|-----|----------|
| `startTime` | string | Время начала прогулки (HH:mm) |
| `endTime` | string | Время конца прогулки (HH:mm) |
| `places` | array | Список мест для включения в маршрут |
| `startLatitude` | number | Опционально. Широта стартовой точки — если указана, учитывается время пешего пути (5 км/ч) |
| `startLongitude` | number | Опционально. Долгота стартовой точки |

**Ответ `200 OK`:**
```json
{
  "data": {
    "items": [
      {
        "placeId": "otm_museum_001",
        "placeName": "Третьяковская галерея",
        "arrivalTime": "10:18",
        "departureTime": "12:48",
        "category": "museum",
        "travelTimeMinutes": 18
      }
    ],
    "totalPlaces": 1,
    "totalHours": 2.5,
    "warnings": [],
    "evaluatedOrderings": 1
  }
}
```

### `GET /api/v1/plan/health` — planner-service

Проверка работоспособности сервиса.

**Ответ `200 OK`:**
```json
{ "status": "ok" }
```

---

### Формат ошибок (все сервисы)

```json
{
  "errors": [
    {
      "code": "VALIDATION_ERROR",
      "message": "radiusMeters: must be positive"
    }
  ]
}
```

| Код | HTTP | Описание |
|-----|------|----------|
| `VALIDATION_ERROR` | 400 | Некорректные параметры запроса |
| `ADDRESS_NOT_FOUND` | 404 | Адрес не удалось геокодировать |
| `INTERNAL_ERROR` | 500 | Внутренняя ошибка сервера |

---

## 5. Как тестировать

### Запуск тестов

```bash
# places-service
cd places-service && ./gradlew test

# planner-service
cd planner-service && ./gradlew test

# location-context-service
cd location-context-service && ./gradlew test
```

### Проверка линтера

```bash
cd places-service && ./gradlew checkstyleMain
cd planner-service && ./gradlew checkstyleMain
cd location-context-service && ./gradlew checkstyleMain
```

### Автоформатирование кода

```bash
cd places-service && ./gradlew spotlessApply
cd planner-service && ./gradlew spotlessApply
cd location-context-service && ./gradlew spotlessApply
```

### Git pre-commit hook

Перед каждым коммитом автоматически запускаются тесты всех трёх сервисов. Если хотя бы один тест не проходит — коммит блокируется.

Установка (один раз после клонирования репозитория):

```bash
cp scripts/pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

### Проверка через Swagger

После запуска сервисов откройте:
- http://localhost:8081/swagger-ui/index.html — основной маршрут
- http://localhost:8082/swagger-ui/index.html — поиск мест
- http://localhost:8083/swagger-ui/index.html — планировщик

---

## 6. Контакты и поддержка

**Команда ПИН-33**

| Участник | Сервис | GitHub |
|---------|--------|--------|
| Шабунова Софья | places-service | [@shabunovasofia](https://github.com/shabunovasofia) |
| Макшанцева Софья | planner-service | [@goiddochka1408](https://github.com/goiddochka1408) |
| Холодов Степан | location-context-service | [@StepanKholodov](https://github.com/StepanKholodov) |

По вопросам и багам: [GitHub Issues](https://github.com/shabunovasofia/travel-day-planner/issues)
