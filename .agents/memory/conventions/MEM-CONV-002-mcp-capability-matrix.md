# MEM-CONV-002: MCP Control Plane Integration & Operational Protocol

- **ID**: `MEM-CONV-002`
- **TYPE**: `CONVENTION`
- **TITLE**: MCP Server & Capability Architecture Matrix
- **PROJECT**: `BhashaSetu-AI`
- **SOURCE**: [docs/mcp-registry.md](file:///d:/HACKTHON/bhashasetu-ai/docs/mcp-registry.md)
- **CREATED_AT**: 2026-09-16T10:10:00+05:30
- **UPDATED_AT**: 2026-09-16T10:10:00+05:30
- **CONFIDENCE**: 1.0 (VERIFIED)
- **STATUS**: ACTIVE
- **TAGS**: `["mcp", "control-plane", "architecture", "tool-integration", "guardrails"]`
- **RELATED_COMPONENTS**: `["docs/mcp-registry.md", "mcp_config.json", ".mcp.json"]`
- **RELATED_DECISIONS**: `["MEM-ARCH-001", "MEM-CONV-001"]`
- **EXPIRY / STALENESS SIGNAL**: Permanent
- **PROVENANCE**: Antigravity Engineering OS Control Plane Policy

---

## CONTENT

The Antigravity Engineering OS assigns explicit operational responsibilities across 12 MCP server classes:

1. **GitHub MCP**: Source control intelligence, code search, issue management, PR reviews, workflow CI/CD diagnostics.
2. **Playwright / Chrome DevTools MCP**: Browser execution, E2E verification, live DOM inspection, and visual UI auditing.
3. **Context7**: Real-time official library/framework documentation retrieval (FastAPI, React 19, Next.js 15).
4. **MCP Inspector**: Protocol testing, schema diagnostics, and CI-testing for MCP servers.
5. **Memory**: Persistent project knowledge graph storing entities, relations, and observations across sessions.
6. **Git**: Repository-level Git operations (status, diffs, branches, commits, staging).
7. **Filesystem**: Controlled, sandboxed file access scoped strictly to approved roots (`D:/HACKTHON`, `C:/Users/ravir`).
8. **PostgreSQL**: Schema/query inspection, pgvector vector indexing, and table migrations.
9. **Sequential Thinking**: Structured analytical workflows, hypothesis evaluation, and multi-step plan verification.
10. **Cloud MCPs**: Infrastructure provisioning, quotas, and operational cloud context.
11. **Observability MCPs**: Centralized logging, distributed tracing, and metrics auditing.
12. **Research MCPs**: Upstream documentation and technical research papers.

### Invariants:
- Never assume an MCP server succeeded; verify with health check queries.
- Never expose sensitive tokens in logs or committed documentation.
- Maintain deterministic local fallbacks for every server class.
