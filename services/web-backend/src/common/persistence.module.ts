import { Global, Module } from '@nestjs/common';
import { PersistenceService } from './persistence.service';

/**
 * Global so every domain module can inject the durable store without each one
 * having to re-import it. Marked @Global deliberately: persistence is cross-cutting
 * infrastructure, not a domain concern.
 */
@Global()
@Module({
  providers: [PersistenceService],
  exports: [PersistenceService],
})
export class PersistenceModule {}
