-- Sample task data for EPMICMPCOD-300: View Task List
-- Initialized on application startup via spring.sql.init.mode=always

INSERT INTO tasks (id, title, description, user_id, status, created_at, updated_at)
VALUES
    ('550e8400-e29b-41d4-a716-446655440000', 'Fix login bug', 'OAuth token expiration issue in authentication flow', 'user1', 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440001', 'Add unit tests', 'Complete test coverage for TaskService layer', 'user1', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440002', 'Deploy to staging', 'Release v1.0 to staging environment', 'user2', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440003', 'Update documentation', 'Update API documentation and deployment guides', 'user2', 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
