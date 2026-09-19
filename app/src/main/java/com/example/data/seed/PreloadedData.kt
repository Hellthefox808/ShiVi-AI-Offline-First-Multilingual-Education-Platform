package com.example.data.seed

import com.example.data.local.CurriculumContentEntity
import com.example.data.local.GlossaryEntity
import com.example.data.local.LessonEntity
import com.example.data.local.StudentEntity
import com.example.data.local.WorksheetEntity
import com.example.domain.model.ClassroomQuickPhrase
import com.example.domain.model.GradeLevel
import com.example.domain.model.StudentFlashcard
import com.example.domain.model.SubjectArea
import com.example.domain.model.TargetLanguage
import com.example.domain.model.TraceItem
import com.example.domain.model.WorksheetQuestion
import com.example.domain.model.toJsonString

object PreloadedData {

    val defaultGlossaryItems = listOf(
        GlossaryEntity(
            id = "gl_1",
            category = "प्रकृति व पर्यावरण (Nature & Environment)",
            hindiWord = "पेड़ (वृक्ष)",
            santhaliWord = "ᱫᱟᱨᱮ (Dare)",
            santhaliOlChiki = "ᱫᱟᱨᱮ",
            hoWord = "ᱫᱟᱨᱩ (Daru)",
            hoDevanagari = "दारू",
            mundariWord = "दारू (Daru)",
            pronunciation = "Da-re / Da-ru",
            englishMeaning = "Tree",
            exampleSentenceHindi = "यह साल का पेड़ है।",
            exampleSentenceTarget = "ᱱᱚᱣᱟ ᱫᱚ ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱮ ᱠᱟᱱᱟ (Nowa do sarjom dare kana)"
        ),
        GlossaryEntity(
            id = "gl_2",
            category = "प्रकृति व पर्यावरण (Nature & Environment)",
            hindiWord = "पानी (जल)",
            santhaliWord = "ᱫᱟᱜ (Dah)",
            santhaliOlChiki = "ᱫᱟᱜ",
            hoWord = "ᱫᱟᱜ (Da')",
            hoDevanagari = "दाः",
            mundariWord = "दाः (Dah)",
            pronunciation = "Daah (glottal stop)",
            englishMeaning = "Water",
            exampleSentenceHindi = "नदी का पानी साफ़ है।",
            exampleSentenceTarget = "ᱜᱟᱰᱟ ᱫᱟᱜ ᱫᱚ ᱥᱟᱯᱷᱟ ᱜᱮᱭᱟ (Gada dah do sapha geya)"
        ),
        GlossaryEntity(
            id = "gl_3",
            category = "गणित व संख्या (Numeracy & Counting)",
            hindiWord = "एक (1)",
            santhaliWord = "ᱢᱤᱫ (Mit')",
            santhaliOlChiki = "ᱢᱤᱫ",
            hoWord = "ᱢᱤᱭᱟᱹᱫᱽ (Miyad)",
            hoDevanagari = "मियाद",
            mundariWord = "मियाद (Miyad)",
            pronunciation = "Mit / Miyad",
            englishMeaning = "One (1)",
            exampleSentenceHindi = "मेरे पास एक कलम है।",
            exampleSentenceTarget = "ᱤᱧ ᱴᱷᱮᱱ ᱢᱤᱫᱴᱟᱝ ᱠᱚᱞᱚᱢ ᱢᱮᱱᱟᱜ-ᱟ (Ing then mit'tang kolom menag-a)"
        ),
        GlossaryEntity(
            id = "gl_4",
            category = "गणित व संख्या (Numeracy & Counting)",
            hindiWord = "दो (2)",
            santhaliWord = "ᱵᱟᱨ (Bar)",
            santhaliOlChiki = "ᱵᱟᱨ",
            hoWord = "ᱵᱟᱨᱤᱭᱟ (Bariya)",
            hoDevanagari = "बरिया",
            mundariWord = "बरिया (Bariya)",
            pronunciation = "Bar / Bariya",
            englishMeaning = "Two (2)",
            exampleSentenceHindi = "आकाश में दो पक्षी उड़ रहे हैं।",
            exampleSentenceTarget = "ᱥᱮᱨᱢᱟ ᱨᱮ ᱵᱟᱨᱭᱟ ᱪᱮᱬᱮ ᱠᱤᱱ ᱩᱰᱟᱹᱣᱜ ᱠᱟᱱᱟ (Serma re barya chene kin udawg kana)"
        ),
        GlossaryEntity(
            id = "gl_5",
            category = "जीव-जंतु व पशु (Animals & Birds)",
            hindiWord = "गाय (गौमाता)",
            santhaliWord = "ᱜᱟᱹᱭ (Gai)",
            santhaliOlChiki = "ᱜᱟᱹᱭ",
            hoWord = "ᱜᱟᱹᱭ (Gai)",
            hoDevanagari = "गई",
            mundariWord = "उरीः (Urih / Gai)",
            pronunciation = "Ga-ee",
            englishMeaning = "Cow",
            exampleSentenceHindi = "गाय हरी घास चरती है।",
            exampleSentenceTarget = "ᱜᱟᱹᱭ ᱦᱟᱹᱨᱭᱟᱹᱲ ᱜᱷᱟᱸᱥ ᱮ ᱡᱚᱢ-ᱮᱫᱟ (Gai hariyad ghas e jom-eda)"
        ),
        GlossaryEntity(
            id = "gl_6",
            category = "जीव-जंतु व पशु (Animals & Birds)",
            hindiWord = "पक्षी (चिड़िया)",
            santhaliWord = "ᱪᱮᱬᱮ (Chene)",
            santhaliOlChiki = "ᱪᱮᱬᱮ",
            hoWord = "ᱪᱮᱬᱮ (Chene)",
            hoDevanagari = "चेणे",
            mundariWord = "चेड़े (Chede)",
            pronunciation = "Che-ne",
            englishMeaning = "Bird",
            exampleSentenceHindi = "चिड़िया घोंसला बनाती है।",
            exampleSentenceTarget = "ᱪᱮᱬᱮ ᱛᱩᱠᱟᱹᱭ ᱵᱮᱱᱟᱣ-ᱮᱫᱟ (Chene tukay benaw-eda)"
        ),
        GlossaryEntity(
            id = "gl_7",
            category = "विद्यालय व शिक्षण (School & Classroom)",
            hindiWord = "पुस्तक (किताब)",
            santhaliWord = "ᱯᱩᱛᱷᱤ (Puthi)",
            santhaliOlChiki = "ᱯᱩᱛᱷᱤ",
            hoWord = "ᱯᱚᱛᱚᱵ (Potob)",
            hoDevanagari = "पोतोब",
            mundariWord = "पुथी (Puthi)",
            pronunciation = "Pu-thi / Po-tob",
            englishMeaning = "Book",
            exampleSentenceHindi = "हम सब किताब पढ़ेंगे।",
            exampleSentenceTarget = "ᱟᱵᱚ ᱡᱚᱛᱚ ᱦᱚᱲ ᱯᱩᱛᱷᱤ ᱵᱚᱱ ᱯᱟᱲᱦᱟᱣ-ᱟ (Abo joto hod puthi bon padhaw-a)"
        ),
        GlossaryEntity(
            id = "gl_8",
            category = "विद्यालय व शिक्षण (School & Classroom)",
            hindiWord = "मित्र (दोस्त / सखा)",
            santhaliWord = "ᱜᱟᱛᱮ (Gate)",
            santhaliOlChiki = "ᱜᱟᱛᱮ",
            hoWord = "ᱡᱩᱲᱤ (Juri)",
            hoDevanagari = "जुड़ी",
            mundariWord = "संगाती (Sangati / Gate)",
            pronunciation = "Ga-te / Ju-ri",
            englishMeaning = "Friend",
            exampleSentenceHindi = "रोहन मेरा अच्छा मित्र है।",
            exampleSentenceTarget = "ᱨᱳᱦᱚᱱ ᱤᱧᱤᱡ ᱵᱷᱟᱹᱜᱤ ᱜᱟᱛᱮ ᱠᱟᱱᱟᱭ (Rohan ingij bhagi gate kanay)"
        ),
        GlossaryEntity(
            id = "gl_9",
            category = "संस्कृति व त्योहार (Culture & Festivals)",
            hindiWord = "सरहुल (फूलों का पर्व)",
            santhaliWord = "ᱵᱟᱦᱟ ᱯᱚᱨᱚᱵ (Baha Porob)",
            santhaliOlChiki = "ᱵᱟᱦᱟ ᱯᱚᱨᱚᱵ",
            hoWord = "ᱵᱟᱦᱟ ᱯᱟᱨᱟᱵ (Baha Parab)",
            hoDevanagari = "बाहा परब",
            mundariWord = "बाहा परब (Baha Parab)",
            pronunciation = "Ba-ha Po-rob (Flower Festival)",
            englishMeaning = "Sarhul / Flower Spring Festival",
            exampleSentenceHindi = "सरहुल में साल के फूलों की पूजा होती है।",
            exampleSentenceTarget = "ᱵᱟᱦᱟ ᱯᱚᱨᱚᱵ ᱨᱮ ᱥᱟᱨᱡᱚᱢ ᱵᱟᱦᱟ ᱠᱚ ᱵᱚᱸᱜᱟᱭᱟ (Baha porob re sarjom baha ko bongaya)"
        ),
        GlossaryEntity(
            id = "gl_10",
            category = "शरीर के अंग व स्वास्थ्य (Body Parts)",
            hindiWord = "आंख (नेत्र)",
            santhaliWord = "ᱢᱮᱫ (Met')",
            santhaliOlChiki = "ᱢᱮᱫ",
            hoWord = "ᱢᱮᱫ (Med)",
            hoDevanagari = "मेद",
            mundariWord = "मेद (Med)",
            pronunciation = "Met / Med",
            englishMeaning = "Eye",
            exampleSentenceHindi = "हम अपनी आँखों से देखते हैं।",
            exampleSentenceTarget = "ᱟᱵᱚ ᱢᱮᱫ ᱛᱮ ᱵᱚᱱ ᱧᱮᱞᱟ (Abo met' te bon nyela)"
        )
    )

    val defaultStudents = listOf(
        StudentEntity("st_1", "सुनीता मुर्मू (Sunita Murmu)", "01", "Grade 2", "डुमका (Dumka)", 95, 8),
        StudentEntity("st_2", "बिरसा हेम्ब्रम (Birsa Hembram)", "02", "Grade 2", "चाईबासा (Chaibasa)", 88, 7),
        StudentEntity("st_3", "जयराम सोरेन (Jayram Soren)", "03", "Grade 2", "रांची (Ranchi)", 92, 8),
        StudentEntity("st_4", "सानिया हो (Saniya Ho)", "04", "Grade 2", "सरायकेला (Seraikela)", 84, 6),
        StudentEntity("st_5", "अमित मुंडा (Amit Munda)", "05", "Grade 2", "खूंटी (Khunti)", 90, 7)
    )

