import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { PersistenceModule } from './common/persistence.module';
import { AuthModule } from './auth/auth.module';
import { CurriculumModule } from './curriculum/curriculum.module';
import { LessonsModule } from './lessons/lessons.module';
import { SyncModule } from './sync/sync.module';
import { AnalyticsModule } from './analytics/analytics.module';
import { DevicesModule } from './devices/devices.module';
import { ReviewsModule } from './reviews/reviews.module';
import { OfflinePacksModule } from './offline-packs/offline-packs.module';
import { AuditModule } from './audit/audit.module';
import { AiClientModule } from './ai-client/ai-client.module';

@Module({
  imports: [
    PersistenceModule,
    AuthModule,
    CurriculumModule,
    LessonsModule,
    SyncModule,
    AnalyticsModule,
    DevicesModule,
    ReviewsModule,
    OfflinePacksModule,
    AuditModule,
    AiClientModule,
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
