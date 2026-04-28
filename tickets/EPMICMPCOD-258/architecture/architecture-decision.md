# Architecture Decision Document — EPMICMPCOD-258 (REVISED)

## AI Services Module - Local/Mock Implementation (No External Servers)

**Ticket:** EPMICMPCOD-258  
**Team:** EXE (Execution Engine)  
**Date:** 2026-04-28  
**Status:** REVISED FOR LOCAL TESTING (Pending Approval)  
**Mode:** Self-Contained (No External AI Models Required)

---

## Executive Summary

The **revised AI Services Module** is completely **self-contained** and eliminates all external AI server dependencies. Three local implementations replace external models:
- **LocalQuestionGenerator** (template-based generation)
- **MockTestCaseGenerator** (pattern-based test creation)  
- **RuleBasedValidator** (deterministic rule checking)

A **factory pattern** enables seamless switching between mock and real models when available. The system maintains full scalability, async processing, caching, and fallback mechanisms—suitable for local development, testing, and demonstration **without requiring external AI services**.

---

## Core Redesign: From External to Local

### What Changed:

| Component | Original | Revised |
|-----------|----------|---------|
| **Question Generator** | External AI Model 1 | LocalQuestionGenerator (templates + rules) |
| **Test Case Generator** | External AI Model 2 | MockTestCaseGenerator (patterns) |
| **Question Validator** | External AI Model 3 | RuleBasedValidator (logic rules) |
| **Dependencies** | ❌ 3 external servers | ✅ **Zero external dependencies** |
| **Model Selection** | Hardcoded | ✅ Factory Pattern + Config |
| **Testing** | Requires live servers | ✅ **Works offline, entirely local** |

### What Remained Unchanged:

- ✅ **Orchestrator:** Pipeline orchestration logic
- ✅ **API Gateway:** Request routing, auth, rate limiting
- ✅ **Caching:** Redis for templates, questions, results
- ✅ **Storage:** PostgreSQL for persistence
- ✅ **Async Processing:** Message Queue (RabbitMQ/Kafka)
- ✅ **Scalability:** All horizontal scaling strategies intact
- ✅ **Security:** JWT, RBAC, encryption policies
- ✅ **Deployment:** Kubernetes, containerization
- ✅ **Monitoring:** Structured logging, auditing

---

## High-Level Architecture (Self-Contained)

```
┌─────────────────────────────────────────────────────────────┐
│                    SELF-CONTAINED SYSTEM                    │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Editor UI (Admin Interface)                           │ │
│  └────────────────┬─────────────────────────────────────┘ │
│                   │                                        │
│  ┌────────────────▼──────────────────────────────────────┐ │
│  │  API Gateway (Validation, Auth, Rate Limiting)        │ │
│  └────────────────┬──────────────────────────────────────┘ │
│                   │                                        │
│  ┌────────────────▼───────────────────────────────────┐   │
│  │  Pipeline Orchestrator                             │   │
│  │  ├─ Model Factory (Selects Mock/Real)              │   │
│  │  ├─ Config Service (Environment-based)             │   │
│  │  └─ Fallback Manager                               │   │
│  └────────────────┬───────────────────────────────────┘   │
│          ┌────────┼────────┬──────────────┐                │
│          │        │        │              │                │
│   ┌──────▼──┐ ┌──▼──┐ ┌───▼────┐  ┌──────▼──┐            │
│   │Question │ │Test │ │Question│  │  Cache  │            │
│   │Generator│ │Case │ │Validator  │  (Redis)│            │
│   └────┬────┘ │Gen  │ └────┬────┘  └────────┘            │
│        │      └──┬──┘      │                               │
│   ┌────▼──────┬──▼───┬─────▼────┐                         │
│   │ Local     │ Mock │ RuleBased │  (All Local!)          │
│   │Generator  │ Test │ Validator │                         │
│   │(Template) │ Gen  │           │                         │
│   │           │(Pat) │(Logic)    │                         │
│   └─────────────────────────────┘                         │
│                                                            │
│  ┌──────────────────────────────────────┐                │
│  │ Message Queue (Async Processing)     │                │
│  │ RabbitMQ / Kafka                     │                │
│  └──────────────┬───────────────────────┘                │
│                 │                                         │
│  ┌──────────────▼───────────────────────┐                │
│  │ Storage Layer (PostgreSQL)           │                │
│  │ - Questions, Test Cases              │                │
│  │ - Validation Results, Metadata       │                │
│  └──────────────────────────────────────┘                │
│                                                            │
│  ┌──────────────────────────────────────┐                │
│  │ Audit Log & Monitoring               │                │
│  │ Structured Logs, Events              │                │
│  └──────────────────────────────────────┘                │
│                                                            │
└─────────────────────────────────────────────────────────────┘
```

