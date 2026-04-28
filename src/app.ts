import express, { Express, Request, Response, NextFunction } from 'express';
import helmet from 'helmet';
import cors from 'cors';
import bodyParser from 'body-parser';
import rateLimit from 'express-rate-limit';
import { DataSource } from 'typeorm';
import { DatabaseConfig } from './config/DatabaseConfig';
import { AppConfig } from './config/AppConfig';
import { ModelFactory } from './models/ModelFactory';
import { Logger } from './logging/Logger';
import {
  errorHandler,
  ApiRequest,
} from './api/middlewares/errorHandler';
import {
  requestIdMiddleware,
  corsMiddleware,
  contentTypeMiddleware,
  authMiddleware,
} from './api/middlewares/requestMiddleware';
import {
  createQuestionRoutes,
  createHealthCheckRoutes,
} from './api/routes/questionRoutes';

class Application {
  private app: Express;
  private logger = Logger.getInstance();
  private dataSource: DataSource | null = null;

  constructor() {
    this.app = express();
    this.setupMiddleware();
  }

  private setupMiddleware(): void {
    // Security middleware
    this.app.use(helmet());

    // CORS
    this.app.use(
      cors({
        origin: AppConfig.CORS_ORIGINS,
        credentials: AppConfig.CORS_CREDENTIALS,
      })
    );

    // Body parser
    this.app.use(bodyParser.json({ limit: AppConfig.MAX_REQUEST_SIZE }));
    this.app.use(bodyParser.urlencoded({ limit: AppConfig.MAX_REQUEST_SIZE, extended: true }));

    // Custom middleware
    this.app.use(requestIdMiddleware);
    this.app.use(corsMiddleware);
    this.app.use(contentTypeMiddleware);
    this.app.use(authMiddleware);

    // Rate limiting
    const limiter = rateLimit({
      windowMs: AppConfig.RATE_LIMIT_WINDOW_MS,
      max: AppConfig.RATE_LIMIT_MAX_REQUESTS,
      message: 'Too many requests from this IP, please try again later.',
    });
    this.app.use('/api/', limiter);

    // Health check routes (before authentication)
    this.app.use('/', createHealthCheckRoutes());

    // API routes (after authentication)
    this.app.use('/api/v1/questions', async (req, res, next) => {
      if (!this.dataSource) {
        return res.status(503).json({
          success: false,
          errors: [{ code: 'SERVICE_UNAVAILABLE', message: 'Database not initialized' }],
          meta: {
            timestamp: new Date().toISOString(),
            requestId: (req as ApiRequest).requestId || 'unknown',
            version: '1.0',
          },
        });
      }
      next();
    });

    this.app.use('/api/v1/questions', createQuestionRoutes(this.dataSource!));

    // 404 handler
    this.app.use((req: ApiRequest, res: Response) => {
      res.status(404).json({
        success: false,
        errors: [
          {
            code: 'NOT_FOUND',
            message: `Route ${req.method} ${req.path} not found`,
          },
        ],
        meta: {
          timestamp: new Date().toISOString(),
          requestId: req.requestId || 'unknown',
          version: '1.0',
        },
      });
    });

    // Error handler (must be last)
    this.app.use(errorHandler);
  }

  async initialize(): Promise<void> {
    try {
      this.logger.info('Application: Initializing database connection');

      // Initialize database
      const dbConfig = DatabaseConfig.getConfig();
      this.dataSource = new DataSource(dbConfig);
      await this.dataSource.initialize();

      this.logger.info('Application: Database connected successfully');

      // Run migrations
      if (process.env.RUN_MIGRATIONS === 'true') {
        this.logger.info('Application: Running migrations');
        await this.dataSource.runMigrations();
        this.logger.info('Application: Migrations completed');
      }

      // Log model configuration
      const modelStatus = ModelFactory.getStatus();
      this.logger.info('Application: Model Factory Status', modelStatus);

    } catch (error) {
      this.logger.error('Application: Failed to initialize', { error });
      throw error;
    }
  }

  async start(): Promise<void> {
    try {
      await this.initialize();

      this.app.listen(AppConfig.PORT, AppConfig.HOST, () => {
        this.logger.info('Application: Server started', {
          host: AppConfig.HOST,
          port: AppConfig.PORT,
          env: AppConfig.NODE_ENV,
          apiPrefix: AppConfig.API_PREFIX,
        });
      });
    } catch (error) {
      this.logger.error('Application: Failed to start', { error });
      process.exit(1);
    }
  }

  getApp(): Express {
    return this.app;
  }

  async shutdown(): Promise<void> {
    if (this.dataSource && this.dataSource.isInitialized) {
      await this.dataSource.destroy();
      this.logger.info('Application: Database connection closed');
    }
  }
}

// Export Application class for testing
export { Application };

// Start server if running directly
if (require.main === module) {
  const app = new Application();
  app.start().catch((error) => {
    console.error('Failed to start application:', error);
    process.exit(1);
  });

  // Graceful shutdown
  process.on('SIGTERM', async () => {
    console.log('SIGTERM received, shutting down gracefully');
    await app.shutdown();
    process.exit(0);
  });

  process.on('SIGINT', async () => {
    console.log('SIGINT received, shutting down gracefully');
    await app.shutdown();
    process.exit(0);
  });
}
