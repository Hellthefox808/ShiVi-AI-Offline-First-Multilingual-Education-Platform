import { Controller, Post, Get, Body, HttpCode, HttpStatus } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse } from '@nestjs/swagger';
import { IsArray, IsBoolean, IsIn, IsInt, IsNumber, IsOptional, IsString, Max, Min, MinLength, ValidateNested } from 'class-validator';
import { Type } from 'class-transformer';
import { AiClientService } from './ai-client.service';
import { TargetLanguage } from '@bhashasetu/contracts';

const SPEAKER_ROLES = ['TEACHER', 'STUDENT'] as const;
export const TARGET_LANGUAGES = ['SANTHALI', 'HO', 'MUNDARI'] as const;

export class PronunciationScoreDto {
  @IsString()
  @MinLength(1)
  referenceText!: string;

  @IsString()
  spokenTranscript!: string;

  @IsIn(TARGET_LANGUAGES)
  targetLanguage!: TargetLanguage;

  @IsOptional()
  @IsString()
  referenceDevanagari?: string;

  @IsOptional()
  @IsInt()
  spokenDurationMs?: number;

  @IsOptional()
  @IsString()
  studentId?: string;
}

class PronunciationAttemptDto {
  @IsOptional()
  @IsString()
  studentId?: string;

  @IsString()
  @MinLength(1)
  referenceText!: string;

  @IsString()
  spokenTranscript!: string;

  @IsOptional()
  @IsString()
  referenceDevanagari?: string;

  @IsOptional()
  @IsInt()
  spokenDurationMs?: number;
}

export class PronunciationBatchDto {
  @IsIn(TARGET_LANGUAGES)
  targetLanguage!: TargetLanguage;

  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => PronunciationAttemptDto)
  attempts!: PronunciationAttemptDto[];
}

export class BatchVoiceTranslateDto {
  @IsArray()
  @IsString({ each: true })
  transcripts!: string[];

  @IsIn(TARGET_LANGUAGES)
  targetLanguage!: TargetLanguage;

  @IsOptional()
  @IsIn(SPEAKER_ROLES)
  speakerRole?: 'TEACHER' | 'STUDENT';

  @IsOptional()
  @IsBoolean()
  flnMode?: boolean;

  @IsOptional()
  @IsBoolean()
  bilingualRelay?: boolean;

  @IsOptional()
  @IsString()
  voiceTimbre?: string;

  @IsOptional()
  @IsNumber()
  @Min(0.5)
  @Max(1.5)
  speechRate?: number;

  @IsOptional()
  @IsNumber()
  @Min(0.5)
  @Max(2.0)
  pitch?: number;
}

export class VoiceTranslateDto {
  @IsOptional()
  @IsString()
  audioBase64?: string;

  @IsOptional()
  @IsString()
  transcriptionHindi?: string;

  @IsIn(TARGET_LANGUAGES)
  targetLanguage!: TargetLanguage;

  @IsOptional()
  @IsBoolean()
  flnMode?: boolean;

  @IsOptional()
  @IsBoolean()
  bilingualRelay?: boolean;

  @IsOptional()
  @IsIn(SPEAKER_ROLES)
  speakerRole?: 'TEACHER' | 'STUDENT';
}

export class CreateVoiceSessionDto {
  @IsIn(TARGET_LANGUAGES)
  targetLanguage!: TargetLanguage;

  @IsOptional()
  @IsIn(SPEAKER_ROLES)
  speakerRole?: 'TEACHER' | 'STUDENT';

  @IsOptional()
  @IsBoolean()
  interruptEnabled?: boolean;

  @IsOptional()
  @IsBoolean()
  flnMode?: boolean;

  @IsOptional()
  @IsBoolean()
  bilingualRelay?: boolean;
}

export class TwoWayVoiceTranslateDto {
  @IsOptional()
  @IsString()
  transcript?: string;

  @IsOptional()
  @IsString()
  studentTribalTranscript?: string;

  @IsOptional()
  @IsString()
  hindiTranscript?: string;

  @IsIn(TARGET_LANGUAGES)
  targetLanguage!: TargetLanguage;

  @IsOptional()
  @IsIn(SPEAKER_ROLES)
  speakerRole?: 'TEACHER' | 'STUDENT';

  @IsOptional()
  @IsBoolean()
  flnMode?: boolean;

  @IsOptional()
  @IsBoolean()
  bilingualRelay?: boolean;
}

export class WebRtcOfferDto {
  @IsString()
  @MinLength(1)
  sdp!: string;

  @IsOptional()
  @IsString()
  type?: string;

  @IsOptional()
  @IsString()
  sessionId?: string;

  @IsIn(TARGET_LANGUAGES)
  targetLanguage!: TargetLanguage;

  @IsOptional()
  @IsIn(SPEAKER_ROLES)
  speakerRole?: 'TEACHER' | 'STUDENT';
}

export class LiveKitTokenDto {
  @IsString()
  @MinLength(1)
  roomName!: string;

  @IsString()
  @MinLength(1)
  participantName!: string;

  @IsOptional()
  @IsIn(['speaker', 'listener'])
  role?: 'speaker' | 'listener';

