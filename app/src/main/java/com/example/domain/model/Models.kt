package com.example.domain.model

import org.json.JSONArray
import org.json.JSONObject

enum class TargetLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val scriptName: String,
    val region: String
) {
    SANTHALI("sat", "Santhali", "ᱥᱟᱱᱛᱟᱲᱤ", "Ol Chiki & Devanagari", "Santhal Pargana, Dumka, East Singhbhum"),
    HO("hoc", "Ho", "ᱦᱳ", "Warang Chiti & Devanagari", "Kolhan, West Singhbhum, Chaibasa"),
    MUNDARI("unr", "Mundari", "मुण्डारी", "Devanagari & Nag Mundari", "Ranchi, Khunti, Gumla, Simdega")
}

enum class GradeLevel(val label: String, val flnStage: String) {
    GRADE_1("Grade 1 (बालवाटिका / कक्षा 1)", "Foundational - Letter & Number Sense"),
    GRADE_2("Grade 2 (कक्षा 2)", "Foundational - Word Building & Basic Math"),
    GRADE_3("Grade 3 (कक्षा 3)", "Preparatory - Reading Fluency & EVS"),
    GRADE_4("Grade 4 (कक्षा 4)", "Preparatory - Applied Concepts & Local Science"),
    GRADE_5("Grade 5 (कक्षा 5)", "Middle - Regional Heritage & Advanced FLN")
}

enum class SubjectArea(val titleHindi: String, val iconName: String) {
    HINDI_FLN("हिन्दी भाषा व साक्षरता (Language)", "menu_book"),
    MATH_NUMERACY("गणित व संख्या ज्ञान (Numeracy)", "calculate"),
    EVS_ENVIRONMENT("पर्यावरण व प्रकृति (EVS)", "forest"),
    TRIBAL_HERITAGE("संस्कृति, कला व कहानियां (Heritage)", "auto_stories")
}

enum class LessonStatus {
    DRAFT,
    GENERATING,
    REVIEW_REQUIRED,
    APPROVED,
    PUBLISHED
}

enum class SyncState {
    SYNCED,
    PENDING_OUTBOX,
    SENDING,
    CONFLICT,
    OFFLINE_ONLY
}

data class PedagogicalAdaptation(
    val hindiCoreConcept: String,
    val targetTranslation: String,
    val nativeScript: String,
    val transliteration: String,
    val gradeAppropriateExplanation: String,
    val localCulturalAnalogy: String,
    val classroomActivityPrompt: String,
    val formativeQuestions: List<String>,
    val pronunciationGuide: String,
    val groundingScore: Float = 0.94f,
    val estimatedLatencyMs: Long = 1200L,
    val modelUsed: String = "gemini-3.1-pro-preview"
)

data class WorksheetQuestion(
    val id: String,
    val questionHindi: String,
    val questionTarget: String,
    val type: String, // MCQ, MATCH, TRUE_FALSE, ORAL_PRACTICE
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val localContextHint: String
) {
    fun toJson(): String {
        fun escape(s: String): String {
            return buildString {
                for (ch in s) {
                    when (ch) {
                        '\\' -> append("\\\\")
                        '\"' -> append("\\\"")
                        '\b' -> append("\\b")
                        '\u000C' -> append("\\f")
                        '\n' -> append("\\n")
                        '\r' -> append("\\r")
                        '\t' -> append("\\t")
                        else -> append(ch)
                    }
                }
            }
        }
        val optsJson = options.joinToString(prefix = "[", postfix = "]") { "\"${escape(it)}\"" }
        return """{"id":"${escape(id)}","questionHindi":"${escape(questionHindi)}","questionTarget":"${escape(questionTarget)}","type":"${escape(type)}","options":$optsJson,"correctAnswer":"${escape(correctAnswer)}","localContextHint":"${escape(localContextHint)}"}"""
    }
}

fun List<WorksheetQuestion>.toJsonString(): String =
    joinToString(prefix = "[\n", postfix = "\n]", separator = ",\n") { it.toJson() }

