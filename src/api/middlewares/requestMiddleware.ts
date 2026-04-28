import { Request, Response, NextFunction } from 'express';
import { v4 as uuidv4 } from 'uuid';
import { ApiRequest } from './errorHandler';

export function requestIdMiddleware(
  req: ApiRequest,
  res: Response,
  next: NextFunction
): void {
  req.requestId = req.headers['x-request-id'] as string || uuidv4();
  res.setHeader('X-Request-ID', req.requestId);
  next();
}

export function corsMiddleware(
  req: ApiRequest,
  res: Response,
  next: NextFunction
): void {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept, Authorization');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, PATCH, OPTIONS');

  if (req.method === 'OPTIONS') {
    res.sendStatus(200);
    return;
  }

  next();
}

export function contentTypeMiddleware(
  req: ApiRequest,
  res: Response,
  next: NextFunction
): void {
  res.setHeader('Content-Type', 'application/json');
  next();
}

export function authMiddleware(
  req: ApiRequest,
  res: Response,
  next: NextFunction
): void {
  // For now, just extract user from JWT token if present
  // In production, this would validate JWT tokens properly

  const authHeader = req.headers.authorization;
  if (authHeader && authHeader.startsWith('Bearer ')) {
    const token = authHeader.substring(7);
    // TODO: Implement JWT token validation
    req.userId = 'user-id'; // Mock user ID
  }

  next();
}
