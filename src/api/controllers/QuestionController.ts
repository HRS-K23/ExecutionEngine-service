import { Response } from 'express';
import { DataSource } from 'typeorm';
import { ApiRequest, ApiResponse, asyncHandler } from '../middlewares/errorHandler';
import { QuestionService } from '../../services/QuestionService';
import { DifficultyLevel } from '../../entities/Question';
import { ValidationException, NotFoundException } from '../../exceptions/BaseException';
import { Logger } from '../../logging/Logger';

export class QuestionController {
  private questionService: QuestionService;
  private logger = Logger.getInstance();

  constructor(dataSource: DataSource) {
    this.questionService = new QuestionService(dataSource);
  }

  generateQuestion = asyncHandler(async (req: ApiRequest, res: Response) => {
    this.logger.info('QuestionController: Generating question', {
      requestId: req.requestId,
      body: req.body,
    });

    const { topic, subtopic, difficulty, language, title, description } = req.body;

    // Validate required fields
    if (!topic || !subtopic || !difficulty || !language || !title) {
      throw new ValidationException('Missing required fields', {
        required: ['topic', 'subtopic', 'difficulty', 'language', 'title'],
      });
    }

    // Validate difficulty
    if (!Object.values(DifficultyLevel).includes(difficulty)) {
      throw new ValidationException('Invalid difficulty level', {
        valid: Object.values(DifficultyLevel),
        provided: difficulty,
      });
    }

    const result = await this.questionService.generateQuestion({
      topic,
      subtopic,
      difficulty,
      language,
      title,
      description,
      userId: req.userId,
    });

    const response: ApiResponse = {
      success: result.status === 'SUCCESS',
      data: result,
      meta: {
        timestamp: new Date().toISOString(),
        requestId: req.requestId || 'unknown',
        version: '1.0',
      },
    };

    res.status(result.status === 'SUCCESS' ? 200 : 400).json(response);
  });

  getQuestion = asyncHandler(async (req: ApiRequest, res: Response) => {
    this.logger.info('QuestionController: Getting question', {
      requestId: req.requestId,
      questionId: req.params.id,
    });

    const question = await this.questionService.getQuestionById(req.params.id);

    const response: ApiResponse = {
      success: true,
      data: question,
      meta: {
        timestamp: new Date().toISOString(),
        requestId: req.requestId || 'unknown',
        version: '1.0',
      },
    };

    res.json(response);
  });

  getQuestionsByTopic = asyncHandler(async (req: ApiRequest, res: Response) => {
    const { topic } = req.params;
    const { limit = 10, offset = 0 } = req.query;

    this.logger.info('QuestionController: Getting questions by topic', {
      requestId: req.requestId,
      topic,
      limit,
      offset,
    });

    const questions = await this.questionService.getQuestionsByTopic(
      topic,
      Number(limit),
      Number(offset)
    );

    const response: ApiResponse = {
      success: true,
      data: questions,
      meta: {
        timestamp: new Date().toISOString(),
        requestId: req.requestId || 'unknown',
        version: '1.0',
      },
    };

    res.json(response);
  });

  getAllQuestions = asyncHandler(async (req: ApiRequest, res: Response) => {
    const { limit = 10, offset = 0 } = req.query;

    this.logger.info('QuestionController: Getting all questions', {
      requestId: req.requestId,
      limit,
      offset,
    });

    const questions = await this.questionService.getAllQuestions(Number(limit), Number(offset));

    const response: ApiResponse = {
      success: true,
      data: questions,
      meta: {
        timestamp: new Date().toISOString(),
        requestId: req.requestId || 'unknown',
        version: '1.0',
      },
    };

    res.json(response);
  });

  updateQuestion = asyncHandler(async (req: ApiRequest, res: Response) => {
    this.logger.info('QuestionController: Updating question', {
      requestId: req.requestId,
      questionId: req.params.id,
      updates: Object.keys(req.body),
    });

    const { problemStatement, sampleSolution, title } = req.body;

    const question = await this.questionService.updateQuestion(req.params.id, {
      problemStatement,
      sampleSolution,
      title,
      userId: req.userId,
    });

    const response: ApiResponse = {
      success: true,
      data: question,
      meta: {
        timestamp: new Date().toISOString(),
        requestId: req.requestId || 'unknown',
        version: '1.0',
      },
    };

    res.json(response);
  });

  publishQuestion = asyncHandler(async (req: ApiRequest, res: Response) => {
    this.logger.info('QuestionController: Publishing question', {
      requestId: req.requestId,
      questionId: req.params.id,
    });

    const question = await this.questionService.publishQuestion(req.params.id, req.userId);

    const response: ApiResponse = {
      success: true,
      data: question,
      meta: {
        timestamp: new Date().toISOString(),
        requestId: req.requestId || 'unknown',
        version: '1.0',
      },
    };

    res.json(response);
  });

  archiveQuestion = asyncHandler(async (req: ApiRequest, res: Response) => {
    this.logger.info('QuestionController: Archiving question', {
      requestId: req.requestId,
      questionId: req.params.id,
    });

    const question = await this.questionService.archiveQuestion(req.params.id, req.userId);

    const response: ApiResponse = {
      success: true,
      data: question,
      meta: {
        timestamp: new Date().toISOString(),
        requestId: req.requestId || 'unknown',
        version: '1.0',
      },
    };

    res.json(response);
  });

  deleteQuestion = asyncHandler(async (req: ApiRequest, res: Response) => {
    this.logger.info('QuestionController: Deleting question', {
      requestId: req.requestId,
      questionId: req.params.id,
    });

    await this.questionService.deleteQuestion(req.params.id);

    const response: ApiResponse = {
      success: true,
      data: { deleted: true },
      meta: {
        timestamp: new Date().toISOString(),
        requestId: req.requestId || 'unknown',
        version: '1.0',
      },
    };

    res.json(response);
  });

  searchQuestions = asyncHandler(async (req: ApiRequest, res: Response) => {
    const { q } = req.query;

    if (!q || typeof q !== 'string') {
      throw new ValidationException('Search query (q) is required');
    }

    this.logger.info('QuestionController: Searching questions', {
      requestId: req.requestId,
      query: q,
    });

    const questions = await this.questionService.searchQuestions(q);

    const response: ApiResponse = {
      success: true,
      data: questions,
      meta: {
        timestamp: new Date().toISOString(),
        requestId: req.requestId || 'unknown',
        version: '1.0',
      },
    };

    res.json(response);
  });

  getStats = asyncHandler(async (req: ApiRequest, res: Response) => {
    this.logger.info('QuestionController: Getting statistics', {
      requestId: req.requestId,
    });

    const stats = await this.questionService.getQuestionStats();

    const response: ApiResponse = {
      success: true,
      data: stats,
      meta: {
        timestamp: new Date().toISOString(),
        requestId: req.requestId || 'unknown',
        version: '1.0',
      },
    };

    res.json(response);
  });
}
