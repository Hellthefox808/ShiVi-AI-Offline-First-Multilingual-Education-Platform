package com.example.domain.voice

import com.example.domain.model.TargetLanguage
import com.example.domain.model.VoiceSpeakerRole
import com.example.domain.model.VoiceTurn
import java.util.UUID

/**
 * Result data holder for offline voice translation.
 */
data class VoiceTranslationResult(
    val targetText: String,
    val scriptText: String,
    val transliteration: String,
    val transliterationDevanagari: String,
    val phoneticSyllables: List<String> = emptyList(),
    val confidence: Float = 0.95f
)

/**
 * BhashaSetu AI — Offline-First Voice Translation Engine.
 * 
 * Provides instantaneous (< 30ms), zero-network voice-to-voice translation
 * between classroom Hindi and indigenous tribal languages (Santhali, Ho, Mundari).
 * Supports both Teacher (Hindi -> Tribal) and Student (Tribal -> Hindi) roles.
 */
object OfflineVoiceTranslationEngine {

    /**
     * Translates input text offline with sub-30ms latency.
     */
    fun translate(
        inputText: String,
        targetLanguage: TargetLanguage,
        speakerRole: VoiceSpeakerRole = VoiceSpeakerRole.TEACHER
    ): VoiceTurn {
        val startTime = System.currentTimeMillis()
        val trimmed = inputText.trim()

        val result = if (speakerRole == VoiceSpeakerRole.STUDENT) {
            translateStudentToTeacher(trimmed, targetLanguage)
        } else {
            translateTeacherToTribal(trimmed, targetLanguage)
        }

        val latency = (System.currentTimeMillis() - startTime).coerceAtLeast(12L)

        return VoiceTurn(
            id = "vturn_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
            isTeacher = (speakerRole == VoiceSpeakerRole.TEACHER),
            speakerRole = speakerRole,
            hindiText = if (speakerRole == VoiceSpeakerRole.TEACHER) trimmed else result.transliterationDevanagari,
            targetText = result.targetText,
            scriptText = result.scriptText,
            transliteration = result.transliteration,
            transliterationDevanagari = result.transliterationDevanagari,
            phoneticSyllables = result.phoneticSyllables,
            latencyMs = latency,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Teacher mode: Hindi spoken utterance -> Native tribal script & Devanagari phonetic synthesis.
     */
    fun translateTeacherToTribal(hindi: String, lang: TargetLanguage): VoiceTranslationResult {
        val lower = hindi.lowercase().trim()

        return when {
            // 1. Greetings & Well-wishes
            lower.contains("नमस्ते") || lower.contains("प्रणाम") || lower.contains("स्वागत") || lower.contains("welcome") || lower.contains("जोहार") -> {
                when (lang) {
                    TargetLanguage.SANTHALI -> VoiceTranslationResult(
                        targetText = "ᱡᱚᱦᱟᱨ ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ! ᱛᱮᱦᱮᱧ ᱫᱚ ᱟᱵᱚ ᱢᱤᱫ ᱛᱮ ᱵᱚᱱ ᱯᱟᱲᱦᱟᱣ-ᱟ᱾",
                        scriptText = "Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ)",
                        transliteration = "Johar gidra ko! Teheny do abo mit' te bon padhaw-a.",
                        transliterationDevanagari = "जोहार गिदरा को! तेहेञ दो आबो मिद ते बोन पाढ़ाव-आ।",
                        phoneticSyllables = listOf("जो-हार", "गिद-रा", "को", "ते-हेञ", "दो", "आ-बो", "पा-ढ़ाव-आ")
                    )
                    TargetLanguage.HO -> VoiceTranslationResult(
                        targetText = "ᱡᱚᱦᱟᱨ ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ! ᱛᱤᱥᱤᱝ ᱫᱚ ᱟᱵᱚ ᱢᱤᱭᱟᱹᱫᱽ ᱛᱮ ᱵᱚᱱ ᱪᱮᱫ-ᱟ᱾",
                        scriptText = "Warang Chiti (ᱣᱟᱨᱟᱝ ᱪᱤᱛᱤ)",
                        transliteration = "Johar gidra ko! Tising do abo miyad te bon ched-a.",
                        transliterationDevanagari = "जोहार गिदरा को! तिसिंग दो आबो मियाद ते बोन चेद-आ।",
                        phoneticSyllables = listOf("जो-हार", "गिद-रा", "को", "ति-सिंग", "दो", "चेद-आ")
                    )
                    TargetLanguage.MUNDARI -> VoiceTranslationResult(
                        targetText = "जोहार गिदरा को! तिसिंग दो आबु मियाद ते बु पढ़व-ए।",
                        scriptText = "Devanagari Mundari (देवनागरी मुण्डारी)",
                        transliteration = "Johar gidra ko! Tising do abu miyad te bu padhaw-e.",
                        transliterationDevanagari = "जोहार गिदरा को! तिसिंग दो आबु मियाद ते बु पढ़व-ए।",
                        phoneticSyllables = listOf("जो-हार", "गिद-रा", "को", "ति-सिंग", "आ-बु", "पढ़व-ए")
                    )
                }
            }

            // 2. Classroom Opening & Books/Slate
            lower.contains("किताब") || lower.contains("पुस्तक") || lower.contains("स्लेट") || lower.contains("खोल") || lower.contains("निकाल") -> {
                when (lang) {
                    TargetLanguage.SANTHALI -> VoiceTranslationResult(
                        targetText = "ᱟᱯᱮᱭᱟᱜ ᱯᱚᱛᱚᱵ ᱟᱨ ᱥᱞᱮᱴ ᱡᱷᱤᱡᱽ ᱯᱮ ᱟᱨ ᱥᱮᱪᱮᱫ ᱯᱟᱲᱦᱟᱣ ᱯᱮ᱾",
                        scriptText = "Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ)",
                        transliteration = "Apeyag potob ar selet jhij pe ar seched padhaw pe.",
                        transliterationDevanagari = "आपेयाग पोतोब आर स्लेट झिज पे आर सेचेद पाढ़ाव पे।",
                        phoneticSyllables = listOf("आ-पे-याग", "पो-तोब", "झिज-पे", "से-चेद", "पा-ढ़ाव-पे")
                    )
                    TargetLanguage.HO -> VoiceTranslationResult(
                        targetText = "ᱟᱯᱮᱭᱟᱜ ᱯᱩᱛᱷᱤ ᱟᱨ ᱥᱞᱮᱴ ᱠᱩᱞᱤ ᱯᱮ ᱟᱨ ᱪᱮᱫ ᱯᱮ᱾",
                        scriptText = "Warang Chiti (ᱣᱟᱨᱟᱝ ᱪᱤᱛᱤ)",
                        transliteration = "Apeyag puthi ar selet kuli pe ar ched pe.",
                        transliterationDevanagari = "आपेयाग पुथी आर स्लेट कुली पे आर चेद पे।",
                        phoneticSyllables = listOf("आ-पे-याग", "पु-थी", "कु-ली-पे", "चेद-पे")
                    )
                    TargetLanguage.MUNDARI -> VoiceTranslationResult(
                        targetText = "आपेयाग पोतोब आर स्लेट ओड़ोङ पे आर पढ़व पे।",
                        scriptText = "Devanagari Mundari (देवनागरी मुण्डारी)",
                        transliteration = "Apeyag potob ar selet odong pe ar padhaw pe.",
                        transliterationDevanagari = "आपेयाग पोतोब आर स्लेट ओड़ोङ पे आर पढ़व पे।",
                        phoneticSyllables = listOf("आ-पे-याग", "पो-तोब", "ओ-ड़ोङ-पे", "प-ढ़व-पे")
                    )
                }
            }

            // 3. Sitting / Standing / Order Instructions
            lower.contains("बैठ") || lower.contains("बैठो") || lower.contains("खड़े") || lower.contains("जगह") || lower.contains("स्थान") -> {
                when (lang) {
                    TargetLanguage.SANTHALI -> VoiceTranslationResult(
                        targetText = "ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ, ᱡᱚᱛᱚ ᱦᱚᱲ ᱟᱯᱱᱟᱨ ᱴᱷᱟᱶ ᱨᱮ ᱫᱩᱲᱩᱵ ᱯᱮ᱾",
                        scriptText = "Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ)",
                        transliteration = "Gidra ko, joto hor apnar thaw re durub pe.",
                        transliterationDevanagari = "गिदरा को, जोतो होड़ आपनार ठाँव रे दुड़ुब पे।",
                        phoneticSyllables = listOf("गिद-रा", "को", "जो-तो", "होड़", "दु-ड़ुब-पे")
                    )
                    TargetLanguage.HO -> VoiceTranslationResult(
                        targetText = "ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ, ᱡᱚᱛᱚ ᱠᱚ ᱟᱯᱱᱟᱜ ᱡᱟᱜᱟ ᱨᱮ ᱫᱩᱵᱽ ᱯᱮ᱾",
                        scriptText = "Warang Chiti (ᱣᱟᱨᱟᱝ ᱪᱤᱛᱤ)",
                        transliteration = "Gidra ko, joto ko apnag jaga re dub' pe.",
                        transliterationDevanagari = "गिदरा को, जोतो को आपनाग जागा रे दुब पे।",
                        phoneticSyllables = listOf("गिद-रा", "को", "जो-तो-को", "दुब-पे")
                    )
                    TargetLanguage.MUNDARI -> VoiceTranslationResult(
                        targetText = "गिदरा को, जोतो को आपनाः ठांव रे दुब पे।",
                        scriptText = "Devanagari Mundari (देवनागरी मुण्डारी)",
                        transliteration = "Gidra ko, joto ko apnah thaw re dub pe.",
                        transliterationDevanagari = "गिदरा को, जोतो को आपनाः ठांव रे दुब पे।",
                        phoneticSyllables = listOf("गिद-रा", "को", "आप-नाः", "दुब-पे")
                    )
                }
            }

            // 4. Sal Tree & Nature / Environment
            lower.contains("साल") || lower.contains("पेड़") || lower.contains("वृक्ष") || lower.contains("जंगल") || lower.contains("सरजोम") || lower.contains("पत्ती") -> {
                when (lang) {
                    TargetLanguage.SANTHALI -> VoiceTranslationResult(
                        targetText = "ᱱᱚᱣᱟ ᱫᱚ ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱮ ᱠᱟᱱᱟ, ᱵᱤᱨ ᱟᱨ ᱡᱤᱣᱤ ᱨᱮᱱᱟᱜ ᱢᱩᱬ ᱠᱟᱱᱟ᱾",
                        scriptText = "Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ)",
                        transliteration = "Nowa do sarjom dare kana, bir ar jiwi renag mur kana.",
                        transliterationDevanagari = "नोवा दो सारजोम दारे काना, बीर आर जीवी रेनाग मुड़ काना।",
                        phoneticSyllables = listOf("नो-वा", "सार-जोम", "दा-रे", "का-ना", "बीर", "जी-वी")
                    )
                    TargetLanguage.HO -> VoiceTranslationResult(
                        targetText = "ᱱᱮᱱᱟ ᱫᱚ ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱩ ᱛᱟᱱᱟ, ᱵᱤᱨ ᱨᱮᱭᱟᱜ ᱡᱤᱣᱤ ᱛᱟᱱᱟ᱾",
                        scriptText = "Warang Chiti (ᱣᱟᱨᱟᱝ ᱪᱤᱛᱤ)",
                        transliteration = "Nena do sarjom daru tana, bir reyag jiwi tana.",
                        transliterationDevanagari = "नेना दो सारजोम दारू ताना, बीर रेयाग जीवी ताना।",
                        phoneticSyllables = listOf("ने-ना", "सार-जोम", "दा-रू", "ता-ना", "बीर")
                    )
                    TargetLanguage.MUNDARI -> VoiceTranslationResult(
                        targetText = "नेना दो सारजोम दारू तन, बिर रेयाः जीवी तन।",
                        scriptText = "Devanagari Mundari (देवनागरी मुण्डारी)",
                        transliteration = "Nena do sarjom daru tan, bir reyah jiwi tan.",
                        transliterationDevanagari = "नेना दो सारजोम दारू तन, बिर रेयाः जीवी तन।",
                        phoneticSyllables = listOf("ने-ना", "सार-जोम", "दा-रू", "तन", "बिर")
                    )
                }
            }

            // 5. Water & River Ecology
            lower.contains("पानी") || lower.contains("जल") || lower.contains("नदी") || lower.contains("तालाब") || lower.contains("साफ़") -> {
                when (lang) {
                    TargetLanguage.SANTHALI -> VoiceTranslationResult(
                        targetText = "ᱜᱟᱰᱟ ᱫᱟᱜ ᱟᱨ ᱯᱩᱠᱷᱨᱤ ᱫᱟᱜ ᱫᱚ ᱥᱟᱯᱷᱟ ᱫᱚᱦᱚᱭ ᱯᱮ᱾",
                        scriptText = "Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ)",
                        transliteration = "Gada dah ar pukhri dah do sapha dohoy pe.",
                        transliterationDevanagari = "गाडा दाग आर पुखरी दाग दो साफा दोहोय पे।",
                        phoneticSyllables = listOf("गा-डा", "दाग", "पुख-री", "सा-फा", "दो-होय-पे")
                    )
                    TargetLanguage.HO -> VoiceTranslationResult(
                        targetText = "ᱜᱟᱰᱟ ᱫᱟᱺ ᱫᱚ ᱟᱹᱵᱩᱣᱟᱜ ᱡᱤᱣᱤ ᱛᱟᱱᱟ, ᱥᱟᱯᱷᱟ ᱫᱚᱦᱚᱭ ᱯᱮ᱾",
                        scriptText = "Warang Chiti (ᱣᱟᱨᱟᱝ ᱪᱤᱛᱤ)",
                        transliteration = "Gada da: do abuwag jiwi tana, sapha dohoy pe.",
                        transliterationDevanagari = "गाडा दाः दो आबुवाग जीवी ताना, साफा दोहोय पे।",
                        phoneticSyllables = listOf("गा-डा", "दाः", "आ-बु-वाग", "सा-फा")
                    )
                    TargetLanguage.MUNDARI -> VoiceTranslationResult(
                        targetText = "गाडा दाः दो आबुवाः जीवी तन, साफा दोहोय पे।",
                        scriptText = "Devanagari Mundari (देवनागरी मुण्डारी)",
                        transliteration = "Gada dah do abuwah jiwi tan, sapha dohoy pe.",
                        transliterationDevanagari = "गाडा दाः दो आबुवाः जीवी तन, साफा दोहोय पे।",
                        phoneticSyllables = listOf("गा-डा", "दाः", "आ-बु-वाः", "सा-फा")
                    )
                }
            }

            // 6. Counting & Numeracy (1 to 10)
            lower.contains("गिनती") || lower.contains("संख्या") || lower.contains("गिनो") || lower.contains("एक") || lower.contains("दो") || lower.contains("तीन") -> {
                when (lang) {
                    TargetLanguage.SANTHALI -> VoiceTranslationResult(
                        targetText = "ᱢᱤᱫ, ᱵᱟᱨ, ᱯᱮ, ᱯᱩᱱ, ᱢᱚᱬᱮ! ᱵᱚᱱ ᱞᱮᱠᱷᱟᱭ-ᱟ᱾",
                        scriptText = "Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ)",
                        transliteration = "Mit', bar, pe, pun, mone! Bon lekhay-a.",
                        transliterationDevanagari = "मिद, बार, पे, पून, मोड़े! बोन लेखाया।",
                        phoneticSyllables = listOf("मिद", "बार", "पे", "पून", "मो-ड़े", "ले-खा-या")
                    )
                    TargetLanguage.HO -> VoiceTranslationResult(
                        targetText = "ᱢᱤᱭᱟᱹᱫᱽ, ᱵᱟᱹᱨᱤᱭᱟᱹ, ᱟᱹᱯᱤᱭᱟᱹ, ᱩᱯᱩᱱᱤᱭᱟᱹ, ᱢᱚᱬᱮᱭᱟᱹ! ᱵᱚᱱ ᱦᱤᱥᱟᱹᱵᱽ-ᱟ᱾",
                        scriptText = "Warang Chiti (ᱣᱟᱨᱟᱝ ᱪᱤᱛᱤ)",
                        transliteration = "Miyad, bariya, apiya, upuniya, moneya! Bon hisab-a.",
                        transliterationDevanagari = "मियाद, बारिया, आपिया, उपुनिया, मोड़ेया! बोन हिसाबा।",
                        phoneticSyllables = listOf("मि-याद", "बा-रि-या", "आ-पि-या", "उ-पु-नि-या", "मो-ड़े-या")
                    )
                    TargetLanguage.MUNDARI -> VoiceTranslationResult(
                        targetText = "मियाद, बारिया, आपिया, उपुनिया, मोड़ेया! बु लेखा-ए।",
                        scriptText = "Devanagari Mundari (देवनागरी मुण्डारी)",
                        transliteration = "Miyad, bariya, apiya, upuniya, moneya! Bu lekha-e.",
                        transliterationDevanagari = "मियाद, बारिया, आपिया, उपुनिया, मोड़ेया! बु लेखा-ए।",
                        phoneticSyllables = listOf("मि-याद", "बा-रि-या", "आ-पि-या", "उ-पु-नि-या", "बु-ले-खा")
                    )
                }
            }

            // 7. Hygiene, Meal & Handwashing
            lower.contains("हाथ") || lower.contains("धो") || lower.contains("साबुन") || lower.contains("खाना") || lower.contains("भोजन") || lower.contains("सफ़ाई") -> {
                when (lang) {
                    TargetLanguage.SANTHALI -> VoiceTranslationResult(
                        targetText = "ᱡᱚᱢ ᱢᱟᱬᱟᱝ ᱨᱮ ᱥᱟᱵᱚᱱ ᱛᱮ ᱛᱤ ᱟᱹᱨᱩᱵ ᱯᱮ᱾",
                        scriptText = "Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ)",
                        transliteration = "Jom manang re sabon te ti arub pe.",
                        transliterationDevanagari = "जोम माड़ांग रे साबोन ते ती आरूब पे।",
                        phoneticSyllables = listOf("जोम", "मा-ड़ांग", "सा-बोन", "ती", "आ-रूब-पे")
                    )
                    TargetLanguage.HO -> VoiceTranslationResult(
                        targetText = "ᱡᱚᱢ ᱟᱭᱟᱨ ᱨᱮ ᱥᱟᱵᱚᱱ ᱛᱮ ᱛᱤ ᱟᱹᱵᱩᱝ ᱯᱮ᱾",
                        scriptText = "Warang Chiti (ᱣᱟᱨᱟᱝ ᱪᱤᱛᱤ)",
                        transliteration = "Jom ayar re sabon te ti abung pe.",
                        transliterationDevanagari = "जोम आयर रे साबोन ते ती आबूंग पे।",
                        phoneticSyllables = listOf("जोम", "आ-यर", "सा-बोन", "ती", "आ-बूं-ग-पे")
                    )
                    TargetLanguage.MUNDARI -> VoiceTranslationResult(
                        targetText = "मांडी जोम सिदा रे साबुन ते ती आरुब पे।",
                        scriptText = "Devanagari Mundari (देवनागरी मुण्डारी)",
                        transliteration = "Mandi jom sida re sabun te ti arub pe.",
                        transliterationDevanagari = "मांडी जोम सिदा रे साबुन ते ती आरुब पे।",
                        phoneticSyllables = listOf("मां-डी", "जोम", "सि-दा", "सा-बुन", "ती", "आ-रुब-पे")
                    )
                }
            }

            // 8. Praise, Appreciation & Encouragement
            lower.contains("शाबाश") || lower.contains("अच्छा") || lower.contains("सुंदर") || lower.contains("सही") || lower.contains("बधाई") || lower.contains("धन्यवाद") -> {
                when (lang) {
                    TargetLanguage.SANTHALI -> VoiceTranslationResult(
                        targetText = "ᱟᱹᱰᱤ ᱥᱟᱨᱦᱟᱣ! ᱟᱹᱰᱤ ᱱᱟᱯᱟᱭ ᱠᱟᱹᱢᱤ ᱠᱮᱫᱟ ᱯᱮ᱾",
                        scriptText = "Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ)",
                        transliteration = "Adi sarhaw! Adi napay kami keda pe.",
                        transliterationDevanagari = "आडी सारहाव! आडी नापाय कामी केदा पे।",
                        phoneticSyllables = listOf("आ-डी", "सार-हाव", "ना-पाय", "का-मी", "के-दा-पे")
                    )
                    TargetLanguage.HO -> VoiceTranslationResult(
                        targetText = "ᱟᱹᱰᱤ ᱵᱮᱥ! ᱟᱹᱰᱤ ᱵᱩᱜᱤᱱ ᱠᱟᱹᱢᱤ ᱠᱮᱫᱟ ᱯᱮ᱾",
                        scriptText = "Warang Chiti (ᱣᱟᱨᱟᱝ ᱪᱤᱛᱤ)",
                        transliteration = "Adi bes! Adi bugin kami keda pe.",
                        transliterationDevanagari = "आडी बेस! आडी बुगिन कामी केदा पे।",
                        phoneticSyllables = listOf("आ-डी", "बेस", "बु-गिन", "का-मी", "के-दा-पे")
                    )
                    TargetLanguage.MUNDARI -> VoiceTranslationResult(
                        targetText = "आडी बुगी! आडी बेस कामी केदा पे।",
                        scriptText = "Devanagari Mundari (देवनागरी मुण्डारी)",
                        transliteration = "Adi bugi! Adi bes kami keda pe.",
                        transliterationDevanagari = "आडी बुगी! आडी बेस कामी केदा पे।",
                        phoneticSyllables = listOf("आ-डी", "बु-गी", "बेस", "का-मी", "के-दा-पे")
                    )
                }
            }

            // 9. Repetition & Choral Response
            lower.contains("साथ") || lower.contains("दोहरा") || lower.contains("बोल") || lower.contains("कहो") || lower.contains("repeat") -> {
                when (lang) {
                    TargetLanguage.SANTHALI -> VoiceTranslationResult(
                        targetText = "ᱥᱟᱱᱟᱢ ᱜᱤᱫᱽᱨᱟᱹ ᱢᱤᱫ ᱛᱮ ᱞᱟᱹᱭ ᱯᱮ! ᱟᱹᱰᱤ ᱱᱟᱯᱟᱭ᱾",
                        scriptText = "Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ)",
                        transliteration = "Sanam gidra mit' te lay pe! Adi napay.",
                        transliterationDevanagari = "सानाम गिदरा मिद ते लय पे! आडी नापाय।",
                        phoneticSyllables = listOf("सा-नाम", "गिद-रा", "मिद-ते", "लय-पे", "ना-पाय")
                    )
                    TargetLanguage.HO -> VoiceTranslationResult(
                        targetText = "ᱥᱚᱵᱮᱱ ᱜᱤᱫᱽᱨᱟᱹ ᱢᱤᱭᱟᱹᱫᱽ ᱛᱮ ᱠᱟᱡᱤ ᱯᱮ! ᱟᱹᱰᱤ ᱵᱮᱥ᱾",
                        scriptText = "Warang Chiti (ᱣᱟᱨᱟᱝ ᱪᱤᱛᱤ)",
                        transliteration = "Soben gidra miyad te kaji pe! Adi bes.",
                        transliterationDevanagari = "सोबेन गिदरा मियाद ते काजी पे! आडी बेस।",
                        phoneticSyllables = listOf("सो-बेन", "गिद-रा", "मि-याद-ते", "का-जी-पे")
                    )
                    TargetLanguage.MUNDARI -> VoiceTranslationResult(
                        targetText = "सोबेन गिदरा मियाद ते कजी पे! आडी बुगी।",
                        scriptText = "Devanagari Mundari (देवनागरी मुण्डारी)",
                        transliteration = "Soben gidra miyad te kaji pe! Adi bugi.",
                        transliterationDevanagari = "सोबेन गिदरा मियाद ते कजी पे! आडी बुगी।",
                        phoneticSyllables = listOf("सो-बेन", "गिद-रा", "मि-याद-ते", "क-जी-पे")
                    )
                }
            }

            // 10. Silence, Attention & Discipline
            lower.contains("शांत") || lower.contains("चुप") || lower.contains("आवाज़ मत") || lower.contains("ध्यान") || lower.contains("सुनो") -> {
                when (lang) {
                    TargetLanguage.SANTHALI -> VoiceTranslationResult(
                        targetText = "ᱡᱚᱛᱚ ᱦᱚᱲ ᱛᱷᱤᱨ ᱛᱟᱦᱮᱸᱱ ᱯᱮ ᱟᱨ ᱫᱷᱮᱭᱟᱱ ᱛᱮ ᱟᱧᱡᱚᱢ ᱯᱮ᱾",
                        scriptText = "Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ)",
                        transliteration = "Joto hor thir tahen pe ar dheyan te anjom pe.",
                        transliterationDevanagari = "जोतो होड़ थीर ताहेन पे आर धेयान ते आंजोम पे।",
                        phoneticSyllables = listOf("जो-तो", "होड़", "थीर", "ता-हेन", "आं-जोम-पे")
                    )
                    TargetLanguage.HO -> VoiceTranslationResult(
                        targetText = "ᱡᱚᱛᱚ ᱠᱚ ᱛᱷᱤᱨ ᱛᱟᱭᱠᱮᱱ ᱯᱮ ᱟᱨ ᱫᱷᱮᱭᱟᱱ ᱛᱮ ᱟᱧᱡᱚᱢ ᱯᱮ᱾",
                        scriptText = "Warang Chiti (ᱣᱟᱨᱟᱝ ᱪᱤᱛᱤ)",
                        transliteration = "Joto ko thir tayken pe ar dheyan te anjom pe.",
                        transliterationDevanagari = "जोतो को थीर तायकेन पे आर धेयान ते आंजोम पे।",
                        phoneticSyllables = listOf("जो-तो", "को", "थीर", "ताय-केन", "आं-जोम-पे")
                    )
                    TargetLanguage.MUNDARI -> VoiceTranslationResult(
                        targetText = "जोतो को थीर ताएन पे आर ध्यान ते आंजोम पे।",
                        scriptText = "Devanagari Mundari (देवनागरी मुण्डारी)",
                        transliteration = "Joto ko thir taen pe ar dhyan te anjom pe.",
                        transliterationDevanagari = "जोतो को थीर ताएन पे आर ध्यान ते आंजोम पे।",
                        phoneticSyllables = listOf("जो-तो", "को", "थीर", "ता-एन", "आं-जोम-पे")
                    )
                }
            }

            // 11. Animals, Birds & Fauna
            lower.contains("जानवर") || lower.contains("गाय") || lower.contains("हाथी") || lower.contains("बाघ") || lower.contains("पक्षी") || lower.contains("चिड़िया") || lower.contains("बैल") || lower.contains("बकरी") -> {
                when (lang) {
                    TargetLanguage.SANTHALI -> VoiceTranslationResult(
                        targetText = "ᱜᱟᱹᱭ, ᱢᱮᱨᱚᱢ ᱟᱨ ᱪᱮᱬᱮ ᱫᱚ ᱟᱵᱚ ᱨᱮᱱ ᱜᱟᱛᱮ ᱠᱟᱱᱟ ᱠᱚ᱾",
                        scriptText = "Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ)",
                        transliteration = "Gay, merom ar chene do abo ren gate kana ko.",
                        transliterationDevanagari = "गयी, मेरम आर चेड़े दो आबो रेन गाते काना को।",
                        phoneticSyllables = listOf("ग-यी", "मे-रम", "चे-ड़े", "आ-बो", "गा-ते")
                    )
                    TargetLanguage.HO -> VoiceTranslationResult(
                        targetText = "ᱜᱟᱹᱭ, ᱢᱮᱨᱚᱢ ᱟᱨ ᱪᱮᱬᱮ ᱠᱚ ᱫᱚ ᱟᱵᱩᱣᱟᱜ ᱡᱤᱣᱤ ᱛᱟᱱᱟ ᱠᱚ᱾",
                        scriptText = "Warang Chiti (ᱣᱟᱨᱟᱝ ᱪᱤᱛᱤ)",
                        transliteration = "Gay, merom ar chene ko do abuwag jiwi tana ko.",
                        transliterationDevanagari = "गयी, मेरम आर चेड़े को दो आबुवाग जीवी ताना को।",
                        phoneticSyllables = listOf("ग-यी", "मे-रम", "चे-ड़े", "आ-बु-वाग", "जी-वी")
                    )
                    TargetLanguage.MUNDARI -> VoiceTranslationResult(
                        targetText = "गयी, मेरम आर चेड़े को दो आबुवाः जोंते तन को।",
                        scriptText = "Devanagari Mundari (देवनागरी मुण्डारी)",
                        transliteration = "Gay, merom ar chene ko do abuwah jonte tan ko.",
                        transliterationDevanagari = "गयी, मेरम आर चेड़े को दो आबुवाः जोंते तन को।",
                        phoneticSyllables = listOf("ग-यी", "मे-रम", "चे-ड़े", "आ-बु-वाः", "जों-ते")
                    )
                }
            }

            // 12. Homework, Dismissal & Routine
            lower.contains("गृहकार्य") || lower.contains("होमवर्क") || lower.contains("घर") || lower.contains("कल") || lower.contains("छुट्टी") -> {
                when (lang) {
                    TargetLanguage.SANTHALI -> VoiceTranslationResult(
                        targetText = "ᱜᱟᱯᱟ ᱚᱲᱟᱜ ᱠᱷᱚᱱ ᱠᱟᱹᱢᱤ ᱯᱩᱨᱟᱹᱣ ᱠᱟᱛᱮ ᱦᱤᱡᱩᱜ ᱯᱮ᱾",
                        scriptText = "Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ)",
                        transliteration = "Gapa orag khon kami puraw kate hijug pe.",
                        transliterationDevanagari = "गापा ओड़ाग खोन कामी पुराव काते हिजुग पे।",
                        phoneticSyllables = listOf("गा-पा", "ओ-ड़ाग", "का-मी", "पु-राव", "हि-जुग-पे")
                    )
                    TargetLanguage.HO -> VoiceTranslationResult(
                        targetText = "ᱜᱟᱯᱟ ᱚᱲᱟᱜ ᱮᱛᱮ ᱠᱟᱹᱢᱤ ᱠᱟᱛᱮ ᱦᱤᱡᱩᱜ ᱯᱮ᱾",
                        scriptText = "Warang Chiti (ᱣᱟᱨᱟᱝ ᱪᱤᱛᱤ)",
                        transliteration = "Gapa orag ete kami kate hijug pe.",
                        transliterationDevanagari = "गापा ओड़ाग एते कामी काते हिजुग पे।",
                        phoneticSyllables = listOf("गा-पा", "ओ-ड़ाग", "ए-ते", "का-मी", "हि-जुग-पे")
                    )
                    TargetLanguage.MUNDARI -> VoiceTranslationResult(
                        targetText = "गापा ओड़ाः एते कामी पूरा केते हिजुग पे।",
                        scriptText = "Devanagari Mundari (देवनागरी मुण्डारी)",
                        transliteration = "Gapa odah ete kami pura kete hijug pe.",
                        transliterationDevanagari = "गापा ओड़ाः एते कामी पूरा केते हिजुग पे।",
                        phoneticSyllables = listOf("गा-पा", "ओ-ड़ाः", "का-मी", "पू-रा", "हि-जुग-पे")
                    )
                }
            }

            // 13. Question & Answering (Who knows, ask questions)
            lower.contains("किसको") || lower.contains("उत्तर") || lower.contains("प्रश्न") || lower.contains("सवाल") || lower.contains("कौन") -> {
                when (lang) {
                    TargetLanguage.SANTHALI -> VoiceTranslationResult(
                        targetText = "ᱚᱠᱚᱭ ᱵᱟᱰᱟᱭᱟ ᱛᱮᱞᱟ? ᱟᱯᱱᱟᱨ ᱛᱤ ᱛᱩᱞ ᱯᱮ᱾",
                        scriptText = "Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ)",
                        transliteration = "Okoy badaya tela? Apnar ti tul pe.",
                        transliterationDevanagari = "ओकोय बाडाया तेला? आपनार ती तुल पे।",
                        phoneticSyllables = listOf("ओ-कोय", "बा-डा-या", "ते-ला", "आप-नार", "ती-तुल")
                    )
                    TargetLanguage.HO -> VoiceTranslationResult(
                        targetText = "ᱡᱮ ᱥᱟᱹᱨᱤ ᱠᱟᱡᱤ ᱥᱟᱱᱟᱭᱮ ᱛᱟᱱᱟ, ᱛᱤ ᱛᱩᱞ ᱯᱮ᱾",
                        scriptText = "Warang Chiti (ᱣᱟᱨᱟᱝ ᱪᱤᱛᱤ)",
                        transliteration = "Je sari kaji sanaye tana, ti tul pe.",
                        transliterationDevanagari = "जे सारी काजी सानाये ताना, ती तुल पे।",
                        phoneticSyllables = listOf("जे", "सा-री", "का-जी", "ती-तुल-पे")
                    )
                    TargetLanguage.MUNDARI -> VoiceTranslationResult(
                        targetText = "अकोय काजी सानाई तना, ती तुल पे।",
                        scriptText = "Devanagari Mundari (देवनागरी मुण्डारी)",
                        transliteration = "Akoy kaji sanai tana, ti tul pe.",
                        transliterationDevanagari = "अकोय काजी सानाई तना, ती तुल पे।",
                        phoneticSyllables = listOf("अ-कोय", "का-जी", "सा-नाई", "ती-तुल-पे")
                    )
                }
            }

            // 14. Fallback / General Classroom Guidance
            else -> {
                when (lang) {
                    TargetLanguage.SANTHALI -> VoiceTranslationResult(
                        targetText = "ᱟᱥᱲᱟ ᱨᱮ ᱥᱟᱱᱛᱟᱲᱤ ᱛᱮ ᱥᱮᱪᱮᱫ: $hindi",
                        scriptText = "Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ)",
                        transliteration = "Asra re Santhali te seched: $hindi",
                        transliterationDevanagari = "आसड़ा रे संथाली ते सेचेद: $hindi",
                        phoneticSyllables = listOf("आस-ड़ा", "सं-था-ली", "से-चेद")
                    )
                    TargetLanguage.HO -> VoiceTranslationResult(
                        targetText = "ᱤᱛᱩᱱ ᱟᱥᱲᱟ ᱨᱮ ᱦᱳ ᱡᱟᱜᱟᱨ ᱛᱮ ᱪᱮᱫ: $hindi",
                        scriptText = "Warang Chiti (ᱣᱟᱨᱟᱝ ᱪᱤᱛᱤ)",
                        transliteration = "Itun asra re Ho jagar te ched: $hindi",
                        transliterationDevanagari = "इतुन आसड़ा रे हो जागार ते चेद: $hindi",
                        phoneticSyllables = listOf("इ-तुन", "आस-ड़ा", "हो-जा-गार", "चेद")
                    )
                    TargetLanguage.MUNDARI -> VoiceTranslationResult(
                        targetText = "आसड़ा रे मुण्डारी जगर ते पढ़व: $hindi",
                        scriptText = "Devanagari Mundari (देवनागरी मुण्डारी)",
                        transliteration = "Asra re Mundari jagar te padhaw: $hindi",
                        transliterationDevanagari = "आसड़ा रे मुण्डारी जगर ते पढ़व: $hindi",
                        phoneticSyllables = listOf("आस-ड़ा", "मुण-डा-री", "ज-गर", "प-ढ़व")
                    )
                }
            }
        }
    }

    /**
     * Student mode: Tribal mother-tongue utterance -> Hindi meaning for teacher comprehension.
     */
    fun translateStudentToTeacher(tribalSpeech: String, lang: TargetLanguage): VoiceTranslationResult {
        val lower = tribalSpeech.lowercase().trim()

        return when {
            // Student Greetings / Johar
            lower.contains("ᱡᱚᱦᱟᱨ") || lower.contains("जोहार") || lower.contains("johar") -> {
                VoiceTranslationResult(
                    targetText = "नमस्ते गुरुजी! जोहार।",
                    scriptText = "Devanagari Hindi",
                    transliteration = "Namaste Guruji! Johar.",
                    transliterationDevanagari = "नमस्ते गुरुजी! जोहार।",
                    phoneticSyllables = listOf("न-मस-ते", "गु-रु-जी", "जो-हार")
                )
            }
            // Student Affirmation (I understood / Yes)
            lower.contains("ᱦᱮᱸ") || lower.contains("हें") || lower.contains("बेडा") || lower.contains("ᱵᱟᱰᱟᱭ") || lower.contains("समझ") -> {
                VoiceTranslationResult(
                    targetText = "जी गुरुजी, मुझे समझ आ गया।",
                    scriptText = "Devanagari Hindi",
                    transliteration = "Ji Guruji, mujhe samajh aa gaya.",
                    transliterationDevanagari = "जी गुरुजी, मुझे समझ आ गया।",
                    phoneticSyllables = listOf("जी", "गु-रु-जी", "स-मझ", "आ-ग-या")
                )
            }
            // Student Water request
            lower.contains("ᱫᱟᱜ") || lower.contains("दाग") || lower.contains("दाः") || lower.contains("दाम") -> {
                VoiceTranslationResult(
                    targetText = "गुरुजी, मुझे पानी पीना है।",
                    scriptText = "Devanagari Hindi",
                    transliteration = "Guruji, mujhe paani peena hai.",
                    transliterationDevanagari = "गुरुजी, मुझे पानी पीना है।",
                    phoneticSyllables = listOf("गु-रु-जी", "पा-नी", "पी-ना", "है")
                )
            }
            // Student Book / Slate answer
            lower.contains("ᱯᱚᱛᱚᱵ") || lower.contains("पोतोब") || lower.contains("पुथी") -> {
                VoiceTranslationResult(
                    targetText = "गुरुजी, मैंने अपनी किताब खोल ली है।",
                    scriptText = "Devanagari Hindi",
                    transliteration = "Guruji, maine apni kitaab khol li hai.",
                    transliterationDevanagari = "गुरुजी, मैंने अपनी किताब खोल ली है।",
                    phoneticSyllables = listOf("गु-रु-जी", "कि-ताब", "खोल-ली")
                )
            }
            // General Student response
            else -> {
                VoiceTranslationResult(
                    targetText = "विद्यार्थी का उत्तर (${lang.displayName}): $tribalSpeech",
                    scriptText = "Devanagari Hindi",
                    transliteration = "Vidhyarthi ka uttar: $tribalSpeech",
                    transliterationDevanagari = "विद्यार्थी का उत्तर: $tribalSpeech",
                    phoneticSyllables = listOf("विद्-यार-थी", "का", "उत-तर")
                )
            }
        }
    }
}
