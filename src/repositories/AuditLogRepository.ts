import { DataSource, Repository } from 'typeorm';
import { AuditLog, AuditActionType } from '../entities/AuditLog';
import { Logger } from '../logging/Logger';

export class AuditLogRepository {
  private repository: Repository<AuditLog>;
  private logger = Logger.getInstance();

  constructor(dataSource: DataSource) {
    this.repository = dataSource.getRepository(AuditLog);
  }

  async findById(id: string): Promise<AuditLog | null> {
    try {
      return await this.repository.findOne({ where: { id } });
    } catch (error) {
      this.logger.error('AuditLogRepository: Error finding by ID', { id, error });
      throw error;
    }
  }

  async findByEntityId(entityId: string, limit: number = 50): Promise<AuditLog[]> {
    try {
      return await this.repository.find({
        where: { entityId },
        take: limit,
        order: { createdAt: 'DESC' },
      });
    } catch (error) {
      this.logger.error('AuditLogRepository: Error finding by entity', { entityId, error });
      throw error;
    }
  }

  async findByUserId(userId: string, limit: number = 50): Promise<AuditLog[]> {
    try {
      return await this.repository.find({
        where: { userId },
        take: limit,
        order: { createdAt: 'DESC' },
      });
    } catch (error) {
      this.logger.error('AuditLogRepository: Error finding by user', { userId, error });
      throw error;
    }
  }

  async findByActionType(actionType: AuditActionType, limit: number = 50): Promise<AuditLog[]> {
    try {
      return await this.repository.find({
        where: { actionType },
        take: limit,
        order: { createdAt: 'DESC' },
      });
    } catch (error) {
      this.logger.error('AuditLogRepository: Error finding by action', { actionType, error });
      throw error;
    }
  }

  async save(log: AuditLog): Promise<AuditLog> {
    try {
      return await this.repository.save(log);
    } catch (error) {
      this.logger.error('AuditLogRepository: Error saving', { id: log.id, error });
      throw error;
    }
  }

  async findAll(limit: number = 100, offset: number = 0): Promise<AuditLog[]> {
    try {
      return await this.repository.find({
        take: limit,
        skip: offset,
        order: { createdAt: 'DESC' },
      });
    } catch (error) {
      this.logger.error('AuditLogRepository: Error finding all', { error });
      throw error;
    }
  }
}
