import {
  CanActivate,
  ExecutionContext,
  Injectable,
  SetMetadata,
  UnauthorizedException,
  ForbiddenException,
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { TokenService, JwtPayload, UserRole } from './token.service';

export const ROLES_KEY = 'bhashasetu_roles';
export const PUBLIC_KEY = 'bhashasetu_public';

/** Marks a route as reachable without a bearer token (e.g. login, health). */
export const Public = () => SetMetadata(PUBLIC_KEY, true);

/** Restricts a route to the listed roles. Requires a valid token. */
export const Roles = (...roles: UserRole[]) => SetMetadata(ROLES_KEY, roles);

/**
 * Global authentication + RBAC guard.
 *
 * Secure by default: every route requires a valid signed token unless explicitly
 * annotated `@Public()`. This is the inverse of the previous arrangement, where the
 * README claimed "Auth Required: Yes" on nearly every endpoint while no guard existed
 * anywhere in the codebase — so all data endpoints were in fact open.
 */
@Injectable()
export class AuthGuard implements CanActivate {
  constructor(
    private readonly reflector: Reflector,
    private readonly tokenService: TokenService,
  ) {}

  canActivate(context: ExecutionContext): boolean {
    const isPublic = this.reflector.getAllAndOverride<boolean>(PUBLIC_KEY, [
      context.getHandler(),
      context.getClass(),
    ]);
    if (isPublic) {
      return true;
    }

    const request = context.switchToHttp().getRequest();
    const authHeader: string | undefined =
      request?.headers?.authorization ?? request?.headers?.Authorization;

    if (!authHeader || !authHeader.toLowerCase().startsWith('bearer ')) {
      throw new UnauthorizedException('Authorization header with Bearer token is required');
    }

    const token = authHeader.slice(7).trim();
    // Throws UnauthorizedException on bad signature / expiry / malformed input.
    const payload: JwtPayload = this.tokenService.verify(token);

    const requiredRoles = this.reflector.getAllAndOverride<UserRole[]>(ROLES_KEY, [
      context.getHandler(),
      context.getClass(),
    ]);

    if (requiredRoles && requiredRoles.length > 0 && !requiredRoles.includes(payload.role)) {
      throw new ForbiddenException(
        `Role '${payload.role}' is not permitted to access this resource`
      );
    }

    // Expose the verified principal for downstream handlers.
    request.user = payload;
    return true;
  }
}
