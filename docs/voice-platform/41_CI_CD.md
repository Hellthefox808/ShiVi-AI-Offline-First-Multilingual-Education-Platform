# VoxBridge AI — CI/CD Pipeline & Progressive GitOps Delivery

## 1. CI/CD Pipeline Flow (Mermaid Diagram 19)

VoxBridge AI enforces an automated, security-hardened delivery pipeline combining GitHub Actions for continuous integration and **ArgoCD + Flagger** for progressive canary GitOps deployments.

```mermaid
graph TB
    subgraph CommitStage["1. Code Commit & Quality Gate"]
        Developer["Developer Git Push (Feature Branch / PR)"]
        Linter["GolangCI-Lint / Ruff / Clippy"]
        UnitTests["Unit Tests & Coverage Check (>= 85% Required)"]
        ContractTests["Pact Contract Tests (API & Microservices)"]
        
        Developer --> Linter
        Linter --> UnitTests
        UnitTests --> ContractTests
    end

    subgraph BuildAndSecurity["2. Build & Supply Chain Security"]
        DockerBuild["Multi-Stage Distroless Docker Build"]
        TrivyScan["Trivy Vulnerability Scan (CVE CVSS >= 7.0 Fails Build)"]
        SyftSBOM["Syft SBOM Generation (CycloneDX Format)"]
        CosignSign["Cosign Cryptographic Signature & Attestation"]

        ContractTests --> DockerBuild
        DockerBuild --> TrivyScan
        TrivyScan --> SyftSBOM
        SyftSBOM --> CosignSign
    end

    subgraph RegistryStage["3. Container Registry & GitOps Trigger"]
        ECR["AWS ECR Private Container Registry"]
        GitOpsRepo["GitOps Manifests Repo (Config Update)"]
        
        CosignSign --> ECR
        CosignSign --> GitOpsRepo
    end

    subgraph DeploymentStage["4. Progressive Canary Delivery (ArgoCD + Flagger)"]
        ArgoCD["ArgoCD Sync (Detects Git Commit)"]
        Flagger["Flagger Canary Controller"]
        Canary_10["Canary Step 1: 10% Traffic (Wait 5m)"]
        Canary_50["Canary Step 2: 50% Traffic (Wait 5m)"]
        Canary_100["Canary Step 3: 100% Full Promotion"]
        AutoRollback["Automated Instant Rollback (5xx > 0.05% OR Latency > 1.5x)"]

        GitOpsRepo --> ArgoCD
        ArgoCD --> Flagger
        Flagger --> Canary_10
        Canary_10 -->|Prometheus Metric Checks Pass| Canary_50
        Canary_50 -->|Prometheus Metric Checks Pass| Canary_100
        Canary_10 -.->|Metric Threshold Breached| AutoRollback
        Canary_50 -.->|Metric Threshold Breached| AutoRollback
    end
```

---

## 2. Quality & Security Gates in CI

Every pull request must successfully pass the following non-negotiable automated gates before merge approval:

1. **Test Coverage Barrier:** CI fails if global unit and integration test coverage drops below **85%**.
2. **Deterministic Contract Testing (Pact):** The API Gateway contract with internal services is verified via Pact tests. Breaking Protobuf or JSON schema changes fail the build before deployment.
3. **Container Security Gate (Trivy):**
   ```yaml
   - name: Run Trivy Vulnerability Scanner
     uses: aquasecurity/trivy-action@master
     with:
       image-ref: ${{ env.IMAGE_TAG }}
       format: 'table'
       exit-code: '1' # Fails CI on finding
       ignore-unfixed: true
       severity: 'CRITICAL,HIGH'
   ```
4. **Supply Chain Attestation (Cosign):** Images pushed to ECR are cryptographically signed using GitHub Actions OIDC identity. The Kyverno policy running in Kubernetes blocks any pod whose image lacks a valid Cosign signature.

---

## 3. Flagger Progressive Canary Configuration

```yaml
apiVersion: flagger.app/v1beta1
kind: Canary
metadata:
  name: streaming-gateway-canary
  namespace: voxbridge-core
spec:
  targetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: streaming-gateway
  service:
    port: 8443
    targetPort: 8443
  analysis:
    interval: 1m
    threshold: 3 # Rollback after 3 consecutive failed checks
    maxWeight: 50
    stepWeight: 10
    metrics:
      - name: request-success-rate
        thresholdRange:
          min: 99.95
        interval: 1m
      - name: p95-latency
        thresholdRange:
          max: 0.850 # Fail if TTFA exceeds 850ms
        interval: 1m
```
