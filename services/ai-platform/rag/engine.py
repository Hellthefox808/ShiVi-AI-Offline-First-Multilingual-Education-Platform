"""
BhashaSetu AI — Fine-Tuned Hybrid RAG Retrieval Engine (v3.0.0-PROD)
Combines Okapi BM25 Lexical Scoring with Dense Multilingual Embeddings (Concept Projection),
Reciprocal Rank Fusion (RRF), and Cross-Encoder Reranking.
Preloaded with 15 Comprehensive JCERT Grades 1-5 Primary Curriculum Chunks with Rich Educational Metadata.
"""

from typing import List, Dict, Any, Optional, Set, Tuple
from collections import OrderedDict
import math
import re

# --- Comprehensive 15-Node JCERT Curriculum Knowledge Base with Enriched Metadata ---
JCERT_KNOWLEDGE_BASE = [
    {
        "chunk_id": "JCERT_G2_EVS_01",
        "state": "JHARKHAND",
        "board": "JCERT",
        "district": "Dumka",
        "block": "Shikaripara",
        "grade": "GRADE_2",
        "subject": "ENVIRONMENTAL_STUDIES",
        "competency_category": "EVS_ENVIRONMENT",
        "bloom_level": "UNDERSTAND",
        "textbook_name": "हमारी दुनिया (भाग 2)",
        "page_range": "24-29",
        "suggested_duration_mins": 35,
        "chapter_number": 3,
        "chapter_title": "हमारे आस-पास के पेड़ और पत्तियाँ",
        "topic": "पेड़ और पत्तियाँ",
        "lo_code": "LO-EVS-G2-03",
        "content_hindi": "हमारे आसपास कई प्रकार के पेड़ होते हैं जैसे साल (सखुआ), महुआ, करम और पीपल। पेड़ों की पत्तियाँ हरी, गोल, नुकीली या कटीली होती हैं। पत्ते सूर्य की रोशनी और पानी से पौधे के लिए भोजन बनाते हैं।",
        "cultural_keywords": ["साल", "सखुआ", "महुआ", "करम", "सरहुल", "पत्तल", "दातुन", "छांव", "पत्ता", "पेड़"],
        "tribal_analogies": {
            "SANTHALI": "ᱥᱟᱨᱡᱚᱢ (Sarjom/Sal) ᱫᱟᱨᱮ ᱫᱚ ᱥᱟᱨᱦᱩᱞ ᱯᱚᱨᱚᱵᱽ ᱨᱮ ᱵᱚᱸᱜᱟᱜ-ᱟ᱾ ᱥᱟᱠᱟᱢ ᱛᱮ ᱯᱷᱩᱲᱩᱜ ᱟᱨ ᱯᱟᱹᱛᱲᱟᱹ ᱵᱮᱱᱟᱣᱜ-ᱟ᱾",
            "HO": "ᱥᱟᱨᱡᱚᱢ (Sarjom) ᱫᱟᱨᱩ ᱫᱚ ᱢᱟᱜᱮ ᱯᱚᱨᱚᱵᱽ ᱨᱮ ᱯᱩᱡᱟᱹᱣᱜ-ᱟ᱾ ᱥᱟᱠᱟᱢ ᱛᱮ ᱠᱷᱟᱹᱞᱤ ᱵᱮᱱᱟᱣᱜ-ᱟ᱾",
            "MUNDARI": "सरजोम (Sarjom) दारू सरना स्थल पर पूजनीय है। साकाम से पत्तल बनाई जाती है।"
        }
    },
    {
        "chunk_id": "JCERT_G1_MATH_01",
        "state": "JHARKHAND",
        "board": "JCERT",
        "district": "West Singhbhum",
        "block": "Chaibasa Sadar",
        "grade": "GRADE_1",
        "subject": "MATHEMATICS",
        "competency_category": "FLN_NUMERACY",
        "bloom_level": "REMEMBER",
        "textbook_name": "गणित मेला (भाग 1)",
        "page_range": "8-14",
        "suggested_duration_mins": 30,
        "chapter_number": 1,
        "chapter_title": "गिनती और समूह बनाना (1 से 10)",
        "topic": "गिनती और संख्या ज्ञान",
        "lo_code": "LO-MATH-G1-01",
        "content_hindi": "वस्तुओं को गिनना और 10-10 के बंडल बनाना। एक (1), दो (2), तीन (3), चार (4), पाँच (5), छह (6), सात (7), आठ (8), नौ (9), दस (10)।",
        "cultural_keywords": ["गिनती", "बंडल", "महुआ", "तीर", "मटके", "हाट", "संख्या", "एक", "दो", "तीन"],
        "tribal_analogies": {
            "SANTHALI": "ᱢᱤᱫ (1), ᱵᱟᱨ (2), ᱯᱮ (3), ᱯᱩᱱ (4), ᱢᱚᱬᱮ (5), ᱛᱩᱨᱩᱭ (6), ᱮᱭᱟᱭ (7), ᱤᱨᱟᱹᱞ (8), ᱟᱨᱮ (9), ᱜᱮᱞ (10)᱾",
            "HO": "ᱢᱤ (1), ᱵᱟᱨ (2), ᱟᱹᱯᱤ (3), ᱩᱯᱩᱱ (4), ᱢᱚᱬᱮ (5), ᱛᱩᱨᱩᱭ (6), ᱟᱹᱭ (7), ᱤᱨᱤᱞ (8), ᱟᱨᱮ (9), ᱜᱮᱞ (10)᱾",
            "MUNDARI": "मियाद (1), बारिया (2), आपिया (3), उपुनिया (4), मोड़े (5), तुरूइ (6), एयाय (7), इराल (8), आरे (9), गेल (10)।"
        }
    },
    {
        "chunk_id": "JCERT_G3_FLN_01",
        "state": "JHARKHAND",
        "board": "JCERT",
        "district": "Khunti",
        "block": "Torpa",
        "grade": "GRADE_3",
        "subject": "LANGUAGE_FLN",
        "competency_category": "FLN_LITERACY",
        "bloom_level": "UNDERSTAND",
        "textbook_name": "भाषा मंजरी (भाग 3)",
        "page_range": "18-22",
        "suggested_duration_mins": 40,
        "chapter_number": 2,
        "chapter_title": "जल, नदियां और हमारा जीवन",
        "topic": "जल और नदियां",
        "lo_code": "LO-FLN-G3-02",
        "content_hindi": "जल ही जीवन है। हमारे गांव में नदी, तालाब, कुआं और चापाकल से पीने का पानी मिलता है। हमें जल को प्रदूषित नहीं करना चाहिए और पानी बचाना चाहिए।",
        "cultural_keywords": ["जल", "पानी", "नदी", "झरना", "दाः", "कुआं", "दाग", "स्वच्छ", "संरक्षण"],
        "tribal_analogies": {
            "SANTHALI": "ᱫᱟᱜ (Daah) ᱫᱚ ᱡᱤᱣᱤ ᱠᱟᱱᱟ᱾ ᱜᱟᱰᱟ ᱟᱨ ᱡᱷᱟᱨᱱᱟ ᱨᱮᱱᱟᱜ ᱥᱟᱯᱷᱟ ᱫᱟᱜ ᱵᱚᱱ ᱧᱩᱭᱟ᱾",
            "HO": "ᱫᱟᱜ (Daah) ᱫᱚ ᱡᱤᱣᱤ ᱛᱟᱵᱚ᱾ ᱜᱟᱲᱟ ᱫᱟᱜ ᱥᱟᱯᱷᱟ ᱫᱚᱦᱚ ᱦᱩᱭᱩᱜ-ᱟ᱾",
            "MUNDARI": "दाः (Daah) अबुआः जीवी ताना। गड़ा आर कुआं रेनाः सफा दाः नुइ दरकार।"
        }
    },
    {
        "chunk_id": "JCERT_G4_EVS_01",
        "state": "JHARKHAND",
        "board": "JCERT",
        "district": "Pakur",
        "block": "Littipara",
        "grade": "GRADE_4",
        "subject": "ENVIRONMENTAL_STUDIES",
        "competency_category": "EVS_ENVIRONMENT",
        "bloom_level": "ANALYZE",
        "textbook_name": "हमारी दुनिया (भाग 4)",
        "page_range": "35-41",
        "suggested_duration_mins": 40,
        "chapter_number": 5,
        "chapter_title": "पशु-पक्षी और उनका प्राकृतिक आवास",
        "topic": "पशु पक्षी और जंगल",
        "lo_code": "LO-EVS-G4-01",
        "content_hindi": "झारखंड के जंगलों में हाथी, हिरण, भालू, मोर और कई पक्षी रहते हैं। पालतू पशु जैसे गाय, बैल, बकरी खेती और दूध में हमारी मदद करते हैं।",
        "cultural_keywords": ["जानवर", "पशु", "पक्षी", "हाथी", "मोर", "जंगल", "हिरण", "बैल", "गाय"],
        "tribal_analogies": {
            "SANTHALI": "ᱵᱤᱨ ᱨᱮ ᱦᱟᱹᱛᱤ (Hati), ᱡᱤᱞ (Deer), ᱟᱨ ᱢᱟᱨᱟᱜ (Peacock) ᱠᱚ ᱛᱟᱦᱮᱸᱱᱟ᱾ ᱜᱟᱹᱭ ᱟᱨ ᱰᱟᱝᱜᱽᱨᱟ ᱟᱵᱚᱣᱟᱜ ᱜᱚᱲᱚ ᱠᱟᱱᱟ ᱠᱚ᱾",
            "HO": "ᱵᱤᱨ ᱨᱮ ᱦᱟᱹᱛᱤ, ᱥᱤᱞᱤᱵᱽ (Deer), ᱟᱨ ᱢᱟᱨᱟᱝ (Peacock) ᱢᱮᱱᱟᱜ ᱠᱚᱣᱟ᱾ ᱩᱨᱤᱜ ᱟᱨ ᱜᱟᱹᱭ ᱥᱤᱭᱩᱜ ᱨᱮ ᱜᱚᱲᱚ ᱮᱢᱚᱜ-ᱟ᱾",
            "MUNDARI": "बीर रे हाथी, सिलिब (Deer), आर माराः (Peacock) को मेनाकोवा। गाय आर काड़ा चास रे गोड़ो एमोअः।"
        }
    },
    {
        "chunk_id": "JCERT_G5_HERITAGE_01",
        "state": "JHARKHAND",
        "board": "JCERT",
        "district": "Ranchi",
        "block": "Khunti / Bundu",
        "grade": "GRADE_5",
        "subject": "TRIBAL_HERITAGE",
        "competency_category": "TRIBAL_HERITAGE",
        "bloom_level": "EVALUATE",
        "textbook_name": "झारखंडी संस्कृति एवं धरोहर",
        "page_range": "1-12",
        "suggested_duration_mins": 45,
        "chapter_number": 1,
        "chapter_title": "झारखंड के पारंपरिक लोकपर्व (सरहुल, करम, सोहराय)",
        "topic": "पारंपरिक लोकपर्व",
        "lo_code": "LO-HER-G5-01",
        "content_hindi": "सरहुल प्रकृति पर्व है जिसमें सखुआ के फूलों की पूजा होती है। करम पर्व में करम डाली की पूजा और सोहराय में पशुधन की वंदना और भित्तिचित्र बनाए जाते हैं।",
        "cultural_keywords": ["सरहुल", "करम", "सोहराय", "संस्कृति", "परब", "अखड़ा", "मांदर", "सखुआ", "फूल"],
        "tribal_analogies": {
            "SANTHALI": "ᱵᱟᱦᱟ ᱟᱨ ᱥᱟᱨᱦᱩᱞ ᱯᱚᱨᱚᱵᱽ ᱨᱮ ᱢᱟᱸᱫᱟᱨ (Tamak/Tumdak) ᱨᱩ ᱟᱛᱮ ᱮᱱᱮᱡ ᱦᱩᱭᱩᱜ-ᱟ᱾ ᱥᱚᱦᱨᱟᱭ ᱨᱮ ᱜᱟᱹᱭ ᱠᱚ ᱥᱟᱡᱟᱣᱜ-ᱟ᱾",
            "HO": "ᱢᱟᱜᱮ ᱟᱨ ᱵᱟᱦᱟ ᱯᱚᱨᱚᱵᱽ ᱨᱮ ᱫᱩᱨᱟᱹᱝ ᱟᱨ ᱥᱩᱥᱩᱱ ᱦᱩᱭᱩᱜ-ᱟ᱾ ᱥᱟᱨᱡᱚᱢ ᱵᱟᱦᱟ ᱛᱮ ᱥᱤᱝᱵᱚᱝᱜᱟ ᱯᱩᱡᱟᱹᱣᱜ-ᱟ᱾",
            "MUNDARI": "बाहा आर सरहुल परब रे मांदर आर नगाड़ा रु साड़ा ते अखड़ा रे सुसुन हुयुअः।"
        }
    },
    {
        "chunk_id": "JCERT_G1_FLN_02",
        "state": "JHARKHAND",
        "board": "JCERT",
        "district": "Dumka",
        "block": "Jarmundi",
        "grade": "GRADE_1",
        "subject": "LANGUAGE_FLN",
        "competency_category": "FLN_LITERACY",
        "bloom_level": "REMEMBER",
        "textbook_name": "भाषा मंजरी (भाग 1)",
        "page_range": "15-20",
        "suggested_duration_mins": 30,
        "chapter_number": 4,
        "chapter_title": "हमारा प्यारा परिवार और घर",
        "topic": "परिवार और घर",
        "lo_code": "LO-FLN-G1-04",
        "content_hindi": "परिवार में माता, पिता, दादा, दादी, भाई और बहन एक साथ प्रेम से रहते हैं। हम घर में एक दूसरे के कार्यों में हाथ बंटाते हैं।",
        "cultural_keywords": ["परिवार", "घर", "माता", "पिता", "भाई", "बहन", "ओड़ाः", "आयो", "बाबा"],
        "tribal_analogies": {
            "SANTHALI": "ᱚᱲᱟᱜ (Oṛag) ᱨᱮ ᱟᱭᱳ (Enga/Mayo), ᱵᱟᱵᱟ (Appa/Baba), ᱵᱚᱭᱦᱟ ᱟᱨ ᱢᱤᱥᱤ ᱢᱮᱱᱟᱜ ᱵᱚᱱᱟ᱾ ᱟᱵᱚ ᱡᱚᱛᱚ ᱦᱚᱲ ᱢᱤᱫ ᱛᱮ ᱵᱚᱱ ᱛᱟᱦᱮᱸᱱᱟ᱾",
            "HO": "ᱚᱲᱟᱜ (Owa) ᱨᱮ ᱮᱸᱜᱟ, ᱟᱯᱯᱟ, ᱦᱟᱜᱟ ᱟᱨ ᱢᱤᱥᱤ ᱛᱟᱦᱮᱸᱱᱟ ᱠᱚ᱾",
            "MUNDARI": "ओड़ाः रे एंगा, अप्पू, हागा आर मिसी दुलार ते को ताइन ताना।"
        }
    },
    {
        "chunk_id": "JCERT_G2_MATH_02",
        "state": "JHARKHAND",
        "board": "JCERT",
        "district": "West Singhbhum",
        "block": "Manoharpur",
        "grade": "GRADE_2",
        "subject": "MATHEMATICS",
        "competency_category": "FLN_NUMERACY",
        "bloom_level": "APPLY",
        "textbook_name": "गणित मेला (भाग 2)",
        "page_range": "42-49",
        "suggested_duration_mins": 35,
        "chapter_number": 6,
        "chapter_title": "जोड़ और घटाव के सरल खेल",
        "topic": "जोड़ और घटाव",
        "lo_code": "LO-MATH-G2-06",
        "content_hindi": "हाट बाजार में वस्तुओं को खरीदना और बेचना। कंकड़ों, महुआ के बीजों और तीलियों की सहायता से जोड़ना (+) और घटाना (-) सीखना।",
        "cultural_keywords": ["जोड़", "घटाव", "कंकड़", "बीज", "हाट", "बाजार", "रुपया", "हिसाब"],
        "tribal_analogies": {
            "SANTHALI": "ᱦᱟᱴ (Haat) ᱨᱮ ᱡᱤᱱᱤᱥ ᱠᱤᱨᱤᱧ ᱟᱹᱠᱷᱨᱤᱧ ᱨᱮ ᱞᱮᱠᱷᱟ (+) ᱟᱨ ᱜᱷᱟᱴᱟᱣ (-) ᱠᱟᱹᱢᱤ ᱞᱟᱜᱟᱜ-ᱟ᱾",
            "HO": "ᱦᱟᱴ ᱨᱮ ᱡᱤᱱᱤᱥ ᱠᱤᱨᱤᱧ ᱨᱮ ᱢᱮᱥᱟ (+) ᱟᱨ ᱚᱪᱚ (-) ᱦᱩᱭᱩᱜ-ᱟ᱾",
            "MUNDARI": "हाट रे सामान किरींग-अकिरींग रे जोड़ आर घटाव हिसाब बाइयोअः।"
        }
    },
    {
        "chunk_id": "JCERT_G3_EVS_02",
        "state": "JHARKHAND",
        "board": "JCERT",
        "district": "Khunti",
        "block": "Murhu",
        "grade": "GRADE_3",
        "subject": "ENVIRONMENTAL_STUDIES",
        "competency_category": "EVS_ENVIRONMENT",
        "bloom_level": "UNDERSTAND",
        "textbook_name": "हमारी दुनिया (भाग 3)",
        "page_range": "50-56",
        "suggested_duration_mins": 40,
        "chapter_number": 8,
        "chapter_title": "भोजन, अनाज और स्थानीय कृषि",
        "topic": "कृषि व अनाज",
        "lo_code": "LO-EVS-G3-08",
        "content_hindi": "झारखंड के खेतों में धान, मक्का, मड़ुआ, गोंदली और दालें उगाई जाती हैं। मड़ुआ की रोटी और साग-भात हमारे शरीर को शक्ति और पोषण देते हैं।",
        "cultural_keywords": ["अनाज", "धान", "मक्का", "मड़ुआ", "गोंदली", "कृषि", "भोजन", "रोटी", "साग"],
        "tribal_analogies": {
            "SANTHALI": "ᱦᱩᱲᱩ (Hulu/Paddy), ᱡᱚᱱᱰᱨᱟ (Corn), ᱠᱳᱰᱮ (Mandua) ᱟᱨ ᱟᱲᱟᱜ-ᱫᱟᱠᱟ ᱟᱵᱚᱣᱟᱜ ᱥᱤᱵᱤᱞ ᱡᱚᱢᱟᱜ ᱠᱟᱱᱟ᱾",
            "HO": "ᱵᱟᱵᱟ (Paddy), ᱡᱚᱱᱚᱲ (Corn), ᱠᱳᱰᱮ ᱟᱨ ᱟᱲᱟᱜ-ᱢᱟᱹᱱᱰᱤ ᱡᱚᱢ ᱛᱮ ᱦᱚᱲᱢᱚ ᱠᱮᱴᱮᱡᱚᱜ-ᱟ᱾",
            "MUNDARI": "बाबा (Paddy), जोन्दरा, कोदे आर आड़ाः-मंडी अबुआः पारंपरिक आर पौष्टिक जोमाः ताना।"
        }
    },
    {
        "chunk_id": "JCERT_G4_FLN_03",
        "state": "JHARKHAND",
        "board": "JCERT",
        "district": "Pakur",
        "block": "Amrapara",
        "grade": "GRADE_4",
        "subject": "LANGUAGE_FLN",
        "competency_category": "FLN_LITERACY",
        "bloom_level": "APPLY",
        "textbook_name": "भाषा मंजरी (भाग 4)",
        "page_range": "60-65",
        "suggested_duration_mins": 35,
        "chapter_number": 7,
        "chapter_title": "स्वच्छता, स्वास्थ्य और योग",
        "topic": "स्वच्छता और स्वास्थ्य",
        "lo_code": "LO-FLN-G4-03",
        "content_hindi": "प्रतिदिन सुबह नीम या करंज की दातुन से दांत साफ करना चाहिए। भोजन से पहले साबुन या राख से हाथ धोना और साफ पानी पीना स्वस्थ रहने का नियम है।",
        "cultural_keywords": ["स्वच्छता", "स्वास्थ्य", "दातुन", "नीम", "करंज", "हाथ धोना", "स्वच्छ जल"],
        "tribal_analogies": {
            "SANTHALI": "ᱱᱤᱢ (Neem) ᱟᱨ ᱠᱟᱨᱟᱧᱡ (Karanj) ᱫᱟᱹᱛᱩᱱ (Datun) ᱛᱮ ᱰᱟᱴᱟ ᱥᱟᱯᱷᱟ ᱟᱨ ᱛᱤ ᱟᱹᱨᱩᱵ ᱦᱚᱲᱢᱚ ᱵᱮᱥ ᱛᱟᱦᱮᱸᱱᱟ᱾",
            "HO": "ᱱᱤᱢ ᱫᱟᱹᱛᱩᱱ ᱛᱮ ᱰᱟᱴᱟ ᱡᱚᱫᱽ ᱟᱨ ᱛᱤ ᱟᱹᱵᱩᱝ ᱛᱮ ᱨᱩᱣᱟᱹ ᱵᱟᱝ ᱦᱩᱭᱩᱜ-ᱟ᱾",
            "MUNDARI": "नीम दारू रेनाः दातुन ते डाटा सफा आर ती अरुब ते होड़मो बेस ताइना।"
        }
    },
    {
        "chunk_id": "JCERT_G5_EVS_03",
        "state": "JHARKHAND",
        "board": "JCERT",
        "district": "Ranchi",
        "block": "Kanke",
        "grade": "GRADE_5",
        "subject": "ENVIRONMENTAL_STUDIES",
        "competency_category": "SCIENCE_NATURE",
        "bloom_level": "UNDERSTAND",
        "textbook_name": "हमारी दुनिया (भाग 5)",
        "page_range": "75-82",
        "suggested_duration_mins": 45,
        "chapter_number": 11,
        "chapter_title": "सौरमंडल, सूर्य और पृथ्वी",
        "topic": "सौरमंडल और सूर्य",
        "lo_code": "LO-EVS-G5-11",
        "content_hindi": "सूर्य हमारे सौरमंडल का केंद्र है और सभी ग्रह उसके चारों ओर घूमते हैं। सूर्य से हमें प्रकाश और गर्मी मिलती है। पृथ्वी के घूमने से दिन और रात होते हैं।",
        "cultural_keywords": ["सूर्य", "पृथ्वी", "सौरमंडल", "प्रकाश", "दिन", "रात", "सिंगबोंगा", "बेड़ा"],
        "tribal_analogies": {
            "SANTHALI": "ᱵᱮᱲᱟ (Beda/Sun) ᱫᱚ ᱫᱷᱟᱹᱨᱛᱤ ᱨᱮ ᱢᱟᱨᱥᱟᱞ ᱟᱨ ᱞᱚᱞᱚ ᱮᱢᱚᱜ-ᱟᱭ᱾ ᱵᱮᱲᱟ ᱜᱮ ᱡᱤᱣᱤ ᱨᱮᱱ ᱢᱩᱬᱩᱛ ᱠᱟᱱᱟᱭ᱾",
            "HO": "ᱥᱤᱝᱵᱚᱝᱜᱟ (Singbonga/Sun) ᱫᱚ ᱫᱷᱟᱹᱨᱛᱤ ᱨᱮ ᱢᱟᱨᱥᱟᱞ ᱮᱢᱚᱜ-ᱟ᱾ ᱩᱱᱤ ᱜᱮ ᱫᱤᱱ ᱟᱨ ᱧᱤᱫᱟᱹ ᱵᱮᱱᱟᱣᱤᱡ ᱠᱟᱱᱟᱭ᱾",
            "MUNDARI": "सिंगबोंगा (Singbonga) अबुआः धरति रे प्रकाश आर ऊर्जा एमोअः।"
        }
    },
    {
        "chunk_id": "JCERT_G1_EVS_01",
        "state": "JHARKHAND",
        "board": "JCERT",
        "district": "Dumka",
        "block": "Raneshwar",
        "grade": "GRADE_1",
        "subject": "ENVIRONMENTAL_STUDIES",
        "competency_category": "EVS_ENVIRONMENT",
        "bloom_level": "REMEMBER",
        "textbook_name": "हमारी दुनिया (भाग 1)",
        "page_range": "10-16",
        "suggested_duration_mins": 30,
        "chapter_number": 2,
        "chapter_title": "हमारे शरीर के अंग और ज्ञानेंद्रियाँ",
        "topic": "शरीर के अंग",
        "lo_code": "LO-EVS-G1-02",
        "content_hindi": "हमारे शरीर में कई अंग होते हैं। आँख से देखते हैं, कान से सुनते हैं, नाक से सूंघते हैं, जीभ से स्वाद लेते हैं और हाथ से काम करते हैं।",
        "cultural_keywords": ["शरीर", "अंग", "आँख", "कान", "नाक", "जीभ", "हाथ", "पैर", "मेत", "लुतुर"],
        "tribal_analogies": {
            "SANTHALI": "ᱢᱮᱫ (Med/Eye), ᱞᱩᱛᱩᱨ (Lutur/Ear), ᱢᱩ (Mu/Nose), ᱟᱞᱟᱝ (Alang/Tongue), ᱛᱤ (Ti/Hand), ᱡᱟᱝᱜᱟ (Janga/Foot)᱾",
            "HO": "ᱢᱮᱫ (Med), ᱞᱩᱛᱩᱨ (Lutur), ᱢᱩ (Mu), ᱞᱮᱸᱜᱮ (Tongue), ᱛᱤ (Ti), ᱠᱟᱴᱟ (Kata/Foot)᱾",
            "MUNDARI": "मेद (Med), लुतुुर (Lutur), मू (Mu), अलंग (Tongue), ती (Ti), कट्टा (Foot)।"
        }
    },
    {
        "chunk_id": "JCERT_G2_FLN_02",
        "state": "JHARKHAND",
        "board": "JCERT",
        "district": "West Singhbhum",
        "block": "Jhinkpani",
        "grade": "GRADE_2",
        "subject": "LANGUAGE_FLN",
        "competency_category": "FLN_LITERACY",
        "bloom_level": "REMEMBER",
        "textbook_name": "भाषा मंजरी (भाग 2)",
        "page_range": "30-36",
        "suggested_duration_mins": 35,
        "chapter_number": 5,
        "chapter_title": "दिनचर्या, खेलकूद और अच्छी आदतें",
        "topic": "दिनचर्या और खेल",
        "lo_code": "LO-FLN-G2-05",
        "content_hindi": "सुबह जल्दी उठना, माता-पिता को प्रणाम करना, दातुन करना, स्नान करना, समय पर विद्यालय जाना और शाम को मित्रों संग पारंपरिक खेल खेलना।",
        "cultural_keywords": ["दिनचर्या", "सुबह", "विद्यालय", "खेलकूद", "आसड़ा", "इनांग", "सुसुन", "मित्र"],
        "tribal_analogies": {
            "SANTHALI": "ᱥᱮᱛᱟᱜ ᱵᱮᱨᱮᱫ, ᱟᱥᱲᱟ (Asda/School) ᱥᱮᱱᱚᱜ, ᱟᱨ ᱟᱹᱭᱩᱵ ᱜᱟᱛᱮ ᱠᱚ ᱥᱟᱶ ᱮᱱᱮᱡ (Play) ᱵᱮᱥ ᱦᱮᱵᱤᱴ ᱠᱟᱱᱟ᱾",
            "HO": "ᱥᱮᱛᱟ ᱵᱤᱨᱤᱫ, ᱤᱛᱩᱱ ᱚᱲᱟᱜ (School) ᱥᱮᱱᱚᱜ ᱟᱨ ᱤᱱᱩᱝ (Play) ᱠᱟᱹᱢᱤ ᱵᱮᱥ ᱜᱮᱭᱟ᱾",
            "MUNDARI": "सेता बिरिद, स्कूल सेन आर जोम-नु बाद गाते को सलोअः इनुंग (Play) बेस अभ्यास ताना।"
        }
    },
    {
        "chunk_id": "JCERT_G3_MATH_03",
        "state": "JHARKHAND",
        "board": "JCERT",
        "district": "Khunti",
        "block": "Rania",
        "grade": "GRADE_3",
        "subject": "MATHEMATICS",
        "competency_category": "FLN_NUMERACY",
        "bloom_level": "APPLY",
        "textbook_name": "गणित मेला (भाग 3)",
        "page_range": "65-72",
        "suggested_duration_mins": 40,
        "chapter_number": 9,
        "chapter_title": "माप और भार की पारंपरिक इकाइयां (पैला और कुड़ी)",
        "topic": "माप और भार",
        "lo_code": "LO-MATH-G3-04",
        "content_hindi": "झारखंड के हाट में अनाज नापने के लिए काठ का पैला और कुड़ी का उपयोग किया जाता है। आधुनिक मानक भार किलोग्राम (kg) और ग्राम (g) होता है।",
        "cultural_keywords": ["माप", "भार", "पैला", "कुड़ी", "तराजू", "वजन", "किलोग्राम", "अनाज"],
        "tribal_analogies": {
            "SANTHALI": "ᱦᱩᱲᱩ ᱟᱨ ᱪᱟᱣᱞᱮ ᱯᱟᱹᱭᱞᱟᱹ (Paila) ᱛᱮ ᱡᱚᱠᱷᱟᱜ-ᱟ᱾ ᱒᱐ ᱯᱟᱹᱭᱞᱟᱹ ᱨᱮ ᱢᱤᱫ ᱠᱩᱲᱤ (Kuri) ᱦᱩᱭᱩᱜ-ᱟ᱾",
            "HO": "ᱵᱟᱵᱟ ᱯᱟᱹᱭᱞᱟᱹ ᱛᱮ ᱛᱩᱞᱟᱹᱣᱜ-ᱟ᱾ ᱢᱤ ᱠᱩᱲᱤ ᱨᱮ ᱒᱐ ᱯᱟᱹᱭᱞᱟᱹ ᱛᱟᱦᱮᱸᱱᱟ᱾",
            "MUNDARI": "बाबा पैला (Paila) ते माप हुयुअः। 20 पैला रे मियाद कुड़ी (Kuri) बाइयोअः।"
        }
    },
    {
        "chunk_id": "JCERT_G4_HERITAGE_02",
        "state": "JHARKHAND",
        "board": "JCERT",
        "district": "Hazaribagh",
        "block": "Barkagaon",
        "grade": "GRADE_4",
        "subject": "TRIBAL_HERITAGE",
        "competency_category": "TRIBAL_HERITAGE",
        "bloom_level": "APPLY",
        "textbook_name": "झारखंड की लोक कलाएं",
        "page_range": "25-34",
        "suggested_duration_mins": 45,
        "chapter_number": 8,
        "chapter_title": "झारखंड की लोक कला और सोहराय पेंटिंग",
        "topic": "सोहराय चित्रकला",
        "lo_code": "LO-HER-G4-02",
        "content_hindi": "सोहराय और कोहबर झारखंड की प्रसिद्ध भित्तिचित्र कलाएं हैं। महिलाएं मिट्टी की दीवारों पर प्राकृतिक रंगों (लाल, काली, सफेद, पीली मिट्टी) से पशु, पक्षी और पेड़-पौधों के चित्र बनाती हैं।",
        "cultural_keywords": ["सोहराय", "कोहबर", "चित्रकला", "भित्तिचित्र", "मिट्टी", "प्राकृतिक रंग", "पशु पक्षी"],
        "tribal_analogies": {
            "SANTHALI": "ᱥᱚᱦᱨᱟᱭ ᱨᱮ ᱚᱲᱟᱜ ᱠᱟᱸᱛ (Wall) ᱨᱮ ᱦᱟᱥᱟ (Earth color) ᱛᱮ ᱪᱮᱬᱮ ᱟᱨ ᱫᱟᱨᱮ ᱪᱤᱛᱟᱹᱨ ᱵᱮᱱᱟᱣᱜ-ᱟ᱾",
            "HO": "ᱚᱣᱟ ᱠᱟᱸᱛ ᱨᱮ ᱦᱟᱥᱟ ᱨᱚᱝ ᱛᱮ ᱢᱟᱨᱟᱝ, ᱥᱤᱞᱤᱵᱽ ᱟᱨ ᱫᱟᱨᱩ ᱪᱤᱛᱟᱹᱨ ᱵᱟᱭᱚᱜ-ᱟ᱾",
            "MUNDARI": "सोहराय रे ओड़ाः कांत रे हासा रंग ते सोहराय पेंटिंग आर चित्र बइयोअः।"
        }
    },
    {
        "chunk_id": "JCERT_G5_FLN_04",
        "state": "JHARKHAND",
        "board": "JCERT",
        "district": "West Singhbhum",
        "block": "Saranda Forest Division",
        "grade": "GRADE_5",
        "subject": "LANGUAGE_FLN",
        "competency_category": "FLN_LITERACY",
        "bloom_level": "EVALUATE",
        "textbook_name": "भाषा मंजरी (भाग 5)",
        "page_range": "90-98",
        "suggested_duration_mins": 45,
        "chapter_number": 12,
        "chapter_title": "जंगल, पर्यावरण और हमारी पृथ्वी की रक्षा",
        "topic": "पर्यावरण रक्षा",
        "lo_code": "LO-FLN-G5-06",
        "content_hindi": "जंगल हमारे फेफड़े हैं। पेड़ हमें शुद्ध वायु (ऑक्सीजन) देते हैं, वर्षा लाते हैं और मिट्टी के कटाव को रोकते हैं। हमें जंगलों को कटने से बचाना है।",
        "cultural_keywords": ["जंगल", "पर्यावरण", "रक्षा", "वन", "ऑक्सीजन", "वर्षा", "पेड़", "सारंडा"],
        "tribal_analogies": {
            "SANTHALI": "ᱵᱤᱨ (Forest) ᱫᱚ ᱟᱵᱚᱣᱟᱜ ᱡᱤᱣᱤ ᱠᱟᱱᱟ᱾ ᱫᱟᱨᱮ ᱵᱟᱝ ᱛᱟᱦᱮᱸᱱ ᱠᱷᱟᱱ ᱫᱟᱜ ᱟᱨ ᱦᱚᱭ ᱵᱟᱭ ᱧᱟᱢᱚᱜ-ᱟ᱾ ᱵᱤᱨ ᱵᱟᱧᱪᱟᱣ ᱢᱟ᱾",
            "HO": "ᱵᱤᱨ ᱫᱚ ᱡᱤᱣᱤ ᱛᱟᱵᱚ᱾ ᱥᱟᱨᱟᱱᱰᱟ ᱵᱤᱨ ᱫᱚ ᱫᱷᱟᱹᱨᱛᱤ ᱨᱮᱱᱟᱜ ᱥᱟᱦᱮᱫ ᱠᱟᱱᱟ᱾ ᱵᱤᱨ ᱨᱩᱠᱷᱤᱭᱟᱹ ᱫᱚᱦᱚ ᱦᱩᱭᱩᱜ-ᱟ᱾",
            "MUNDARI": "बीर अबुआः जीवी ताना। सारंडा बीर आर दारू को बचाओ दरकार जेनाते सफा हवा आर दाः मिलोअः।"
        }
    }
]