    val defaultLessons = listOf(
        LessonEntity(
            id = "les_1",
            title = "हमारे आसपास के पेड़-पौधे और साल का वृक्ष (Our Trees & The Holy Sarjom)",
            grade = GradeLevel.GRADE_2.label,
            subject = SubjectArea.EVS_ENVIRONMENT.titleHindi,
            learningOutcome = "स्थानीय वनस्पतियों, साल वृक्ष (Sarjom) व प्रकृति संरक्षण की पहचान",
            hindiPrompt = "बच्चों को हमारे जंगल, साल के पेड़ और प्रकृति से मिलने वाले लाभ के बारे में बताएं।",
            targetLanguage = TargetLanguage.SANTHALI.displayName,
            adaptedExplanation = "ᱟᱵᱚᱣᱟᱜ ᱵᱤᱨ ᱨᱮ ᱟᱹᱰᱤ ᱞᱮᱠᱟᱱ ᱫᱟᱨᱮ ᱢᱮᱱᱟᱜ-ᱟ᱾ ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱮ (साल वृक्ष) ᱫᱚ ᱟᱵᱚ ᱞᱟᱹᱜᱤᱫ ᱟᱹᱰᱤ ᱢᱟᱨᱟᱝ ᱫᱟᱨᱮ ᱠᱟᱱᱟ᱾ ᱱᱚᱣᱟ ᱫᱟᱨᱮ ᱠᱷᱚᱱ ᱟᱵᱚ ᱥᱟᱦᱟᱱ, ᱩᱢᱩᱞ ᱟᱨ ᱥᱟᱯᱷᱟ ᱦᱚᱭ ᱵᱚᱱ ᱧᱟᱢᱟ᱾",
            nativeScriptText = "ᱟᱵᱚ ᱫᱟᱨᱮ ᱵᱚᱱ ᱡᱚᱛᱚᱱᱟ ᱟᱨ ᱵᱤᱨ ᱵᱚᱱ ᱵᱟᱧᱪᱟᱣᱟ᱾ (Abo dare bon jotona ar bir bon banchawa)",
            transliterationText = "Abowag bir re adi lekan dare menag-a. Sarjom dare do abo lagid adi marang dare kana. Nowa dare khon abo sahan, umul ar sapha hoy bon nyama.",
            culturalAnalogy = "झारखंड के सरहुल (Baha Porob) पर्व में साल के नए फूलों का स्वागत किया जाता है, जो जीवन और नए आरंभ का प्रतीक है।",
            activityPrompt = "कक्षा के बाहर जाकर 3 अलग-अलग पत्तियों को छूकर देखें और उनके संथाली नाम बोलें।",
            pronunciationGuide = "साल के पेड़ को संथाली में 'ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱮ' (Sarjom Dare) कहते हैं। 'ᱫ' का उच्चारण कोमल 'D' जैसा करें।",
            status = "PUBLISHED",
            qualityScore = 0.96f,
            groundingScore = 0.98f,
            approvedAt = System.currentTimeMillis() - 86400000L,
            syncStatus = "SYNCED"
        ),
        LessonEntity(
            id = "les_2",
            title = "गिनती और समूह बनाना: 1 से 10 तक (Counting Objects 1 to 10)",
            grade = GradeLevel.GRADE_1.label,
            subject = SubjectArea.MATH_NUMERACY.titleHindi,
            learningOutcome = "संख्या बोध: स्थानीय बीजों व पत्तों से 1 से 10 तक गिनना",
            hindiPrompt = "महुआ के बीज और पत्थरों की मदद से 1 से 5 तक गिनती सिखाएं।",
            targetLanguage = TargetLanguage.HO.displayName,
            adaptedExplanation = "ᱢᱤᱭᱟᱹᱫᱽ (1), ᱵᱟᱨᱤᱭᱟ (2), ᱟᱹᱯᱤᱭᱟᱹ (3), ᱩᱯᱩᱱᱤᱭᱟᱹ (4), ᱢᱚᱬᱮᱭᱟ (5)᱾ ᱟᱵᱚ ᱢᱟᱦᱩᱣᱟ ᱡᱟᱝ ᱛᱮ ᱞᱮᱠᱷᱟ ᱵᱚᱱ ᱪᱮᱫ-ᱟ᱾",
            nativeScriptText = "ᱢᱤᱭᱟᱹᱫᱽ (1), ᱵᱟᱨᱤᱭᱟ (2), ᱟᱹᱯᱤᱭᱟᱹ (3), ᱩᱯᱩᱱᱤᱭᱟᱹ (4), ᱢᱚᱬᱮᱭᱟ (5) (Miyad, Bariya, Apiya, Upuniya, Moneya)",
            transliterationText = "Miyad (1), Bariya (2), Apiya (3), Upuniya (4), Moneya (5). Abo mahuwa jang te lekha bon ched-a.",
            culturalAnalogy = "गाँव के हाट (बाज़ार) में महुआ फल और इमली के दानों से हिसाब लगाने की विधि।",
            activityPrompt = "अपनी हथेली की पाँचों उंगलियों को गिनते हुए हो (Ho) भाषा में संख्याएं दोहराएं।",
            pronunciationGuide = "हो भाषा में 1 को 'Miyad' (मियाद) और 2 को 'Bariya' (बरिया) बोलते हैं।",
            status = "APPROVED",
            qualityScore = 0.94f,
            groundingScore = 0.95f,
            approvedAt = System.currentTimeMillis() - 3600000L,
            syncStatus = "SYNCED"
        )
    )