---

## Mock/Local Model Implementations

### 1. LocalQuestionGenerator

**Purpose:** Generate coding questions using templates + attribute injection

**Inputs:**
- Topic (e.g., "Arrays", "Sorting", "Graph Traversal")
- Subtopic (e.g., "Merge Sort", "DFS")
- Difficulty (EASY, MEDIUM, HARD)
- Language (Python, Java, C++)

**Logic:**
1. Load prompt template from PostgreSQL `prompt_templates` table
2. Inject attributes into template using variable substitution
3. Generate problem statement by combining template + random variations
4. Create sample solution with complexity adjustments based on difficulty
5. Return synthetic but realistic question package

**Caching:**
- Cache templates in Redis (1-hour TTL)
- Cache generated questions (2-hour TTL)

**Testing:** ✅ Works offline; no external calls

---

### 2. MockTestCaseGenerator

**Purpose:** Generate test cases using pattern matching

**Inputs:**
- Problem description
- Sample solution code
- Difficulty level
- Test case count (default: 8)

**Logic:**
1. Parse sample solution to extract variables, functions, logic branches
2. Generate test cases from predefined patterns:
   - Normal Case: Valid input within expected bounds
   - Edge Case: Empty input, single element, maximum size
   - Boundary Case: Min/max values, off-by-one errors
   - Error Case: Invalid input format, NULL values
   - Stress Case: Large input (for HARD difficulty only)
3. Validate each test case by executing against solution code
4. Return: visible_tests (3-5), hidden_tests (3-5), test_metadata

**Testing:** ✅ Works offline; pattern database is local

---

### 3. RuleBasedValidator

**Purpose:** Validate questions using logic rules (no external AI)

**Inputs:**
- Problem statement
- Sample solution code
- Topic, Difficulty, Subtopic
- Test cases

**Validation Rules:**

**Rule 1 - Structure Check:**
- Problem statement non-empty (length > 20 chars)
- Sample solution non-empty (length > 10 chars)
- Test cases count ≥ 5 (sufficient coverage)

**Rule 2 - Topic Alignment:**
- Problem statement contains keywords from topic taxonomy
- Example: "Arrays" → words like "array", "element", "index" present

**Rule 3 - Difficulty Calibration:**
- EASY: Problem < 150 words, solution < 20 lines
- MEDIUM: Problem 150-300 words, solution 20-50 lines
- HARD: Problem > 300 words, solution > 50 lines

**Rule 4 - Code Syntax Check:**
- Attempt to parse/compile sample solution
- Check for obvious syntax errors

**Rule 5 - Logic Consistency:**
- Execute sample solution against all provided test cases
- Confirm solution produces expected output

**Rule 6 - Edge Case Coverage:**
- Analyze test cases for coverage gaps

**Severity Levels:**
- ✅ **PASS:** All rules satisfied, question is publishable
- ⚠️ **WARN:** Minor issues; allow with review
- ❌ **FAIL:** Critical issues; reject

**Testing:** ✅ Works offline; rules are deterministic and local

---

## Configuration System

### Environment Variables

```yaml
# MODEL_MODE: Determines whether to use mock or real models
MODEL_MODE: "mock"  # "mock" | "real"

# Mock Mode Configuration
MOCK_QUESTION_GENERATOR_ENABLED: true
MOCK_TEST_CASE_GENERATOR_ENABLED: true
MOCK_VALIDATOR_ENABLED: true

# Real Mode Configuration (when AI models become available)
REAL_QUESTION_GENERATOR_ENABLED: false
REAL_TEST_CASE_GENERATOR_ENABLED: false
REAL_VALIDATOR_ENABLED: false

# Model Endpoints (used when MODEL_MODE=real)
QUESTION_GENERATOR_URL: "http://localhost:8001/generate"
TEST_CASE_GENERATOR_URL: "http://localhost:8002/generate-tests"
VALIDATOR_URL: "http://localhost:8003/validate"

# Mock Data & Testing
MOCK_DATA_DIR: "./mock-data/"
INJECT_TEST_DATA: true
```

### Factory Pattern Implementation

```typescript
// ModelFactory.ts
class ModelFactory {
  static getQuestionGenerator(): IQuestionGenerator {
    if (process.env.MODEL_MODE === 'mock') {
      return new LocalQuestionGenerator();  // NO external call
    } else {
      return new RealQuestionGeneratorClient(
        process.env.QUESTION_GENERATOR_URL
      );
    }
  }

  static getTestCaseGenerator(): ITestCaseGenerator {
    if (process.env.MODEL_MODE === 'mock') {
      return new MockTestCaseGenerator();  // NO external call
    } else {
      return new RealTestCaseGeneratorClient(
        process.env.TEST_CASE_GENERATOR_URL
      );
    }
  }

  static getValidator(): IValidator {
    if (process.env.MODEL_MODE === 'mock') {
      return new RuleBasedValidator();  // NO external call
    } else {
      return new RealValidatorClient(process.env.VALIDATOR_URL);
    }
  }
}
```

