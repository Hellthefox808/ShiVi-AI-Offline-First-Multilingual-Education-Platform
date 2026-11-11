import { describe, it, beforeEach } from 'node:test';
import * as assert from 'node:assert/strict';
import { AuthService } from '../src/auth/auth.service';
import { PasswordService } from '../src/auth/password.service';
import { TokenService } from '../src/auth/token.service';
import { AuthGuard } from '../src/auth/auth.guard';
import { SyncService } from '../src/sync/sync.service';
import { LessonsService } from '../src/lessons/lessons.service';
import { CurriculumService } from '../src/curriculum/curriculum.service';

describe('BhashaSetu Auth, RBAC & Durable Sync Integrity', () => {
  let passwordService: PasswordService;
  let tokenService: TokenService;
  let authService: AuthService;

  beforeEach(() => {
    passwordService = new PasswordService();
    tokenService = new TokenService();
    authService = new AuthService(passwordService, tokenService);
  });

  it('should hash passwords with a memory-hard KDF, never storing plaintext', () => {
    const digest = passwordService.hash('password123');
    assert.ok(digest.startsWith('scrypt$'), `digest must be a scrypt record, got ${digest}`);
    assert.ok(!digest.includes('password123'), 'digest must not contain the plaintext');
    assert.notEqual(passwordService.hash('password123'), digest, 'salt must make hashes unique');
    assert.equal(passwordService.verify('password123', digest), true);
    assert.equal(passwordService.verify('password124', digest), false);
  });

  it('should reject malformed digests without throwing', () => {
    assert.equal(passwordService.verify('x', ''), false);
    assert.equal(passwordService.verify('x', 'not-a-digest'), false);
    assert.equal(passwordService.verify('x', 'scrypt$bogus$8$1$AA==$AA=='), false);
  });

  it('should issue a real signed JWT with expiry and jti', () => {
    const session = authService.login({ email: 'ramesh.kumar@jcert.in', password: 'password123' });
    assert.equal(session.role, 'TEACHER');
    assert.equal(session.token.split('.').length, 3, 'JWT must have header.payload.signature');
    assert.ok(session.expiresIn > 0);

    const payload = tokenService.verify(session.token);
    assert.equal(payload.sub, 'USR-001');
    assert.equal(payload.role, 'TEACHER');
    assert.ok(payload.jti, 'token must carry a jti for replay detection');
    assert.ok(payload.exp > payload.iat);
  });

  it('should reject the legacy forgeable token format', () => {
    // Previously the gateway returned `jwt_<userId>_<timestamp>` with no signature.
    assert.throws(() => tokenService.verify('jwt_USR-003_1234567890'), /Malformed token/);
  });

  it('should reject a privilege-escalation payload re-signed onto a valid token', () => {
    const session = authService.login({ email: 'ramesh.kumar@jcert.in', password: 'password123' });
    const [, , signature] = session.token.split('.');
    const escalated = Buffer.from(
      JSON.stringify({
        sub: 'USR-001',
        email: 'ramesh.kumar@jcert.in',
        role: 'DISTRICT_ADMIN',
        schoolId: 'SCH-HQ-001',
        iat: 1,
        exp: 9999999999,
        jti: 'forged',
      })
    ).toString('base64url');

    const forged = `${session.token.split('.')[0]}.${escalated}.${signature}`;
    assert.throws(() => tokenService.verify(forged), /Invalid token signature/);
  });

  it('should reject an expired token', () => {
    const issued = tokenService.issue(
      { userId: 'USR-001', email: 'a@b.in', role: 'TEACHER', schoolId: 'S' },
      -10 // already expired
    );
    assert.throws(() => tokenService.verify(issued.token), /expired/i);
  });

  it('should return one indistinguishable error for bad password and unknown user', () => {
    let wrongPassword = '';
    let unknownUser = '';
    try {
      authService.login({ email: 'ramesh.kumar@jcert.in', password: 'nope' });
    } catch (e: any) {
      wrongPassword = e.message;
    }
    try {
      authService.login({ email: 'ghost@jcert.in', password: 'password123' });
    } catch (e: any) {
      unknownUser = e.message;
    }
    assert.equal(wrongPassword, unknownUser, 'errors must not reveal whether the account exists');
    assert.ok(wrongPassword.length > 0);
  });

  it('should enforce RBAC: a TEACHER may not reach a DISTRICT_ADMIN-only route', () => {
    const reflector = {
      getAllAndOverride: (key: string) => (key === 'bhashasetu_roles' ? ['DISTRICT_ADMIN'] : undefined),
    } as any;

    const guard = new AuthGuard(reflector, tokenService);
    const teacherSession = authService.login({
      email: 'ramesh.kumar@jcert.in',
      password: 'password123',
    });

    const context = {
      getHandler: () => ({}),
      getClass: () => ({}),
      switchToHttp: () => ({
        getRequest: () => ({ headers: { authorization: `Bearer ${teacherSession.token}` } }),
      }),
    } as any;

    assert.throws(() => guard.canActivate(context), /not permitted/);
  });

  it('should require a bearer token when a route is not marked @Public()', () => {
    const reflector = { getAllAndOverride: () => undefined } as any;
    const guard = new AuthGuard(reflector, tokenService);
    const context = {
      getHandler: () => ({}),
      getClass: () => ({}),
      switchToHttp: () => ({ getRequest: () => ({ headers: {} }) }),
    } as any;

    assert.throws(() => guard.canActivate(context), /Bearer token is required/);
  });

  it('should scope outbox idempotency per device so two tablets cannot cancel each other', () => {
    const sync = new SyncService(new LessonsService(), new CurriculumService());

    const op = {
      operationId: 'OP-COLLIDE-001',
      entityType: 'ASSESSMENT_ATTEMPT',
      entityId: 'ASMT-1',
      schoolId: 'SCH-DUMKA-042',
      operation: 'CREATE',
      payload: { score: 8 },
      sequenceNo: 1,
      timestamp: new Date().toISOString(),
      status: 'PENDING' as const,
      retryCount: 0,
    };

    const firstDevice = sync.processPush({
      schoolId: 'SCH-DUMKA-042',
      deviceId: 'TAB-DUMKA-001',
      operations: [op as any],
    });
    const secondDevice = sync.processPush({
      schoolId: 'SCH-PAKUR-011',
      deviceId: 'TAB-PAKUR-007',
      operations: [op as any], // same client-generated id, different tablet
    });

    assert.deepEqual(firstDevice.acknowledgedOperationIds, ['OP-COLLIDE-001']);
    assert.deepEqual(
      secondDevice.acknowledgedOperationIds,
      ['OP-COLLIDE-001'],
      'a distinct device reusing an operation id must still be accepted'
    );
    assert.equal(sync.getAttempts().count, 2, 'both devices disputes must be persisted');
  });

  it('should drop a genuine replay from the same device', () => {
    const sync = new SyncService(new LessonsService(), new CurriculumService());
    const op = {
      operationId: 'OP-REPLAY-001',
      entityType: 'ASSESSMENT_ATTEMPT',
      entityId: 'ASMT-2',
      schoolId: 'SCH-DUMKA-042',
      operation: 'CREATE',
      payload: { score: 9 },
      sequenceNo: 1,
      timestamp: new Date().toISOString(),
      status: 'PENDING' as const,
      retryCount: 0,
    };

    sync.processPush({ schoolId: 'S', deviceId: 'TAB-1', operations: [op as any] });
    const replay = sync.processPush({ schoolId: 'S', deviceId: 'TAB-1', operations: [op as any] });

    assert.deepEqual(replay.acknowledgedOperationIds, ['OP-REPLAY-001']);
    assert.equal(sync.getAttempts().count, 1, 'replayed operation must not be double-counted');
  });

  it('should report a conflict for an operation missing its idempotency id', () => {
    const sync = new SyncService(new LessonsService(), new CurriculumService());
    const result = sync.processPush({
      schoolId: 'S',
      deviceId: 'TAB-1',
      operations: [{ entityType: 'ASSESSMENT_ATTEMPT', payload: {} } as any],
    });
    assert.equal(result.acknowledgedOperationIds.length, 0);
    assert.equal(result.conflicts.length, 1);
    assert.equal(result.conflicts[0].reason, 'MISSING_OPERATION_ID');
  });

  it('should advance the delta cursor monotonically and never rewind it', () => {
    const sync = new SyncService(new LessonsService(), new CurriculumService());
    const first = sync.processPull();
    const rewound = sync.processPull('cursor_1');
    assert.ok(
      Number((rewound.newCursor.match(/\d+/) || ['0'])[0]) >=
        Number((first.newCursor.match(/\d+/) || ['0'])[0]),
      'a stale client cursor must not rewind the server cursor'
    );
  });
});
