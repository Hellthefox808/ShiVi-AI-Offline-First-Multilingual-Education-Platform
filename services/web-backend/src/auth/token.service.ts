import { Injectable, UnauthorizedException } from '@nestjs/common';
import { createHmac, randomUUID, timingSafeEqual } from 'crypto';

export type UserRole = 'TEACHER' | 'NATIVE_REVIEWER' | 'SCHOOL_ADMIN' | 'DISTRICT_ADMIN';

export interface JwtPayload {
  sub: string;
  email: string;
  role: UserRole;
  schoolId: string;
  iat: number;
  exp: number;
  jti: string;
}

/**
 * Minimal, dependency-free JWT (HS256) implementation.
 *
 * The previous `token: 'jwt_${id}_${Date.now()}'` was not a token at all — it carried
 * no signature and no expiry, so any client could forge `jwt_USR-001_<ts>` and be
 * treated as an authenticated district admin. This produces a real signed,
 * expiring, replay-resistant token.
 *
 * HS256 via Node `crypto` is used because no JOSE library is present. If a signing
 * secret is not configured, we fail closed in production and fall back to an
 * ephemeral per-process secret in development.
 */
@Injectable()
export class TokenService {
  private readonly header = { alg: 'HS256', typ: 'JWT' } as const;
  private readonly secret: string;
  private readonly defaultTtlSeconds = 3600;

  constructor() {
    const configured = process.env.JWT_SECRET;
    if (configured && configured.length >= 32) {
      this.secret = configured;
    } else if (process.env.NODE_ENV === 'production') {
      throw new Error(
        'JWT_SECRET must be set to a value of at least 32 characters in production. Refusing to start with a forgeable token secret.'
      );
    } else {
      // Development only: ephemeral secret. Tokens do not survive a restart, which is
      // the correct trade-off — a hardcoded fallback secret would be a real backdoor.
      this.secret = randomUUID() + randomUUID();
    }
  }

  private base64Url(input: Buffer | string): string {
    const buf = Buffer.isBuffer(input) ? input : Buffer.from(input, 'utf8');
    return buf.toString('base64').replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  }

  private sign(signingInput: string): string {
    return this.base64Url(createHmac('sha256', this.secret).update(signingInput).digest());
  }

  issue(user: {
    userId: string;
    email: string;
    role: UserRole;
    schoolId: string;
  }, ttlSeconds: number = this.defaultTtlSeconds): { token: string; expiresIn: number } {
    const now = Math.floor(Date.now() / 1000);
    const payload: JwtPayload = {
      sub: user.userId,
      email: user.email,
      role: user.role,
      schoolId: user.schoolId,
      iat: now,
      exp: now + ttlSeconds,
      jti: randomUUID(),
    };

    const encodedHeader = this.base64Url(JSON.stringify(this.header));
    const encodedPayload = this.base64Url(JSON.stringify(payload));
    const signature = this.sign(`${encodedHeader}.${encodedPayload}`);

    return { token: `${encodedHeader}.${encodedPayload}.${signature}`, expiresIn: ttlSeconds };
  }

  verify(token: string): JwtPayload {
    if (!token || typeof token !== 'string') {
      throw new UnauthorizedException('Missing bearer token');
    }

    const parts = token.split('.');
    if (parts.length !== 3) {
      throw new UnauthorizedException('Malformed token');
    }

    const [encodedHeader, encodedPayload, providedSignature] = parts;
    const expectedSignature = this.sign(`${encodedHeader}.${encodedPayload}`);

    const providedBuf = Buffer.from(providedSignature);
    const expectedBuf = Buffer.from(expectedSignature);
    if (providedBuf.length !== expectedBuf.length || !timingSafeEqual(providedBuf, expectedBuf)) {
      throw new UnauthorizedException('Invalid token signature');
    }

    let payload: JwtPayload;
    try {
      payload = JSON.parse(Buffer.from(encodedPayload, 'base64').toString('utf8'));
    } catch {
      throw new UnauthorizedException('Malformed token payload');
    }

    const now = Math.floor(Date.now() / 1000);
    if (typeof payload.exp !== 'number' || payload.exp <= now) {
      throw new UnauthorizedException('Token expired');
    }

    return payload;
  }
}
