# VoxBridge AI — Linguistic Quality & Language Testing Matrix

## 1. Linguistic Test Matrix & Script Typology

The Language Testing framework validates linguistic fidelity, dialectal sensitivity, script preservation, and numerical accuracy across diverse language families:

| Test Group | Language Pair Example | Primary Evaluation Focus | Target Quality Metric | Pass Threshold |
|---|---|---|---|---|
| **Major High-Resource** | `en-US` $\to$ `es-ES`, `fr-FR`, `de-DE` | Conversational fluency, grammatical agreement | SacreBLEU / COMET | BLEU $\ge 42.0$ / COMET $\ge 0.88$ |
| **Cross-Script Transliteration**| `en-US` $\to$ `hi-IN` (Latin $\to$ Devanagari)| Phonetic script accuracy, matra placement | Character Error Rate (CER) | CER $\le 4.5\%$ |
| **Bidirectional RTL** | `en-US` $\to$ `ar-SA` (Latin $\to$ Arabic) | Right-to-left punctuation, numeral direction | Bidi Rendering Accuracy | $100\%$ Valid Bidi Marks |
| **Dialectal Variation** | `es-ES` (Spain) vs `es-MX` (Mexico) | Regional vocabulary (e.g., *ordenador* vs *computadora*) | Dialectal Accuracy Score | $\ge 95\%$ Lexical Match |
| **Code-Switching (Mixed)**| `hi-IN` + `en-US` (Hinglish) | Token boundary preservation, non-translation of loanwords | Entity Retention Rate | $\ge 98\%$ Technical Nouns |
| **Tribal / Low-Resource** | `hi-IN` $\to$ `sat-Olck` (Santhali) | Ol Chiki phonetic synthesis via Devanagari relay | Human Expert MOS | MOS $\ge 3.8$ |
| **Numeric & Currencies** | `en-US` $\to$ `ja-JP`, `hi-IN`, `de-DE` | Currency symbols, decimal/comma swap, lakhs/crores | Numeric Exact Match | $\mathbf{100\%}$ Exact Match |

---

## 2. Rigorous Numeric & Entity Preservation Test Suite

Numerical errors in voice translation (e.g., in banking IVRs) lead to catastrophic business liability:

```python
# tests/linguistic/test_numeric_preservation.py
import pytest
from voxbridge_eval.translation import evaluate_translation

NUMERIC_TEST_CASES = [
    {
        "source_text": "Transfer $1,250.50 to account 9876543210.",
        "target_lang": "es-ES",
        "must_contain": ["1.250,50", "9876543210"]
    },
    {
        "source_text": "The patient received 500mg of Amoxicillin at 08:30 AM.",
        "target_lang": "fr-FR",
        "must_contain": ["500", "mg", "Amoxicilline", "08:30"]
    },
    {
        "source_text": "The total budget is 25 lakh rupees.",
        "target_lang": "hi-IN",
        "must_contain": ["25", "लाख"]
    }
]

@pytest.mark.parametrize("case", NUMERIC_TEST_CASES)
def test_numeric_preservation_invariants(case):
    result = evaluate_translation(
        text=case["source_text"], 
        source_lang="en-US", 
        target_lang=case["target_lang"]
    )
    for token in case["must_contain"]:
        assert token in result.translated_text, (
            f"CRITICAL: Required numerical/entity token '{token}' missing from translation: {result.translated_text}"
        )
```
