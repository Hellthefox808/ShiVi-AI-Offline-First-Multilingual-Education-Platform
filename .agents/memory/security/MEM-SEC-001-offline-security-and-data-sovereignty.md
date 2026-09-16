# MEM-SEC-001: Security Architecture & Data Sovereignty Invariants

- **ID**: `MEM-SEC-001`
- **TYPE**: `SECURITY`
- **TITLE**: Offline-First Classroom Privacy, Token Vaulting, and Data Sovereignty
- **PROJECT**: `BhashaSetu-AI`
- **SOURCE**: [docs/SAD.md](file:///d:/HACKTHON/bhashasetu-ai/docs/SAD.md), [README.md](file:///d:/HACKTHON/bhashasetu-ai/README.md)
- **CREATED_AT**: 2026-09-16T10:00:00+05:30
- **UPDATED_AT**: 2026-09-16T10:00:00+05:30
- **CONFIDENCE**: 1.0 (VERIFIED)
- **STATUS**: ACTIVE
- **TAGS**: `["security", "privacy", "data-sovereignty", "offline-first", "jwt", "tokens"]`
- **RELATED_COMPONENTS**: `["app/", "services/web-backend/", ".env"]`
- **RELATED_DECISIONS**: `["MEM-ARCH-001"]`
- **EXPIRY / STALENESS SIGNAL**: Permanent
- **PROVENANCE**: Team SHIVI@808 Security Architecture

---

## CONTENT

1. **Student Voice Privacy & Edge Invariant**:
   - Audio recorded in classrooms is processed directly on-device via Android `SpeechRecognizer` and never streamed unencrypted or stored in unvetted third-party clouds.
   - Child privacy complies with POCSO and National Digital Education Architecture (NDEAR) specifications.

2. **Credential & Secret Protection**:
   - Zero hardcoded tokens in source code. All external API keys (`GEMINI_API_KEY`, `GITHUB_PERSONAL_ACCESS_TOKEN`, `JWT_SECRET`) are stored in isolated environment variables or protected keyrings.
   - `.env` and `.env.local` are explicitly added to `.gitignore`.

3. **Immutable Outbox & Idempotent Sync**:
   - Outbox events use UUID v4 correlation keys.
   - Gateway sync endpoint checks `event_id` against processed event hashes, ensuring replay attacks and network duplicate submissions are dropped gracefully.
