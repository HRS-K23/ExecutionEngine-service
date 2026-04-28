import { DifficultyLevel } from '../entities/Question';

export class CreateQuestionDTO {
  topic: string;
  subtopic: string;
  difficulty: DifficultyLevel;
  language: string;
  title: string;
  description?: string;
}

export class UpdateQuestionDTO {
  problemStatement?: string;
  sampleSolution?: string;
  sampleSolutionExplanation?: Record<string, any>;
  title?: string;
  testCases?: any[];
  metadata?: Record<string, any>;
}

export class PublishQuestionDTO {
  validateBeforePublish?: boolean;
}

export class QuestionResponseDTO {
  id: string;
  topic: string;
  subtopic: string;
  difficulty: DifficultyLevel;
  language: string;
  title: string;
  problemStatement: string;
  sampleSolution: string;
  testCases: any[];
  validationResult?: any;
  status: string;
  createdAt: Date;
  updatedAt: Date;
  publishedAt?: Date;
}

export class TestCaseDTO {
  id: string;
  testInput: string;
  expectedOutput: string;
  caseType: string;
  visible: boolean;
  explanation?: string;
  difficulty?: string;
  executionTimeMs?: number;
  memoryUsageMb?: number;
}

export class ValidationResultDTO {
  id: string;
  questionId: string;
  status: string;
  rulesPassed: number;
  totalRules: number;
  rules: any[];
  notes?: string;
  createdAt: Date;
  updatedAt: Date;
}

export class ValidateQuestionDTO {
  force?: boolean;
  skipRules?: string[];
}

export class ApiResponseDTO<T> {
  success: boolean;
  data?: T;
  errors?: ApiErrorDTO[];
  meta: {
    timestamp: string;
    requestId: string;
    version: string;
  };
}

export class ApiErrorDTO {
  code: string;
  message: string;
  details?: Record<string, any>;
}
