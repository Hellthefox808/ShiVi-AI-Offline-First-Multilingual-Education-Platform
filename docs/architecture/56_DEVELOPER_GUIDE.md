# 56 — DEVELOPER ONBOARDING & LOCAL SETUP GUIDE

> **Document ID:** `BS-ARCH-56-DEV-GUIDE`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Target OS:** Windows 10/11 & Linux Workstations  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Prerequisites & Toolchain Verification

Before cloning or compiling BhashaSetu AI, verify that your local workstation meets the following minimum toolchain versions:

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                           DEVELOPER TOOLCHAIN MATRIX                        │
├────────────────────┬────────────────────┬───────────────────────────────────┤
│ Runtime / SDK      │ Minimum Version    │ Verification Command              │
├────────────────────┼────────────────────┼───────────────────────────────────┤
│ 1. Java JDK        │ OpenJDK 21 LTS     │ java -version                     │
│ 2. Node.js         │ Node.js 22 LTS     │ node -v                           │
│ 3. Python          │ Python 3.12+       │ python --version                  │
│ 4. Android SDK     │ Android SDK 36.1   │ sdkmanager --list_installed       │
│ 5. Docker Engine   │ Docker 26.0+       │ docker version                    │
│ 6. Git             │ Git 2.40+          │ git --version                     │
└────────────────────┴────────────────────┴───────────────────────────────────┘
```

---

## 2. Step-by-Step Local Environment Launch

### Step 1: Start Persistence & Messaging Mesh
From the monorepo root:
```powershell
cd infra
docker-compose up -d postgres redis
```
Verify databases are ready:
```powershell
docker ps --filter "name=bhashasetu"
```

### Step 2: Launch AI Platform Microservice (FastAPI)
```powershell
cd ../services/ai-platform
# Create and activate virtual environment
python -m venv venv
.\venv\Scripts\Activate.ps1
pip install -r requirements.txt
# Launch API server on port 8000
python main.py
```

### Step 3: Launch Web Backend Gateway (NestJS 11)
```powershell
cd ../web-backend
npm install
npm run start:dev
```
Gateway will listen on `http://localhost:3001` with Swagger docs at `http://localhost:3001/api/v1/docs`.

### Step 4: Launch Web Frontend Studio (Next.js 16.3)
> [!IMPORTANT]
> **Windows Port 3000 Invariant**: Launch on **Port 3002** to prevent collisions with Antigravity IDE:
```powershell
cd ../../apps/web-frontend
npm install
npm run dev -- -p 3002
```

### Step 5: Build Android Edge App
```powershell
cd ../..
./gradlew assembleDebug
```
The compiled APK will be located at:  
`app/build/outputs/apk/debug/app-debug.apk` ($29.6\text{ MB}$).
