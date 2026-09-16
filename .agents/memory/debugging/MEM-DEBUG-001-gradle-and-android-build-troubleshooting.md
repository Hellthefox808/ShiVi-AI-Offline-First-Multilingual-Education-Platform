# MEM-DEBUG-001: Gradle Daemon & Android Build Troubleshooting Guide

- **ID**: `MEM-DEBUG-001`
- **TYPE**: `BUG`
- **TITLE**: Diagnostic & Troubleshooting Guide for Android Native Build on Windows
- **PROJECT**: `BhashaSetu-AI`
- **SOURCE**: [gradle.properties](file:///d:/HACKTHON/bhashasetu-ai/gradle.properties), [local.properties](file:///d:/HACKTHON/bhashasetu-ai/local.properties)
- **CREATED_AT**: 2026-09-16T10:00:00+05:30
- **UPDATED_AT**: 2026-09-16T10:00:00+05:30
- **CONFIDENCE**: 1.0 (VERIFIED)
- **STATUS**: ACTIVE
- **TAGS**: `["debugging", "gradle", "troubleshooting", "android-sdk", "windows"]`
- **RELATED_COMPONENTS**: `["app/build.gradle.kts", "gradle.properties"]`
- **RELATED_DECISIONS**: `["MEM-INC-001"]`
- **EXPIRY / STALENESS SIGNAL**: Permanent
- **PROVENANCE**: Team SHIVI@808 Debugging Playbook

---

## CONTENT

### Checklist for Clean Build Execution:
1. **Kill Stray Daemons**:
   ```powershell
   Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like "*gradle*" } | Stop-Process -Force
   ```
2. **Verify Android SDK Path**:
   `local.properties` must point to:
   ```properties
   sdk.dir=C\:\\Users\\ravir\\AppData\\Local\\Android\\Sdk
   ```
3. **Verify Installed Platform**:
   Target platform: `platforms/android-36.1` with build-tools `36.0.0`.
4. **Compile APK**:
   ```powershell
   ./gradlew assembleDebug
   ```
   Do NOT run plain `gradle` commands without the wrapper `./gradlew`.
