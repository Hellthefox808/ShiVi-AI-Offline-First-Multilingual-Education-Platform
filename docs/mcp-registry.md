# MCP Integration Control Plane & Architecture Registry

This registry provides the operational specification for all Model Context Protocol (MCP) server classes utilized within the **Antigravity Engineering OS** for **BhashaSetu AI (भाषासेतु)**.

---

## 1. Master Server & Capability Matrix

| Server / Capability | Real Engineering Use | Active Implementation / Provider | Operational Status |
| :--- | :--- | :--- | :--- |
| **GitHub MCP** | Repositories, code, issues, PRs, Actions, security findings | `@modelcontextprotocol/server-github` via npx | **VERIFIED** (Auth: `Hellthefox808`) |
| **Playwright MCP / Browser** | Browser execution, DOM inspection, and E2E verification | `chrome-devtools-mcp` + `browser_subagent` | **VERIFIED** (Live Chrome devtools) |
| **Context7** | Current library/framework documentation & API specs | `mcp-remote` @ `https://mcp.context7.com/mcp` | **VERIFIED** (FastAPI, React, Next.js) |
| **MCP Inspector** | Inspect, debug, and CI-test MCP servers & schemas | `npx @modelcontextprotocol/inspector` / Local CLI | **AVAILABLE** |
| **Memory** | Persistent project knowledge, entities, relations, observations | `@modelcontextprotocol/server-memory` via npx | **VERIFIED** (10 nodes, 10 relations) |
| **Git** | Repository-level Git operations (status, diffs, branches, commits) | `mcp-server-git` via uvx | **VERIFIED** (Local workspace repo) |
| **Filesystem** | Controlled file access scoped to workspace directories | `@modelcontextprotocol/server-filesystem` via npx | **VERIFIED** (Scoped: `D:/HACKTHON`, `C:/Users/ravir`) |
| **PostgreSQL** | Schema/query inspection, pgvector embeddings, table migrations | `alloydb-postgresql` + Docker Compose PostgreSQL 18 | **CONFIGURED** (`infra/docker-compose.yml`) |
| **Sequential Thinking** | Structured analytical workflows, multi-step problem solving | `@modelcontextprotocol/server-sequential-thinking` via npx | **VERIFIED** (Dynamic branch tracking) |
| **Cloud MCPs** | Infrastructure, quotas, compute, and operational context | Google Cloud Engine, PubSub, Resource Manager, Quotas | **CONFIGURED** (GCP integration mesh) |
| **Observability MCPs** | Distributed logs, operational metrics, telemetry traces | `google-cloud-logging`, `google-cloud-monitoring` | **CONFIGURED** (GCP Cloud Operations) |
| **Research MCPs** | Current technical research, academic papers, documentation | `arxiv`, `gemini-api-docs`, `duckduckgo-search` | **VERIFIED** (Official documentation engines) |

---

## 2. Server Class Specifications

### 1. GitHub MCP
- **SERVER**: `github`
- **PURPOSE**: Repository intelligence, code search, issue management, PR reviews, workflow CI/CD diagnostics.
- **OWNER**: Model Context Protocol Official (`@modelcontextprotocol/server-github`)
- **TRANSPORT**: `stdio` via `npx -y @modelcontextprotocol/server-github`
- **AUTH**: Environment Variable `GITHUB_PERSONAL_ACCESS_TOKEN` (verified user: `Hellthefox808`).
- **TOOLS**: `search_repositories`, `get_file_contents`, `list_commits`, `list_issues`, `create_pull_request`, `search_code`, etc.
- **WRITE CAPABILITIES**: Yes (Issue/PR creation, branch management).
- **DESTRUCTIVE CAPABILITIES**: Guarded (`merge_pull_request`, file deletion).
- **HEALTH CHECK**: Authenticated user query via GitHub API v3.
- **FAILURE MODE**: Fallback to local `git` CLI operations.
- **LAST VERIFIED**: 2026-09-16T09:46:54+05:30 (Status: HEALTHY).

---

