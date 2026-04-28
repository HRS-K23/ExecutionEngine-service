import { v4 as uuidv4 } from 'uuid';
import { ModelFactory } from '../models/ModelFactory';
import {
  GenerateQuestionInput,
  GenerateTestCasesInput,
  ValidateQuestionInput,
} from '../models/interfaces/IModels';
import { Question, DifficultyLevel, QuestionStatus } from '../entities/Question';
import { TestCase } from '../entities/TestCase';
import { ValidationResult } from '../entities/ValidationResult';
import { QuestionRepository } from '../repositories/QuestionRepository';
import { TestCaseRepository } from '../repositories/TestCaseRepository';
import { ValidationResultRepository } from '../repositories/ValidationResultRepository';
import { AuditLogRepository } from '../repositories/AuditLogRepository';
import { PromptTemplateRepository } from '../repositories/PromptTemplateRepository';
import { AuditLog, AuditActionType } from '../entities/AuditLog';
import { Logger } from '../logging/Logger';
import { ExternalServiceException, InternalServerException } from '../exceptions/BaseException';
import { DataSource } from 'typeorm';

export interface OrchestrationRequest {
  topic: string;
  subtopic: string;
  difficulty: DifficultyLevel;
  language: string;
  title: string;
  description?: string;
  userId?: string;
  requestId?: string;
}

export interface OrchestrationResult {
  questionId: string;
  status: 'SUCCESS' | 'PARTIAL' | 'FAILED';
  question?: Question;
  testCases?: TestCase[];
  validationResult?: ValidationResult;
  errors?: string[];
  executionTimeMs: number;
}

/**
 * PipelineOrchestrator
 *
 * Manages the complete workflow for:
 * 1. Question generation (using LocalQuestionGenerator or real models)
 * 2. Test case generation (using MockTestCaseGenerator or real models)
 * 3. Question validation (using RuleBasedValidator or real models)
 * 4. Persistence and auditing
 *
 * Uses the Factory Pattern to switch between mock and real implementations seamlessly.
 */
export class PipelineOrchestrator {
  private logger = Logger.getInstance();

  constructor(private dataSource: DataSource) {}

  /**
   * Main orchestration method that generates question, test cases, and validates
   */
  async orchestrate(request: OrchestrationRequest): Promise<OrchestrationResult> {
    const startTime = Date.now();
    const requestId = request.requestId || uuidv4();
    const questionId = uuidv4();

    this.logger.info('PipelineOrchestrator: Starting orchestration', {
      requestId,
      questionId,
      topic: request.topic,
      difficulty: request.difficulty,
    });

    try {
      // Step 1: Generate Question
      this.logger.debug('PipelineOrchestrator: Step 1 - Generating question', { requestId });
      const generatedQuestion = await this.generateQuestion(
        request,
        requestId
      );

      // Step 2: Generate Test Cases
      this.logger.debug('PipelineOrchestrator: Step 2 - Generating test cases', { requestId });
      const generatedTestCases = await this.generateTestCases(
        generatedQuestion,
        request,
        requestId
      );

      // Step 3: Validate Question
      this.logger.debug('PipelineOrchestrator: Step 3 - Validating question', { requestId });
      const validationResult = await this.validateQuestion(
        generatedQuestion,
        generatedTestCases,
        request,
        requestId
      );

      // Step 4: Persist Question and Related Data
      this.logger.debug('PipelineOrchestrator: Step 4 - Persisting data', { requestId });
      const persistedQuestion = await this.persistQuestion(
        questionId,
        generatedQuestion,
        request,
        requestId
      );

      // Step 5: Persist Test Cases
      const persistedTestCases = await this.persistTestCases(
        questionId,
        generatedTestCases,
        requestId
      );

      // Step 6: Persist Validation Result
      const persistedValidationResult = await this.persistValidationResult(
        questionId,
        validationResult,
        requestId
      );

      // Step 7: Audit Log
      await this.logAuditEvent(
        questionId,
        AuditActionType.CREATE,
        request.userId,
        'Question generated via orchestration',
        requestId
      );

      const executionTimeMs = Date.now() - startTime;

      this.logger.info('PipelineOrchestrator: Orchestration completed successfully', {
        requestId,
        questionId,
        executionTimeMs,
        validationStatus: persistedValidationResult.status,
      });

      return {
        questionId,
        status: 'SUCCESS',
        question: persistedQuestion,
        testCases: persistedTestCases,
        validationResult: persistedValidationResult,
        executionTimeMs,
      };
    } catch (error) {
      const executionTimeMs = Date.now() - startTime;

      this.logger.error('PipelineOrchestrator: Orchestration failed', {
        requestId,
        questionId,
        error,
        executionTimeMs,
      });

      return {
        questionId,
        status: 'FAILED',
        errors: [error instanceof Error ? error.message : 'Unknown error'],
        executionTimeMs,
      };
    }
  }

  private async generateQuestion(
    request: OrchestrationRequest,
    requestId: string
  ): Promise<any> {
    try {
      const questionGenerator = ModelFactory.getQuestionGenerator();

      this.logger.info('PipelineOrchestrator: Using question generator', {
        generator: questionGenerator.getName(),
        requestId,
      });

      const input: GenerateQuestionInput = {
        topic: request.topic,
        subtopic: request.subtopic,
        difficulty: request.difficulty,
        language: request.language,
        title: request.title,
        description: request.description,
      };

      return await questionGenerator.generate(input);
    } catch (error) {
      this.logger.error('PipelineOrchestrator: Question generation failed', {
        requestId,
        error,
      });
      throw new ExternalServiceException(
        'Failed to generate question',
        'QuestionGenerator',
        { requestId }
      );
    }
  }

