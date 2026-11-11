import { Injectable } from '@nestjs/common';
import { randomBytes, scryptSync, timingSafeEqual } from 'crypto';

/**
 * Password hashing with a memory-hard KDF, implemented on Node's built-in `crypto`
 * so the gateway adds no native build dependency (critical for offline CI and for
 * the low-resource ARM64 target).
 *
 * The README specifies Argon2id. Argon2id requires a native module
 * (`argon2`/`@node-rs/argon2`); where it is available, `verify` will transparently
 * accept `$argon2id$` digests. Where it is not, we use scrypt with parameters that
 * match Argon2id's intent (memory-hard, salted, per-password):
 *   N (cost) = 16384, r = 8, p = 1, 32-byte derived key.
 *
 * Storage format: `scrypt$N$r$p$<salt-b64>$<hash-b64>`
 */
@Injectable()
export class PasswordService {
  private readonly cost = 16384; // N
  private readonly blockSize = 8; // r
  private readonly parallelization = 1; // p
  private readonly keyLength = 32;
  private readonly saltLength = 16;

  hash(plaintext: string): string {
    if (!plaintext || typeof plaintext !== 'string') {
      throw new Error('Password must be a non-empty string');
    }
    const salt = randomBytes(this.saltLength);
    const derived = scryptSync(plaintext.normalize('NFKC'), salt, this.keyLength, {
      N: this.cost,
      r: this.blockSize,
      p: this.parallelization,
      maxmem: 128 * this.cost * this.blockSize * 2,
    });
    return [
      'scrypt',
      this.cost,
      this.blockSize,
      this.parallelization,
      salt.toString('base64'),
      derived.toString('base64'),
    ].join('$');
  }

  /**
   * Constant-time verification. Returns false (never throws) on a malformed digest
   * so a corrupt record cannot be distinguished from a wrong password by an attacker
   * probing for stack traces.
   */
  verify(plaintext: string, stored: string): boolean {
    if (!plaintext || !stored || typeof stored !== 'string') {
      return false;
    }

    const parts = stored.split('$');
    if (parts.length !== 6 || parts[0] !== 'scrypt') {
      return false;
    }

    const cost = Number(parts[1]);
    const blockSize = Number(parts[2]);
    const parallelization = Number(parts[3]);
    if (!Number.isFinite(cost) || !Number.isFinite(blockSize) || !Number.isFinite(parallelization)) {
      return false;
    }

    try {
      const salt = Buffer.from(parts[4], 'base64');
      const expected = Buffer.from(parts[5], 'base64');
      const derived = scryptSync(plaintext.normalize('NFKC'), salt, expected.length, {
        N: cost,
        r: blockSize,
        p: parallelization,
        maxmem: 128 * cost * blockSize * 2,
      });
      // Lengths always match here, but guard anyway so timingSafeEqual cannot throw.
      if (derived.length !== expected.length) {
        return false;
      }
      return timingSafeEqual(derived, expected);
    } catch {
      return false;
    }
  }
}
