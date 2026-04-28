import { DataSource, Repository } from 'typeorm';
import { PromptTemplate } from '../entities/PromptTemplate';
import { Logger } from '../logging/Logger';

export class PromptTemplateRepository {
  private repository: Repository<PromptTemplate>;
  private logger = Logger.getInstance();

  constructor(dataSource: DataSource) {
    this.repository = dataSource.getRepository(PromptTemplate);
  }

  async findById(id: string): Promise<PromptTemplate | null> {
    try {
      return await this.repository.findOne({ where: { id } });
    } catch (error) {
      this.logger.error('PromptTemplateRepository: Error finding by ID', { id, error });
      throw error;
    }
  }

  async findByTopicAndSubtopic(
    topic: string,
    subtopic: string,
    difficulty: string,
    language: string
  ): Promise<PromptTemplate | null> {
    try {
      return await this.repository.findOne({
        where: { topic, subtopic, difficulty, language, status: 'ACTIVE' },
      });
    } catch (error) {
      this.logger.error('PromptTemplateRepository: Error finding by topic/subtopic', {
        topic,
        subtopic,
        error,
      });
      throw error;
    }
  }

  async findByTopic(topic: string, status: string = 'ACTIVE'): Promise<PromptTemplate[]> {
    try {
      return await this.repository.find({
        where: { topic, status },
        order: { createdAt: 'DESC' },
      });
    } catch (error) {
      this.logger.error('PromptTemplateRepository: Error finding by topic', { topic, error });
      throw error;
    }
  }

  async findAll(status: string = 'ACTIVE'): Promise<PromptTemplate[]> {
    try {
      return await this.repository.find({
        where: { status },
        order: { createdAt: 'DESC' },
      });
    } catch (error) {
      this.logger.error('PromptTemplateRepository: Error finding all', { error });
      throw error;
    }
  }

  async save(template: PromptTemplate): Promise<PromptTemplate> {
    try {
      return await this.repository.save(template);
    } catch (error) {
      this.logger.error('PromptTemplateRepository: Error saving', { id: template.id, error });
      throw error;
    }
  }

  async update(id: string, updates: Partial<PromptTemplate>): Promise<void> {
    try {
      await this.repository.update({ id }, updates);
    } catch (error) {
      this.logger.error('PromptTemplateRepository: Error updating', { id, error });
      throw error;
    }
  }

  async delete(id: string): Promise<void> {
    try {
      await this.repository.delete({ id });
    } catch (error) {
      this.logger.error('PromptTemplateRepository: Error deleting', { id, error });
      throw error;
    }
  }

  async incrementUsageCount(id: string): Promise<void> {
    try {
      await this.repository.increment({ id }, 'usageCount', 1);
      await this.repository.update({ id }, { lastUsedAt: new Date() });
    } catch (error) {
      this.logger.error('PromptTemplateRepository: Error incrementing usage', { id, error });
      throw error;
    }
  }
}
