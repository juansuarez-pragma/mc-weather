# Quick Start Guide

## 🚀 Get Running in 5 Minutes

### Prerequisites

Make sure you have installed:
- ✅ Java 17 or higher (`java -version`)
- ✅ Maven 3.9+ (`mvn -version`)

### Step 1: Build the Project

```bash
mvn clean install
```

This will:
- Download all dependencies
- Compile the code
- Run all tests (80%+ coverage)
- Create executable JAR

### Step 2: Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR directly:
```bash
java -jar target/weather-api-service-1.0.0.jar
```

### Step 3: Verify It's Running

Open your browser and go to:
- 🏥 Health Check: http://localhost:8080/actuator/health
- 📚 API Docs: http://localhost:8080/swagger-ui.html

### Step 4: Test the API

**Get Weather (New York):**
```bash
curl "http://localhost:8080/api/v1/weather/forecast?latitude=40.7128&longitude=-74.0060"
```

**Search Cities:**
```bash
curl "http://localhost:8080/api/v1/weather/search?name=London"
```

---

## 🐳 Docker Alternative

If you prefer Docker:

```bash
# Build and run
docker-compose up -d

# Check logs
docker-compose logs -f

# Stop
docker-compose down
```

---

## 📊 Access Monitoring

- **Prometheus Metrics**: http://localhost:8080/actuator/prometheus
- **Health Check**: http://localhost:8080/actuator/health
- **Application Info**: http://localhost:8080/actuator/info

---

## 🧪 Run Tests

```bash
# Run all tests
mvn test

# Run with coverage report
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

---

## 📦 Import Postman Collection

1. Open Postman
2. Click **Import**
3. Select `Weather-API.postman_collection.json`
4. Start testing all endpoints

---

## 🔧 Change Configuration

Edit `src/main/resources/application.yml`:

```yaml
# Change server port
server:
  port: 9090

# Adjust cache TTL
spring:
  cache:
    caffeine:
      spec: maximumSize=5000,expireAfterWrite=10m

# Change rate limit
rate-limit:
  requests-per-minute: 120
```

---

## 📱 Connect iOS App

Update your iOS app's base URL to:

**Local Development:**
```swift
let baseURL = "http://localhost:8080"
```

**Docker (from iOS Simulator):**
```swift
let baseURL = "http://host.docker.internal:8080"
```

**Production:**
```swift
let baseURL = "https://your-domain.com"
```

Then replace all Open-Meteo API calls with backend calls:

**Before (Direct API):**
```swift
let url = "https://api.open-meteo.com/v1/forecast?latitude=\(lat)&longitude=\(lon)..."
```

**After (Through Backend):**
```swift
let url = "\(baseURL)/api/v1/weather/forecast?latitude=\(lat)&longitude=\(lon)"
```

---

## ⚡ Performance Tips

1. **Cache is Active**: Second request to same coordinates will be instant
2. **Circuit Breaker**: Protects against Open-Meteo API failures
3. **Rate Limiting**: Prevents abuse (60 req/min default)

---

## 🐛 Troubleshooting

### Port 8080 Already in Use

```bash
# Change port in application.yml
server:
  port: 9090
```

### Tests Failing

```bash
# Skip tests during build
mvn clean install -DskipTests
```

### Cannot Connect to Open-Meteo API

Check circuit breaker status:
```bash
curl http://localhost:8080/actuator/health
```

If circuit is open, wait 10 seconds and try again.

---

## 📚 Next Steps

1. ✅ Read [README.md](README.md) for full documentation
2. ✅ Review [ARCHITECTURE.md](ARCHITECTURE.md) for design decisions
3. ✅ Import Postman collection for testing
4. ✅ Deploy with Docker Compose
5. ✅ Update iOS app to use backend

---

**Happy Coding! 🎉**
