import { Module } from '@nestjs/common';
import { AiClientService } from './ai-client.service';
import { VoiceController } from './voice.controller';
import { TranslationController } from './translation.controller';

@Module({
  controllers: [VoiceController, TranslationController],
  providers: [AiClientService],
  exports: [AiClientService]
})
export class AiClientModule {}
