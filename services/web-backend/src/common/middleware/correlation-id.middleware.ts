import { Injectable, NestMiddleware } from '@nestjs/common';
import { Request, Response, NextFunction } from 'express';
import { randomUUID } from 'crypto';

/**
 * Correlation ID middleware.
 *
 * Every incoming request receives a stable X-Request-ID (or echoes the client-supplied
 * one) so that logs, traces, and error responses can be correlated across service
 * boundaries. The ID is exposed on `request.id` for downstream handlers.
 */
@Injectable()
export class CorrelationIdMiddleware implements NestMiddleware {
  private readonly headerName = 'x-request-id';

  use(req: Request, res: Response, next: NextFunction): void {
    const incoming = req.headers[this.headerName];
    const requestId =
      Array.isArray(incoming) ? incoming[0] : typeof incoming === 'string' ? incoming : randomUUID();

    (req as any).id = requestId;
    res.setHeader(this.headerName, requestId);
    next();
  }
}
