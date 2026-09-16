import { Injectable } from '@nestjs/common';

@Injectable()
export class AppService {
  private readonly startTime = Date.now();

  getHealth() {
    const uptimeSeconds = Math.floor((Date.now() - this.startTime) / 1000);
    const memory = process.memoryUsage();

    return {
      status: 'UP',
      service: 'BhashaSetu NestJS Web Backend Gateway',
      version: '3.0.0-PROD',
      timestamp: new Date().toISOString(),
      uptimeSeconds,
      tenancy: 'ENABLED',
      memory: {
        rssMb: Math.round((memory.rss / (1024 * 1024)) * 100) / 100,
        heapUsedMb: Math.round((memory.heapUsed / (1024 * 1024)) * 100) / 100,
        heapTotalMb: Math.round((memory.heapTotal / (1024 * 1024)) * 100) / 100,
      },
      domains: {
        curriculum: 'HEALTHY',
        lessons: 'HEALTHY',
        sync: 'HEALTHY',
        aiClient: 'HEALTHY',
        auth: 'HEALTHY',
        analytics: 'HEALTHY',
        devices: 'HEALTHY',
        reviews: 'HEALTHY',
        offlinePacks: 'HEALTHY',
        audit: 'HEALTHY',
      },
    };
  }
}
