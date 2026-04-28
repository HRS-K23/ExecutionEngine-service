# ExecutionEngine Service - URL Shortening Microservice

## Overview

The **ExecutionEngine Service** is a production-ready Spring Boot 3.2 microservice that provides URL shortening capabilities. It converts long URLs into shareable short codes using Base62 encoding, tracks access statistics, and supports URL expiration.

### Key Features

- ✅ **URL Shortening**: Convert long URLs into 6-character Base62 short codes
- ✅ **Collision Handling**: Automatic retry logic for short code uniqueness
- ✅ **URL Expiration**: Optional expiration dates for temporary links
- ✅ **Access Analytics**: Track total and daily access counts
- ✅ **REST API**: Full OpenAPI/Swagger documentation
- ✅ **Global Exception Handling**: Consistent error responses
- ✅ **Production Ready**: Health checks, metrics, logging

## Technology Stack

| Component | Version |
|-----------|---------|
| **Java** | 21 LTS |
| **Spring Boot** | 3.2.0 |
| **Spring Data JPA** | 3.2.0 |
| **Database** | PostgreSQL 12+ (dev: H2 in-memory) |
| **Build Tool** | Maven 3.8+ |
| **Documentation** | OpenAPI 3.0 / Swagger UI |

## Project Structure

```
ExecutionEngine-service/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/epam/executionengine/
│   │   │   ├── ExecutionEngineApplication.java
│   │   │   └── urlshortener/
│   │   │       ├── controller/
│   │   │       │   └── URLShorteningController.java
│   │   │       ├── service/
│   │   │       │   ├── URLShorteningService.java
│   │   │       │   └── ShortCodeGenerator.java
│   │   │       ├── repository/
│   │   │       │   └── ShortenedUrlRepository.java
│   │   │       ├── entity/
│   │   │       │   └── ShortenedUrl.java
│   │   │       ├── dto/
│   │   │       │   ├── CreateShortUrlRequest.java
│   │   │       │   ├── CreateShortUrlResponse.java
│   │   │       │   └── AnalyticsResponse.java
│   │   │       ├── exception/
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   ├── ShortURLException.java
│   │   │       │   ├── URLNotFoundException.java
│   │   │       │   ├── URLExpiredException.java
│   │   │       │   └── RestApiErrorResponse.java
│   │   │       └── config/
│   │   │           └── URLShorteningConfig.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       └── logback-spring.xml
│   └── test/ (handled by Unit Test Agent)
└── tickets/EPMICMPCOD-293/
    ├── architecture/
    │   └── architecture-decision.md
    ├── implementation/
    │   ├── files-changed.md
    │   └── implementation-notes.md
    └── ...
```

## Database Schema

### Table: `shortened_urls`

```sql
CREATE TABLE shortened_urls (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    short_code VARCHAR(20) NOT NULL UNIQUE,
    long_url TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    access_count BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(255) NOT NULL
);

-- Indexes for performance
CREATE INDEX idx_short_code ON shortened_urls(short_code);
CREATE INDEX idx_created_at ON shortened_urls(created_at DESC);
CREATE INDEX idx_expires_at ON shortened_urls(expires_at) WHERE expires_at IS NOT NULL;
CREATE INDEX idx_created_by ON shortened_urls(created_by);
```

## Build Instructions

### Prerequisites

- **Java 21 JDK** or higher
- **Maven 3.8.1** or higher
- **PostgreSQL 12+** (for production) or **H2** (for development)
- **Git** for version control

### Build the Project

```bash
# Navigate to project root
cd ExecutionEngine-service

# Build with Maven
mvn clean install

# Build without running tests (for faster builds)
mvn clean install -DskipTests

# Package as JAR
mvn clean package
```

### Build Output

- **JAR file**: `target/execution-engine-service-1.0.0.jar`
- **Executable**: Ready to run with `java -jar`

## Running the Application

### Development Environment (H2 In-Memory Database)

```bash
# Run with development profile
java -jar target/execution-engine-service-1.0.0.jar --spring.profiles.active=dev

# Or using Maven
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

**Access Points (Development):**
- 🌐 **API**: http://localhost:8080/api/v1/urls
- 📚 **Swagger UI**: http://localhost:8080/swagger-ui.html
- 🔍 **OpenAPI Docs**: http://localhost:8080/v3/api-docs
- 💾 **H2 Console**: http://localhost:8080/h2-console

### Production Environment (PostgreSQL)

```bash
# Set environment variables
export DB_URL=jdbc:postgresql://db-host:5432/executionengine
export DB_USERNAME=postgres
export DB_PASSWORD=your_password

# Run with production profile
java -jar target/execution-engine-service-1.0.0.jar --spring.profiles.active=prod
```

## API Examples

### Create a Shortened URL

```bash
curl -X POST http://localhost:8080/api/v1/urls/shorten \
  -H "Content-Type: application/json" \
  -d '{
    "longUrl": "https://example.com/very/long/path?param=value",
    "expirationDays": 30
  }'
```

### Redirect Using Short Code

```bash
curl -L http://localhost:8080/api/v1/urls/aB3xY9
```

### Get Analytics

```bash
curl http://localhost:8080/api/v1/urls/aB3xY9/analytics
```

## Configuration

- **Development**: H2 in-memory database, DEBUG logging
- **Production**: PostgreSQL, environment variables for secrets
- All configuration externalizable via properties files or environment variables

## Support

**Team**: EPAM ExecutionEngine Team  
**Issue Tracking**: EPMICMPCOD-293