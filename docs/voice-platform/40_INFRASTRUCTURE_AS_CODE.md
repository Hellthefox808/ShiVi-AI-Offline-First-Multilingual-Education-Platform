# VoxBridge AI — Infrastructure as Code (Terraform / OpenTofu)

## 1. IaC Principles & Module Architecture

All physical and virtual cloud resources in VoxBridge AI are defined declaratively via **OpenTofu / Terraform (v1.8+)**. Manual changes via AWS or Cloudflare web consoles are strictly forbidden and overwritten by automated drift remediation.

```
infra/terraform/
├── environments/
│   ├── production/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   ├── terraform.tfvars
│   │   └── backend.tf
│   └── staging/
├── modules/
│   ├── network/          # VPC, Subnets, NAT, Route Tables, Transit Gateway
│   ├── eks_cluster/      # EKS Control Plane, OIDC, Karpenter, Cilium CNI
│   ├── gpu_nodegroup/    # NVIDIA A10G G5 GPU Launch Templates & ASGs
│   ├── aurora_postgres/  # PostgreSQL 18 Multi-AZ, Read Replicas, Parameter Groups
│   ├── elasticache/      # Redis 7.4 Cluster (6 Shards, Multi-AZ)
│   ├── s3_storage/       # Multi-tier encrypted media buckets & WORM Vaults
│   ├── nats_cluster/     # NATS JetStream Raft cluster deployment
│   ├── kms_keys/         # Customer-managed encryption keys & rotation policies
│   └── cloudflare_edge/  # Anycast DNS, SSL, WAF rules, Rate limiting
```

---

## 2. Remote State & Distributed Concurrency Locking

State is persisted in a locked, encrypted S3 bucket with Amazon DynamoDB state locking to prevent concurrent apply collisions:

```hcl
# infra/terraform/environments/production/backend.tf
terraform {
  required_version = ">= 1.8.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.60"
    }
    cloudflare = {
      source  = "cloudflare/cloudflare"
      version = "~> 4.38"
    }
  }

  backend "s3" {
    bucket         = "voxbridge-terraform-state-prod"
    key            = "core/production.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "voxbridge-terraform-locks"
  }
}
```

---

## 3. Sample Production Module: Multi-AZ Aurora PostgreSQL 18

```hcl
# infra/terraform/modules/aurora_postgres/main.tf
resource "aws_rds_cluster" "aurora_primary" {
  cluster_identifier      = "voxbridge-aurora-prod"
  engine                  = "aurora-postgresql"
  engine_version          = "16.2" # Aurora PostgreSQL 16/18 Compatible
  database_name           = "voxbridge_core"
  master_username         = "voxbridge_admin"
  manage_master_user_password = true # Managed via AWS Secrets Manager

  db_subnet_group_name    = var.db_subnet_group_name
  vpc_security_group_ids = [var.db_security_group_id]

  storage_encrypted       = true
  kms_key_id             = var.kms_key_arn

  backup_retention_period = 30
  preferred_backup_window = "03:00-04:00"
  deletion_protection     = true
  copy_tags_to_snapshot   = true

  serverlessv2_scaling_configuration {
    min_capacity = 2.0
    max_capacity = 64.0
  }

  enabled_cloudwatch_logs_exports = ["postgresql"]
}

resource "aws_rds_cluster_instance" "aurora_instances" {
  count               = 3
  identifier          = "voxbridge-aurora-instance-${count.index}"
  cluster_identifier  = aws_rds_cluster.aurora_primary.id
  instance_class      = "db.serverless"
  engine              = aws_rds_cluster.aurora_primary.engine
  engine_version      = aws_rds_cluster.aurora_primary.engine_version
  publicly_accessible = false
}
```

---

## 4. Automated Drift Detection & Reconciliation Workflow

A scheduled GitHub Actions workflow executes `tofu plan -detailed-exitcode` every 24 hours at 01:00 UTC:
- **Exit Code 0:** Zero drift detected.
- **Exit Code 2:** Infrastructure drift detected! The workflow posts a formatted diff to `#alerts-sre-iac` Slack channel and automatically files an incident issue to reconcile unauthorized modifications.
