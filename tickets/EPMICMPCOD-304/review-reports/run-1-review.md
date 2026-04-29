# Code Review Report — Run 1
**Ticket:** EPMICMPCOD-304 — Delete a Task  
**Date:** 2026-04-29  
**Reviewer:** Code Review Agent

---

## Verdict
REQUEST_CHANGES

---

## Summary
The implementation is functionally correct: the DELETE endpoint returns 204/404 as specified, constructor injection is used throughout, and all 41 tests pass with coverage above thresholds. However, two HIGH findings block merge — the H2 web console is enabled in the main application config (security risk if deployed without a profile override) and JPA entities are returned directly from controller endpoints instead of response DTOs (information leakage + SRP violation). Five MEDIUM issues should be addressed soon.

---

## Findings

### CRITICAL
None.

---

### HIGH

#### H1. H2 console enabled in main application.properties
- **File:** `src/main/resources/application.properties:17-18`
- **Category:** Security — Exposed Admin Interface (OWASP A05: Security Misconfiguration)
- **Problem:** `spring.h2.console.enabled=true` is set in `src/main/resources/application.properties`, not in a test-only or dev profile. The datasource password is also blank (`spring.datasource.password=`). Any environment that loads this config without an explicit override will expose `/h2-console` — a full web SQL interface — with no authentication beyond the blank credential.
- **Impact:** In any staging or production deployment that forgets a profile override, an attacker can browse, query, or wipe the entire in-memory database through a browser. Even for dev-only use, the correct pattern is to restrict this to a `dev` or `test` Spring profile.
- **Suggested fix:**
  ```properties
  # src/main/resources/application.properties — remove or set to false
  spring.h2.console.enabled=false

  # src/main/resources/application-dev.properties (create) — safe for local only
  spring.h2.console.enabled=true
  spring.h2.console.path=/h2-console
  ```
- **Standard:** OWASP A05 — Security Misconfiguration.

---

#### H2. JPA entity returned directly from controller endpoints
- **File:** `src/main/java/com/example/task/controller/TaskController.java:28,35,40,46`
- **Category:** Design — SRP / Information Leakage
- **Problem:** All controller methods return `Task` (the JPA entity) directly as the HTTP response body. The entity carries JPA annotations, lifecycle timestamps, and internal column mappings as part of the public API contract.
- **Impact:** (1) Any column added to the entity is immediately published in the API without a versioning decision. (2) Jackson serialises all fields including `createdAt`, `updatedAt`, `completedAt` — if any future field is sensitive, it leaks automatically. (3) The API contract is rigidly coupled to the DB schema. (4) Changing the entity shape is a breaking API change.
- **Suggested fix:** Introduce a `TaskResponse` DTO with only the fields the API contract needs, and map `Task → TaskResponse` in the service or a mapper utility.
  ```java
  // controller — before
  public ResponseEntity<Task> getTask(@PathVariable Long taskId) { ... }

  // controller — after
  public ResponseEntity<TaskResponse> getTask(@PathVariable Long taskId) { ... }
  ```

---

### MEDIUM

#### M1. `completedAt` is never cleared when a task reverts from COMPLETED
- **File:** `src/main/java/com/example/task/service/TaskServiceImpl.java:62-65`
- **Category:** Correctness — stale data
- **Problem:** When `status == COMPLETED`, `completedAt` is set to `now()`. But if the status is subsequently changed back to `IN_PROGRESS` or `PENDING`, `completedAt` is never nulled out. A task that was briefly completed and then re-opened will perpetually report a stale completion timestamp.
- **Suggested fix:**
  ```java
  if (request.getStatus() != null) {
      task.setStatus(request.getStatus());
      if (request.getStatus() == TaskStatus.COMPLETED) {
          task.setCompletedAt(LocalDateTime.now());
      } else {
          task.setCompletedAt(null);   // clear on revert
      }
  }
  ```

---

#### M2. Blank title in `UpdateTaskRequest` is silently ignored
- **File:** `src/main/java/com/example/task/service/TaskServiceImpl.java:57`
- **Category:** Correctness — silent no-op
- **Problem:** `if (request.getTitle() != null && !request.getTitle().isBlank())` silently drops a blank title without error. A client that sends `"title": "   "` receives HTTP 200 with the old title unchanged and no indication that the update was rejected.
- **Suggested fix:** Either add `@NotBlank` to `UpdateTaskRequest.title` when it is present (use a custom constraint like `@NullOrNotBlank`) or throw `InvalidTaskException("Title must not be blank")` inside the guard.

---

