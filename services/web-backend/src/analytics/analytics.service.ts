import { Injectable } from '@nestjs/common';

export interface DistrictDetail {
  district: string;
  primary_tribal_language: string;
  schools: number;
  tablets: number;
  sync_health_percentage: number;
  fln_gain_percentage: number;
  active_teachers: number;
}

@Injectable()
export class AnalyticsService {
  private districtDetails: Record<string, DistrictDetail> = {
    dumka: {
      district: 'Dumka',
      primary_tribal_language: 'SANTHALI',
      schools: 48,
      tablets: 124,
      sync_health_percentage: 99.1,
      fln_gain_percentage: 45.3,
      active_teachers: 76
    },
    'west singhbhum': {
      district: 'West Singhbhum',
      primary_tribal_language: 'HO',
      schools: 42,
      tablets: 110,
      sync_health_percentage: 98.2,
      fln_gain_percentage: 43.1,
      active_teachers: 64
    },
    khunti: {
      district: 'Khunti',
      primary_tribal_language: 'MUNDARI',
      schools: 28,
      tablets: 78,
      sync_health_percentage: 97.9,
      fln_gain_percentage: 41.8,
      active_teachers: 44
    },
    chaibasa: {
      district: 'Chaibasa',
      primary_tribal_language: 'HO',
      schools: 14,
      tablets: 42,
      sync_health_percentage: 98.6,
      fln_gain_percentage: 40.5,
      active_teachers: 22
    },
    pakur: {
      district: 'Pakur',
      primary_tribal_language: 'SANTHALI',
      schools: 10,
      tablets: 32,
      sync_health_percentage: 98.0,
      fln_gain_percentage: 42.0,
      active_teachers: 18
    }
  };

  getDistrictSummary(filterDistrict?: string): {
    state: string;
    districts_covered: string[];
    total_active_schools: number;
    active_tablets: number;
    offline_sync_health_percentage: number;
    total_lessons_generated: number;
    language_distribution: { SANTHALI: number; HO: number; MUNDARI: number };
    fln_grade2_attainment_rate: { baseline: number; current_with_bhashasetu: number; gain_percentage: number };
    nipun_bharat_milestones: { milestone: string; target: string; achieved: string }[];
    district_breakdown: DistrictDetail[];
    filtered_district?: DistrictDetail;
  } {
    const defaultData = {
      state: 'JHARKHAND',
      districts_covered: ['Dumka', 'West Singhbhum', 'Khunti', 'Chaibasa', 'Pakur'],
      total_active_schools: 142,
      active_tablets: 386,
      offline_sync_health_percentage: 98.4,
      total_lessons_generated: 1248,
      language_distribution: {
        SANTHALI: 58,
        HO: 26,
        MUNDARI: 16
      },
      fln_grade2_attainment_rate: {
        baseline: 28.5,
        current_with_bhashasetu: 71.2,
        gain_percentage: 42.7
      },
      nipun_bharat_milestones: [
        { milestone: 'Oral Reading Fluency (Tribal Language)', target: '30-35 wpm', achieved: '33.4 wpm' },
        { milestone: 'Number Recognition (1-99)', target: '80% mastery', achieved: '84.6% mastery' },
        { milestone: 'Bilingual Word Association', target: '75% comprehension', achieved: '82.1% comprehension' }
      ],
      district_breakdown: Object.values(this.districtDetails)
    };

    if (filterDistrict) {
      const key = filterDistrict.trim().toLowerCase();
      const detail = this.districtDetails[key];
      if (detail) {
        return {
          ...defaultData,
          filtered_district: detail,
          active_tablets: detail.tablets,
          total_active_schools: detail.schools,
          offline_sync_health_percentage: detail.sync_health_percentage
        };
      }
    }

    return defaultData;
  }

  getDistricts() {
    return Object.values(this.districtDetails);
  }
}

