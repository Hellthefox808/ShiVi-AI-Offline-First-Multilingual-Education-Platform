import { Module } from '@nestjs/common';
import { AiClientService } from './ai-client.service';
import { VoiceController } from './voice.controller';

@Module({
  controllers: [VoiceController],
  providers: [AiClientService],
  exports: [AiClientService]
})
export class AiClientModule {}
