# Implementation Plan — EPMICMPCOD-258
## AI Services Module - Self-Contained Backend System

**Ticket:** EPMICMPCOD-258  
**Team:** EXE (Execution Engine)  
**Date:** 2026-04-28  
**Status:** Implementation Design (Ready for Approval)  
**Mode:** Self-Contained (No External AI Services Required)

---

## Table of Contents

1. [Technology Stack](#technology-stack)
2. [Module Structure](#module-structure)
3. [API Design](#api-design)
4. [Database Schema](#database-schema)
5. [Configuration Strategy](#configuration-strategy)
6. [Implementation Dependencies](#implementation-dependencies)
7. [Error Handling & Fallback](#error-handling--fallback)
8. [Deployment Plan](#deployment-plan)

---

## Technology Stack

### Core Backend

| Component | Technology | Rationale |
|-----------|-----------|-----------|
| **Language** | TypeScript | Type-safe, excellent for large-scale systems; native Node.js support |
| **Runtime** | Node.js 20 LTS | High-performance async I/O, excellent for message queue integration |
| **Framework** | Express.js | Lightweight, battle-tested, rich middleware ecosystem |
| **API Protocol** | REST + WebSocket | REST for standard operations; WebSocket for real-time updates |

### Data & Storage

| Component | Technology | Rationale |
|-----------|-----------|-----------|
| **Database** | PostgreSQL 15+ | ACID compliance, excellent JSON support, proven scalability |
| **ORM** | TypeORM | Type-safe ORM; supports migrations, repositories pattern |
| **Cache** | Redis 7+ | Sub-millisecond performance; critical for template caching |
| **Query Language** | SQL (TypeORM abstracts) | Efficient for complex queries on question metadata |

### Messaging & Async

| Component | Technology | Rationale |
|-----------|-----------|-----------|
| **Message Broker** | RabbitMQ 3.12+ | Reliable queue, dead-letter handling, perfect for async workflows |
| **Queue Client** | amqplib | Official RabbitMQ client; robust error handling |

### Testing & Quality

| Component | Technology | Rationale |
|-----------|-----------|-----------|
| **Unit Testing** | Jest 29+ | Zero-config TypeScript support, excellent coverage reporting |
| **Integration Testing** | SuperTest + Jest | HTTP request simulation for API testing |
| **Database Testing** | testcontainers-node | Isolated PostgreSQL for each test run |

### Development & Deployment

| Component | Technology | Rationale |
|-----------|-----------|-----------|
| **Build Tool** | TypeScript Compiler | Native type checking at compile time |
| **Task Runner** | npm scripts | Lightweight; no external dependency |
| **Container** | Docker + Docker Compose | Reproducible environments; multi-service orchestration |
| **Logging** | Winston (structured logs) | JSON output; integration with centralized logging |
| **Environment** | dotenv | Secure configuration management |

### Quality & Security

| Component | Technology | Rationale |
|-----------|-----------|-----------|
| **Linting** | ESLint + Prettier | Enforce code style; automatic formatting |
| **Type Checking** | TypeScript strict mode | Catch errors before runtime |
| **Auth** | JWT (OAuth2 bearer tokens) | Stateless, scalable authentication |
| **Validation** | Joi or Zod | Schema validation for all inputs |

---

## Module Structure

### Directory Layout

```
backend/
├── src/
│   ├── api/
│   │   ├── controllers/          # Request handlers
│   │   │   ├── QuestionController.ts
│   │   │   └── ValidationController.ts
│   │   ├── routes/               # Express route definitions
│   │   │   ├── questionRoutes.ts
│   │   │   └── validationRoutes.ts
│   │   ├── middlewares/          # Express middlewares
│   │   │   ├── authMiddleware.ts
│   │   │   ├── errorHandler.ts
│   │   │   ├── requestValidator.ts
│   │   │   └── rateLimiter.ts
│   │   └── dtos/                 # Request/Response DTOs
│   │       ├── CreateQuestionDTO.ts
│   │       ├── QuestionResponseDTO.ts
│   │       └── ValidationResultDTO.ts
│   │
│   ├── orchestrator/             # Pipeline orchestration
│   │   ├── PipelineOrchestrator.ts  # Main orchestration logic
│   │   ├── ModelFactory.ts          # Factory pattern for model selection
│   │   ├── FallbackManager.ts       # Fallback strategies
│   │   └── ValidationOrchestrator.ts # Validation pipeline
│   │
│   ├── models/                   # Core model interfaces & implementations
│   │   ├── interfaces/
│   │   │   ├── IQuestionGenerator.ts
│   │   │   ├── ITestCaseGenerator.ts
│   │   │   ├── IValidator.ts
│   │   │   └── IModel.ts
│   │   ├── local/                # Local/Mock implementations
│   │   │   ├── LocalQuestionGenerator.ts
│   │   │   ├── MockTestCaseGenerator.ts
│   │   │   └── RuleBasedValidator.ts
│   │   ├── real/                 # Real model clients (for future use)
│   │   │   ├── RealQuestionGeneratorClient.ts
│   │   │   ├── RealTestCaseGeneratorClient.ts
│   │   │   └── RealValidatorClient.ts
│   │   └── factories/
│   │       └── TestCasePatternFactory.ts
│   │
│   ├── services/                 # Business logic layer
│   │   ├── QuestionService.ts
│   │   ├── TemplateService.ts
│   │   ├── CacheService.ts
│   │   ├── AuditLogService.ts
│   │   └── PromptTemplateService.ts
│   │
│   ├── repositories/             # Data access layer (TypeORM)
│   │   ├── QuestionRepository.ts
│   │   ├── TestCaseRepository.ts
│   │   ├── ValidationResultRepository.ts
│   │   ├── PromptTemplateRepository.ts
│   │   ├── AuditLogRepository.ts
│   │   └── RepositoryFactory.ts
│   │
│   ├── entities/                 # TypeORM entities (database models)
│   │   ├── Question.ts
│   │   ├── TestCase.ts
│   │   ├── ValidationResult.ts
│   │   ├── PromptTemplate.ts
│   │   ├── AuditLog.ts
│   │   └── User.ts
│   │
│   ├── config/
│   │   ├── AppConfig.ts          # Application configuration
│   │   ├── DatabaseConfig.ts     # TypeORM configuration
│   │   ├── CacheConfig.ts        # Redis configuration
│   │   ├── QueueConfig.ts        # RabbitMQ configuration
│   │   ├── ModelFactoryConfig.ts # Model selection config
│   │   └── LoggerConfig.ts       # Winston logger setup
│   │
│   ├── cache/
│   │   ├── RedisClient.ts        # Redis connection pool
│   │   ├── CacheManager.ts       # Cache abstraction layer
│   │   ├── CacheStrategies.ts    # Cache key patterns
│   │   └── CacheInvalidation.ts  # Cache invalidation logic
│   │
│   ├── queue/
│   │   ├── MessageConsumer.ts    # RabbitMQ consumer
│   │   ├── MessageProducer.ts    # Message publishing
│   │   ├── QueueWorker.ts        # Worker for async tasks
│   │   ├── MessageTypes.ts       # Message type definitions
│   │   └── DeadLetterHandler.ts  # DLQ error handling
│   │
│   ├── database/
│   │   ├── migrations/           # TypeORM migrations
│   │   │   ├── 1000_InitialSchema.ts
│   │   │   ├── 2000_AddAuditLogs.ts
│   │   │   └── 3000_AddIndexes.ts
│   │   ├── seeders/              # Test data seeding
│   │   │   ├── DatabaseSeeder.ts
│   │   │   └── SampleTemplates.ts
│   │   └── DataSource.ts         # TypeORM DataSource initialization
│   │
│   ├── logging/
│   │   ├── Logger.ts             # Structured logging wrapper
│   │   ├── LogFormatter.ts       # JSON log formatting
│   │   └── AuditLogger.ts        # Business event logging
│   │
│   ├── exceptions/
│   │   ├── BaseException.ts
│   │   ├── ValidationException.ts
│   │   ├── NotFoundException.ts
│   │   ├── UnauthorizedException.ts
│   │   ├── RateLimitException.ts
│   │   ├── ExternalServiceException.ts
│   │   └── GlobalExceptionHandler.ts
│   │
│   ├── utils/
│   │   ├── validators/
│   │   │   ├── InputValidator.ts
│   │   │   └── CodeValidator.ts
│   │   ├── helpers/
│   │   │   ├── StringUtils.ts
│   │   │   ├── CodeAnalyzer.ts
│   │   │   └── TopicMatcher.ts
│   │   └── constants/
│   │       ├── Topics.ts
│   │       ├── ErrorCodes.ts
│   │       └── ValidationRules.ts
│   │
│   └── app.ts                    # Express app initialization
│
├── tests/                        # Test suites (NOT in src/)
│   ├── unit/
│   │   ├── models/
│   │   │   ├── LocalQuestionGenerator.test.ts
│   │   │   ├── MockTestCaseGenerator.test.ts
│   │   │   ├── RuleBasedValidator.test.ts
│   │   │   └── ModelFactory.test.ts
│   │   ├── services/
│   │   │   ├── QuestionService.test.ts
│   │   │   └── TemplateService.test.ts
│   │   └── utils/
│   │       ├── InputValidator.test.ts
│   │       └── CodeValidator.test.ts
│   ├── integration/
│   │   ├── api/
│   │   │   ├── QuestionAPI.integration.test.ts
│   │   │   └── ValidationAPI.integration.test.ts
│   │   ├── database/
│   │   │   └── QuestionRepository.integration.test.ts
│   │   └── queue/
│   │       └── MessageQueue.integration.test.ts
│   ├── fixtures/
│   │   ├── sampleQuestions.ts
│   │   ├── sampleTemplates.ts
│   │   └── mockData.ts
│   └── setup/
│       ├── testDatabaseSetup.ts
│       └── jest.config.ts
│
├── docker/
│   ├── Dockerfile
│   ├── Dockerfile.dev
│   └── .dockerignore
│
├── scripts/
│   ├── build.sh
│   ├── start.sh
│   ├── migrate.sh
│   ├── seed.sh
│   └── test.sh
│
├── .env.local                    # Local development config
├── .env.production               # Production config
├── .env.example                  # Template config
├── package.json
├── tsconfig.json
├── jest.config.js
├── docker-compose.yml
├── docker-compose.dev.yml
├── README.md
└── LICENSE
```

---

## API Design

### Base URL
```
https://api.executionengine.com/api/v1
```

### Authentication
All endpoints require JWT Bearer token in Authorization header:
```
Authorization: Bearer <jwt_token>
```

### Response Format
```json
{
  "success": true,
  "data": {},
  "meta": {
    "timestamp": "2026-04-28T10:30:00Z",
    "requestId": "req_abc123",
    "version": "1.0"
  },
  "errors": null
}
```

### Error Response Format
```json
{
  "success": false,
  "data": null,
  "meta": {
    "timestamp": "2026-04-28T10:30:00Z",
    "requestId": "req_abc123",
    "version": "1.0"
  },
  "errors": [
    {
      "code": "VALIDATION_ERROR",
      "message": "Invalid input",
      "details": {
        "field": "difficulty",
        "reason": "Must be EASY, MEDIUM, or HARD"
      }
    }
  ]
}
```

### Core Endpoints

#### 1. Generate Question
```
POST /api/v1/questions/generate
Content-Type: application/json
Authorization: Bearer <jwt_token>

Request Body:
{
  "topic": "Arrays",
  "subtopic": "Merge Sort",
  "difficulty": "MEDIUM",
  "language": "Python",
  "title": "Sort Array with Merge Sort",
  "description": "Implement merge sort algorithm"
}

Response (200 OK):
{
  "success": true,
  "data": {
    "id": "q_550e8400e29b41d4a716446655440000",
    "topic": "Arrays",
    "subtopic": "Merge Sort",
    "difficulty": "MEDIUM",
    "language": "Python",
    "title": "Sort Array with Merge Sort",
    "problemStatement": "Write a function to sort an array using merge sort algorithm...",
    "sampleSolution": "def merge_sort(arr):\n    if len(arr) <= 1:\n        return arr\n    ...",
    "testCases": [
      {
        "id": "tc_1",
        "testInput": "[64, 34, 25, 12, 22, 11, 90]",
        "expectedOutput": "[11, 12, 22, 25, 34, 64, 90]",
        "difficulty": "EASY",
        "visible": true,
        "explanation": "Ascending order sort"
      }
    ],
    "validationResult": {
      "status": "PASS",
      "rulesPassed": 6,
      "totalRules": 6,
      "notes": "All validation rules passed"
    },
    "createdAt": "2026-04-28T10:30:00Z",
    "status": "DRAFT"
  },
  "meta": { ... }
}

Error (422 Unprocessable Entity):
{
  "success": false,
  "data": null,
  "errors": [
    {
      "code": "INVALID_TOPIC",
      "message": "Topic not recognized",
      "details": {
        "field": "topic",
        "provided": "InvalidTopic",
        "valid_topics": ["Arrays", "Sorting", "Graphs", ...]
      }
    }
  ],
  "meta": { ... }
}
```

#### 2. Get Question
```
GET /api/v1/questions/{id}
Authorization: Bearer <jwt_token>

Response (200 OK):
{
  "success": true,
  "data": {
    "id": "q_550e8400e29b41d4a716446655440000",
    "topic": "Arrays",
    "title": "Sort Array with Merge Sort",
    "problemStatement": "...",
    "sampleSolution": "...",
    "testCases": [...],
    "validationResult": {...},
    "createdAt": "2026-04-28T10:30:00Z",
    "updatedAt": "2026-04-28T10:35:00Z",
    "status": "PUBLISHED"
  },
  "meta": { ... }
}

Error (404 Not Found):
{
  "success": false,
  "errors": [
    {
      "code": "QUESTION_NOT_FOUND",
      "message": "Question with ID q_invalid not found"
    }
  ]
}
```

#### 3. Update Question
```
PUT /api/v1/questions/{id}
Content-Type: application/json
Authorization: Bearer <jwt_token>

Request Body:
{
  "problemStatement": "Updated problem statement...",
  "sampleSolution": "Updated solution...",
  "testCases": [...]
}

Response (200 OK):
{
  "success": true,
  "data": {
    "id": "q_550e8400e29b41d4a716446655440000",
    "topic": "Arrays",
    ...
    "updatedAt": "2026-04-28T10:40:00Z",
    "status": "DRAFT"
  },
  "meta": { ... }
}
```

#### 4. Publish Question
```
POST /api/v1/questions/{id}/publish
Authorization: Bearer <jwt_token>

Response (200 OK):
{
  "success": true,
  "data": {
    "id": "q_550e8400e29b41d4a716446655440000",
    "status": "PUBLISHED",
    "publishedAt": "2026-04-28T10:45:00Z"
  },
  "meta": { ... }
}

Error (422 Unprocessable Entity):
{
  "success": false,
  "errors": [
    {
      "code": "VALIDATION_FAILED",
      "message": "Question cannot be published. Validation status: FAIL",
      "details": {
        "validationStatus": "FAIL",
        "failedRules": ["RULE_3_DIFFICULTY_CALIBRATION"]
      }
    }
  ]
}
```

#### 5. Validate Question
```
POST /api/v1/questions/{id}/validate
Authorization: Bearer <jwt_token>

Optional Request Body:
{
  "force": false,
  "skipRules": []
}

Response (200 OK):
{
  "success": true,
  "data": {
    "questionId": "q_550e8400e29b41d4a716446655440000",
    "validationResult": {
      "id": "vr_650e8400e29b41d4a716446655440001",
      "status": "PASS",
      "rulesPassed": 6,
      "totalRules": 6,
      "ruleDetails": [
        {
          "rule": "STRUCTURE_CHECK",
          "passed": true,
          "message": "Problem statement length valid"
        },
        {
          "rule": "TOPIC_ALIGNMENT",
          "passed": true,
          "message": "Contains keywords: array, element, index"
        },
        {
          "rule": "DIFFICULTY_CALIBRATION",
          "passed": true,
          "message": "Difficulty level consistent with content"
        },
        {
          "rule": "CODE_SYNTAX",
          "passed": true,
          "message": "Solution code is syntactically valid"
        },
        {
          "rule": "LOGIC_CONSISTENCY",
          "passed": true,
          "message": "Solution passes all test cases"
        },
        {
          "rule": "EDGE_CASE_COVERAGE",
          "passed": true,
          "message": "Test cases cover edge cases"
        }
      ],
      "timestamp": "2026-04-28T10:50:00Z"
    }
  },
  "meta": { ... }
}

Error (422 Unprocessable Entity - Validation Fails):
{
  "success": false,
  "errors": [
    {
      "code": "VALIDATION_FAILED",
      "message": "Question validation failed",
      "details": {
        "validationStatus": "FAIL",
        "failedRules": [
          {
            "rule": "RULE_5_LOGIC_CONSISTENCY",
            "message": "Solution fails on test case #3",
            "testCase": {
              "id": "tc_3",
              "testInput": "[5, 1, 9, 3]",
              "expectedOutput": "[1, 3, 5, 9]",
              "actualOutput": "[1, 3, 9, 5]"
            }
          }
        ]
      }
    }
  ]
}
```

#### 6. List Questions (with Pagination & Filtering)
```
GET /api/v1/questions?status=PUBLISHED&topic=Arrays&page=1&limit=20
Authorization: Bearer <jwt_token>

Response (200 OK):
{
  "success": true,
  "data": {
    "items": [
      { "id": "q_1", "title": "...", "topic": "Arrays", ... },
      { "id": "q_2", "title": "...", "topic": "Arrays", ... }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 250,
      "hasNext": true
    }
  },
  "meta": { ... }
}
```

#### 7. Delete Question
```
DELETE /api/v1/questions/{id}
Authorization: Bearer <jwt_token>

Response (204 No Content):
```

#### 8. Get Validation Results
```
GET /api/v1/questions/{id}/validation-results
Authorization: Bearer <jwt_token>

Response (200 OK):
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "vr_1",
        "status": "PASS",
        "rulesPassed": 6,
        "totalRules": 6,
        "createdAt": "2026-04-28T10:50:00Z"
      }
    ]
  }
}
```

---

## Database Schema

### Entity-Relationship Diagram

```
┌─────────────────────────────┐
│         Users               │
├─────────────────────────────┤
│ id (UUID)                   │
│ email (VARCHAR, UNIQUE)     │
│ name (VARCHAR)              │
│ role (ENUM)                 │
│ created_at (TIMESTAMP)      │
└─────────────────────────────┘
         │ 1:N
         │
         ├─────────────────┐
         │                 │
    ┌────▼────────────┐  ┌─▼─────────────────┐
    │   Questions     │  │  Audit_Logs       │
    ├────────────────┤  ├───────────────────┤
    │ id (UUID)      │  │ id (UUID)         │
    │ title          │  │ event_type        │
    │ topic          │  │ question_id       │
    │ difficulty     │──│ user_id           │
    │ problem_stmt   │  │ details (JSON)    │
    │ sample_sol     │  │ timestamp         │
    │ created_by     │  └───────────────────┘
    │ created_at     │
    │ updated_at     │
    │ status (ENUM)  │
    │ version        │
    └────┬───────────┘
         │ 1:N
         │
    ┌────▼─────────────────┐
    │   Test_Cases         │
    ├──────────────────────┤
    │ id (UUID)            │
    │ question_id (FK)     │
    │ test_input (TEXT)    │
    │ expected_output      │
    │ difficulty (ENUM)    │
    │ visible (BOOLEAN)    │
    │ created_at           │
    └──────────────────────┘

    ┌─────────────────────────────┐
    │  Validation_Results         │
    ├─────────────────────────────┤
    │ id (UUID)                   │
    │ question_id (FK)            │
    │ status (ENUM: PASS/WARN/FAIL
    │ rules_passed (INT)          │
    │ total_rules (INT)           │
    │ rule_details (JSONB)        │
    │ notes (TEXT)                │
    │ created_at (TIMESTAMP)      │
    └─────────────────────────────┘

    ┌──────────────────────────────┐
    │  Prompt_Templates            │
    ├──────────────────────────────┤
    │ id (UUID)                    │
    │ topic (VARCHAR)              │
    │ subtopic (VARCHAR)           │
    │ difficulty (ENUM)            │
    │ language (VARCHAR)           │
    │ template (TEXT)              │
    │ variables (JSONB)            │
    │ cached_at (TIMESTAMP)        │
    │ version (INT)                │
    └──────────────────────────────┘
```

### SQL Table Definitions

```sql
-- Users Table
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email VARCHAR(255) UNIQUE NOT NULL,
  name VARCHAR(255) NOT NULL,
  role ENUM ('ADMIN', 'EDITOR', 'REVIEWER') NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_email (email),
  INDEX idx_role (role)
);

-- Questions Table
CREATE TABLE questions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title VARCHAR(255) NOT NULL,
  topic VARCHAR(100) NOT NULL,
  subtopic VARCHAR(100),
  difficulty ENUM ('EASY', 'MEDIUM', 'HARD') NOT NULL,
  language VARCHAR(50) NOT NULL,
  problem_statement TEXT NOT NULL,
  sample_solution TEXT NOT NULL,
  created_by UUID NOT NULL REFERENCES users(id),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  status ENUM ('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT',
  version INT DEFAULT 1,
  is_deleted BOOLEAN DEFAULT false,
  INDEX idx_topic (topic),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at),
  INDEX idx_difficulty_topic (difficulty, topic)
);

-- Test Cases Table
CREATE TABLE test_cases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
  test_input TEXT NOT NULL,
  expected_output TEXT NOT NULL,
  actual_output TEXT,
  difficulty ENUM ('EASY', 'MEDIUM', 'HARD') NOT NULL,
  is_visible BOOLEAN DEFAULT false,
  execution_time_ms FLOAT,
  passed BOOLEAN,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_deleted BOOLEAN DEFAULT false,
  INDEX idx_question_id (question_id),
  INDEX idx_visible (is_visible),
  FOREIGN KEY idx_question_difficulty (question_id, difficulty)
);

-- Validation Results Table
CREATE TABLE validation_results (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
  status ENUM ('PASS', 'WARN', 'FAIL') NOT NULL,
  rules_passed INT NOT NULL,
  total_rules INT NOT NULL DEFAULT 6,
  rule_details JSONB NOT NULL DEFAULT '{}',
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_deleted BOOLEAN DEFAULT false,
  INDEX idx_question_status (question_id, status),
  INDEX idx_created_at (created_at)
);

-- Prompt Templates Table
CREATE TABLE prompt_templates (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  topic VARCHAR(100) NOT NULL,
  subtopic VARCHAR(100),
  difficulty ENUM ('EASY', 'MEDIUM', 'HARD') NOT NULL,
  language VARCHAR(50) NOT NULL,
  template TEXT NOT NULL,
  variables JSONB NOT NULL DEFAULT '{}',
  version INT DEFAULT 1,
  cached_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_active BOOLEAN DEFAULT true,
  is_deleted BOOLEAN DEFAULT false,
  UNIQUE INDEX idx_template_key (topic, subtopic, difficulty, language),
  INDEX idx_topic_difficulty (topic, difficulty)
);

-- Audit Logs Table
CREATE TABLE audit_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  event_type VARCHAR(100) NOT NULL,
  question_id UUID REFERENCES questions(id) ON DELETE SET NULL,
  user_id UUID REFERENCES users(id) ON DELETE SET NULL,
  action VARCHAR(50) NOT NULL,
  details JSONB NOT NULL DEFAULT '{}',
  ip_address VARCHAR(45),
  user_agent TEXT,
  status VARCHAR(50),
  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_question_id (question_id),
  INDEX idx_user_id (user_id),
  INDEX idx_event_type (event_type),
  INDEX idx_timestamp (timestamp)
);
```

### TypeORM Entity Definitions

```typescript
// Question.ts
@Entity('questions')
export class Question {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column('varchar', { length: 255 })
  title: string;

  @Column('varchar', { length: 100 })
  topic: string;

  @Column('varchar', { length: 100, nullable: true })
  subtopic: string;

  @Column('enum', { enum: DifficultyLevel })
  difficulty: DifficultyLevel;

  @Column('varchar', { length: 50 })
  language: string;

  @Column('text')
  problemStatement: string;

  @Column('text')
  sampleSolution: string;

  @ManyToOne(() => User)
  @JoinColumn({ name: 'created_by' })
  createdBy: User;

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;

  @Column('enum', { enum: QuestionStatus, default: QuestionStatus.DRAFT })
  status: QuestionStatus;

  @Column('int', { default: 1 })
  version: number;

  @OneToMany(() => TestCase, tc => tc.question, { cascade: true })
  testCases: TestCase[];

  @OneToMany(() => ValidationResult, vr => vr.question, { cascade: true })
  validationResults: ValidationResult[];
}
```

---

## Configuration Strategy

### Environment-Based Configuration

#### `.env.local` (Development)
```env
# Application
NODE_ENV=development
PORT=3000
LOG_LEVEL=debug

# Model Configuration
MODEL_MODE=mock
MOCK_QUESTION_GENERATOR_ENABLED=true
MOCK_TEST_CASE_GENERATOR_ENABLED=true
MOCK_VALIDATOR_ENABLED=true
MOCK_DATA_DIR=./mock-data/

# Database (Development)
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE_NAME=execution_engine_dev
DATABASE_USER=postgres
DATABASE_PASSWORD=postgres
DATABASE_POOL_SIZE=5
DATABASE_SSL=false

# Redis (Development)
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DB=0
REDIS_PASSWORD=

# RabbitMQ (Development)
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest
RABBITMQ_VHOST=/

# JWT Configuration
JWT_SECRET=dev-secret-key-change-in-production
JWT_EXPIRY=24h
REFRESH_TOKEN_SECRET=dev-refresh-secret
REFRESH_TOKEN_EXPIRY=7d

# Caching
CACHE_TTL_TEMPLATE=3600
CACHE_TTL_QUESTION=7200
CACHE_ENABLED=true

# Rate Limiting
RATE_LIMIT_ENABLED=false
RATE_LIMIT_WINDOW_MS=60000
RATE_LIMIT_MAX_REQUESTS=100

# Feature Flags
FEATURE_ASYNC_VALIDATION=true
FEATURE_PUBLISH_NOTIFICATION=false
```

#### `.env.production`
```env
# Application
NODE_ENV=production
PORT=8080
LOG_LEVEL=info

# Model Configuration
MODEL_MODE=mock  # Switch to 'real' when external models available
MOCK_QUESTION_GENERATOR_ENABLED=true
MOCK_TEST_CASE_GENERATOR_ENABLED=true
MOCK_VALIDATOR_ENABLED=true

# Database (Production)
DATABASE_HOST=${DB_PROD_HOST}
DATABASE_PORT=5432
DATABASE_NAME=execution_engine_prod
DATABASE_USER=${DB_PROD_USER}
DATABASE_PASSWORD=${DB_PROD_PASSWORD}
DATABASE_POOL_SIZE=20
DATABASE_SSL=true
DATABASE_SSL_REJECT_UNAUTHORIZED=true

# Redis (Production)
REDIS_HOST=${REDIS_PROD_HOST}
REDIS_PORT=6380
REDIS_DB=0
REDIS_PASSWORD=${REDIS_PROD_PASSWORD}
REDIS_TLS=true

# RabbitMQ (Production)
RABBITMQ_HOST=${RABBITMQ_PROD_HOST}
RABBITMQ_PORT=5671
RABBITMQ_USER=${RABBITMQ_USER}
RABBITMQ_PASSWORD=${RABBITMQ_PASSWORD}
RABBITMQ_VHOST=/prod

# JWT Configuration
JWT_SECRET=${JWT_SECRET_PROD}
JWT_EXPIRY=12h
REFRESH_TOKEN_SECRET=${REFRESH_TOKEN_SECRET_PROD}
REFRESH_TOKEN_EXPIRY=30d

# Caching
CACHE_TTL_TEMPLATE=86400
CACHE_TTL_QUESTION=43200
CACHE_ENABLED=true

# Rate Limiting
RATE_LIMIT_ENABLED=true
RATE_LIMIT_WINDOW_MS=60000
RATE_LIMIT_MAX_REQUESTS=1000

# Feature Flags
FEATURE_ASYNC_VALIDATION=true
FEATURE_PUBLISH_NOTIFICATION=true

# Monitoring
SENTRY_DSN=${SENTRY_DSN}
DATADOG_API_KEY=${DATADOG_API_KEY}
```

### Configuration Service Pattern

```typescript
// AppConfig.ts
export class AppConfig {
  static getModelMode(): 'mock' | 'real' {
    const mode = process.env.MODEL_MODE || 'mock';
    if (!['mock', 'real'].includes(mode)) {
      throw new Error(`Invalid MODEL_MODE: ${mode}`);
    }
    return mode as 'mock' | 'real';
  }

  static getDatabaseConfig(): DatabaseConnectionOptions {
    return {
      host: process.env.DATABASE_HOST || 'localhost',
      port: parseInt(process.env.DATABASE_PORT || '5432'),
      username: process.env.DATABASE_USER,
      password: process.env.DATABASE_PASSWORD,
      database: process.env.DATABASE_NAME,
      entities: [Question, TestCase, ValidationResult, PromptTemplate, AuditLog],
      synchronize: process.env.NODE_ENV === 'development',
      logging: process.env.LOG_LEVEL === 'debug',
      pool: {
        max: parseInt(process.env.DATABASE_POOL_SIZE || '10'),
      },
    };
  }

  static getRedisConfig(): RedisClientOptions {
    return {
      host: process.env.REDIS_HOST || 'localhost',
      port: parseInt(process.env.REDIS_PORT || '6379'),
      db: parseInt(process.env.REDIS_DB || '0'),
      password: process.env.REDIS_PASSWORD,
      tls: process.env.REDIS_TLS === 'true',
    };
  }

  static getRabbitMQConfig(): RabbitMQOptions {
    return {
      hostname: process.env.RABBITMQ_HOST || 'localhost',
      port: parseInt(process.env.RABBITMQ_PORT || '5672'),
      username: process.env.RABBITMQ_USER || 'guest',
      password: process.env.RABBITMQ_PASSWORD || 'guest',
      vhost: process.env.RABBITMQ_VHOST || '/',
    };
  }

  static getCacheTTL(key: 'TEMPLATE' | 'QUESTION'): number {
    const ttl = process.env[`CACHE_TTL_${key}`];
    return ttl ? parseInt(ttl) : 3600;
  }
}
```

### Factory Pattern for Model Selection

```typescript
// ModelFactory.ts
export class ModelFactory {
  private static mode: 'mock' | 'real' = AppConfig.getModelMode();

  static getQuestionGenerator(): IQuestionGenerator {
    if (this.mode === 'mock') {
      return new LocalQuestionGenerator();
    } else {
      return new RealQuestionGeneratorClient(
        process.env.QUESTION_GENERATOR_URL
      );
    }
  }

  static getTestCaseGenerator(): ITestCaseGenerator {
    if (this.mode === 'mock') {
      return new MockTestCaseGenerator();
    } else {
      return new RealTestCaseGeneratorClient(
        process.env.TEST_CASE_GENERATOR_URL
      );
    }
  }

  static getValidator(): IValidator {
    if (this.mode === 'mock') {
      return new RuleBasedValidator();
    } else {
      return new RealValidatorClient(process.env.VALIDATOR_URL);
    }
  }

  // Allow runtime switching for testing
  static setMode(mode: 'mock' | 'real'): void {
    this.mode = mode;
    Logger.info(`Model factory switched to ${mode} mode`);
  }
}
```

---

## Implementation Dependencies

### npm Package Dependencies (package.json)

#### Core Framework & Server
```json
{
  "express": "^4.18.2",
  "cors": "^2.8.5",
  "helmet": "^7.0.0",
  "compression": "^1.7.4"
}
```

#### Database & ORM
```json
{
  "typeorm": "^0.3.16",
  "pg": "^8.10.0",
  "mysql2": "^3.6.0"
}
```

#### Caching
```json
{
  "redis": "^4.6.10",
  "ioredis": "^5.3.2"
}
```

#### Message Queue
```json
{
  "amqplib": "^0.10.3",
  "rabbitmq": "^2.3.0"
}
```

#### Configuration & Environment
```json
{
  "dotenv": "^16.3.1",
  "joi": "^17.10.1"
}
```

#### Logging
```json
{
  "winston": "^3.11.0",
  "winston-daily-rotate-file": "^4.7.1"
}
```

#### Authentication & Security
```json
{
  "jsonwebtoken": "^9.1.1",
  "bcryptjs": "^2.4.3",
  "express-rate-limit": "^7.1.5"
}
```

#### Data Validation & Transformation
```json
{
  "class-validator": "^0.14.0",
  "class-transformer": "^0.5.1",
  "uuid": "^9.0.0"
}
```

#### Development Dependencies
```json
{
  "typescript": "^5.2.2",
  "@types/node": "^20.5.9",
  "@types/express": "^4.17.20",
  "ts-node": "^10.9.1",
  "ts-loader": "^9.5.0",
  "nodemon": "^3.0.1",
  "jest": "^29.7.0",
  "@types/jest": "^29.5.7",
  "ts-jest": "^29.1.1",
  "supertest": "^6.3.3",
  "testcontainers": "^9.2.1",
  "@testcontainers/postgresql": "^9.2.1"
}
```

#### Linting & Formatting
```json
{
  "eslint": "^8.50.0",
  "@typescript-eslint/eslint-plugin": "^6.7.5",
  "@typescript-eslint/parser": "^6.7.5",
  "prettier": "^3.0.3"
}
```

#### Build & Runtime
```json
{
  "webpack": "^5.89.0",
  "webpack-cli": "^5.1.4"
}
```

### Complete Dependencies List

```json
{
  "name": "execution-engine-ai-services",
  "version": "1.0.0",
  "description": "Self-contained AI Services Module for Execution Engine",
  "main": "dist/app.js",
  "scripts": {
    "dev": "nodemon --exec ts-node src/app.ts",
    "build": "tsc",
    "start": "node dist/app.js",
    "test": "jest",
    "test:watch": "jest --watch",
    "test:coverage": "jest --coverage",
    "lint": "eslint src --ext .ts",
    "format": "prettier --write src",
    "migrate": "typeorm migration:run",
    "seed": "ts-node scripts/seed.ts",
    "docker:build": "docker build -t execution-engine:latest .",
    "docker:up": "docker-compose up -d",
    "docker:down": "docker-compose down"
  },
  "dependencies": {
    "express": "^4.18.2",
    "cors": "^2.8.5",
    "helmet": "^7.0.0",
    "compression": "^1.7.4",
    "typeorm": "^0.3.16",
    "pg": "^8.10.0",
    "redis": "^4.6.10",
    "amqplib": "^0.10.3",
    "dotenv": "^16.3.1",
    "joi": "^17.10.1",
    "winston": "^3.11.0",
    "jsonwebtoken": "^9.1.1",
    "bcryptjs": "^2.4.3",
    "express-rate-limit": "^7.1.5",
    "class-validator": "^0.14.0",
    "class-transformer": "^0.5.1",
    "uuid": "^9.0.0"
  },
  "devDependencies": {
    "typescript": "^5.2.2",
    "@types/node": "^20.5.9",
    "@types/express": "^4.17.20",
    "ts-node": "^10.9.1",
    "ts-loader": "^9.5.0",
    "nodemon": "^3.0.1",
    "jest": "^29.7.0",
    "@types/jest": "^29.5.7",
    "ts-jest": "^29.1.1",
    "supertest": "^6.3.3",
    "testcontainers": "^9.2.1",
    "@testcontainers/postgresql": "^9.2.1",
    "eslint": "^8.50.0",
    "@typescript-eslint/eslint-plugin": "^6.7.5",
    "@typescript-eslint/parser": "^6.7.5",
    "prettier": "^3.0.3"
  },
  "engines": {
    "node": ">=20.0.0",
    "npm": ">=9.0.0"
  }
}
```

### Module Relationships & Import Dependencies

```
app.ts
  ├─ express setup
  ├─ middleware configuration
  └─ route initialization
        ├─ api/routes/questionRoutes.ts
        │  └─ api/controllers/QuestionController.ts
        │     ├─ orchestrator/PipelineOrchestrator.ts
        │     │  ├─ orchestrator/ModelFactory.ts
        │     │  │  ├─ models/local/LocalQuestionGenerator.ts
        │     │  │  ├─ models/local/MockTestCaseGenerator.ts
        │     │  │  └─ models/local/RuleBasedValidator.ts
        │     │  ├─ services/QuestionService.ts
        │     │  │  ├─ repositories/QuestionRepository.ts
        │     │  │  ├─ cache/CacheManager.ts
        │     │  │  └─ services/TemplateService.ts
        │     │  ├─ cache/CacheManager.ts
        │     │  └─ queue/MessageProducer.ts
        │     └─ logging/Logger.ts
        ├─ api/middlewares/authMiddleware.ts
        ├─ api/middlewares/errorHandler.ts
        └─ api/middlewares/requestValidator.ts
```

---

## Error Handling & Fallback

### Error Categories & Handling Strategy

```typescript
// Error Hierarchy
BaseException
  ├─ ValidationException (400)
  │  └─ InvalidInputException
  ├─ AuthenticationException (401)
  │  └─ UnauthorizedException
  ├─ ForbiddenException (403)
  ├─ NotFoundException (404)
  │  └─ QuestionNotFoundException
  ├─ RateLimitException (429)
  ├─ ConflictException (409)
  ├─ ServiceException (500)
  │  ├─ ExternalServiceException
  │  ├─ DatabaseException
  │  ├─ CacheException
  │  └─ QueueException
  └─ NotImplementedException (501)
```

### Fallback Matrix

| Scenario | Error | Fallback Action | Outcome |
|----------|-------|-----------------|---------|
| **Redis Unavailable** | CacheException | Query database directly | Slower performance; data retrieved |
| **Template Not Found** | NotFoundException | Return default template | Default question generated |
| **Test Generator Fails** | ServiceException | Return empty test cases | Question created with warning |
| **Validator Fails** | ServiceException | Set status to WARN | Manual review required |
| **Database Unavailable** | DatabaseException | Return error (no fallback) | Error response; action blocked |
| **RabbitMQ Unavailable** | QueueException | Store in local queue (retry) | Async task delayed until queue recovers |
| **Model Timeout (external)** | TimeoutException | Switch to mock implementation | Use LocalQuestionGenerator fallback |

### Fallback Implementation

```typescript
// FallbackManager.ts
export class FallbackManager {
  static async executeWithFallback<T>(
    primary: () => Promise<T>,
    fallback: () => Promise<T>,
    context: string
  ): Promise<T> {
    try {
      return await primary();
    } catch (error) {
      Logger.warn(`Primary execution failed for ${context}, using fallback`, {
        error: error.message,
      });
      return await fallback();
    }
  }

  // Cache fallback
  static async getWithCacheFallback<T>(
    key: string,
    loader: () => Promise<T>,
    ttl: number
  ): Promise<T> {
    try {
      // Try cache first
      const cached = await RedisClient.get(key);
      if (cached) return JSON.parse(cached);
    } catch (cacheError) {
      Logger.warn(`Cache miss for ${key}`, { error: cacheError.message });
    }

    // Load from primary source
    const data = await loader();

    // Try to cache (but don't fail if redis is down)
    try {
      await RedisClient.set(key, JSON.stringify(data), ttl);
    } catch (cacheError) {
      Logger.warn(`Cache write failed for ${key}`, {
        error: cacheError.message,
      });
    }

    return data;
  }

  // Template fallback
  static getDefaultTemplate(topic: string, difficulty: string): PromptTemplate {
    return {
      id: 'default',
      topic,
      difficulty,
      template: DEFAULT_TEMPLATE,
      variables: DEFAULT_VARIABLES,
    };
  }

  // Test case generation fallback
  static getEmptyTestCaseResult(): TestCaseGenerationResult {
    return {
      testCases: [],
      visibleCount: 0,
      hiddenCount: 0,
      warning: 'Test case generation failed; no test cases provided',
    };
  }
}
```

### Global Exception Handler

```typescript
// GlobalExceptionHandler.ts
export const globalExceptionHandler = (
  err: Error,
  req: Request,
  res: Response,
  next: NextFunction
) => {
  const requestId = req.get('X-Request-ID') || uuid();
  Logger.error('Unhandled exception', {
    requestId,
    error: err.message,
    stack: err.stack,
    path: req.path,
    method: req.method,
  });

  let statusCode = 500;
  let errorCode = 'INTERNAL_SERVER_ERROR';
  let message = 'An unexpected error occurred';

  if (err instanceof ValidationException) {
    statusCode = 400;
    errorCode = 'VALIDATION_ERROR';
    message = err.message;
  } else if (err instanceof NotFoundException) {
    statusCode = 404;
    errorCode = 'NOT_FOUND';
    message = err.message;
  } else if (err instanceof RateLimitException) {
    statusCode = 429;
    errorCode = 'RATE_LIMIT_EXCEEDED';
    message = 'Too many requests';
  }

  res.status(statusCode).json({
    success: false,
    data: null,
    errors: [
      {
        code: errorCode,
        message,
        requestId,
      },
    ],
    meta: {
      timestamp: new Date().toISOString(),
      requestId,
    },
  });
};
```

---

## Deployment Plan

### Docker Setup

#### Dockerfile (Multi-Stage Build)
```dockerfile
# Stage 1: Build
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --only=production && \
    npm install --save-dev typescript @types/node

# Copy source
COPY tsconfig.json ./
COPY src ./src

# Build TypeScript
RUN npm run build

# Stage 2: Runtime
FROM node:20-alpine

WORKDIR /app

# Install dumb-init for proper signal handling
RUN apk add --no-cache dumb-init

# Create non-root user
RUN addgroup -g 1001 -S nodejs && \
    adduser -S nodejs -u 1001

# Copy built app from builder
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/package*.json ./

# Set environment
ENV NODE_ENV=production
ENV PORT=3000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD node -e "require('http').get('http://localhost:3000/health', (r) => {if (r.statusCode !== 200) throw new Error(r.statusCode)})"

# Switch to non-root user
USER nodejs

# Expose port
EXPOSE 3000

# Use dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "--"]

# Start application
CMD ["node", "dist/app.js"]
```

#### docker-compose.yml (Development)
```yaml
version: '3.9'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: execution_engine_dev
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./scripts/init-db.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  rabbitmq:
    image: rabbitmq:3.12-management-alpine
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    build:
      context: .
      dockerfile: docker/Dockerfile.dev
    environment:
      NODE_ENV: development
      DATABASE_HOST: postgres
      DATABASE_PORT: 5432
      DATABASE_NAME: execution_engine_dev
      DATABASE_USER: postgres
      DATABASE_PASSWORD: postgres
      REDIS_HOST: redis
      REDIS_PORT: 6379
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      MODEL_MODE: mock
    ports:
      - "3000:3000"
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    volumes:
      - .:/app
      - /app/node_modules
    command: npm run dev

volumes:
  postgres_data:
  redis_data:
  rabbitmq_data:
```

#### docker-compose.prod.yml (Production)
```yaml
version: '3.9'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: execution_engine_prod
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_prod_data:/var/lib/postgresql/data
    restart: always
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER}"]
      interval: 30s
      timeout: 10s
      retries: 3

  redis:
    image: redis:7-alpine
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis_prod_data:/data
    restart: always
    healthcheck:
      test: ["CMD", "redis-cli", "--raw", "incr", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3

  rabbitmq:
    image: rabbitmq:3.12-management-alpine
    environment:
      RABBITMQ_DEFAULT_USER: ${RABBITMQ_USER}
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_PASSWORD}
    volumes:
      - rabbitmq_prod_data:/var/lib/rabbitmq
    restart: always
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3

  app:
    image: execution-engine:${VERSION}
    environment:
      NODE_ENV: production
      DATABASE_HOST: postgres
      DATABASE_PORT: 5432
      DATABASE_NAME: execution_engine_prod
      DATABASE_USER: ${DB_USER}
      DATABASE_PASSWORD: ${DB_PASSWORD}
      REDIS_HOST: redis
      REDIS_PORT: 6379
      REDIS_PASSWORD: ${REDIS_PASSWORD}
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      RABBITMQ_USER: ${RABBITMQ_USER}
      RABBITMQ_PASSWORD: ${RABBITMQ_PASSWORD}
      MODEL_MODE: mock
      JWT_SECRET: ${JWT_SECRET_PROD}
    ports:
      - "8080:3000"
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    restart: always
    deploy:
      replicas: 3
      update_config:
        parallelism: 1
        delay: 10s
      restart_policy:
        condition: on-failure

volumes:
  postgres_prod_data:
  redis_prod_data:
  rabbitmq_prod_data:
```

### Kubernetes Deployment

#### k8s/deployment.yaml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: execution-engine-ai-services
  namespace: default
spec:
  replicas: 3
  selector:
    matchLabels:
      app: execution-engine-ai-services
  template:
    metadata:
      labels:
        app: execution-engine-ai-services
    spec:
      containers:
      - name: app
        image: execution-engine:latest
        imagePullPolicy: Always
        ports:
        - containerPort: 3000
        env:
        - name: NODE_ENV
          value: "production"
        - name: DATABASE_HOST
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: database-host
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: database-password
        resources:
          requests:
            cpu: 100m
            memory: 256Mi
          limits:
            cpu: 500m
            memory: 512Mi
        livenessProbe:
          httpGet:
            path: /health
            port: 3000
          initialDelaySeconds: 10
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /ready
            port: 3000
          initialDelaySeconds: 5
          periodSeconds: 10
```

### Database Migration Strategy

#### Migration Script (TypeORM)
```typescript
// scripts/migrate.ts
import { DataSource } from "typeorm";
import { DatabaseConfig } from "../src/config/DatabaseConfig";

const AppDataSource = new DataSource(DatabaseConfig.getConfig());

AppDataSource.initialize()
  .then(async (dataSource) => {
    console.log("Running migrations...");
    await dataSource.runMigrations();
    console.log("Migrations completed successfully");
    await dataSource.destroy();
  })
  .catch((err) => {
    console.error("Migration failed:", err);
    process.exit(1);
  });
```

#### Example Migration
```typescript
// src/database/migrations/1000_InitialSchema.ts
import { MigrationInterface, QueryRunner, Table } from "typeorm";

export class InitialSchema1000 implements MigrationInterface {
  public async up(queryRunner: QueryRunner): Promise<void> {
    await queryRunner.createTable(
      new Table({
        name: "questions",
        columns: [
          {
            name: "id",
            type: "uuid",
            isPrimary: true,
            default: "gen_random_uuid()",
          },
          { name: "title", type: "varchar", length: "255" },
          { name: "topic", type: "varchar", length: "100" },
          { name: "difficulty", type: "enum", enum: ["EASY", "MEDIUM", "HARD"] },
          { name: "status", type: "enum", enum: ["DRAFT", "PUBLISHED", "ARCHIVED"] },
          { name: "created_at", type: "timestamp", default: "CURRENT_TIMESTAMP" },
        ],
      }),
      true
    );
  }

  public async down(queryRunner: QueryRunner): Promise<void> {
    await queryRunner.dropTable("questions");
  }
}
```

### Startup & Health Check Scripts

#### scripts/start.sh
```bash
#!/bin/bash
set -e

echo "Starting Execution Engine AI Services..."

# Check environment
if [ -z "$NODE_ENV" ]; then
  echo "ERROR: NODE_ENV not set"
  exit 1
fi

# Run migrations
echo "Running database migrations..."
npm run migrate

# Seed database (if development)
if [ "$NODE_ENV" = "development" ]; then
  echo "Seeding database with test data..."
  npm run seed
fi

# Start application
echo "Starting application server..."
npm start
```

#### scripts/health-check.sh
```bash
#!/bin/bash

HEALTH_ENDPOINT="http://localhost:3000/health"
MAX_RETRIES=30
RETRY_DELAY=2

echo "Checking application health..."

for i in $(seq 1 $MAX_RETRIES); do
  RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" $HEALTH_ENDPOINT)
  
  if [ "$RESPONSE" = "200" ]; then
    echo "✓ Application is healthy"
    exit 0
  fi
  
  echo "Attempt $i/$MAX_RETRIES: Health check returned $RESPONSE"
  sleep $RETRY_DELAY
done

echo "✗ Application failed health check after $MAX_RETRIES attempts"
exit 1
```

### Environment Setup Instructions

#### Local Development Setup
```bash
# 1. Clone repository
git clone <repo_url>
cd backend

# 2. Install dependencies
npm install

# 3. Create .env.local from template
cp .env.example .env.local

# 4. Start Docker services
docker-compose up -d

# 5. Run migrations
npm run migrate

# 6. Seed database
npm run seed

# 7. Start dev server
npm run dev

# Application will be available at http://localhost:3000
```

#### Production Deployment
```bash
# 1. Build Docker image
docker build -t execution-engine:v1.0.0 .

# 2. Push to registry
docker push <registry>/execution-engine:v1.0.0

# 3. Create .env.production with production values
# (Secrets should be managed by orchestrator)

# 4. Deploy using docker-compose
docker-compose -f docker-compose.prod.yml up -d

# 5. Verify deployment
./scripts/health-check.sh
```

---

## File Implementation Checklist

### Source Code Files (`src/`)

| File | Purpose | Status |
|------|---------|--------|
| `app.ts` | Express app initialization | Pending |
| `api/controllers/QuestionController.ts` | Question API handlers | Pending |
| `api/controllers/ValidationController.ts` | Validation API handlers | Pending |
| `api/routes/questionRoutes.ts` | Question route definitions | Pending |
| `api/routes/validationRoutes.ts` | Validation route definitions | Pending |
| `api/middlewares/authMiddleware.ts` | JWT authentication | Pending |
| `api/middlewares/errorHandler.ts` | Global error handling | Pending |
| `api/middlewares/requestValidator.ts` | Input validation | Pending |
| `api/middlewares/rateLimiter.ts` | Rate limiting | Pending |
| `api/dtos/CreateQuestionDTO.ts` | Request DTO | Pending |
| `api/dtos/QuestionResponseDTO.ts` | Response DTO | Pending |
| `orchestrator/PipelineOrchestrator.ts` | Main orchestration logic | Pending |
| `orchestrator/ModelFactory.ts` | Factory pattern implementation | Pending |
| `orchestrator/FallbackManager.ts` | Fallback strategies | Pending |
| `models/interfaces/IQuestionGenerator.ts` | Generator interface | Pending |
| `models/interfaces/ITestCaseGenerator.ts` | Test case interface | Pending |
| `models/interfaces/IValidator.ts` | Validator interface | Pending |
| `models/local/LocalQuestionGenerator.ts` | Template-based generator | Pending |
| `models/local/MockTestCaseGenerator.ts` | Pattern-based test generator | Pending |
| `models/local/RuleBasedValidator.ts` | Logic-based validator | Pending |
| `services/QuestionService.ts` | Question business logic | Pending |
| `services/TemplateService.ts` | Template management | Pending |
| `services/CacheService.ts` | Cache abstraction | Pending |
| `services/AuditLogService.ts` | Audit logging | Pending |
| `repositories/QuestionRepository.ts` | Question data access | Pending |
| `repositories/TestCaseRepository.ts` | Test case data access | Pending |
| `repositories/ValidationResultRepository.ts` | Validation result access | Pending |
| `entities/Question.ts` | Question entity | Pending |
| `entities/TestCase.ts` | Test case entity | Pending |
| `entities/ValidationResult.ts` | Validation result entity | Pending |
| `config/AppConfig.ts` | Application config | Pending |
| `config/DatabaseConfig.ts` | Database config | Pending |
| `cache/RedisClient.ts` | Redis connection | Pending |
| `cache/CacheManager.ts` | Cache abstraction | Pending |
| `queue/MessageConsumer.ts` | RabbitMQ consumer | Pending |
| `queue/MessageProducer.ts` | Message producer | Pending |
| `logging/Logger.ts` | Logging wrapper | Pending |
| `exceptions/GlobalExceptionHandler.ts` | Global error handler | Pending |
| `utils/InputValidator.ts` | Input validation utilities | Pending |

### Configuration Files

| File | Purpose |
|------|---------|
| `package.json` | Dependencies & scripts |
| `tsconfig.json` | TypeScript configuration |
| `jest.config.js` | Jest test configuration |
| `.env.local` | Local environment variables |
| `.env.production` | Production environment variables |
| `.env.example` | Environment template |
| `.dockerignore` | Docker build ignore patterns |
| `.eslintrc.json` | ESLint configuration |
| `.prettierrc.json` | Prettier formatting config |

### Database Files

| File | Purpose |
|------|---------|
| `src/database/migrations/1000_InitialSchema.ts` | Initial schema migration |
| `src/database/seeders/DatabaseSeeder.ts` | Test data seeding |
| `scripts/init-db.sql` | PostgreSQL initialization |

### Docker Files

| File | Purpose |
|------|---------|
| `docker/Dockerfile` | Production image |
| `docker/Dockerfile.dev` | Development image |
| `docker-compose.yml` | Dev compose file |
| `docker-compose.prod.yml` | Prod compose file |

### Test Files

| File | Purpose |
|------|---------|
| `tests/unit/models/LocalQuestionGenerator.test.ts` | Generator tests |
| `tests/integration/api/QuestionAPI.integration.test.ts` | API tests |
| `tests/fixtures/sampleQuestions.ts` | Test data |
| `tests/setup/testDatabaseSetup.ts` | Test database setup |

---

## Next Steps: Ready for Approval

This **implementation-plan.md** provides:

✅ **Technology Stack** — Fully specified with rationale  
✅ **Module Structure** — Complete directory layout with 40+ files  
✅ **API Design** — 8 core endpoints with request/response examples  
✅ **Database Schema** — 6 tables with TypeORM entities  
✅ **Configuration** — Factory pattern + environment-based setup  
✅ **Error Handling** — Comprehensive fallback strategies  
✅ **Dependencies** — npm packages with versions  
✅ **Deployment** — Docker, Kubernetes, migration scripts  

**Status:** Ready for `/approve` command to generate production-ready backend code.

