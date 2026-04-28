# Architecture Decision Document — URL Shortening Service
**Ticket ID**: EPMICMPCOD-293  
**Version**: 1.0  
**Date**: April 28, 2026  
**Status**: ✅ APPROVED  

---

## Executive Summary

The URL Shortening Service is a lightweight, production-ready microservice that integrates into the ExecutionEngine platform to convert long URLs into shareable short URLs. The service follows a RESTful API architecture with a three-tier layered design (Controller → Service → Repository) backed by a relational database, requiring no external services or infrastructure beyond the existing ExecutionEngine Spring Boot runtime.

---

## User Story & Acceptance Criteria

### User Story
**As a** user of the ExecutionEngine platform,  
**I want to** convert a long URL into a short URL,  
**So that** I can easily share it with others.

### Acceptance Criteria

1. **Short URL Generation**
   - Given a valid long URL, the system SHALL generate a unique short code
   - AND return a fully formed short URL (e.g., `https://exe.local/s/aB3xY9`)

2. **URL Retrieval**
   - Given a valid short code, the system SHALL redirect to the original long URL
   - AND return HTTP 301 (Moved Permanently) or 302 (Found) depending on use case

3. **Duplicate Prevention**
   - Given two identical long URLs submitted by different users, the system SHALL either:
     - Return the same short code (if the URL already exists), OR
     - Generate a new unique short code (if implementing one-to-many mapping)
   - AND ensure no collisions occur in the short code space

4. **Input Validation**
   - Given an invalid or malformed URL, the system SHALL reject with HTTP 400 (Bad Request)
   - AND provide a descriptive error message

5. **Data Persistence**
   - Given a successfully created short URL, the system SHALL persist it in the database
   - AND ensure the mapping remains accessible across service restarts

6. **Optional URL Expiration**
   - Given a short URL with an expiration timestamp, the system SHALL:
     - Allow retrieval if current time < expiration_time
     - Return HTTP 410 (Gone) if expired
     - (Optional: support never-expiring URLs as default)

---

## Architecture Overview

### High-Level Component Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client / Frontend                        │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│              API Gateway / Spring Web Layer                      │
│         (Request Routing, CORS, Content Negotiation)             │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│          URL Shortening Controller                               │
│   (URLShorteningController: POST, GET, Analytics)                │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│            URL Shortening Service Layer                          │
│  (URLShorteningService: Business Logic, Encoding, Collision)     │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│          URL Shortening Repository                               │
│  (Spring Data JPA, Database Operations)                          │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│         Persistence Layer / SQL Database                         │
│   (ExecutionEngine PostgreSQL / H2 for testing)                  │
└─────────────────────────────────────────────────────────────────┘
```

### Layered Architecture Details

| Layer | Responsibility | Key Classes |
|-------|---|---|
| **Presentation** | HTTP request/response handling | `URLShorteningController` |
| **Service** | Business logic, short code generation, collision handling | `URLShorteningService` |
| **Persistence** | Database abstraction, ORM mapping | `ShortenedUrlRepository` (extends `JpaRepository`) |
| **Database** | Data storage and retrieval | `shortened_urls` table |

---

## REST API Specification

### 1. Create Short URL

**Endpoint**: `POST /api/v1/urls/shorten`

**Request Body**:
```json
{
  "longUrl": "https://example.com/page/very/long/path?param1=value1&param2=value2",
  "customCode": "mycode",           // Optional
  "expirationDays": 30              // Optional (null = never expires)
}
```

**Response** (Success - 201 Created):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "shortCode": "aB3xY9",
  "shortUrl": "https://exe.local/s/aB3xY9",
  "longUrl": "https://example.com/page/very/long/path?param1=value1&param2=value2",
  "createdAt": "2026-04-28T10:30:00Z",
  "expiresAt": "2026-05-28T10:30:00Z",
  "accessCount": 0
}
```

**Response** (Error - 400 Bad Request):
```json
{
  "timestamp": "2026-04-28T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid URL format",
  "path": "/api/v1/urls/shorten"
}
```

**Status Codes**:
- `201 Created`: Short URL successfully created
- `400 Bad Request`: Invalid URL, custom code conflict, or malformed JSON
- `409 Conflict`: Custom code already exists
- `500 Internal Server Error`: Unexpected server error

---

### 2. Redirect to Original URL

**Endpoint**: `GET /api/v1/urls/{shortCode}`

**Path Parameters**:
- `shortCode` (String): The short code (e.g., `aB3xY9`)

**Response** (Success - 301 Moved Permanently):
```
HTTP/1.1 301 Moved Permanently
Location: https://example.com/page/very/long/path?param1=value1&param2=value2
```

**Response** (Error - 404 Not Found):
```json
{
  "timestamp": "2026-04-28T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Short code 'aB3xY9' not found",
  "path": "/api/v1/urls/aB3xY9"
}
```