### 2. Playwright / Browser MCP
- **SERVER**: `chrome-devtools-mcp` (paired with native `browser_subagent`)
- **PURPOSE**: End-to-end browser execution, visual UI validation, live DOM analysis, console message tracking, and network waterfall auditing.
- **OWNER**: Chrome DevTools MCP (`chrome-devtools-mcp`)
- **TRANSPORT**: `stdio` via `npx -y chrome-devtools-mcp`
- **AUTH**: Local Chrome debugging socket.
- **TOOLS**: `navigate_page`, `click`, `fill`, `take_screenshot`, `evaluate_script`, `lighthouse_audit`, `list_console_messages`, `list_network_requests`.
- **WRITE CAPABILITIES**: Browser DOM mutations.
- **DESTRUCTIVE CAPABILITIES**: None (Isolated to browser sandbox).
- **HEALTH CHECK**: `list_pages` or browser viewport initialize.
- **FAILURE MODE**: Native Headless Chromium fallback.
- **LAST VERIFIED**: Active.

---

### 3. Context7
- **SERVER**: `context7`
- **PURPOSE**: Fetch authoritative, real-time library documentation and code snippets for modern frameworks (FastAPI, React 19, Next.js 15, Tailwind CSS).
- **OWNER**: Context7 MCP Team (`https://mcp.context7.com/mcp`)
- **TRANSPORT**: `mcp-remote` over HTTPS via `npx -y mcp-remote@latest`
- **AUTH**: Public Gateway
- **TOOLS**: `resolve-library-id`, `query-docs`
- **WRITE CAPABILITIES**: None (Read-only documentation retrieval).
- **DESTRUCTIVE CAPABILITIES**: None.
- **HEALTH CHECK**: Call `resolve-library-id` with `FastAPI`.
- **FAILURE MODE**: Falls back to offline documentation in `docs/` and cached skills.
- **LAST VERIFIED**: 2026-09-16T10:09:14+05:30 (Status: HEALTHY).

---

### 4. MCP Inspector
- **SERVER**: `mcp-inspector`
- **PURPOSE**: Interactive inspection, protocol tracing, schema validation, and integration testing for all MCP servers.
- **OWNER**: Model Context Protocol Official (`@modelcontextprotocol/inspector`)
- **TRANSPORT**: CLI execution via `npx -y @modelcontextprotocol/inspector`
- **AUTH**: None (Local development tool)
- **TOOLS**: Protocol diagnostics & schema validation suite.
- **WRITE CAPABILITIES**: Test harness execution.
- **DESTRUCTIVE CAPABILITIES**: None.
- **HEALTH CHECK**: Local CLI invocation.
- **FAILURE MODE**: Manual schema inspection via JSON Schema validator.
- **LAST VERIFIED**: Available on demand.

---

### 5. Memory MCP
- **SERVER**: `memory`
- **PURPOSE**: Persistent semantic knowledge graph tracking entities, relationships, bug fixes, and architectural invariants.
- **OWNER**: Model Context Protocol Official (`@modelcontextprotocol/server-memory`)
- **TRANSPORT**: `stdio` via `npx -y @modelcontextprotocol/server-memory`
- **AUTH**: None (Local process memory)
- **TOOLS**: `create_entities`, `create_relations`, `add_observations`, `read_graph`, `search_nodes`, `open_nodes`.
- **WRITE CAPABILITIES**: Yes (Semantic graph nodes and edges).
- **DESTRUCTIVE CAPABILITIES**: `delete_entities`, `delete_observations`.
- **HEALTH CHECK**: Call `read_graph` or `search_nodes`.
- **FAILURE MODE**: Falls back to version-controlled `.agents/memory/` and Knowledge Items (`<appDataDir>/knowledge`).
- **LAST VERIFIED**: 2026-09-16T09:58:55+05:30 (Status: HEALTHY, 10 entities, 10 relations).

---

### 6. Git MCP
- **SERVER**: `git`
- **PURPOSE**: Native repository-level Git operations, branch management, diff auditing, and staging verification.
- **OWNER**: MCP Git Team (`mcp-server-git`)
- **TRANSPORT**: `stdio` via `uvx mcp-server-git`
- **AUTH**: Local repository permissions.
- **TOOLS**: `git_status`, `git_diff`, `git_diff_unstaged`, `git_diff_staged`, `git_commit`, `git_add`, `git_log`, `git_branch`.
- **WRITE CAPABILITIES**: Yes (Staging and committing).
- **DESTRUCTIVE CAPABILITIES**: Moderate (`git_reset`, `git_checkout`).
- **HEALTH CHECK**: `git_status` call on project path.
- **FAILURE MODE**: Local shell `git` CLI fallback.
- **LAST VERIFIED**: 2026-09-16T10:08:44+05:30 (Status: HEALTHY).

---

