import dotenv from 'dotenv';

dotenv.config();

export class AppConfig {
  static readonly NODE_ENV = process.env.NODE_ENV || 'development';
  static readonly PORT = parseInt(process.env.PORT || '3000', 10);
  static readonly HOST = process.env.HOST || '0.0.0.0';
  static readonly API_PREFIX = '/api/v1';
  static readonly REQUEST_TIMEOUT_MS = parseInt(process.env.REQUEST_TIMEOUT_MS || '30000', 10);
  static readonly MAX_REQUEST_SIZE = process.env.MAX_REQUEST_SIZE || '10mb';
  
  // CORS
  static readonly CORS_ORIGINS = (process.env.CORS_ORIGINS || 'http://localhost:3000').split(',');
  static readonly CORS_CREDENTIALS = process.env.CORS_CREDENTIALS === 'true';
  
  // Rate Limiting
  static readonly RATE_LIMIT_WINDOW_MS = parseInt(process.env.RATE_LIMIT_WINDOW_MS || '900000', 10);
  static readonly RATE_LIMIT_MAX_REQUESTS = parseInt(process.env.RATE_LIMIT_MAX_REQUESTS || '100', 10);
  
  // JWT
  static readonly JWT_SECRET = process.env.JWT_SECRET || 'your-secret-key';
  static readonly JWT_EXPIRY = process.env.JWT_EXPIRY || '24h';
  
  // Environment
  static isDevelopment(): boolean {
    return this.NODE_ENV === 'development';
  }

  static isProduction(): boolean {
    return this.NODE_ENV === 'production';
  }

  static isTest(): boolean {
    return this.NODE_ENV === 'test';
  }
}

export class ModelConfig {
  static readonly MODEL_MODE = process.env.MODEL_MODE || 'mock';
  
  // Mock Mode Configuration
  static readonly MOCK_QUESTION_GENERATOR_ENABLED = process.env.MOCK_QUESTION_GENERATOR_ENABLED !== 'false';
  static readonly MOCK_TEST_CASE_GENERATOR_ENABLED = process.env.MOCK_TEST_CASE_GENERATOR_ENABLED !== 'false';
  static readonly MOCK_VALIDATOR_ENABLED = process.env.MOCK_VALIDATOR_ENABLED !== 'false';
  
  // Real Mode Configuration
  static readonly REAL_QUESTION_GENERATOR_ENABLED = process.env.REAL_QUESTION_GENERATOR_ENABLED === 'true';
  static readonly REAL_TEST_CASE_GENERATOR_ENABLED = process.env.REAL_TEST_CASE_GENERATOR_ENABLED === 'true';
  static readonly REAL_VALIDATOR_ENABLED = process.env.REAL_VALIDATOR_ENABLED === 'true';
  
  // Model Endpoints (used when MODEL_MODE=real)
  static readonly QUESTION_GENERATOR_URL = process.env.QUESTION_GENERATOR_URL || 'http://localhost:8001/generate';
  static readonly TEST_CASE_GENERATOR_URL = process.env.TEST_CASE_GENERATOR_URL || 'http://localhost:8002/generate-tests';
  static readonly VALIDATOR_URL = process.env.VALIDATOR_URL || 'http://localhost:8003/validate';
  
  // Timeout settings
  static readonly EXTERNAL_SERVICE_TIMEOUT_MS = parseInt(process.env.EXTERNAL_SERVICE_TIMEOUT_MS || '30000', 10);
  
  // Mock data & testing
  static readonly MOCK_DATA_DIR = process.env.MOCK_DATA_DIR || './mock-data/';
  static readonly INJECT_TEST_DATA = process.env.INJECT_TEST_DATA === 'true';
  
  static isMockMode(): boolean {
    return this.MODEL_MODE === 'mock';
  }

  static isRealMode(): boolean {
    return this.MODEL_MODE === 'real';
  }
}