    val defaultWorksheets = listOf(
        WorksheetEntity(
            id = "ws_1",
            lessonId = "les_1",
            title = "साल का वृक्ष व पर्यावरण कार्यपत्रक (Sarjom Dare Worksheet)",
            grade = "Grade 2 (कक्षा 2)",
            targetLanguage = "Santhali (ᱥᱟᱱᱛᱟᱲᱤ)",
            instructions = "निर्देश: सभी प्रश्नों को ध्यान से पढ़ें और संथाली (Ol Chiki) व हिन्दी में उत्तर दें।",
            questionsJson = listOf(
                WorksheetQuestion(
                    id = "ws_q_1_1",
                    questionHindi = "साल के पेड़ को संथाली भाषा (Ol Chiki) में क्या कहा जाता है?",
                    questionTarget = "ᱥᱟᱞ ᱫᱟᱨᱮ ᱥᱟᱱᱛᱟᱲᱤ ᱛᱮ ᱪᱮᱫ ᱠᱚ ᱢᱮᱛᱟᱜ-ᱟ?",
                    type = "MCQ",
                    options = listOf(
                        "A. ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱮ (Sarjom Dare)",
                        "B. ᱫᱟᱜ (Dah)",
                        "C. ᱥᱤᱝ (Sing)",
                        "D. ᱵᱤᱨ (Bir)"
                    ),
                    correctAnswer = "A. ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱮ (Sarjom Dare)",
                    localContextHint = "सरहुल पर्व में साल के नए फूलों की पूजा की जाती है।"
                ),
                WorksheetQuestion(
                    id = "ws_q_1_2",
                    questionHindi = "सही जोड़ी बनाएं: 'ᱫᱟᱜ' (Dah) का हिंदी अर्थ क्या है?",
                    questionTarget = "ᱡᱚᱲ ᱵᱮᱱᱟᱣ ᱢᱮ: 'ᱫᱟᱜ' ᱨᱮᱱᱟᱜ ᱢᱮᱱᱮᱛ ᱪᱮᱫ ᱠᱟᱱᱟ?",
                    type = "MATCH",
                    options = listOf(
                        "A. जल / पानी",
                        "B. अग्नि / आग",
                        "C. पवन / हवा",
                        "D. जंगल / वन"
                    ),
                    correctAnswer = "A. जल / पानी",
                    localContextHint = "गाँव के झरने और कुएं के स्वच्छ जल को संथाली में 'ᱫᱟᱜ' (Dah) कहते हैं।"
                ),
                WorksheetQuestion(
                    id = "ws_q_1_3",
                    questionHindi = "सही या गलत: पेड़ हमें स्वच्छ हवा (ᱦᱚᱭ) और छाया (ᱩᱢᱩᱞ) देते हैं।",
                    questionTarget = "ᱥᱟᱹᱨᱤ ᱥᱮ ᱮᱲᱮ: ᱫᱟᱨᱮ ᱫᱚ ᱟᱵᱚ ᱥᱟᱯᱷᱟ ᱦᱚᱭ ᱟᱨ ᱩᱢᱩᱞ ᱮᱢᱟ ᱵᱚᱱᱟ᱾",
                    type = "TRUE_FALSE",
                    options = listOf(
                        "A. सही (ᱥᱟᱹᱨᱤ)",
                        "B. गलत (ᱮᱲᱮ)"
                    ),
                    correctAnswer = "A. सही (ᱥᱟᱹᱨᱤ)",
                    localContextHint = "पेड़ हमारे पर्यावरण की आत्मा हैं, इनकी रक्षा करना हमारा धर्म है।"
                ),
                WorksheetQuestion(
                    id = "ws_q_1_4",
                    questionHindi = "मौखिक अभिव्यक्ति: संथाली में अभिवादन के लिए कौन सा शब्द बोला जाता है?",
                    questionTarget = "ᱨᱚᱲ ᱟᱵᱷᱭᱟᱥ: ᱥᱟᱱᱛᱟᱲᱤ ᱛᱮ ᱡᱚᱦᱟᱨ (Greetings) ᱪᱮᱫ ᱠᱟᱱᱟ?",
                    type = "ORAL_PRACTICE",
                    options = listOf(
                        "A. ᱡᱚᱦᱟᱨ (Johar)",
                        "B. ᱫᱟᱠᱟ (Daka)",
                        "C. ᱵᱤᱨ (Bir)",
                        "D. ᱥᱟᱦᱟᱱ (Sahan)"
                    ),
                    correctAnswer = "A. ᱡᱚᱦᱟᱨ (Johar)",
                    localContextHint = "कक्षा में शिक्षक और मित्रों को दोनों हाथ जोड़कर 'जोहार' कहें।"
                )
            ).toJsonString(),
            isApproved = true,
            createdAt = System.currentTimeMillis() - 86400000L
        ),
        WorksheetEntity(
            id = "ws_2",
            lessonId = "les_2",
            title = "संख्या बोध व गिनती कार्यपत्रक (Ho Counting 1 to 5)",
            grade = "Grade 1 (बालवाटिका / कक्षा 1)",
            targetLanguage = "Ho (ᱦᱳ)",
            instructions = "निर्देश: महुआ के बीज और पत्तों को गिनते हुए हो (Ho) भाषा में संख्याएं पहचानें।",
            questionsJson = listOf(
                WorksheetQuestion(
                    id = "ws_q_2_1",
                    questionHindi = "हो भाषा में संख्या '1' को क्या कहते हैं?",
                    questionTarget = "ᱦᱳ ᱵᱷᱟᱥᱟ ᱛᱮ '᱑' ᱪᱮᱫ ᱠᱚ ᱢᱮᱛᱟᱜ-ᱟ?",
                    type = "MCQ",
                    options = listOf(
                        "A. ᱢᱤᱭᱟᱹᱫᱽ (Miyad / मियाद)",
                        "B. ᱵᱟᱨᱤᱭᱟ (Bariya / बरिया)",
                        "C. ᱟᱹᱯᱤᱭᱟᱹ (Apiya / अपिया)",
                        "D. ᱩᱯᱩᱱᱤᱭᱟᱹ (Upuniya / उपुनिया)"
                    ),
                    correctAnswer = "A. ᱢᱤᱭᱟᱹᱫᱽ (Miyad / मियाद)",
                    localContextHint = "एक कलम या एक बीज के लिए 'मियाद' का प्रयोग करें।"
                ),
                WorksheetQuestion(
                    id = "ws_q_2_2",
                    questionHindi = "हो भाषा में 'दो' (2) के लिए सही शब्द चुनें:",
                    questionTarget = "ᱦᱳ ᱛᱮ '᱒' (Two) ᱨᱮᱱᱟᱜ ᱟᱹᱲᱟᱹ ᱪᱤᱱᱦᱟᱹᱣ ᱢᱮ:",
                    type = "MATCH",
                    options = listOf(
                        "A. ᱵᱟᱨᱤᱭᱟ (Bariya)",
                        "B. ᱢᱚᱬᱮᱭᱟ (Moneya)",
                        "C. ᱛᱩᱨᱩᱭᱟ (Turuiya)",
                        "D. ᱢᱤᱭᱟᱹᱫᱽ (Miyad)"
                    ),
                    correctAnswer = "A. ᱵᱟᱨᱤᱭᱟ (Bariya)",
                    localContextHint = "हमारे पास दो आँखें (ᱵᱟᱨᱤᱭᱟ ᱢᱮᱫ) हैं।"
                ),
                WorksheetQuestion(
                    id = "ws_q_2_3",
                    questionHindi = "सही या गलत: ᱢᱚᱬᱮᱭᱟ (Moneya) का अर्थ 'पाँच' (5) होता है।",
                    questionTarget = "ᱥᱟᱹᱨᱤ ᱥᱮ ᱮᱲᱮ: ᱢᱚᱬᱮᱭᱟ (Moneya) ᱨᱮᱱᱟᱜ ᱢᱮᱱᱮᱛ ᱫᱚ ᱕ (Five) ᱠᱟᱱᱟ᱾",
                    type = "TRUE_FALSE",
                    options = listOf(
                        "A. सही (ᱥᱟᱹᱨᱤ)",
                        "B. गलत (ᱮᱲᱮ)"
                    ),
                    correctAnswer = "A. सही (ᱥᱟᱹᱨᱤ)",
                    localContextHint = "एक हाथ में पाँच उंगलियाँ होती हैं - ᱢᱚᱬᱮᱭᱟ ᱜᱚᱴᱟᱝ ᱠᱟᱹᱴᱩᱵ᱾"
                ),
                WorksheetQuestion(
                    id = "ws_q_2_4",
                    questionHindi = "मौखिक गिनती अभ्यास: 1 से 3 तक हो भाषा में बोलें।",
                    questionTarget = "ᱞᱮᱠᱷᱟ ᱟᱵᱷᱭᱟᱥ: ᱑ ᱠᱷᱚᱱ ᱓ ᱦᱟᱹᱵᱤᱡ ᱦᱳ ᱛᱮ ᱨᱚᱲ ᱢᱮ (Miyad, Bariya, Apiya)",
                    type = "ORAL_PRACTICE",
                    options = listOf(
                        "A. Miyad (1), Bariya (2), Apiya (3)",
                        "B. Upuniya (4), Moneya (5)",
                        "C. Turuiya (6), Eya (7)",
                        "D. Iral (8), Are (9)"
                    ),
                    correctAnswer = "A. Miyad (1), Bariya (2), Apiya (3)",
                    localContextHint = "स्थानीय हाट में वस्तुएं गिनने में यह उपयोगी है।"
                )
            ).toJsonString(),
            isApproved = true,
            createdAt = System.currentTimeMillis() - 43200000L
        ),
        WorksheetEntity(
            id = "ws_3",
            lessonId = "les_3",
            title = "जल, प्रकृति व गाँव कार्यपत्रक (Mundari Water & Nature)",
            grade = "Grade 2 (कक्षा 2)",
            targetLanguage = "Mundari (मुण्डारी)",
            instructions = "निर्देश: मुण्डारी शब्दों को पहचानें और अपने गाँव के जल स्रोतों के बारे में बताएं।",
            questionsJson = listOf(
                WorksheetQuestion(
                    id = "ws_q_3_1",
                    questionHindi = "मुण्डारी भाषा में पानी (जल) को क्या कहते हैं?",
                    questionTarget = "मुण्डारी ते 'पानी' चेद को मेतागा?",
                    type = "MCQ",
                    options = listOf(
                        "A. दाः (Dah)",
                        "B. दारू (Daru)",
                        "C. बुरू (Buru)",
                        "D. हातू (Hatu)"
                    ),
                    correctAnswer = "A. दाः (Dah)",
                    localContextHint = "नदी, कुएं और बारिश के पानी को मुण्डारी में 'दाः' कहते हैं।"
                ),
                WorksheetQuestion(
                    id = "ws_q_3_2",
                    questionHindi = "मुण्डारी शब्द 'हातू' (Hatu) का अर्थ क्या है?",
                    questionTarget = "मुण्डारी शब्द 'हातू' (Hatu) राः मने चेद?",
                    type = "MATCH",
                    options = listOf(
                        "A. गाँव (Village)",
                        "B. पहाड़ (Mountain)",
                        "C. नदी (River)",
                        "D. खेत (Field)"
                    ),
                    correctAnswer = "A. गाँव (Village)",
                    localContextHint = "खूंटी और रांची के मुण्डारी गाँवों को 'हातू' कहा जाता है।"
                ),
                WorksheetQuestion(
                    id = "ws_q_3_3",
                    questionHindi = "सही या गलत: 'बुरू' (Buru) का अर्थ पहाड़ होता है।",
                    questionTarget = "सारि या एड़े: मुण्डारी ते 'बुरू' (Buru) राः अर्थ पहाड़ तना।",
                    type = "TRUE_FALSE",
                    options = listOf(
                        "A. सही (सारि / Sari)",
                        "B. गलत (एड़े / Ede)"
                    ),
                    correctAnswer = "A. सही (सारि / Sari)",
                    localContextHint = "मरांग बुरू झारखंड के प्रकृति पूजकों के सर्वोच्च पूज्य हैं।"
                ),
                WorksheetQuestion(
                    id = "ws_q_3_4",
                    questionHindi = "मौखिक अभ्यास: मुण्डारी में 'जोहार' कहकर अपनी कक्षा का स्वागत करें।",
                    questionTarget = "काजी अभ्यास: मुण्डारी ते 'जोहार' मेन्ते स्वागत रीकाए पे।",
                    type = "ORAL_PRACTICE",
                    options = listOf(
                        "A. जोहार! (Johar - स्वागत)",
                        "B. सेनोः तनिं (Senoh taning - जा रहा हूँ)",
                        "C. दाः (Dah - पानी)",
                        "D. दारू (Daru - पेड़)"
                    ),
                    correctAnswer = "A. जोहार! (Johar - स्वागत)",
                    localContextHint = "सभी सहपाठियों से मुस्कुराकर 'जोहार' कहें।"
                )
            ).toJsonString(),
            isApproved = true,
            createdAt = System.currentTimeMillis() - 21600000L
        )
    )

    val flnSyllabusTopics = listOf(
        "कक्षा 1 - वर्णमाला व प्रथम ध्वनियां (Letter Sounds & Phonetics)",
        "कक्षा 1 - 1 से 10 तक संख्या पहचान (Number Sense 1-10)",
        "कक्षा 2 - हमारे आसपास के पशु-पक्षी (Local Animals & Birds)",
        "कक्षा 2 - सरल जोड़ व घटाव (Basic Addition & Subtraction)",
        "कक्षा 3 - जल के स्रोत और स्वच्छता (Water Sources & Sanitation)",
        "कक्षा 3 - झारखंड के लोकपर्व: सरहुल व करम (Sarhul & Karam)",
        "कक्षा 4 - मिट्टी, बीज और खेती (Soil, Seeds & Farming)",
        "कक्षा 5 - सौरमंडल और दिशा ज्ञान (Solar System & Navigation)"
    )

    val traceabilityLedger = listOf(
        TraceItem(
            prdId = "PRD-REQ-001",
            tadId = "TAD-INTELLIGENCE-01",
            sadId = "SAD-AI-RAG-01",
            fsdId = "FSD-LESSON-001",
            title = "Hindi → Tribal FLN Curriculum Grounded RAG & Adaptation",
            status = "VERIFIED E4 (Active)",
            latencyTarget = "< 2.5s Target",
            verifiedLayer = "Gemini 3.1 Pro + NCERT/JCERT Grounding"
        ),
        TraceItem(
            prdId = "PRD-REQ-005",
            tadId = "TAD-VOICE-02",
            sadId = "SAD-AI-VOICE-01",
            fsdId = "FSD-VOICE-001",
            title = "Sub-3s Live Voice-to-Voice Classroom Translation",
            status = "VERIFIED E4 (Active)",
            latencyTarget = "< 3.0s Budget",
            verifiedLayer = "Gemini 3.1 Flash-Lite + Gemini TTS"
        ),
        TraceItem(
            prdId = "PRD-REQ-010",
            tadId = "TAD-EDGE-01",
            sadId = "SAD-OFFLINE-SYNC-01",
            fsdId = "FSD-SYNC-001",
            title = "Offline-First Room DB + Durable Outbox Queue",
            status = "VERIFIED E4 (Active)",
            latencyTarget = "< 50ms Local DB",
            verifiedLayer = "Room SQLite + Conflict Resolver Engine"
        ),
        TraceItem(
            prdId = "PRD-REQ-007",
            tadId = "TAD-MULTIMODAL-01",
            sadId = "SAD-AI-VISION-01",
            fsdId = "FSD-VISUAL-001",
            title = "Visual Flashcards & Veo Concept Video Animation",
            status = "VERIFIED E4 (Active)",
            latencyTarget = "Async Generation",
            verifiedLayer = "Gemini 3 Pro Image + Veo 3.1 Fast Preview"
        ),
        TraceItem(
            prdId = "PRD-REQ-009",
            tadId = "TAD-GOVERNANCE-01",
            sadId = "SAD-REVIEW-GATE-01",
            fsdId = "FSD-REVIEW-001",
            title = "Teacher Review, Approval & Provenance Audit Gate",
            status = "VERIFIED E4 (Active)",
            latencyTarget = "Human-in-the-Loop",
            verifiedLayer = "Draft → Review → Approved State Machine"
        )
    )

