import winston from 'winston';
import { LoggerConfig } from '../config/DatabaseConfig';

export class Logger {
  private static instance: Logger | null = null;
  private winstonLogger: winston.Logger;

  private constructor() {
    const transports: winston.transport[] = [];

    // Console transport
    if (LoggerConfig.LOG_CONSOLE_ENABLED) {
      transports.push(
        new winston.transports.Console({
          format: winston.format.combine(
            winston.format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss' }),
            winston.format.errors({ stack: true }),
            LoggerConfig.LOG_FORMAT === 'json'
              ? winston.format.json()
              : winston.format.combine(
                  winston.format.colorize(),
                  winston.format.printf(({ timestamp, level, message, ...meta }) => {
                    const metaStr = Object.keys(meta).length > 0 ? JSON.stringify(meta) : '';
                    return `${timestamp} [${level}]: ${message} ${metaStr}`;
                  })
                )
          ),
        })
      );
    }

    // File transport
    if (LoggerConfig.LOG_FILE_ENABLED) {
      transports.push(
        new winston.transports.File({
          filename: 'logs/error.log',
          level: 'error',
          format: winston.format.combine(
            winston.format.timestamp(),
            winston.format.json()
          ),
          maxsize: this.parseSize(LoggerConfig.LOG_FILE_MAX_SIZE),
          maxFiles: parseInt(LoggerConfig.LOG_FILE_MAX_FILES, 10),
        })
      );

      transports.push(
        new winston.transports.File({
          filename: 'logs/combined.log',
          format: winston.format.combine(
            winston.format.timestamp(),
            winston.format.json()
          ),
          maxsize: this.parseSize(LoggerConfig.LOG_FILE_MAX_SIZE),
          maxFiles: parseInt(LoggerConfig.LOG_FILE_MAX_FILES, 10),
        })
      );
    }

    this.winstonLogger = winston.createLogger({
      level: LoggerConfig.LOG_LEVEL,
      format: winston.format.combine(
        winston.format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss' }),
        winston.format.errors({ stack: true }),
        winston.format.json()
      ),
      defaultMeta: { service: 'execution-engine-ai-services' },
      transports,
    });
  }

  static getInstance(): Logger {
    if (!Logger.instance) {
      Logger.instance = new Logger();
    }
    return Logger.instance;
  }

  info(message: string, meta?: Record<string, any>): void {
    this.winstonLogger.info(message, meta);
  }

  error(message: string, meta?: Record<string, any>): void {
    this.winstonLogger.error(message, meta);
  }

  warn(message: string, meta?: Record<string, any>): void {
    this.winstonLogger.warn(message, meta);
  }

  debug(message: string, meta?: Record<string, any>): void {
    this.winstonLogger.debug(message, meta);
  }

  private parseSize(size: string): number {
    const match = size.match(/^(\d+)(b|kb|mb|gb)$/i);
    if (!match) return 10485760; // 10MB default

    const value = parseInt(match[1], 10);
    const unit = match[2].toLowerCase();

    const multipliers: Record<string, number> = {
      b: 1,
      kb: 1024,
      mb: 1024 * 1024,
      gb: 1024 * 1024 * 1024,
    };

    return value * (multipliers[unit] || 1);
  }
}
