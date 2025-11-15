# Servicio API del Clima

Servicio backend para la aplicación iOS del clima - Un robusto BFF (Backend For Frontend) que actúa como proxy para las peticiones a la API de Open-Meteo con características mejoradas como caché, circuit breaking y monitoreo completo.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Arquitectura](#-arquitectura)
- [Stack Tecnológico](#-stack-tecnológico)
- [Requisitos Previos](#-requisitos-previos)
- [Primeros Pasos](#-primeros-pasos)
- [Documentación de la API](#-documentación-de-la-api)
- [Configuración](#-configuración)
- [Testing](#-testing)
- [Docker](#-docker)
- [Monitoreo](#-monitoreo)
- [Estructura del Proyecto](#-estructura-del-proyecto)

## ✨ Características

- **🔄 Caché Inteligente**: Caché basado en Caffeine con TTL de 5 minutos para reducir llamadas a APIs externas
- **🛡️ Circuit Breaker**: Patrón circuit breaker con Resilience4j para tolerancia a fallos
- **🔁 Lógica de Reintentos**: Reintentos automáticos con backoff exponencial para fallos transitorios
- **⏱️ Rate Limiting**: Protección contra abuso de la API (60 peticiones/minuto)
- **📊 Monitoreo**: Métricas de Prometheus y health checks vía Spring Actuator
- **📝 Documentación de API**: Swagger UI interactivo (OpenAPI 3.0)
- **🏗️ Arquitectura Limpia**: Arquitectura hexagonal con clara separación de responsabilidades
- **✅ Alta Cobertura de Tests**: >80% de cobertura de código con tests unitarios e integración
- **🐳 Containerizado**: Listo para Docker y Docker Compose

## 🏗️ Arquitectura

Este proyecto sigue los principios de **Arquitectura Hexagonal** (Puertos y Adaptadores):

```
┌─────────────────────────────────────────────────────┐
│              Capa de Infraestructura                 │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐ │
│  │   REST API   │  │ Feign Client │  │   Config  │ │
│  └──────┬───────┘  └──────┬───────┘  └───────────┘ │
└─────────┼──────────────────┼──────────────────────────┘
          │                  │
┌─────────┼──────────────────┼──────────────────────────┐
│         ▼                  ▼                           │
│              Capa de Aplicación                       │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐  │
│  │   Servicios  │  │    Mappers   │  │    DTOs   │  │
│  └──────┬───────┘  └──────────────┘  └───────────┘  │
└─────────┼──────────────────────────────────────────────┘
          │
┌─────────┼──────────────────────────────────────────────┐
│         ▼                                               │
│              Capa de Dominio (Lógica de Negocio)       │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐   │
│  │    Models    │  │  Exceptions  │  │   Ports   │   │
│  └──────────────┘  └──────────────┘  └───────────┘   │
└─────────────────────────────────────────────────────────┘
```

Ver [ARCHITECTURE.md](ARCHITECTURE.md) para decisiones de diseño detalladas.

## 🛠️ Stack Tecnológico

| Componente | Tecnología | Propósito |
|-----------|-----------|---------|
| **Lenguaje** | Java 17 | Java moderno con records, pattern matching |
| **Framework** | Spring Boot 3.2.0 | Framework de aplicaciones de nivel empresarial |
| **Cliente HTTP** | OpenFeign | Cliente REST declarativo |
| **Resiliencia** | Resilience4j | Circuit breaker, retry, rate limiting |
| **Caché** | Caffeine | Caché en memoria de alto rendimiento |
| **Validación** | Jakarta Validation | Validación de beans |
| **Documentación** | SpringDoc OpenAPI | Documentación interactiva de API |
| **Monitoreo** | Micrometer + Actuator | Métricas y health checks |
| **Testing** | JUnit 5 + Mockito + WireMock | Testing completo |
| **Build** | Maven 3.9+ | Gestión de dependencias |
| **Containerización** | Docker + Docker Compose | Despliegue |

## 📦 Requisitos Previos

- **Java 17** o superior
- **Maven 3.9** o superior
- **Docker** (opcional, para despliegue containerizado)

## 🚀 Primeros Pasos

### 1. Clonar el Repositorio

```bash
git clone git@github.com:juansuarez-pragma/mc-weather.git
cd mc-weather
```

### 2. Compilar el Proyecto

```bash
mvn clean install
```

### 3. Ejecutar la Aplicación

```bash
mvn spring-boot:run
```

La API estará disponible en `http://localhost:8080`

### 4. Acceder a Swagger UI

Navegar a: `http://localhost:8080/swagger-ui.html`

## 📚 Documentación de la API

### Endpoints

#### Obtener Pronóstico del Clima

```http
GET /api/v1/weather/forecast?latitude={lat}&longitude={lon}&timezone={tz}
```

**Parámetros:**
- `latitude` (requerido): Coordenada de latitud (-90 a 90)
- `longitude` (requerido): Coordenada de longitud (-180 a 180)
- `timezone` (opcional): Zona horaria (por defecto: "auto")

**Ejemplo de Petición:**
```bash
curl "http://localhost:8080/api/v1/weather/forecast?latitude=40.7128&longitude=-74.0060"
```

**Ejemplo de Respuesta:**
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

#### Buscar Ciudades

```http
GET /api/v1/weather/search?name={cityName}&count={count}&language={lang}
```

**Parámetros:**
- `name` (requerido): Nombre de la ciudad (mínimo 2 caracteres)
- `count` (opcional): Resultados máximos (por defecto: 10, máx: 20)
- `language` (opcional): Código de idioma (por defecto: "en")

**Ejemplo de Petición:**
```bash
curl "http://localhost:8080/api/v1/weather/search?name=New%20York&count=5"
```

**Ejemplo de Respuesta:**
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

### Respuestas de Error

Todas las respuestas de error siguen esta estructura:

```json
{
  "timestamp": "2025-11-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "La latitud debe estar entre -90 y 90"
}
```

**Códigos de Estado:**
- `200` - Éxito
- `400` - Bad Request (parámetros inválidos)
- `404` - Not Found (ciudad no encontrada)
- `429` - Too Many Requests (límite de tasa excedido)
- `503` - Service Unavailable (API externa caída)

## ⚙️ Configuración

### Propiedades de la Aplicación

Configuración clave en `application.yml`:

```yaml
# Configuración de Caché
spring:
  cache:
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=5m

# Circuit Breaker de Resilience4j
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

### Variables de Entorno

| Variable | Descripción | Por Defecto |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Perfil activo (dev/prod) | `dev` |
| `SERVER_PORT` | Puerto del servidor | `8080` |
| `JAVA_OPTS` | Opciones de JVM | `-Xms256m -Xmx512m` |

## 🧪 Testing

### Ejecutar Todos los Tests

```bash
mvn test
```

### Ejecutar Tests con Cobertura

```bash
mvn clean test jacoco:report
```

Ver reporte de cobertura: `target/site/jacoco/index.html`

### Estructura de Tests

- **Tests Unitarios**: Prueban componentes individuales de forma aislada
- **Tests de Integración**: Prueban el stack completo con WireMock
- **Cobertura**: >80% de cobertura de líneas forzada

## 🐳 Docker

### Construir Imagen Docker

```bash
docker build -t weather-api-service:latest .
```

### Ejecutar con Docker Compose

```bash
docker-compose up -d
```

Esto iniciará:
- Servicio API del Clima en el puerto `8080`

### Con Stack de Monitoreo

```bash
docker-compose --profile monitoring up -d
```

Esto añade:
- Prometheus en el puerto `9090`
- Grafana en el puerto `3000` (admin/admin)

### Detener Servicios

```bash
docker-compose down
```

## 📊 Monitoreo

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

Respuesta:
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

### Métricas

Métricas de Prometheus disponibles en:
```
http://localhost:8080/actuator/prometheus
```

Métricas clave:
- `http_server_requests_seconds` - Latencia de peticiones
- `resilience4j_circuitbreaker_state` - Estado del circuit breaker
- `cache_gets_total` - Ratio de aciertos/fallos de caché

### Logs

Los logs están estructurados con contexto MDC:

```
2025-11-15 10:30:00 - Obteniendo pronóstico del clima para coordenadas: (40.7128, -74.0060)
```

## 📁 Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/weather/api/
│   │   ├── application/           # Capa de Aplicación
│   │   │   ├── dto/               # Data Transfer Objects
│   │   │   ├── mapper/            # Mapeadores de Objetos
│   │   │   └── service/           # Lógica de Negocio
│   │   ├── domain/                # Capa de Dominio
│   │   │   ├── model/             # Modelos de Dominio
│   │   │   ├── exception/         # Excepciones de Dominio
│   │   │   └── port/              # Puertos (Interfaces)
│   │   ├── infrastructure/        # Capa de Infraestructura
│   │   │   ├── adapter/           # Adaptadores (REST, Clientes)
│   │   │   ├── config/            # Configuración
│   │   │   └── monitoring/        # Health Checks
│   │   └── WeatherApiApplication.java
│   └── resources/
│       ├── application.yml
│       └── application-prod.yml
└── test/
    └── java/com/weather/api/
        ├── application/service/   # Tests de Servicios
        ├── infrastructure/        # Tests de Controladores
        └── integration/           # Tests de Integración
```

## 🤝 Contribuir

1. Fork el repositorio
2. Crear una rama de feature (`git checkout -b feature/caracteristica-increible`)
3. Commit de los cambios (`git commit -m 'Añadir característica increíble'`)
4. Push a la rama (`git push origin feature/caracteristica-increible`)
5. Abrir un Pull Request

## 📄 Licencia

Este proyecto está licenciado bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

## 👥 Autores

- **Weather API Team** - *Trabajo inicial*

## 🙏 Agradecimientos

- [Open-Meteo](https://open-meteo.com/) por proporcionar la API del clima gratuita
- [Spring Boot](https://spring.io/projects/spring-boot) por el increíble framework
- [Resilience4j](https://resilience4j.readme.io/) por los patrones de resiliencia

## 📞 Soporte

Para soporte, enviar email a weather-api@example.com o abrir un issue en GitHub.

---

**Hecho con ❤️ por Weather API Team**
