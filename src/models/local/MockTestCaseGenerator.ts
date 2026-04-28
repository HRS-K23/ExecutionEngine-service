import {
  ITestCaseGenerator,
  GenerateTestCasesInput,
  GeneratedTestCases,
  GeneratedTestCase,
} from '../interfaces/IModels';
import { DifficultyLevel } from '../../entities/Question';
import { Logger } from '../../logging/Logger';

export class MockTestCaseGenerator implements ITestCaseGenerator {
  private logger = Logger.getInstance();

  async generate(input: GenerateTestCasesInput): Promise<GeneratedTestCases> {
    try {
      this.logger.info('MockTestCaseGenerator: Generating test cases', {
        difficulty: input.difficulty,
        language: input.language,
        testCaseCount: input.testCaseCount || 8,
      });

      const testCaseCount = input.testCaseCount || 8;
      const visibleCount = Math.ceil(testCaseCount / 2);
      const hiddenCount = testCaseCount - visibleCount;

      const allTestCases = this.generateTestCasesFromPatterns(
        input.problemDescription,
        input.sampleSolution,
        input.difficulty,
        testCaseCount
      );

      return {
        visibleTests: allTestCases.slice(0, visibleCount).map((tc) => ({
          ...tc,
          visible: true,
        })),
        hiddenTests: allTestCases.slice(visibleCount).map((tc) => ({
          ...tc,
          visible: false,
        })),
        testMetadata: {
          generatedAt: new Date().toISOString(),
          totalCount: testCaseCount,
          visibleCount,
          hiddenCount,
          language: input.language,
          difficulty: input.difficulty,
        },
      };
    } catch (error) {
      this.logger.error('MockTestCaseGenerator: Failed to generate test cases', { error });
      throw error;
    }
  }

  getName(): string {
    return 'MockTestCaseGenerator (Pattern-based)';
  }

  async isAvailable(): Promise<boolean> {
    return true; // Always available in mock mode
  }

  private generateTestCasesFromPatterns(
    problemDescription: string,
    sampleSolution: string,
    difficulty: DifficultyLevel,
    count: number
  ): GeneratedTestCase[] {
    const testCases: GeneratedTestCase[] = [];

    // Generate normal cases
    testCases.push(...this.generateNormalCases(problemDescription, 2));

    // Generate edge cases
    testCases.push(...this.generateEdgeCases(problemDescription, 2));

    // Generate boundary cases
    testCases.push(...this.generateBoundaryCases(problemDescription, 1));

    // Generate error cases
    testCases.push(...this.generateErrorCases(problemDescription, 1));

    // For HARD difficulty, add stress cases
    if (difficulty === DifficultyLevel.HARD) {
      testCases.push(...this.generateStressCases(problemDescription, 2));
    }

    return testCases.slice(0, count);
  }

  private generateNormalCases(description: string, count: number): GeneratedTestCase[] {
    const cases: GeneratedTestCase[] = [];

    if (description.toLowerCase().includes('array') || description.toLowerCase().includes('sort')) {
      cases.push({
        testInput: '[64, 34, 25, 12, 22, 11, 90]',
        expectedOutput: '[11, 12, 22, 25, 34, 64, 90]',
        caseType: 'NORMAL',
        visible: true,
        explanation: 'Normal case: sorting an unsorted array',
        difficulty: 'EASY',
      });

      if (count > 1) {
        cases.push({
          testInput: '[5, 2, 8, 1, 9]',
          expectedOutput: '[1, 2, 5, 8, 9]',
          caseType: 'NORMAL',
          visible: true,
          explanation: 'Normal case: smaller array',
          difficulty: 'EASY',
        });
      }
    } else if (description.toLowerCase().includes('search')) {
      cases.push({
        testInput: 'arr=[1,3,5,7,9], target=5',
        expectedOutput: '2',
        caseType: 'NORMAL',
        visible: true,
        explanation: 'Normal case: element found at index 2',
        difficulty: 'EASY',
      });

      if (count > 1) {
        cases.push({
          testInput: 'arr=[10,20,30,40], target=20',
          expectedOutput: '1',
          caseType: 'NORMAL',
          visible: true,
          explanation: 'Normal case: element at index 1',
          difficulty: 'EASY',
        });
      }
    } else {
      // Generic test cases
      for (let i = 0; i < count; i++) {
        cases.push({
          testInput: `input_${i + 1}`,
          expectedOutput: `output_${i + 1}`,
          caseType: 'NORMAL',
          visible: true,
          explanation: `Normal test case ${i + 1}`,
          difficulty: 'EASY',
        });
      }
    }

    return cases.slice(0, count);
  }