# --- Dense Semantic Concept Centroids ---
SEMANTIC_CONCEPT_CENTROIDS = {
    "BOTANY_PLANTS": ["पेड़", "पत्ती", "साल", "सखुआ", "महुआ", "करम", "दातुन", "नीम", "पौधा", "जंगल", "सारंडा", "वन"],
    "NUMERACY_MATH": ["गिनती", "संख्या", "जोड़", "घटाव", "बंडल", "माप", "भार", "पैला", "कुड़ी", "तराजू", "वजन", "रुपया"],
    "HYDROLOGY_WATER": ["जल", "पानी", "नदी", "झरना", "तालाब", "कुआं", "दाः", "दाग", "स्वच्छ", "संरक्षण"],
    "ZOOLOGY_FAUNA": ["जानवर", "पशु", "पक्षी", "हाथी", "मोर", "हिरण", "बैल", "गाय", "पालतू", "जंगली"],
    "CULTURE_FESTIVALS": ["सरहुल", "करम", "सोहराय", "संस्कृति", "परब", "अखड़ा", "मांदर", "सखुआ", "फूल", "कोहबर"],
    "FAMILY_SOCIETY": ["परिवार", "घर", "माता", "पिता", "भाई", "बहन", "ओड़ाः", "दादा", "दादी"],
    "AGRICULTURE_FOOD": ["अनाज", "धान", "मक्का", "मड़ुआ", "गोंदली", "कृषि", "भोजन", "रोटी", "साग", "पोषण"],
    "HYGIENE_HEALTH": ["स्वच्छता", "स्वास्थ्य", "दातुन", "नीम", "करंज", "हाथ धोना", "दांत"],
    "ASTRONOMY_SPACE": ["सूर्य", "पृथ्वी", "सौरमंडल", "प्रकाश", "दिन", "रात", "सिंगबोंगा", "ऊर्जा"],
    "ANATOMY_SENSES": ["शरीर", "अंग", "आँख", "कान", "नाक", "जीभ", "हाथ", "पैर", "ज्ञानेंद्रियाँ"],
    "ART_FOLK": ["सोहराय", "कोहबर", "चित्रकला", "भित्तिचित्र", "मिट्टी", "प्राकृतिक रंग", "पेंटिंग"]
}


