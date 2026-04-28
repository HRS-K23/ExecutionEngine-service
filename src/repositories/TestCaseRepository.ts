import { DataSource, Repository } from 'typeorm';
import { TestCase } from '../entities/TestCase';
import { Logger } from '../logging/Logger';

export class TestCaseRepository {
  private repository: Repository<TestCase>;
  private logger = Logger.getInstance();

  constructor(dataSource: DataSource) {
    this.repository = dataSource.getRepository(TestCase);
  }

  async findById(id: string): Promise<TestCase | null> {
    try {
      return await this.repository.findOne({ where: { id } });
    } catch (error) {
      this.logger.error('TestCaseRepository: Error finding test case by ID', { id, error });
      throw error;
    }
  }

  async findByQuestionId(questionId: string): Promise<TestCase[]> {
    try {
      return await this.repository.find({
        where: { questionId },
        order: { createdAt: 'ASC' },
      });
    } catch (error) {
      this.logger.error('TestCaseRepository: Error finding test cases by question', {
        questionId,
        error,
      });
      throw error;
    }
  }

  async findVisibleByQuestionId(questionId: string): Promise<TestCase[]> {
    try {
      return await this.repository.find({
        where: { questionId, visible: true },
        order: { createdAt: 'ASC' },
      });
    } catch (error) {
      this.logger.error('TestCaseRepository: Error finding visible test cases', {
        questionId,
        error,
      });
      throw error;
    }
  }

  async findHiddenByQuestionId(questionId: string): Promise<TestCase[]> {
    try {
      return await this.repository.find({
        where: { questionId, visible: false },
        order: { createdAt: 'ASC' },
      });
    } catch (error) {
      this.logger.error('TestCaseRepository: Error finding hidden test cases', {
        questionId,
        error,
      });
      throw error;
    }
  }

  async findByCaseType(
    questionId: string,
    caseType: 'NORMAL' | 'EDGE' | 'BOUNDARY' | 'ERROR' | 'STRESS'
  ): Promise<TestCase[]> {
    try {
      return await this.repository.find({
        where: { questionId, caseType },
        order: { createdAt: 'ASC' },
      });
    } catch (error) {
      this.logger.error('TestCaseRepository: Error finding test cases by type', {
        questionId,
        caseType,
        error,
      });
      throw error;
    }
  }

  async save(testCase: TestCase): Promise<TestCase> {
    try {
      return await this.repository.save(testCase);
    } catch (error) {
      this.logger.error('TestCaseRepository: Error saving test case', { id: testCase.id, error });
      throw error;
    }
  }

  async saveMany(testCases: TestCase[]): Promise<TestCase[]> {
    try {
      return await this.repository.save(testCases);
    } catch (error) {
      this.logger.error('TestCaseRepository: Error saving multiple test cases', { error });
      throw error;
    }
  }

  async update(id: string, updates: Partial<TestCase>): Promise<void> {
    try {
      await this.repository.update({ id }, updates);
    } catch (error) {
      this.logger.error('TestCaseRepository: Error updating test case', { id, error });
      throw error;
    }
  }

  async delete(id: string): Promise<void> {
    try {
      await this.repository.delete({ id });
    } catch (error) {
      this.logger.error('TestCaseRepository: Error deleting test case', { id, error });
      throw error;
    }
  }

  async deleteByQuestionId(questionId: string): Promise<void> {
    try {
      await this.repository.delete({ questionId });
    } catch (error) {
      this.logger.error('TestCaseRepository: Error deleting test cases by question', {
        questionId,
        error,
      });
      throw error;
    }
  }

  async countByQuestionId(questionId: string): Promise<number> {
    try {
      return await this.repository.count({ where: { questionId } });
    } catch (error) {
      this.logger.error('TestCaseRepository: Error counting test cases', { questionId, error });
      throw error;
    }
  }
}
