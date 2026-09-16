# MEM-INC-001: Windows JVM Native Memory Allocation Failure & Daemon Explosion

- **ID**: `MEM-INC-001`
- **TYPE**: `INCIDENT`
- **TITLE**: JVM Native Memory Exhaustion (malloc failed / arena.cpp:168) on Windows
- **PROJECT**: `BhashaSetu-AI`
- **SOURCE**: [gradle.properties](file:///d:/HACKTHON/bhashasetu-ai/gradle.properties), hs_err crash logs (session 146f505d)
- **CREATED_AT**: 2026-09-16T10:00:00+05:30
- **UPDATED_AT**: 2026-09-16T10:00:00+05:30
- **CONFIDENCE**: 1.0 (VERIFIED)
- **STATUS**: RESOLVED
- **TAGS**: `["incident", "jvm", "gradle", "windows", "out-of-memory", "arena-cpp"]`
- **RELATED_COMPONENTS**: `["gradle.properties", "app/build.gradle.kts", "gradlew.bat"]`
- **RELATED_DECISIONS**: `["MEM-CONV-001"]`
- **EXPIRY / STALENESS SIGNAL**: Permanent guardrail for Android build on Windows host
- **PROVENANCE**: Debugged and resolved by Lead Architect Ravi Ranjan Singh & Antigravity

---

## CONTENT

### Symptoms
Android Gradle builds crashed with fatal JVM errors:
`There is insufficient memory for the Java Runtime Environment to continue.`
`Native memory allocation (malloc) failed to allocate ... bytes for Chunk::new (arena.cpp:168)`

### Root Cause Analysis
1. 37 orphaned background Gradle daemons were lingering in Windows task memory, each reserving 4GB virtual address space.
2. Default Gradle configuration had unbounded worker concurrency and default unconstrained heap arguments.
3. Windows commit limit was reached, causing malloc failures in native C++ code within HotSpot JVM.

### Permanent Fix
1. Terminated all orphaned Gradle and Kotlin compiler daemons.
2. In [gradle.properties](file:///d:/HACKTHON/bhashasetu-ai/gradle.properties), enforced strict memory caps:
   ```properties
   org.gradle.jvmargs=-Xmx2048m -XX:+UseG1GC -XX:MaxMetaspaceSize=512m
   org.gradle.workers.max=2
   org.gradle.parallel=true
   kotlin.daemon.jvm.options=-Xmx1024m -XX:MaxMetaspaceSize=256m
   ```
3. Ensured `gradlew.bat` with Gradle 9.3.1 is always used for `./gradlew assembleDebug`.
