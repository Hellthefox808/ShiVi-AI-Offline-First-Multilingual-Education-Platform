import { Injectable, NestMiddleware, HttpException, HttpStatus } from '@nestjs/common';
import { Request, Response, NextFunction } from 'express';

/**
 * Simple in-memory fixed-window rate limiter.
 *
 * Tracks requests per IP address within a rolling window. Intended as a first line
 * of defense against brute-force login and abuse on public endpoints (auth/login).
 * Not a substitute for a distributed rate limiter (Redis) behind a load balancer,
 * but protects a single-node deployment.
 */
@Injectable()
export class RateLimitMiddleware implements NestMiddleware {
  private readonly windowMs = 60_000;
  private readonly maxRequests = 30;
  private readonly hits = new Map<string, { count: number; resetAt: number }>();

  use(req: Request, res: Response, next: NextFunction): void {
    const ip = req.ip ?? req.socket?.remoteAddress ?? 'unknown';
    const now = Date.now();

    let entry = this.hits.get(ip);
    if (!entry || now >= entry.resetAt) {
      entry = { count: 0, resetAt: now + this.windowMs };
      this.hits.set(ip, entry);
    }

    entry.count++;

    // Informative headers for clients.
    res.setHeader('X-RateLimit-Limit', this.maxRequests);
    res.setHeader('X-RateLimit-Remaining', Math.max(0, this.maxRequests - entry.count));
    res.setHeader('X-RateLimit-Reset', entry.resetAt);

    if (entry.count > this.maxRequests) {
      throw new HttpException('Too many requests. Please slow down and try again later.', HttpStatus.TOO_MANY_REQUESTS);
    }

    // Clean up stale entries periodically to prevent unbounded map growth.
    if (this.hits.size > 10_000) {
      for (const [key, value] of this.hits) {
        if (now >= value.resetAt) {
          this.hits.delete(key);
        }
      }
    }

    next();
  }
}
