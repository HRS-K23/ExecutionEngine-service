import { DifficultyLevel } from '../entities/Question';

export interface GenerateQuestionInput {
  topic: string;
  subtopic: string;
  difficulty: DifficultyLevel;
  language: string;
  title: string;
  description?: string;
  seed?: number;
}

export interface GeneratedQuestion {
  problemStatement: string;
  sampleSolution: string;
  sampleSolutionExplanation?: Record<string, any>;
}

export interface IQuestionGenerator {
  generate(input: GenerateQuestionInput): Promise<GeneratedQuestion>;
  getName(): string;
  isAvailable(): Promise<boolean>;
}

export interface GenerateTestCasesInput {
  problemDescription: string;
  sampleSolution: string;
  difficulty: DifficultyLevel;
  language: string;
  testCaseCount?: number;
}

export interface GeneratedTestCase {
  testInput: string;
  expectedOutput: string;
  caseType: 'NORMAL' | 'EDGE' | 'BOUNDARY' | 'ERROR' | 'STRESS';
  visible: boolean;
  explanation?: string;
  difficulty?: string;
}

export interface GeneratedTestCases {
  visibleTests: GeneratedTestCase[];
  hiddenTests: GeneratedTestCase[];
  testMetadata?: Record<string, any>;
}

export interface ITestCaseGenerator {
  generate(input: GenerateTestCasesInput): Promise<GeneratedTestCases>;
  getName(): string;
  isAvailable(): Promise<boolean>;
}

export interface ValidateQuestionInput {
  topic: string;
  subtopic: string;
  difficulty: DifficultyLevel;
  language: string;
  problemStatement: string;
  sampleSolution: string;
  testCases: GeneratedTestCase[];
}

export interface ValidationRuleResult {
  id: string;
  name: string;
  description: string;
  status: 'PASS' | 'WARN' | 'FAIL';
  message?: string;
  severity?: 'INFO' | 'WARNING' | 'ERROR';
}

export interface ValidationQuestionResult {
  status: 'PASS' | 'WARN' | 'FAIL';
  rulesPassed: number;
  totalRules: number;
  rules: ValidationRuleResult[];
  notes?: string;
}

export interface IValidator {
  validate(input: ValidateQuestionInput): Promise<ValidationQuestionResult>;
  getName(): string;
  isAvailable(): Promise<boolean>;
}

export interface IModel {
  getName(): string;
  isAvailable(): Promise<boolean>;
}
