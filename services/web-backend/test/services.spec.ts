import { describe, it, beforeEach } from 'node:test';
import * as assert from 'node:assert/strict';
import { AppService } from '../src/app.service';
import { LessonsService } from '../src/lessons/lessons.service';
import { CurriculumService } from '../src/curriculum/curriculum.service';
import { SyncService } from '../src/sync/sync.service';
import { AiClientService } from '../src/ai-client/ai-client.service';
import { AnalyticsService } from '../src/analytics/analytics.service';
import { OutboxSyncItem, SyncPushRequest } from '@bhashasetu/contracts';

describe('BhashaSetu Web Backend Domain Services', () => {
  describe('AppService', () => {
    it('should return complete health diagnostics', () => {
      const appService = new AppService();
      const health = appService.getHealth();

      assert.equal(health.status, 'UP');
      assert.equal(health.service, 'BhashaSetu NestJS Web Backend Gateway');
      assert.equal(health.version, '3.0.0-PROD');
      assert.equal(health.tenancy, 'ENABLED');
      assert.ok(typeof health.uptimeSeconds === 'number');
      assert.ok(health.memory.heapUsedMb > 0);
      assert.equal(health.domains.curriculum, 'HEALTHY');
      assert.equal(health.domains.lessons, 'HEALTHY');
      assert.equal(health.domains.sync, 'HEALTHY');
    });
  });

  describe('LessonsService', () => {
    let lessonsService: LessonsService;

    beforeEach(() => {
      lessonsService = new LessonsService();
    });

    it('should list all initial lessons', () => {
      const lessons = lessonsService.findAll();
      assert.ok(Array.isArray(lessons));
      assert.ok(lessons.length >= 1);
      assert.equal(lessons[0].id, 'LES-001');
      assert.equal(lessons[0].adaptation.targetLanguage, 'SANTHALI');
      assert.equal(lessons[0].adaptation.nativeScript, 'OL_CHIKI');
    });

    it('should retrieve a lesson by valid ID', () => {
      const lesson = lessonsService.findOne('LES-001');
      assert.equal(lesson.id, 'LES-001');
      assert.equal(lesson.curriculumNodeId, 'JCERT_G2_EVS_01');
    });

    it('should throw NotFoundException on non-existent lesson ID', () => {
      assert.throws(() => {
        lessonsService.findOne('LES-NONEXISTENT');
      }, /Lesson with ID LES-NONEXISTENT not found/);
    });

    it('should scaffold and create a new lesson plan', () => {
      const created = lessonsService.create({
        schoolId: 'SCH-RANCHI-007',
        teacherId: 'USR-999',
        title: 'गिनती और संख्या (Numbers)',
        hindiPrompt: 'बच्चों को 1 से 10 तक सिखाएं',
        targetLanguage: 'HO',
      });

      assert.ok(created.id.startsWith('LES-'));
      assert.equal(created.status, 'REVIEW_REQUIRED');
      assert.equal(created.isApprovedByTeacher, false);
      assert.equal(created.schoolId, 'SCH-RANCHI-007');
    });

    it('should approve a lesson and update status to APPROVED', () => {
      const approved = lessonsService.approve('LES-001');
      assert.equal(approved.status, 'APPROVED');
      assert.equal(approved.isApprovedByTeacher, true);
    });

    it('should publish an approved lesson and queue for offline distribution', () => {
      const published = lessonsService.publish('LES-001');
      assert.equal(published.status, 'PUBLISHED');
    });

    it('should generate printable bilingual worksheets and flashcards', () => {
      const worksheet = lessonsService.getWorksheet('LES-001');
      assert.equal(worksheet.lessonId, 'LES-001');
      assert.ok(Array.isArray(worksheet.questions));
      assert.ok(worksheet.questions.length > 0);

      const flashcards = lessonsService.getFlashcards('LES-001');
      assert.ok(Array.isArray(flashcards));
      assert.ok(flashcards.length >= 1);
      assert.equal(flashcards[0].lessonId, 'LES-001');
      assert.ok(flashcards[0].tribalWord.length > 0);
    });
  });

  describe('CurriculumService', () => {
    let curriculumService: CurriculumService;

    beforeEach(() => {
      curriculumService = new CurriculumService();
    });

    it('should list all state-prescribed JCERT nodes', () => {
      const nodes = curriculumService.findAll();
      assert.ok(Array.isArray(nodes));
      assert.ok(nodes.length >= 4);
    });

    it('should filter nodes by grade level', () => {
      const grade2Nodes = curriculumService.findAll({ grade: 'GRADE_2' });
      assert.ok(grade2Nodes.length >= 1);
      for (const node of grade2Nodes) {
        assert.equal(node.grade, 'GRADE_2');
      }
    });

    it('should filter nodes by district', () => {
      const dumkaNodes = curriculumService.findAll({ district: 'Dumka' });
      assert.ok(dumkaNodes.length >= 1);
      for (const node of dumkaNodes) {
        assert.equal(node.district, 'Dumka');
      }
    });

    it('should retrieve a specific node by ID', () => {
      const node = curriculumService.findOne('JCERT_G2_EVS_01');
      assert.ok(node);
      assert.equal(node?.id, 'JCERT_G2_EVS_01');
      assert.equal(node?.chapterTitle, 'हमारे आस-पास के पेड़ और पत्तियाँ');
    });
  });

  describe('SyncService', () => {
    let syncService: SyncService;
    let lessonsService: LessonsService;
    let curriculumService: CurriculumService;

    beforeEach(() => {
      lessonsService = new LessonsService();
      curriculumService = new CurriculumService();
      syncService = new SyncService(lessonsService, curriculumService);
    });

    const createMockOutboxItem = (operationId: string): OutboxSyncItem => ({
      id: `ITEM-${operationId}`,
      operationId,
      entityType: 'ASSESSMENT_ATTEMPT',
      entityId: `ENT-${operationId}`,
      schoolId: 'SCH-DUMKA-042',
      operation: 'CREATE',
      payload: { studentId: 'STU-01', score: 95 },
      sequenceNo: 1,
      timestamp: new Date().toISOString(),
      status: 'PENDING',
      retryCount: 0,
    });

    it('should process outbox push operations and acknowledge them', () => {
      const pushRequest: SyncPushRequest = {
        deviceId: 'TAB-DUMKA-001',
        schoolId: 'SCH-DUMKA-042',
        operations: [createMockOutboxItem('OP-TEST-001')],
      };

      const result = syncService.processPush(pushRequest);

      assert.deepEqual(result.acknowledgedOperationIds, ['OP-TEST-001']);
      assert.equal(result.conflicts.length, 0);
    });

    it('should enforce idempotency by dropping duplicate operation IDs', () => {
      const item = createMockOutboxItem('OP-DUP-001');
      const pushRequest: SyncPushRequest = {
        deviceId: 'TAB-DUMKA-001',
        schoolId: 'SCH-DUMKA-042',
        operations: [item],
      };

      const firstPush = syncService.processPush(pushRequest);
      assert.deepEqual(firstPush.acknowledgedOperationIds, ['OP-DUP-001']);

      // Duplicate push
      const secondPush = syncService.processPush(pushRequest);
      assert.deepEqual(secondPush.acknowledgedOperationIds, ['OP-DUP-001']);
    });

    it('should pull curriculum updates and approved lessons with pagination cursor', () => {
      const pull = syncService.processPull();
      assert.ok(pull.newCursor.startsWith('cursor_'));
      assert.ok(pull.curriculumUpdates.length >= 1);
      assert.ok(Array.isArray(pull.approvedLessons));
    });
  });

  describe('AnalyticsService', () => {
    it('should return district summary and district breakdown', () => {
      const analyticsService = new AnalyticsService();
      const summary = analyticsService.getDistrictSummary();

      assert.equal(summary.state, 'JHARKHAND');
      assert.ok(summary.districts_covered.length >= 5);
      assert.ok(summary.total_active_schools > 0);
      assert.ok(summary.active_tablets > 0);
      assert.ok(summary.offline_sync_health_percentage > 95);
      assert.ok(Array.isArray(summary.district_breakdown));
      assert.ok(summary.district_breakdown.length >= 5);
    });

    it('should filter summary by specific district', () => {
      const analyticsService = new AnalyticsService();
      const dumka = analyticsService.getDistrictSummary('Dumka');

      assert.ok(dumka.filtered_district);
      assert.equal(dumka.filtered_district.district, 'Dumka');
      assert.equal(dumka.filtered_district.primary_tribal_language, 'SANTHALI');
      assert.equal(dumka.total_active_schools, 48);
    });

    it('should return list of all active tribal districts', () => {
      const analyticsService = new AnalyticsService();
      const districts = analyticsService.getDistricts();

      assert.ok(Array.isArray(districts));
      assert.equal(districts.length, 5);
      const names = districts.map((d) => d.district);
      assert.ok(names.includes('Dumka'));
      assert.ok(names.includes('West Singhbhum'));
      assert.ok(names.includes('Khunti'));
    });
  });
});
