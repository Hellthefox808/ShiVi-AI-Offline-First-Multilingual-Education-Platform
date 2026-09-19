# 57 — CONTRIBUTING GUIDELINES & CODEOWNERS GOVERNANCE

> **Document ID:** `BS-ARCH-57-CONTRIBUTING`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Repository Governance:** GitHub Flow & Mandatory Peer Review Standards (§75 Master Standard)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Code Review & CODEOWNERS Assignment

All pull requests must receive approval from designated module owners before merge:

```text
# CODEOWNERS Map for BhashaSetu AI
*                                   @Hellthefox808 (Lead Architect)
/app/                               @Hellthefox808 (Android Lead)
/services/ai-platform/              @Hellthefox808 @ai-team
/services/web-backend/              @Hellthefox808 @backend-team
/apps/web-frontend/                 @Hellthefox808 @frontend-team
/infra/                             @Hellthefox808 @sre-team
/docs/                              @Hellthefox808
```

---

## 2. Pull Request Pre-Flight Checklist

Before opening a PR, the contributor must verify:
- [ ] `./gradlew testDebugUnitTest` runs cleanly with zero failures.
- [ ] `pytest services/ai-platform/test_platform.py` passes with COMET $\ge 0.85$.
- [ ] `npm test` passes in `services/web-backend/`.
- [ ] No hardcoded passwords, tokens, or private keys exist in diff (`git diff`).
- [ ] Commit message follows Conventional Commits specification.
- [ ] Any modified API route has its OpenAPI 3.1 schema updated.
