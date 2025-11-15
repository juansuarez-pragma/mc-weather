# Guía de Inicio Rápido

## 🚀 Funcionando en 5 Minutos

### Requisitos Previos

Asegúrate de tener instalado:
- ✅ Java 17 o superior (`java -version`)
- ✅ Maven 3.9+ (`mvn -version`)

### Paso 1: Compilar el Proyecto

```bash
mvn clean install
```

Esto hará:
- Descargar todas las dependencias
- Compilar el código
- Ejecutar todos los tests (cobertura >80%)
- Crear el JAR ejecutable

### Paso 2: Ejecutar la Aplicación

```bash
mvn spring-boot:run
```

O ejecutar el JAR directamente:
```bash
java -jar target/weather-api-service-1.0.0.jar
```

### Paso 3: Verificar que Está Funcionando

Abre tu navegador y ve a:
- 🏥 Health Check: http://localhost:8080/actuator/health
- 📚 Documentación de API: http://localhost:8080/swagger-ui.html

### Paso 4: Probar la API

**Obtener Clima (Nueva York):**
```bash
curl "http://localhost:8080/api/v1/weather/forecast?latitude=40.7128&longitude=-74.0060"
```

**Buscar Ciudades:**
```bash
curl "http://localhost:8080/api/v1/weather/search?name=London"
```

---

## 🐳 Alternativa con Docker

Si prefieres Docker:

```bash
# Compilar y ejecutar
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener
docker-compose down
```

---

## 📊 Acceder al Monitoreo

- **Métricas Prometheus**: http://localhost:8080/actuator/prometheus
- **Health Check**: http://localhost:8080/actuator/health
- **Información de la Aplicación**: http://localhost:8080/actuator/info

---

## 🧪 Ejecutar Tests

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar con reporte de cobertura
mvn clean test jacoco:report

# Ver reporte de cobertura
open target/site/jacoco/index.html
```

---

## 📦 Importar Colección de Postman

1. Abrir Postman
2. Click en **Import**
3. Seleccionar `Weather-API.postman_collection.json`
4. Empezar a probar todos los endpoints

---

## 🔧 Cambiar Configuración

Editar `src/main/resources/application.yml`:

```yaml
# Cambiar puerto del servidor
server:
  port: 9090

# Ajustar TTL de caché
spring:
  cache:
    caffeine:
      spec: maximumSize=5000,expireAfterWrite=10m

# Cambiar límite de tasa
rate-limit:
  requests-per-minute: 120
```

---

## 📱 Conectar con la App iOS

Actualiza la URL base de tu app iOS a:

**Desarrollo Local:**
```swift
let baseURL = "http://localhost:8080"
```

**Docker (desde el Simulador iOS):**
```swift
let baseURL = "http://host.docker.internal:8080"
```

**Producción:**
```swift
let baseURL = "https://tu-dominio.com"
```

Luego reemplaza todas las llamadas a la API de Open-Meteo con llamadas al backend:

**Antes (API Directa):**
```swift
let url = "https://api.open-meteo.com/v1/forecast?latitude=\(lat)&longitude=\(lon)..."
```

**Después (A través del Backend):**
```swift
let url = "\(baseURL)/api/v1/weather/forecast?latitude=\(lat)&longitude=\(lon)"
```

---

## ⚡ Consejos de Rendimiento

1. **El Caché está Activo**: La segunda petición a las mismas coordenadas será instantánea
2. **Circuit Breaker**: Protege contra fallos de la API de Open-Meteo
3. **Rate Limiting**: Previene abuso (60 req/min por defecto)

---

## 🐛 Resolución de Problemas

### Puerto 8080 Ya en Uso

```bash
# Cambiar puerto en application.yml
server:
  port: 9090
```

### Tests Fallando

```bash
# Saltar tests durante la compilación
mvn clean install -DskipTests
```

### No se Puede Conectar a la API de Open-Meteo

Verificar el estado del circuit breaker:
```bash
curl http://localhost:8080/actuator/health
```

Si el circuito está abierto, espera 10 segundos e intenta de nuevo.

---

## 📚 Próximos Pasos

1. ✅ Leer [README.md](README.md) para documentación completa
2. ✅ Revisar [ARCHITECTURE.md](ARCHITECTURE.md) para decisiones de diseño
3. ✅ Importar colección de Postman para testing
4. ✅ Desplegar con Docker Compose
5. ✅ Actualizar la app iOS para usar el backend

---

**¡Feliz Codificación! 🎉**
