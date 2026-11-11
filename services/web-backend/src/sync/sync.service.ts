import { Injectable, BadRequestException, Optional } from '@nestjs/common';
import { SyncPushRequest, SyncPushResponse, SyncPullResponse } from '@bhashasetu/contracts';
import { LessonsService } from '../lessons/lessons.service';
import { CurriculumService } from '../curriculum/curriculum.service';
import { PersistenceService } from '../common/persistence.service';

interface SyncCandidate {
  attempts: any[];
  cursor: number;
}

/**
 * Durable outbox reconciliation with server-side idempotency.
 *
 * Idempotency key is the composite (deviceId, operationId), NOT operationId alone.
 * Operation ids are client-generated (`OP-<ts>` / UUIDs) and two offline tablets can
 * legitimately collide on the same value; keying on operationId alone would make one
 * device's write silently vanish as a "duplicate".
 */
@Injectable()
export class SyncService {
  private processedOperationIds: Set<string> = new Set();

  constructor(
    private readonly lessonsService: LessonsService,
    private readonly curriculumService: CurriculumService,
    @Optional() private readonly persistence?: PersistenceService,
  ) {}

  private readonly COLLECTION = 'sync';
  private memoryState: SyncCandidate = { attempts: [], cursor: 0 };

  private get state(): SyncCandidate {
    if (!this.persistence) return this.memoryState;
    return this.persistence.read<SyncCandidate>(this.COLLECTION, { attempts: [], cursor: 0 });
  }

  private persist(): void {
    if (this.persistence) {
      this.persistence.write(this.COLLECTION, this.state);
    }
  }

  private get storedAttempts(): any[] {
    return this.state.attempts;
  }

  private get lastCursorTimestamp(): number {
    return this.state.cursor;
  }

  private idempotencyKey(deviceId: string, operationId: string): string {
    return `${deviceId || 'UNKNOWN_DEVICE'}::${operationId}`;
  }

  processPush(request: SyncPushRequest): SyncPushResponse {
    if (!request || !Array.isArray(request.operations)) {
      throw new BadRequestException('Sync push requires an operations array');
    }

    const acknowledgedIds: string[] = [];
    const conflicts: any[] = [];

    for (const op of request.operations) {
      if (!op || !op.operationId) {
        conflicts.push({
          operationId: op?.operationId ?? null,
          reason: 'MISSING_OPERATION_ID',
          message: 'Outbox operations must carry a client-generated operationId for idempotency'
        });
        continue;
      }

      const key = this.idempotencyKey(request.deviceId, op.operationId);

      if (this.processedOperationIds.has(key)) {
        // Idempotency: Already processed, acknowledge silently
        acknowledgedIds.push(op.operationId);
        continue;
      }

      if (op.entityType === 'ASSESSMENT_ATTEMPT') {
        // Append-only student attempt log
        this.storedAttempts.push({
          ...op.payload,
          deviceId: request.deviceId,
          serverReceivedAt: new Date().toISOString()
        });
        this.processedOperationIds.add(key);
        acknowledgedIds.push(op.operationId);
      } else if (op.entityType === 'LESSON') {
        // Teacher lesson draft or review
        this.lessonsService.create(op.payload);
        this.processedOperationIds.add(key);
        acknowledgedIds.push(op.operationId);
      } else {
        this.processedOperationIds.add(key);
        acknowledgedIds.push(op.operationId);
      }
    }

    this.state.cursor = Math.max(this.state.cursor, Date.now());
    this.persist();

    return {
      acknowledgedOperationIds: acknowledgedIds,
      conflicts,
      serverTimestamp: new Date().toISOString()
    };
  }

  processPull(cursor?: string): SyncPullResponse {
    const curriculum = this.curriculumService.findAll();
    const lessons = this.lessonsService.findAll().filter((l) => l.status === 'PUBLISHED');

    // Cursor is a monotonic timestamp; only advance it forward so a stale client cursor
    // cannot rewind the server's notion of "what the client already has".
    const cursorTimestamp = this.parseCursor(cursor);
    const newCursorTimestamp = Math.max(cursorTimestamp, this.lastCursorTimestamp);
    return {
      newCursor: `cursor_${newCursorTimestamp}`,
      curriculumUpdates: curriculum,
      approvedLessons: lessons
    };
  }

  private parseCursor(cursor?: string): number {
    if (!cursor) return 0;
    const match = /(\d{10,})/.exec(cursor);
    if (!match) return 0;
    const parsed = Number(match[1]);
    return Number.isFinite(parsed) ? parsed : 0;
  }

  getAttempts() {
    return {
      count: this.storedAttempts.length,
      data: this.storedAttempts
    };
  }
}
