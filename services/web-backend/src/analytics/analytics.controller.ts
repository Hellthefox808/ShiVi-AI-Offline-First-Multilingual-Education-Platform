import { Controller, Get, Query } from '@nestjs/common';
import { AnalyticsService } from './analytics.service';
import { ApiTags, ApiOperation, ApiResponse, ApiQuery } from '@nestjs/swagger';

@ApiTags('analytics')
@Controller('analytics')
export class AnalyticsController {
  constructor(private readonly analyticsService: AnalyticsService) {}

  @Get('district-summary')
  @ApiOperation({ summary: 'Get real-time district FLN attainment and sync telemetry' })
  @ApiQuery({ name: 'district', required: false, example: 'Dumka' })
  @ApiResponse({ status: 200, description: 'District telemetry retrieved successfully' })
  getDistrictSummary(@Query('district') district?: string) {
    return this.analyticsService.getDistrictSummary(district);
  }

  @Get('districts')
  @ApiOperation({ summary: 'Get list of covered tribal districts with telemetry summary' })
  @ApiResponse({ status: 200, description: 'List of districts' })
  getDistricts() {
    return this.analyticsService.getDistricts();
  }
}