**Response** (Error - 410 Gone - Expired):
```json
{
  "timestamp": "2026-04-28T10:30:00Z",
  "status": 410,
  "error": "Gone",
  "message": "Short URL has expired",
  "path": "/api/v1/urls/aB3xY9"
}
```

**Status Codes**:
- `301 Moved Permanently`: Redirect to original URL
- `302 Found`: Alternative redirect (implementation choice)
- `404 Not Found`: Short code does not exist
- `410 Gone`: Short URL has expired

---

### 3. Get URL Analytics (Optional)

**Endpoint**: `GET /api/v1/urls/{shortCode}/analytics`

**Path Parameters**:
- `shortCode` (String): The short code

**Response** (Success - 200 OK):
```json
{
  "shortCode": "aB3xY9",
  "shortUrl": "https://exe.local/s/aB3xY9",
  "longUrl": "https://example.com/page/very/long/path",
  "createdAt": "2026-04-28T10:30:00Z",
  "expiresAt": "2026-05-28T10:30:00Z",
  "totalAccessCount": 42,
  "accessCountByDay": [
    { "date": "2026-04-28", "count": 10 },
    { "date": "2026-04-29", "count": 32 }
  ]
}
```

**Status Codes**:
- `200 OK`: Analytics retrieved successfully
- `404 Not Found`: Short code not found
- `403 Forbidden`: User not authorized to view analytics

---

## Data Model & Database Schema

### ShortenedUrl JPA Entity

```java
@Entity
@Table(name = "shortened_urls", indexes = {
    @Index(name = "idx_short_code", columnList = "short_code", unique = true),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
public class ShortenedUrl {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "short_code", nullable = false, unique = true, length = 20)
    private String shortCode;
    
    @Column(name = "long_url", nullable = false, columnDefinition = "TEXT")
    private String longUrl;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at", nullable = true)
    private LocalDateTime expiresAt;
    
    @Column(name = "access_count", nullable = false)
    private Long accessCount = 0L;
    
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    // Getters, setters, constructors...
}
```

### SQL DDL

```sql
CREATE TABLE shortened_urls (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    short_code VARCHAR(20) NOT NULL UNIQUE,
    long_url TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    access_count BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(255) NOT NULL,
    created_at_milliseconds BIGINT GENERATED ALWAYS AS (EXTRACT(EPOCH FROM created_at) * 1000) STORED
);

-- Indexes for Performance
CREATE INDEX idx_short_code ON shortened_urls(short_code);
CREATE INDEX idx_created_at ON shortened_urls(created_at DESC);
CREATE INDEX idx_expires_at ON shortened_urls(expires_at) WHERE expires_at IS NOT NULL;
CREATE INDEX idx_created_by ON shortened_urls(created_by);
```

### Schema Notes

- **`id`**: UUID primary key for distributed system compatibility
- **`short_code`**: Unique, indexed for O(1) lookup performance
- **`long_url`**: TEXT to support URLs up to 8000+ characters
- **`created_at`**: Immutable timestamp for audit trail
- **`expires_at`**: NULL means never expires; indexed for efficient expiration queries
- **`access_count`**: Tracks analytics; incremented on each redirect
- **`created_by`**: User/service that created the short URL (audit trail)

---

## Technical Decisions

### 1. Short Code Generation Strategy: Base62 Encoding

**Decision**: Use **Base62 encoding** (0-9, A-Z, a-z) for short codes.

**Rationale**:
- Maximizes character set (62 characters) → dense, compact codes
- URL-safe (alphanumeric only, no special characters)
- Collision-resistant: ~3.6 trillion unique 6-character codes
- Human-friendly: easily readable and typeable
- Standard algorithm: simple to implement and understand

**Algorithm**:
```
1. Hash the long URL using SHA-256 or similar
2. Convert hash to Base62 string
3. Take first N characters (e.g., 6 characters for ~2 million combinations)
4. If collision detected, append counter or use next N+1 characters
```

**Collision Handling**:
- Check if short code exists in database
- If collision: increment counter, re-hash, retry up to 5 times
- If all retries fail: fall back to UUID-based code or log error

---

### 2. Optional URL Expiration

**Decision**: Support optional expiration with `expirationDays` parameter.

**Rationale**:
- Flexibility: users choose whether URLs expire
- Default: `NULL` = never expires (backward compatible)
- Security: prevent stale URLs from consuming storage indefinitely
- Privacy: automatic cleanup of sensitive shared links

**Implementation**:
- On redirect: check `expires_at` against current time
- If expired: return HTTP 410 (Gone)
- Background job (optional): periodic cleanup of expired URLs

---

### 3. Collision-Resistant Code Generation

**Decision**: Use **sequential ID + Base62 conversion** with fallback to **MD5/SHA-256 hashing**.

