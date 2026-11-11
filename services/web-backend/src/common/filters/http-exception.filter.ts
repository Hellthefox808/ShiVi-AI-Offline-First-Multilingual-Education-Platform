import { ArgumentsHost, Catch, ExceptionFilter, HttpException, HttpStatus, Logger } from '@nestjs/common';
import { Request, Response } from 'express';

/**
 * Global exception filter that guarantees every error response carries a stable,
 * structured shape with a correlation ID, error code, and timestamp.
 *
 * Unhandled errors are mapped to 500 with a generic message so that stack traces
 * and implementation details never leak to the client.
 */
@Catch()
export class HttpExceptionFilter implements ExceptionFilter {
  private readonly logger = new Logger(HttpExceptionFilter.name);

  catch(exception: unknown, host: ArgumentsHost): void {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse<Response>();
    const request = ctx.getRequest<Request>();

    const requestId = (request as any).id ?? request.headers['x-request-id'] ?? 'unknown';
    const now = new Date().toISOString();

    let status: number;
    let code: string;
    let message: string;

    if (exception instanceof HttpException) {
      status = exception.getStatus();
      const responseBody = exception.getResponse();
      code =
        typeof responseBody === 'string'
          ? responseBody
          : (responseBody as any)?.code ?? exception.constructor.name;
      message =
        typeof responseBody === 'string'
          ? responseBody
          : (responseBody as any)?.message ?? exception.message;
    } else {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      code = 'INTERNAL_SERVER_ERROR';
      message = 'An unexpected error occurred';
      this.logger.error(
        `Unhandled exception on ${request.method} ${request.url}`,
        exception instanceof Error ? exception.stack : String(exception),
      );
    }

    response.status(status).json({
      error: true,
      code,
      message,
      statusCode: status,
      timestamp: now,
      path: request.url,
      requestId,
    });
  }
}