---

## Request Flow (Self-Contained)

### Phase 1: Input
- Admin submits generation request with attributes
- API Gateway validates input

### Phase 2: Configuration Check
- Pipeline Orchestrator checks `MODEL_MODE` environment variable
- Model Factory instantiates appropriate implementation

### Phase 3: Question Generation (Local)
- Orchestrator invokes LocalQuestionGenerator (no external HTTP call)
- Retrieves prompt template from Redis/Database
- Injects attributes (topic, difficulty) into template
- Returns synthetic question package immediately (~50ms)

### Phase 4: Parallel Validation & Test Case Generation (Local)
- **MockTestCaseGenerator:** Generates test cases using patterns (~100ms)
- **RuleBasedValidator:** Validates using rules (~50ms)
- Both operate independently

### Phase 5: Aggregation & Caching
- Results cached in Redis (2-hour TTL)
- Stored in PostgreSQL with metadata
- **Total response time: ~200ms** (vs. 30-45s with external models)

### Phase 6: Display & Publication
- Admin reviews question package in Editor UI
- Admin edits or publishes
- Audit event logged

---

## Scaling & Performance (Local Mode)

### Performance Characteristics:
- **Question Generation:** ~50ms (template lookup + injection)
- **Test Case Generation:** ~100ms (pattern matching + validation)
- **Validation:** ~50ms (rule-based checks)
- **Total Per Request:** ~200ms (vs. 30-45s for external models)

### Scalability:
- **Stateless Services:** Orchestrator, API Gateway scale horizontally
- **Cache Efficiency:** Redis caches 1-hour templates, 2-hour questions
- **Load:** Single instance can handle ~50 questions/second locally
- **Bottleneck:** PostgreSQL writes (solved by read replicas for queries)

---

## Upgrade Path: From Mock to Real

When external AI models become available, the upgrade is **seamless:**

1. **Deploy Real Model Servers**
2. **Update Environment Variables (MODEL_MODE=real)**
3. **Restart Services** (no code changes required)
4. **Factory Pattern automatically routes to real models**
5. **Fallback: If real model fails, automatically uses mock**

---

## Testing Strategy (Self-Contained)

### Unit Testing:
- Mock generators produce deterministic output
- Test input validation, error handling without dependencies
- 100% success rate (no external failures)

### Integration Testing:
- Test full pipeline: Input → Generation → Validation → Storage
- Inject test data via `MOCK_DATA_DIR`
- Verify caching, database persistence, async processing

### Load Testing:
- Generate 1000 questions locally (no external rate limits)
- Verify PostgreSQL and Redis performance
- Measure response times, memory usage, CPU

### Demo/Showcase:
- ✅ Works entirely offline
- ✅ No setup needed for external services
- ✅ Works on local laptop, development machine, CI/CD

---

## Technology Stack (Local Mode)

- **Backend:** Node.js (Orchestrator, API Gateway) or Python
- **Local Models:** Implemented in TypeScript/Python (no external dependencies)
- **Database:** PostgreSQL 14+
- **Cache:** Redis 7.0+ (optional for local dev)
- **Message Queue:** RabbitMQ 3.12+ or Kafka 3.5+
- **Containers:** Docker (optional; can run directly)

---

## Deployment (Local Testing)

### Option 1: Direct Node.js
```bash
npm install
MODEL_MODE=mock npm run start
# API available at http://localhost:3000
```

### Option 2: Docker
```bash
docker build -t codeval-service .
docker run -e MODEL_MODE=mock -p 3000:3000 codeval-service
```

### Option 3: Docker Compose (Full Stack)
```bash
docker-compose -f docker-compose.local.yml up
# Includes: API, PostgreSQL, Redis, UI
# All running locally, no external services
```

---

## Summary

**The revised architecture eliminates all external dependencies while maintaining full functionality:**

1. **LocalQuestionGenerator** replaces external AI Model 1 with template-based generation
2. **MockTestCaseGenerator** replaces external AI Model 2 with pattern-based test creation
3. **RuleBasedValidator** replaces external AI Model 3 with deterministic rule checking
4. **Factory Pattern** enables seamless upgrade to real models when available
5. **All other components** remain unchanged and fully scalable

**Testing is now possible entirely offline, on any machine, without external services. When real AI models become available, a simple environment variable change switches the system to use them—no code changes required.**

---

**Status:** READY FOR IMPLEMENTATION (No External Dependencies)  
**Architecture Version:** 2.0 (REVISED FOR LOCAL TESTING)  
**Approval:** Pending Human Review
