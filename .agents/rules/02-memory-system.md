# 5-Level Memory Model & Salience Policy

**Theoretical Grounding**: MemGPT (Virtual Context), LongMemEval, MemoryOS, Mem0, Zep/Graphiti (Temporal KG), Memoria (Versioned Memory)

---

## 1. Five Operational Memory Levels

| Level | Scope & Purpose | Content Types | TTL / Lifecycle |
| :--- | :--- | :--- | :--- |
| **Level 0: Live Context** | Working memory for current task | Prompt, diffs, tool returns, stack traces | Current task execution |
| **Level 1: Episodic Memory** | Session trajectories & run logs | Completed workflows, failed test attempts, triage traces | Days to months |
| **Level 2: Semantic Memory** | Curated project knowledge | Architecture, conventions, API contracts, DB invariants | Persistent across versions |
| **Level 3: Temporal KG** | Time-aware factual relations | Entities, relations, `valid_at`, `invalid_at`, supersedes | Evolving permanent |
| **Level 4: Versioned Memory** | Git-backed engineering facts | Commit-linked ADRs, verifiable incident post-mortems | Permanent with provenance |

---

## 2. Memory Record Standard Schema

All durable memory items stored in `.agents/memory/` must comply with this schema:

```json
{
  "id": "mem_01J...",
  "type": "architecture | decision | incident | convention | database | security",
  "scope": {
    "project": "bhashasetu-ai",
    "subsystem": "app | web-backend | ai-platform | web-frontend | contracts"
  },
  "content": "Specific verifiable technical statement or invariant.",
  "source": {
    "type": "repository | documentation | pr | benchmark",
    "path": "path/to/source.md",
    "commit": "git-sha"
  },
  "created_at": "ISO-8601 timestamp when learned",
  "valid_at": "ISO-8601 timestamp when fact became effective",
  "invalid_at": null,
  "confidence": 0.99,
  "importance": 0.95,
  "verification": "VERIFIED",
  "tags": ["phonetics", "tts", "devanagari-relay"],
  "supersedes": null,
  "superseded_by": null
}
```

---

## 3. Salience Filter & Contradiction Resolution

```text
INPUT -> FACT EXTRACTION -> SALIENCE TEST

  ├── HIGH   -> Contradiction Check -> Write / Version (Temporal KG)
  ├── MEDIUM -> Episodic Archive
  └── LOW    -> Discard (Conversational noise, greetings, transient steps)
```

- **Temporal Distinctions**: Never confuse `created_at` (when the agent learned the fact) with `valid_at` (when the fact actually became true).
- **Contradictions**: When new verified observations conflict with an existing memory record, do NOT silently delete the old record. Set `invalid_at` on the old record and link `supersedes` / `superseded_by`.