    val defaultCurriculumChunks = listOf(
        CurriculumContentEntity(
            id = "rag_jcert_g2_evs_01",
            state = "झारखंड (Jharkhand)",
            curriculumBoard = "JCERT / NCERT",
            grade = "कक्षा 2 (Grade 2)",
            subject = "पर्यावरण अध्ययन (EVS)",
            chapterNumber = 3,
            chapterTitle = "हमारे आसपास के पेड़-पौधे और जंगल",
            topic = "साल का पेड़ (Sarjom Dare) और सरहुल पर्व",
            learningOutcomeCode = "JCERT-EVS-G2-03",
            learningOutcomeDescription = "बच्चे अपने स्थानीय जंगल के मुख्य वृक्षों की पहचान कर सकें तथा प्रकृति पूजा के महत्व को समझें।",
            lessonTextHindi = "हमारे झारखंड के जंगलों में सबसे पवित्र और उपयोगी पेड़ साल (सखुआ) का है। साल की लकड़ी, पत्ते और फूल हमारे दैनिक जीवन और त्योहारों में काम आते हैं। सरहुल के दिन साल के नए फूलों की पूजा की जाती है।",
            pedagogicalExplanationHindi = "शिक्षक बच्चों को कक्षा के बाहर ले जाकर साल और महुआ के पत्तों का स्पर्श कराएं और दोनों में अंतर बताएं।",
            classroomActivityPrompt = "बच्चे 3 अलग-अलग पत्तियों को एकत्र करें और उनके संथाली नाम अपनी कॉपी में चित्र बनाकर लिखें।",
            oralAssessmentQuestion = "साल के पेड़ को संथाली में क्या कहते हैं और इसके फूलों का उपयोग किस पर्व में होता है?",
            tribalLanguage = "SANTHALI",
            tribalLessonText = "ᱟᱵᱚᱣᱟᱜ ᱵᱤᱨ ᱨᱮ ᱟᱹᱰᱤ ᱢᱟᱨᱟᱝ ᱫᱟᱨᱮ ᱫᱚ ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱮ ᱠᱟᱱᱟ᱾ ᱥᱟᱨᱡᱚᱢ ᱵᱟᱦᱟ ᱛᱮ ᱟᱵᱚ ᱵᱟᱦᱟ ᱯᱚᱨᱚᱵ ᱨᱮ ᱵᱚᱸᱜᱟᱭᱟ᱾",
            tribalScriptType = "OL_CHIKI",
            tribalNativeScriptText = "ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱮ, ᱫᱟᱜ ᱟᱨ ᱵᱤᱨ ᱫᱚ ᱟᱵᱚᱣᱟᱜ ᱡᱤᱣᱤ ᱠᱟᱱᱟ᱾",
            transliterationLatin = "Abowag bir re adi marang dare do sarjom dare kana. Sarjom baha te abo baha porob re bongaya.",
            transliterationDevanagari = "आबोवाग बीर रे आडी मारांग दारे दो सारजोम दारे काना। सारजोम बाहा ते आबो बाहा परब रे बोंगाया।",
            dialectOrRegion = "संथाल परगना (Dumka, Deoghar, Godda)",
            culturalContextTag = "सरहुल (Baha Porob) & जाहेरथान",
            bloomsTaxonomyLevel = "UNDERSTAND",
            difficultyLevel = "FOUNDATIONAL",
            ageGroupMinYears = 6,
            ageGroupMaxYears = 8,
            estimatedDurationMinutes = 45,
            keywordsForRetrieval = "पेड़, साल, सखुआ, ᱫᱟᱨᱮ, sarjom, baha porob, सरहुल, जंगल, पर्यावरण, पत्तियां",
            ragDenseVectorTag = "cluster_nature_botany_santhali_01",
            approvalStatus = "JCERT_VERIFIED",
            textbookSourceReference = "JCERT हमारी दुनिया (EVS) कक्षा 2, अध्याय 3, पृष्ठ 18",
            version = 1,
            isOfflineAvailable = true
        ),
        CurriculumContentEntity(
            id = "rag_jcert_g1_math_02",
            state = "झारखंड (Jharkhand)",
            curriculumBoard = "JCERT / NCERT",
            grade = "कक्षा 1 (Grade 1)",
            subject = "गणित व संख्या ज्ञान (FLN Numeracy)",
            chapterNumber = 1,
            chapterTitle = "गिनती और समूह बनाना: 1 से 10",
            topic = "स्थानीय महुआ व इमली के बीजों से संख्या बोध",
            learningOutcomeCode = "FLN-JH-N1-01",
            learningOutcomeDescription = "मूर्त वस्तुओं (कंकड़, बीज) की सहायता से 1 से 10 तक संख्याओं को गिनना और बोलना।",
            lessonTextHindi = "आज हम महुआ के बीज और इमली के दानों को गिनकर 1 से 5 तक की संख्या सीखेंगे: 1 (एक), 2 (दो), 3 (तीन), 4 (चार), 5 (पाँच)।",
            pedagogicalExplanationHindi = "बच्चों को 5-5 के समूह में बैठाकर महुआ के बीज बांटें और 'मियाद' (1), 'बरिया' (2) का समवेत उच्चारण कराएं।",
            classroomActivityPrompt = "शिक्षक जितनी ताली बजाएं, बच्चे उतने कंकड़ अपनी थाली में उठाकर हो (Ho) भाषा में संख्या बोलें।",
            oralAssessmentQuestion = "हो भाषा में 3 और 5 को क्या कहते हैं?",
            tribalLanguage = "HO",
            tribalLessonText = "ᱢᱤᱭᱟᱹᱫᱽ (1), ᱵᱟᱨᱤᱭᱟ (2), ᱟᱹᱯᱤᱭᱟᱹ (3), ᱩᱯᱩᱱᱤᱭᱟᱹ (4), ᱢᱚᱬᱮᱭᱟ (5)᱾ ᱟᱵᱚ ᱢᱟᱦᱩᱣᱟ ᱡᱟᱝ ᱛᱮ ᱞᱮᱠᱷᱟ ᱵᱚᱱ ᱪᱮᱫ-ᱟ᱾",
            tribalScriptType = "WARANG_CHITI",
            tribalNativeScriptText = "ᱢᱤᱭᱟᱹᱫᱽ (1), ᱵᱟᱨᱤᱭᱟ (2), ᱟᱹᱯᱤᱭᱟᱹ (3), ᱩᱯᱩᱱᱤᱭᱟᱹ (4), ᱢᱚᱬᱮᱭᱟ (5)",
            transliterationLatin = "Miyad (1), Bariya (2), Apiya (3), Upuniya (4), Moneya (5).",
            transliterationDevanagari = "मियाद (1), बरिया (2), अपिया (3), उपुनिया (4), मोणेया (5)।",
            dialectOrRegion = "कोल्हान प्रमंडल (Chaibasa, Seraikela, East Singhbhum)",
            culturalContextTag = "हाट-बाज़ार व बीज संचयन",
            bloomsTaxonomyLevel = "APPLY",
            difficultyLevel = "FOUNDATIONAL",
            ageGroupMinYears = 5,
            ageGroupMaxYears = 7,
            estimatedDurationMinutes = 40,
            keywordsForRetrieval = "गिनती, संख्या, महुआ, ᱢᱤᱭᱟᱹᱫᱽ, miyad, bariya, apiya, ho, गणित, fln",
            ragDenseVectorTag = "cluster_math_fln_ho_01",
            approvalStatus = "JCERT_VERIFIED",
            textbookSourceReference = "JCERT गणित का जादू कक्षा 1, पृष्ठ 12",
            version = 1,
            isOfflineAvailable = true
        ),
        CurriculumContentEntity(
            id = "rag_jcert_g2_lang_03",
            state = "झारखंड (Jharkhand)",
            curriculumBoard = "JCERT / NCERT",
            grade = "कक्षा 2 (Grade 2)",
            subject = "भाषा व बुनियादी साक्षरता (FLN)",
            chapterNumber = 2,
            chapterTitle = "मेरा परिवार और मेरा गाँव",
            topic = "रिश्ते-नाते और दैनिक संवाद",
            learningOutcomeCode = "FLN-JH-L2-02",
            learningOutcomeDescription = "बच्चे अपने परिवार के सदस्यों (माता, पिता, दादा, दादी) के लिए मातृभाषा में प्रयुक्त शब्दों को सहजता से बोल सकें।",
            lessonTextHindi = "हमारे परिवार में माँ, पिताजी, भाई और बहन मिलकर रहते हैं। हम एक-दूसरे की मदद करते हैं और शाम को साथ बैठकर बातें करते हैं।",
            pedagogicalExplanationHindi = "द्विभाषी चित्र कार्ड (Flashcard) दिखाकर मुण्डारी में रिश्तों के नाम दोहराएं।",
            classroomActivityPrompt = "रोल-प्ले गतिविधि: एक बच्चा माँ, एक पिताजी और एक बच्चा शिक्षक बनकर मुण्डारी में संवाद करेंगे।",
            oralAssessmentQuestion = "मुण्डारी भाषा में 'गाँव' और 'घर' को क्या कहते हैं?",
            tribalLanguage = "MUNDARI",
            tribalLessonText = "आबुवाः ओड़ाः रे एंगा, अपा, हागा आर मिसि मेनाकोवा। आबु हातू रे सोबेन को संगे बु तईना।",
            tribalScriptType = "DEVANAGARI_PHONETIC",
            tribalNativeScriptText = "आबुवाः ओड़ाः (घर), आबुवाः हातू (गाँव)।",
            transliterationLatin = "Abuwah odah re enga, apa, haga ar misi menakowa. Abu hatu re soben ko sange bu taina.",
            transliterationDevanagari = "आबुवाः ओड़ाः रे एंगा, अपा, हागा आर मिसि मेनाकोवा।",
            dialectOrRegion = "खूंटी व राँची ग्रामीण (Khunti, Torpa, Murhu, Tamar)",
            culturalContextTag = "ग्राम सभा व अखड़ा (Akhra)",
            bloomsTaxonomyLevel = "UNDERSTAND",
            difficultyLevel = "FOUNDATIONAL",
            ageGroupMinYears = 6,
            ageGroupMaxYears = 8,
            estimatedDurationMinutes = 45,
            keywordsForRetrieval = "परिवार, घर, गाँव, ओड़ाः, हातू, enga, apa, hatu, mundari, fln, bhasha anjali",
            ragDenseVectorTag = "cluster_language_family_mundari_01",
            approvalStatus = "JCERT_VERIFIED",
            textbookSourceReference = "JCERT भाषा अंजलि कक्षा 2, अध्याय 2, पृष्ठ 14",
            version = 1,
            isOfflineAvailable = true
        ),
        CurriculumContentEntity(
            id = "rag_jcert_g3_evs_04",
            state = "झारखंड (Jharkhand)",
            curriculumBoard = "JCERT / NCERT",
            grade = "कक्षा 3 (Grade 3)",
            subject = "पर्यावरण अध्ययन (EVS)",
            chapterNumber = 5,
            chapterTitle = "जल ही जीवन है: नदियां और जल स्रोत",
            topic = "स्वच्छ जल, नदी (Gada) और वर्षा जल संरक्षण",
            learningOutcomeCode = "JCERT-EVS-G3-05",
            learningOutcomeDescription = "प्राकृतिक जल स्रोतों की पहचान करना और जल प्रदूषण से बचने के उपाय समझना।",
            lessonTextHindi = "नदी, तालाब, कुआं और झरना हमारे जल के मुख्य स्रोत हैं। हमें पानी को गंदा नहीं करना चाहिए और बारिश के पानी को सहेजना चाहिए।",
            pedagogicalExplanationHindi = "स्थानीय स्वर्णरेखा और कोयल नदी का उदाहरण देकर बच्चों को जल संरक्षण का महत्व बताएं।",
            classroomActivityPrompt = "बच्चे अपनी स्लेट पर एक कुआं और नदी का चित्र बनाकर संथाली में 'ᱫᱟᱜ' (दाग) लिखें।",
            oralAssessmentQuestion = "संथाली में 'पानी' और 'नदी' को क्या कहते हैं?",
            tribalLanguage = "SANTHALI",
            tribalLessonText = "ᱫᱟᱜ ᱫᱚ ᱟᱵᱚᱣᱟᱜ ᱡᱤᱣᱤ ᱠᱟᱱᱟ᱾ ᱜᱟᱰᱟ ᱫᱟᱜ ᱟᱨ ᱯᱩᱠᱷᱨᱤ ᱫᱟᱜ ᱫᱚ ᱥᱟᱯᱷᱟ ᱫᱚᱦᱚᱭ ᱦᱩᱭᱩᱜ-ᱟ᱾",
            tribalScriptType = "OL_CHIKI",
            tribalNativeScriptText = "ᱫᱟᱜ (Daq - पानी), ᱜᱟᱰᱟ (Gada - नदी), ᱯᱩᱠᱷᱨᱤ (Pukhri - तालाब)᱾",
            transliterationLatin = "Dah do abowag jiwi kana. Gada dah ar pukhri dah do sapha dohoy huyug-a.",
            transliterationDevanagari = "दाग दो आबोवाग जीवी काना। गाडा दाग आर पुखरी दाग दो साफा दोहोय हुयुग-आ।",
            dialectOrRegion = "संथाल परगना व पूर्वी सिंहभूम",
            culturalContextTag = "बंधना व सोहराय जल संरक्षण",
            bloomsTaxonomyLevel = "APPLY",
            difficultyLevel = "INTERMEDIATE",
            ageGroupMinYears = 7,
            ageGroupMaxYears = 9,
            estimatedDurationMinutes = 45,
            keywordsForRetrieval = "पानी, जल, नदी, ᱫᱟᱜ, ᱜᱟᱰᱟ, daq, gada, pukhri, santhali, evs, जल संरक्षण",
            ragDenseVectorTag = "cluster_evs_water_santhali_01",
            approvalStatus = "JCERT_VERIFIED",
            textbookSourceReference = "JCERT पर्यावरण अध्ययन कक्षा 3, अध्याय 5, पृष्ठ 32",
            version = 1,
            isOfflineAvailable = true
        )
    )

