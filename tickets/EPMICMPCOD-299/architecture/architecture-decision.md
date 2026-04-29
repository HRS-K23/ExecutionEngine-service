# Architecture Decision — EPMICMPCOD-299

**Ticket:** Create Task  
**Date:** 2026-04-29  
**Status:** ✅ APPROVED  
**Version:** 1.0

---

## Overview

This document defines the architecture for the Task creation feature. The design prioritizes simplicity and minimal dependencies using vanilla Spring Framework with H2 in-memory database.

**Objective:** Create a lightweight task management REST service that allows users to create, read, update, and delete tasks.

---

## Technology Stack

| Component | Version | Rationale |
|---|---|---|
| **Java** | 21 | Target version per ticket |
| **SpringBoot** | 3.4.5 | Target version per ticket |
| **Database** | H2 (embedded) | No external DB required; in-memory for testing |
| **ORM** | Spring Data JPA | Built-in, minimal configuration |
| **Validation** | Spring Framework (@Valid) | No external library |
| **API Protocol** | REST (HTTP) | Standard web service pattern |
| **Serialization** | JSON (built-in Spring) | Default for Spring REST controllers |

---

## Domain Model

### Task Entity

```java
@Entity
@Table(name = "tasks")
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    @NotBlank(message = "Title is required")
    private String title;
    
    @Column(length = 2000)
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority = TaskPriority.MEDIUM;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

### Enumerations

```java
public enum TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}

public enum TaskPriority {
    LOW,
    MEDIUM,
    HIGH
}
```

---

## Database Schema (H2)

### Tasks Table

```sql
CREATE TABLE tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    completed_at TIMESTAMP
);

-- Indexes for common queries
CREATE INDEX idx_status ON tasks(status);
CREATE INDEX idx_created_at ON tasks(created_at);
```

### H2 Configuration

**File:** `application.properties`

```properties
# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:taskdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false

# H2 Console (dev/testing only)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

---

## API Design

### Base URL
```
/api/v1/tasks
```

### Endpoints

#### 1. Create Task
```
POST /api/v1/tasks
Content-Type: application/json

Request Body:
{
    "title": "Buy groceries",
    "description": "Milk, eggs, bread",
    "priority": "HIGH"
}

Response: 201 Created
{
    "id": 1,
    "title": "Buy groceries",
    "description": "Milk, eggs, bread",
    "status": "PENDING",
    "priority": "HIGH",
    "createdAt": "2026-04-29T06:15:00",
    "updatedAt": "2026-04-29T06:15:00",
    "completedAt": null
}
```

#### 2. List All Tasks (Paginated)
```
GET /api/v1/tasks?page=0&size=20&sort=createdAt,desc

Response: 200 OK
{
    "content": [
        { task object 1 },
        { task object 2 }
    ],
    "pageable": { ... },
    "totalElements": 50,
    "totalPages": 3,
    "number": 0,
    "size": 20
}
```

#### 3. Get Single Task
```
GET /api/v1/tasks/{taskId}

Response: 200 OK
{
    "id": 1,
    "title": "Buy groceries",
    ...
}

Response: 404 Not Found
{
    "error": "Task not found",
    "status": 404,
    "timestamp": "2026-04-29T06:15:00"
}
```

#### 4. Update Task
```
PUT /api/v1/tasks/{taskId}
Content-Type: application/json

Request Body:
{
    "title": "Buy groceries (updated)",
    "description": "Milk, eggs, bread, butter",
    "status": "IN_PROGRESS",
    "priority": "MEDIUM"
}

Response: 200 OK
{ updated task object }
```

#### 5. Delete Task
```
DELETE /api/v1/tasks/{taskId}

Response: 204 No Content
```

---

## Service Layer Architecture

### TaskService Interface

```java
public interface TaskService {
    Task createTask(CreateTaskRequest request);
    Task getTaskById(Long taskId);
    Page<Task> listTasks(Pageable pageable);
    Task updateTask(Long taskId, UpdateTaskRequest request);
    void deleteTask(Long taskId);
}
```

### TaskServiceImpl