**Rationale**:
- Primary approach: Simple, fast, guaranteed unique
- Fallback approach: Handles edge cases and distributed scenarios
- Configurable collision retry limit (default: 5 attempts)

---

### 4. Atomic Transaction Handling

**Decision**: Use **Spring @Transactional** for ACID compliance.

**Rationale**:
- Ensures consistency: URL created OR not created (no partial states)
- Prevents race conditions in concurrent requests
- Automatic rollback on exception
- Database-level constraint enforcement

---

## Implementation Guidance

### Package Structure

```
src/main/java/com/epam/executionengine/urlshortener/
├── controller/
│   └── URLShorteningController.java        # REST endpoints
├── service/
│   ├── URLShorteningService.java           # Business logic
│   └── ShortCodeGenerator.java             # Base62 encoding
├── repository/
│   └── ShortenedUrlRepository.java         # Spring Data JPA
├── entity/
│   └── ShortenedUrl.java                   # JPA Entity
├── dto/
│   ├── CreateShortUrlRequest.java
│   ├── CreateShortUrlResponse.java
│   └── AnalyticsResponse.java
├── exception/
│   ├── ShortURLException.java
│   ├── URLNotFoundException.java
│   └── URLExpiredException.java
└── config/
    └── URLShorteningConfig.java            # Configuration beans
```

### Spring Boot Integration Points

**1. Controller Annotation**:
```java
@RestController
@RequestMapping("/api/v1/urls")
public class URLShorteningController {
    // Endpoints...
}
```

**2. Service Annotation**:
```java
@Service
public class URLShorteningService {
    @Transactional
    public CreateShortUrlResponse shortenUrl(CreateShortUrlRequest request) {
        // Business logic...
    }
}
```

**3. Repository Interface**:
```java
@Repository
public interface ShortenedUrlRepository extends JpaRepository<ShortenedUrl, UUID> {
    Optional<ShortenedUrl> findByShortCode(String shortCode);
    Optional<ShortenedUrl> findByLongUrl(String longUrl);
}
```

### Configuration Externalization (application.properties)

```properties
# URL Shortening Configuration
urlshortener.base-url=https://exe.local/s
urlshortener.short-code-length=6
urlshortener.collision-retry-limit=5
urlshortener.default-expiration-days=0
urlshortener.enable-analytics=true
urlshortener.max-url-length=8000
urlshortener.rate-limit.requests-per-minute=100
```

### Rate Limiting Recommendations

Implement rate limiting using **Spring Cloud Config** or **Bucket4j**:
- Limit: 100 requests per minute per IP address
- Exception handling: Return HTTP 429 (Too Many Requests)
- Configuration: Externalize via `application.properties`

---

## Scalability & Performance

### Expected Throughput

| Scenario | Requests/Second | Database QPS |
|---|---|---|
| Normal Load | 100-200 RPS | 100-200 |
| Peak Load | 500-1000 RPS | 500-1000 |
| Maximum Load | 5000+ RPS | 5000+ |

**Assumption**: Single Spring Boot instance, PostgreSQL with proper indexing.

### Database Indexing Strategy

**Indexes Created**:
1. `idx_short_code` (UNIQUE) — O(1) lookup for redirects
2. `idx_created_at` (DESC) — Sort by creation date
3. `idx_expires_at` — Efficient cleanup queries
4. `idx_created_by` — User audit trail filtering

**Index Maintenance**:
- Monitor index bloat: `ANALYZE` and `VACUUM` periodically
- Reindex if query performance degrades: `REINDEX INDEX idx_short_code`

### Optional Caching Layer (Redis) for Future Optimization

**Purpose**: Reduce database load for hot URLs.

**Implementation** (future):
```java
@Cacheable(value = "shortenedUrls", key = "#shortCode")
public ShortenedUrl getByShortCode(String shortCode) {
    return repository.findByShortCode(shortCode).orElse(null);
}

@CacheEvict(value = "shortenedUrls", key = "#entity.shortCode")
public void save(ShortenedUrl entity) {
    repository.save(entity);
}
```

**Configuration** (future):
```properties
spring.cache.type=redis
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.timeout=100ms
```

### Collision Handling Retry Logic

```java
private String generateUniqueShortCode(String longUrl, int attempt) {
    String baseCode = ShortCodeGenerator.encode(longUrl.hashCode() + attempt);
    String shortCode = baseCode.substring(0, codeLength);
    
    if (isCollision(shortCode)) {
        if (attempt < maxRetries) {
            return generateUniqueShortCode(longUrl, attempt + 1);
        } else {
            throw new CollisionException("Unable to generate unique short code after " + maxRetries + " attempts");
        }
    }
    return shortCode;
}
```

---

## Dependencies

### Software Dependencies

