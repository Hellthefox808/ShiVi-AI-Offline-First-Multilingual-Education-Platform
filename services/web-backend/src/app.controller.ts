import { Controller, Get } from '@nestjs/common';
import { AppService } from './app.service';
import { Public } from './auth/auth.guard';

@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  // Health/readiness probes must be reachable without credentials.
  @Public()
  @Get('health')
  getHealth() {
    return this.appService.getHealth();
  }
}