#### M3. `GlobalExceptionHandler` returns only the first validation error
- **File:** `src/main/java/com/example/task/exception/GlobalExceptionHandler.java:37`
- **Category:** API usability — incomplete error feedback
- **Problem:** `ex.getBindingResult().getFieldErrors().stream().findFirst()` discards all but the first constraint violation. A client submitting a request with multiple invalid fields must fix them one at a time.
- **Suggested fix:**
  ```java
  String message = ex.getBindingResult().getFieldErrors().stream()
          .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
          .collect(Collectors.joining("; "));
  ```

---

#### M4. `LocalDateTime` used for timestamps — no timezone context
- **File:** `src/main/java/com/example/task/entity/Task.java:33,36,39`, `src/main/java/com/example/task/exception/ErrorResponse.java:9`
- **Category:** Design — ambiguous temporal data
- **Problem:** `LocalDateTime` has no timezone offset. Any consumer outside the JVM's default timezone will misinterpret the values. This affects `createdAt`, `updatedAt`, `completedAt` in the entity, and `timestamp` in `ErrorResponse`.
- **Suggested fix:** Use `Instant` (stored as `TIMESTAMP WITH TIME ZONE`) or `OffsetDateTime` for all temporal API fields.

---

#### M5. `spring.jpa.show-sql=true` in main application config
- **File:** `src/main/resources/application.properties:12`
- **Category:** Security — sensitive data in logs (OWASP A09: Security Logging)
- **Problem:** `show-sql=true` logs every SQL statement to standard output. In production this logs query parameters that could include PII or confidential task data.
- **Suggested fix:** Move to `application-dev.properties` only; set `spring.jpa.show-sql=false` in main config.

---

### LOW

#### L1. `@Autowired` on single-constructor classes is redundant
- **File:** `src/main/java/com/example/task/service/TaskServiceImpl.java:22`, `src/main/java/com/example/task/controller/TaskController.java:20`
- **Category:** Code smell — unnecessary annotation
- **Problem:** Since Spring 4.3, `@Autowired` is implicit when a class has exactly one constructor. The annotation adds noise without effect.

---

#### L2. `@Repository` redundant on Spring Data JPA interface
- **File:** `src/main/java/com/example/task/repository/TaskRepository.java:10`
- **Category:** Code smell — redundant stereotype
- **Problem:** Spring Data JPA automatically registers interfaces extending `JpaRepository` as beans. `@Repository` is unnecessary and misleading (it implies a concrete class).

---

#### L3. Public `setCreatedAt` setter on an `updatable = false` column
- **File:** `src/main/java/com/example/task/entity/Task.java:62-63`
- **Category:** Design — misleading mutability
- **Problem:** `Task` exposes a public `setCreatedAt(LocalDateTime)` setter, but `@Column(updatable = false)` ensures that value is never written to the DB after the initial insert. Calling the setter post-persist silently has no effect, which is confusing.
- **Suggested fix:** Remove `setCreatedAt` or make it package-private.

---

#### L4. DELETE idempotency — 404 on second call
- **File:** `src/main/java/com/example/task/service/TaskServiceImpl.java:79-82`
- **Category:** HTTP semantics — debatable but notable
- **Problem:** HTTP DELETE is defined as idempotent (RFC 7231). Calling `DELETE /api/v1/tasks/{id}` twice returns 204 then 404. This is technically non-idempotent in terms of status code, though the server state is the same. Some clients (retry logic, load balancers) may treat the 404 as an error.
- **Note:** This is a design decision. Returning 204 even when the resource is already gone (treating "not found" as "already deleted") is equally valid and more RESTful.

---

## Coverage Notes
*(Based on `tickets/EPMICMPCOD-304/test-reports/run-1-report.md`)*

- All coverage thresholds met: Controller 100%, Service 92.6% (≥90%), Overall 88.6% (≥85%).
- `TaskNotFoundException` at 50% — the `(String, Throwable)` constructor is untested. Acceptable for a trivial delegate, but worth a single test to close it.
- `Task` entity at 83.3% — likely missing tests for the `@PrePersist`/`@PreUpdate` hooks directly, and setter/getter paths not exercised via integration paths.
- `TaskApplication` at 33.3% is expected and correctly skipped per project skip rules.

---

## Recommendation

Two items must be fixed before merge:

1. **H1 — Move H2 console config to a `dev` profile only.** This is a one-line fix to `application.properties` + creation of `application-dev.properties`.
2. **H2 — Introduce a `TaskResponse` DTO** so the entity is not serialised directly into HTTP responses. Even a simple record mirroring current fields prevents future accidental leakage.

MEDIUM findings M1 (stale `completedAt`) and M2 (silent blank title) should also be addressed in this PR since they are correctness issues in the same service methods already being touched. M3, M4, M5 can follow in the next sprint.
