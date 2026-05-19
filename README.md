# Travel Day Planner

REST API для планирования дня в городе. Пользователь вводит адрес, временной интервал и темп прогулки — система автоматически подбирает подходящие места и строит готовое расписание на день.

---

## 1. Название и назначение

Проект реализован как три независимых микросервиса:

| Сервис                     | Порт | Роль |
|----------------------------|------|------|
| `location-context-service` | 8081 | Точка входа. Геокодирует адрес, рассчитывает радиус пешеходной доступности, оркестрирует вызовы к двум другим сервисам, возвращает клиенту готовый маршрут |
| `places-service`           | 8082 | Ищет достопримечательности вблизи заданных координат через OpenTripMap API, фильтрует по категориям, сортирует по рейтингу |
| `planner-service`          | 8083 | Строит оптимальное расписание из списка мест с учётом времени в пути между точками и графика работы |

Клиент отправляет **один запрос** в `location-context-service` и получает полный маршрут на день.

---

## 2. Архитектура и зависимости

### Технологии

- **Java 21**
- **Spring Boot 3.x**
- **Gradle** (Groovy DSL)
- **JUnit 5** + **Mockito**
- **Docker**

### Взаимодействие между сервисами

```
Клиент
  └─► location-context-service :8081
            ├─► places-service :8082
            └─► planner-service :8083
```

`location-context-service` вызывает `places-service` и `planner-service` по HTTP.
Остальные сервисы не обращаются друг к другу.

### Внешние сервисы

| Сервис                                             | Используется в | Назначение |
|----------------------------------------------------|---------------|------------|
| [OpenTripMap API](https://opentripmap.io)          | places-service | Поиск мест по координатам и категориям |
| [OpenStreetMap API](https://openstreetmap.org)     | places-service | Загрузка графика работы мест |
| [LocationIQ API](https://locationiq.com)           | location-context-service | Геокодинг адреса в координаты |
| [ORS Isochrones API](https://openrouteservice.org) | location-context-service | Расчёт радиуса пешеходной доступности |

---

## 3. Способы запуска

Сервисы запускаются независимо. Каждый требует своих переменных окружения.

### Локальный запуск

**places-service:**
```bash
cd places-service
OPENTRIPMAP_API_KEY=ваш_ключ ./gradlew bootRun
```

**planner-service:**
```bash
cd planner-service
./gradlew bootRun
```

**location-context-service:**
```bash
cd location-context-service
GEOCODING_API_KEY=ваш_ключ \
ORS_API_KEY=ваш_ключ \
./gradlew bootRun
```

### Запуск через Docker

**places-service:**
```bash
cd places-service
./gradlew bootJar
docker build -t places-service .
docker run -p 8082:8082 -e OPENTRIPMAP_API_KEY=ваш_ключ places-service
```

**planner-service:**
```bash
cd planner-service
./gradlew bootJar
docker build -t planner-service .
docker run -p 8083:8083 planner-service
```

### Конфигурация

Настройки каждого сервиса задаются в `src/main/resources/application.properties`.
API-ключи вынесены в переменные окружения и подставляются через `${VAR_NAME}`.
URL соседних сервисов прописаны в properties напрямую.

**places-service** (`application.properties`):
```properties
server.port=8082
opentripmap.api-key=${OPENTRIPMAP_API_KEY}
```

**location-context-service** (`application.properties`):
```properties
server.port=8081
geocoding.url=https://us1.locationiq.com/v1/search
geocoding.api-key=${GEOCODING_API_KEY}
ors.url=https://api.openrouteservice.org/v2/isochrones/foot-walking
ors.api-key=${ORS_API_KEY}
```

**planner-service** (`application.properties`):
```properties
server.port=8083
planner.integration.location-context-base-url=http://localhost:8081
planner.integration.places-base-url=http://localhost:8082
```

**Переменные окружения (API-ключи):**

| Переменная | Сервис | Описание |
|-----------|--------|----------|
| `OPENTRIPMAP_API_KEY` | places-service | API-ключ OpenTripMap. Получить: https://opentripmap.io |
| `GEOCODING_API_KEY` | location-context-service | API-ключ LocationIQ. Получить: https://locationiq.com |
| `ORS_API_KEY` | location-context-service | API-ключ OpenRouteService. Получить: https://openrouteservice.org |

---

## 4. API документация

Swagger UI доступен при запущенном сервисе:
- location-context-service: http://localhost:8081/swagger-ui/index.html
- places-service: http://localhost:8082/swagger-ui/index.html
- planner-service: http://localhost:8083/swagger-ui/index.html

### location-context-service — `POST /api/v1/context/analyze`

Основной эндпоинт. Принимает адрес и параметры прогулки, возвращает полный маршрут.

**Запрос:**
```json
{
  "location": "Арбат, Москва",
  "startTime": "10:00",
  "endTime": "16:00",
  "pace": "MEDIUM",
  "categories": ["museum", "park"]
}
```

`pace`: `SLOW` / `MEDIUM` / `FAST` — влияет на радиус поиска мест.

**Ответ:**
```json
{
  "data": {
    "resolvedLocation": "Арбат, Москва",
    "latitude": 55.752,
    "longitude": 37.592,
    "radiusMeters": 3000,
    "availableHours": 6.0,
    "places": [...],
    "plan": {
      "items": [...],
      "totalPlaces": 2,
      "totalHours": 4.0,
      "warnings": []
    }
  }
}
```

---

### places-service — `POST /api/v1/places/search`

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

**Ответ:**
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
        "openingHoursText": "Mo-Su 10:00-18:00; PH off",
        "scheduleUnknown": false
      }
    ],
    "totalFound": 1
  }
}
```

Поддерживаемые категории: `museum`, `park`, `cafe`, `gallery`, `restaurant`

---

### planner-service — `POST /api/v1/plan/build`

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
      "openingHoursText": "Mo-Su 10:00-18:00",
      "scheduleUnknown": false
    }
  ]
}
```

`startLatitude` / `startLongitude` — опциональные. Если переданы, учитывается время пешего пути от стартовой точки (5 км/ч).

**Ответ:**
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
    "warnings": []
  }
}
```

### planner-service — `GET /api/v1/plan/health`

Проверка работоспособности.

**Ответ:**
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

---

## 5. Как тестировать

### Запуск тестов

```bash
cd places-service && ./gradlew test
cd location-context-service && ./gradlew test
cd planner-service && ./gradlew test
```

### Git pre-commit hook

Перед каждым коммитом автоматически запускаются тесты всех трёх сервисов. Если хотя бы один тест не проходит — коммит не создаётся.

Установка (один раз после клонирования репозитория):

```bash
cp scripts/pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

---

## 6. Контакты и поддержка

**Команда ПИН-33**

| Участник | Сервис | GitHub |
|---------|--------|--------|
| Шабунова Софья | places-service | [@shabunovasofia](https://github.com/shabunovasofia) |
| Макшанцева Софья | planner-service | [@goiddochka1408](https://github.com/goiddochka1408) |
| Холодов Степан | location-context-service | [@StepanKholodov](https://github.com/StepanKholodov) |

По вопросам: GitHub Issues в этом репозитории.
