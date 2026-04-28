import {
  Entity,
  PrimaryColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  Index,
  ManyToOne,
  JoinColumn,
  OneToMany,
} from 'typeorm';
import { User } from './User';
import { TestCase } from './TestCase';
import { ValidationResult } from './ValidationResult';

export enum DifficultyLevel {
  EASY = 'EASY',
  MEDIUM = 'MEDIUM',
  HARD = 'HARD',
}

export enum QuestionStatus {
  DRAFT = 'DRAFT',
  REVIEW = 'REVIEW',
  PUBLISHED = 'PUBLISHED',
  ARCHIVED = 'ARCHIVED',
}

@Entity('questions')
@Index(['topic', 'subtopic', 'difficulty'])
@Index(['status', 'createdAt'])
@Index(['createdBy', 'status'])
export class Question {
  @PrimaryColumn('uuid')
  id: string;

  @Column({ type: 'varchar', length: 255 })
  topic: string;

  @Column({ type: 'varchar', length: 255 })
  subtopic: string;

  @Column({
    type: 'enum',
    enum: DifficultyLevel,
    default: DifficultyLevel.MEDIUM,
  })
  difficulty: DifficultyLevel;

  @Column({ type: 'varchar', length: 50 })
  language: string;

  @Column({ type: 'varchar', length: 500 })
  title: string;

  @Column({ type: 'text' })
  problemStatement: string;

  @Column({ type: 'text' })
  sampleSolution: string;

  @Column({ type: 'jsonb', nullable: true })
  sampleSolutionExplanation?: Record<string, any>;

  @Column({
    type: 'enum',
    enum: QuestionStatus,
    default: QuestionStatus.DRAFT,
  })
  status: QuestionStatus;

  @Column({ type: 'uuid', nullable: true })
  validationResultId?: string;

  @Column({ type: 'jsonb', nullable: true })
  metadata?: Record<string, any>;

  @Column({ type: 'varchar', length: 255, nullable: true })
  createdBy?: string;

  @Column({ type: 'varchar', length: 255, nullable: true })
  updatedBy?: string;

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;

  @Column({ type: 'timestamp', nullable: true })
  publishedAt?: Date;

  @OneToMany(() => TestCase, (testCase) => testCase.question, {
    eager: false,
    cascade: false,
  })
  testCases?: TestCase[];

  @OneToMany(() => ValidationResult, (validationResult) => validationResult.question)
  validationResults?: ValidationResult[];
}
