import { Controller, Post, Get, Body, HttpCode, HttpStatus } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse } from '@nestjs/swagger';
import { AiClientService } from './ai-client.service';
import { TargetLanguage } from '@bhashasetu/contracts';

export class VoiceTranslateDto {
  audioBase64?: string;
  transcriptionHindi?: string;
  targetLanguage!: TargetLanguage;
  flnMode?: boolean;
  bilingualRelay?: boolean;
}

export class CreateVoiceSessionDto {
  targetLanguage!: TargetLanguage;
  speakerRole?: 'TEACHER' | 'STUDENT';
  sampleRate?: number;
  interruptEnabled?: boolean;
  flnMode?: boolean;
  bilingualRelay?: boolean;
}

export class TwoWayVoiceTranslateDto {
  transcript?: string;
  studentTribalTranscript?: string;
  hindiTranscript?: string;
  targetLanguage!: TargetLanguage;
  speakerRole?: 'TEACHER' | 'STUDENT';
  flnMode?: boolean;
  bilingualRelay?: boolean;
}

export class WebRtcOfferDto {
  sdp!: string;
  type?: string;
  sessionId?: string;
  targetLanguage!: TargetLanguage;
  speakerRole?: 'TEACHER' | 'STUDENT';
}

export class LiveKitTokenDto {
  roomName!: string;
  participantName!: string;
  role?: 'speaker' | 'listener';
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
      'Issues ephemeral JWT session credentials, WebRTC ICE servers, and WebSocket streaming URL for sub-300ms bidirectional voice agents.',
  })
  @ApiResponse({
    status: 200,
    description: 'Voice session created with streaming endpoint and token',
  })
  async createSession(@Body() body: CreateVoiceSessionDto) {
    return this.aiClientService.createVoiceSession(body);
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

