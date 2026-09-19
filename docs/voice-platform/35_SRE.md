# VoxBridge AI — Site Reliability Engineering (SRE) & Operational Framework

## 1. Incident Severity Matrix & Response Protocols

VoxBridge AI operates under a 24/7/365 follow-the-sun Site Reliability Engineering on-call rotation with strict Mean Time to Acknowledge (MTTA) and Mean Time to Resolve (MTTR) targets:

| Severity Level | Definition & Operational Impact | MTTA Target | MTTR Target | Escalation & Coordination Protocol |
|---|---|---|---|---|
| **SEV-1 (Critical)** | Core API / Streaming Gateway down ($> 5\%$ global traffic impacted), active data breach, or silent billing corruption. | $\le 5 \text{ Minutes}$ | $\le 45 \text{ Minutes}$ | Automatic PagerDuty blast to Primary + Secondary SRE + VP Eng; dedicated incident war room; status page updated every 15m. |
| **SEV-2 (Major)** | Regional edge outage, self-hosted GPU inference down (cloud fallback absorbing traffic), p95 latency $> 2\text{x}$ SLO. | $\le 15 \text{ Minutes}$ | $\le 2 \text{ Hours}$ | Primary SRE paged; domain lead engineer engaged; status page updated with advisory. |
| **SEV-3 (Moderate)** | Non-blocking batch worker lag $> 10\text{k}$ jobs, customer webhook retry backlog, single enterprise tenant rate-limit bug. | $\le 60 \text{ Minutes}$ | $\le 8 \text{ Hours}$ | Jira ticket automatically generated; notified via `#alerts-sre-ops` Slack channel during business hours. |
| **SEV-4 (Low)** | Minor dashboard UI glitch, documentation error, non-impacting telemetry scrape failure. | Next Business Day | $\le 5 \text{ Days}$ | Triaged in regular team sprint planning. |

---

## 2. On-Call Rotations & Fatigue Prevention

To prevent engineer burnout and ensure alert signals are actionable:
1. **Two-Tier Rotation:** Primary On-Call (handles pages and active mitigations) and Secondary On-Call (handles escalations, queries, and backup).
2. **Weekly Shift Handoff:** Shifts rotate every Wednesday at 14:00 UTC with an hour-long synchronous review of alerts, near-misses, and ongoing remediation work.
3. **Alert Actionability Mandate:** Every PagerDuty alert must link directly to a specific, tested Markdown **Runbook**. If an alert fires without requiring an immediate human action, it is categorized as noise, stripped of paging priority, and converted to a dashboard metric.

---

## 3. Error Budget Policies & Deployment Halts

Error budgets represent the contractual agreement between Product Management and Platform SRE:

$$\text{Error Budget} = 1.0 - \text{SLO Target} \quad (\text{e.g., for } 99.95\% \text{ SLO, Budget} = 0.05\% \text{ allowable failure})$$

### 3.1 Error Budget Burn Rate Rules
- **Fast Burn (14.4x rate — 2% budget consumed in 1 hour):** PagerDuty Sev-1 page; active deployments halted automatically in ArgoCD.
- **Slow Burn (3.6x rate — 10% budget consumed in 24 hours):** Slack Sev-2 notification; SRE initiates investigation into intermittent provider timeouts.
- **Budget Exhaustion Policy:** If a service consumes $100\%$ of its 30-day rolling error budget:
  1. All new feature deployments to production are automatically frozen.
  2. Engineering effort shifts $100\%$ to stability, resilience engineering, and bug remediation for the subsequent 14-day cycle.

---

## 4. Blameless Post-Mortem Standard

Every Sev-1 and Sev-2 incident requires a published **Blameless Post-Mortem** within 72 hours of incident resolution:
1. **Timeline:** High-resolution chronological log of events from initial failure injection to detection, escalation, mitigation, and permanent fix.
2. **Root Cause Analysis (5 Whys):** Deep systemic inquiry avoiding individual blame; identifies gaps in testing, circuit breaking, monitoring, or architecture.
3. **Action Items:** Trackable Jira tickets assigned to specific owners with strict 30-day deadlines to prevent recurrence.
