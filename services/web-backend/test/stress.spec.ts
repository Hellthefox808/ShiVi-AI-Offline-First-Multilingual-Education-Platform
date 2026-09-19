import { describe, it, beforeEach } from 'node:test';
import * as assert from 'node:assert/strict';
import { AiClientService } from '../src/ai-client/ai-client.service';
import { SyncService } from '../src/sync/sync.service';
import { LessonsService } from '../src/lessons/lessons.service';
import { CurriculumService } from '../src/curriculum/curriculum.service';
import { OutboxSyncItem, SyncPushRequest } from '@bhashasetu/contracts';

describe('BhashaSetu Web Backend — High-Concurrency Stress & Circuit Breaker Resilience', () => {
  describe('AiClientService Circuit Breaker, Load Balancing & Query Caching', () => {
    let aiClient: AiClientService;

    beforeEach(() => {
      // Configure multi-target load balancer endpoints
      process.env.AI_PLATFORM_URL = 'http://127.0.0.1:8000,http://127.0.0.1:8001,http://127.0.0.1:8002';
      aiClient = new AiClientService();
    });

    it('should initialize load balancer with multi-target endpoints', () => {
      const status = aiClient.getCircuitBreakerStatus();
      assert.equal(status.circuitState, 'CLOSED');
      assert.equal(status.endpoints.length, 3);
      assert.equal(status.endpoints[0], 'http://127.0.0.1:8000');
      assert.equal(status.endpoints[1], 'http://127.0.0.1:8001');
      assert.equal(status.endpoints[2], 'http://127.0.0.1:8002');
      assert.equal(status.loadBalancerMode, 'ROUND_ROBIN_FAILOVER');
    });

    it('should trip circuit breaker to OPEN after failure threshold and provide fast fallback', async () => {
      // Execute calls to trigger failureThreshold (5)
      for (let i = 0; i < 6; i++) {
        const res = await aiClient.generateLesson({
          hindiPrompt: `फेल टेस्ट प्रॉम्प्ट ${i}`,
          targetLanguage: 'SANTHALI',
          gradeLevel: 'GRADE_2'
        });
        assert.ok(res);
        assert.equal(res.target_language, 'SANTHALI');
      }

      const status = aiClient.getCircuitBreakerStatus();
      assert.equal(status.circuitState, 'OPEN');
      assert.ok(status.consecutiveFailures >= 5);

      // Subsequent call when OPEN should instantly return fallback without waiting on network
      const t0 = Date.now();
      const fastRes = await aiClient.generateLesson({
        hindiPrompt: 'फास्ट फ़ॉलबैक प्रॉम्प्ट',
        targetLanguage: 'HO',
        gradeLevel: 'GRADE_1'
      });
      const durationMs = Date.now() - t0;
      assert.ok(fastRes);
      assert.equal(fastRes.target_language, 'HO');
      assert.ok(durationMs < 20, `Fast fallback must return in < 20ms (was ${durationMs}ms)`);
    });

    it('should cache repeated identical prompts and boost query throughput', async () => {
      aiClient.clearCache();
      aiClient.resetCircuitBreaker();

      const payload = {
        hindiPrompt: 'साल का पेड़ और सरजोम दारे',
        targetLanguage: 'SANTHALI' as const,
        gradeLevel: 'GRADE_2'
      };

      // Cold call
      const res1 = await aiClient.generateLesson(payload);
      assert.ok(res1);

      // Warm call (should hit cache)
      const t0 = Date.now();
      const res2 = await aiClient.generateLesson(payload);
      const cacheLatencyMs = Date.now() - t0;

      assert.deepEqual(res1, res2);
      assert.ok(cacheLatencyMs < 10, `Cached query latency must be < 10ms (was ${cacheLatencyMs}ms)`);

      const status = aiClient.getCircuitBreakerStatus();
      assert.equal(status.cacheStats.hits, 1);
      assert.ok(status.cacheStats.hitRatio > 0);
    });

    it('should handle 100 concurrent voice translation turns gracefully', async () => {
      aiClient.resetCircuitBreaker();
      const concurrency = 100;
      const startTime = Date.now();

      const promises = Array.from({ length: concurrency }).map((_, i) => {
        const lang = i % 2 === 0 ? ('SANTHALI' as const) : ('HO' as const);
        return aiClient.translateVoiceTurn({
          transcriptionHindi: `कक्षा ${i % 5 + 1} के छात्र, आज हम सीखेंगे`,
          targetLanguage: lang,
          flnMode: true,
          bilingualRelay: true
        });
      });

      const results = await Promise.all(promises);
      const totalTimeMs = Date.now() - startTime;

      assert.equal(results.length, concurrency);
      for (const r of results) {
        assert.ok(r.turn_id.startsWith('VOICE-'));
        assert.ok(r.bilingual_relay.enabled);
        assert.equal(r.acoustic_engine, 'hi-IN');
        assert.equal(r.speech_rate, 0.72);
      }

      const avgPerCallMs = totalTimeMs / concurrency;
      console.log(`Backend Voice Stress: ${concurrency} turns finished in ${totalTimeMs}ms (avg ${avgPerCallMs.toFixed(2)}ms/turn)`);
      assert.ok(avgPerCallMs < 25, `Average time per call under load should be < 25ms`);
    });
  });

  describe('SyncService High-Concurrency Outbox Ingestion Stress', () => {
    let syncService: SyncService;
    let lessonsService: LessonsService;
    let curriculumService: CurriculumService;

    beforeEach(() => {
      lessonsService = new LessonsService();
      curriculumService = new CurriculumService();
      syncService = new SyncService(lessonsService, curriculumService);
    });

    it('should process 100 concurrent school edge outbox pushes without race conditions', async () => {
      const concurrency = 100;
      const pushes: Promise<any>[] = [];

      for (let i = 0; i < concurrency; i++) {
        const schoolId = `SCH-DUMKA-${(i % 10) + 1}`;
        const opId = `OP-STRESS-${i}`;
        const item: OutboxSyncItem = {
          id: `ITEM-${opId}`,
          operationId: opId,
          entityType: 'ASSESSMENT_ATTEMPT',
          entityId: `ENT-${opId}`,
          schoolId,
          operation: 'CREATE',
          payload: { studentId: `STU-${i}`, score: 90 + (i % 10) },
          sequenceNo: i + 1,
          timestamp: new Date().toISOString(),
          status: 'PENDING',
          retryCount: 0
        };

        const req: SyncPushRequest = {
          schoolId,
          deviceId: `DEV-TABLET-${i}`,
          operations: [item]
        };

        pushes.push(Promise.resolve(syncService.processPush(req)));
      }

      const responses = await Promise.all(pushes);
      assert.equal(responses.length, concurrency);

      for (let i = 0; i < concurrency; i++) {
        const res = responses[i];
        assert.deepEqual(res.acknowledgedOperationIds, [`OP-STRESS-${i}`]);
        assert.equal(res.conflicts.length, 0);
      }

      // Verify idempotency on repeated push
      const replayReq: SyncPushRequest = {
        schoolId: 'SCH-DUMKA-1',
        deviceId: 'DEV-TABLET-0',
        operations: [
          {
            id: 'ITEM-OP-STRESS-0',
            operationId: 'OP-STRESS-0',
            entityType: 'ASSESSMENT_ATTEMPT',
            entityId: 'ENT-OP-STRESS-0',
            schoolId: 'SCH-DUMKA-1',
            operation: 'CREATE',
            payload: { studentId: 'STU-0', score: 90 },
            sequenceNo: 1,
            timestamp: new Date().toISOString(),
            status: 'PENDING',
            retryCount: 0
          }
        ]
      };

      const replayRes = syncService.processPush(replayReq);
      assert.deepEqual(replayRes.acknowledgedOperationIds, ['OP-STRESS-0']);
      assert.equal(replayRes.conflicts.length, 0);
    });
  });

  describe('LessonsService Concurrent Scaffolding Stress', () => {
    let lessonsService: LessonsService;

    beforeEach(() => {
      lessonsService = new LessonsService();
    });

    it('should create 50 lessons concurrently without identifier collisions', async () => {
      const concurrency = 50;
      const creations = Array.from({ length: concurrency }).map((_, i) => {
        return Promise.resolve(
          lessonsService.create({
            schoolId: `SCH-RANCHI-${(i % 5) + 1}`,
            teacherId: `TCH-${i}`,
            title: `प्रकृति पाठ संख्या ${i}`,
            hindiPrompt: `प्रकृति पाठ संख्या ${i}`,
            targetLanguage: i % 2 === 0 ? 'SANTHALI' : 'HO'
          })
        );
      });

      const results = await Promise.all(creations);
      assert.equal(results.length, concurrency);

      const uniqueIds = new Set(results.map((l) => l.id));
      assert.equal(uniqueIds.size, concurrency, 'All generated lesson IDs must be globally unique');

      for (const l of results) {
        assert.ok(l.id.startsWith('LES-'));
        assert.ok(['SANTHALI', 'HO'].includes(l.adaptation.targetLanguage));
        assert.equal(l.status, 'REVIEW_REQUIRED');
        assert.equal(l.isApprovedByTeacher, false);
      }
    });
  });
});
