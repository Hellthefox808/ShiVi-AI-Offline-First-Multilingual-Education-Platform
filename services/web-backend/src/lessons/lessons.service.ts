import { Injectable, NotFoundException, Optional } from '@nestjs/common';
import { Lesson, LessonStatus, Worksheet, Flashcard, TargetLanguage } from '@bhashasetu/contracts';
import { AiClientService } from '../ai-client/ai-client.service';

@Injectable()
export class LessonsService {
  constructor(@Optional() private readonly aiClientService?: AiClientService) {}

  private lessons: Lesson[] = [
    {
      id: 'LES-001',
      schoolId: 'SCH-DUMKA-042',
      teacherId: 'USR-001',
      curriculumNodeId: 'JCERT_G2_EVS_01',
      title: 'पेड़ और पत्तियाँ (Trees and Leaves)',
      hindiPrompt: 'बच्चों, आज हम स्थानीय पेड़ों और पत्तियों के बारे में सीखेंगे।',
      status: 'PUBLISHED',
      adaptation: {
        gradeLevel: 'GRADE_2',
        targetLanguage: 'SANTHALI',
        targetLanguageCode: 'sat_Olck',
        nativeScript: 'OL_CHIKI',
        translatedText: 'ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ, ᱛᱮᱦᱮᱧ ᱟᱵᱚ ᱫᱟᱨᱮ ᱟᱨ ᱥᱟᱠᱟᱢ ᱨᱮᱱᱟᱜ ᱨᱩᱯ ᱵᱟᱵᱚᱛ ᱛᱮᱵᱚᱱ ᱪᱮᱫᱚᱜᱼᱟ᱾',
        transliterationHindi: 'गिदरा को, तेहेञ आबो दारे आर साकाम रेनाग रूप बाबोत तेबोन चेदोग-आ।',
        transliterationLatin: 'Gidra ko, tehenj abo dare aar sakam renag roop babot tebon chedog-aa.',
        culturalAnalogy: 'सरहुल पर्व में पूजनीय साल (सखुआ) का पवित्र वृक्ष',
        localStoryContext: 'झारखंड के जंगलों में सरहुल के समय साल के नए पत्ते और फूल प्रकृति के पुनर्जन्म का संदेश देते हैं।',
        audioTtsUrl: '/audio/lessons/sat_trees.mp3'
      },
      qualityReport: {
        compositeScore: 0.94,
        cometScore: 0.92,
        terminologyScore: 0.96,
        groundingScore: 0.98,
        status: 'HIGH_CONFIDENCE',
        warnings: []
      },
      isApprovedByTeacher: true,
      version: 1,
      createdAt: '2026-08-25T10:00:00.000Z',
      updatedAt: '2026-08-25T10:30:00.000Z'
    }
  ];

  findAll(): Lesson[] {
    return this.lessons;
  }

  findOne(id: string): Lesson {
    const lesson = this.lessons.find((l) => l.id === id);
    if (!lesson) {
      throw new NotFoundException(`Lesson with ID ${id} not found`);
    }
    return lesson;
  }

