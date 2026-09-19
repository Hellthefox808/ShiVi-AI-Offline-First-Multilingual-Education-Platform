# VoxBridge AI — Spoken Language Identification (LID) & Multilingual Routing

## 1. Multi-Tier Language Detection Architecture

VoxBridge AI employs a cascading, multi-tier Language Identification (LID) architecture combining fast acoustic feature modeling on raw audio frames with lexical Bayesian verification on partial text transcripts.

```
                  [RAW AUDIO CHUNKS (0 - 800ms)]
                                │
                                ▼
         ┌─────────────────────────────────────────────┐
         │  Tier 1: Fast Acoustic LID (Silero / ECAPA) │
         │  - 192-dimensional x-vector embedding      │
         │  - Inference Latency: 15ms                  │
         │  - Classifies Top 107 Spoken Languages      │
         └──────────────────────┬──────────────────────┘
                                │
             Confidence >= 0.85?│
                  ┌─────────────┴─────────────┐
                  ▼ YES                       ▼ NO (Ambiguous 0.40 - 0.84)
       [Lock Acoustic Language]    [Forward Audio to Multilingual STT]
                                              │
                                              ▼
                               ┌─────────────────────────────┐
                               │ Tier 2: Lexical N-Gram LID  │
                               │ (FastText / CLD3 on Tokens) │
                               │ - Evaluates partial text    │
                               │ - Resolves similar dialects │
                               └──────────────┬──────────────┘
                                              │
                                              ▼
                                 [Final Ensembled Decision]
```

---

## 2. Language Routing Policies

The platform enforces three deterministic language detection policies specified during session creation:

### 2.1 Policy 1: `EXPLICIT`
- **Behavior:** The client specifies a fixed language tag (e.g., `source_language: "es-MX"`).
- **Enforcement:** LID models are completely bypassed. Audio frames are routed directly to the dedicated Mexican Spanish acoustic STT model.
- **Latency Optimization:** Saves 20ms to 40ms of initial identification latency.

### 2.2 Policy 2: `AUTO`
- **Behavior:** The system evaluates acoustic frames continuously across all 100+ supported global languages.
- **Ambiguity Guardrail:** If the highest model probability is below `0.50`, the system **REFUSES** to guess. It defaults to the project's configured fallback language (or returns `UNCONFIRMED_LANGUAGE` error) instead of inventing a false classification.

### 2.3 Policy 3: `AUTO_WITH_ALLOWED_SET`
- **Behavior:** Constrains the classifier's softmax probability distribution to an explicit candidate list (e.g., `allowed_languages: ["en-US", "hi-IN", "es-ES"]`).
- **Use Case:** Multilingual call centers in defined geographic territories.
- **Accuracy Boost:** Eliminates false positive classifications between phonetically adjacent regional dialects (e.g., distinguishing Canadian French from Haitian Creole).

---

## 3. Code-Switching & Mixed-Language Speech

In many global regions (e.g., India, Southeast Asia, Hispanic America), speakers frequently code-switch within the exact same sentence (e.g., *Hinglish* or *Spanglish*):
> *"Kal presentation hai, so please make sure slides properly format ho jayein."*

### 3.1 Dual-Tokenization Strategy
1. **Multilingual Shared Subword Vocabulary:** The acoustic tokenizer uses a unified 256,000-token SentencePiece vocabulary encompassing Latin, Devanagari, Cyrillic, Arabic, and Hanzi scripts.
2. **Dynamic Language Tagging per Token:** Words in the decoded stream carry subword language probabilities:
   ```json
   {
     "detected_language": "hi-IN",
     "overall_confidence": 0.89,
     "code_switching_detected": true,
     "segments": [
       {"text": "Kal presentation hai, so please make sure", "language": "hi-IN", "script": "Devanagari/Latin"},
       {"text": "slides properly format ho jayein.", "language": "hi-IN", "script": "Latin"}
     ],
     "alternative_languages": [
       {"language": "en-IN", "confidence": 0.74},
       {"language": "ur-PK", "confidence": 0.31}
     ]
   }
   ```
3. **Translation Preservation:** The downstream translation engine receives explicit code-switch boundaries to ensure technical terminology borrowed from English is not erroneously re-translated into archaic target language terms.
