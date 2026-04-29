# Files Changed — EPMICMPCOD-304: Delete a Task

**Generated:** 2026-04-29T00:10:00Z  
**Branch:** EXE-304/delete-a-task  
**Build Status:** ✅ PASSED (`mvn compile` exit 0)

---

## Files Created

| # | File | Type | Description |
|---|---|---|---|
| 1 | `pom.xml` | Build | Spring Boot 3.4.5, Java 21, JPA, H2, Validation, Test, JaCoCo |
| 2 | `src/main/resources/application.properties` | Config | H2 datasource, JPA DDL-auto, H2 console |
| 3 | `src/main/java/com/example/task/TaskApplication.java` | Entrypoint | `@SpringBootApplication` bootstrap |
| 4 | `src/main/java/com/example/task/entity/Task.java` | Entity | JPA entity mapped to `tasks` table |
| 5 | `src/main/java/com/example/task/entity/TaskStatus.java` | Enum | PENDING, IN_PROGRESS, COMPLETED, CANCELLED |
| 6 | `src/main/java/com/example/task/entity/TaskPriority.java` | Enum | LOW, MEDIUM, HIGH |
| 7 | `src/main/java/com/example/task/repository/TaskRepository.java` | Repository | `JpaRepository<Task, Long>` |
| 8 | `src/main/java/com/example/task/dto/CreateTaskRequest.java` | DTO | Create request with validation |
| 9 | `src/main/java/com/example/task/dto/UpdateTaskRequest.java` | DTO | Update request |
| 10 | `src/main/java/com/example/task/service/TaskService.java` | Interface | Service contract incl. `deleteTask(Long)` |
| 11 | `src/main/java/com/example/task/service/TaskServiceImpl.java` | Service | All CRUD ops incl. `deleteTask` |
| 12 | `src/main/java/com/example/task/controller/TaskController.java` | Controller | REST endpoints incl. `DELETE /api/v1/tasks/{taskId}` |
| 13 | `src/main/java/com/example/task/exception/TaskNotFoundException.java` | Exception | 404 trigger |
| 14 | `src/main/java/com/example/task/exception/InvalidTaskException.java` | Exception | 400 trigger |
| 15 | `src/main/java/com/example/task/exception/ErrorResponse.java` | DTO | Structured error payload |
| 16 | `src/main/java/com/example/task/exception/GlobalExceptionHandler.java` | Handler | `@RestControllerAdvice` — maps exceptions to HTTP responses |

## Files Modified

None — full scaffold creation.

## Dependencies Added

All managed by Spring Boot parent 3.4.5 — no version overrides needed.  
No new dependencies beyond what was established in EPMICMPCOD-299 conventions.

## Config Changes

| File | Change | Approval Status |
|---|---|---|
| `pom.xml` | Full scaffold (new file) | N/A — new project |
| `application.properties` | Full scaffold (new file) | N/A — new project |

## Primary Deliverable

`DELETE /api/v1/tasks/{taskId}` → `204 No Content` / `404 ErrorResponse`

---

## Deviations from Architecture

None.
