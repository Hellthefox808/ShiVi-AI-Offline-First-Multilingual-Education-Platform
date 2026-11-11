import { Injectable, UnauthorizedException } from '@nestjs/common';
import { PasswordService } from './password.service';
import { TokenService, UserRole } from './token.service';

export interface UserSession {
  userId: string;
  email: string;
  role: UserRole;
  schoolId: string;
  token: string;
  expiresIn: number;
}

interface SeedUser {
  userId: string;
  email: string;
  role: UserRole;
  schoolId: string;
  name: string;
  /** Password digest. Seed values are hashed at construction — never stored in plaintext. */
  passwordHash: string;
}

/**
 * Multi-tenant credential store with RBAC.
 *
 * Previous implementation compared plaintext passwords with `===` and returned a
 * non-cryptographic token (`jwt_<id>_<ts>`), which any client could forge. Both are
 * now fixed:
 *  - Digests are memory-hard (see PasswordService) and compared in constant time.
 *  - Tokens are signed HS256 JWTs with an expiry and a jti (see TokenService).
 *  - Failed logins return one indistinguishable error to avoid user enumeration.
 */
@Injectable()
export class AuthService {
  private readonly users: SeedUser[];

  constructor(
    private readonly passwordService: PasswordService,
    private readonly tokenService: TokenService,
  ) {
    // Demo seed credentials. In a real deployment these load from the database and
    // the digests are provisioned out-of-band; they are hashed here so that no
    // plaintext credential ever lives in a field on the instance.
    const seed = [
      {
        userId: 'USR-001',
        email: 'ramesh.kumar@jcert.in',
        password: 'password123',
        role: 'TEACHER' as UserRole,
        schoolId: 'SCH-DUMKA-042',
        name: 'Ramesh Kumar (Grade 2 Teacher)',
      },
      {
        userId: 'USR-002',
        email: 'sunita.soren@jcert.in',
        password: 'password123',
        role: 'NATIVE_REVIEWER' as UserRole,
        schoolId: 'SCH-HQ-001',
        name: 'Dr. Sunita Soren (Santhali Linguist)',
      },
      {
        userId: 'USR-003',
        email: 'admin.dumka@jcert.in',
        password: 'password123',
        role: 'DISTRICT_ADMIN' as UserRole,
        schoolId: 'SCH-HQ-001',
        name: 'District Admin (Dumka)',
      },
    ];

    this.users = seed.map(({ password, ...rest }) => ({
      ...rest,
      passwordHash: this.passwordService.hash(password),
    }));
  }

  login(credentials: { email: string; password: string }): UserSession {
    const email = (credentials?.email ?? '').trim().toLowerCase();
    const password = credentials?.password ?? '';

    const user = this.users.find((u) => u.email.toLowerCase() === email);

    // Always run a verification (even for an unknown user) so response time does not
    // reveal whether the account exists.
    const dummyHash =
      'scrypt$16384$8$1$AAAAAAAAAAAAAAAAAAAAAA==$AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=';
    const isValid = this.passwordService.verify(password, user?.passwordHash ?? dummyHash);

    if (!user || !isValid) {
      throw new UnauthorizedException('Invalid email or password');
    }

    const { token, expiresIn } = this.tokenService.issue({
      userId: user.userId,
      email: user.email,
      role: user.role,
      schoolId: user.schoolId,
    });

    return {
      userId: user.userId,
      email: user.email,
      role: user.role,
      schoolId: user.schoolId,
      token,
      expiresIn,
    };
  }

  /** Looks up a user's public profile without exposing the digest. */
  findPublicUser(userId: string) {
    const user = this.users.find((u) => u.userId === userId);
    if (!user) return null;
    const { passwordHash, ...publicFields } = user;
    return publicFields;
  }
}