  private generateEdgeCases(description: string, count: number): GeneratedTestCase[] {
    const cases: GeneratedTestCase[] = [];

    // Empty input edge case
    cases.push({
      testInput: '[]',
      expectedOutput: '[]',
      caseType: 'EDGE',
      visible: false,
      explanation: 'Edge case: empty input',
      difficulty: 'EASY',
    });

    // Single element edge case
    if (count > 1) {
      cases.push({
        testInput: '[42]',
        expectedOutput: '[42]',
        caseType: 'EDGE',
        visible: false,
        explanation: 'Edge case: single element',
        difficulty: 'EASY',
      });
    }

    // Null/None case
    if (count > 2) {
      cases.push({
        testInput: 'null',
        expectedOutput: 'null or empty',
        caseType: 'EDGE',
        visible: false,
        explanation: 'Edge case: null input',
        difficulty: 'EASY',
      });
    }

    return cases.slice(0, count);
  }

  private generateBoundaryCases(description: string, count: number): GeneratedTestCase[] {
    const cases: GeneratedTestCase[] = [];

    // Minimum boundary
    cases.push({
      testInput: '[1]',
      expectedOutput: '1 or [1]',
      caseType: 'BOUNDARY',
      visible: false,
      explanation: 'Boundary case: minimum value',
      difficulty: 'MEDIUM',
    });

    // Maximum boundary
    if (count > 1) {
      cases.push({
        testInput: 'max_int_value',
        expectedOutput: 'max_int_value',
        caseType: 'BOUNDARY',
        visible: false,
        explanation: 'Boundary case: maximum value',
        difficulty: 'MEDIUM',
      });
    }

    return cases.slice(0, count);
  }

  private generateErrorCases(description: string, count: number): GeneratedTestCase[] {
    const cases: GeneratedTestCase[] = [];

    cases.push({
      testInput: 'invalid_input_type',
      expectedOutput: 'Error or exception',
      caseType: 'ERROR',
      visible: false,
      explanation: 'Error case: invalid input type',
      difficulty: 'MEDIUM',
    });

    if (count > 1) {
      cases.push({
        testInput: 'None',
        expectedOutput: 'Error or exception',
        caseType: 'ERROR',
        visible: false,
        explanation: 'Error case: None/null input',
        difficulty: 'MEDIUM',
      });
    }

    return cases.slice(0, count);
  }

  private generateStressCases(description: string, count: number): GeneratedTestCase[] {
    const cases: GeneratedTestCase[] = [];

    // Large input stress case
    cases.push({
      testInput: '[' + Array.from({ length: 1000 }, (_, i) => Math.floor(Math.random() * 10000)).join(',') + ']',
      expectedOutput: '[sorted_large_array]',
      caseType: 'STRESS',
      visible: false,
      explanation: 'Stress case: large input (1000 elements)',
      difficulty: 'HARD',
    });

    if (count > 1) {
      cases.push({
        testInput: '[' + Array.from({ length: 10000 }, (_, i) => i).join(',') + ']',
        expectedOutput: '[0,1,2,...,9999]',
        caseType: 'STRESS',
        visible: false,
        explanation: 'Stress case: very large input (10000 elements)',
        difficulty: 'HARD',
      });
    }

    return cases.slice(0, count);
  }
}
