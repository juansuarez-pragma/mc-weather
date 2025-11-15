# Architecture Documentation

## Overview

This document describes the architectural decisions, patterns, and design principles used in the Weather API Service.

## Table of Contents

1. [Architectural Style](#architectural-style)
2. [Layer Description](#layer-description)
3. [Design Patterns](#design-patterns)
4. [Data Flow](#data-flow)
5. [Resilience Patterns](#resilience-patterns)
6. [Caching Strategy](#caching-strategy)
7. [Security Considerations](#security-considerations)
8. [Performance Optimizations](#performance-optimizations)
9. [ADRs (Architecture Decision Records)](#architecture-decision-records)

---

## Architectural Style

### Hexagonal Architecture (Ports & Adapters)

The system follows **Hexagonal Architecture** principles to achieve:

- **Separation of Concerns**: Business logic isolated from external dependencies
- **Testability**: Easy to test with mocked dependencies
- **Flexibility**: Easy to swap implementations (e.g., different API clients)
- **Maintainability**: Clear boundaries between layers

```
┌──────────────────────────────────────────────┐
│         Infrastructure Layer                 │
│  (Frameworks, Databases, External APIs)      │
│                                              │
│  ┌────────────┐         ┌────────────┐      │
│  │ REST       │         │ Feign      │      │
│  │ Controller │         │ Client     │      │
│  └─────┬──────┘         └──────┬─────┘      │
└────────┼───────────────────────┼────────────┘
         │                       │
         │    ┌─────────────┐    │
         ├────│   Ports     │────┤
         │    │ (Interfaces)│    │
         │    └─────────────┘    │
         │                       │
┌────────┼───────────────────────┼────────────┐
│        ▼                       ▼            │
│         Application Layer                   │
│  (Use Cases, Business Rules)                │
│                                              │
│  ┌────────────────────────────────┐         │
│  │   WeatherService               │         │
│  │   (Business Logic)             │         │
│  └────────────────────────────────┘         │
└──────────────────────────────────────────────┘
                    │
┌───────────────────┼──────────────────────────┐
│                   ▼                          │
│         Domain Layer                         │
│  (Core Business Models)                      │
│                                              │
│  ┌────────────┐  ┌────────────┐             │
│  │  Weather   │  │  Location  │             │
│  │  (Entity)  │  │  (Value    │             │
│  │            │  │   Object)  │             │
│  └────────────┘  └────────────┘             │
└──────────────────────────────────────────────┘
```

---

## Layer Description

### 1. Domain Layer (`com.weather.api.domain`)

**Purpose**: Core business logic and rules

**Components**:
- **Models**: `Weather`, `Location`, `GeocodingResult`
- **Exceptions**: `InvalidCoordinatesException`, `CityNotFoundException`, `ExternalApiException`
- **Ports**: `WeatherUseCase` (input), `WeatherRepositoryPort` (output)

**Principles**:
- No framework dependencies
- Pure Java objects
- Immutable where possible (using Lombok `@Value`)
- Self-validating models

**Example**:
```java
@Value
@Builder
public class Location {
    Double latitude;
    Double longitude;

    public boolean isValid() {
        return latitude >= -90.0 && latitude <= 90.0
            && longitude >= -180.0 && longitude <= 180.0;
    }
}
```

### 2. Application Layer (`com.weather.api.application`)

**Purpose**: Orchestrate business logic and coordinate between layers

**Components**:
- **Services**: `WeatherService` implements `WeatherUseCase`
- **DTOs**: Request/Response objects for API
- **Mappers**: Transform between domain models and DTOs

**Responsibilities**:
- Input validation
- Caching annotations
- MDC logging context
- Error handling delegation

**Example**:
```java
@Service
@RequiredArgsConstructor
public class WeatherService implements WeatherUseCase {

    @Cacheable(value = "weatherForecast", key = "#latitude + '_' + #longitude")
    public Weather getWeatherForecast(Double latitude, Double longitude, String timezone) {
        validateCoordinates(latitude, longitude);
        return weatherRepositoryPort.fetchWeatherForecast(latitude, longitude, timezone);
    }
}
```

### 3. Infrastructure Layer (`com.weather.api.infrastructure`)

**Purpose**: Technical implementation details

**Components**:
- **Adapters**: REST controllers, Feign clients
- **Configuration**: Cache, CORS, Swagger, Resilience
- **Monitoring**: Health indicators, metrics

**Characteristics**:
- Framework-dependent
- External integrations
- Technical cross-cutting concerns

---

## Design Patterns

### 1. **Repository Pattern**

Even though we're calling an HTTP API, we treat it as a repository:

```java
// Port (interface in domain)
public interface WeatherRepositoryPort {
    Weather fetchWeatherForecast(Double latitude, Double longitude, String timezone);
}

// Adapter (implementation in infrastructure)
@Component
public class OpenMeteoClientImpl implements WeatherRepositoryPort {
    // Implementation using Feign
}
```

**Benefits**:
- Easy to test with mocks
- Can swap implementations (e.g., different weather APIs)
- Domain layer doesn't know about HTTP

### 2. **Mapper Pattern**

Separate DTOs from domain models:

```java
@Component
public class WeatherMapper {
    public WeatherForecastResponse toResponse(Weather weather) {
        // Mapping logic
    }
}
```

**Benefits**:
- Domain models stay clean
- API contract separate from business logic
- Versioning flexibility

### 3. **Circuit Breaker Pattern**

Using Resilience4j:

```java
@CircuitBreaker(name = "openMeteoService", fallbackMethod = "fetchWeatherForecastFallback")
@Retry(name = "openMeteoService")
public Weather fetchWeatherForecast(...) {
    // Call external API
}
```

**States**:
- **CLOSED**: Normal operation
- **OPEN**: Too many failures, reject requests immediately
- **HALF_OPEN**: Test if service recovered

### 4. **Retry Pattern**

Exponential backoff for transient failures:

```yaml
resilience4j:
  retry:
    instances:
      openMeteoService:
        maxAttempts: 3
        waitDuration: 1s
        exponentialBackoffMultiplier: 2
```

**Retry Sequence**:
1. Initial call fails
2. Wait 1s, retry
3. Wait 2s, retry
4. Wait 4s, retry
5. Give up, throw exception

### 5. **Builder Pattern**

For constructing complex objects:

```java
Weather weather = Weather.builder()
    .temperature(15.5)
    .weatherCode(2)
    .latitude(40.7128)
    .longitude(-74.0060)
    .build();
```

---

## Data Flow

### Request Flow (Weather Forecast)

```
iOS App
  │
  │ HTTP GET /api/v1/weather/forecast?latitude=40.7128&longitude=-74.0060
  │
  ▼
┌─────────────────────────────────────────┐
│ 1. WeatherController                    │
│    - Validate request params            │
│    - Rate limiting check                │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│ 2. WeatherService                       │
│    - Check cache (Caffeine)             │
│    - If cached: return immediately      │
│    - If not: continue to repository     │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│ 3. OpenMeteoClientImpl                  │
│    - Circuit breaker check              │
│    - Call Open-Meteo API via Feign      │
│    - Retry if transient failure         │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│ 4. Open-Meteo API                       │
│    - External weather service           │
└─────────────┬───────────────────────────┘
              │
              ▼ Response
┌─────────────────────────────────────────┐
│ 5. Map to Domain Model                  │
│    - OpenMeteoWeatherResponse → Weather │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│ 6. Store in Cache                       │
│    - TTL: 5 minutes                     │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│ 7. Map to DTO                           │
│    - Weather → WeatherForecastResponse  │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│ 8. Return JSON Response                 │
└─────────────────────────────────────────┘
```

---

## Resilience Patterns

### Circuit Breaker Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      openMeteoService:
        slidingWindowSize: 10           # Track last 10 calls
        minimumNumberOfCalls: 5         # Need 5 calls to calculate failure rate
        failureRateThreshold: 50        # Open if 50% fail
        waitDurationInOpenState: 10s    # Stay open for 10s
        permittedNumberOfCallsInHalfOpenState: 3  # Allow 3 test calls
```

**Decision Matrix**:

| Scenario | Action | Reason |
|----------|--------|--------|
| 5/10 calls fail | Open circuit | Failure rate = 50% |
| Circuit open | Reject immediately | Prevent cascading failures |
| After 10s | Enter half-open | Test if service recovered |
| 3/3 test calls succeed | Close circuit | Service recovered |
| Any test call fails | Re-open circuit | Not recovered yet |

### Fallback Strategy

When circuit is open or all retries exhausted:

```java
private Weather fetchWeatherForecastFallback(Exception e) {
    throw new ExternalApiException(
        "Weather service is currently unavailable. Please try again later.",
        e
    );
}
```

**Alternative Strategies** (not implemented, for future):
- Return cached data even if expired
- Return default weather data
- Call backup weather API

---

## Caching Strategy

### Configuration

```java
Caffeine.newBuilder()
    .maximumSize(1000)              // Max 1000 entries
    .expireAfterWrite(5, MINUTES)   // Expire after 5 minutes
    .recordStats()                   // Enable metrics
    .build();
```

### Cache Keys

- **Weather Forecast**: `{latitude}_{longitude}`
- **City Search**: `{cityName}_{count}_{language}`

### Why 5 Minutes?

| Factor | Consideration |
|--------|---------------|
| **Data Freshness** | Weather doesn't change every second |
| **API Cost** | Reduce calls to external API |
| **User Experience** | Fast response for repeated requests |
| **Memory** | 1000 entries ≈ 10MB RAM |

### Cache Invalidation

- **Time-based**: Automatic after 5 minutes (expireAfterWrite)
- **Size-based**: LRU eviction when max size reached
- **Manual**: Not implemented (could add endpoint to clear cache)

---

## Security Considerations

### 1. Input Validation

**Multiple layers**:

```java
// 1. Bean Validation (Controller)
@NotNull
@DecimalMin("-90.0")
@DecimalMax("90.0")
Double latitude;

// 2. Business Validation (Service)
private void validateCoordinates(Double latitude, Double longitude) {
    Location location = Location.builder()
        .latitude(latitude)
        .longitude(longitude)
        .build();

    if (!location.isValid()) {
        throw new InvalidCoordinatesException(latitude, longitude);
    }
}
```

### 2. Rate Limiting

Prevent abuse:

```yaml
resilience4j:
  ratelimiter:
    instances:
      openMeteoService:
        limitForPeriod: 60      # 60 requests
        limitRefreshPeriod: 60s  # per minute
```

Returns `429 Too Many Requests` when exceeded.

### 3. CORS Configuration

```yaml
cors:
  allowed-origins: "*"           # Change to specific domain in production
  allowed-methods: GET,OPTIONS
  allowed-headers: "*"
```

**Production**: Should restrict `allowed-origins` to iOS app domain.

### 4. Error Messages

Never expose:
- Stack traces (in production)
- Internal implementation details
- Database/API credentials

**Safe error response**:
```json
{
  "message": "Weather service is temporarily unavailable"
}
```

Instead of:
```json
{
  "message": "Connection timeout to api.open-meteo.com:443"
}
```

---

## Performance Optimizations

### 1. Caching

**Impact**:
- Cache hit: ~1ms response time
- Cache miss: ~200ms response time
- Hit ratio target: >70%

### 2. Connection Pooling

Feign uses built-in connection pooling:

```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 3000
        readTimeout: 5000
```

### 3. Multi-stage Docker Build

Reduces image size:
- Builder stage: ~600MB
- Runtime stage: ~200MB

### 4. JVM Tuning

```dockerfile
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport"
```

---

## Architecture Decision Records

### ADR-001: Why Hexagonal Architecture?

**Context**: Need to build maintainable, testable backend service.

**Decision**: Use Hexagonal Architecture (Ports & Adapters).

**Consequences**:
- ✅ Easy to test with mocked dependencies
- ✅ Can swap weather API providers
- ✅ Clear separation of business logic
- ❌ More boilerplate code (interfaces, adapters)
- ❌ Steeper learning curve for new developers

**Status**: Accepted

---

### ADR-002: Why Caffeine over Redis?

**Context**: Need caching for weather data.

**Options**:
1. Caffeine (in-memory)
2. Redis (distributed)

**Decision**: Use Caffeine for initial version.

**Rationale**:
- Weather data is not critical (can refetch)
- Single instance deployment (no need for distributed cache)
- Simpler setup (no external dependencies)
- Lower latency (~1ms vs ~5ms)

**Future**: If we scale horizontally, migrate to Redis.

**Status**: Accepted

---

### ADR-003: Why OpenFeign over RestTemplate?

**Context**: Need HTTP client for external API calls.

**Decision**: Use Spring Cloud OpenFeign.

**Rationale**:
- Declarative syntax (cleaner code)
- Better integration with Resilience4j
- Automatic serialization/deserialization
- Industry standard for microservices

**Status**: Accepted

---

### ADR-004: Why Not Store User Data?

**Context**: Could cache user search history in database.

**Decision**: Do NOT store user data backend-side.

**Rationale**:
- iOS app already handles local history (UserDefaults)
- Avoid GDPR/privacy concerns
- Simpler backend (stateless)
- Reduce infrastructure cost (no database needed)

**Status**: Accepted

---

## Future Enhancements

### 1. Database Integration

If we decide to store user preferences:
- PostgreSQL for relational data
- Redis for distributed caching
- Flyway for migrations

### 2. Authentication

Add JWT-based authentication:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // JWT filter configuration
}
```

### 3. Advanced Monitoring

- Distributed tracing (Zipkin/Jaeger)
- Log aggregation (ELK stack)
- Custom Grafana dashboards

### 4. Multi-Region Deployment

- Active-active architecture
- Geographic load balancing
- Redis for shared cache

---

## Conclusion

This architecture provides:
- ✅ **Maintainability**: Clear separation of concerns
- ✅ **Testability**: High test coverage possible
- ✅ **Resilience**: Circuit breaker + retry + caching
- ✅ **Performance**: Fast responses with caching
- ✅ **Scalability**: Stateless, can scale horizontally
- ✅ **Observability**: Comprehensive monitoring

The design follows industry best practices for a production-ready backend service.

---

**Last Updated**: 2025-11-15
**Author**: Weather API Team