  @IsIn(TARGET_LANGUAGES)
  targetLanguage!: TargetLanguage;
}

@ApiTags('voice')
@Controller('voice')
export class VoiceController {
  constructor(private readonly aiClientService: AiClientService) {}

  @Post('translate')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Real-time Live Voice Translation & Relay Dispatcher',
    description:
      'Translates spoken Hindi classroom speech into tribal target language (Santhali Ol Chiki, Ho Warang Chiti, Mundari) with phonetic relay metadata (450ms pause, hi-IN acoustic synthesis, FLN 0.72x speed).',
  })
  @ApiResponse({
    status: 200,
    description: 'Voice turn translated successfully with phonetic relay instructions',
  })
  async translateVoice(@Body() body: VoiceTranslateDto) {
    return this.aiClientService.translateVoiceTurn(body);
  }

  @Post('session')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Negotiate Real-Time Voice AI Streaming Session',
    description:
      'Issues ephemeral session credentials, WebRTC ICE servers, and WebSocket streaming URL for sub-300ms bidirectional voice agents.',
  })
  @ApiResponse({
    status: 200,
    description: 'Voice session created with streaming endpoint and token',
  })
  async createSession(@Body() body: CreateVoiceSessionDto) {
    return this.aiClientService.createVoiceSession(body);
  }

  @Post('translate/batch')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Batch Voice Translation for Pre-Recorded Classroom Audio',
    description:
      'Translates many spoken utterances in one request so a tablet regaining connectivity does not issue one round-trip per sentence over a slow 2G link. Reports per-entry errors instead of failing the whole batch.',
  })
  @ApiResponse({ status: 200, description: 'Batch processed with per-entry results' })
  async batchTranslateVoice(@Body() body: BatchVoiceTranslateDto) {
    return this.aiClientService.batchVoiceTranslate(body);
  }

  @Post('pronunciation/score')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Score a Child\'s Tribal Pronunciation Attempt (FLN)',
    description:
      'Returns word-level accuracy, syllable drill-down, oral reading fluency against NIPUN Bharat benchmarks, and Devanagari remediation guidance. Scored as a linguistic transcript match; no tribal acoustic model is deployed, and every response says so explicitly.',
  })
  @ApiResponse({ status: 200, description: 'Attempt scored with diagnostic breakdown' })
  @ApiResponse({ status: 422, description: 'Reference text missing or unusable' })
  async scorePronunciation(@Body() body: PronunciationScoreDto) {
    return this.aiClientService.scorePronunciation(body);
  }

  @Post('pronunciation/batch')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Score a Whole Classroom\'s Pronunciation Attempts',
    description:
      'Scores every student attempt in one call and returns a class average plus a needs-attention count, so a teacher can review the session offline.',
  })
  @ApiResponse({ status: 200, description: 'Classroom attempts scored and aggregated' })
  async scorePronunciationBatch(@Body() body: PronunciationBatchDto) {
    return this.aiClientService.scorePronunciationBatch(body);
  }

  @Get('capabilities')
  @ApiOperation({
    summary: 'Voice Capability & Limitation Declaration',
    description:
      'Declares which voice features this deployment actually serves, including explicit limitations, so clients degrade gracefully instead of calling an endpoint that is not backed.',
  })
  getVoiceCapabilities() {
    return this.aiClientService.getVoiceCapabilities();
  }

  @Post('two-way')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Bidirectional Voice Translation (Teacher & Student)',
    description:
      'Translates teacher Hindi prompts to native tribal script or student tribal responses to Hindi comprehension.',
  })
  @ApiResponse({
    status: 200,
    description: 'Two-way voice turn processed successfully',
  })
  async translateTwoWayVoice(@Body() body: TwoWayVoiceTranslateDto) {
    return this.aiClientService.translateTwoWayVoiceTurn(body);
  }

  @Post('webrtc/offer')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'WebRTC SDP Offer / Answer Handshake',
    description:
      'Negotiates client SDP offer with STUN/TURN ICE candidates and active audio transceivers for real-time voice streaming.',
  })
  @ApiResponse({
    status: 200,
    description: 'WebRTC answer generated successfully',
  })
  async negotiateWebRtcOffer(@Body() body: WebRtcOfferDto) {
    return this.aiClientService.negotiateWebRtcOffer(body);
  }

  @Post('livekit/token')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'LiveKit Room Token Minting',
    description:
      'Generates signed JWT room tokens and SFU endpoint connection details for low-latency WebRTC mesh or multi-participant audio.',
  })
  @ApiResponse({
    status: 200,
    description: 'LiveKit room token minted successfully',
  })
  async generateLiveKitToken(@Body() body: LiveKitTokenDto) {
    return this.aiClientService.generateLiveKitToken(body);
  }

  @Get('circuit-status')
  @ApiOperation({
    summary: 'Circuit Breaker, Load Balancer & Cache Telemetry',
    description: 'Returns real-time gateway resilience metrics, endpoint pools, and query cache hit rates.',
  })
  getCircuitStatus() {
    return this.aiClientService.getCircuitBreakerStatus();
  }
}