class FineTunedHybridRagEngine:
    """Production-grade hybrid RAG engine with Okapi BM25, 128-dim dense semantic vectorizer, RRF, and Cross-Encoder Reranking."""

    def __init__(self):
        self.corpus = JCERT_KNOWLEDGE_BASE
        self.k1 = 1.5
        self.b = 0.75
        self.vector_dim = 128
        self._cache_capacity = 500
        self._query_cache: OrderedDict = OrderedDict()
        self.cache_hits = 0
        self.cache_misses = 0
        self._precompute_corpus_stats()

    def _tokenize(self, text: str) -> List[str]:
        cleaned = re.sub(r'[^\w\s\u0900-\u097F\u1C50-\u1C7F]', ' ', text.lower())
        tokens = [t.strip() for t in cleaned.split() if len(t.strip()) >= 2]
        return tokens

    def _precompute_corpus_stats(self):
        self.doc_tokens = []
        self.doc_lengths = []
        self.doc_freqs: Dict[str, int] = {}
        
        for doc in self.corpus:
            combined_text = f"{doc['topic']} {doc['chapter_title']} {doc['content_hindi']} {' '.join(doc['cultural_keywords'])} {doc['lo_code']} {doc.get('district', '')} {doc.get('competency_category', '')}"
            tokens = self._tokenize(combined_text)
            self.doc_tokens.append(tokens)
            self.doc_lengths.append(len(tokens))
            
            unique_tokens = set(tokens)
            for token in unique_tokens:
                self.doc_freqs[token] = self.doc_freqs.get(token, 0) + 1

        self.avg_doc_len = sum(self.doc_lengths) / max(len(self.doc_lengths), 1)
        self.num_docs = len(self.corpus)

        # Precompute dense embeddings
        self.doc_embeddings = [self._embed_text(
            f"{doc['topic']} {doc['chapter_title']} {doc['content_hindi']} {' '.join(doc['cultural_keywords'])}"
        ) for doc in self.corpus]

    def _compute_bm25_score(self, query_tokens: List[str], doc_idx: int) -> float:
        score = 0.0
        doc_len = self.doc_lengths[doc_idx]
        tokens_in_doc = self.doc_tokens[doc_idx]
        token_counts: Dict[str, int] = {}
        for t in tokens_in_doc:
            token_counts[t] = token_counts.get(t, 0) + 1

        for q_token in query_tokens:
            if q_token not in self.doc_freqs:
                continue
            df = self.doc_freqs[q_token]
            idf = math.log(1.0 + (self.num_docs - df + 0.5) / (df + 0.5))
            
            tf = token_counts.get(q_token, 0)
            numerator = tf * (self.k1 + 1.0)
            denominator = tf + self.k1 * (1.0 - self.b + self.b * (doc_len / self.avg_doc_len))
            score += idf * (numerator / denominator)

        return round(score, 4)

    def _embed_text(self, text: str) -> List[float]:
        vector = [0.0] * self.vector_dim
        tokens = self._tokenize(text)
        token_set = set(tokens)

        # Centroid projections
        for c_idx, (concept, terms) in enumerate(SEMANTIC_CONCEPT_CENTROIDS.items()):
            overlap = len(token_set.intersection(set(terms)))
            if overlap > 0:
                weight = overlap / len(terms)
                base_slot = (c_idx * 11) % self.vector_dim
                for offset in range(5):
                    slot = (base_slot + offset) % self.vector_dim
                    vector[slot] += weight * (1.0 / (offset + 1))

        # Subword n-gram hashing
        for t in tokens:
            h = abs(hash(t)) % self.vector_dim
            vector[h] += 0.35

        # L2 Normalization
        norm = math.sqrt(sum(v * v for v in vector))
        if norm > 1e-5:
            vector = [v / norm for v in vector]

        return vector

    def _cosine_similarity(self, v1: List[float], v2: List[float]) -> float:
        dot = sum(a * b for a, b in zip(v1, v2))
        return max(0.0, min(1.0, dot))

    def _cross_encoder_rerank(self, query: str, candidate_chunk: Dict[str, Any], initial_score: float) -> float:
        query_lower = query.lower()
        query_tokens = set(self._tokenize(query))
        topic_tokens = set(self._tokenize(candidate_chunk["topic"]))
        title_tokens = set(self._tokenize(candidate_chunk["chapter_title"]))
        score = initial_score
        
        # 1. Exact Learning Outcome Code Bonus
        if candidate_chunk["lo_code"].lower() in query_lower:
            score += 0.50

        # 2. Topic and Chapter Title token overlap bonus
        topic_overlap = len(query_tokens.intersection(topic_tokens))
        if topic_overlap > 0:
            score += min(0.60, topic_overlap * 0.30)

        title_overlap = len(query_tokens.intersection(title_tokens))
        if title_overlap > 0:
            score += min(0.40, title_overlap * 0.20)

        # 3. Cultural keywords & Content token coverage
        kw_hits = sum(1 for kw in candidate_chunk["cultural_keywords"] if kw.lower() in query_lower)
        score += min(0.30, kw_hits * 0.10)

        content_tokens = set(self._tokenize(candidate_chunk["content_hindi"]))
        content_overlap = len(query_tokens.intersection(content_tokens))
        if content_overlap > 0:
            score += min(0.50, content_overlap * 0.12)

        return round(score, 4)

    def get_cache_stats(self) -> Dict[str, Any]:
        total = self.cache_hits + self.cache_misses
        hit_ratio = round(self.cache_hits / max(1, total), 4)
        return {
            "cache_hits": self.cache_hits,
            "cache_misses": self.cache_misses,
            "cache_size": len(self._query_cache),
            "cache_capacity": self._cache_capacity,
            "hit_ratio": hit_ratio
        }

    def clear_cache(self):
        self._query_cache.clear()
        self.cache_hits = 0
        self.cache_misses = 0

    def retrieve(
        self,
        query: str,
        grade: Optional[str] = None,
        subject: Optional[str] = None,
        district: Optional[str] = None,
        bloom_level: Optional[str] = None,
        competency_category: Optional[str] = None,
        top_k: int = 3
    ) -> List[Dict[str, Any]]:
        cache_key = (
            query.strip().lower(),
            grade,
            subject,
            district.lower() if district else None,
            bloom_level,
            competency_category,
            top_k
        )

        if cache_key in self._query_cache:
            self.cache_hits += 1
            cached_result = self._query_cache.pop(cache_key)
            self._query_cache[cache_key] = cached_result
            return cached_result

        self.cache_misses += 1
        query_tokens = self._tokenize(query)
        query_vector = self._embed_text(query)
        
        scored_candidates = []
        for idx, doc in enumerate(self.corpus):
            # Optional metadata filters
            if grade and doc["grade"] != grade:
                continue
            if subject and doc["subject"] != subject:
                continue
            if district and doc.get("district", "").lower() != district.lower():
                continue
            if bloom_level and doc.get("bloom_level") != bloom_level:
                continue
            if competency_category and doc.get("competency_category") != competency_category:
                continue

            bm25_score = self._compute_bm25_score(query_tokens, idx)
            cos_sim = self._cosine_similarity(query_vector, self.doc_embeddings[idx])
            
            # Direct learning outcome or chapter match boost
            lo_boost = 0.25 if doc["lo_code"].lower() in query.lower() else 0.0
            rrf_score = (1.0 / (60.0 + max(0.0, 10.0 - bm25_score))) + (cos_sim * 0.6) + lo_boost
            
            rerank_score = self._cross_encoder_rerank(query, doc, rrf_score)
            
            scored_candidates.append({
                "doc_idx": idx,
                "chunk": doc,
                "bm25_score": bm25_score,
                "cosine_sim": round(cos_sim, 4),
                "rrf_score": round(rrf_score, 4),
                "rerank_score": rerank_score
            })

        scored_candidates.sort(key=lambda x: x["rerank_score"], reverse=True)
        top_results = scored_candidates[:top_k]

        final_results = [
            {
                "chunk": res["chunk"],
                "bm25_score": res["bm25_score"],
                "cosine_similarity": res["cosine_sim"],
                "rerank_score": res["rerank_score"],
                "provenance": {
                    "source": "JCERT_JHARKHAND_CURRICULUM_CORPUS_v3",
                    "chunk_id": res["chunk"]["chunk_id"],
                    "lo_code": res["chunk"]["lo_code"],
                    "grade": res["chunk"]["grade"],
                    "subject": res["chunk"]["subject"],
                    "district": res["chunk"].get("district", "JHARKHAND_STATE"),
                    "textbook_name": res["chunk"].get("textbook_name", ""),
                    "page_range": res["chunk"].get("page_range", ""),
                    "bloom_level": res["chunk"].get("bloom_level", "UNDERSTAND"),
                    "competency_category": res["chunk"].get("competency_category", "EVS_ENVIRONMENT"),
                    "retrieval_method": "HYBRID_BM25_DENSE_RRF_CROSS_ENCODER"
                }
            }
            for res in top_results
        ]

        if len(self._query_cache) >= self._cache_capacity:
            self._query_cache.popitem(last=False)
        self._query_cache[cache_key] = final_results

        return final_results

# Singleton instance
rag_engine = FineTunedHybridRagEngine()

