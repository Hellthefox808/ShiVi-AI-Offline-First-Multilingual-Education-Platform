import { NestFactory, Reflector } from '@nestjs/core';
import { AppModule } from './app.module';
import { AuthGuard } from './auth/auth.guard';
import { TokenService } from './auth/token.service';
import { ValidationPipe } from '@nestjs/common';
import { SwaggerModule, DocumentBuilder } from '@nestjs/swagger';
import { CorrelationIdMiddleware } from './common/middleware/correlation-id.middleware';
import { RateLimitMiddleware } from './common/middleware/rate-limit.middleware';
import { HttpExceptionFilter } from './common/filters/http-exception.filter';
import { Request, Response, NextFunction } from 'express';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  app.enableCors();
  app.setGlobalPrefix('api/v1');

  // Correlation ID + rate limiting (first: every request gets an ID, then rate-limited).
  const correlationIdMiddleware = new CorrelationIdMiddleware();
  const rateLimitMiddleware = new RateLimitMiddleware();
  app.use((req: Request, res: Response, next: NextFunction) => correlationIdMiddleware.use(req, res, next));
  app.use((req: Request, res: Response, next: NextFunction) => rateLimitMiddleware.use(req, res, next));

  // Global structured exception filter: every error response carries code, message,
  // statusCode, timestamp, path, and requestId.
  app.useGlobalFilters(new HttpExceptionFilter());

  // Reject unknown properties and enforce DTO decorators from the voice/other controllers.
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: false,
      transform: true,
    }),
  );

  // Global auth + RBAC: every route requires a signed token unless annotated @Public().
  app.useGlobalGuards(new AuthGuard(app.get(Reflector), app.get(TokenService)));

  // OpenAPI 3.1 Swagger Documentation Setup
  const config = new DocumentBuilder()
    .setTitle('BhashaSetu AI — Web Backend Gateway API')
    .setDescription(
      'Mother-Tongue-Based Multilingual Education (MTB-MLE) API for Primary Schools in Jharkhand (SIH26042)',
    )
    .setVersion('3.0.0-PROD')
    .addTag('auth', 'Authentication, Sessions & RBAC')
    .addTag('curriculum', 'State JCERT / NCERT Curriculum Nodes & Learning Outcomes')
    .addTag('lessons', 'Lesson Lifecycle State Machine, HITL Review & Publishing')
    .addTag('sync', 'Durable Outbox Reconciliation & Delta Generator')
    .addTag('analytics', 'District FLN Telemetry & Sync Health Metrics')
    .addTag('devices', 'Tablet Fleet Management, Hardware Health & Remote Lock')
    .addTag('reviews', 'Native Language Linguist Verification & Review Workflow')
    .addTag('offline-packs', 'Signed Offline Content Bundle Bundler & Distribution')
    .addTag('audit', 'Immutable Security Audit Trail & OpenTelemetry Tracing')
    .addBearerAuth()
    .build();

  const document = SwaggerModule.createDocument(app, config);
  SwaggerModule.setup('api/docs', app, document);

  const port = process.env.PORT || 3001;
  await app.listen(port);
  console.log(`BhashaSetu Web Backend running on http://localhost:${port}/api/v1`);
  console.log(`Swagger OpenAPI Documentation available at http://localhost:${port}/api/docs`);
}
bootstrap();
