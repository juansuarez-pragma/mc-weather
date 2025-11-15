# Documentación de Arquitectura

## Descripción General

Este documento describe las decisiones arquitectónicas, patrones y principios de diseño utilizados en el Servicio API del Clima.

## Tabla de Contenidos

1. [Estilo Arquitectónico](#estilo-arquitectónico)
2. [Descripción de Capas](#descripción-de-capas)
3. [Patrones de Diseño](#patrones-de-diseño)
4. [Flujo de Datos](#flujo-de-datos)
5. [Patrones de Resiliencia](#patrones-de-resiliencia)
6. [Estrategia de Caché](#estrategia-de-caché)
7. [Consideraciones de Seguridad](#consideraciones-de-seguridad)
8. [Optimizaciones de Rendimiento](#optimizaciones-de-rendimiento)
9. [ADRs (Registros de Decisiones Arquitectónicas)](#registros-de-decisiones-arquitectónicas)

---

## Estilo Arquitectónico

### Arquitectura Hexagonal (Puertos y Adaptadores)

El sistema sigue los principios de **Arquitectura Hexagonal** para lograr:

- **Separación de Responsabilidades**: Lógica de negocio aislada de dependencias externas
- **Testabilidad**: Fácil de probar con dependencias mockeadas
- **Flexibilidad**: Fácil de intercambiar implementaciones (ej: diferentes clientes de API)
- **Mantenibilidad**: Límites claros entre capas

```
┌──────────────────────────────────────────────┐
│         Capa de Infraestructura              │
│  (Frameworks, Bases de Datos, APIs Externas) │
│                                              │
│  ┌────────────┐         ┌────────────┐      │
│  │ REST       │         │ Feign      │      │
│  │ Controller │         │ Client     │      │
│  └─────┬──────┘         └──────┬─────┘      │
└────────┼───────────────────────┼────────────┘
         │                       │
         │    ┌─────────────┐    │
         ├────│   Puertos   │────┤
         │    │ (Interfaces)│    │
         │    └─────────────┘    │
         │                       │
┌────────┼───────────────────────┼────────────┐
│        ▼                       ▼            │
│         Capa de Aplicación                  │
│  (Casos de Uso, Reglas de Negocio)          │
│                                              │
│  ┌────────────────────────────────┐         │
│  │   WeatherService               │         │
│  │   (Lógica de Negocio)          │         │
│  └────────────────────────────────┘         │
└──────────────────────────────────────────────┘
                    │
┌───────────────────┼──────────────────────────┐
│                   ▼                          │
│         Capa de Dominio                      │
│  (Modelos de Negocio Core)                   │
│                                              │
│  ┌────────────┐  ┌────────────┐             │
│  │  Weather   │  │  Location  │             │
│  │  (Entity)  │  │  (Value    │             │
│  │            │  │   Object)  │             │
│  └────────────┘  └────────────┘             │
└──────────────────────────────────────────────┘
```

---

## Descripción de Capas

### 1. Capa de Dominio (`com.weather.api.domain`)

**Propósito**: Lógica de negocio central y reglas

**Componentes**:
- **Models**: `Weather`, `Location`, `GeocodingResult`
- **Exceptions**: `InvalidCoordinatesException`, `CityNotFoundException`, `ExternalApiException`
- **Ports**: `WeatherUseCase` (entrada), `WeatherRepositoryPort` (salida)

**Principios**:
- Sin dependencias de frameworks
- Objetos Java puros
- Inmutables cuando sea posible (usando `@Value` de Lombok)
- Modelos auto-validables

**Ejemplo**:
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

### 2. Capa de Aplicación (`com.weather.api.application`)

**Propósito**: Orquestar la lógica de negocio y coordinar entre capas

**Componentes**:
- **Services**: `WeatherService` implementa `WeatherUseCase`
- **DTOs**: Objetos Request/Response para la API
- **Mappers**: Transforman entre modelos de dominio y DTOs

**Responsabilidades**:
- Validación de entrada
- Anotaciones de caché
- Contexto de logging MDC
- Delegación de manejo de errores

**Ejemplo**:
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

### 3. Capa de Infraestructura (`com.weather.api.infrastructure`)

**Propósito**: Detalles de implementación técnica

**Componentes**:
- **Adapters**: Controladores REST, clientes Feign
- **Configuration**: Caché, CORS, Swagger, Resiliencia
- **Monitoring**: Indicadores de salud, métricas

**Características**:
- Dependiente de frameworks
- Integraciones externas
- Preocupaciones técnicas transversales

---

## Patrones de Diseño

### 1. **Patrón Repository**

Aunque estamos llamando una API HTTP, la tratamos como un repositorio:

```java
// Puerto (interface en el dominio)
public interface WeatherRepositoryPort {
    Weather fetchWeatherForecast(Double latitude, Double longitude, String timezone);
}

// Adaptador (implementación en infraestructura)
@Component
public class OpenMeteoClientImpl implements WeatherRepositoryPort {
    // Implementación usando Feign
}
```

**Beneficios**:
- Fácil de probar con mocks
- Se pueden intercambiar implementaciones (ej: diferentes APIs del clima)
- La capa de dominio no conoce HTTP

### 2. **Patrón Mapper**

Separar DTOs de modelos de dominio:

```java
@Component
public class WeatherMapper {
    public WeatherForecastResponse toResponse(Weather weather) {
        // Lógica de mapeo
    }
}
```

**Beneficios**:
- Los modelos de dominio se mantienen limpios
- Contrato de API separado de la lógica de negocio
- Flexibilidad para versionado

### 3. **Patrón Circuit Breaker**

Usando Resilience4j:

```java
@CircuitBreaker(name = "openMeteoService", fallbackMethod = "fetchWeatherForecastFallback")
@Retry(name = "openMeteoService")
public Weather fetchWeatherForecast(...) {
    // Llamar a la API externa
}
```

**Estados**:
- **CLOSED**: Operación normal
- **OPEN**: Demasiadas fallas, rechazar peticiones inmediatamente
- **HALF_OPEN**: Probar si el servicio se recuperó

### 4. **Patrón Retry**

Backoff exponencial para fallas transitorias:

```yaml
resilience4j:
  retry:
    instances:
      openMeteoService:
        maxAttempts: 3
        waitDuration: 1s
        exponentialBackoffMultiplier: 2
```

**Secuencia de Reintentos**:
1. La llamada inicial falla
2. Esperar 1s, reintentar
3. Esperar 2s, reintentar
4. Esperar 4s, reintentar
5. Rendirse, lanzar excepción

### 5. **Patrón Builder**

Para construir objetos complejos:

```java
Weather weather = Weather.builder()
    .temperature(15.5)
    .weatherCode(2)
    .latitude(40.7128)
    .longitude(-74.0060)
    .build();
```

---

## Flujo de Datos

### Flujo de Peticiones (Pronóstico del Clima)

```
App iOS
  │
  │ HTTP GET /api/v1/weather/forecast?latitude=40.7128&longitude=-74.0060
  │
  ▼
┌─────────────────────────────────────────┐
│ 1. WeatherController                    │
│    - Validar parámetros de petición     │
│    - Verificar rate limiting            │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│ 2. WeatherService                       │
│    - Verificar caché (Caffeine)         │
│    - Si está en caché: retornar inmediato│
│    - Si no: continuar al repositorio    │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│ 3. OpenMeteoClientImpl                  │
│    - Verificar circuit breaker          │
│    - Llamar a Open-Meteo API via Feign  │
│    - Reintentar si hay falla transitoria│
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│ 4. Open-Meteo API                       │
│    - Servicio del clima externo         │
└─────────────┬───────────────────────────┘
              │
              ▼ Respuesta
┌─────────────────────────────────────────┐
│ 5. Mapear a Modelo de Dominio           │
│    - OpenMeteoWeatherResponse → Weather │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│ 6. Almacenar en Caché                   │
│    - TTL: 5 minutos                     │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│ 7. Mapear a DTO                         │
│    - Weather → WeatherForecastResponse  │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│ 8. Retornar Respuesta JSON              │
└─────────────────────────────────────────┘
```

---

## Patrones de Resiliencia

### Configuración del Circuit Breaker

```yaml
resilience4j:
  circuitbreaker:
    instances:
      openMeteoService:
        slidingWindowSize: 10           # Rastrear últimas 10 llamadas
        minimumNumberOfCalls: 5         # Necesita 5 llamadas para calcular tasa de fallo
        failureRateThreshold: 50        # Abrir si 50% fallan
        waitDurationInOpenState: 10s    # Permanecer abierto por 10s
        permittedNumberOfCallsInHalfOpenState: 3  # Permitir 3 llamadas de prueba
```

**Matriz de Decisiones**:

| Escenario | Acción | Razón |
|----------|--------|--------|
| 5/10 llamadas fallan | Abrir circuito | Tasa de fallo = 50% |
| Circuito abierto | Rechazar inmediatamente | Prevenir fallas en cascada |
| Después de 10s | Entrar a half-open | Probar si el servicio se recuperó |
| 3/3 llamadas de prueba exitosas | Cerrar circuito | Servicio recuperado |
| Cualquier llamada de prueba falla | Re-abrir circuito | No recuperado aún |

### Estrategia de Fallback

Cuando el circuito está abierto o todos los reintentos se agotaron:

```java
private Weather fetchWeatherForecastFallback(Exception e) {
    throw new ExternalApiException(
        "El servicio del clima no está disponible actualmente. Por favor, intenta más tarde.",
        e
    );
}
```

**Estrategias Alternativas** (no implementadas, para el futuro):
- Retornar datos en caché incluso si están expirados
- Retornar datos del clima por defecto
- Llamar a una API de respaldo del clima

---

## Estrategia de Caché

### Configuración

```java
Caffeine.newBuilder()
    .maximumSize(1000)              // Máximo 1000 entradas
    .expireAfterWrite(5, MINUTES)   // Expirar después de 5 minutos
    .recordStats()                   // Habilitar métricas
    .build();
```

### Claves de Caché

- **Pronóstico del Clima**: `{latitude}_{longitude}`
- **Búsqueda de Ciudades**: `{cityName}_{count}_{language}`

### ¿Por Qué 5 Minutos?

| Factor | Consideración |
|--------|---------------|
| **Frescura de Datos** | El clima no cambia cada segundo |
| **Costo de API** | Reducir llamadas a API externa |
| **Experiencia de Usuario** | Respuesta rápida para peticiones repetidas |
| **Memoria** | 1000 entradas ≈ 10MB RAM |

### Invalidación de Caché

- **Basada en tiempo**: Automática después de 5 minutos (expireAfterWrite)
- **Basada en tamaño**: Evicción LRU cuando se alcanza el tamaño máximo
- **Manual**: No implementada (se podría añadir endpoint para limpiar caché)

---

## Consideraciones de Seguridad

### 1. Validación de Entrada

**Múltiples capas**:

```java
// 1. Bean Validation (Controlador)
@NotNull
@DecimalMin("-90.0")
@DecimalMax("90.0")
Double latitude;

// 2. Validación de Negocio (Servicio)
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

Prevenir abuso:

```yaml
resilience4j:
  ratelimiter:
    instances:
      openMeteoService:
        limitForPeriod: 60      # 60 peticiones
        limitRefreshPeriod: 60s  # por minuto
```

Retorna `429 Too Many Requests` cuando se excede.

### 3. Configuración CORS

```yaml
cors:
  allowed-origins: "*"           # Cambiar a dominio específico en producción
  allowed-methods: GET,OPTIONS
  allowed-headers: "*"
```

**Producción**: Debería restringir `allowed-origins` al dominio de la app iOS.

### 4. Mensajes de Error

Nunca exponer:
- Stack traces (en producción)
- Detalles de implementación interna
- Credenciales de base de datos/API

**Respuesta de error segura**:
```json
{
  "message": "El servicio del clima está temporalmente no disponible"
}
```

En lugar de:
```json
{
  "message": "Connection timeout to api.open-meteo.com:443"
}
```

---

## Optimizaciones de Rendimiento

### 1. Caché

**Impacto**:
- Acierto de caché: ~1ms tiempo de respuesta
- Fallo de caché: ~200ms tiempo de respuesta
- Objetivo de tasa de acierto: >70%

### 2. Connection Pooling

Feign usa connection pooling integrado:

```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 3000
        readTimeout: 5000
```

### 3. Multi-stage Docker Build

Reduce el tamaño de la imagen:
- Etapa de construcción: ~600MB
- Etapa de ejecución: ~200MB

### 4. Ajuste de JVM

```dockerfile
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport"
```

---

## Registros de Decisiones Arquitectónicas

### ADR-001: ¿Por Qué Arquitectura Hexagonal?

**Contexto**: Necesidad de construir un servicio backend mantenible y testeable.

**Decisión**: Usar Arquitectura Hexagonal (Puertos y Adaptadores).

**Consecuencias**:
- ✅ Fácil de probar con dependencias mockeadas
- ✅ Se pueden intercambiar proveedores de API del clima
- ✅ Separación clara de la lógica de negocio
- ❌ Más código boilerplate (interfaces, adaptadores)
- ❌ Curva de aprendizaje más pronunciada para nuevos desarrolladores

**Estado**: Aceptado

---

### ADR-002: ¿Por Qué Caffeine sobre Redis?

**Contexto**: Necesidad de caché para datos del clima.

**Opciones**:
1. Caffeine (en memoria)
2. Redis (distribuido)

**Decisión**: Usar Caffeine para la versión inicial.

**Razonamiento**:
- Los datos del clima no son críticos (se pueden volver a obtener)
- Despliegue de una sola instancia (no se necesita caché distribuido)
- Configuración más simple (sin dependencias externas)
- Menor latencia (~1ms vs ~5ms)

**Futuro**: Si escalamos horizontalmente, migrar a Redis.

**Estado**: Aceptado

---

### ADR-003: ¿Por Qué OpenFeign sobre RestTemplate?

**Contexto**: Necesidad de cliente HTTP para llamadas a API externa.

**Decisión**: Usar Spring Cloud OpenFeign.

**Razonamiento**:
- Sintaxis declarativa (código más limpio)
- Mejor integración con Resilience4j
- Serialización/deserialización automática
- Estándar de la industria para microservicios

**Estado**: Aceptado

---

### ADR-004: ¿Por Qué No Almacenar Datos de Usuario?

**Contexto**: Se podría cachear el historial de búsqueda de usuario en base de datos.

**Decisión**: NO almacenar datos de usuario del lado del backend.

**Razonamiento**:
- La app iOS ya maneja el historial local (UserDefaults)
- Evitar preocupaciones de GDPR/privacidad
- Backend más simple (stateless)
- Reducir costo de infraestructura (no se necesita base de datos)

**Estado**: Aceptado

---

## Mejoras Futuras

### 1. Integración de Base de Datos

Si decidimos almacenar preferencias de usuario:
- PostgreSQL para datos relacionales
- Redis para caché distribuido
- Flyway para migraciones

### 2. Autenticación

Añadir autenticación basada en JWT:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Configuración de filtro JWT
}
```

### 3. Monitoreo Avanzado

- Trazado distribuido (Zipkin/Jaeger)
- Agregación de logs (stack ELK)
- Dashboards personalizados de Grafana

### 4. Despliegue Multi-Región

- Arquitectura activo-activo
- Balanceo de carga geográfico
- Redis para caché compartido

---

## Conclusión

Esta arquitectura proporciona:
- ✅ **Mantenibilidad**: Separación clara de responsabilidades
- ✅ **Testabilidad**: Alta cobertura de tests posible
- ✅ **Resiliencia**: Circuit breaker + retry + caché
- ✅ **Rendimiento**: Respuestas rápidas con caché
- ✅ **Escalabilidad**: Stateless, puede escalar horizontalmente
- ✅ **Observabilidad**: Monitoreo completo

El diseño sigue las mejores prácticas de la industria para un servicio backend listo para producción.

---

**Última Actualización**: 2025-11-15
**Autor**: Weather API Team
