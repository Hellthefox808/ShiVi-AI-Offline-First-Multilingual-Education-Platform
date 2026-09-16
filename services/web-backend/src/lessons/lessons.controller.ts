import { Controller, Get, Post, Put, Body, Param } from '@nestjs/common';
import { LessonsService } from './lessons.service';
import { ApiTags, ApiOperation, ApiResponse } from '@nestjs/swagger';

@ApiTags('lessons')
@Controller('lessons')
export class LessonsController {
  constructor(private readonly lessonsService: LessonsService) {}

  @Get()
  @ApiOperation({ summary: 'List all lesson plans across curriculum nodes' })
  @ApiResponse({ status: 200, description: 'List of lessons returned successfully' })
  listLessons() {
    const data = this.lessonsService.findAll();
    return { count: data.length, data };
  }

  @Get(':id')
  @ApiOperation({ summary: 'Get a specific lesson by ID' })
  getLesson(@Param('id') id: string) {
    return this.lessonsService.findOne(id);
  }

  @Get(':id/worksheet')
  @ApiOperation({ summary: 'Get bilingual printable worksheet for a lesson' })
  getWorksheet(@Param('id') id: string) {
    return this.lessonsService.getWorksheet(id);
  }

  @Get(':id/flashcards')
  @ApiOperation({ summary: 'Get multimodal visual flashcard deck for a lesson' })
  getFlashcards(@Param('id') id: string) {
    return this.lessonsService.getFlashcards(id);
  }

  @Post()
  @ApiOperation({ summary: 'Create or scaffold a new lesson plan' })
  createLesson(@Body() body: any) {
    return this.lessonsService.create(body);
  }

  @Post('scaffold-ai')
  @ApiOperation({ summary: 'Scaffold a lesson plan using the AI platform RAG engine with resilient fallback' })
  scaffoldAi(@Body() body: any) {
    return this.lessonsService.scaffoldWithAi(body);
  }


  @Put(':id/approve')
  @ApiOperation({ summary: 'Teacher HITL review approval of lesson' })
  approveLesson(@Param('id') id: string) {
    return this.lessonsService.approve(id);
  }

  @Put(':id/publish')
  @ApiOperation({ summary: 'Publish lesson and queue for offline package sync' })
  publishLesson(@Param('id') id: string) {
    return this.lessonsService.publish(id);
  }
}
