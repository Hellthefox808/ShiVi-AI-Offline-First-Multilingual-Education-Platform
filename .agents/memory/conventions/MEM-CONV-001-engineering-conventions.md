# MEM-CONV-001: Monorepo Engineering Conventions & Invariants

- **ID**: `MEM-CONV-001`
- **TYPE**: `CONVENTION`
- **TITLE**: Monorepo Engineering Conventions, Script Standards, and Zero-Network Invariants
- **PROJECT**: `BhashaSetu-AI`
- **SOURCE**: [AGENTS.md](file:///d:/HACKTHON/bhashasetu-ai/AGENTS.md), [packages/contracts/](file:///d:/HACKTHON/bhashasetu-ai/packages/contracts)
- **CREATED_AT**: 2026-09-16T10:00:00+05:30
- **UPDATED_AT**: 2026-09-16T10:00:00+05:30
- **CONFIDENCE**: 1.0 (VERIFIED)
- **STATUS**: ACTIVE
- **TAGS**: `["conventions", "coding-standards", "typescript", "kotlin", "python"]`
- **RELATED_COMPONENTS**: `["app/", "services/", "apps/", "packages/"]`
- **RELATED_DECISIONS**: `["MEM-ARCH-001"]`
- **EXPIRY / STALENESS SIGNAL**: Permanent
- **PROVENANCE**: Team SHIVI@808 Monorepo Quality Standards

---

## CONTENT

1. **Strict Type Safety**:
   - TypeScript: `strict: true`, zero untyped `any`, shared DTOs imported from `@bhashasetu/contracts`.
   - Kotlin: Explicit nullability checks, Compose State hoisting, immutable state data classes.
   - Python: Pydantic v2 strict models for all API request/response payloads.

2. **Zero-Network Invariant**:
   - Any classroom feature running in the Android client must gracefully render offline using preloaded assets and local Room DB (`bhashasetu_local_db`).
   - Network calls must be non-blocking and queued via the `OutboxEntity`.

3. **Tribal Script Handling**:
   - Ol Chiki characters (` sat `): Unicode `U+1C50 - U+1C7F`.
   - Warang Chiti characters (` hoc `): Unicode `U+118A0 - U+118FF`.
   - Always pair native scripts with Devanagari transliteration for display and acoustic synthesis fallback.
