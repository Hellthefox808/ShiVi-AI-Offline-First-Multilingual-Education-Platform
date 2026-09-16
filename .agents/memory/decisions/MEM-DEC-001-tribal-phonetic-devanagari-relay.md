# MEM-DEC-001: Devanagari Phonetic Synthesis for Tribal Dialects & Bilingual Relay

- **ID**: `MEM-DEC-001`
- **TYPE**: `DECISION`
- **TITLE**: Devanagari Phonetic Synthesis Strategy for Native Tribal Scripts
- **PROJECT**: `BhashaSetu-AI`
- **SOURCE**: [app/src/main/java/com/example/ui/util/TtsManager.kt](file:///d:/HACKTHON/bhashasetu-ai/app/src/main/java/com/example/ui/util/TtsManager.kt), [docs/FSD.md](file:///d:/HACKTHON/bhashasetu-ai/docs/FSD.md)
- **CREATED_AT**: 2026-09-16T10:00:00+05:30
- **UPDATED_AT**: 2026-09-16T10:00:00+05:30
- **CONFIDENCE**: 1.0 (VERIFIED)
- **STATUS**: ACTIVE
- **TAGS**: `["decision", "tts", "santhali", "ho", "mundari", "ol-chiki", "warang-chiti"]`
- **RELATED_COMPONENTS**: `["app/src/main/java/com/example/ui/util/TtsManager.kt", "app/src/main/java/com/example/ui/screens/VoiceTranslateScreen.kt"]`
- **RELATED_DECISIONS**: `["MEM-ARCH-001"]`
- **EXPIRY / STALENESS SIGNAL**: Permanent until native Ol Chiki/Warang Chiti acoustic models are released in Android TTS engines
- **PROVENANCE**: Core acoustic breakthrough by Team SHIVI@808

---

## CONTENT

### Context
Android TTS engines (Google Speech Services / Samsung TTS) do not ship with acoustic voice models for **Ol Chiki (Santhali, sat)** or **Warang Chiti (Ho, hoc)** scripts. Passing raw Unicode code points (`U+1C50-U+1C7F` or `U+118A0-U+118FF`) results in dropped audio glyphs or silence on Android devices.

### Decision
1. Route all tribal audio playback through `TtsManager.speakTribalPhonetic(...)`.
2. Extract the authentic Devanagari phonetic transliteration (`transliterationDevanagari`) corresponding to the tribal term and synthesize via the high-fidelity `hi-IN` acoustic voice model.
3. Enforce **Bilingual Relay Mode**:
   - Speak teacher's Hindi instruction first.
   - Insert an intentional 450ms pedagogical silence.
   - Speak the tribal mother-tongue translation.
4. Enforce **FLN Slow Practice Mode (0.72x speed)** for Grade 1-2 repetition drills.
