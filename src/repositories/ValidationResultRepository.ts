import { DataSource, Repository } from 'typeorm';
import { ValidationResult, ValidationStatus } from '../entities/ValidationResult';
import { Logger } from '../logging/Logger';

export class ValidationResultRepository {
  private repository: Repository<ValidationResult>;
  private logger = Logger.getInstance();

  constructor(dataSource: DataSource) {
    this.repository = dataSource.getRepository(ValidationResult);
  }

  async findById(id: string): Promise<ValidationResult | null> {
    try {
      return await this.repository.findOne({ where: { id } });
    } catch (error) {
      this.logger.error('ValidationResultRepository: Error finding by ID', { id, error });
      throw error;
    }
  }

  async findByQuestionId(questionId: string): Promise<ValidationResult | null> {
    try {
      return await this.repository.findOne({
        where: { questionId },
        order: { createdAt: 'DESC' },
      });
    } catch (error) {
      this.logger.error('ValidationResultRepository: Error finding by question', {
        questionId,
        error,
      });
      throw error;
    }
  }

  async findByStatus(status: ValidationStatus, limit: number = 10): Promise<ValidationResult[]> {
    try {
      return await this.repository.find({
        where: { status },
        take: limit,
        order: { createdAt: 'DESC' },
      });
    } catch (error) {
      this.logger.error('ValidationResultRepository: Error finding by status', { status, error });
      throw error;
    }
  }

  async save(result: ValidationResult): Promise<ValidationResult> {
    try {
      return await this.repository.save(result);
    } catch (error) {
      this.logger.error('ValidationResultRepository: Error saving', { id: result.id, error });
      throw error;
    }
  }

  async update(id: string, updates: Partial<ValidationResult>): Promise<void> {
    try {
      await this.repository.update({ id }, updates);
    } catch (error) {
      this.logger.error('ValidationResultRepository: Error updating', { id, error });
      throw error;
    }
  }

  async delete(id: string): Promise<void> {
    try {
      await this.repository.delete({ id });
    } catch (error) {
      this.logger.error('ValidationResultRepository: Error deleting', { id, error });
      throw error;
    }
  }
}
