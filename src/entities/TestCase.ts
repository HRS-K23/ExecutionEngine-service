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

@Entity('test_cases')
@Index(['questionId', 'visible'])
@Index(['difficulty'])
export class TestCase {
  @PrimaryColumn('uuid')
  id: string;

  @Column('uuid')
  questionId: string;

  @Column({ type: 'text' })
  testInput: string;

  @Column({ type: 'text' })
  expectedOutput: string;

  @Column({ type: 'varchar', length: 50, default: 'NORMAL' })
  caseType: 'NORMAL' | 'EDGE' | 'BOUNDARY' | 'ERROR' | 'STRESS';

  @Column({ type: 'boolean', default: false })
  visible: boolean;

  @Column({ type: 'text', nullable: true })
  explanation?: string;

  @Column({ type: 'varchar', length: 50, nullable: true })
  difficulty?: string;

  @Column({ type: 'jsonb', nullable: true })
  metadata?: Record<string, any>;

  @Column({ type: 'integer', default: 0 })
  executionTimeMs: number;

  @Column({ type: 'integer', default: 0 })
  memoryUsageMb: number;

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;

  @ManyToOne(() => Question, (question) => question.testCases, {
    onDelete: 'CASCADE',
  })
  @JoinColumn({ name: 'questionId' })
  question: Question;
}
