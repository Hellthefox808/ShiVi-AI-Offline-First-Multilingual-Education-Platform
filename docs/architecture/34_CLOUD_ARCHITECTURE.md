# 34 — CLOUD ARCHITECTURE & SOVEREIGN HOSTING BLUEPRINT

> **Document ID:** `BS-ARCH-34-CLOUD-ARCH`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Deployment Target:** Google Cloud Platform (GCP) / National Sovereign Cloud (MeitY Approved)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Cloud Provider & Sovereign Data Center Strategy

To comply with India's **Digital Personal Data Protection (DPDP) Act 2023** and Government of India Ministry of Electronics and Information Technology (MeitY) standards, all cloud infrastructure resides within the **India Sovereign Data Boundary** (`asia-south1` / Mumbai or `asia-south2` / Delhi):

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                       SOVEREIGN HOSTING BOUNDARY                            │
├────────────────────┬────────────────────┬───────────────────────────────────┤
│ Infrastructure Tier│ Target Platform    │ Configuration & SLA               │
├────────────────────┼────────────────────┼───────────────────────────────────┤
│ Primary Region     │ GCP asia-south1    │ Multi-Zone High Availability      │
│                    │ (Mumbai, India)    │ (Zones a, b, c). SLA: 99.95%      │
├────────────────────┼────────────────────┼───────────────────────────────────┤
│ DR / Backup Region │ GCP asia-south2    │ Asynchronous replica & snapshot   │
│                    │ (Delhi, India)     │ archive. RPO: < 15m, RTO: < 30m   │
├────────────────────┼────────────────────┼───────────────────────────────────┤
│ CDN & Edge Pop     │ Cloudflare / Fastly│ India-wide caching of static APKs,│
│                    │ Edge Locations     │ fonts, and offline package zips.  │
└────────────────────┴────────────────────┴───────────────────────────────────┘
```

---

## 2. Cloud Infrastructure Resource Topology

```mermaid
graph TD
    subgraph Cloudflare["Edge Network & CDN (India POPs)"]
        DNS["Cloud DNS (bhashasetu.in)"]
        CDN["Edge Cache (Static Bundles & APKs)"]
        WAF["Web Application Firewall (DDoS Guard)"]
    end

    subgraph GCP["Google Cloud Platform (Region: asia-south1)"]
        subgraph VPC["bhashasetu-vpc (10.0.0.0/16)"]
            subgraph PublicSubnet["Public Ingress Subnet (10.0.1.0/24)"]
                LoadBalancer["Cloud HTTP(S) Load Balancer<br/>(External IP, SSL Termination)"]
                NATGateway["Cloud NAT Gateway (Outbound Egress)"]
            end

            subgraph AppSubnet["Private Application Subnet (10.0.10.0/24)"]
                GKE["GKE Cluster (bhashasetu-prod-gke)<br/>• Web Backend (NestJS 11)<br/>• AI Platform (FastAPI + GPU/CPU)<br/>• Web Frontend (Next.js 16.3)"]
            end

            subgraph DataSubnet["Private Isolated Data Subnet (10.0.20.0/24)"]
                CloudSQL[("PostgreSQL 18 + pgvector<br/>(HA Regional Primary + Standby)")]
                CloudRedis[("Memorystore for Redis 7.4<br/>(BullMQ Task Queues & Cache)")]
            end
        end

        CloudStorage[("Cloud Storage (GCS Multi-Regional)<br/>bhashasetu-packages-in")]
    end

    Users["Teachers & Tablets"] --> DNS
    DNS --> WAF
    WAF --> LoadBalancer
    LoadBalancer --> GKE
    GKE --> CloudSQL
    GKE --> CloudRedis
    GKE --> CloudStorage
    GKE --> NATGateway
    NATGateway -->|External Model Calls| ExternalAPIs["Bhashini / Gemini"]
```
