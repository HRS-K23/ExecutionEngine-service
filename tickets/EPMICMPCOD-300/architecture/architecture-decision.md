# Architecture Decision Record — EPMICMPCOD-300: View Task List

**Title:** Self-Contained Spring Boot Application with Embedded H2 Database  
**Date:** 2026-04-29  
**Status:** ✅ APPROVED  
**Ticket ID:** EPMICMPCOD-300  
**Decision Owner:** Architecture Design Agent  
**Reviewed By:** Orchestrator Ticket Agent  

---

## 1. Executive Summary

This Architecture Decision Record (ADR) documents the approved architecture design for implementing the **View Task List** feature (EPMICMPCOD-300). The solution is a self-contained Spring Boot 3.4.5 application with an embedded H2 database, providing developers with a fully functional, zero-configuration local development environment that requires no external services or infrastructure.

**Key Decision:** Deploy as a single executable JAR with embedded H2 database, eliminating setup complexity while maintaining production-grade architecture patterns.

---

## 2. System Overview

### What the System Does

The **View Task List** feature enables authenticated users to retrieve a paginated, filterable list of tasks assigned to them through a RESTful API. The system manages task data, applies business logic for access control, and returns tasks in a structured JSON format.

**Core Responsibilities:**
- Accept HTTP GET requests for task retrieval
- Authenticate users via Bearer token
- Apply role-based access control
- Query task data from H2 embedded database
- Return paginated, sorted results
- Handle error cases gracefully

### Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Runtime** | Java | 21 (LTS) |
| **Framework** | Spring Boot | 3.4.5 |
| **Web Layer** | Spring Web MVC | 3.4.5 |
| **Data Access** | Spring Data JPA + Hibernate | 3.4.5 |
| **Database** | H2 (Embedded) | 2.x |
| **Security** | Spring Security | 3.4.5 |
| **Testing** | JUnit 5, Mockito, Spring Boot Test | Latest |
| **Build Tool** | Maven | 3.9+ |
| **Containerization** | Docker | Optional (Phase 2) |

### Key Objectives

1. **Self-Contained:** No external dependencies (PostgreSQL, Redis, auth servers)
2. **Zero Configuration:** Developers clone repository and run immediately
3. **Production-Grade Patterns:** Layered architecture, proper separation of concerns
4. **Local Development:** Fast iteration, easy debugging, no network latency
5. **Testability:** Comprehensive test coverage with mock authentication
6. **Scalability Foundation:** Architecture supports future scaling to distributed systems

---

## 3. High-Level Architecture

### Component Diagram

```
┌─────────────────────────────────────────────────────────┐
│                   HTTP Client / Postman                 │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP GET /api/v1/tasks
                       │ Authorization: Bearer <token>
                       ▼
┌─────────────────────────────────────────────────────────┐
│                 Spring Boot Application                 │
│ ┌─────────────────────────────────────────────────────┐ │
│ │         Spring Security Filter Chain                │ │
│ │  (JWT Validation, Principal Extraction)             │ │
│ └──────────────────────┬────────────────────────────────┤ │
│                        ▼                               │ │
│ ┌─────────────────────────────────────────────────────┐ │
│ │      TaskController (REST Endpoint)                 │ │
│ │  • GET /api/v1/tasks (paginated, filtered)         │ │
│ │  • @PostAuthorize for access control               │ │
│ └──────────────────────┬────────────────────────────────┤ │
│                        ▼                               │ │
│ ┌─────────────────────────────────────────────────────┐ │
│ │         TaskService (Business Logic)                │ │
│ │  • Apply filtering and pagination                   │ │
│ │  • Enforce user-task ownership                      │ │
│ │  • Apply sorting logic                              │ │
│ └──────────────────────┬────────────────────────────────┤ │
│                        ▼                               │ │
│ ┌─────────────────────────────────────────────────────┐ │
│ │      TaskRepository (Data Access)                   │ │
│ │  • JPA interface for CRUD operations                │ │
│ │  • Custom query methods                             │ │
│ │  • Pagination support                               │ │
│ └──────────────────────┬────────────────────────────────┤ │
│                        ▼                               │ │
│ ┌─────────────────────────────────────────────────────┐ │
│ │      H2 Embedded Database (In-Memory)               │ │
│ │  • Task table with schema                           │ │
│ │  • Indexes for performance                          │ │
│ │  • DDL auto-creation via Spring Boot                │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### Request Flow Sequence

```
User Request
    │
    ▼
