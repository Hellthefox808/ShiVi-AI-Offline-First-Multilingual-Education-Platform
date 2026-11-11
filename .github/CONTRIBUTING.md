# Contributing to BhashaSetu AI (भाषासेतु)

Thank you for your interest in contributing to BhashaSetu AI! Our mission is to bridge the linguistic divide in primary education across Jharkhand and other multilingual regions.

---

## 🧭 Code of Conduct

All contributors and maintainers are expected to maintain a professional, respectful, and inclusive collaboration environment.

---

## 🌿 Branching & Git Workflow

We adhere to a standard Trunk-Based / Feature-Branch workflow:

1. **Fork the repository** and clone your fork locally.
2. Create a descriptive feature branch from `main`:
   ```bash
   git checkout -b feat/your-feature-name
   # or
   git checkout -b fix/your-bug-fix
   ```
3. Use Conventional Commits formatting:
   - `feat(component): add new capability`
   - `fix(voice): correct phonetics audio relay`
   - `perf(backend): optimize outbox sync query`
   - `docs(readme): clarify local setup steps`
   - `test(rag): expand retrieval benchmark queries`

---

## 🛠️ Local Development & Testing

Ensure your changes build and pass all existing test suites before submitting a PR:

### Web Backend (`services/web-backend`)
```bash
cd services/web-backend
npm install
npm test
npm run build
```

### Shared Contracts (`packages/contracts`)
```bash
cd packages/contracts
npm install
npm run build
```

### Web Frontend (`apps/web-frontend`)
```bash
cd apps/web-frontend
npm install
npm run build
```

### AI Platform & Verification Suites (`tests/`)
```bash
# Infrastructure and manifest checks
python tests/test_infra.py

# Hybrid RAG retrieval benchmark
python tests/benchmark_rag.py

# Voice synthesis tests
python tests/test_voice_finetuning.py
```

---

## 🔒 Security & Data Hygiene Invariants

- **Zero Secret Commits:** Never commit `.env` files, production tokens, API keys, or keystores.
- **Offline Classroom Guarantee:** Changes to core classroom loops (audio playback, translation, quizzes) must operate 100% offline without blocking on network availability.
- **Pedagogical Invariant:** Educational content adaptation must preserve state-prescribed Learning Outcome (LO) codes.

---

## 📬 Submitting a Pull Request

1. Push your branch to your GitHub fork.
2. Open a Pull Request targeting the `main` branch.
3. Complete all sections of the [Pull Request Template](.github/PULL_REQUEST_TEMPLATE.md).
4. Maintainers will review your contribution and provide feedback.
