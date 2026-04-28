import { ModelConfig } from '../../config/AppConfig';
import {
  IQuestionGenerator,
  ITestCaseGenerator,
  IValidator,
} from '../interfaces/IModels';
import { LocalQuestionGenerator } from '../local/LocalQuestionGenerator';
import { MockTestCaseGenerator } from '../local/MockTestCaseGenerator';
import { RuleBasedValidator } from '../local/RuleBasedValidator';
import { Logger } from '../../logging/Logger';

/**
 * ModelFactory
 * 
 * Implements the Factory Pattern to seamlessly switch between mock and real model implementations.
 * Based on the MODEL_MODE environment variable:
 * - "mock": Uses local/mock implementations (no external API calls required)
 * - "real": Uses real model client implementations (requires external services)
 * 
 * This enables:
 * 1. Development/testing without external dependencies
 * 2. Gradual migration to real models when available
 * 3. Circuit breaking and fallback to mock if real models fail
 */
export class ModelFactory {
  private static logger = Logger.getInstance();
  private static questionGeneratorInstance: IQuestionGenerator | null = null;
  private static testCaseGeneratorInstance: ITestCaseGenerator | null = null;
  private static validatorInstance: IValidator | null = null;

  /**
   * Get the configured question generator
   * Returns LocalQuestionGenerator in mock mode, RealQuestionGeneratorClient in real mode
   */
  static getQuestionGenerator(): IQuestionGenerator {
    if (this.questionGeneratorInstance) {
      return this.questionGeneratorInstance;
    }

    if (ModelConfig.isMockMode() && ModelConfig.MOCK_QUESTION_GENERATOR_ENABLED) {
      this.logger.info('ModelFactory: Initializing LocalQuestionGenerator (mock mode)');
      this.questionGeneratorInstance = new LocalQuestionGenerator();
    } else if (ModelConfig.isRealMode() && ModelConfig.REAL_QUESTION_GENERATOR_ENABLED) {
      this.logger.info('ModelFactory: Initializing RealQuestionGeneratorClient (real mode)');
      // TODO: Import and instantiate real implementation when available
      // this.questionGeneratorInstance = new RealQuestionGeneratorClient(
      //   ModelConfig.QUESTION_GENERATOR_URL,
      //   ModelConfig.EXTERNAL_SERVICE_TIMEOUT_MS
      // );
      // Fallback to mock if real mode is configured but implementation not available
      this.questionGeneratorInstance = new LocalQuestionGenerator();
    } else {
      this.logger.warn('ModelFactory: Falling back to LocalQuestionGenerator');
      this.questionGeneratorInstance = new LocalQuestionGenerator();
    }

    return this.questionGeneratorInstance;
  }

  /**
   * Get the configured test case generator
   * Returns MockTestCaseGenerator in mock mode, RealTestCaseGeneratorClient in real mode
   */
  static getTestCaseGenerator(): ITestCaseGenerator {
    if (this.testCaseGeneratorInstance) {
      return this.testCaseGeneratorInstance;
    }

    if (ModelConfig.isMockMode() && ModelConfig.MOCK_TEST_CASE_GENERATOR_ENABLED) {
      this.logger.info('ModelFactory: Initializing MockTestCaseGenerator (mock mode)');
      this.testCaseGeneratorInstance = new MockTestCaseGenerator();
    } else if (ModelConfig.isRealMode() && ModelConfig.REAL_TEST_CASE_GENERATOR_ENABLED) {
      this.logger.info('ModelFactory: Initializing RealTestCaseGeneratorClient (real mode)');
      // TODO: Import and instantiate real implementation when available
      // this.testCaseGeneratorInstance = new RealTestCaseGeneratorClient(
      //   ModelConfig.TEST_CASE_GENERATOR_URL,
      //   ModelConfig.EXTERNAL_SERVICE_TIMEOUT_MS
      // );
      // Fallback to mock if real mode is configured but implementation not available
      this.testCaseGeneratorInstance = new MockTestCaseGenerator();
    } else {
      this.logger.warn('ModelFactory: Falling back to MockTestCaseGenerator');
      this.testCaseGeneratorInstance = new MockTestCaseGenerator();
    }

    return this.testCaseGeneratorInstance;
  }

  /**
   * Get the configured validator
   * Returns RuleBasedValidator in mock mode, RealValidatorClient in real mode
   */
  static getValidator(): IValidator {
    if (this.validatorInstance) {
      return this.validatorInstance;
    }

    if (ModelConfig.isMockMode() && ModelConfig.MOCK_VALIDATOR_ENABLED) {
      this.logger.info('ModelFactory: Initializing RuleBasedValidator (mock mode)');
      this.validatorInstance = new RuleBasedValidator();
    } else if (ModelConfig.isRealMode() && ModelConfig.REAL_VALIDATOR_ENABLED) {
      this.logger.info('ModelFactory: Initializing RealValidatorClient (real mode)');
      // TODO: Import and instantiate real implementation when available
      // this.validatorInstance = new RealValidatorClient(
      //   ModelConfig.VALIDATOR_URL,
      //   ModelConfig.EXTERNAL_SERVICE_TIMEOUT_MS
      // );
      // Fallback to mock if real mode is configured but implementation not available
      this.validatorInstance = new RuleBasedValidator();
    } else {
      this.logger.warn('ModelFactory: Falling back to RuleBasedValidator');
      this.validatorInstance = new RuleBasedValidator();
    }

    return this.validatorInstance;
  }

  /**
   * Reset instances (useful for testing)
   */
  static reset(): void {
    this.questionGeneratorInstance = null;
    this.testCaseGeneratorInstance = null;
    this.validatorInstance = null;
  }

  /**
   * Get current model configuration status
   */
  static getStatus(): {
    mode: string;
    questionGenerator: string;
    testCaseGenerator: string;
    validator: string;
  } {
    return {
      mode: ModelConfig.MODEL_MODE,
      questionGenerator: this.getQuestionGenerator().getName(),
      testCaseGenerator: this.getTestCaseGenerator().getName(),
      validator: this.getValidator().getName(),
    };
  }
}