fun parseWorksheetQuestionsJson(jsonString: String): List<WorksheetQuestion> {
    if (jsonString.isBlank()) return emptyList()
    try {
        val arr = JSONArray(jsonString)
        val list = mutableListOf<WorksheetQuestion>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val optArr = obj.optJSONArray("options")
            val optionsList = mutableListOf<String>()
            if (optArr != null) {
                for (j in 0 until optArr.length()) {
                    optionsList.add(optArr.getString(j))
                }
            }
            list.add(
                WorksheetQuestion(
                    id = obj.optString("id", "q_${i + 1}"),
                    questionHindi = obj.optString("questionHindi", ""),
                    questionTarget = obj.optString("questionTarget", ""),
                    type = obj.optString("type", "MCQ"),
                    options = optionsList,
                    correctAnswer = obj.optString("correctAnswer", ""),
                    localContextHint = obj.optString("localContextHint", "")
                )
            )
        }
        if (list.isNotEmpty()) return list
    } catch (t: Throwable) {
        // Fallback for environments where org.json is stubbed or fails
    }

    return parseWorksheetQuestionsFallback(jsonString)
}

fun parseWorksheetQuestionsFallback(json: String): List<WorksheetQuestion> {
    val results = mutableListOf<WorksheetQuestion>()
    val objRegex = Regex("""\{([^{}]+)\}""")
    fun extractField(body: String, name: String): String {
        val fieldRegex = Regex(""""$name"\s*:\s*"([^"\\]*(?:\\.[^"\\]*)*)"""")
        val match = fieldRegex.find(body) ?: return ""
        return match.groupValues[1]
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
    }
    fun extractOptions(body: String): List<String> {
        val optRegex = Regex(""""options"\s*:\s*\[(.*?)\]""", RegexOption.DOT_MATCHES_ALL)
        val match = optRegex.find(body) ?: return emptyList()
        val inside = match.groupValues[1]
        val itemRegex = Regex(""""([^"\\]*(?:\\.[^"\\]*)*)"""")
        return itemRegex.findAll(inside).map {
            it.groupValues[1].replace("\\\"", "\"").replace("\\\\", "\\")
        }.toList()
    }

    for (match in objRegex.findAll(json)) {
        val body = match.groupValues[1]
        val id = extractField(body, "id")
        val qHindi = extractField(body, "questionHindi")
        val qTarget = extractField(body, "questionTarget")
        val type = extractField(body, "type").ifBlank { "MCQ" }
        val options = extractOptions(body)
        val answer = extractField(body, "correctAnswer")
        val hint = extractField(body, "localContextHint")
        if (qHindi.isNotBlank() || qTarget.isNotBlank()) {
            results.add(
                WorksheetQuestion(
                    id = id.ifBlank { "q_${results.size + 1}" },
                    questionHindi = qHindi,
                    questionTarget = qTarget,
                    type = type,
                    options = options,
                    correctAnswer = answer,
                    localContextHint = hint
                )
            )
        }
    }
    return results
}

data class CurriculumFilterParams(
    val query: String = "",
    val language: String? = null,
    val grade: String? = null,
    val subject: String? = null
)

data class Flashcard(
    val id: String,
    val hindiWord: String,
    val targetWord: String,
    val transliteration: String,
    val scriptVisual: String,
    val category: String, // Animals, Nature, Numbers, Daily Life, Family
    val culturalNote: String,
    val imageUrl: String? = null
)

enum class VoiceSpeakerRole(val displayName: String, val iconEmoji: String) {
    TEACHER("शिक्षक (Teacher: Hindi → Tribal)", "👨‍🏫"),
    STUDENT("विद्यार्थी (Student: Tribal → Hindi)", "🧒")
}

data class VoiceTurn(
    val id: String,
    val isTeacher: Boolean = true,
    val speakerRole: VoiceSpeakerRole = if (isTeacher) VoiceSpeakerRole.TEACHER else VoiceSpeakerRole.STUDENT,
    val hindiText: String,
    val targetText: String,
    val scriptText: String,
    val transliteration: String,
    val transliterationDevanagari: String = "",
    val phoneticSyllables: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val latencyMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

enum class VoiceTimbre(
    val displayNameHindi: String,
    val descriptionHindi: String,
    val defaultPitch: Float,
    val defaultSpeed: Float,
    val defaultRelayPauseMs: Long
) {
    CLEAR_EDUCATIONAL(
        displayNameHindi = "स्पष्ट शैक्षिक (Standard)",
        descriptionHindi = "कक्षा शिक्षण के लिए मानक स्पष्ट उच्चारण",
        defaultPitch = 1.00f,
        defaultSpeed = 0.92f,
        defaultRelayPauseMs = 450L
    ),
    WARM_TEACHER(
        displayNameHindi = "स्नेही शिक्षक (Warm)",
        descriptionHindi = "छोटे बच्चों के लिए धैर्यपूर्ण और मधुर स्वर",
        defaultPitch = 0.96f,
        defaultSpeed = 0.88f,
        defaultRelayPauseMs = 500L
    ),
    EXPRESSIVE_STORYTELLER(
        displayNameHindi = "कथावाचक (Storyteller)",
        descriptionHindi = "लोककथाओं और कहानियों के लिए समृद्ध हाव-भाव",
        defaultPitch = 1.08f,
        defaultSpeed = 0.82f,
        defaultRelayPauseMs = 600L
    ),
    YOUNG_STUDENT(
        displayNameHindi = "बाल सहपाठी (Student)",
        descriptionHindi = "साथी विद्यार्थियों के साथ संवाद हेतु चंचल स्वर",
        defaultPitch = 1.20f,
        defaultSpeed = 0.95f,
        defaultRelayPauseMs = 350L
    )
}

enum class VoiceGender(
    val displayNameHindi: String,
    val displayNameEnglish: String,
    val icon: String,
    val pitchMultiplier: Float
) {
    FEMALE("महिला", "Female", "👩", 1.15f),
    MALE("पुरुष", "Male", "👨", 0.85f)
}

data class VoiceSettings(
    val speechRate: Float = 0.92f,
    val pitch: Float = 1.0f,
    val timbre: VoiceTimbre = VoiceTimbre.CLEAR_EDUCATIONAL,
    val voiceGender: VoiceGender = VoiceGender.FEMALE,
    val relayPauseMs: Long = 450L,
    val isBilingualRelayEnabled: Boolean = false,
    val isSlowClassroomMode: Boolean = false,
    val autoPlayOnTranslate: Boolean = true,
    val isTwoWayDialogueMode: Boolean = false,
    val activeSpeakerRole: VoiceSpeakerRole = VoiceSpeakerRole.TEACHER,
    val enablePhoneticSyllables: Boolean = true
)

data class PracticeQuizQuestion(
    val id: String,
    val questionHindi: String,
    val questionTarget: String,
    val targetLanguage: TargetLanguage,
    val options: List<String>,
    val correctIndex: Int,
    val explanationHindi: String,
    val audioUtterance: String
)

data class StudentAssessment(
    val attemptId: String,
    val studentName: String,
    val rollNo: String,
    val grade: String,
    val lessonTitle: String,
    val score: Int,
    val maxScore: Int,
    val timeSpentSeconds: Int,
    val dateString: String,
    val syncState: SyncState = SyncState.PENDING_OUTBOX
)

data class TraceItem(
    val prdId: String,
    val tadId: String,
    val sadId: String,
    val fsdId: String,
    val title: String,
    val status: String,
    val latencyTarget: String,
    val verifiedLayer: String
)

enum class ChatPersonaRole(
    val id: String,
    val titleHindi: String,
    val subtitle: String,
    val iconName: String,
    val systemInstruction: String
) {
    MTB_MLE_PEDAGOGY(
        id = "mtb_mle",
        titleHindi = "MTB-MLE शिक्षा विशेषज्ञ (Pedagogical Mentor)",
        subtitle = "मातृभाषा शिक्षण, बाल-केंद्रित शिक्षा शास्त्र व बहुभाषी कक्षा रणनीति",
        iconName = "school",
        systemInstruction = "You are BhashaSetu AI's Senior MTB-MLE (Mother-Tongue-Based Multilingual Education) Pedagogical Specialist for primary schools in Jharkhand, India. You assist non-tribal Hindi-speaking teachers in scaffolding, adapting, and structuring FLN (Foundational Literacy and Numeracy) lesson plans and interactive activities into Santhali (Ol Chiki), Mundari, and Ho. Provide step-by-step guidance, cultural context (Sarhul, Sohrai, Karam, local nature), and bilingual teaching strategies."
    ),
    TRIBAL_LINGUIST(
        id = "linguist",
        titleHindi = "आदिवासी भाषा व व्याकरण गुरु (Native Language Tutor)",
        subtitle = "संथाली (ओल चिकी), हो (वारंग क्षिति), मुण्डारी शब्दावली, उच्चारण व लिपि",
        iconName = "translate",
        systemInstruction = "You are an expert tribal linguist and native language tutor specializing in Santhali (Ol Chiki script), Ho (Warang Chiti / Devanagari), and Mundari. You explain vocabulary origins, phonetic pronunciations, grammatical particles, and accurate Latin/Devanagari transliterations to help teachers communicate effectively."
    ),
    NIPUN_LESSON_PLANNER(
        id = "nipun_planner",
        titleHindi = "NIPUN भारत FLN योजनाकार (FLN Lesson Planner)",
        subtitle = "कक्षा 1-3 बुनियादी साक्षरता, खेल-गतिविधियां, कार्यपत्रक व मौखिक मूल्यांकन",
        iconName = "auto_stories",
        systemInstruction = "You are a NIPUN Bharat Foundational Literacy and Numeracy (FLN) Curriculum Architect for grades 1-3 in Jharkhand. You create structured 45-minute lesson plans, interactive circle-time activities, rhymes, and oral assessment rubrics aligned with JCERT / NCERT standards, bridging Hindi to local tribal languages."
    )
}

enum class GeminiModelChoice(
    val modelId: String,
    val label: String,
    val descriptionHindi: String,
    val category: String
) {
    GEMINI_3_1_PRO(
        modelId = "gemini-3.1-pro-preview",
        label = "Gemini 3.1 Pro",
        descriptionHindi = "गहन विश्लेषण, जटिल शिक्षा शास्त्र व विस्तृत पाठ योजना (Complex Tasks)",
        category = "Complex"
    ),
    GEMINI_3_5_FLASH(
        modelId = "gemini-3.5-flash",
        label = "Gemini 3.5 Flash",
        descriptionHindi = "सामान्य संवाद, तीव्र बहुभाषी अनुवाद व गूगल सर्च ग्राउंडिंग (General Tasks)",
        category = "General"
    ),
    GEMINI_3_1_FLASH_LITE(
        modelId = "gemini-3.1-flash-lite-preview",
        label = "Gemini 3.1 Flash Lite",
        descriptionHindi = "अति-त्वरित शब्दावली खोज व वास्तविक-समय वाक्य सुझाव (Fast Tasks)",
        category = "Fast"
    )
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String? = null,
    val personaRole: ChatPersonaRole? = null,
    val searchGrounded: Boolean = false,
    val sources: List<String> = emptyList(),
    val isError: Boolean = false
)

data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
    val role: String = "प्राथमिक शिक्षक (Primary Teacher)",
    val schoolName: String = "राजकीय उत्क्रमित प्राथमिक विद्यालय, खूंटी",
    val district: String = "खूंटी (Khunti), झारखंड",
    val isAuthenticated: Boolean = true,
    val firestoreSynced: Boolean = true
)

enum class RagMatchType(val labelHindi: String, val badgeColorHex: Long) {
    HYBRID_EMBEDDING_BM25("हाइब्रिड (वेक्टर + BM25)", 0xFF0D9488),
    DENSE_VECTOR_COSINE("सघन वेक्टर साम्यता (Cosine Similarity)", 0xFF6366F1),
    LEXICAL_BM25("शाब्दिक मिलान (BM25 Lexical)", 0xFFF59E0B),
    OUTCOME_CODE_EXACT("पाठ्यक्रम कोड प्रत्यक्ष मिलान (JCERT FLN Code)", 0xFF10B981)
}

data class RagCurriculumMatch(
    val chunk: com.example.data.local.CurriculumContentEntity,
    val similarityScore: Float, // 0.0f to 1.0f
    val denseCosineScore: Float,
    val bm25LexicalScore: Float,
    val matchType: RagMatchType,
    val matchedKeywords: List<String>,
    val relevanceExplanation: String
)

data class RagQueryContext(
    val query: String,
    val topMatches: List<RagCurriculumMatch>,
    val primaryGroundedChunk: com.example.data.local.CurriculumContentEntity?,
    val formattedPromptContext: String,
    val retrievalLatencyMs: Long
)

// --- Full-Stack Tech Stack & Workflow Architecture Models ---

data class TechStackTier(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String, // CLIENT, BACKEND, AI_ML, DATA_INFRA
    val primaryTech: String,
    val version: String,
    val keyLibraries: List<String>,
    val protocols: List<String>,
    val responsibilities: List<String>,
    val slaOrLatency: String,
    val hardwareOrHosting: String,
    val offlineCapabilities: String,
    val iconEmoji: String
)

data class MonorepoNode(
    val path: String,
    val name: String,
    val type: String, // APP, SERVICE, PACKAGE, INFRA, DOCS
    val techStack: String,
    val purpose: String,
    val exposedContracts: List<String>,
    val sampleCodeSnippet: String
)

data class DiskAnnComparison(
    val metricName: String,
    val diskAnnValue: String,
    val hnswValue: String,
    val deltaAdvantage: String,
    val isAdvantage: Boolean = true
)

data class MqmErrorSpan(
    val tokenOrSpan: String,
    val severity: String, // CRITICAL, MAJOR, MINOR, NONE
    val category: String, // MISTRANSLATION, OMISSION, TERMINOLOGY, UNTRANSLATED, FLUENCY
    val startIndex: Int,
    val endIndex: Int,
    val suggestedFix: String
)

data class CometQualityEstimation(
    val cometScore: Float, // 0.0 to 1.0
    val xcometConfidence: Float,
    val confidenceTier: String, // HIGH, MEDIUM, LOW
    val actionDecision: String, // AUTO_PUBLISH, TEACHER_REVIEW_REQUIRED, RETRY_ESCALATE
    val sourceHindi: String,
    val translatedTribal: String,
    val targetLanguage: TargetLanguage,
    val detectedErrorSpans: List<MqmErrorSpan>,
    val explanation: String,
    val evaluationLatencyMs: Long = 180L
)

data class OfflineTabletState(
    val tabletId: String,
    val schoolCode: String,
    val localDbEngine: String, // SQLite 3 (Room / Drift)
    val ramAvailableMb: Int, // e.g. 1950 MB
    val syncCursorPosition: Long,
    val pendingOutboxMutations: Int,
    val lastSyncTimestamp: Long,
    val retryBackoffSeconds: Int,
    val isNetworkAvailable: Boolean
)

enum class AppUserMode(
    val titleHindi: String,
    val subtitleHindi: String,
    val shortLabel: String,
    val iconEmoji: String,
    val colorHex: Long
) {
    TEACHER(
        titleHindi = "शिक्षक मोड (Teacher)",
        subtitleHindi = "पाठ निर्माण, लाइव अनुवाद, कार्यपत्रक व NIPUN FLN",
        shortLabel = "शिक्षक",
        iconEmoji = "👩‍🏫",
        colorHex = 0xFF4A3428
    ),
    STUDENT(
        titleHindi = "बाल संसार (Student)",
        subtitleHindi = "चित्र पहेली, सचित्र शब्द कार्ड, खेल-खेल में सीखें",
        shortLabel = "बाल संसार",
        iconEmoji = "🎒",
        colorHex = 0xFFD97706
    ),
    COMMUNITY(
        titleHindi = "सांस्कृतिक मंच (Community)",
        subtitleHindi = "जनजातीय लोककथाएं, शब्दकोश व सांस्कृतिक धरोहर",
        shortLabel = "समुदाय",
        iconEmoji = "🏡",
        colorHex = 0xFF059669
    )
}

data class StudentFlashcard(
    val id: String,
    val category: String,
    val hindiWord: String,
    val santhaliWord: String,
    val santhaliOlChiki: String,
    val hoWord: String,
    val hoDevanagari: String,
    val mundariWord: String,
    val devanagariPhonetic: String,
    val englishMeaning: String,
    val iconEmoji: String,
    val exampleSentenceHindi: String,
    val exampleSentenceTribal: String
)

data class ClassroomQuickPhrase(
    val id: String,
    val hindiText: String,
    val santhaliOlChiki: String,
    val santhaliPhonetic: String,
    val hoText: String,
    val hoPhonetic: String,
    val mundariText: String,
    val category: String, // INSTRUCTION, PRAISE, HYGIENE, ROUTINE
    val iconEmoji: String
)

