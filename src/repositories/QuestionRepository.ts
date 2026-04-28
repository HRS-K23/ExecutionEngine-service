import { DataSource, Repository } from 'typeorm';
import { Question, DifficultyLevel, QuestionStatus } from '../entities/Question';
import { Logger } from '../logging/Logger';

export class QuestionRepository {
  private repository: Repository<Question>;
  private logger = Logger.getInstance();

  constructor(dataSource: DataSource) {
    this.repository = dataSource.getRepository(Question);
  }

  async findById(id: string): Promise<Question | null> {
    try {
      return await this.repository.findOne({
        where: { id },
        relations: ['testCases', 'validationResults'],
      });
    } catch (error) {
      this.logger.error('QuestionRepository: Error finding question by ID', { id, error });
      throw error;
    }
  }

  async findByTopic(topic: string, limit: number = 10, offset: number = 0): Promise<Question[]> {
    try {
      return await this.repository.find({
        where: { topic },
        take: limit,
        skip: offset,
        order: { createdAt: 'DESC' },
      });
    } catch (error) {
      this.logger.error('QuestionRepository: Error finding questions by topic', { topic, error });
      throw error;
    }
  }

  async findByTopicAndSubtopic(
    topic: string,
    subtopic: string,
    limit: number = 10
  ): Promise<Question[]> {
    try {
      return await this.repository.find({
        where: { topic, subtopic },
        take: limit,
        order: { createdAt: 'DESC' },
      });
    } catch (error) {
      this.logger.error('QuestionRepository: Error finding questions by topic and subtopic', {
        topic,
        subtopic,
        error,
      });
      throw error;
    }
  }

  async findByDifficulty(difficulty: DifficultyLevel, limit: number = 10): Promise<Question[]> {
    try {
      return await this.repository.find({
        where: { difficulty },
        take: limit,
        order: { createdAt: 'DESC' },
      });
    } catch (error) {
      this.logger.error('QuestionRepository: Error finding questions by difficulty', {
        difficulty,
        error,
      });
      throw error;
    }
  }

  async findByStatus(status: QuestionStatus, limit: number = 10): Promise<Question[]> {
    try {
      return await this.repository.find({
        where: { status },
        take: limit,
        order: { createdAt: 'DESC' },
      });
    } catch (error) {
      this.logger.error('QuestionRepository: Error finding questions by status', { status, error });
      throw error;
    }
  }

  async findAll(limit: number = 10, offset: number = 0): Promise<Question[]> {
    try {
      return await this.repository.find({
        take: limit,
        skip: offset,
        order: { createdAt: 'DESC' },
      });
    } catch (error) {
      this.logger.error('QuestionRepository: Error finding all questions', { error });
      throw error;
    }
  }

  async count(): Promise<number> {
    try {
      return await this.repository.count();
    } catch (error) {
      this.logger.error('QuestionRepository: Error counting questions', { error });
      throw error;
    }
  }

  async save(question: Question): Promise<Question> {
    try {
      return await this.repository.save(question);
    } catch (error) {
      this.logger.error('QuestionRepository: Error saving question', { id: question.id, error });
      throw error;
    }
  }

  async update(id: string, updates: Partial<Question>): Promise<void> {
    try {
      await this.repository.update({ id }, updates);
    } catch (error) {
      this.logger.error('QuestionRepository: Error updating question', { id, error });
      throw error;
    }
  }

  async delete(id: string): Promise<void> {
    try {
      await this.repository.delete({ id });
    } catch (error) {
      this.logger.error('QuestionRepository: Error deleting question', { id, error });
      throw error;
    }
  }

  async findByCreatedBy(userId: string, limit: number = 10): Promise<Question[]> {
    try {
      return await this.repository.find({
        where: { createdBy: userId },
        take: limit,
        order: { createdAt: 'DESC' },
      });
    } catch (error) {
      this.logger.error('QuestionRepository: Error finding questions by creator', { userId, error });
      throw error;
    }
  }

  async search(searchTerm: string, limit: number = 10): Promise<Question[]> {
    try {
      return await this.repository
        .createQueryBuilder('q')
        .where('q.title ILIKE :term', { term: `%${searchTerm}%` })
        .orWhere('q.problemStatement ILIKE :term', { term: `%${searchTerm}%` })
        .orWhere('q.topic ILIKE :term', { term: `%${searchTerm}%` })
        .take(limit)
        .orderBy('q.createdAt', 'DESC')
        .getMany();
    } catch (error) {
      this.logger.error('QuestionRepository: Error searching questions', { searchTerm, error });
      throw error;
    }
  }
}
