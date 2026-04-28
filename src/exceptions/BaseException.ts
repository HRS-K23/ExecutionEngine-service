export class BaseException extends Error {
  constructor(
    public code: string,
    public message: string,
    public statusCode: number = 500,
    public details?: Record<string, any>
  ) {
    super(message);
    this.name = this.constructor.name;
    Error.captureStackTrace(this, this.constructor);
  }

  toJSON() {
    return {
      code: this.code,
      message: this.message,
      statusCode: this.statusCode,
      details: this.details,
    };
  }
}

export class ValidationException extends BaseException {
  constructor(message: string, details?: Record<string, any>) {
    super('VALIDATION_ERROR', message, 422, details);
  }
}

export class NotFoundException extends BaseException {
  constructor(message: string, details?: Record<string, any>) {
    super('NOT_FOUND', message, 404, details);
  }
}

export class UnauthorizedException extends BaseException {
  constructor(message: string = 'Unauthorized access', details?: Record<string, any>) {
    super('UNAUTHORIZED', message, 401, details);
  }
}

export class ForbiddenException extends BaseException {
  constructor(message: string = 'Access forbidden', details?: Record<string, any>) {
    super('FORBIDDEN', message, 403, details);
  }
}

export class RateLimitException extends BaseException {
  constructor(message: string = 'Rate limit exceeded', details?: Record<string, any>) {
    super('RATE_LIMIT_EXCEEDED', message, 429, details);
  }
}

export class ExternalServiceException extends BaseException {
  constructor(message: string, serviceName: string, details?: Record<string, any>) {
    super('EXTERNAL_SERVICE_ERROR', message, 503, {
      ...details,
      serviceName,
    });
  }
}

export class TimeoutException extends BaseException {
  constructor(message: string = 'Operation timeout', details?: Record<string, any>) {
    super('TIMEOUT', message, 504, details);
  }
}

export class ConflictException extends BaseException {
  constructor(message: string, details?: Record<string, any>) {
    super('CONFLICT', message, 409, details);
  }
}

export class InternalServerException extends BaseException {
  constructor(message: string, details?: Record<string, any>) {
    super('INTERNAL_SERVER_ERROR', message, 500, details);
  }
}

export interface ResponseError {
  code: string;
  message: string;
  details?: Record<string, any>;
}

export function createErrorResponse(error: BaseException): ResponseError {
  return {
    code: error.code,
    message: error.message,
    details: error.details,
  };
}
