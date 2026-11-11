import { describe, it, beforeEach, after } from 'node:test';
import * as assert from 'node:assert/strict';
import { mkdtempSync, rmSync, readdirSync, writeFileSync } from 'fs';
import { tmpdir } from 'os';
import { join } from 'path';

// Isolate the durable store to a throwaway directory before the module is required,
// so these tests never touch a developer's real data directory.
const testDir = mkdtempSync(join(tmpdir(), 'bhashasetu-persist-'));
process.env.BHASHASETU_DATA_DIR = testDir;

import { PersistenceService } from '../src/common/persistence.service';
import { LessonsService } from '../src/lessons/lessons.service';

describe('BhashaSetu Durability & Lesson Lifecycle Invariants', () => {
  let persistence: PersistenceService;

  beforeEach(() => {
    persistence = new PersistenceService();
    persistence.onModuleInit();
    persistence.resetAll();
  });

  after(() => {
    try {
      rmSync(testDir, { recursive: true, force: true });
    } catch {
      /* best effort */
    }
  });

  it('should persist state so it survives a simulated process boundary', () => {
    const first = new LessonsService(undefined, persistence);
    const created = first.create({ title: 'SURVIVES RESTART', hindiPrompt: 'परीक्षण' });
    persistence.flushAll();

    // A brand-new service instance backed by the same directory must see the record.
    const second = new PersistenceService();
    second.onModuleInit();
    const reloaded = new LessonsService(undefined, second);

    assert.ok(
      reloaded.findAll().some((l) => l.id === created.id),
      'a lesson written by one instance must be visible to a fresh instance'
    );
    assert.equal(reloaded.findOne(created.id).title, 'SURVIVES RESTART');
  });

  it('should write atomically and leave no temp files behind', () => {
    persistence.write('atomic', [{ id: 1 }]);
    persistence.flushAll();

    const files = readdirSync(testDir);
    assert.ok(files.includes('atomic.json'));
    assert.equal(
      files.filter((f) => f.includes('.tmp-')).length,
      0,
      'temp files must be renamed away, never left on disk'
    );
  });

  it('should quarantine a corrupt record file instead of crashing', () => {
    writeFileSync(join(testDir, 'broken.json'), '{ not valid json', 'utf8');

    const recovered = persistence.read('broken', { safe: true });
    assert.deepEqual(recovered, { safe: true }, 'corrupt file must fall back, not throw');
    assert.ok(
      readdirSync(testDir).some((f) => f.startsWith('broken.json.corrupt-')),
      'corrupt file must be preserved for forensics rather than silently destroyed'
    );
  });

  it('should refuse to publish a lesson that no teacher has approved', () => {
    const lessons = new LessonsService(undefined, persistence);
    const draft = lessons.create({ title: 'UNAPPROVED', hindiPrompt: 'x' });

    assert.equal(draft.status, 'REVIEW_REQUIRED');
    assert.throws(
      () => lessons.publish(draft.id),
      /has not been approved by a teacher/,
      'publishing unapproved AI output into state curriculum must be blocked'
    );
  });

  it('should enforce the lesson lifecycle state machine', () => {
    const lessons = new LessonsService(undefined, persistence);
    const draft = lessons.create({ title: 'LIFECYCLE', hindiPrompt: 'x' });

    assert.equal(lessons.approve(draft.id).status, 'APPROVED');
    assert.equal(lessons.publish(draft.id).status, 'PUBLISHED');

    // PUBLISHED is intentionally terminal until archived.
    assert.throws(() => lessons.approve(draft.id), /Illegal lesson transition/);
    assert.equal(lessons.archive(draft.id).status, 'ARCHIVED');
    assert.throws(() => lessons.publish(draft.id), /Illegal lesson transition/);
  });

  it('should not let the concurrency limiter underflow below zero', () => {
    const { AiClientService } = require('../src/ai-client/ai-client.service');
    const client = new AiClientService();
    const release = (client as any).releaseSlot.bind(client);

    release();
    release();
    assert.equal(
      (client as any).activeRequests,
      0,
      'repeated releases must clamp at zero, never go negative'
    );
  });
});
