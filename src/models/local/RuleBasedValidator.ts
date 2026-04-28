import {
  IValidator,
  ValidateQuestionInput,
  ValidationQuestionResult,
  ValidationRuleResult,
} from '../interfaces/IModels';
import { Logger } from '../../logging/Logger';

export class RuleBasedValidator implements IValidator {
  private logger = Logger.getInstance();

  private static readonly RULES = {
    RULE_1_STRUCTURE: 'Structure Check',
    RULE_2_TOPIC_ALIGNMENT: 'Topic Alignment',
    RULE_3_DIFFICULTY_CALIBRATION: 'Difficulty Calibration',
    RULE_4_CODE_SYNTAX: 'Code Syntax',
    RULE_5_LOGIC_CONSISTENCY: 'Logic Consistency',
    RULE_6_EDGE_CASE_COVERAGE: 'Edge Case Coverage',
  };

  async validate(input: ValidateQuestionInput): Promise<ValidationQuestionResult> {
    try {
      this.logger.info('RuleBasedValidator: Starting validation', {
        topic: input.topic,
        difficulty: input.difficulty,
      });

      const ruleResults: ValidationRuleResult[] = [];

      // Execute all rules
      ruleResults.push(this.checkStructure(input));
      ruleResults.push(this.checkTopicAlignment(input));
      ruleResults.push(this.checkDifficultCalibration(input));
      ruleResults.push(this.checkCodeSyntax(input));
      ruleResults.push(this.checkLogicConsistency(input));
      ruleResults.push(this.checkEdgeCaseCoverage(input));

      // Aggregate results
      const rulesPassed = ruleResults.filter((r) => r.status === 'PASS').length;
      const totalRules = ruleResults.length;
      const overallStatus =
        rulesPassed === totalRules
          ? 'PASS'
          : ruleResults.some((r) => r.status === 'FAIL')
            ? 'FAIL'
            : 'WARN';

      const result: ValidationQuestionResult = {
        status: overallStatus as 'PASS' | 'WARN' | 'FAIL',
        rulesPassed,
        totalRules,
        rules: ruleResults,
        notes: this.generateNotes(ruleResults, overallStatus),
      };

      this.logger.info('RuleBasedValidator: Validation complete', {
        status: result.status,
        rulesPassed,
        totalRules,
      });

      return result;
    } catch (error) {
      this.logger.error('RuleBasedValidator: Validation failed', { error });
      throw error;
    }
  }

  getName(): string {
    return 'RuleBasedValidator (Deterministic)';
  }

  async isAvailable(): Promise<boolean> {
    return true; // Always available in mock mode
  }

  private checkStructure(input: ValidateQuestionInput): ValidationRuleResult {
    const issues: string[] = [];

    if (!input.problemStatement || input.problemStatement.length < 20) {
      issues.push('Problem statement too short (min 20 chars)');
    }

    if (!input.sampleSolution || input.sampleSolution.length < 10) {
      issues.push('Sample solution too short (min 10 chars)');
    }

    if (!input.testCases || input.testCases.length < 5) {
      issues.push(`Insufficient test cases (found ${input.testCases?.length || 0}, min 5)`);
    }

    const status = issues.length === 0 ? 'PASS' : 'FAIL';

    return {
      id: 'RULE_1',
      name: RuleBasedValidator.RULES.RULE_1_STRUCTURE,
      description: 'Validates problem statement, solution, and test cases structure',
      status,
      message: issues.length > 0 ? issues.join('; ') : 'All structure checks passed',
      severity: 'ERROR',
    };
  }

  private checkTopicAlignment(input: ValidateQuestionInput): ValidationRuleResult {
    const topicKeywords: Record<string, string[]> = {
      Arrays: ['array', 'element', 'index', 'list', 'sorted'],
      Graphs: ['graph', 'vertex', 'edge', 'node', 'traverse'],
      'Dynamic Programming': ['dp', 'memoization', 'subproblem', 'recurrence', 'optimal'],
      Sorting: ['sort', 'order', 'compare', 'swap', 'algorithm'],
      Searching: ['search', 'find', 'lookup', 'index', 'binary'],
    };

    const problemLower = input.problemStatement.toLowerCase();
    const keywordList = topicKeywords[input.topic] || [];

    const matchedKeywords = keywordList.filter((kw) => problemLower.includes(kw));
    const matchPercentage = (matchedKeywords.length / Math.max(1, keywordList.length)) * 100;

    const status = matchPercentage >= 50 ? 'PASS' : matchPercentage >= 25 ? 'WARN' : 'FAIL';

    return {
      id: 'RULE_2',
      name: RuleBasedValidator.RULES.RULE_2_TOPIC_ALIGNMENT,
      description: 'Validates problem statement contains topic-related keywords',
      status,
      message: `Topic alignment: ${Math.round(matchPercentage)}% (${matchedKeywords.length}/${keywordList.length} keywords found)`,
      severity: status === 'FAIL' ? 'ERROR' : 'WARNING',
    };
  }

