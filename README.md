# Weather API Service

Backend service for iOS Weather App - A robust BFF (Backend For Frontend) proxying requests to Open-Meteo API with enhanced features like caching, circuit breaking, and comprehensive monitoring.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Configuration](#-configuration)
- [Testing](#-testing)
- [Docker](#-docker)
- [Monitoring](#-monitoring)
- [Project Structure](#-project-structure)

## ✨ Features

- **🔄 Intelligent Caching**: Caffeine-based caching with 5-minute TTL to reduce external API calls
- **🛡️ Circuit Breaker**: Resilience4j circuit breaker pattern for fault tolerance
- **🔁 Retry Logic**: Automatic retry with exponential backoff for transient failures
- **⏱️ Rate Limiting**: Protection against API abuse (60 requests/minute)
- **📊 Monitoring**: Prometheus metrics and health checks via Spring Actuator
- **📝 API Documentation**: Interactive Swagger UI (OpenAPI 3.0)
- **🏗️ Clean Architecture**: Hexagonal architecture with clear separation of concerns
- **✅ High Test Coverage**: >80% code coverage with unit and integration tests
- **🐳 Containerized**: Docker and Docker Compose ready

## 🏗️ Architecture

This project follows **Hexagonal Architecture** (Ports & Adapters) principles:

```
┌─────────────────────────────────────────────────────┐
│              Infrastructure Layer                    │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐ │
│  │   REST API   │  │ Feign Client │  │   Config  │ │
│  └──────┬───────┘  └──────┬───────┘  └───────────┘ │
└─────────┼──────────────────┼──────────────────────────┘
          │                  │
┌─────────┼──────────────────┼──────────────────────────┐
│         ▼                  ▼                           │
│              Application Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐  │
│  │   Services   │  │    Mappers   │  │    DTOs   │  │
│  └──────┬───────┘  └──────────────┘  └───────────┘  │
└─────────┼──────────────────────────────────────────────┘
          │
┌─────────┼──────────────────────────────────────────────┐
│         ▼                                               │
│              Domain Layer (Business Logic)             │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐   │
│  │    Models    │  │  Exceptions  │  │   Ports   │   │
│  └──────────────┘  └──────────────┘  └───────────┘   │
└─────────────────────────────────────────────────────────┘
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed design decisions.

## 🛠️ Tech Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Language** | Java 17 | Modern Java with records, pattern matching |
| **Framework** | Spring Boot 3.2.0 | Enterprise-grade application framework |
| **HTTP Client** | OpenFeign | Declarative REST client |
| **Resilience** | Resilience4j | Circuit breaker, retry, rate limiting |
| **Caching** | Caffeine | High-performance in-memory cache |
| **Validation** | Jakarta Validation | Bean validation |
| **Documentation** | SpringDoc OpenAPI | Interactive API docs |
| **Monitoring** | Micrometer + Actuator | Metrics and health checks |
| **Testing** | JUnit 5 + Mockito + WireMock | Comprehensive testing |
| **Build** | Maven 3.9+ | Dependency management |
| **Containerization** | Docker + Docker Compose | Deployment |

## 📦 Prerequisites

- **Java 17** or higher
- **Maven 3.9** or higher
- **Docker** (optional, for containerized deployment)

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/weather-api-service.git
cd weather-api-service
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### 4. Access Swagger UI

Navigate to: `http://localhost:8080/swagger-ui.html`

## 📚 API Documentation

### Endpoints

#### Get Weather Forecast

```http
GET /api/v1/weather/forecast?latitude={lat}&longitude={lon}&timezone={tz}
```

**Parameters:**
- `latitude` (required): Latitude coordinate (-90 to 90)
- `longitude` (required): Longitude coordinate (-180 to 180)
- `timezone` (optional): Timezone (default: "auto")

**Example Request:**
```bash
curl "http://localhost:8080/api/v1/weather/forecast?latitude=40.7128&longitude=-74.0060"
```

**Example Response:**
```json
{
  "latitude": 40.7128,
  "longitude": -74.0060,
  "timezone": "America/New_York",
  "current": {
    "time": "2025-11-15T10:30:00",
    "temperature": 15.5,
    "weatherCode": 2,
    "windSpeed": 12.3,
    "humidity": 65
  }
}
```

#### Search Cities

```http
GET /api/v1/weather/search?name={cityName}&count={count}&language={lang}
```

**Parameters:**
- `name` (required): City name (minimum 2 characters)
- `count` (optional): Maximum results (default: 10, max: 20)
- `language` (optional): Language code (default: "en")

**Example Request:**
```bash
curl "http://localhost:8080/api/v1/weather/search?name=New%20York&count=5"
```

**Example Response:**
```json
{
  "results": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "New York",
      "latitude": 40.7128,
      "longitude": -74.0060,
      "country": "United States",
      "admin1": "New York",
      "displayName": "New York, New York, United States"
    }
  ]
}
```

### Error Responses

All error responses follow this structure:

```json
{
  "timestamp": "2025-11-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Latitude must be between -90 and 90"
}
```

**Status Codes:**
- `200` - Success
- `400` - Bad Request (invalid parameters)
- `404` - Not Found (city not found)
- `429` - Too Many Requests (rate limit exceeded)
- `503` - Service Unavailable (external API down)

## ⚙️ Configuration

### Application Properties

Key configuration in `application.yml`:

```yaml
# Cache Configuration
spring:
  cache:
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=5m

# Resilience4j Circuit Breaker
resilience4j:
  circuitbreaker:
    instances:
      openMeteoService:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s

# Rate Limiting
rate-limit:
  requests-per-minute: 60
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active profile (dev/prod) | `dev` |
| `SERVER_PORT` | Server port | `8080` |
| `JAVA_OPTS` | JVM options | `-Xms256m -Xmx512m` |

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Tests with Coverage

```bash
mvn clean test jacoco:report
```

View coverage report: `target/site/jacoco/index.html`

### Test Structure

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test full stack with WireMock
- **Coverage**: >80% line coverage enforced

## 🐳 Docker

### Build Docker Image

```bash
docker build -t weather-api-service:latest .
```

### Run with Docker Compose

```bash
docker-compose up -d
```

This will start:
- Weather API Service on port `8080`

### With Monitoring Stack

```bash
docker-compose --profile monitoring up -d
```

This adds:
- Prometheus on port `9090`
- Grafana on port `3000` (admin/admin)

### Stop Services

```bash
docker-compose down
```

## 📊 Monitoring

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "openMeteo": {
      "status": "UP",
      "details": {
        "latency": "120ms"
      }
    }
  }
}
```

### Metrics

Prometheus metrics available at:
```
http://localhost:8080/actuator/prometheus
```

Key metrics:
- `http_server_requests_seconds` - Request latency
- `resilience4j_circuitbreaker_state` - Circuit breaker state
- `cache_gets_total` - Cache hit/miss ratio

### Logs

Logs are structured with MDC context:

```
2025-11-15 10:30:00 - Getting weather forecast for coordinates: (40.7128, -74.0060)
```

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/weather/api/
│   │   ├── application/           # Application Layer
│   │   │   ├── dto/               # Data Transfer Objects
│   │   │   ├── mapper/            # Object Mappers
│   │   │   └── service/           # Business Logic
│   │   ├── domain/                # Domain Layer
│   │   │   ├── model/             # Domain Models
│   │   │   ├── exception/         # Domain Exceptions
│   │   │   └── port/              # Ports (Interfaces)
│   │   ├── infrastructure/        # Infrastructure Layer
│   │   │   ├── adapter/           # Adapters (REST, Clients)
│   │   │   ├── config/            # Configuration
│   │   │   └── monitoring/        # Health Checks
│   │   └── WeatherApiApplication.java
│   └── resources/
│       ├── application.yml
│       └── application-prod.yml
└── test/
    └── java/com/weather/api/
        ├── application/service/   # Service Tests
        ├── infrastructure/        # Controller Tests
        └── integration/           # Integration Tests
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Weather API Team** - *Initial work*

## 🙏 Acknowledgments

- [Open-Meteo](https://open-meteo.com/) for providing free weather API
- [Spring Boot](https://spring.io/projects/spring-boot) for the amazing framework
- [Resilience4j](https://resilience4j.readme.io/) for resilience patterns

## 📞 Support

For support, email weather-api@example.com or open an issue on GitHub.

---

**Made with ❤️ by Weather API Team**