Spring Security Filter
    │ Validates Bearer token
    │ Extracts UserPrincipal
    │
    ▼
TaskController.getTasks()
    │ Receives pagination params
    │ Current user from SecurityContext
    │
    ▼
TaskService.getTasksByUser()
    │ Applies filters (status, assignee)
    │ Applies pagination
    │ Calls repository
    │
    ▼
TaskRepository.findByUserIdAndStatus()
    │ Executes SQL query on H2
    │
    ▼
H2 Database
    │ Returns task records
    │
    ▼
TaskService (Map to DTOs)
    │ Converts Entity → DTO
    │ Enriches response data
    │
    ▼
TaskController
    │ Wraps in HTTP response (200 OK)
    │
    ▼
HTTP Response (JSON)
    │ Content-Type: application/json
```

---

## 4. Detailed Component Design

### 4.1 TaskController (REST Endpoint Handler)

**Responsibility:** Handle HTTP requests, validate inputs, delegate to service layer, return responses

**Key Considerations:**
- Single responsibility: HTTP request/response handling
- No business logic
- Input validation via `@Valid`
- Error handling via `@ExceptionHandler`
- RESTful conventions

**Annotations & Features:**
- `@RestController`: Spring annotation for REST endpoints
- `@RequestMapping("/api/v1/tasks")`: Base endpoint
- `@GetMapping`: GET HTTP method binding
- `@PostAuthorize`: Verify user owns task before returning
- `@RequestParam`: Pagination and filtering parameters

### 4.2 TaskService (Business Logic Layer)

**Responsibility:** Implement business rules, orchestrate repository calls, handle pagination/filtering

**Capabilities:**
- Get tasks filtered by user ID and status
- Apply sorting (creation date, title, priority)
- Handle pagination logic
- Validate user ownership
- Transform entities to DTOs
- Error handling and logging

**Dependencies:**
- `TaskRepository` (data access)
- `SecurityContextHolder` (current user)
- Logger

### 4.3 TaskRepository (Data Access Layer)

**Responsibility:** CRUD operations, database queries, data persistence

**Interface Methods:**
```
findByUserId(Long userId, Pageable pageable)
findByUserIdAndStatus(Long userId, String status, Pageable pageable)
findById(Long id)
save(Task task)
delete(Long id)
```

**Implementation:** Spring Data JPA generates SQL automatically

### 4.4 Task Entity (Domain Model)

**Responsibility:** Represent task data structure, persist to database

**Fields:**
- `id` (Primary Key, Long)
- `title` (String, 255 chars)
- `description` (String, 2000 chars)
- `userId` (Long, Foreign Key)
- `status` (String, enum: NEW, IN_PROGRESS, COMPLETED, BLOCKED)
- `priority` (String, enum: LOW, MEDIUM, HIGH, CRITICAL)
- `dueDate` (LocalDate)
- `createdAt` (LocalDateTime, auto-populated)
- `updatedAt` (LocalDateTime, auto-updated)

**JPA Annotations:**
- `@Entity`: Maps to database table
- `@Table(name = "tasks")`: Table name
- `@Id`: Primary key
- `@GeneratedValue`: Auto-increment strategy
- `@Column`: Field constraints and mappings
- `@CreationTimestamp`: Auto-populate creation date
- `@UpdateTimestamp`: Auto-populate update date

### 4.5 TaskDTO (Data Transfer Object)

**Responsibility:** API contract for request/response, decouple entity from API

**Fields (Response):**
```
{
  id: Long,
  title: String,
  description: String,
  status: String,
  priority: String,
  dueDate: String (ISO-8601),
  createdAt: String (ISO-8601),
  updatedAt: String (ISO-8601),
  userId: Long
}
```

**Rationale:** DTOs prevent exposing internal entity structure, allow API evolution independently

### 4.6 Spring Security Configuration

**Responsibility:** Authentication, authorization, token validation

**Components:**
- **JWT Token Validation:** Bearer token extraction and validation
- **UserPrincipal:** Custom principal with user metadata
- **SecurityContextHolder:** Store authenticated user during request
- **AuthenticationFilter:** Intercept requests, validate tokens
- **@PostAuthorize:** Method-level access control

**Mock Users (Development):**
```
User 1: username=user1, password=password1, roles=[TASK_VIEWER]
User 2: username=user2, password=password2, roles=[TASK_VIEWER]
User 3: username=admin, password=admin123, roles=[TASK_VIEWER, ADMIN]
```

---

## 5. Database Schema (H2 Embedded)

### DDL Definition

```sql
CREATE TABLE tasks (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  description VARCHAR(2000),
  user_id BIGINT NOT NULL,
  status VARCHAR(50) DEFAULT 'NEW',
  priority VARCHAR(50) DEFAULT 'MEDIUM',
  due_date DATE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) UNIQUE NOT NULL,
  email VARCHAR(100) UNIQUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tasks_user_id ON tasks(user_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_user_status ON tasks(user_id, status);
CREATE INDEX idx_tasks_created_at ON tasks(created_at DESC);
```

### Schema Documentation

| Table | Column | Type | Constraints | Purpose |
|-------|--------|------|-------------|---------|
| **tasks** | id | BIGINT | PK, AUTO_INCREMENT | Unique task identifier |
| | title | VARCHAR(255) | NOT NULL | Task name/title |
| | description | VARCHAR(2000) | NULLABLE | Detailed task description |
| | user_id | BIGINT | FK, NOT NULL | Owner of task |
| | status | VARCHAR(50) | DEFAULT 'NEW' | Task state (NEW, IN_PROGRESS, COMPLETED, BLOCKED) |
| | priority | VARCHAR(50) | DEFAULT 'MEDIUM' | Task urgency (LOW, MEDIUM, HIGH, CRITICAL) |
| | due_date | DATE | NULLABLE | Task deadline |
| | created_at | TIMESTAMP | DEFAULT NOW | Audit: creation time |
| | updated_at | TIMESTAMP | DEFAULT NOW | Audit: last modified time |

### Indexing Strategy

```
Composite Index (user_id, status):
  • Query: Find all tasks for user with specific status
  • Performance: O(log n) lookup instead of full table scan
  
Single Index (created_at DESC):
  • Query: Order by creation date (recent tasks first)
  • Performance: Avoids sorting large result sets in memory
```

### H2 Configuration (application.properties)

```properties
# H2 Database
spring.datasource.url=jdbc:h2:mem:taskdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 Console (development only)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
```

### Initialization Data (data.sql)

```sql
-- Create default users
INSERT INTO users (username, email) VALUES ('user1', 'user1@example.com');
INSERT INTO users (username, email) VALUES ('user2', 'user2@example.com');

-- Seed tasks for user1
INSERT INTO tasks (title, description, user_id, status, priority, due_date)
VALUES
('Implement Task List API', 'Create REST endpoint for retrieving user tasks', 1, 'IN_PROGRESS', 'HIGH', '2026-05-15'),
('Write Unit Tests', 'Add comprehensive test coverage for TaskService', 1, 'NEW', 'MEDIUM', '2026-05-20'),
('Database Design Review', 'Validate H2 schema and indexing strategy', 1, 'COMPLETED', 'HIGH', '2026-04-28');

-- Seed tasks for user2
INSERT INTO tasks (title, description, user_id, status, priority, due_date)
VALUES
('Review Architecture Document', 'Approve ADR for EPMICMPCOD-300', 2, 'IN_PROGRESS', 'HIGH', '2026-05-01'),
('Integration Testing', 'Set up Spring Boot Test suite', 2, 'NEW', 'MEDIUM', '2026-05-10');
```

---

## 6. API Contract

### Endpoint: GET /api/v1/tasks

**Purpose:** Retrieve paginated list of tasks for authenticated user

**HTTP Method:** GET  
**Authentication:** Required (Bearer Token)  
**Authorization:** ROLE_TASK_VIEWER minimum  

### Request

```
GET /api/v1/tasks?page=0&size=20&status=IN_PROGRESS&sort=createdAt,desc
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | Integer | No | 0 | Zero-indexed page number |
| `size` | Integer | No | 20 | Records per page (max 100) |
| `status` | String | No | All | Filter by status (NEW, IN_PROGRESS, COMPLETED, BLOCKED) |
| `sort` | String | No | createdAt,desc | Sorting criteria (field,asc\|desc) |

### Response (200 OK)

```json
{
  "content": [
    {
      "id": 1,
      "title": "Implement Task List API",
      "description": "Create REST endpoint for retrieving user tasks",
      "status": "IN_PROGRESS",
      "priority": "HIGH",
      "dueDate": "2026-05-15",
      "createdAt": "2026-04-28T10:30:00Z",
      "updatedAt": "2026-04-28T14:22:00Z",
      "userId": 1
    },
    {
      "id": 2,
      "title": "Write Unit Tests",
      "description": "Add comprehensive test coverage for TaskService",
      "status": "NEW",
      "priority": "MEDIUM",
      "dueDate": "2026-05-20",
      "createdAt": "2026-04-28T11:00:00Z",
      "updatedAt": "2026-04-28T11:00:00Z",
      "userId": 1
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 2,
    "totalPages": 1
  },
  "timestamp": "2026-04-29T08:15:30Z",
  "status": "success"
}
```

### Error Responses

**401 Unauthorized**
```json
{
  "error": "UNAUTHORIZED",
  "message": "Missing or invalid authorization token",
  "timestamp": "2026-04-29T08:15:30Z",
  "path": "/api/v1/tasks"
}
```

**403 Forbidden**
```json
{
  "error": "FORBIDDEN",
  "message": "User does not have permission to view tasks",
  "timestamp": "2026-04-29T08:15:30Z",
  "path": "/api/v1/tasks"
}
```

**400 Bad Request**
```json
{
  "error": "BAD_REQUEST",
  "message": "Invalid pagination parameters: page must be >= 0",
  "timestamp": "2026-04-29T08:15:30Z",
  "path": "/api/v1/tasks",
  "details": {
    "page": "must be greater than or equal to 0"
  }
}
```

**500 Internal Server Error**
```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred",
  "timestamp": "2026-04-29T08:15:30Z",
  "path": "/api/v1/tasks",
  "traceId": "abc123def456"
}
```

---

## 7. Deployment Artifact

### Build Artifact: Executable JAR

**File:** `ExecutionEngine-service-1.0.0.jar`  
**Size:** ~45 MB (includes embedded Tomcat, Spring Boot, dependencies)  
**Packaging:** Spring Boot executable JAR (fat JAR with nested JARs)  

### Run Instructions

```bash
# Prerequisites
- Java 21 installed (java -version)
- Maven 3.9+ installed (mvn -version)

# Build from source
mvn clean package

# Run the application
java -jar target/ExecutionEngine-service-1.0.0.jar

# Application starts on
http://localhost:8080

# Access points
- REST API: http://localhost:8080/api/v1/tasks
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs
- H2 Console: http://localhost:8080/h2-console
```

### Environment Variables (Optional)

```bash
# Server configuration
SERVER_PORT=8080
SERVER_SERVLET_CONTEXT_PATH=/

# Database configuration
SPRING_DATASOURCE_URL=jdbc:h2:mem:taskdb
SPRING_DATASOURCE_USERNAME=sa

# Logging level
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_EXAMPLE_EXECUTIONENGINE=DEBUG

# H2 Console
SPRING_H2_CONSOLE_ENABLED=true

# Example: Run on custom port
java -Dserver.port=9090 -jar ExecutionEngine-service-1.0.0.jar
```

### Access Endpoints

| Endpoint | Purpose | Authentication |
|----------|---------|-----------------|
| `GET /api/v1/tasks` | Retrieve task list | Required |
| `POST /swagger-ui.html` | API documentation | Optional |
| `GET /h2-console` | Database browser | Optional (dev only) |
| `GET /actuator/health` | Health check | Optional |

---

## 8. Performance Considerations

### H2 Database Mode: In-Memory vs File-Based

**Chosen: In-Memory (`jdbc:h2:mem:taskdb`)**

| Aspect | In-Memory | File-Based |
|--------|-----------|-----------|
| Speed | Ultra-fast (~10x faster) | Slower due to disk I/O |
| Persistence | Lost on shutdown | Survives restarts |
| Use Case | Development, testing | Production-like testing |
| Recommendation | ✅ Development (selected) | Future production |

**Rationale:** For MVP development, fast iteration and zero-setup justify losing persistence on restart.

### Query Optimization

**Indexing Strategy:**
```
Composite Index (user_id, status):
  • Allows efficient filtering by both columns
  • Query: WHERE user_id = ? AND status = ? uses index
  • Estimated improvement: 100x faster on 10K records

Single Index (created_at DESC):
  • Enables fast sorting by creation date
  • Avoids in-memory sorting for large result sets
```

**Query Examples (Optimized):**
```sql
-- Optimized: Uses composite index
SELECT * FROM tasks 
WHERE user_id = 1 AND status = 'IN_PROGRESS'
ORDER BY created_at DESC
LIMIT 20 OFFSET 0;

-- Optimized: Uses index on user_id
SELECT COUNT(*) FROM tasks WHERE user_id = 1;
```

### Connection Pooling (HikariCP)

```properties
# HikariCP Default Configuration (Spring Boot 3.4.5)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
```

**Impact:**
- **Connection Reuse:** Reduces connection overhead
- **Thread Safety:** HikariCP manages concurrent access
- **Performance:** Minimizes connection creation latency

### Scalability Constraints (MVP Phase)

| Constraint | Value | Mitigation Path |
|-----------|-------|-----------------|
| **Memory** | ~500 MB (in-memory DB) | Phase 4: Switch to PostgreSQL |
| **Concurrent Users** | ~50 (single JVM) | Phase 2: Horizontal scaling with load balancer |
| **Query Latency** | <10ms (avg) | Phase 3: Redis caching layer |
| **Task Dataset** | ~10,000 records | Phase 4: Partitioning, archival |

### Performance Monitoring

```properties
# Enable metrics
management.endpoints.web.exposure.include=health,metrics,prometheus
management.metrics.export.prometheus.enabled=true

# Access metrics
GET http://localhost:8080/actuator/metrics
GET http://localhost:8080/actuator/prometheus
```

---

## 9. Security Considerations

### Authentication Flow

```
1. Client sends HTTP request with Bearer token
   GET /api/v1/tasks
   Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGc...

2. Spring Security Filter interceptepts request
   • Extracts token from Authorization header
   • Validates token signature and expiration
   • Creates UserPrincipal from token claims

3. SecurityContext populated
   • User ID and roles stored in context
   • Available via SecurityContextHolder.getContext()

4. Controller method invoked
   • @PostAuthorize checks authorization
   • Ensures user owns task before returning data

5. Response sent to client
   • Only authorized data included
```

### Spring Security Configuration

**Bean: SecurityFilterChain**
```
• Disable CSRF protection (stateless API)
• Require HTTPS for production
• Add JWT authentication filter
• Configure authorization rules
```

**Stateless Design:**
- No session cookies
- No server-side session storage
- Each request validated independently
- Suitable for distributed systems

### Authorization Model

**@PostAuthorize** (Method-Level Authorization)
```
@PostAuthorize("returnObject.stream().allMatch(task -> task.userId == #id)")
public List<TaskDTO> getTasksByUser(Long id) { ... }

// Ensures returned tasks belong to authenticated user
```

**Role-Based Access Control (RBAC)**
```
ROLE_TASK_VIEWER: Can read own tasks
ROLE_ADMIN: Can read all tasks, modify any task
```

### Data Protection

**Password Encoding (BCrypt)**
```
Mock users in development:
user1 password → bcrypt: $2a$10$SlV3r2z...
admin password → bcrypt: $2a$10$kJ2x5m...

Default rounds: 10 (configurable)
```

**JWT Token Claims**
```json
{
  "sub": "user1",
  "userId": 1,
  "roles": ["ROLE_TASK_VIEWER"],
  "iat": 1682692533,
  "exp": 1682696133,
  "iss": "ExecutionEngine"
}
```

### CSRF Protection

**Disabled for API** (justified for stateless REST):
```
Rationale:
• Stateless API using Bearer tokens (not cookies)
• CSRF tokens unnecessary for token-based auth
• Enable CSRF for future web UI (forms)
```

### Security Headers (Recommended)

```properties
spring.web.resources.cache.period=0
# Add headers via SecurityFilterChain bean
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
```

---

## 10. Testing Strategy

### Test Pyramid

```
        ▲
       / \
      /   \  End-to-End Tests (5%)
     /     \
    /-------\
   /         \  Integration Tests (20%)
  /           \
 /             \
/---------------\
/                 \  Unit Tests (75%)
/                   \
```

### Unit Tests (@WebMvcTest, @DataJpaTest)

**TaskControllerTest**
- Mock TaskService
- Test HTTP status codes
- Validate response format
- Test error handling

**TaskServiceTest**
- Mock TaskRepository
- Test business logic (filtering, pagination)
- Test error scenarios
- Verify service calls repository correctly

**TaskRepositoryTest (@DataJpaTest)**
- Test JPA queries
- Validate index usage
- Test pagination
- Verify relationships

### Integration Tests (@SpringBootTest)

**Full Application Context**
- Test Spring Boot application startup
- Load all beans and configurations
- Use embedded H2 database
- Test REST endpoints end-to-end

**Test Data Fixtures**
```sql
-- test-data.sql loaded before each test
INSERT INTO users VALUES (1, 'testuser', 'test@example.com', NOW());
INSERT INTO tasks VALUES (1, 'Test Task', 'Description', 1, 'NEW', 'MEDIUM', NULL, NOW(), NOW());
```

### Mock Authentication (@WithMockUser)

```java
@WithMockUser(username = "user1", roles = {"TASK_VIEWER"}, id = "1")
public void testGetTasksAsAuthenticatedUser() {
    // User1 context is available during test
    // SecurityContextHolder populated automatically
}
```

### Test Coverage Goals

| Component | Target Coverage |
|-----------|-----------------|
| TaskService | ≥ 90% |
| TaskController | ≥ 85% |
| TaskRepository | ≥ 80% |
| Overall | ≥ 85% |

### Running Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=TaskServiceTest

# With coverage report
mvn test jacoco:report

# Integration tests only
mvn test -Dgroups=integration

# Generate coverage report
mvn clean verify
# Report at: target/site/jacoco/index.html
```

---

## 11. Future Enhancement Roadmap

### Phase 2: Redis Caching Layer (Q3 2026)

**Objective:** Reduce database load, improve response time

**Implementation:**
- Cache task list for 5 minutes
- Cache task detail for 10 minutes
- Invalidate cache on write operations
- Use Spring Data Redis

**Expected Impact:**
- Response time: 10ms → 2ms (for cached requests)
- Database load: -60%

### Phase 3: Event-Driven Architecture (Q4 2026)

**Objective:** Decouple components, enable async processing

**Implementation:**
- Apache Kafka for event streaming
- Events: TaskCreated, TaskUpdated, TaskCompleted
- Event handlers for notifications, auditing
- CQRS pattern for read/write separation

**Expected Impact:**
- System resilience improved
- Real-time event streaming
- Audit trail automatically maintained

### Phase 4: Distributed Database (Q1 2027)

**Objective:** Enable production scaling, data persistence

**Implementation:**
- Migrate from H2 to PostgreSQL
- Multi-region replication
- Connection pooling optimization
- Automated backups and recovery

**Expected Impact:**
- Unlimited data scaling
- Data durability guaranteed
- Multi-node resilience

### Phase 5: Advanced Features (Q2 2027)

**Objective:** Enhance user experience and system intelligence

**Implementation:**
- Elasticsearch for full-text search
- Task recommendations via ML
- GraphQL API for flexible queries
- WebSockets for real-time updates
- Task subtasks and dependencies

---

## 12. Assumptions & Constraints

### Assumptions

1. **Single Instance Deployment:** MVP runs on one server (no clustering)
2. **Embedded Database Sufficient:** H2 in-memory handles MVP data volume
3. **Development Environment:** No external auth service required
4. **Stateless API:** REST API with Bearer tokens (not session-based)
5. **Mock Users Acceptable:** Development uses hardcoded users (future: external auth)
6. **Low Concurrent Users:** <50 concurrent users acceptable for MVP

### Constraints

| Constraint | Value | Reason | Mitigation |
|-----------|-------|--------|-----------|
| **Memory Usage** | ~500 MB max | In-memory H2 database | Phase 4: PostgreSQL |
| **Concurrent Connections** | ~50 | Single JVM limit | Phase 2: Horizontal scaling |
| **Task Dataset Size** | ~10,000 records | Memory constraint | Phase 4: Partitioning, archival |
| **Query Latency** | <100ms (p99) | Network + DB latency | Phase 3: Redis caching |
| **No Data Persistence** | On restart | In-memory database | Phase 4: File-based or external DB |

### Mitigation Strategies

**For Memory Constraint:**
- Monitor H2 memory usage via actuator metrics
- Implement data archival for old tasks
- Switch to file-based H2 if necessary

**For Concurrent User Limit:**
- Design stateless API for horizontal scaling
- Use load balancer when scaling
- Monitor connection pool utilization

**For Data Loss (No Persistence):**
- Document behavior clearly
- Implement optional file-based H2 (via config)
- Add data export capability in Phase 2

---

## 13. Decision Rationale

### Why H2 Embedded Instead of External PostgreSQL?

**Rationale:**
1. **Zero Setup:** Developers run app immediately, no Docker, no external servers
2. **Fast Development:** No network latency, instant database startup
3. **Cost Effective:** No infrastructure costs for MVP
4. **Sufficient for MVP:** 10K records fit in memory
5. **Path to Production:** Easy migration path to PostgreSQL in Phase 4

**Trade-offs:**
- ❌ Data lost on restart (acceptable for dev)
- ❌ Limited to single instance (acceptable for MVP)
- ✅ Ultra-fast (10x faster than PostgreSQL)
- ✅ No configuration required

### Why Spring Security Instead of External Auth Service?

**Rationale:**
1. **Zero External Dependencies:** No OAuth server, no identity provider required
2. **Full Control:** Mock users easily for testing
3. **Industry Standard:** Production-proven security framework
4. **Framework Integration:** Seamless integration with Spring Boot
5. **Clear Migration Path:** Easy to swap with Keycloak/Auth0 later

**Trade-offs:**
- ❌ Hardcoded users (dev only, not production-grade)
- ✅ Immediate development velocity
- ✅ Comprehensive security features (encryption, filters)

### Why Layered Architecture Over Alternatives?

**Alternatives Considered:**

| Architecture | Pros | Cons | Verdict |
|---|---|---|---|
| **Layered (Selected)** | Clear separation, testable, maintainable | More files | ✅ Best for MVP + scalability |
| **Hexagonal** | Flexible, highly decoupled | Over-engineered for MVP | ❌ Too complex |
| **CQRS** | Scalable, event-driven | Complex, overkill for MVP | ❌ Future phase |
| **Microservices** | Independent scaling | Distributed complexity | ❌ Too early |

**Rationale for Layered:**
- Clear responsibility separation (Controller → Service → Repository)
- Easy to test each layer independently
- Natural progression path (monolith → microservices)
- Minimal overhead, maximum clarity

### Benefits vs. Trade-offs Summary

**Benefits:**
✅ Zero configuration, immediate productivity  
✅ Self-contained, no external services  
✅ Fast iteration for development  
✅ Comprehensive test coverage  
✅ Production-grade architecture patterns  
✅ Clear migration path to distributed systems  

**Trade-offs:**
❌ Single instance (scaling requires Phase 2)  
❌ No data persistence (acceptable for MVP)  
❌ Limited to 50 concurrent users (acceptable for MVP)  
❌ Mock authentication (external auth in Phase 2)  

---

## 14. Sign-Off & Approval

**Architecture Decision:** APPROVED ✅

### Decision Information

| Field | Value |
|-------|-------|
| **Decision** | Self-contained Spring Boot 3.4.5 with embedded H2 database |
| **Status** | ✅ APPROVED |
| **Decision Date** | 2026-04-29 |
| **Ticket ID** | EPMICMPCOD-300 |
| **Feature** | View Task List |

### Approvers

| Role | Name/Agent | Approved | Date |
|------|-----------|----------|------|
| **Architecture Lead** | Architecture Design Agent | ✅ Yes | 2026-04-29 |
| **Review Authority** | Orchestrator Ticket Agent | ✅ Yes | 2026-04-29 |
| **Implementation Owner** | Backend Implementation Agent (Pending) | ⏳ Pending | TBD |

### Next Steps

1. ✅ **Architecture Document:** Complete (this document)
2. ⏳ **Implementation Phase:** Backend Implementation Agent creates project structure, implements classes
3. ⏳ **Code Review:** Peer review of implementation against ADR
4. ⏳ **Testing Phase:** Comprehensive unit and integration tests
5. ⏳ **Quality Assurance:** QA testing, security review
6. ⏳ **Deployment:** Package as executable JAR, deploy to staging

### Handoff Information

This ADR is complete and ready for **Backend Implementation Agent** to begin coding implementation. The document provides:

- ✅ Complete system architecture
- ✅ Component responsibilities and interactions
- ✅ Database schema with DDL
- ✅ API contract with examples
- ✅ Security design and implementation details
- ✅ Testing strategy and structure
- ✅ Performance considerations and constraints
- ✅ Future scaling roadmap

**Implementation Ready:** Yes ✅  
**Technical Clarity:** Comprehensive  
**Approval Status:** APPROVED  
**Document Version:** v1.0  

---

## Appendix A: Technology Stack Versions

```
Java: 21 (LTS)
Spring Boot: 3.4.5
Spring Framework: 6.1.x
Spring Data JPA: 3.4.5
Spring Security: 3.4.5
Hibernate ORM: 6.4.x
H2 Database: 2.x
JUnit: 5.10.x
Mockito: 5.x
Maven: 3.9+
```

---

## Appendix B: File Structure

```
ExecutionEngine-service/
├── src/
│   ├── main/
│   │   ├── java/com/example/executionengine/
│   │   │   ├── controller/
│   │   │   │   └── TaskController.java
│   │   │   ├── service/
│   │   │   │   └── TaskService.java
│   │   │   ├── repository/
│   │   │   │   └── TaskRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── Task.java
│   │   │   │   └── User.java
│   │   │   ├── dto/
│   │   │   │   └── TaskDTO.java
│   │   │   ├── security/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── UserPrincipal.java
│   │   │   └── ExecutionEngineServiceApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── data.sql
│   │       └── schema.sql
│   └── test/
│       ├── java/com/example/executionengine/
│       │   ├── controller/
│       │   │   └── TaskControllerTest.java
│       │   ├── service/
│       │   │   └── TaskServiceTest.java
│       │   ├── repository/
│       │   │   └── TaskRepositoryTest.java
│       │   └── integration/
│       │       └── TaskListIntegrationTest.java
│       └── resources/
│           ├── application-test.properties
│           └── test-data.sql
├── pom.xml
├── Dockerfile (Phase 2)
└── README.md
```

---

**End of Architecture Decision Record**

*This document represents the approved system architecture for EPMICMPCOD-300. All implementation must adhere to this design.*

---

**Document Control**
- **Version:** v1.0
- **Date Created:** 2026-04-29
- **Status:** APPROVED ✅
- **Ticket:** EPMICMPCOD-300
- **Last Updated:** 2026-04-29
