import {
  Entity,
  PrimaryColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  Index,
  ManyToOne,
  JoinColumn,
} from 'typeorm';
import { Question } from './Question';

export enum ValidationStatus {
  PASS = 'PASS',
  WARN = 'WARN',
  FAIL = 'FAIL',
}

export interface ValidationRule {
  id: string;
  name: string;
  description: string;
  status: 'PASS' | 'WARN' | 'FAIL';
  message?: string;
  severity?: 'INFO' | 'WARNING' | 'ERROR';
}

@Entity('validation_results')
@Index(['questionId', 'status'])
@Index(['createdAt'])
export class ValidationResult {
  @PrimaryColumn('uuid')
  id: string;

  @Column('uuid')
  questionId: string;

  @Column({
    type: 'enum',
    enum: ValidationStatus,
    default: ValidationStatus.PASS,
  })
  status: ValidationStatus;

  @Column({ type: 'integer', default: 0 })
  rulesPassed: number;

  @Column({ type: 'integer', default: 6 })
  totalRules: number;

  @Column({ type: 'jsonb' })
  rules: ValidationRule[];

  @Column({ type: 'text', nullable: true })
  notes?: string;

  @Column({ type: 'jsonb', nullable: true })
  metadata?: Record<string, any>;

  @Column({ type: 'varchar', length: 255, nullable: true })
  validatedBy?: string;

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;

  @ManyToOne(() => Question, (question) => question.validationResults, {
    onDelete: 'CASCADE',
  })
  @JoinColumn({ name: 'questionId' })
  question: Question;
}
