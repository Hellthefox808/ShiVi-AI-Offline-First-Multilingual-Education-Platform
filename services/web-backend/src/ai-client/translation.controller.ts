import { Controller, Post, Get, Body, Query, HttpCode, HttpStatus } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiQuery } from '@nestjs/swagger';
import { AiClientService } from './ai-client.service';
import { TargetLanguage } from '@bhashasetu/contracts';

export class SingleTranslateDto {
  text!: string;
  targetLanguage!: TargetLanguage;
  sourceLanguage?: string;
  speakerRole?: string;
  flnMode?: boolean;
  includeAlignment?: boolean;
}

export class TransliterateDto {
  text!: string;
  sourceScript?: string;
  targetScript?: string;
  language?: TargetLanguage;
}

export class DetectLanguageDto {
  text!: string;
}

export class AlignTokensDto {
  text!: string;
  targetLanguage!: TargetLanguage;
}

export class BackTranslateDto {
  text!: string;
  targetLanguage!: TargetLanguage;
}

export class DialectAdaptDto {
  text!: string;
  targetLanguage!: TargetLanguage;
  dialectRegion?: string;
}

@ApiTags('translate')
@Controller('translate')
export class TranslationController {
  constructor(private readonly aiClientService: AiClientService) {}

  @Post()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Direct Multilingual Translation with Phonetics & FLN Adaptation',
    description:
      'Translates source classroom instruction into target indigenous language (Santhali, Ho, Mundari) with dual transliteration (Devanagari & Latin).',
  })
  async translate(@Body() dto: SingleTranslateDto) {
    return this.aiClientService.translateText(dto);
  }

  @Get('glossary')
  @ApiOperation({
    summary: 'Pedagogical Indigenous Glossary Repository',
    description: 'Retrieves curated MTB-MLE vocabulary entries across 7 curriculum domains.',
  })
  @ApiQuery({ name: 'category', required: false })
  @ApiQuery({ name: 'language', required: false, enum: ['SANTHALI', 'HO', 'MUNDARI'] })
  async getGlossary(
    @Query('category') category?: string,
    @Query('language') language?: TargetLanguage,
  ) {
    return this.aiClientService.getGlossary({ category, language });
  }

  @Get('glossary/search')
  @ApiOperation({
    summary: 'Search Indigenous Glossary Repository',
    description: 'Searches vocabulary terms by Hindi, native tribal script, or phonetic transliteration.',
  })
  @ApiQuery({ name: 'q', required: true })
  @ApiQuery({ name: 'language', required: false, enum: ['SANTHALI', 'HO', 'MUNDARI'] })
  async searchGlossary(
    @Query('q') q: string,
    @Query('language') language?: TargetLanguage,
  ) {
    return this.aiClientService.searchGlossary({ query: q, language });
  }

  @Get('glossary/categories')
  @ApiOperation({
    summary: 'Glossary Category Taxonomy Metadata',
    description: 'Retrieves all 7 pedagogical categories with English and Hindi titles.',
  })
  async getGlossaryCategories() {
    return this.aiClientService.getGlossaryCategories();
  }

  @Post('transliterate')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Grapheme-to-Phoneme Script Transliteration',
    description: 'Converts text between Ol Chiki, Devanagari, and Latin orthography.',
  })
  async transliterate(@Body() dto: TransliterateDto) {
    return this.aiClientService.transliterate(dto);
  }

  @Post('detect')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Language & Script Identification',
    description: 'Identifies Unicode script block and lexical markers for indigenous Jharkhand languages.',
  })
  async detect(@Body() dto: DetectLanguageDto) {
    return this.aiClientService.detectLanguage(dto);
  }

  @Post('align')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Phonetic & Token-by-Token Bilingual Alignment',
    description: 'Extracts word-level alignments between Hindi source and tribal target tokens.',
  })
  async align(@Body() dto: AlignTokensDto) {
    return this.aiClientService.alignTokens(dto);
  }

  @Post('back-translate')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Dual-Hop Roundtrip Translation Consistency Audit',
    description: 'Translates source -> tribal -> reverse Hindi to verify pedagogical fidelity.',
  })
  async backTranslate(@Body() dto: BackTranslateDto) {
    return this.aiClientService.backTranslate(dto);
  }

  @Post('dialect')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'District Dialect & Regional Orthography Adaptation',
    description:
      'Adapts standard literary translations to district dialects (Kolhan, Santhal Pargana, Seraikela, Naguri).',
  })
  async adaptDialect(@Body() dto: DialectAdaptDto) {
    return this.aiClientService.adaptDialect(dto);
  }
}
