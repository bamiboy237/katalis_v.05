# Phase 0: Project Genesis & Foundational Architecture

**Status:** 🟡 In Progress (75% Complete)  
**Started:** July 17, 2025  
**Target Completion:** 3 Days

## Objective

To establish a pristine, professionally configured, and completely stable Android Studio project
environment. This phase is non-negotiable and must be completed before any feature code is written.

## Task Checklist

### Step 0.1: IDE and Environment Configuration ✅

- [x] Latest stable Android Studio installed
- [x] Kotlin plugin enabled
- [x] Jetpack Compose plugin enabled
- [x] Android Gradle Plugin configured
- [x] Dagger Hilt plugin available
- [x] Git repository initialized

### Step 0.2: Android Project Initialization ✅

- [x] Project created with "Empty Activity" template
- [x] Name: `Katalis`
- [x] Package Name: `com.katalis.app`
- [x] Language: Kotlin
- [x] Minimum SDK: API 24
- [x] Build Configuration: Kotlin DSL

### Step 0.3: Gradle Dependency & Plugin Configuration ✅

- [x] Added Hilt plugin to project `build.gradle.kts`
- [x] Applied Hilt and KSP plugins in app `build.gradle.kts`
- [x] Updated version catalog with all required versions
- [x] Added all dependencies to app `build.gradle.kts`:
    - [x] Jetpack Compose dependencies
    - [x] Lifecycle & State Management
    - [x] Hilt for Dependency Injection
    - [x] Room for Data Persistence
    - [x] DataStore for preferences
    - [x] On-Device AI (Google AI Edge)
    - [x] WorkManager

### Step 0.4: API Key Management ✅

- [x] Created `local.properties` entry for API key
- [x] Added `buildConfigField` in `build.gradle.kts`
- [x] Enabled `buildConfig` feature

### Step 0.5: Package Structure Definition ⏳

- [ ] Create `data/local/dao` package
- [ ] Create `data/local/entity` package
- [ ] Create `data/repository` package
- [ ] Create `di` package
- [ ] Create `domain/engine` package
- [ ] Create `domain/service` package
- [ ] Create `ui/components` package
- [ ] Create `ui/navigation` package
- [ ] Create `ui/screens/dashboard` package
- [ ] Create `ui/screens/chat` package
- [ ] Create `ui/screens/onboarding` package
- [ ] Create `util` package

### Step 0.6: Hilt Application Class ⏳

- [ ] Create `KatalisApplication.kt` with `@HiltAndroidApp`
- [ ] Update `AndroidManifest.xml` with application name

## Current Issues

1. **Gradle Sync Failed** - No space left on device
    - Need to clear Gradle cache: `rm -rf ~/.gradle/caches/`
    - Alternative: Free up disk space

## Code Snippets Created

### libs.versions.toml additions

```toml
hilt = "2.56.2"
hiltNavigationCompose = "1.2.0"
navigationCompose = "2.9.2"
lifecycleViewModelCompose = "2.8.3"
coroutines = "1.8.0"
room = "2.7.2"
datastore = "1.1.1"
androidAiEdge = "1.0.0-beta01"
work = "2.10.2"
ksp = "2.0.21-1.0.25"
```

### build.gradle.kts (project level)

```kotlin
plugins {
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
```

### build.gradle.kts (app level) - Key additions

```kotlin
plugins {
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// In defaultConfig:
val properties = org.jetbrains.kotlin.konan.properties.Properties()
properties.load(project.rootProject.file("local.properties").inputStream())
buildConfigField("String", "GEMINI_API_KEY", "\"${properties.getProperty("GEMINI_API_KEY", "")}\"")

// In buildFeatures:
buildConfig = true
```

## Next Steps

1. Clear Gradle cache to resolve sync issue
2. Run Gradle sync again
3. Create all package directories
4. Implement Hilt Application class
5. Update AndroidManifest.xml
6. Verify clean build and run

## Acceptance Criteria

- [ ] Project created with correct configuration
- [ ] All dependencies synced without errors
- [ ] Complete package structure in place
- [ ] Empty "Hello World" app builds and runs successfully