```java
@Service
@Transactional
public class TaskServiceImpl implements TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Override
    public Task createTask(CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority() != null ? 
            request.getPriority() : TaskPriority.MEDIUM);
        return taskRepository.save(task);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException(
                "Task not found with id: " + taskId));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Task> listTasks(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }
    
    @Override
    public Task updateTask(Long taskId, UpdateTaskRequest request) {
        Task task = getTaskById(taskId);
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
            if (request.getStatus() == TaskStatus.COMPLETED) {
                task.setCompletedAt(LocalDateTime.now());
            }
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        return taskRepository.save(task);
    }
    
    @Override
    public void deleteTask(Long taskId) {
        Task task = getTaskById(taskId);
        taskRepository.delete(task);
    }
}
```

### TaskRepository

```java
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    Page<Task> findByPriority(TaskPriority priority, Pageable pageable);
}
```

---

## REST Controller

```java
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task task = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }
    
    @GetMapping
    public ResponseEntity<Page<Task>> listTasks(Pageable pageable) {
        Page<Task> tasks = taskService.listTasks(pageable);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTask(@PathVariable Long taskId) {
        Task task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(task);
    }
    
    @PutMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request) {
        Task task = taskService.updateTask(taskId, request);
        return ResponseEntity.ok(task);
    }
    
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Exception Handling

### Custom Exceptions

```java
public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}

public class InvalidTaskException extends RuntimeException {
    public InvalidTaskException(String message) {
        super(message);
    }
}
```

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFound(
            TaskNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
            "Task not found",
            HttpStatus.NOT_FOUND.value(),
            LocalDateTime.now(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        ErrorResponse error = new ErrorResponse(
            message,
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
            "Internal server error",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            LocalDateTime.now(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### Error Response DTO

```java
public class ErrorResponse {
    private String error;
    private int status;
    private LocalDateTime timestamp;
    private String path;
    
    // Constructor, getters
}
```

---

## Request/Response DTOs

### CreateTaskRequest

```java
public class CreateTaskRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @Size(max = 2000)
    private String description;
    
    private TaskPriority priority;
    
    // Getters and setters
}
```

### UpdateTaskRequest

```java
public class UpdateTaskRequest {
    
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    
    // Getters and setters (all optional for PATCH-like behavior)
}
```

---

## Dependencies

### pom.xml

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
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## Key Features & Design Decisions

✅ **Lightweight:** Minimal dependencies, vanilla Spring only  
✅ **Scalable:** Service layer abstraction allows easy DB swaps  
✅ **Validated:** Input validation at DTO level using Spring @Valid  
✅ **Transactional:** Service methods properly decorated with @Transactional  
✅ **RESTful:** Standard HTTP methods and status codes  
✅ **Error Handling:** Global exception handler with consistent error responses  
✅ **Pagination:** List endpoint supports Spring Data pagination  
✅ **Timestamps:** Automatic createdAt/updatedAt tracking  
✅ **H2 Embedded:** No external database needed for development/testing  

---

## Development & Testing

### Run Application
```bash
mvn spring-boot:run
```

### Access H2 Console (dev only)
```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:taskdb
```

### Example cURL Commands

```bash
# Create task
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Buy milk","priority":"HIGH"}'

# List tasks
curl http://localhost:8080/api/v1/tasks

# Get task by ID
curl http://localhost:8080/api/v1/tasks/1

# Update task
curl -X PUT http://localhost:8080/api/v1/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"status":"COMPLETED"}'

# Delete task
curl -X DELETE http://localhost:8080/api/v1/tasks/1
```

---

## Assumptions & Notes

⚠️ **Assumptions:**
- Single instance deployment (no distributed caching needed)
- H2 database suitable for dev/test; can be swapped with PostgreSQL/MySQL in production
- Synchronous API (no async processing required)
- No authentication/authorization in MVP (can be added in future iterations)
- No audit logging in MVP (can be added with Spring Data Envers)

✅ **Future Enhancements:**
- Task categories/tags
- Task assignments and permissions
- Recurring tasks
- Subtasks
- Email notifications
- Advanced filtering and search
- Batch operations

---

## Approval & Signature

| Role | Name | Date | Status |
|---|---|---|---|
| **Architect** | Orchestrator Agent | 2026-04-29 | ✅ APPROVED |
| **Human Review** | Devendra Kishor Mahajan | 2026-04-29 | ✅ APPROVED |

---

**Document Version:** 1.0  
**Last Updated:** 2026-04-29  
**Status:** 🟢 ACTIVE