    // --- Full-Stack Tech Stack Architecture Tiers ---

    val fullStackTiers = listOf(
        com.example.domain.model.TechStackTier(
            id = "tier_web_fe",
            title = "Web Frontend",
            subtitle = "Admin Dashboard, Curriculum Authoring & School Analytics",
            category = "CLIENT",
            primaryTech = "Next.js 16.3+ (App Router) + React 19.2",
            version = "Next.js 16.3.2 / React 19.2.0 / TS 5.4",
            keyLibraries = listOf("Tailwind CSS 3", "Radix UI", "TanStack Query v5", "Zustand", "MSW v2 (Mock Service Worker)"),
            protocols = listOf("HTTPS / TLS 1.3", "SSE (Server-Sent Events)", "WebSocket", "REST"),
            responsibilities = listOf(
                "Instant Navigations SPA-like responsiveness for curriculum editors",
                "State caching and stale-time invalidation via TanStack Query v5",
                "Offline API mocking via MSW v2 for uninterrupted UI development",
                "Responsive dashboard for block education officers (BEO) & master trainers"
            ),
            slaOrLatency = "< 100ms Page Load / Real-time SSE Stream",
            hardwareOrHosting = "Vercel Edge / Netlify CDN Edge Nodes",
            offlineCapabilities = "PWA Service Worker + IndexedDB Local Cache",
            iconEmoji = "💻"
        ),
        com.example.domain.model.TechStackTier(
            id = "tier_mobile_app",
            title = "Mobile Teacher App",
            subtitle = "Offline-First Classroom Tablet for Rural Jharkhand",
            category = "CLIENT",
            primaryTech = "Flutter 3.x (3.47+) & Native Jetpack Compose",
            version = "Flutter 3.47.0 / Dart 3.7 / Android 9.0+ (API 28+)",
            keyLibraries = listOf("BLoC / Cubit Pattern", "sqflite / Drift", "Room SQLite", "WorkManager (Background Sync)"),
            protocols = listOf("Local SQLite IPC", "Durable Outbox Queue (HTTPS/gRPC)", "On-Device ASR Bridge"),
            responsibilities = listOf(
                "Guaranteed offline operation on low-cost 2 GB RAM Android tablets",
                "Immediate local write-first architecture with zero-latency UI updates",
                "Durable Outbox pattern with exponential backoff & sync cursor continuation",
                "Real-time voice playback and audio flashcards for tribal classrooms"
            ),
            slaOrLatency = "< 50ms Local DB Write / < 3.0s Voice Translation SLA",
            hardwareOrHosting = "On-Device Tablet (ARMv8 / Android 9.0+ / 2 GB RAM)",
            offlineCapabilities = "100% Offline (24h+ local lesson cache & SQLite outbox)",
            iconEmoji = "📱"
        ),
        com.example.domain.model.TechStackTier(
            id = "tier_web_be",
            title = "Web Backend Platform",
            subtitle = "Auth, RBAC, Multi-Tenancy & Business Logic Orchestrator",
            category = "BACKEND",
            primaryTech = "NestJS 11 + Node.js LTS (TypeScript)",
            version = "NestJS 11.0.4 / Node.js 22 LTS / TypeScript 5.4",
            keyLibraries = listOf("OpenAPI / Swagger 3.0", "Fastify Engine", "Passport JWT / Firebase Auth", "TypeORM / Drizzle", "BullMQ (Redis Queue)"),
            protocols = listOf("RESTful JSON", "OpenAPI Contract", "Internal gRPC (to AI Platform)", "BullMQ Redis IPC"),
            responsibilities = listOf(
                "Role-based access control (RBAC: Teacher, BEO, Linguist, Admin)",
                "Multi-tenancy scoping by schoolId and workspaceId",
                "Contract synchronization with frontend via auto-generated OpenAPI schemas",
                "Dispatches asynchronous generation & batch translation jobs to AI workers"
            ),
            slaOrLatency = "< 25ms API P95 Latency / High-throughput Fastify",
            hardwareOrHosting = "AWS ECS Fargate / Render Cloud / Docker Container",
            offlineCapabilities = "Cloud Synchronization Hub & Durable Outbox Receiver",
            iconEmoji = "🛡️"
        ),
        com.example.domain.model.TechStackTier(
            id = "tier_ai_ml",
            title = "AI/ML Microservices",
            subtitle = "RAG Retrieval, ASR, MT, TTS & Quality Estimation",
            category = "AI_ML",
            primaryTech = "Python 3.12+ + FastAPI (Starlette)",
            version = "FastAPI 0.111+ / Python 3.12 / PyTorch 2.4",
            keyLibraries = listOf("BGE-M3 Multilingual Embeddings", "Unbabel COMET / XCOMET", "Google Gemini 3.5 Live & 3.1 Pro", "HuggingFace Transformers", "Celery / Ray"),
            protocols = listOf("gRPC", "HTTP/2 REST (Pydantic)", "SSE Token Streaming"),
            responsibilities = listOf(
                "Hybrid RAG retrieval combining dense BGE-M3 vectors & BM25 sparse index",
                "High-accuracy Hindi → Santhali/Ho/Mundari translation & pedagogical prompting",
                "Automated MT Quality Estimation via COMETKiwi (>= 0.8) & XCOMET MQM span triage",
                "Voice pipeline orchestration: Streaming ASR + Gemini LLM + Neural TTS"
            ),
            slaOrLatency = "< 250ms RAG Inference / < 2.5s Total Translation Cycle",
            hardwareOrHosting = "GPU-Accelerated Cloud Workers (NVIDIA L4 / A10G)",
            offlineCapabilities = "Fallback to On-Device Quantized Model + Cached Phrasebook",
            iconEmoji = "🧠"
        ),
        com.example.domain.model.TechStackTier(
            id = "tier_data_infra",
            title = "Data & Vector Infrastructure",
            subtitle = "Relational Ledger, DiskANN Vector Index & Redis Cache",
            category = "DATA_INFRA",
            primaryTech = "PostgreSQL 18 + pgvector / StreamingDiskANN",
            version = "PostgreSQL 18.0 / pgvectorscale (DiskANN) / Redis 7.4",
            keyLibraries = listOf("pgvector (Dense 1024-dim)", "StreamingDiskANN (Memory efficient)", "Redis Cache", "MinIO / S3 Object Storage"),
            protocols = listOf("PostgreSQL Wire Protocol", "Redis RESP3", "S3 API"),
            responsibilities = listOf(
                "Stores 25,000+ curriculum chunks with sub-6ms similarity search",
                "DiskANN enables 21 MB index memory footprint vs 193 MB for traditional HNSW",
                "Redis manages distributed session tokens, idempotency locks & sync cursors",
                "Object storage holds generated audio recordings, worksheets & lesson PDFs"
            ),
            slaOrLatency = "3.2ms Vector Query P95 / 99.4% Recall@10",
            hardwareOrHosting = "Managed Cloud PostgreSQL (AWS RDS / GCP Cloud SQL)",
            offlineCapabilities = "Mirrored in local SQLite for tablet edge nodes",
            iconEmoji = "🗄️"
        )
    )

    // --- Monorepo Structure & Contract Specs ---

