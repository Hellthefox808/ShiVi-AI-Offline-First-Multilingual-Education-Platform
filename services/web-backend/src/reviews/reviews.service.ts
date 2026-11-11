import { Injectable, NotFoundException, Optional } from '@nestjs/common';
import { PersistenceService } from '../common/persistence.service';

export interface ReviewItem {
  reviewId: string;
  lessonId: string;
  reviewerId: string;
  reviewerName: string;
  targetLanguage: string;
  sourceTextHindi: string;
  originalGeneratedScript: string;
  correctedScript: string;
  phoneticTransliteration: string;
  culturalAccuracyRating: number; // 1 to 5
  orthographicAccuracyRating: number; // 1 to 5
  status: 'PENDING_REVIEW' | 'VERIFIED_CORRECT' | 'REVISED_APPROVED' | 'REJECTED';
  linguistComments: string;
  reviewedAt: string;
}

@Injectable()
export class ReviewsService {
  constructor(@Optional() private readonly persistence?: PersistenceService) {}

  private readonly COLLECTION = 'reviews';

  private readonly seedReviews: ReviewItem[] = [
    {
      reviewId: 'REV-001',
      lessonId: 'LES-001',
      reviewerId: 'USR-002',
      reviewerName: 'Dr. Sunita Soren (Senior Tribal Linguist)',
      targetLanguage: 'SANTHALI',
      sourceTextHindi: 'बच्चों, आज हम स्थानीय पेड़ों और पत्तियों के बारे में सीखेंगे।',
      originalGeneratedScript: 'ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ, ᱛᱮᱦᱮᱧ ᱟᱵᱚ ᱫᱟᱨᱮ ᱟᱨ ᱥᱟᱠᱟᱢ ᱨᱮᱱᱟᱜ ᱨᱩᱯ ᱵᱟᱵᱚᱛ ᱛᱮᱵᱚᱱ ᱪᱮᱫᱚᱜᱼᱟ᱾',
      correctedScript: 'ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ, ᱛᱮᱦᱮᱧ ᱟᱵᱚ ᱫᱟᱨᱮ ᱟᱨ ᱥᱟᱠᱟᱢ ᱨᱮᱱᱟᱜ ᱨᱩᱯ ᱵᱟᱵᱚᱛ ᱛᱮᱵᱚᱱ ᱪᱮᱫᱚᱜᱼᱟ᱾',
      phoneticTransliteration: 'Gidra ko, tehenj abo dare aar sakam renag roop babot tebon chedog-aa.',
      culturalAccuracyRating: 5,
      orthographicAccuracyRating: 5,
      status: 'VERIFIED_CORRECT',
      linguistComments: 'Ol Chiki orthography is authentic. Sarhul Sal tree analogy aligns with tribal tradition.',
      reviewedAt: '2026-08-25T11:00:00.000Z'
    }
  ];

  private get reviews(): ReviewItem[] {
    if (!this.persistence) {
      if (!this.memoryFallback) this.memoryFallback = [...this.seedReviews];
      return this.memoryFallback;
    }
    return this.persistence.read<ReviewItem[]>(this.COLLECTION, [...this.seedReviews]);
  }

  private memoryFallback: ReviewItem[] | null = null;

  private persist(): void {
    if (this.persistence) {
      this.persistence.write(this.COLLECTION, this.reviews);
    }
  }

  findAll(): ReviewItem[] {
    return this.reviews;
  }

  findOne(reviewId: string): ReviewItem {
    const item = this.reviews.find((r) => r.reviewId === reviewId);
    if (!item) {
      throw new NotFoundException(`Review record ${reviewId} not found`);
    }
    return item;
  }

  submitReview(payload: Partial<ReviewItem>): ReviewItem {
    const newReview: ReviewItem = {
      reviewId: `REV-${Date.now().toString().slice(-6)}`,
      lessonId: payload.lessonId || 'LES-001',
      reviewerId: payload.reviewerId || 'USR-002',
      reviewerName: payload.reviewerName || 'Native Linguist Reviewer',
      targetLanguage: payload.targetLanguage || 'SANTHALI',
      sourceTextHindi: payload.sourceTextHindi || 'मूल हिंदी पाठ',
      originalGeneratedScript: payload.originalGeneratedScript || '',
      correctedScript: payload.correctedScript || payload.originalGeneratedScript || '',
      phoneticTransliteration: payload.phoneticTransliteration || '',
      culturalAccuracyRating: payload.culturalAccuracyRating || 5,
      orthographicAccuracyRating: payload.orthographicAccuracyRating || 5,
      status: payload.status || 'REVISED_APPROVED',
      linguistComments: payload.linguistComments || 'सत्यापित भाषा अनुवाद',
      reviewedAt: new Date().toISOString()
    };
    this.reviews.unshift(newReview);
    this.persist();
    return newReview;
  }
}
