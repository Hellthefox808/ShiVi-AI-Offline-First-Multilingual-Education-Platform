# VoxBridge AI — Distributed In-Memory Cache Architecture

## 1. Cache Cluster Topology (Mermaid Diagram 14)

VoxBridge AI deploys **Redis 7.4 Cluster** across 6 primary master shards and 6 read replicas distributed across 3 Availability Zones, providing sub-millisecond key-value lookups, atomic sliding-window rate limiting, and real-time session registration.

```mermaid
graph TB
    subgraph Clients["Platform Service Clients"]
        APIGW["API Gateway (Auth & Rate Limits)"]
        StreamGW["Streaming Gateway (Session Registration)"]
        StreamOrch["Stream Orchestrator (Session Context)"]
        BillingSrv["Metering Worker (Quota Deductions)"]
    end

    subgraph RedisCluster["Redis 7.4 Cluster (6 Shards, 16,384 Hash Slots)"]
        subgraph AZ_A["Availability Zone A"]
            Shard1_M["Shard 1 Master (Slots 0 - 2730)"]
            Shard2_R["Shard 2 Replica"]
        end
        subgraph AZ_B["Availability Zone B"]
            Shard2_M["Shard 2 Master (Slots 2731 - 5460)"]
            Shard3_R["Shard 3 Replica"]
        end
        subgraph AZ_C["Availability Zone C"]
            Shard3_M["Shard 3 Master (Slots 5461 - 8192)"]
            Shard1_R["Shard 1 Replica"]
        end
    end

    subgraph Persistence["Cache Persistence & Snapshotting"]
        AOF["Append-Only File (AOF: fsync everysec)"]
        RDB["RDB Snapshot (Every 15 min)"]
    end

    APIGW -->|Hash Slot Routing (CRC16)| Shard1_M
    StreamGW -->|Hash Slot Routing| Shard2_M
    StreamOrch -->|Hash Slot Routing| Shard3_M
    BillingSrv -->|Hash Slot Routing| Shard1_M

    Shard1_M -.->|Asynchronous Replication| Shard1_R
    Shard2_M -.->|Asynchronous Replication| Shard2_R
    Shard3_M -.->|Asynchronous Replication| Shard3_R

    Shard1_M --> AOF
    Shard1_M --> RDB
```

---

## 2. Cache Namespace Catalog & TTL Matrix

To prevent key collision and ensure zero cross-tenant contamination, all Redis keys are structured using strict, colon-delimited hierarchical namespaces with deterministic expiration policies:

| Key Pattern | Data Structure | Purpose | TTL | Eviction Policy Group |
|---|---|---|---|---|
| `auth:key:{key_hash}` | `STRING (JSON)` | Fast API key lookup and permission scopes | 60 Seconds | `volatile-lru` |
| `rl:{tenant_id}:{window_epoch}`| `HASH` | Sliding-window request counter | 120 Seconds | `volatile-lru` |
| `sess:{session_id}:meta` | `HASH` | Active session state and linguistic parameters | 4 Hours | `volatile-lru` |
| `sess:{session_id}:conn` | `STRING` | Gateway Pod ID where WebSocket is bound | 30 Seconds | `volatile-lru` |
| `quota:{tenant_id}:current` | `HASH` | Real-time concurrent stream counters | Permanent | `noeviction` |
| `provider:health:{provider_id}` | `HASH` | Circuit breaker failure count & state | 60 Seconds | `volatile-lru` |
| `lock:session:{session_id}` | `STRING` | Distributed lock for turn-taking state | 5 Seconds | `volatile-lru` |

---

## 3. Atomic Sliding-Window Rate Limiting (Redis Lua)

To prevent boundary burst exploits common in standard fixed-window counters, VoxBridge executes an atomic Lua script running directly inside Redis:

```lua
-- KEYS[1]: Rate limit key (e.g., rl:org_01J8ABC:requests)
-- ARGV[1]: Current Unix millisecond timestamp
-- ARGV[2]: Window size in milliseconds (e.g., 60000 for 1 minute)
-- ARGV[3]: Maximum permitted requests within the window (e.g., 1000)

local key = KEYS[1]
local now = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local limit = tonumber(ARGV[3])
local clearBefore = now - window

-- 1. Remove timestamps outside the active sliding window
redis.call('ZREMRANGEBYSCORE', key, 0, clearBefore)

-- 2. Fetch the current count of elements in the set
local currentRequests = redis.call('ZCARD', key)

-- 3. Check threshold
if currentRequests < limit then
    -- Add the current unique request timestamp to the sorted set
    redis.call('ZADD', key, now, now)
    redis.call('PEXPIRE', key, window)
    return {1, limit - currentRequests - 1} -- Allowed: {true, remaining}
else
    return {0, 0} -- Rejected: {false, 0}
end
```

---

## 4. Distributed Locking & Race-Condition Safeguards (Redlock)

When coordinating multi-party session updates (e.g., two participants speaking simultaneously in a meeting), the system acquires an ephemeral distributed lock:
- **Lock Acquisition:** `SET lock:session:{id} {node_token} NX PX 5000` (5-second auto-release).
- **Safe Release (Atomic Lua):** Guarantees that a slow worker node does not release a lock that has already expired and been acquired by another pod:
  ```lua
  if redis.call("GET", KEYS[1]) == ARGV[1] then
      return redis.call("DEL", KEYS[1])
  else
      return 0
  end
  ```
