# Model Context Protocol (MCP) Governance & Execution Policy

**Specification Compliance**: MCP 2026-07-28 Release  
**Architecture**: Stateless Request/Response Core, Header Routing, Cacheable Metadata, Client ID Authorization

---

## 1. Governance Architecture

All MCP interactions within BhashaSetu AI must adhere to the configured `.agents/mcp/registry.yaml` and `.agents/mcp/policies.yaml`.

```
CONVERSATION HISTORY != MEMORY
MEMORY != KNOWLEDGE BASE
KNOWLEDGE BASE != TOOL STATE
TOOL STATE != EXECUTION LOG
```

- **Stateless Protocol Core**: Do not assume stateful session affinity. All requests must provide complete context and support arbitrary load balancing.
- **Cacheable Metadata**: Respect `ttlMs` and `cacheScope` in `tools/list`, `resources/list`, and `prompts/list`. Avoid rediscovering static tools on each agent loop.
- **Header-Based Routing**: Expose method names and routing parameters in HTTP headers when querying remote MCP proxies.

---

## 2. Least Privilege & Tool Scoping

- Subagents must receive only the explicit subset of tools required for their bounded objective (`enabled_tools`).
- **Deny Policies Override Everything**:
  - `Deny` > `Ask` > `Allow`.
  - Mutating operations (`merge_pull_request`, database writes, file deletions) require interactive approval (`Ask`).
- **Network Boundaries & SSRF Defense**:
  - Web fetching tools (e.g. Fetch, URL content readers) are strictly prohibited from resolving loopback (`127.0.0.1`, `::1`) or private RFC-1918 subnets (`10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`).

---

## 3. Real MCP Server Validation Pipeline

Whenever verifying or introducing an MCP server:
```
DISCOVER -> VERIFY SOURCE -> VERIFY VERSION -> VERIFY AUTHENTICATION -> 
VERIFY BOUNDARIES -> CONNECT -> LIST TOOLS -> VALIDATE SCHEMAS -> 
READ-ONLY TEST -> RECORD HEALTH
```
Never report an MCP server as operational without a successful health probe and schema validation.
