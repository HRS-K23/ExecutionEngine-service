import { DataSourceOptions } from 'typeorm';
import { AppConfig } from './AppConfig';

// Entities
import { Question } from '../entities/Question';
import { TestCase } from '../entities/TestCase';
import { ValidationResult } from '../entities/ValidationResult';
import { PromptTemplate } from '../entities/PromptTemplate';
import { AuditLog } from '../entities/AuditLog';
import { User } from '../entities/User';

export class DatabaseConfig {
  static readonly HOST = process.env.DB_HOST || 'localhost';
  static readonly PORT = parseInt(process.env.DB_PORT || '5432', 10);
  static readonly USERNAME = process.env.DB_USERNAME || 'postgres';
  static readonly PASSWORD = process.env.DB_PASSWORD || 'postgres';
  static readonly DATABASE = process.env.DB_NAME || 'execution_engine';
  static readonly LOGGING = AppConfig.isDevelopment();
  static readonly SYNCHRONIZE = AppConfig.isDevelopment() && process.env.DB_SYNC === 'true';

  static getConfig(): DataSourceOptions {
    return {
      type: 'postgres',
      host: this.HOST,
      port: this.PORT,
      username: this.USERNAME,
      password: this.PASSWORD,
      database: this.DATABASE,
      entities: [Question, TestCase, ValidationResult, PromptTemplate, AuditLog, User],
      migrations: ['src/database/migrations/*.ts'],
      subscribers: ['src/database/subscribers/*.ts'],
      logging: this.LOGGING,
      synchronize: this.SYNCHRONIZE,
      ssl: process.env.DB_SSL === 'true' ? { rejectUnauthorized: false } : false,
      extra: {
        max: 20,
        min: 5,
        idleTimeoutMillis: 30000,
        connectionTimeoutMillis: 2000,
      },
    };
  }
}

export class CacheConfig {
  static readonly REDIS_HOST = process.env.REDIS_HOST || 'localhost';
  static readonly REDIS_PORT = parseInt(process.env.REDIS_PORT || '6379', 10);
  static readonly REDIS_PASSWORD = process.env.REDIS_PASSWORD;
  static readonly REDIS_DB = parseInt(process.env.REDIS_DB || '0', 10);
  
  // Cache TTLs (in seconds)
  static readonly TEMPLATE_CACHE_TTL = parseInt(process.env.TEMPLATE_CACHE_TTL || '3600', 10);
  static readonly QUESTION_CACHE_TTL = parseInt(process.env.QUESTION_CACHE_TTL || '7200', 10);
  static readonly VALIDATION_CACHE_TTL = parseInt(process.env.VALIDATION_CACHE_TTL || '1800', 10);
  
  static readonly REDIS_RETRY_ATTEMPTS = 5;
  static readonly REDIS_RETRY_DELAY = 1000;
}

export class QueueConfig {
  static readonly RABBITMQ_URL = process.env.RABBITMQ_URL || 'amqp://guest:guest@localhost:5672';
  static readonly QUEUE_PREFETCH = parseInt(process.env.QUEUE_PREFETCH || '10', 10);
  static readonly QUEUE_NAME = 'execution-engine-queue';
  static readonly EXCHANGE_NAME = 'execution-engine-exchange';
  static readonly DLQ_NAME = 'execution-engine-dlq';
  
  static readonly QUEUE_TIMEOUT_MS = parseInt(process.env.QUEUE_TIMEOUT_MS || '60000', 10);
  static readonly QUEUE_MAX_RETRIES = parseInt(process.env.QUEUE_MAX_RETRIES || '3', 10);
}

export class LoggerConfig {
  static readonly LOG_LEVEL = process.env.LOG_LEVEL || 'info';
  static readonly LOG_FORMAT = process.env.LOG_FORMAT || 'json';
  static readonly LOG_FILE_MAX_SIZE = process.env.LOG_FILE_MAX_SIZE || '10m';
  static readonly LOG_FILE_MAX_FILES = process.env.LOG_FILE_MAX_FILES || '14';
  static readonly LOG_CONSOLE_ENABLED = process.env.LOG_CONSOLE_ENABLED !== 'false';
  static readonly LOG_FILE_ENABLED = process.env.LOG_FILE_ENABLED !== 'false';
}
