# VoxBridge AI — Chaos Engineering & Resilience Testing

## 1. Chaos Engineering Principles & Tooling

VoxBridge utilizes **Chaos Mesh** and **LitmusChaos** to continuously inject production-like hardware, network, and provider faults into staging environments, verifying that the system degrades gracefully without dropping live voice sessions.

---

## 2. Injected Failure Scenarios & Verification Assertions

| Fault ID | Injected Failure Type | Target Subsystem | Expected System Reaction | Pass / Fail Verification Assertion |
|---|---|---|---|---|
| **CHAOS-01** | Primary STT Provider 10-second timeout | Self-Hosted Whisper GPU Pod | Circuit breaker trips within 400ms; routes audio to secondary Conformer worker. | Session continues; client experiences $< 250\text{ms}$ latency increase; zero dropped calls. |
| **CHAOS-02** | External Provider HTTP 429 Rate Limit | Azure / Google Speech API | Provider Gateway marks provider `UNAVAILABLE` for 60s; shifts to AWS Polly/DeepL. | 100% of in-flight requests fulfilled by fallback; 0 errors returned to client. |
| **CHAOS-03** | Abrupt GPU Node Termination (`node drain --force`)| EKS GPU NodePool | Kubernetes terminates pod; client socket receives `1012 Service Restart`. | Client SDK auto-reconnects to healthy node within 1,200ms; resumes stream from sequence checkpoint. |
| **CHAOS-04** | Redis Shard Master Crash (`SIGKILL`) | Redis 7.4 Cluster Shard 2 | Redis Sentinel/Raft promotes replica to master in $< 3.5\text{ seconds}$. | Rate limit counters gracefully allow traffic during partition; zero session crashes. |
| **CHAOS-05** | PostgreSQL Primary DB Failover | AWS Aurora Multi-AZ | Aurora promotes Read Replica to Writer within 15 seconds. | PgBouncer queues write queries; read queries continue uninterrupted; 0 failed jobs. |
| **CHAOS-06** | Packet Loss & High Jitter | Streaming Gateway Ingress | $20\%$ packet drop and 150ms jitter injected via Linux `tc netem`. | Jitter buffer expands to 250ms; drops non-voiced frames; audio remains intelligible. |
| **CHAOS-07** | Slow Consumer / Socket Starvation | Connected WebSocket Client | Client pauses TCP reads; server ring buffer fills to $> 85\%$ capacity. | Backpressure controller trips: drops partial transcripts; preserves structural boundaries; memory stays bounded. |

---

## 3. Sample Chaos Experiment: GPU Pod Termination

```yaml
# tests/chaos/gpu_worker_pod_kill.yaml
apiVersion: chaos-mesh.org/v1alpha1
kind: PodChaos
metadata:
  name: gpu-stt-worker-abrupt-termination
  namespace: voxbridge-core
spec:
  action: pod-kill
  mode: fixed
  value: "2" # Kill 2 GPU inference pods simultaneously
  selector:
    namespaces:
      - voxbridge-core
    labelSelectors:
      app: stt-inference-worker
  scheduler:
    cron: "@every 15m"
  duration: "5m"
```

### 3.1 Automated Post-Chaos Health Check
A continuous Prometheus monitoring probe asserts that during the 5-minute chaos injection window:
- `sum(rate(vox_session_fatal_errors_total[1m])) == 0`
- `sum(rate(vox_provider_fallback_total[1m])) > 0` (Confirming fallback triggered)
- Cluster GPU pods are re-provisioned and report healthy within 45 seconds.
