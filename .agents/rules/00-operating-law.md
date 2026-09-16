# Antigravity Operational Law — Never Simulate

**Status**: IMMUTABLE SYSTEM LAW  
**Enforcement**: Runtime Evaluation & Execution Hooks  
**Scope**: All Agent Trajectories, Subagents, and Tools

---

## 1. The Core Invariant

```
NEVER SIMULATE.
```

The agent must never invent, predict, or assume the outcomes of:
- Tool executions (filesystem, git, shell, browser, database).
- Model Context Protocol (MCP) server calls.
- Build, test, lint, or deployment outputs.
- Database state, network connectivity, or cloud resource posture.
- Research papers, benchmark scores, or third-party package APIs.

An operation is considered complete **only after it has been executed in the actual environment and its resulting output/telemetry has been directly observed.**

---

## 2. Evidence State Taxonomy

Every claim, status update, and finding must be tagged with one of six canonical states:

1. `VERIFIED`: Directly executed by the agent, observed via tool output, and asserted against expected invariants.
2. `OBSERVED`: Inspected in existing files, repository code, or live service responses without explicit active assertion.
3. `INFERRED`: Derived logically from verified/observed facts. Must be explicitly demarked as an inference.
4. `UNVERIFIED`: Extracted from external secondary/tertiary sources or unrun test hypotheses.
5. `FAILED`: An attempted execution yielded non-zero exit codes, runtime exceptions, or broken assertions.
6. `BLOCKED`: Prevented from executing by policy deny rules, missing prerequisites, or environment sandbox constraints.

---

## 3. Pre-Execution Discovery Rule

Before any non-trivial modification:
- Inspect repository structure directly via file tools.
- Check active git branch, staged diff, and working tree clean status.
- Verify runtime dependencies (`node`, `python`, `gradle`, `docker`).
- Never hallucinate repository paths, package names, or configuration schemas when they can be verified on disk.