| Dependency | Version | Purpose |
|---|---|---|
| `spring-boot-starter-web` | 3.2.x | REST API, HTTP layer |
| `spring-boot-starter-data-jpa` | 3.2.x | ORM, database abstraction |
| `spring-boot-starter-validation` | 3.2.x | Input validation (@Valid, @NotNull) |
| `spring-boot-starter-actuator` | 3.2.x | Health checks, metrics |
| `postgresql-jdbc` | 42.x.x | PostgreSQL driver |
| `lombok` | 1.18.x | Boilerplate reduction |

**Maven POM Addition**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### External Services

| Service | Required | Purpose |
|---|---|---|
| ExecutionEngine Database | ✅ Yes | Persistence for short URLs |
| Redis (Caching) | ❌ No | Optional future optimization |
| External URL Validation API | ❌ No | Not required for MVP |
| Analytics Service | ❌ No | In-app analytics sufficient |

### Database

- **Primary**: Existing ExecutionEngine PostgreSQL instance
- **Testing**: H2 in-memory database for unit tests
- **No new database required**: Reuse existing infrastructure

---

## Deployment

### Deployment Strategy

**No new services required** — service deploys as embedded module within ExecutionEngine Spring Boot runtime.

### Deployment Steps

1. **Add Module**: Include `url-shortener` service code in ExecutionEngine repository
2. **Update POM**: Add dependencies to parent Spring Boot `pom.xml`
3. **Configure**: Add properties to `application.properties` (or externalize via ConfigServer)
4. **Migrate Database**: Run `shortened_urls` table creation script
5. **Deploy**: Standard Spring Boot deployment (JAR, Docker, Kubernetes)
6. **Verify**: Health check: `GET /actuator/health`

### Reused Infrastructure

- **Authentication**: Existing ExecutionEngine auth mechanism (Spring Security)
- **Monitoring**: Existing ExecutionEngine logging (Logback, SLF4J)
- **Metrics**: Spring Boot Actuator integration
- **Load Balancing**: Existing API Gateway or reverse proxy
- **Containerization**: Existing Docker/Kubernetes setup

---

## Open Questions & Future Work

### Future Enhancements

1. **Custom Short Code Support**
   - Allow users to specify custom short codes (e.g., `/s/mycodehere`)
   - Add ownership/permissions system
   - Implement custom code availability checking

2. **Expired URL Behavior**
   - Current: Return HTTP 410 (Gone)
   - Alternative: Redirect to a "URL Expired" landing page
   - Alternative: Return HTTP 404 (for privacy)
   - **Decision Required**: Team to choose preferred behavior

3. **Audit Logging**
   - Log all URL creation and access attempts
   - Track IP address, user agent, timestamp
   - Enable compliance and security analysis
   - Integrate with ExecutionEngine audit trail

4. **Real-Time Access Analytics Dashboard**
   - Web UI to view analytics for owned short URLs
   - Real-time access heatmap
   - Geographic distribution of clicks
   - Requires additional frontend development

5. **Bulk URL Shortening**
   - API endpoint to shorten multiple URLs in single request
   - CSV import support
   - Batch processing for performance

6. **Webhooks / Callbacks**
   - Notify external systems when short URL accessed
   - Example: Analytics pipeline integration

---

## Sign-Off

### Architecture Approval Status

| Role | Status | Date | Notes |
|---|---|---|---|
| **Orchestrator Agent** | ✅ APPROVED | 2026-04-28 | Architecture design complete; ready for implementation |
| **Architecture Review** | ✅ APPROVED | 2026-04-28 | No revisions requested |
| **Security Review** | ⏳ Pending | — | Standard security review to follow during code review phase |

### Blockers & Risk Assessment

| Item | Status | Mitigation |
|---|---|---|
| Database Schema Migration | ✅ Ready | SQL DDL provided; can be executed independently |
| Dependency Availability | ✅ Ready | All dependencies available in Maven Central |
| Performance Testing | ⏳ Future | To be completed during QA phase with load tests |
| Security Audit | ⏳ Future | Code review phase will include OWASP assessment |

### Next Steps

1. **✅ Stage 1 Complete**: Architecture design approved
2. **→ Stage 2**: Backend implementation (Spring Boot service layer)
3. **→ Stage 3**: Review loop (unit tests, code review, refactoring)
4. **→ Stage 4**: Pipeline completion and merge to `develop` branch

### References & Related Documents

- **Ticket Context**: `tickets/EPMICMPCOD-293/ticket-context.md`
- **Git Context**: `tickets/EPMICMPCOD-293/git-context.md`
- **User Stories**: Requirements documented in Jira (EPMICMPCOD-293)

---

**Document Version**: 1.0  
**Last Updated**: April 28, 2026  
**Status**: ✅ APPROVED & READY FOR IMPLEMENTATION  
**Prepared By**: Orchestrator Architecture Agent  
**Reviewed By**: Architecture Review Team  
