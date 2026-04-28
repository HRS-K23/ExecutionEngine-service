import { Entity, PrimaryColumn, Column, CreateDateColumn, UpdateDateColumn, Index } from 'typeorm';

@Entity('prompt_templates')
@Index(['topic', 'subtopic', 'difficulty', 'language'])
@Index(['status'])
export class PromptTemplate {
  @PrimaryColumn('uuid')
  id: string;

  @Column({ type: 'varchar', length: 255 })
  topic: string;

  @Column({ type: 'varchar', length: 255 })
  subtopic: string;

  @Column({ type: 'varchar', length: 50 })
  difficulty: string;

  @Column({ type: 'varchar', length: 50 })
  language: string;

  @Column({ type: 'text' })
  template: string;

  @Column({ type: 'text', nullable: true })
  solutionTemplate?: string;

  @Column({ type: 'jsonb', nullable: true })
  variables?: Record<string, string>;

  @Column({ type: 'varchar', length: 50, default: 'ACTIVE' })
  status: string;

  @Column({ type: 'integer', default: 0 })
  usageCount: number;

  @Column({ type: 'jsonb', nullable: true })
  metadata?: Record<string, any>;

  @Column({ type: 'varchar', length: 255, nullable: true })
  createdBy?: string;

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;

  @Column({ type: 'timestamp', nullable: true })
  lastUsedAt?: Date;
}
