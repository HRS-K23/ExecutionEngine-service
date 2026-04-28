import { DataSource, Repository } from 'typeorm';
import { User, UserRole } from '../entities/User';
import { Logger } from '../logging/Logger';

export class UserRepository {
  private repository: Repository<User>;
  private logger = Logger.getInstance();

  constructor(dataSource: DataSource) {
    this.repository = dataSource.getRepository(User);
  }

  async findById(id: string): Promise<User | null> {
    try {
      return await this.repository.findOne({ where: { id } });
    } catch (error) {
      this.logger.error('UserRepository: Error finding by ID', { id, error });
      throw error;
    }
  }

  async findByEmail(email: string): Promise<User | null> {
    try {
      return await this.repository.findOne({ where: { email } });
    } catch (error) {
      this.logger.error('UserRepository: Error finding by email', { email, error });
      throw error;
    }
  }

  async findByRole(role: UserRole, limit: number = 50): Promise<User[]> {
    try {
      return await this.repository.find({
        where: { role, isActive: true },
        take: limit,
      });
    } catch (error) {
      this.logger.error('UserRepository: Error finding by role', { role, error });
      throw error;
    }
  }

  async findAll(limit: number = 50): Promise<User[]> {
    try {
      return await this.repository.find({
        take: limit,
        order: { createdAt: 'DESC' },
      });
    } catch (error) {
      this.logger.error('UserRepository: Error finding all', { error });
      throw error;
    }
  }

  async save(user: User): Promise<User> {
    try {
      return await this.repository.save(user);
    } catch (error) {
      this.logger.error('UserRepository: Error saving', { id: user.id, error });
      throw error;
    }
  }

  async update(id: string, updates: Partial<User>): Promise<void> {
    try {
      await this.repository.update({ id }, updates);
    } catch (error) {
      this.logger.error('UserRepository: Error updating', { id, error });
      throw error;
    }
  }

  async delete(id: string): Promise<void> {
    try {
      await this.repository.delete({ id });
    } catch (error) {
      this.logger.error('UserRepository: Error deleting', { id, error });
      throw error;
    }
  }

  async updateLastLogin(userId: string): Promise<void> {
    try {
      await this.repository.update({ id: userId }, { lastLoginAt: new Date() });
    } catch (error) {
      this.logger.error('UserRepository: Error updating last login', { userId, error });
      throw error;
    }
  }
}
