import { v4 as uuidv4 } from 'uuid';
import { DataSource } from 'typeorm';
import { Question, QuestionStatus, DifficultyLevel } from '../entities/Question';
import { QuestionRepository } from '../repositories/QuestionRepository';
import { TestCaseRepository } from '../repositories/TestCaseRepository';
import { ValidationResultRepository } from '../repositories/ValidationResultRepository';
import { AuditLogRepository } from '../repositories/AuditLogRepository';
import { PipelineOrchestrator, OrchestrationRequest, OrchestrationResult } from '../orchestrator/PipelineOrchestrator';
import { Logger } from '../logging/Logger';
import { NotFoundException, ValidationException } from '../exceptions/BaseException';

export class QuestionService {
  private logger = Logger.getInstance();
  private questionRepo: QuestionRepository;
  private testCaseRepo: TestCaseRepository;
  private validationRepo: ValidationResultRepository;
  private auditRepo: AuditLogRepository;
  private orchestrator: PipelineOrchestrator;

  constructor(private dataSource: DataSource) {
    this.questionRepo = new QuestionRepository(dataSource);
    this.testCaseRepo = new TestCaseRepository(dataSource);
    this.validationRepo = new ValidationResultRepository(dataSource);
    this.auditRepo = new AuditLogRepository(dataSource);
    this.orchestrator = new PipelineOrchestrator(dataSource);
  }

  async generateQuestion(request: {
    topic: string;
    subtopic: string;
    difficulty: DifficultyLevel;
    language: string;
    title: string;
    description?: string;
    userId?: string;
  }): Promise<OrchestrationResult> {
    this.logger.info('QuestionService: Generating question', {
      topic: request.topic,
      difficulty: request.difficulty,
    });

    const orchestrationRequest: OrchestrationRequest = {
      ...request,
      requestId: uuidv4(),
    };

    return await this.orchestrator.orchestrate(orchestrationRequest);
  }

  async getQuestionById(id: string): Promise<Question> {
    this.logger.debug('QuestionService: Getting question by ID', { id });

    const question = await this.questionRepo.findById(id);
    if (!question) {
      throw new NotFoundException(`Question with ID ${id} not found`);
    }

    return question;
  }

  async getQuestionsByTopic(topic: string, limit: number = 10, offset: number = 0): Promise<Question[]> {
    this.logger.debug('QuestionService: Getting questions by topic', { topic, limit, offset });
    return await this.questionRepo.findByTopic(topic, limit, offset);
  }

  async getQuestionsByTopicAndSubtopic(
    topic: string,
    subtopic: string,
    limit: number = 10
  ): Promise<Question[]> {
    this.logger.debug('QuestionService: Getting questions by topic and subtopic', {
      topic,
      subtopic,
      limit,
    });
    return await this.questionRepo.findByTopicAndSubtopic(topic, subtopic, limit);
  }

  async getQuestionsByDifficulty(difficulty: DifficultyLevel, limit: number = 10): Promise<Question[]> {
    this.logger.debug('QuestionService: Getting questions by difficulty', { difficulty, limit });
    return await this.questionRepo.findByDifficulty(difficulty, limit);
  }

  async getAllQuestions(limit: number = 10, offset: number = 0): Promise<Question[]> {
    this.logger.debug('QuestionService: Getting all questions', { limit, offset });
    return await this.questionRepo.findAll(limit, offset);
  }

  async updateQuestion(
    id: string,
    updates: {
      problemStatement?: string;
      sampleSolution?: string;
      title?: string;
      userId?: string;
    }
  ): Promise<Question> {
    this.logger.info('QuestionService: Updating question', { id });

    const question = await this.getQuestionById(id);

    if (updates.problemStatement) {
      question.problemStatement = updates.problemStatement;
    }
    if (updates.sampleSolution) {
      question.sampleSolution = updates.sampleSolution;
    }
    if (updates.title) {
      question.title = updates.title;
    }

    question.updatedBy = updates.userId;
    question.updatedAt = new Date();

    await this.questionRepo.save(question);

    return question;
  }

  async publishQuestion(id: string, userId?: string): Promise<Question> {
    this.logger.info('QuestionService: Publishing question', { id });

    const question = await this.getQuestionById(id);

    // Check validation status
    const validationResult = await this.validationRepo.findByQuestionId(id);
    if (validationResult && validationResult.status === 'FAIL') {
      throw new ValidationException('Cannot publish question with failed validation');
    }

    question.status = QuestionStatus.PUBLISHED;
    question.publishedAt = new Date();
    question.updatedBy = userId;
    question.updatedAt = new Date();

    await this.questionRepo.save(question);

    return question;
  }

  async archiveQuestion(id: string, userId?: string): Promise<Question> {
    this.logger.info('QuestionService: Archiving question', { id });

    const question = await this.getQuestionById(id);

    question.status = QuestionStatus.ARCHIVED;
    question.updatedBy = userId;
    question.updatedAt = new Date();

    await this.questionRepo.save(question);

    return question;
  }

  async deleteQuestion(id: string): Promise<void> {
    this.logger.info('QuestionService: Deleting question', { id });

    // Verify question exists
    await this.getQuestionById(id);

    // Delete related test cases
    await this.testCaseRepo.deleteByQuestionId(id);

    // Delete related validation results
    await this.questionRepo.delete(id);
  }

  async searchQuestions(searchTerm: string, limit: number = 10): Promise<Question[]> {
    this.logger.debug('QuestionService: Searching questions', { searchTerm, limit });
    return await this.questionRepo.search(searchTerm, limit);
  }

  async countQuestions(): Promise<number> {
    this.logger.debug('QuestionService: Counting questions');
    return await this.questionRepo.count();
  }

  async getQuestionStats(): Promise<{
    total: number;
    byDifficulty: Record<string, number>;
    byStatus: Record<string, number>;
  }> {
    this.logger.debug('QuestionService: Getting question statistics');

    // TODO: Implement proper stats calculation using database queries
    return {
      total: await this.questionRepo.count(),
      byDifficulty: {},
      byStatus: {},
    };
  }
}
