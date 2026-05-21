# Location Context Service

Микросервис для определения географического контекста прогулки: геокодирует адрес, вычисляет
доступное время и возвращает радиус пешеходной доступности через изохрону.

## Стек

| Слой           | Технология                                         |
|----------------|----------------------------------------------------|
| Runtime        | Java 21, Spring Boot 3.5                           |
| HTTP-клиент    | `RestClient` (Spring 6.2)                          |
| Кэш            | Caffeine (Spring Cache, TTL 1 ч, max 1000 записей) |
| Документация   | SpringDoc OpenAPI (Swagger UI)                     |
| Геокодирование | [LocationIQ](https://locationiq.com/)              |
| Изохроны       | [OpenRouteService](https://openrouteservice.org/)  |
| Сборка         | Gradle 9                                           |

## Архитектура

```
POST /api/v1/context/analyze
        │
        ▼
LocationContextController
        │
        ▼
LocationContextService
        ├──▶ GeocodingService  ──▶ LocationIQ API
        │         └── Caffeine Cache
        └──▶ IsochroneService  ──▶ OpenRouteService API
                  └── fallback: pace.speedKmh / 2 × availableHours
```

### Ключевые решения

- **RestClient инициализируется один раз** в конструкторе сервиса (baseUrl, API-ключ, заголовки по
  умолчанию). Сервис передаёт только изменяемые параметры запроса.
- **Caffeine Cache** с TTL 1 час и лимитом 1000 записей заменяет неограниченный `ConcurrentHashMap`.
- **Pace-aware fallback**: если изохрона недоступна, радиус рассчитывается через скорость текущего
  темпа (`Pace.speedKmh`), а не фиксированные 5 км/ч.
- **Таймауты**: 5 сек на подключение, 10 сек на чтение (настраиваются через properties).
- **Маскировка ключей** в логах (`key=***`).

## Быстрый старт

### Требования

- Java 21+
- API-ключ LocationIQ (`GEOCODING_API_KEY`)
- API-ключ OpenRouteService (`ORS_API_KEY`)

### Запуск

```bash
export GEOCODING_API_KEY=your_locationiq_key
export ISOCHRONE_API_KEY=your_ors_key
./gradlew bootRun
```

Сервис запускается на `http://localhost:8081`.

### Swagger UI

После запуска документация доступна по адресу:

```
http://localhost:8081/swagger-ui.html
```

## API

### `POST /api/v1/context/analyze`

Анализирует контекст прогулки.

**Запрос:**

```json
{
  "location": "Арбат, Москва",
  "startTime": "10:00",
  "endTime": "16:00",
  "pace": "MEDIUM"
}
```

| Поле        | Тип                    | Описание                                      |
|-------------|------------------------|-----------------------------------------------|
| `location`  | `string`               | Адрес в свободной форме                       |
| `startTime` | `HH:mm`                | Время начала прогулки                         |
| `endTime`   | `HH:mm`                | Время окончания (должно быть позже startTime) |
| `pace`      | `SLOW / MEDIUM / FAST` | Темп: 3.5 / 5.0 / 6.5 км/ч                    |

**Ответ `200 OK`:**

```json
{
  "resolvedLocation": "Арбат, Москва",
  "latitude": 55.752,
  "longitude": 37.592,
  "radiusMeters": 8500,
  "availableHours": 6.0,
  "startTime": "10:00",
  "endTime": "16:00",
  "pace": "MEDIUM"
}
```

**Коды ошибок:**

| Код   | Причина                                                                  |
|-------|--------------------------------------------------------------------------|
| `400` | Ошибка валидации (пустой адрес, endTime ≤ startTime, отсутствующий темп) |
| `404` | Адрес не найден в LocationIQ                                             |
| `500` | Внутренняя ошибка / недоступность внешних API                            |

## Конфигурация

Параметры задаются в `application.properties` или через переменные окружения:

```properties
geocoding.url=https://us1.locationiq.com/v1/search
geocoding.api-key=${GEOCODING_API_KEY:}

isochrone.url=https://api.openrouteservice.org/v2/isochrones/foot-walking
isochrone.api-key=${ISOCHRONE_API_KEY:}

http.connect-timeout-ms=5000
http.read-timeout-ms=10000

spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=1h
```
