# 35 — NETWORK ARCHITECTURE, CIDR BLOCKS & FIREWALL GUARDS

> **Document ID:** `BS-ARCH-35-NETWORK-ARCH`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** Zero-Trust Segmented Network Architecture (§30 Master Standard)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Virtual Private Cloud (VPC) Subnet Allocation

The production cloud network isolates workloads across three private security zones within `10.0.0.0/16`:

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                           VPC CIDR ALLOCATION MAP                           │
├────────────────────┬────────────────────┬───────────────┬───────────────────┤
│ Subnet Name        │ CIDR Block Range   │ Zone Type     │ Attached Services │
├────────────────────┼────────────────────┼───────────────┼───────────────────┤
│ `subnet-ingress`   │ 10.0.1.0/24        │ Public / NAT  │ Cloud LB, Cloud   │
│                    │ (254 host IPs)     │ Internet Edge │ NAT Gateway.      │
├────────────────────┼────────────────────┼───────────────┼───────────────────┤
│ `subnet-apps`      │ 10.0.10.0/24       │ Private Only  │ GKE Workloads     │
│                    │ (254 host IPs)     │ (No Public IP)│ (Backend, AI, Web)│
├────────────────────┼────────────────────┼───────────────┼───────────────────┤
│ `subnet-data`      │ 10.0.20.0/24       │ Private Only  │ PostgreSQL 18 HA, │
│                    │ (254 host IPs)     │ Isolated DB   │ Redis 7.4 Cluster.│
└────────────────────┴────────────────────┴───────────────┴───────────────────┘
```

---

## 2. Firewall Rules & Network Access Control Lists (NACL)

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                       FIREWALL SECURITY RULES MATRIX                        │
├──────┬────────────────────┬───────────┬──────────────┬──────────────────────┤
│ Rule │ Source CIDR        │ Dest CIDR │ Port / Proto │ Permitted Action     │
├──────┼────────────────────┼───────────┼──────────────┼──────────────────────┤
│ FW-01│ 0.0.0.0/0 (Public) │ Ingress LB│ TCP 443 / 80 │ ALLOW (Public Web)   │
│ FW-02│ Ingress LB         │ subnet-app│ TCP 3000-3001│ ALLOW (Reverse Proxy)│
│ FW-03│ subnet-app         │ subnet-app│ TCP 8000     │ ALLOW (Backend -> AI)│
│ FW-04│ subnet-app         │ subnet-data│ TCP 5432    │ ALLOW (App -> Postgres)
│ FW-05│ subnet-app         │ subnet-data│ TCP 6379    │ ALLOW (App -> Redis) │
│ FW-06│ 0.0.0.0/0 (Public) │ subnet-data│ ALL PORTS    │ DENY (Zero Ingress)  │
│ FW-07│ subnet-data        │ 0.0.0.0/0 │ ALL PORTS    │ DENY (Zero Egress)   │
└──────┴────────────────────┴───────────┴──────────────┴──────────────────────┘
```

---

## 3. Local Development Bridge Network (`bhashasetu-mesh`)

In local development (`infra/docker-compose.yml`), container networking uses an isolated Docker user-defined bridge network:
* **Driver**: `bridge`
* **Subnet**: Automatic internal assignment (`172.28.0.0/16`)
* **DNS Resolution**: Automatic container-name DNS resolution (`http://postgres:5432`, `http://redis:6379`, `http://ai-platform:8000`).
* **Isolation**: Containers cannot be directly reached by external networks except through explicitly exposed host ports (`80`, `3001`, `8000`).
