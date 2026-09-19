# VoxBridge AI — Site Reliability Engineering Operational Runbooks

## Runbook Index
1. `RB-01`: Third-Party Provider Outage / High Latency Failover
2. `RB-02`: GPU Inference CUDA Out-of-Memory (OOM) Crash Loop
3. `RB-03`: Streaming Gateway Socket & Jitter Buffer Memory Congestion
4. `RB-04`: Redis Cluster Shard Saturation & Failover
5. `RB-05`: NATS JetStream Consumer Lag Spike

---

## RB-01: Third-Party Provider Outage / High Latency Failover

- **Severity:** Sev-2 (or Sev-1 if global fallback exhausted).
- **Triggering Alerts:** `ProviderLatencyExceeded`, `ProviderErrorRateSpike (>10%)`.
- **Symptoms:** P95 translation latency exceeds 1.5s; client logs show `provider_fallback_triggered` warnings.

### Immediate Triage Commands
```bash
# 1. Inspect live provider error rates and circuit breaker states
curl -s http://provider-gateway.voxbridge-core.svc:9095/metrics | grep "vox_provider_circuit_breaker_state"

# 2. View recent provider gateway error logs
kubectl logs -n voxbridge-core -l app=provider-gateway --tail=100 | grep -E "TIMEOUT|5xx|CIRCUIT_OPEN"
```

### Mitigation Procedure
1. Force an administrative circuit breaker trip to immediately bypass the degraded provider:
   ```bash
   # Trip circuit breaker via Admin API
   curl -X POST https://api.voxbridge.internal/v1/admin/providers/azure_speech/trip \
     -H "Authorization: Bearer $ADMIN_SECRET" \
     -d '{"duration_minutes": 60, "reason": "Vendor regional latency degradation"}'
   ```
2. Verify traffic shifts cleanly to the next healthy provider (e.g., Google Cloud or self-hosted GPU):
   ```bash
   kubectl logs -n voxbridge-core -l app=provider-gateway --tail=50 | grep "provider.routed"
   ```
3. Update `status.voxbridge.ai` with an advisory notice.

---

## RB-02: GPU Inference CUDA Out-of-Memory (OOM) Crash Loop

- **Severity:** Sev-2.
- **Triggering Alerts:** `CUDAOutOfMemoryAlert`, `KubePodCrashLooping (stt-worker)`.
- **Symptoms:** Whisper STT pods terminating with exit code 137; GPU memory usage pinned at 100%.

### Immediate Triage Commands
```bash
# 1. Identify crashing inference pods
kubectl get pods -n voxbridge-core -l app=stt-inference-worker -o wide

# 2. Check NVIDIA GPU VRAM allocation on the node
kubectl exec -it <pod-name> -n voxbridge-core -- nvidia-smi

# 3. Inspect container termination reason
kubectl describe pod <pod-name> -n voxbridge-core | grep -A 5 "Last State"
```

### Mitigation Procedure
1. Lower dynamic batch size cap temporarily via runtime configuration:
   ```bash
   kubectl patch configmap inference-runtime-config -n voxbridge-core \
     --type merge -p '{"data":{"MAX_DYNAMIC_BATCH_SIZE":"8"}}'
   ```
2. Restart the deployment to pick up the reduced batch cap:
   ```bash
   kubectl rollout restart deployment/stt-inference-worker -n voxbridge-core
   ```
3. Karpenter will automatically provision fresh GPU nodes to handle the reduced batch throughput.

---

## RB-03: Streaming Gateway Socket & Jitter Buffer Congestion

- **Severity:** Sev-2.
- **Triggering Alerts:** `HighDroppedAudioFrames`, `PodMemoryNearLimit (>85%)`.
- **Symptoms:** Streaming gateway pods running low on memory due to slow mobile clients buffering un-acked packets.

### Mitigation Procedure
1. Trigger aggressive server-side drop policy on non-voiced frames:
   ```bash
   curl -X POST http://streaming-gateway.voxbridge-core.svc:8080/admin/backpressure/tighten
   ```
2. Scale out gateway replicas immediately:
   ```bash
   kubectl scale deployment/streaming-gateway -n voxbridge-core --replicas=36
   ```

---

## RB-04: Redis Cluster Shard Saturation & Failover

- **Severity:** Sev-1.
- **Triggering Alerts:** `RedisMemoryHigh (>90%)`, `RedisClusterNodeDown`.
- **Symptoms:** API rate limiter returning 500 errors; session token lookups timing out.

### Mitigation Procedure
1. Connect to Redis Cluster and inspect memory per shard:
   ```bash
   redis-cli -h redis-cluster.voxbridge-core.svc -p 6379 cluster nodes
   redis-cli -h redis-cluster.voxbridge-core.svc -p 6379 info memory
   ```
2. Evict volatile rate limit caches:
   ```bash
   redis-cli -h redis-cluster.voxbridge-core.svc -p 6379 EVAL "return redis.call('DEL', unpack(redis.call('KEYS', 'rl:*')))" 0
   ```
3. If a shard master is unresponsive, initiate manual failover on its replica:
   ```bash
   redis-cli -h <replica-ip> -p 6379 CLUSTER FAILOVER TAKEOVER
   ```

---

## RB-05: NATS JetStream Consumer Lag Spike

- **Severity:** Sev-3.
- **Triggering Alerts:** `NatsConsumerLagHigh (>10,000 messages)`.
- **Symptoms:** Billing usage records lagging behind real-time; customer dashboards delayed by several minutes.

### Mitigation Procedure
1. Check JetStream stream state and consumer lag:
   ```bash
   nats stream info VOX_METERING
   nats consumer info VOX_METERING billing-aggregator-group
   ```
2. Scale up the metering worker consumer group:
   ```bash
   kubectl scale deployment/usage-metering-daemon -n voxbridge-core --replicas=8
   ```
3. Verify lag drops toward zero:
   ```bash
   watch -n 2 "nats consumer info VOX_METERING billing-aggregator-group | grep 'Num Pending'"
   ```
