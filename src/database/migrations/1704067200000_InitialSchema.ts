import { MigrationInterface, QueryRunner, Table, TableIndex } from 'typeorm';

export class InitialSchema1704067200000 implements MigrationInterface {
  public async up(queryRunner: QueryRunner): Promise<void> {
    // Create users table
    await queryRunner.createTable(
      new Table({
        name: 'users',
        columns: [
          { name: 'id', type: 'uuid', isPrimary: true },
          { name: 'email', type: 'varchar', length: '255', isUnique: true },
          { name: 'firstName', type: 'varchar', length: '255' },
          { name: 'lastName', type: 'varchar', length: '255' },
          { name: 'passwordHash', type: 'varchar', length: '255', isNullable: true },
          { name: 'role', type: 'enum', enum: ['ADMIN', 'EDITOR', 'REVIEWER', 'VIEWER'], default: "'VIEWER'" },
          { name: 'isActive', type: 'boolean', default: true },
          { name: 'permissions', type: 'jsonb', isNullable: true },
          { name: 'status', type: 'varchar', length: '50', default: "'ACTIVE'" },
          { name: 'lastLoginAt', type: 'timestamp', isNullable: true },
          { name: 'metadata', type: 'jsonb', isNullable: true },
          { name: 'createdAt', type: 'timestamp', default: 'CURRENT_TIMESTAMP' },
          { name: 'updatedAt', type: 'timestamp', default: 'CURRENT_TIMESTAMP' },
        ],
      })
    );

    await queryRunner.createIndex(
      'users',
      new TableIndex({ columnNames: ['status'] })
    );

    // Create prompt_templates table
    await queryRunner.createTable(
      new Table({
        name: 'prompt_templates',
        columns: [
          { name: 'id', type: 'uuid', isPrimary: true },
          { name: 'topic', type: 'varchar', length: '255' },
          { name: 'subtopic', type: 'varchar', length: '255' },
          { name: 'difficulty', type: 'varchar', length: '50' },
          { name: 'language', type: 'varchar', length: '50' },
          { name: 'template', type: 'text' },
          { name: 'solutionTemplate', type: 'text', isNullable: true },
          { name: 'variables', type: 'jsonb', isNullable: true },
          { name: 'status', type: 'varchar', length: '50', default: "'ACTIVE'" },
          { name: 'usageCount', type: 'integer', default: 0 },
          { name: 'metadata', type: 'jsonb', isNullable: true },
          { name: 'createdBy', type: 'varchar', length: '255', isNullable: true },
          { name: 'createdAt', type: 'timestamp', default: 'CURRENT_TIMESTAMP' },
          { name: 'updatedAt', type: 'timestamp', default: 'CURRENT_TIMESTAMP' },
          { name: 'lastUsedAt', type: 'timestamp', isNullable: true },
        ],
      })
    );

    await queryRunner.createIndex(
      'prompt_templates',
      new TableIndex({ columnNames: ['topic', 'subtopic', 'difficulty', 'language'] })
    );

    await queryRunner.createIndex(
      'prompt_templates',
      new TableIndex({ columnNames: ['status'] })
    );

    // Create questions table
    await queryRunner.createTable(
      new Table({
        name: 'questions',
        columns: [
          { name: 'id', type: 'uuid', isPrimary: true },
          { name: 'topic', type: 'varchar', length: '255' },
          { name: 'subtopic', type: 'varchar', length: '255' },
          { name: 'difficulty', type: 'enum', enum: ['EASY', 'MEDIUM', 'HARD'], default: "'MEDIUM'" },
          { name: 'language', type: 'varchar', length: '50' },
          { name: 'title', type: 'varchar', length: '500' },
          { name: 'problemStatement', type: 'text' },
          { name: 'sampleSolution', type: 'text' },
          { name: 'sampleSolutionExplanation', type: 'jsonb', isNullable: true },
          { name: 'status', type: 'enum', enum: ['DRAFT', 'REVIEW', 'PUBLISHED', 'ARCHIVED'], default: "'DRAFT'" },
          { name: 'validationResultId', type: 'uuid', isNullable: true },
          { name: 'metadata', type: 'jsonb', isNullable: true },
          { name: 'createdBy', type: 'varchar', length: '255', isNullable: true },
          { name: 'updatedBy', type: 'varchar', length: '255', isNullable: true },
          { name: 'createdAt', type: 'timestamp', default: 'CURRENT_TIMESTAMP' },
          { name: 'updatedAt', type: 'timestamp', default: 'CURRENT_TIMESTAMP' },
          { name: 'publishedAt', type: 'timestamp', isNullable: true },
        ],
      })
    );

    await queryRunner.createIndex(
      'questions',
      new TableIndex({ columnNames: ['topic', 'subtopic', 'difficulty'] })
    );

    await queryRunner.createIndex(
      'questions',
      new TableIndex({ columnNames: ['status', 'createdAt'] })
    );

    await queryRunner.createIndex(
      'questions',
      new TableIndex({ columnNames: ['createdBy', 'status'] })
    );

    // Create test_cases table
    await queryRunner.createTable(
      new Table({
        name: 'test_cases',
        columns: [
          { name: 'id', type: 'uuid', isPrimary: true },
          { name: 'questionId', type: 'uuid' },
          { name: 'testInput', type: 'text' },
          { name: 'expectedOutput', type: 'text' },
          { name: 'caseType', type: 'varchar', length: '50', default: "'NORMAL'" },
          { name: 'visible', type: 'boolean', default: false },
          { name: 'explanation', type: 'text', isNullable: true },
          { name: 'difficulty', type: 'varchar', length: '50', isNullable: true },
          { name: 'metadata', type: 'jsonb', isNullable: true },
          { name: 'executionTimeMs', type: 'integer', default: 0 },
          { name: 'memoryUsageMb', type: 'integer', default: 0 },
          { name: 'createdAt', type: 'timestamp', default: 'CURRENT_TIMESTAMP' },
          { name: 'updatedAt', type: 'timestamp', default: 'CURRENT_TIMESTAMP' },
        ],
        foreignKeys: [
          {
            columnNames: ['questionId'],
            referencedTableName: 'questions',
            referencedColumnNames: ['id'],
            onDelete: 'CASCADE',
          },
        ],
      })
    );

    await queryRunner.createIndex(
      'test_cases',
      new TableIndex({ columnNames: ['questionId', 'visible'] })
    );

    await queryRunner.createIndex(
      'test_cases',
      new TableIndex({ columnNames: ['difficulty'] })
    );

    // Create validation_results table
    await queryRunner.createTable(
      new Table({
        name: 'validation_results',
        columns: [
          { name: 'id', type: 'uuid', isPrimary: true },
          { name: 'questionId', type: 'uuid' },
          { name: 'status', type: 'enum', enum: ['PASS', 'WARN', 'FAIL'], default: "'PASS'" },
          { name: 'rulesPassed', type: 'integer', default: 0 },
          { name: 'totalRules', type: 'integer', default: 6 },
          { name: 'rules', type: 'jsonb' },
          { name: 'notes', type: 'text', isNullable: true },
          { name: 'metadata', type: 'jsonb', isNullable: true },
          { name: 'validatedBy', type: 'varchar', length: '255', isNullable: true },
          { name: 'createdAt', type: 'timestamp', default: 'CURRENT_TIMESTAMP' },
          { name: 'updatedAt', type: 'timestamp', default: 'CURRENT_TIMESTAMP' },
        ],
        foreignKeys: [
          {
            columnNames: ['questionId'],
            referencedTableName: 'questions',
            referencedColumnNames: ['id'],
            onDelete: 'CASCADE',
          },
        ],
      })
    );

    await queryRunner.createIndex(
      'validation_results',
      new TableIndex({ columnNames: ['questionId', 'status'] })
    );

    await queryRunner.createIndex(
      'validation_results',
      new TableIndex({ columnNames: ['createdAt'] })
    );

    // Create audit_logs table
    await queryRunner.createTable(
      new Table({
        name: 'audit_logs',
        columns: [
          { name: 'id', type: 'uuid', isPrimary: true },
          { name: 'entityType', type: 'varchar', length: '100' },
          { name: 'entityId', type: 'uuid' },
          { name: 'actionType', type: 'enum', enum: ['CREATE', 'UPDATE', 'DELETE', 'PUBLISH', 'VALIDATE', 'REVIEW', 'ARCHIVE'] },
          { name: 'userId', type: 'uuid', isNullable: true },
          { name: 'userName', type: 'varchar', length: '255', isNullable: true },
          { name: 'ipAddress', type: 'varchar', length: '255', isNullable: true },
          { name: 'changes', type: 'jsonb', isNullable: true },
          { name: 'reason', type: 'text', isNullable: true },
          { name: 'metadata', type: 'jsonb', isNullable: true },
          { name: 'createdAt', type: 'timestamp', default: 'CURRENT_TIMESTAMP' },
        ],
      })
    );

    await queryRunner.createIndex(
      'audit_logs',
      new TableIndex({ columnNames: ['entityType', 'entityId'] })
    );

    await queryRunner.createIndex(
      'audit_logs',
      new TableIndex({ columnNames: ['userId', 'createdAt'] })
    );

    await queryRunner.createIndex(
      'audit_logs',
      new TableIndex({ columnNames: ['actionType', 'createdAt'] })
    );
  }

  public async down(queryRunner: QueryRunner): Promise<void> {
    // Drop all tables in reverse order of creation
    await queryRunner.dropTable('audit_logs');
    await queryRunner.dropTable('validation_results');
    await queryRunner.dropTable('test_cases');
    await queryRunner.dropTable('questions');
    await queryRunner.dropTable('prompt_templates');
    await queryRunner.dropTable('users');
  }
}