    val monorepoNodes = listOf(
        com.example.domain.model.MonorepoNode(
            path = "/bhashasetu/apps/web-frontend",
            name = "web-frontend",
            type = "APP",
            techStack = "Next.js 16.3 + React 19.2 + TS",
            purpose = "Administrative portal, state-level dashboard, curriculum authoring & BEO analytics",
            exposedContracts = listOf("Routes: /admin/curriculum, /schools, /analytics", "TanStack Queries: useLessonQuery(), useSyncState()"),
            sampleCodeSnippet = """
// apps/web-frontend/src/features/curriculum/useCurriculum.ts
import { useQuery } from '@tanstack/react-query';
import { apiContract } from '@bhashasetu/contracts';

export function useCurriculum(grade: string, language: string) {
  return useQuery({
    queryKey: ['curriculum', grade, language],
    queryFn: () => apiContract.curriculum.getCurriculumChunks({ grade, language }),
    staleTime: 1000 * 60 * 5, // 5 min cache
  });
}
            """.trimIndent()
        ),
        com.example.domain.model.MonorepoNode(
            path = "/bhashasetu/apps/mobile",
            name = "mobile",
            type = "APP",
            techStack = "Flutter 3.47+ & Jetpack Compose",
            purpose = "Teacher tablet app with offline-first write model, Room SQLite, outbox queue & audio TTS",
            exposedContracts = listOf("Local Room DB: LessonDao, OutboxDao", "Services: VoiceTranslateService, SyncCursorService"),
            sampleCodeSnippet = """
// apps/mobile/lib/data/repositories/offline_first_lesson_repo.dart
class OfflineFirstLessonRepository {
  final LocalDatabase db;
  final SyncOutbox outbox;

  Future<void> saveAndQueueLesson(LessonDraft draft) async {
    // 1. Immediate SQLite Write
    await db.lessonDao.insert(draft.toEntity());
    // 2. Queue in Durable Outbox
    await outbox.enqueue(ActionType.LESSON_CREATE, draft.toJson());
  }
}
            """.trimIndent()
        ),
        com.example.domain.model.MonorepoNode(
            path = "/bhashasetu/services/web-backend",
            name = "web-backend",
            type = "SERVICE",
            techStack = "NestJS 11 + Fastify + TypeScript",
            purpose = "RBAC auth, workspace tenancy, curriculum persistence, OpenAPI gateway & Redis queues",
            exposedContracts = listOf("OpenAPI / Swagger 3.0", "POST /api/v1/sync/cursor", "GET /api/v1/lessons"),
            sampleCodeSnippet = """
// services/web-backend/src/sync/sync.controller.ts
@Controller('api/v1/sync')
export class SyncController {
  @Post('cursor')
  @ApiOperation({ summary: 'Durable sync continuation for tablet offline outbox' })
  async processOutboxBatch(@Body() batch: SyncBatchDto): Promise<SyncResultDto> {
    return this.syncService.reconcileOutbox(batch.schoolId, batch.mutations, batch.cursor);
  }
}
            """.trimIndent()
        ),
        com.example.domain.model.MonorepoNode(
            path = "/bhashasetu/services/ai-platform",
            name = "ai-platform",
            type = "SERVICE",
            techStack = "FastAPI + Python 3.12 + PyTorch",
            purpose = "BGE-M3 multilingual RAG, Gemini LLM agent, ASR/TTS & COMETKiwi quality estimation",
            exposedContracts = listOf("POST /ai/v1/rag/retrieve", "POST /ai/v1/translate/qe", "gRPC: AudioStreamService"),
            sampleCodeSnippet = """
# services/ai-platform/src/rag/hybrid_retriever.py
from fastapi import FastAPI, Depends
from pydantic import BaseModel

@app.post("/ai/v1/rag/retrieve", response_model=RagResponse)
async def hybrid_retrieve(req: RagQueryRequest):
    dense_vec = bge_m3_model.encode(req.query)
    # DiskANN pgvector query + BM25 Reciprocal Rank Fusion
    results = await db.diskann_hybrid_search(dense_vec, req.query, top_k=req.top_k)
    return RagResponse(matches=results, latency_ms=4.2)
            """.trimIndent()
        ),
        com.example.domain.model.MonorepoNode(
            path = "/bhashasetu/packages/contracts",
            name = "contracts",
            type = "PACKAGE",
            techStack = "TypeScript 5 + JSON Schema + Zod",
            purpose = "Shared type definitions, OpenAPI specifications & DTO schemas shared across all tiers",
            exposedContracts = listOf("LessonDto", "SyncBatchDto", "QeScoreDto", "CurriculumChunkSchema"),
            sampleCodeSnippet = """
// packages/contracts/src/lesson.contract.ts
import { z } from 'zod';

export const LessonDtoSchema = z.object({
  id: z.string().uuid(),
  grade: z.string(),
  subject: z.string(),
  targetLanguage: z.enum(['SANTHALI', 'HO', 'MUNDARI']),
  hindiText: z.string(),
  tribalText: z.string(),
  cometScore: z.number().min(0).max(1),
});
export type LessonDto = z.infer<typeof LessonDtoSchema>;
            """.trimIndent()
        )
    )

    // --- DiskANN vs HNSW Memory & Performance Benchmarks ---

    val diskAnnBenchmarks = listOf(
        com.example.domain.model.DiskAnnComparison(
            metricName = "इंडेक्स मेमोरी फुटप्रिंट (Index Memory Footprint)",
            diskAnnValue = "21 MB (pgvectorscale StreamingDiskANN)",
            hnswValue = "193 MB (पारंपरिक HNSW Index)",
            deltaAdvantage = "89% कम RAM उपयोग (8.9x अधिक मेमोरी-कुशल)",
            isAdvantage = true
        ),
        com.example.domain.model.DiskAnnComparison(
            metricName = "RAG खोज लेटेंसी (Query Search Latency P95)",
            diskAnnValue = "3.2 ms (Disk-backed streaming)",
            hnswValue = "18.5 ms (RAM cache thrashing under load)",
            deltaAdvantage = "5.7x तीव्र लेटेंसी (Sub-5ms SLA)",
            isAdvantage = true
        ),
        com.example.domain.model.DiskAnnComparison(
            metricName = "पुनःप्राप्ति सटीकता (Recall@10 Accuracy)",
            diskAnnValue = "99.4% (Dense BGE-M3 + BM25 Fusion)",
            hnswValue = "99.1% (Standard Cosine Graph)",
            deltaAdvantage = "+0.3% उच्च सटीकता",
            isAdvantage = true
        ),
        com.example.domain.model.DiskAnnComparison(
            metricName = "इंडेक्स निर्माण समय (25k Chunks Build Time)",
            diskAnnValue = "14.2 सेकण्ड (Parallelized Vamana Graph)",
            hnswValue = "48.6 सेकण्ड (Single-thread graph insertion)",
            deltaAdvantage = "3.4x तीव्र इंडेक्सिंग गति",
            isAdvantage = true
        ),
        com.example.domain.model.DiskAnnComparison(
            metricName = "सर्वर लागत दक्षता (Hosting Cost per 1M Vectors)",
            diskAnnValue = "$18 / माह (Standard SSD Storage)",
            hnswValue = "$120 / माह (High-RAM Compute Instances)",
            deltaAdvantage = "85% सर्वर लागत में बचत",
            isAdvantage = true
        )
    )

    // --- Preloaded COMETKiwi & XCOMET Quality Evaluation Cases ---

    val sampleQualityEstimations = listOf(
        com.example.domain.model.CometQualityEstimation(
            cometScore = 0.94f,
            xcometConfidence = 0.96f,
            confidenceTier = "HIGH (उच्च विश्वास)",
            actionDecision = "AUTO_PUBLISH (स्वतः प्रकाशित)",
            sourceHindi = "साल का पेड़ हमारे झारखंड का सबसे पवित्र और उपयोगी वृक्ष है।",
            translatedTribal = "ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱮ ᱫᱚ ᱟᱵᱚᱣᱟᱜ ᱡᱷᱟᱨᱠᱷᱚᱸᱰ ᱨᱮᱱᱟᱜ ᱡᱚᱛᱚ ᱠᱷᱚᱱ ᱵᱚᱸᱜᱟ ᱟᱨ ᱠᱟᱹᱢᱤ ᱫᱟᱨᱮ ᱠᱟᱱᱟ᱾",
            targetLanguage = com.example.domain.model.TargetLanguage.SANTHALI,
            detectedErrorSpans = emptyList(),
            explanation = "COMETKiwi स्कोर 0.94 (सीमा >= 0.80)। ओल चिकी लिपि और अर्थगत सटीकता 100% सही है। कोई त्रुटि नहीं पाई गई।",
            evaluationLatencyMs = 142L
        ),
        com.example.domain.model.CometQualityEstimation(
            cometScore = 0.82f,
            xcometConfidence = 0.84f,
            confidenceTier = "MEDIUM (मध्यम विश्वास)",
            actionDecision = "TEACHER_REVIEW_REQUIRED (शिक्षक समीक्षा अनिवार्य)",
            sourceHindi = "नदी का पानी बहुत ठंडा और मीठा है।",
            translatedTribal = "ᱜᱟᱰᱟ ᱫᱟᱜ ᱫᱚ ᱟᱹᱰᱤ ᱨᱮᱭᱟᱲ ᱟᱨ ᱦᱮᱲᱮᱢ ᱜᱮᱭᱟ᱾",
            targetLanguage = com.example.domain.model.TargetLanguage.SANTHALI,
            detectedErrorSpans = listOf(
                com.example.domain.model.MqmErrorSpan(
                    tokenOrSpan = "ᱦᱮᱲᱮᱢ (Hedem - Sweet)",
                    severity = "MINOR",
                    category = "TERMINOLOGY",
                    startIndex = 24,
                    endIndex = 30,
                    suggestedFix = "साफ जल के लिए 'ᱥᱟᱯᱷᱟ' (Sapha) या 'ᱥᱤᱵᱤᱞ' (Sibil) अधिक उपयुक्त है।"
                )
            ),
            explanation = "COMETKiwi स्कोर 0.82। प्राकृतिक जल संदर्भ में 'मीठा' शब्द के लिए मामूली शब्दावली सुझाव (Minor Terminology Span)।",
            evaluationLatencyMs = 175L
        ),
        com.example.domain.model.CometQualityEstimation(
            cometScore = 0.68f,
            xcometConfidence = 0.71f,
            confidenceTier = "LOW (निम्न विश्वास - अस्वीकृत)",
            actionDecision = "RETRY_ESCALATE (पुनः उत्पन्न करें / सुधारें)",
            sourceHindi = "कक्षा में सभी छात्र शांत बैठकर गणित के सवाल हल करें।",
            translatedTribal = "ᱠᱞᱟᱥ ᱨᱮ ᱡᱚᱛᱚ ᱜᱤᱫᱽᱨᱟᱹ ᱪᱩᱯ ᱪᱟᱯ ᱫᱩᱲᱩᱵ ᱠᱟᱛᱮ ᱦᱤᱥᱟᱹᱵᱽ ᱵᱮᱱᱟᱣ ᱯᱮ᱾",
            targetLanguage = com.example.domain.model.TargetLanguage.SANTHALI,
            detectedErrorSpans = listOf(
                com.example.domain.model.MqmErrorSpan(
                    tokenOrSpan = "ᱪᱩᱯ ᱪᱟᱯ (Chup Chap)",
                    severity = "MAJOR",
                    category = "UNTRANSLATED",
                    startIndex = 18,
                    endIndex = 26,
                    suggestedFix = "संथाली में 'ᱛᱷᱤᱨ ᱠᱟᱛᱮ' (Thir kate - शांत होकर) का उपयोग करें।"
                ),
                com.example.domain.model.MqmErrorSpan(
                    tokenOrSpan = "ᱠᱞᱟᱥ (Class)",
                    severity = "MINOR",
                    category = "TERMINOLOGY",
                    startIndex = 0,
                    endIndex = 5,
                    suggestedFix = "'ᱯᱟᱲᱦᱟᱣ ᱚᱲᱟᱜ' (Padhaw Odah) अधिक शुद्ध है।"
                )
            ),
            explanation = "COMETKiwi स्कोर 0.68 (< 0.80 न्यूनतम सीमा)। अपूर्ण अनुवाद और हिंदी मुहावरों का सीधा उपयोग। स्वतः पुनः निर्माण आवश्यक।",
            evaluationLatencyMs = 198L
        )
    )