  private checkDifficultCalibration(input: ValidateQuestionInput): ValidationRuleResult {
    const difficultyRules: Record<string, { minWords: number; maxWords: number; minLines: number; maxLines: number }> = {
      EASY: { minWords: 10, maxWords: 150, minLines: 1, maxLines: 20 },
      MEDIUM: { minWords: 100, maxWords: 300, minLines: 15, maxLines: 50 },
      HARD: { minWords: 150, maxWords: 500, minLines: 30, maxLines: 100 },
    };

    const rule = difficultyRules[input.difficulty];
    if (!rule) {
      return {
        id: 'RULE_3',
        name: RuleBasedValidator.RULES.RULE_3_DIFFICULTY_CALIBRATION,
        description: 'Validates difficulty calibration with problem and solution length',
        status: 'WARN',
        message: `Unknown difficulty: ${input.difficulty}`,
        severity: 'WARNING',
      };
    }

    const problemWords = input.problemStatement.split(/\s+/).length;
    const solutionLines = input.sampleSolution.split('\n').length;

    const wordCheck = problemWords >= rule.minWords && problemWords <= rule.maxWords;
    const lineCheck = solutionLines >= rule.minLines && solutionLines <= rule.maxLines;

    const status = wordCheck && lineCheck ? 'PASS' : 'WARN';

    return {
      id: 'RULE_3',
      name: RuleBasedValidator.RULES.RULE_3_DIFFICULTY_CALIBRATION,
      description: 'Validates difficulty calibration with problem and solution length',
      status,
      message: `${input.difficulty}: ${problemWords} words (expect ${rule.minWords}-${rule.maxWords}), ${solutionLines} lines (expect ${rule.minLines}-${rule.maxLines})`,
      severity: status === 'FAIL' ? 'ERROR' : 'WARNING',
    };
  }

  private checkCodeSyntax(input: ValidateQuestionInput): ValidationRuleResult {
    const issues: string[] = [];

    // Basic syntax checks
    if (!this.hasValidSyntax(input.sampleSolution)) {
      issues.push('Solution contains unmatched brackets or quotes');
    }

    if (!input.sampleSolution.includes('def ') && 
        !input.sampleSolution.includes('function ') &&
        !input.sampleSolution.includes('class ') &&
        input.language !== 'pseudocode') {
      issues.push('Solution should contain a function or class definition');
    }

    const status = issues.length === 0 ? 'PASS' : 'WARN';

    return {
      id: 'RULE_4',
      name: RuleBasedValidator.RULES.RULE_4_CODE_SYNTAX,
      description: 'Attempts to validate sample solution syntax',
      status,
      message: issues.length > 0 ? issues.join('; ') : 'Syntax checks passed',
      severity: 'ERROR',
    };
  }

  private checkLogicConsistency(input: ValidateQuestionInput): ValidationRuleResult {
    const testCases = input.testCases || [];
    const issues: string[] = [];

    // Check that test cases have inputs and outputs
    const validTestCases = testCases.filter((tc) => tc.testInput && tc.expectedOutput);
    if (validTestCases.length < testCases.length) {
      issues.push(`${testCases.length - validTestCases.length} test cases missing input or output`);
    }

    // Check for variety in test cases
    const caseTypes = new Set(testCases.map((tc) => tc.caseType));
    if (caseTypes.size < 3) {
      issues.push(`Limited test case variety (${caseTypes.size}/5 types covered)`);
    }

    const status = issues.length === 0 ? 'PASS' : 'WARN';

    return {
      id: 'RULE_5',
      name: RuleBasedValidator.RULES.RULE_5_LOGIC_CONSISTENCY,
      description: 'Validates logic consistency and test case quality',
      status,
      message: issues.length > 0 ? issues.join('; ') : 'Logic consistency checks passed',
      severity: 'WARNING',
    };
  }

  private checkEdgeCaseCoverage(input: ValidateQuestionInput): ValidationRuleResult {
    const testCases = input.testCases || [];
    const caseTypes = new Set(testCases.map((tc) => tc.caseType));

    const requiredTypes = ['NORMAL', 'EDGE', 'BOUNDARY'];
    const missingTypes = requiredTypes.filter((type) => !caseTypes.has(type));

    const status = missingTypes.length === 0 ? 'PASS' : missingTypes.length === 1 ? 'WARN' : 'FAIL';

    return {
      id: 'RULE_6',
      name: RuleBasedValidator.RULES.RULE_6_EDGE_CASE_COVERAGE,
      description: 'Validates coverage of normal, edge, and boundary cases',
      status,
      message: 
        missingTypes.length === 0
          ? `Full coverage: ${Array.from(caseTypes).join(', ')}`
          : `Missing case types: ${missingTypes.join(', ')}`,
      severity: missingTypes.length === 1 ? 'WARNING' : 'ERROR',
    };
  }

  private hasValidSyntax(code: string): boolean {
    let bracketCount = 0;
    let parenCount = 0;
    let squareCount = 0;
    let quoteCount = 0;

    for (let i = 0; i < code.length; i++) {
      const char = code[i];
      if (char === '{') bracketCount++;
      if (char === '}') bracketCount--;
      if (char === '(') parenCount++;
      if (char === ')') parenCount--;
      if (char === '[') squareCount++;
      if (char === ']') squareCount--;
      if (char === '"' || char === "'") quoteCount++;
    }

    return bracketCount === 0 && parenCount === 0 && squareCount === 0 && quoteCount % 2 === 0;
  }

  private generateNotes(rules: ValidationRuleResult[], status: string): string {
    if (status === 'PASS') {
      return 'All validation rules passed. Question is ready for publication.';
    }

    const failedRules = rules.filter((r) => r.status === 'FAIL' || r.status === 'WARN');
    const issues = failedRules.map((r) => `${r.name}: ${r.message}`);

    return `Validation issues found: ${issues.join('; ')}`;
  }
}