### 7. Filesystem MCP
- **SERVER**: `filesystem`
- **PURPOSE**: High-security, sandboxed filesystem reading, listing, and inspection.
- **OWNER**: Model Context Protocol Official (`@modelcontextprotocol/server-filesystem`)
- **TRANSPORT**: `stdio` via `npx -y @modelcontextprotocol/server-filesystem`
- **AUTH**: Host OS file ACLs.
- **TOOLS**: `read_file`, `write_file`, `list_directory`, `directory_tree`, `move_file`, `search_files`.
- **WRITE CAPABILITIES**: Yes (Constrained to permitted roots).
- **DESTRUCTIVE CAPABILITIES**: High (`write_file`, `move_file`).
- **HEALTH CHECK**: `list_directory` on `d:/HACKTHON/bhashasetu-ai/tests`.
- **FAILURE MODE**: Native Antigravity tools (`view_file`, `write_to_file`).
- **LAST VERIFIED**: 2026-09-16T10:08:54+05:30 (Status: HEALTHY).

---

### 8. PostgreSQL MCP
- **SERVER**: `alloydb-postgresql` & Docker PostgreSQL 18
- **PURPOSE**: Schema inspection, query execution, vector similarity auditing (`pgvector`), and migration testing.
- **OWNER**: Google Cloud & PostgreSQL Community
- **TRANSPORT**: Google Credentials Auth Provider / Local TCP 5432
- **TOOLS**: `execute_sql`, `execute_sql_read_only`, `list_tables`, `get_table_info`.
- **WRITE CAPABILITIES**: Yes.
- **DESTRUCTIVE CAPABILITIES**: High (Schema modification). Guarded by read-only defaults.
- **HEALTH CHECK**: `pg_isready` probe in Docker healthcheck.
- **FAILURE MODE**: Direct `psql` command execution inside Docker container.
- **LAST VERIFIED**: Verified in Compose mesh test suite.

---

### 9. Sequential Thinking MCP
- **SERVER**: `sequential-thinking`
- **PURPOSE**: Dynamic multi-stage problem-solving, hypothesis evaluation, root-cause decomposition, and plan verification.
- **OWNER**: Model Context Protocol Official (`@modelcontextprotocol/server-sequential-thinking`)
- **TRANSPORT**: `stdio` via `npx -y @modelcontextprotocol/server-sequential-thinking`
- **AUTH**: None (Local computation)
- **TOOLS**: `sequentialthinking`
- **WRITE CAPABILITIES**: Thought branch graph tracking.
- **DESTRUCTIVE CAPABILITIES**: None.
- **HEALTH CHECK**: Single-thought ping execution with `nextThoughtNeeded: false`.
- **FAILURE MODE**: Internal agent cognitive planning.
- **LAST VERIFIED**: 2026-09-16T10:08:27+05:30 (Status: HEALTHY).

---

### 10. Cloud Infrastructure MCPs
- **SERVERS**: `google-compute-engine`, `bigquery`, `google-cloud-firestore`, `google-cloud-pubsub`, `google-cloud-resource-manager`, `google-cloud-quotas`
- **PURPOSE**: Cloud infrastructure provisioning, cloud quota inspection, distributed messaging, and warehouse analytics.
- **OWNER**: Google Cloud Platform
- **AUTH**: Application Default Credentials (ADC) / `google_credentials`
- **FAILURE MODE**: Fallback to local Docker Compose & Kubernetes test harnesses.
- **LAST VERIFIED**: Active in configuration mesh.

---

### 11. Observability MCPs
- **SERVERS**: `google-cloud-logging`, `google-cloud-monitoring`
- **PURPOSE**: Live log query analysis, metric alerting, time-series aggregation, and system telemetry auditing.
- **OWNER**: Google Cloud Platform
- **AUTH**: `google_credentials`
- **TOOLS**: `list_log_entries`, `list_timeseries`, `query_range`, `get_dashboard`.
- **FAILURE MODE**: Local logging via Python `logging` and NestJS structured JSON logger.
- **LAST VERIFIED**: Active in configuration mesh.

---

### 12. Research MCPs
- **SERVERS**: `arxiv`, `gemini-api-docs`, `pubmed-database`, `wikipedia`
- **PURPOSE**: Technical research, AI/ML literature extraction, NLP benchmark validation, and upstream documentation search.
- **OWNER**: Scientific & Official Documentation Providers
- **TOOLS**: `gemini_search_docs`, `gemini_get_doc`, `search_papers`, `get_abstract`.
- **FAILURE MODE**: Native web search (`search_web`).
- **LAST VERIFIED**: 2026-09-16T10:09:50+05:30.