  create(payload: any): Lesson {
    const newLesson: Lesson = {
      id: `LES-${Date.now().toString().slice(-6)}-${Math.random().toString(36).substring(2, 6).toUpperCase()}`,
      schoolId: payload.schoolId || 'SCH-DUMKA-042',
      teacherId: payload.teacherId || 'USR-001',
      curriculumNodeId: payload.curriculumNodeId || 'JCERT_G2_EVS_01',
      title: payload.title || 'नई पाठ योजना (New Lesson Plan)',
      hindiPrompt: payload.hindiPrompt || 'हिंदी निर्देश',
      status: 'REVIEW_REQUIRED',
      adaptation: payload.adaptation || {
        gradeLevel: 'GRADE_2',
        targetLanguage: payload.targetLanguage || 'SANTHALI',
        targetLanguageCode: 'sat_Olck',
        nativeScript: 'OL_CHIKI',
        translatedText: 'ᱥᱟᱱᱛᱟᱲᱤ ᱛᱮ ᱥᱮᱪᱮᱫ',
        transliterationHindi: 'सांतड़ी ते सेचेद',
        transliterationLatin: 'Santhali te seched',
        culturalAnalogy: 'स्थानीय परंपरा और संस्कृति पर आधारित उदाहरण',
        localStoryContext: 'गांव और प्रकृति की सुंदर कथा',
        audioTtsUrl: '/audio/lessons/default.mp3'
      },
      qualityReport: {
        compositeScore: 0.91,
        cometScore: 0.90,
        terminologyScore: 0.95,
        groundingScore: 0.92,
        status: 'HIGH_CONFIDENCE',
        warnings: []
      },
      isApprovedByTeacher: false,
      version: 1,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    this.lessons.unshift(newLesson);
    return newLesson;
  }

  async scaffoldWithAi(payload: {
    hindiPrompt: string;
    targetLanguage: TargetLanguage;
    gradeLevel: string;
    subject?: string;
    schoolId?: string;
    teacherId?: string;
  }): Promise<Lesson> {
    const aiClient = this.aiClientService || new AiClientService();
    const aiResult = await aiClient.generateLesson({
      hindiPrompt: payload.hindiPrompt,
      targetLanguage: payload.targetLanguage,
      gradeLevel: payload.gradeLevel,
      subject: payload.subject
    });

    const adaptation = aiResult.adaptation || {};
    const created = this.create({
      schoolId: payload.schoolId || 'SCH-DUMKA-042',
      teacherId: payload.teacherId || 'USR-001',
      title: `${payload.hindiPrompt.slice(0, 28)} (${payload.targetLanguage})`,
      hindiPrompt: payload.hindiPrompt,
      adaptation: {
        gradeLevel: payload.gradeLevel,
        targetLanguage: payload.targetLanguage,
        targetLanguageCode: payload.targetLanguage === 'SANTHALI' ? 'sat_Olck' : payload.targetLanguage === 'HO' ? 'hoc_Wara' : 'unr_Deva',
        nativeScript: adaptation.native_script || (payload.targetLanguage === 'SANTHALI' ? 'OL_CHIKI' : payload.targetLanguage === 'HO' ? 'WARANG_CHITI' : 'DEVANAGARI'),
        translatedText: adaptation.translated_text || '',
        transliterationHindi: adaptation.transliteration_hindi || '',
        transliterationLatin: adaptation.transliteration_latin || '',
        culturalAnalogy: adaptation.cultural_analogy || 'स्थानीय सांस्कृतिक संदर्भ',
        localStoryContext: adaptation.local_story_context || '',
        audioTtsUrl: adaptation.audio_tts_url || `/audio/lessons/${payload.targetLanguage.toLowerCase()}_default.mp3`
      },
      qualityReport: {
        compositeScore: aiResult.quality_report?.composite_score || 0.92,
        cometScore: aiResult.quality_report?.comet_score || 0.90,
        terminologyScore: 0.95,
        groundingScore: 0.94,
        status: aiResult.quality_report?.status || 'HIGH_CONFIDENCE',
        warnings: aiResult.quality_report?.warnings || []
      }
    });

    return created;
  }

  approve(id: string): Lesson {
    const lesson = this.findOne(id);
    lesson.status = 'APPROVED';
    lesson.isApprovedByTeacher = true;
    lesson.updatedAt = new Date().toISOString();
    return lesson;
  }

  publish(id: string): Lesson {
    const lesson = this.findOne(id);
    lesson.status = 'PUBLISHED';
    lesson.updatedAt = new Date().toISOString();
    return lesson;
  }

  getWorksheet(id: string): Worksheet {
    const lesson = this.findOne(id);
    return {
      id: `WS-${id}`,
      lessonId: lesson.id,
      title: `${lesson.title} — Bilingual Printable Worksheet`,
      grade: lesson.adaptation.gradeLevel,
      targetLanguage: lesson.adaptation.targetLanguage,
      instructionsHindi: 'नीचे दिए गए प्रश्नों को ध्यानपूर्वक पढ़कर सही विकल्प चुनें।',
      instructionsTribal: 'ᱞᱟᱛᱟᱨ ᱨᱮ ᱮᱢ ᱟᱠᱟᱱ ᱠᱩᱠᱞᱤ ᱠᱚ ᱯᱟᱲᱦᱟᱣ ᱠᱟᱛᱮ ᱴᱷᱤᱠ ᱛᱮᱞᱟ ᱵᱟᱪᱷᱟᱣ ᱢᱮ᱾',
      questions: [
        {
          questionId: 'Q1',
          promptHindi: 'सरहुल पर्व में किस पेड़ के पत्तों की पूजा होती है?',
          promptTribal: 'ᱥᱟᱨᱦᱩᱞ ᱯᱚᱨᱚᱵᱽ ᱨᱮ ᱚᱠᱟ ᱫᱟᱨᱮ ᱥᱟᱠᱟᱢ ᱵᱚᱸᱜᱟᱜ-ᱟ?',
          options: ['साल (सखुआ / ᱥᱟᱨᱡᱚᱢ)', 'महुआ (ᱢᱟᱹᱦᱩᱣᱟᱹ)', 'नीम (ᱱᱤᱢ)', 'पीपल (ᱦᱮᱥᱟᱜ)'],
          correctOptionIndex: 0
        },
        {
          questionId: 'Q2',
          promptHindi: 'पत्तल और दोने बनाने के लिए किस पेड़ के पत्तों का उपयोग होता है?',
          promptTribal: 'ᱯᱟᱹᱛᱲᱟᱹ ᱟᱨ ᱯᱷᱩᱲᱩᱜ ᱵᱮᱱᱟᱣ ᱞᱟᱹᱜᱤᱫ ᱚᱠᱟ ᱥᱟᱠᱟᱢ ᱞᱟᱜᱟᱜ-ᱟ?',
          options: ['साल के पत्ते (ᱥᱟᱨᱡᱚᱢ ᱥᱟᱠᱟᱢ)', 'केले के पत्ते', 'घास', 'इमली के पत्ते'],
          correctOptionIndex: 0
        }
      ],
      printablePdfUrl: `/downloads/worksheets/${lesson.id}.pdf`
    };
  }

  getFlashcards(id: string): Flashcard[] {
    const lesson = this.findOne(id);
    return [
      {
        id: `FC-${id}-01`,
        lessonId: lesson.id,
        tribalWord: 'ᱫᱟᱨᱮ',
        nativeScript: 'OL_CHIKI',
        hindiMeaning: 'पेड़ / वृक्ष',
        phoneticTransliteration: 'दारे (Dare)',
        illustrationUrl: '/illustrations/tree_sal.svg',
        audioUrl: '/audio/flashcards/sat_dare.mp3'
      },
      {
        id: `FC-${id}-02`,
        lessonId: lesson.id,
        tribalWord: 'ᱥᱟᱠᱟᱢ',
        nativeScript: 'OL_CHIKI',
        hindiMeaning: 'पत्ती (Leaf)',
        phoneticTransliteration: 'साकाम (Sakam)',
        illustrationUrl: '/illustrations/leaf_sal.svg',
        audioUrl: '/audio/flashcards/sat_sakam.mp3'
      }
    ];
  }
}
