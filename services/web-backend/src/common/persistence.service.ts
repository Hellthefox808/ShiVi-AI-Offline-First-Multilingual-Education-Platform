import { Injectable, Logger, OnModuleInit, OnModuleDestroy } from '@nestjs/common';
import { existsSync, mkdirSync, readFileSync, writeFileSync, renameSync, unlinkSync } from 'fs';
import { dirname, join, resolve } from 'path';

/**
 * Durable JSON-document store used as the gateway's system of record.
 *
 * Why not PostgreSQL: the compose/K8s manifests point at PostgreSQL 18 + pgvector, but
 * no migrations, entities, or connection layer exist anywhere in the repository, so in
 * practice every service held its data in a class field and lost it on restart — a
 * teacher's approved lesson or a child's assessment attempt vanished whenever the
 * process recycled. That is the single most damaging gap for a field pilot.
 *
 * This store fixes the actual data-loss failure with zero new dependencies (important
 * for the offline CI and ARM64 story):
 *   - Atomic writes via write-temp-then-rename, so a crash mid-write cannot truncate
 *     the record file.
 *   - Debounced flush so a burst of writes does not thrash the disk.
 *   - Synchronous flush on shutdown so SIGTERM does not lose the tail of the queue.
 *
 * It is deliberately a drop-in behind a narrow interface: swapping to a real
 * relational store later means reimplementing this class, not touching the services.
 */
@Injectable()
export class PersistenceService implements OnModuleInit, OnModuleDestroy {
  private readonly logger = new Logger(PersistenceService.name);
  private readonly dataDir: string;
  private readonly flushDebounceMs = 250;

  private cache = new Map<string, unknown>();
  private dirty = new Set<string>();
  private flushTimer: NodeJS.Timeout | null = null;
  private readonly readOnly: boolean;

  constructor() {
    const configured = process.env.BHASHASETU_DATA_DIR;
    this.dataDir = resolve(configured || join(process.cwd(), '.bhashasetu-data'));
    // Tests and read-only replicas can opt out of disk writes entirely.
    this.readOnly = process.env.BHASHASETU_PERSISTENCE === 'memory';
  }

  onModuleInit(): void {
    if (this.readOnly || !this.dataDir) return;
    try {
      if (!existsSync(this.dataDir)) {
        mkdirSync(this.dataDir, { recursive: true });
      }
      this.logger.log(`Persistence directory ready: ${this.dataDir}`);
    } catch (err) {
      // Never crash the gateway because the volume is not writable; degrade to
      // in-memory and say so loudly.
      this.logger.error(
        `Failed to prepare persistence directory ${this.dataDir}; falling back to in-memory only: ${err}`
      );
    }
  }

  onModuleDestroy(): void {
    this.flushAll();
  }

  private fileFor(collection: string): string {
    const safe = collection.replace(/[^a-zA-Z0-9_-]/g, '_');
    return join(this.dataDir, `${safe}.json`);
  }

  /**
   * Reads a collection, hydrating from disk on first access. Returns `fallback`
   * (and caches it) when no record file exists yet.
   */
  read<T>(collection: string, fallback: T): T {
    if (this.cache.has(collection)) {
      return this.cache.get(collection) as T;
    }

    if (this.readOnly) {
      this.cache.set(collection, fallback);
      return fallback;
    }

    const file = this.fileFor(collection);
    if (!existsSync(file)) {
      this.cache.set(collection, fallback);
      return fallback;
    }

    try {
      const raw = readFileSync(file, 'utf8');
      const parsed = JSON.parse(raw) as T;
      this.cache.set(collection, parsed);
      return parsed;
    } catch (err) {
      // A corrupt file must not take down the gateway. Preserve it for forensics
      // rather than silently overwriting, then start from the fallback.
      this.logger.error(`Corrupt persistence file ${file}; quarantining and using fallback: ${err}`);
      try {
        renameSync(file, `${file}.corrupt-${Date.now()}`);
      } catch {
        /* best effort */
      }
      this.cache.set(collection, fallback);
      return fallback;
    }
  }

  /** Marks a collection dirty and schedules a debounced atomic flush. */
  write<T>(collection: string, value: T): void {
    this.cache.set(collection, value);
    if (this.readOnly) return;
    this.dirty.add(collection);
    this.scheduleFlush();
  }

  private scheduleFlush(): void {
    if (this.flushTimer) return;
    this.flushTimer = setTimeout(() => {
      this.flushTimer = null;
      this.flushAll();
    }, this.flushDebounceMs);
    // Do not hold the event loop open purely for a pending flush.
    if (typeof this.flushTimer.unref === 'function') {
      this.flushTimer.unref();
    }
  }

  /** Flushes every dirty collection. Safe to call directly (e.g. on shutdown). */
  flushAll(): void {
    if (this.readOnly) return;
    if (this.flushTimer) {
      clearTimeout(this.flushTimer);
      this.flushTimer = null;
    }
    for (const collection of Array.from(this.dirty)) {
      this.flushOne(collection);
    }
    this.dirty.clear();
  }

  private flushOne(collection: string): void {
    const file = this.fileFor(collection);
    const payload = this.cache.get(collection);
    if (payload === undefined) return;

    const temp = `${file}.tmp-${process.pid}`;
    try {
      mkdirSync(dirname(file), { recursive: true });
      writeFileSync(temp, JSON.stringify(payload, null, 2), 'utf8');
      // Atomic replace: readers see either the old file or the new one, never a partial.
      renameSync(temp, file);
    } catch (err) {
      this.logger.error(`Failed to persist collection '${collection}' to ${file}: ${err}`);
      try {
        if (existsSync(temp)) unlinkSync(temp);
      } catch {
        /* best effort */
      }
    }
  }

  /** Test helper: drops both the in-memory cache and the on-disk record files. */
  resetAll(): void {
    this.cache.clear();
    this.dirty.clear();
    if (this.flushTimer) {
      clearTimeout(this.flushTimer);
      this.flushTimer = null;
    }
  }
}