    val defaultPracticeQuestions = listOf(
        com.example.domain.model.PracticeQuizQuestion(
            id = "q_1",
            questionHindi = "साल के पेड़ को संथाली (Ol Chiki) में क्या कहते हैं?",
            questionTarget = "ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱮ (Sarjom Dare) ᱪᱮᱫ ᱠᱟᱱᱟ?",
            targetLanguage = com.example.domain.model.TargetLanguage.SANTHALI,
            options = listOf(
                "A. ᱫᱟᱨᱮ (Dare / साधारण पेड़)",
                "B. ᱫᱟᱜ (Dah / पानी)",
                "C. ᱪᱮᱬᱮ (Chene / पक्षी)",
                "D. ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱮ (Sarjom Dare / पवित्र साल वृक्ष)"
            ),
            correctIndex = 3,
            explanationHindi = "संथाली में साल के पेड़ को 'ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱮ' (Sarjom Dare) कहते हैं। यह सरहुल बाहा परब का प्रमुख वृक्ष है।",
            audioUtterance = "Sarjom dare do saal ka ped hai."
        ),
        com.example.domain.model.PracticeQuizQuestion(
            id = "q_2",
            questionHindi = "हो (Ho) भाषा में संख्या 'तीन (3)' को क्या कहते हैं?",
            questionTarget = "ᱦᱳ ᱵᱷᱟᱥᱟ ᱨᱮ ᱞᱮᱠᱷᱟ '3' ᱫᱚ ᱪᱮᱫ ᱠᱚ ᱢᱮᱛᱟᱜ-ᱟ?",
            targetLanguage = com.example.domain.model.TargetLanguage.HO,
            options = listOf(
                "A. ᱢᱤᱭᱟᱹᱫᱽ (Miyad / 1)",
                "B. ᱵᱟᱨᱤᱭᱟ (Bariya / 2)",
                "C. ᱟᱯᱤᱭᱟ (Apiya / 3)",
                "D. ᱩᱯᱩᱱᱤᱭᱟ (Upuniya / 4)"
            ),
            correctIndex = 2,
            explanationHindi = "हो भाषा में संख्या 3 को 'ᱟᱯᱤᱭᱟ' (Apiya) कहते हैं। (1: Miyad, 2: Bariya, 3: Apiya, 4: Upuniya).",
            audioUtterance = "Ho bhasha me teen ko Apiya kehte hain."
        ),
        com.example.domain.model.PracticeQuizQuestion(
            id = "q_3",
            questionHindi = "मुण्डारी (Mundari) में 'जल/पानी' के लिए कौन सा शब्द सही है?",
            questionTarget = "मुण्डारी रे 'जल' को चेनाः मेनाः?",
            targetLanguage = com.example.domain.model.TargetLanguage.MUNDARI,
            options = listOf(
                "A. दाः (Dah / पानी)",
                "B. दारू (Daru / पेड़)",
                "C. हातु (Hatu / गाँव)",
                "D. ओड़ाः (Odah / घर)"
            ),
            correctIndex = 0,
            explanationHindi = "मुण्डारी भाषा में पानी को 'दाः' (Dah) कहा जाता है।",
            audioUtterance = "Mundari bhasha me paani ko Dah kehte hain."
        ),
        com.example.domain.model.PracticeQuizQuestion(
            id = "q_4",
            questionHindi = "संथाली में 'नमस्ते/प्रणाम' कैसे कहा जाता है?",
            questionTarget = "ᱥᱟᱱᱛᱟᱲᱤ ᱛᱮ ᱡᱚᱦᱟᱨ (Greetings) ᱪᱮᱫ ᱠᱟᱱᱟ?",
            targetLanguage = com.example.domain.model.TargetLanguage.SANTHALI,
            options = listOf(
                "A. ᱫᱟᱠᱟ (Daka / भात)",
                "B. ᱡᱚᱦᱟᱨ (Johar / प्रणाम)",
                "C. ᱵᱤᱨ (Bir / जंगल)",
                "D. ᱥᱤᱝ (Sing / सूर्य)"
            ),
            correctIndex = 1,
            explanationHindi = "संथाली और जनजातीय संस्कृति में अभिवादन के लिए 'ᱡᱚᱦᱟᱨ' (Johar) बोला जाता है।",
            audioUtterance = "Johargidra ko, pranaam ko Johar kehte hain."
        ),
        com.example.domain.model.PracticeQuizQuestion(
            id = "q_5",
            questionHindi = "सरहुल त्योहार को संथाली में किस नाम से जाना जाता है?",
            questionTarget = "ᱥᱟᱨᱦᱩᱞ ᱯᱟᱨᱟᱵᱽ ᱥᱟᱱᱛᱟᱲᱤ ᱛᱮ ᱪᱮᱫ ᱠᱚ ᱢᱮᱛᱟᱜ-ᱟ?",
            targetLanguage = com.example.domain.model.TargetLanguage.SANTHALI,
            options = listOf(
                "A. ᱥᱚᱦᱨᱟᱭ (Sohrai)",
                "B. ᱠᱟᱨᱟᱢ (Karam)",
                "C. ᱵᱟᱦᱟ ᱯᱚᱨᱚᱵ (Baha Porob)",
                "D. ᱫᱟᱥᱟᱸᱭ (Dasai)"
            ),
            correctIndex = 2,
            explanationHindi = "सरहुल को संथाली में 'ᱵᱟᱦᱟ ᱯᱚᱨᱚᱵ' (Baha Porob - फूलों का पर्व) कहा जाता है जिसमें साल के फूलों की पूजा होती है।",
            audioUtterance = "Sarhul tyohar ko Santhali me Baha Porob kehte hain."
        )
    )

    val defaultOfflineTabletState = com.example.domain.model.OfflineTabletState(
        tabletId = "TAB-JH-KHT-042",
        schoolCode = "SCH-200401-GPS-KHUNTI",
        localDbEngine = "SQLite 3.42 (Room / Drift WAL Mode)",
        ramAvailableMb = 1920,
        syncCursorPosition = 10428L,
        pendingOutboxMutations = 0,
        lastSyncTimestamp = System.currentTimeMillis() - 180000L,
        retryBackoffSeconds = 5,
        isNetworkAvailable = true
    )

    val defaultStudentFlashcards = listOf(
        StudentFlashcard(
            id = "fc_1",
            category = "🐾 पशु-पक्षी (Animals)",
            hindiWord = "गाय (गौमाता)",
            santhaliWord = "ᱜᱟᱹᱭ (Gai)",
            santhaliOlChiki = "ᱜᱟᱹᱭ",
            hoWord = "ᱜᱟᱹᱭ (Gai)",
            hoDevanagari = "गई",
            mundariWord = "उरीः (Urih)",
            devanagariPhonetic = "गई",
            englishMeaning = "Cow",
            iconEmoji = "🐄",
            exampleSentenceHindi = "गाय हरी घास खाती है।",
            exampleSentenceTribal = "ᱜᱟᱹᱭ ᱦᱟᱹᱨᱭᱟᱹᱲ ᱜᱷᱟᱸᱥ ᱮ ᱡᱚᱢ-ᱮᱫᱟ"
        ),
        StudentFlashcard(
            id = "fc_2",
            category = "🐾 पशु-पक्षी (Animals)",
            hindiWord = "चिड़िया (पक्षी)",
            santhaliWord = "ᱪᱮᱬᱮ (Chene)",
            santhaliOlChiki = "ᱪᱮᱬᱮ",
            hoWord = "ᱪᱮᱬᱮ (Chene)",
            hoDevanagari = "चेणे",
            mundariWord = "चेड़े (Chede)",
            devanagariPhonetic = "चेणे",
            englishMeaning = "Bird",
            iconEmoji = "🐦",
            exampleSentenceHindi = "चिड़िया पेड़ पर गाती है।",
            exampleSentenceTribal = "ᱪᱮᱬᱮ ᱫᱟᱨᱮ ᱨᱮ ᱥᱮᱨᱮᱧ-ᱮᱫᱟᱭ"
        ),
        StudentFlashcard(
            id = "fc_3",
            category = "🔢 गिनती (Numbers)",
            hindiWord = "एक (1)",
            santhaliWord = "ᱢᱤᱫ (Mit' / Mid)",
            santhaliOlChiki = "ᱢᱤᱫ",
            hoWord = "ᱢᱤᱭᱟᱹᱫᱽ (Miyad)",
            hoDevanagari = "मियद",
            mundariWord = "मियाद (Miyad)",
            devanagariPhonetic = "मिद",
            englishMeaning = "One (1)",
            iconEmoji = "1️⃣",
            exampleSentenceHindi = "मेरे पास एक पेंसिल है।",
            exampleSentenceTribal = "ᱤᱧ ᱴᱷᱮᱱ ᱢᱤᱫᱴᱟᱝ ᱯᱮᱱᱥᱤᱞ ᱢᱮᱱᱟᱜ-ᱟ"
        ),
        StudentFlashcard(
            id = "fc_4",
            category = "🔢 गिनती (Numbers)",
            hindiWord = "दो (2)",
            santhaliWord = "ᱵᱟᱨ (Bar / Barya)",
            santhaliOlChiki = "ᱵᱟᱨ",
            hoWord = "ᱵᱟᱹᱨᱭᱟᱹ (Bariya)",
            hoDevanagari = "बरिया",
            mundariWord = "बारिया (Bariya)",
            devanagariPhonetic = "बार",
            englishMeaning = "Two (2)",
            iconEmoji = "2️⃣",
            exampleSentenceHindi = "पेड़ पर दो फल लगे हैं।",
            exampleSentenceTribal = "ᱫᱟᱨᱮ ᱨᱮ ᱵᱟᱨᱭᱟ ᱡᱚ ᱢᱮᱱᱟᱜ-ᱟ"
        ),
        StudentFlashcard(
            id = "fc_5",
            category = "🌳 प्रकृति व जंगल (Nature)",
            hindiWord = "पेड़ (वृक्ष)",
            santhaliWord = "ᱫᱟᱨᱮ (Dare)",
            santhaliOlChiki = "ᱫᱟᱨᱮ",
            hoWord = "ᱫᱟᱨᱩ (Daru)",
            hoDevanagari = "दारू",
            mundariWord = "दारू (Daru)",
            devanagariPhonetic = "दारे",
            englishMeaning = "Tree",
            iconEmoji = "🌳",
            exampleSentenceHindi = "यह साल का पवित्र पेड़ है।",
            exampleSentenceTribal = "ᱱᱚᱣᱟ ᱫᱚ ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱮ ᱠᱟᱱᱟ"
        ),
        StudentFlashcard(
            id = "fc_6",
            category = "🌳 प्रकृति व जंगल (Nature)",
            hindiWord = "जल (पानी)",
            santhaliWord = "ᱫᱟᱜ (Dak')",
            santhaliOlChiki = "ᱫᱟᱜ",
            hoWord = "ᱫᱟᱜ (Dak')",
            hoDevanagari = "दाग",
            mundariWord = "दाः (Dah)",
            devanagariPhonetic = "दाग",
            englishMeaning = "Water",
            iconEmoji = "💧",
            exampleSentenceHindi = "साफ़ पानी पीना चाहिए।",
            exampleSentenceTribal = "ᱯᱷᱟᱨᱪᱟ ᱫᱟᱜ ᱧᱩ ᱫᱚᱨᱠᱟᱨ"
        ),
        StudentFlashcard(
            id = "fc_7",
            category = "🏫 शाला व मित्र (School & Life)",
            hindiWord = "किताब (पुस्तक)",
            santhaliWord = "ᱯᱩᱛᱷᱤ (Puthi)",
            santhaliOlChiki = "ᱯᱩᱛᱷᱤ",
            hoWord = "ᱯᱚᱛᱚᱵ (Potob)",
            hoDevanagari = "पोतोब",
            mundariWord = "पुथी (Puthi)",
            devanagariPhonetic = "पुथी",
            englishMeaning = "Book",
            iconEmoji = "📚",
            exampleSentenceHindi = "हम सब मिलकर किताब पढ़ेंगे।",
            exampleSentenceTribal = "ᱟᱵᱚ ᱡᱚᱛᱚ ᱦᱚᱲ ᱯᱩᱛᱷᱤ ᱵᱚᱱ ᱯᱟᱲᱦᱟᱣ-ᱟ"
        ),
        StudentFlashcard(
            id = "fc_8",
            category = "🏫 शाला व मित्र (School & Life)",
            hindiWord = "दोस्त (मित्र)",
            santhaliWord = "ᱜᱟᱛᱮ (Gate)",
            santhaliOlChiki = "ᱜᱟᱛᱮ",
            hoWord = "ᱡᱩᱲᱤ (Juri)",
            hoDevanagari = "जुड़ी",
            mundariWord = "संगाती (Sangati)",
            devanagariPhonetic = "गाते",
            englishMeaning = "Friend",
            iconEmoji = "🤝",
            exampleSentenceHindi = "हम सब अच्छे मित्र हैं।",
            exampleSentenceTribal = "ᱟᱵᱚ ᱡᱚᱛᱚ ᱵᱷᱟᱹᱜᱤ ᱜᱟᱛᱮ ᱠᱟᱱᱟ ᱵᱚᱱ"
        )
    )

