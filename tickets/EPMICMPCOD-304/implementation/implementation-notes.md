# Implementation Notes — EPMICMPCOD-304: Delete a Task

**Generated:** 2026-04-29T00:10:00Z  
**Branch:** EXE-304/delete-a-task

---

## Summary

Full Spring Boot 3.4.5 / Java 21 project scaffold created with the DELETE
task endpoint as the primary deliverable. All layers follow the established
EPMICMPCOD-299 conventions.

## Key Design Decisions

1. **Constructor injection** used throughout (field `@Autowired` replaced with
   constructor injection) — aligns with SOLID / testability best practices.

2. **Hard delete** implemented per ticket requirement — no soft-delete, no
   `deletedAt` column.

3. **`deleteTask` flow**: `findById` → throw `TaskNotFoundException` if absent
   → `repository.delete(task)` — the entity is fetched first to ensure a
   meaningful 404 rather than a silent no-op.

4. **`GlobalExceptionHandler`** handles `TaskNotFoundException` → 404,
   `InvalidTaskException` → 400, `MethodArgumentNotValidException` → 400
   (field validation), and generic `Exception` → 500 (safe message only,
   no stack trace exposure).

5. **H2 in-memory DB** used for dev/test consistency with prior tickets.
   `spring.jpa.hibernate.ddl-auto=create-drop` auto-creates the `tasks` table
   on startup.

## Build Verification

```
mvn compile → EXIT 0 ✅
```

## Notes for Unit Test Agent

- `TaskServiceImpl.deleteTask(Long)` has two paths: happy path (task found →
  deleted) and exception path (task not found → `TaskNotFoundException`).
- `TaskController.deleteTask` should be tested for 204 and 404 responses.
- `GlobalExceptionHandler` exception mapping can be covered via
  `@WebMvcTest` slice tests.
