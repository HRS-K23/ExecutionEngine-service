import { Request, Response, NextFunction } from 'express';
import { BaseException, createErrorResponse } from '../exceptions/BaseException';
import { Logger } from '../logging/Logger';

const logger = Logger.getInstance();

export interface ApiRequest extends Request {
  requestId?: string;
  userId?: string;
  user?: any;
}

export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  errors?: any[];
  meta: {
    timestamp: string;
    requestId: string;
    version: string;
  };
}

export function errorHandler(
  err: any,
  req: ApiRequest,
  res: Response,
  next: NextFunction
): void {
  const requestId = req.requestId || 'unknown';

  logger.error('ErrorHandler: Caught error', {
    requestId,
    error: err instanceof Error ? err.message : err,
    stack: err instanceof Error ? err.stack : undefined,
  });

  if (err instanceof BaseException) {
    const response: ApiResponse = {
      success: false,
      errors: [createErrorResponse(err)],
      meta: {
        timestamp: new Date().toISOString(),
        requestId,
        version: '1.0',
      },
    };

    res.status(err.statusCode).json(response);
    return;
  }

  // Handle unexpected errors
  const response: ApiResponse = {
    success: false,
    errors: [
      {
        code: 'INTERNAL_SERVER_ERROR',
        message: 'An unexpected error occurred',
        details: process.env.NODE_ENV === 'development' ? { error: err.message } : undefined,
      },
    ],
    meta: {
      timestamp: new Date().toISOString(),
      requestId,
      version: '1.0',
    },
  };

  res.status(500).json(response);
}

export function asyncHandler(
  fn: (req: ApiRequest, res: Response, next: NextFunction) => Promise<any>
) {
  return (req: ApiRequest, res: Response, next: NextFunction): void => {
    Promise.resolve(fn(req, res, next)).catch(next);
  };
}
