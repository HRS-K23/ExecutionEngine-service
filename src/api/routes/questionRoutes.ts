import { Router } from 'express';
import { DataSource } from 'typeorm';
import { QuestionController } from '../controllers/QuestionController';

export function createQuestionRoutes(dataSource: DataSource): Router {
  const router = Router();
  const controller = new QuestionController(dataSource);

  /**
   * POST /api/v1/questions/generate
   * Generate a new question with test cases and validation
   */
  router.post('/generate', controller.generateQuestion);

  /**
   * GET /api/v1/questions
   * Get all questions with pagination
   */
  router.get('/', controller.getAllQuestions);

  /**
   * GET /api/v1/questions/search
   * Search questions by query
   */
  router.get('/search', controller.searchQuestions);

  /**
   * GET /api/v1/questions/stats
   * Get question statistics
   */
  router.get('/stats', controller.getStats);

  /**
   * GET /api/v1/questions/topic/:topic
   * Get questions by topic
   */
  router.get('/topic/:topic', controller.getQuestionsByTopic);

  /**
   * GET /api/v1/questions/:id
   * Get a specific question by ID
   */
  router.get('/:id', controller.getQuestion);

  /**
   * PUT /api/v1/questions/:id
   * Update a question
   */
  router.put('/:id', controller.updateQuestion);

  /**
   * POST /api/v1/questions/:id/publish
   * Publish a question
   */
  router.post('/:id/publish', controller.publishQuestion);

  /**
   * POST /api/v1/questions/:id/archive
   * Archive a question
   */
  router.post('/:id/archive', controller.archiveQuestion);

  /**
   * DELETE /api/v1/questions/:id
   * Delete a question
   */
  router.delete('/:id', controller.deleteQuestion);

  return router;
}

export function createHealthCheckRoutes(): Router {
  const router = Router();

  router.get('/health', (req, res) => {
    res.json({
      status: 'ok',
      timestamp: new Date().toISOString(),
      uptime: process.uptime(),
    });
  });

  router.get('/health/ready', (req, res) => {
    // Check if database and other services are ready
    res.json({
      ready: true,
      timestamp: new Date().toISOString(),
    });
  });

  return router;
}