    val defaultClassroomQuickPhrases = listOf(
        ClassroomQuickPhrase(
            id = "cqp_1",
            hindiText = "बच्चों, सब अपनी जगह पर बैठ जाओ",
            santhaliOlChiki = "ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ, ᱡᱚᱛᱚ ᱦᱚᱲ ᱫᱩᱲᱩᱵ ᱯᱮ",
            santhaliPhonetic = "गिदरा को, जोतो होड़ दुड़ुब पे",
            hoText = "ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ, ᱡᱚᱛᱚ ᱠᱚ ᱫᱩᱵ ᱯᱮ",
            hoPhonetic = "गिदरा को, जोतो को दुब पे",
            mundariText = "गिदरा को, जोतो को दुब पे",
            category = "INSTRUCTION",
            iconEmoji = "🪑"
        ),
        ClassroomQuickPhrase(
            id = "cqp_2",
            hindiText = "अपनी-अपनी किताबें और स्लेट खोलो",
            santhaliOlChiki = "ᱟᱯᱮᱭᱟᱜ ᱯᱚᱛᱚᱵ ᱟᱨ ᱥᱞᱮᱴ ᱠᱷᱩᱞᱟᱹᱣ ᱯᱮ",
            santhaliPhonetic = "आपेयाग पोतोब आर स्लेट खुलाउ पे",
            hoText = "ᱟᱯᱮᱭᱟᱜ ᱯᱚᱛᱚᱵ ᱟᱨ ᱥᱞᱮᱴ ᱩᱛᱷᱟᱹᱣ ᱯᱮ",
            hoPhonetic = "आपेयाग पोतोब आर स्लेट उथाउ पे",
            mundariText = "आपेयाग पुथी आर स्लेट खुलाउ पे",
            category = "INSTRUCTION",
            iconEmoji = "📖"
        ),
        ClassroomQuickPhrase(
            id = "cqp_3",
            hindiText = "भोजन से पहले साबुन से हाथ धो लो",
            santhaliOlChiki = "ᱡᱚᱢ ᱞᱟᱦᱟ ᱨᱮ ᱥᱟᱵᱚᱱ ᱛᱮ ᱛᱤ ᱟᱹᱨᱩᱵ ᱯᱮ",
            santhaliPhonetic = "जोम लाहा रे साबोन ते ती आरुब पे",
            hoText = "ᱡᱚᱢ ᱢᱟᱲᱟᱝ ᱨᱮ ᱥᱟᱵᱚᱱ ᱛᱮ ᱛᱤ ᱟᱹᱨᱩᱵ ᱯᱮ",
            hoPhonetic = "जोम माड़ांग रे साबोन ते ती आरुब पे",
            mundariText = "मांडी जोम सिदा रे साबुन ते ती आरुब पे",
            category = "HYGIENE",
            iconEmoji = "🧼"
        ),
        ClassroomQuickPhrase(
            id = "cqp_4",
            hindiText = "शाबाश! बहुत ही सुंदर काम किया",
            santhaliOlChiki = "ᱟᱹᱰᱤ ᱱᱟᱯᱟᱭ! ᱟᱹᱰᱤ ᱵᱷᱟᱹᱜᱤ ᱠᱟᱹᱢᱤ ᱠᱮᱫᱟ ᱯᱮ",
            santhaliPhonetic = "आडी नापाय! आडी भागी कामी केदा पे",
            hoText = "ᱵᱮᱥ ᱜᱮ! ᱟᱹᱰᱤ ᱵᱩᱜᱤᱱ ᱠᱟᱹᱢᱤ ᱠᱮᱫᱟ ᱯᱮ",
            hoPhonetic = "बेस गे! आडी बुगिन कामी केदा पे",
            mundariText = "बुगिन गे! आडी बेस कामी केदा पे",
            category = "PRAISE",
            iconEmoji = "👏"
        ),
        ClassroomQuickPhrase(
            id = "cqp_5",
            hindiText = "सब मिलकर ध्यान से सुनो",
            santhaliOlChiki = "ᱡᱚᱛᱚ ᱦᱚᱲ ᱢᱤᱫ ᱛᱮ ᱫᱷᱮᱭᱟᱱ ᱛᱮ ᱟᱧᱡᱚᱢ ᱯᱮ",
            santhaliPhonetic = "जोतो होड़ मिद ते धेयान ते आंजोम पे",
            hoText = "ᱡᱚᱛᱚ ᱠᱚ ᱢᱤᱫ ᱛᱮ ᱫᱷᱮᱭᱟᱱ ᱛᱮ ᱟᱧᱡᱚᱢ ᱯᱮ",
            hoPhonetic = "जोतो को मिद ते धेयान ते आंजोम पे",
            mundariText = "जोतो को मिद ते ध्यान ते आंजोम पे",
            category = "INSTRUCTION",
            iconEmoji = "👂"
        ),
        ClassroomQuickPhrase(
            id = "cqp_6",
            hindiText = "कल घर से गृहकार्य पूरा करके आना",
            santhaliOlChiki = "ᱜᱟᱯᱟ ᱚᱲᱟᱜ ᱠᱷᱚᱱ ᱠᱟᱹᱢᱤ ᱯᱩᱨᱟᱹᱣ ᱠᱟᱛᱮ ᱦᱤᱡᱩᱜ ᱯᱮ",
            santhaliPhonetic = "गापा ओड़ाग खोन कामी पुराव काते हिजुग पे",
            hoText = "ᱜᱟᱯᱟ ᱚᱲᱟᱜ ᱮᱛᱮ ᱠᱟᱹᱢᱤ ᱠᱟᱛᱮ ᱦᱤᱡᱩᱜ ᱯᱮ",
            hoPhonetic = "गापा ओड़ाग एते कामी काते हिजुग पे",
            mundariText = "गापा ओड़ाः एते कामी पूरा केते हिजुग पे",
            category = "ROUTINE",
            iconEmoji = "🏠"
        ),
        ClassroomQuickPhrase(
            id = "cqp_7",
            hindiText = "कक्षा में शांति बनाए रखो",
            santhaliOlChiki = "ᱠᱞᱟᱥ ᱨᱮ ᱛᱷᱤᱨ ᱛᱟᱦᱮᱸᱱ ᱯᱮ",
            santhaliPhonetic = "क्लास रे थीर ताहेन पे",
            hoText = "ᱠᱞᱟᱥ ᱨᱮ ᱛᱷᱤᱨ ᱛᱟᱭᱠᱮᱱ ᱯᱮ",
            hoPhonetic = "क्लास रे थीर तायकेन पे",
            mundariText = "क्लास रे थीर ताएन पे",
            category = "INSTRUCTION",
            iconEmoji = "🤫"
        ),
        ClassroomQuickPhrase(
            id = "cqp_8",
            hindiText = "जो बोलना चाहता है, हाथ उठाओ",
            santhaliOlChiki = "ᱚᱠᱚᱭ ᱨᱚᱲ ᱥᱟᱱᱟᱭᱮ ᱠᱟᱱᱟ, ᱛᱤ ᱛᱩᱞ ᱯᱮ",
            santhaliPhonetic = "ओकोय रोड़ सानाये काना, ती तुल पे",
            hoText = "ᱡᱮ ᱠᱟᱡᱤ ᱥᱟᱱᱟᱭᱮ ᱛᱟᱱᱟ, ᱛᱤ ᱛᱩᱞ ᱯᱮ",
            hoPhonetic = "जे काजी सानाये ताना, ती तुल पे",
            mundariText = "अकोय काजी सानाई तना, ती तुल पे",
            category = "INSTRUCTION",
            iconEmoji = "✋"
        )
    )
}

