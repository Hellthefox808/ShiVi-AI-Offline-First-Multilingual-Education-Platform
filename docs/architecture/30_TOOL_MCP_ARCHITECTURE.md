# 30 — MODEL CONTEXT PROTOCOL (MCP) INTEGRATION CONTROL PLANE

> **Document ID:** `BS-ARCH-30-MCP-CONTROL-PLANE`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Canonical Reference:** [`docs/mcp-registry.md`](file:///d:/HACKTHON/bhashasetu-ai/docs/mcp-registry.md)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. MCP Integration Topology

The Antigravity AI Engineering Operating System communicates with the BhashaSetu codebase through a standardized Model Context Protocol (MCP) server mesh:

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                         MCP SERVER TOPOLOGY MESH                            │
├────────────────────┬────────────────────┬───────────────────────────────────┤
│ Server Identifier  │ Protocol Transport │ Core Engineering Purpose          │
├────────────────────┼────────────────────┼───────────────────────────────────┤
│ 1. github          │ stdio (npx)        │ Repository PRs, issues, Actions CI│
│ 2. chrome-devtools │ stdio (npx)        │ Browser DOM & a11y E2E testing    │
│ 3. context7        │ mcp-remote (HTTPS) │ Authoritative modern docs retrieval│
│ 4. memory          │ stdio (npx)        │ Persistent architectural knowledge│
│ 5. git             │ stdio (uvx)        │ Repository Git diffs, commits     │
│ 6. filesystem      │ stdio (npx)        │ Scoped sandboxed file read/write  │
│ 7. postgresql      │ TCP (5432)         │ Schema & pgvector query auditing  │
│ 8. seq-thinking    │ stdio (npx)        │ Multi-stage cognitive problem solve│
│ 9. cloud-infra     │ ADC / gRPC         │ GCP Quotas, Compute, Pub/Sub      │
│ 10. observability  │ Google Cloud APIs  │ Distributed traces, logs, metrics │
│ 11. research       │ HTTPS APIs         │ Academic NLP papers, Gemini docs  │
│ 12. mcp-inspector  │ CLI Harness        │ Server validation & test harness  │
└────────────────────┴────────────────────┴───────────────────────────────────┘
```

---

## 2. Server Class Specifications & Permission Guardrails

### 2.1 Developer & Repository Control (`github`, `git`, `filesystem`)
- **Transport**: `stdio` spawned sub-processes.
- **Permission Boundaries**: Sandboxed strictly to `d:\HACKTHON\bhashasetu-ai` and `<appDataDir>`. Access to host system files outside permitted directories is rejected.
- **Destructive Capabilities**: Guarded. File deletion, branch force-pushes, and pull request merges require explicit confirmation.

### 2.2 Relational & Vector Data Store (`postgresql`)
- **Transport**: Native PostgreSQL connection over port `5432`.
- **Permission Boundaries**: Read-only access default; write capabilities restricted to verified migration scripts.
- **Safety Rule**: `DROP TABLE`, `TRUNCATE`, and unbounded `DELETE` operations are blocked by security policy.

### 2.3 Research & Documentation (`context7`, `gemini-api-docs`, `arxiv`)
- **Transport**: Remote HTTPS stream via `mcp-remote`.
- **Purpose**: Provides real-time, authoritative documentation for Next.js 16.3, React 19.2, FastAPI, and Hugging Face Transformers, eliminating training cutoff hallucinations.
- **Fallback**: Static local documentation in `docs/` is used if external networks are severed.
