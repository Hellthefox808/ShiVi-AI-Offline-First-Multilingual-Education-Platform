# 31 — FRONTEND ARCHITECTURE & DESIGN SYSTEM SPECIFICATIONS

> **Document ID:** `BS-ARCH-31-FRONTEND-ARCH`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Source Grounding:** [`apps/web-frontend/`](file:///d:/HACKTHON/bhashasetu-ai/apps/web-frontend) & [`docs/TAD.md`](file:///d:/HACKTHON/bhashasetu-ai/docs/TAD.md)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Web Frontend Technology Stack & Directory Seams

```text
apps/web-frontend/
├── app/                        # Next.js 16.3 App Router (React Server Components)
│   ├── (auth)/login/           # Educator & Admin login routes
│   ├── (dashboard)/lessons/    # Multi-step Lesson Studio
│   └── (dashboard)/admin/      # District FLN analytics & sync telemetry
├── features/                   # Domain-driven feature packages
│   ├── lesson-studio/          # Scaffolding editor, RAG preview, Ol Chiki canvas
│   ├── voice-dialogue/         # Web Audio API real-time waveform visualizer
│   └── analytics/              # NIPUN Bharat competency progress heatmaps
├── components/ui/              # Radix UI + Tailwind CSS v4 accessible primitives
├── lib/api/                    # TanStack Query v5 hooks generated from OpenAPI 3.1
└── stores/                     # Lightweight Zustand stores for transient UI states
```

---

## 2. The 5 Standard Screen State Contracts

Every user interface screen across the Web Studio and Edge App strictly implements five explicit screen states:

| Screen State | Visual Contract | Behavioral Contract |
|---|---|---|
| **1. LOADING** | Skeleton loaders matching the exact grid layout; Cumulative Layout Shift (CLS) $< 0.05$. | Non-blocking spinners for background audio synthesis; UI remains interactive. |
| **2. EMPTY** | Culturally grounded illustration with clear Call to Action (e.g., *"No lessons created yet. Click 'New Lesson' to start"*). | Focus placed on primary action button. |
| **3. ERROR** | Bilingual human-readable error banner with explicit recovery button (e.g., *"Failed to load audio. Tap to retry"*). | Technical error codes logged to OpenTelemetry; suppressed from UI. |
| **4. OFFLINE** | Subtle, persistent status badge indicating *"Working Offline (Local Mode)"*. | All create, edit, quiz, and teach actions remain 100% functional. |
| **5. SUCCESS** | Rendered interactive UI with high-contrast elements ($> 4.5:1$ contrast ratio). | Keyboard accessible; screen-reader aria labels announce state changes. |

---

## 3. Ol Chiki Native Script Rendering & Web Audio Visualizer

1. **Native Script Typography Canvas**:
   - Ol Chiki (`sat_Olck`) and Warang Chiti (`hoc_Wara`) fonts are bundled locally using modern CSS `@font-face` rules to eliminate external Google Fonts latency or webfont FOIT (Flash of Invisible Text):
   ```css
   @font-face {
     font-family: 'OlChiki';
     src: url('/fonts/OlChiki-Regular.ttf') format('truetype');
     font-display: swap;
   }
   ```
2. **Web Audio Visualizer**:
   - Real-time microphone input is piped into an `AudioContext` with an `AnalyserNode`.
   - Renders a 60 FPS canvas-based frequency visualizer providing immediate feedback to the educator that speech is actively being captured.

---

## 4. Windows Development & Host Port Invariant

> [!IMPORTANT]
> **Windows Host Port 3000 Collision**:
> On Windows developer workstations, port `127.0.0.1:3000` is bound by the host IDE services (`Antigravity IDE.exe`).  
> The web frontend dev and production servers are hard-configured to use **Port `3002`**:
> ```powershell
> # Running the Web Frontend on Windows
> npm run dev -- -p 3002
> ```