  private async generateTestCases(
    generatedQuestion: any,
    request: OrchestrationRequest,
    requestId: string
  ): Promise<any[]> {
    try {
      const testCaseGenerator = ModelFactory.getTestCaseGenerator();

      this.logger.info('PipelineOrchestrator: Using test case generator', {
        generator: testCaseGenerator.getName(),
        requestId,
      });

      const input: GenerateTestCasesInput = {
        problemDescription: generatedQuestion.problemStatement,
        sampleSolution: generatedQuestion.sampleSolution,
        difficulty: request.difficulty,
        language: request.language,
        testCaseCount: 8,
      };

      const result = await testCaseGenerator.generate(input);

      return [...result.visibleTests, ...result.hiddenTests];
    } catch (error) {
      this.logger.error('PipelineOrchestrator: Test case generation failed', {
        requestId,
        error,
      });
      throw new ExternalServiceException(
        'Failed to generate test cases',
        'TestCaseGenerator',
        { requestId }
      );
    }
  }

  private async validateQuestion(
    generatedQuestion: any,
    generatedTestCases: any[],
    request: OrchestrationRequest,
    requestId: string
  ): Promise<any> {
    try {
      const validator = ModelFactory.getValidator();

      this.logger.info('PipelineOrchestrator: Using validator', {
        validator: validator.getName(),
        requestId,
      });

      const input: ValidateQuestionInput = {
        topic: request.topic,
        subtopic: request.subtopic,
        difficulty: request.difficulty,
        language: request.language,
        problemStatement: generatedQuestion.problemStatement,
        sampleSolution: generatedQuestion.sampleSolution,
        testCases: generatedTestCases,
      };

      return await validator.validate(input);
    } catch (error) {
      this.logger.error('PipelineOrchestrator: Validation failed', {
        requestId,
        error,
      });
      throw new ExternalServiceException(
        'Failed to validate question',
        'Validator',
        { requestId }
      );
    }
  }

  private async persistQuestion(
    questionId: string,
    generatedQuestion: any,
    request: OrchestrationRequest,
    requestId: string
  ): Promise<Question> {
    try {
      const questionRepo = new QuestionRepository(this.dataSource);

      const question = new Question();
      question.id = questionId;
      question.topic = request.topic;
      question.subtopic = request.subtopic;
      question.difficulty = request.difficulty;
      question.language = request.language;
      question.title = request.title;
      question.problemStatement = generatedQuestion.problemStatement;
      question.sampleSolution = generatedQuestion.sampleSolution;
      question.sampleSolutionExplanation = generatedQuestion.sampleSolutionExplanation;
      question.status = QuestionStatus.DRAFT;
      question.createdBy = request.userId;
      question.metadata = {
        requestId,
        generatedAt: new Date().toISOString(),
      };

      return await questionRepo.save(question);
    } catch (error) {
      this.logger.error('PipelineOrchestrator: Failed to persist question', {
        questionId,
        requestId,
        error,
      });
      throw new InternalServerException('Failed to persist question', { requestId });
    }
  }

  private async persistTestCases(
    questionId: string,
    generatedTestCases: any[],
    requestId: string
  ): Promise<TestCase[]> {
    try {
      const testCaseRepo = new TestCaseRepository(this.dataSource);

      const testCases = generatedTestCases.map((tc) => {
        const entity = new TestCase();
        entity.id = uuidv4();
        entity.questionId = questionId;
        entity.testInput = tc.testInput;
        entity.expectedOutput = tc.expectedOutput;
        entity.caseType = tc.caseType;
        entity.visible = tc.visible;
        entity.explanation = tc.explanation;
        entity.difficulty = tc.difficulty;
        entity.metadata = { requestId };
        return entity;
      });

      return await testCaseRepo.saveMany(testCases);
    } catch (error) {
      this.logger.error('PipelineOrchestrator: Failed to persist test cases', {
        questionId,
        requestId,
        error,
      });
      throw new InternalServerException('Failed to persist test cases', { requestId });
    }
  }

  private async persistValidationResult(
    questionId: string,
    validationResult: any,
    requestId: string
  ): Promise<ValidationResult> {
    try {
      const validationRepo = new ValidationResultRepository(this.dataSource);

      const entity = new ValidationResult();
      entity.id = uuidv4();
      entity.questionId = questionId;
      entity.status = validationResult.status;
      entity.rulesPassed = validationResult.rulesPassed;
      entity.totalRules = validationResult.totalRules;
      entity.rules = validationResult.rules;
      entity.notes = validationResult.notes;
      entity.metadata = { requestId };

      return await validationRepo.save(entity);
    } catch (error) {
      this.logger.error('PipelineOrchestrator: Failed to persist validation result', {
        questionId,
        requestId,
        error,
      });
      throw new InternalServerException('Failed to persist validation result', { requestId });
    }
  }

  private async logAuditEvent(
    entityId: string,
    actionType: AuditActionType,
    userId: string | undefined,
    reason: string,
    requestId: string
  ): Promise<void> {
    try {
      const auditRepo = new AuditLogRepository(this.dataSource);

      const log = new AuditLog();
      log.id = uuidv4();
      log.entityType = 'Question';
      log.entityId = entityId;
      log.actionType = actionType;
      log.userId = userId;
      log.reason = reason;
      log.metadata = { requestId };

      await auditRepo.save(log);
    } catch (error) {
      this.logger.warn('PipelineOrchestrator: Failed to log audit event', {
        entityId,
        requestId,
        error,
      });
      // Don't throw - auditing failures shouldn't fail the main operation
    }
  }
}
