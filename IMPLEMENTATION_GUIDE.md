# Project Katalis: The Definitive & Granular Technical Implementation Guide

**Version:** 1.2 (Production Blueprint)
**Status:** Approved for Development
**Lead Android Architect:** Firebender AI

---

## Introduction

This document provides a comprehensive, step-by-step guide for the implementation of Project Katalis. It is designed to be fully understood and executed by an AI development assistant like Gemini in Android Studio. Every step has been meticulously detailed to ensure a smooth, efficient, and error-free development process, incorporating the latest stable libraries and best practices as of July 2025.

---

## **Phase 0: Project Genesis & Foundational Architecture**

**Objective:** To establish a pristine, professionally configured, and completely stable Android Studio project environment. This phase is non-negotiable and must be completed before any feature code is written.

### **Step 0.1: IDE and Environment Configuration**

*   **IDE:** Latest stable version of Android Studio (Jellyfish or newer).
*   **Plugins:** Ensure the following plugins are installed and enabled:
    *   Kotlin
    *   Jetpack Compose
    *   Android Gradle Plugin
    *   Dagger Hilt
*   **Version Control:**
    1.  Initialize a Git repository in the project root.
    2.  Create a `.gitignore` file.
    3.  Create and switch to a `develop` branch. All feature development will occur on branches off `develop`.

### **Step 0.2: Android Project Initialization**

*   **Action:** Create a new project in Android Studio.
*   **Template:** "Empty Activity" with Jetpack Compose.
*   **Configuration:**
    *   **Name:** `Katalis`
    *   **Package Name:** `com.katalis.app`
    *   **Language:** `Kotlin`
    *   **Minimum SDK:** API 24 (Android 7.0 Nougat)
    *   **Build Configuration:** Kotlin DSL (`build.gradle.kts`)

### **Step 0.3: Gradle Dependency & Plugin Configuration**

1.  **Add Hilt Plugin to Project `build.gradle.kts`:**
    ```kotlin
    // Top-level build file where you can add configuration options common to all sub-projects/modules.
    plugins {
        alias(libs.plugins.android.application) apply false
        alias(libs.plugins.kotlin.android) apply false
        alias(libs.plugins.kotlin.compose) apply false
        alias(libs.plugins.hilt) apply false // Add this line
    }
    ```

2.  **Apply Hilt and KSP Plugins in App `build.gradle.kts`:**
    ```kotlin
    plugins {
        alias(libs.plugins.android.application)
        alias(libs.plugins.kotlin.android)
        alias(libs.plugins.kotlin.compose)
        id("com.google.dagger.hilt.android") // Add this line
        id("com.google.devtools.ksp") // Add this line
    }
    ```

3.  **Update App `build.gradle.kts` with All Dependencies:**
    ```kotlin
    dependencies {
        // Jetpack Compose (UI Toolkit)
        implementation(platform("androidx.compose:compose-bom:2025.05.01"))
        implementation("androidx.compose.ui:ui")
        implementation("androidx.compose.ui:ui-graphics")
        implementation("androidx.compose.ui:ui-tooling-preview")
        implementation("androidx.compose.material3:material3")
        implementation("androidx.activity:activity-compose:1.9.0")
        implementation("androidx.navigation:navigation-compose:2.9.2")

        // Lifecycle & State Management
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

        // Dependency Injection (Hilt)
        implementation("com.google.dagger:hilt-android:2.56.2")
        ksp("com.google.dagger:hilt-compiler:2.56.2")
        implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

        // Data & Persistence
        implementation("androidx.room:room-runtime:2.7.2")
        implementation("androidx.room:room-ktx:2.7.2")
        ksp("androidx.room:room-compiler:2.7.2")
        implementation("androidx.datastore:datastore-preferences:1.1.1")

        // On-Device AI
        implementation("com.google.android.ai:edge:1.0.0-beta01")

        // Infrastructure
        implementation("androidx.work:work-runtime-ktx:2.10.2")

        // Core & Testing
        implementation(libs.androidx.core.ktx)
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.ui.test.junit4)
        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)
    }
    ```

### **Step 0.4: API Key Management**

1.  **Create `local.properties`:** In the project root, create this file.
2.  **Store API Key:** Add your Gemini API key: `GEMINI_API_KEY="YOUR_API_KEY_HERE"`.
3.  **Expose Key in `build.gradle.kts`:**
    ```kotlin
    android {
        // ... other configs
        defaultConfig {
            // ... other configs
            buildConfigField("String", "GEMINI_API_KEY", "\"${properties["GEMINI_API_KEY"]}\"")
        }
    }
    ```

### **Step 0.5: Package Structure Definition**

*   **Action:** Inside `com.katalis.app`, create the following packages.

    ```
    com.katalis.app
    ├── data
    │   ├── local
    │   │   ├── dao
    │   │   └── entity
    │   └── repository
    ├── di
    ├── domain
    │   ├── engine
    │   └── service
    ├── ui
    │   ├── components
    │   ├── navigation
    │   ├── screens
    │   │   ├── chat
    │   │   ├── dashboard
    │   │   └── onboarding
    │   └── theme
    └── util
    ```

### **Step 0.6: Hilt Application Class**

1.  **Create `KatalisApplication.kt`:**
    ```kotlin
    package com.katalis.app

    import android.app.Application
    import dagger.hilt.android.HiltAndroidApp

    @HiltAndroidApp
    class KatalisApplication : Application()
    ```
2.  **Update `AndroidManifest.xml`:**
    ```xml
    <application
        android:name=".KatalisApplication"
        ...>
    </application>
    ```

**Acceptance Criteria for Phase 0:** The project is created, dependencies are synced without errors, the package structure is in place, and an empty "Hello World" app successfully builds and runs on an emulator.

---
## **Phase 1: Data Layer Implementation**

**Objective:** To create a robust, type-safe data layer that can load and serve our pre-packaged Knowledge Pack.

### **Step 1.1: Database Asset Preparation**

1.  **Create Directory:** `src/main/assets/database/`
2.  **Add Database:** Place `GCE_PMM.db` inside this directory.

### **Step 1.2: Define Room Entities**

*   **Location:** `data/local/entity`
*   **Action:** Create a Kotlin `@Entity` data class for each table in the database.

### **Step 1.3: Define Data Access Objects (DAOs)**

*   **Location:** `data/local/dao`
*   **Action:** Create a `@Dao` interface for each entity with all necessary `@Query` functions.

### **Step 1.4: Define the Room Database Class**

*   **Location:** `data/local/KatalisDatabase.kt`
*   **Action:** Create an abstract class `KatalisDatabase : RoomDatabase()` annotated with `@Database`, listing all entities.

### **Step 1.5: Implement Repositories**

*   **Location:** `data/repository`
*   **Action:** Create repository implementations (e.g., `KnowledgeRepositoryImpl`) that take DAOs as constructor parameters.

### **Step 1.6: Implement Dependency Injection (Hilt)**

*   **Location:** `di/AppModule.kt`
*   **Action:** Create a Hilt `@Module` to provide singleton instances of the database and repositories. Implement the logic to copy the database from assets on first launch.

---

## **Phase 2: UI Implementation (The App's Body)**

**Objective:** To translate the high-fidelity Figma designs into a complete library of stateless Jetpack Compose components and screens.

### **Step 2.1: Implement the Theme**

*   **Location:** `ui/theme`
*   **Action:**
    1.  Open `Color.kt` and define `val`s for every color in your design system (e.g., `CalmBlue`, `WarmAccentYellow`).
    2.  In `Type.kt`, define your `Typography` scale.
    3.  In `Theme.kt`, wire these up in your `MaterialTheme` composable.

### **Step 2.2: Build the Component Library**

*   **Location:** `ui/components/`. Create files like `Cards.kt`, `Buttons.kt`, etc.
*   **Action:** For every reusable element from your Figma designs, create a dedicated, stateless Composable function.
*   **Best Practice:** Each component should be self-contained and receive all data and event handlers as parameters. Use the `@Preview` annotation extensively.

### **Step 2.3: Assemble the Screens**

*   **Location:** `ui/screens/dashboard/DashboardScreen.kt`, etc.
*   **Action:**
    1.  For each screen, create a main Composable function. This function will be stateful.
    2.  The screen Composable will call `hiltViewModel()` to get its ViewModel instance, collect the state as `val state = viewModel.uiState.collectAsState().value`, and then pass this state down to a stateless "content" Composable (e.g., `DashboardContent(state = state, onEvent = viewModel::onEvent)`).
    3.  Inside the "content" Composable, assemble the screen using your prefabricated components.

### **Step 2.4: Implement Navigation**

*   **Location:** `ui/navigation`
*   **Action:**
    1.  Create a sealed class `Screen(val route: String)` defining all app destinations.
    2.  Create the `NavGraph.kt` file. Implement the main `NavHost` Composable. For each destination, define its route and call the corresponding stateful screen Composable.

---

## **Phase 3: ViewModel Logic & State Management (The App's Nervous System)**

**Objective:** To implement the ViewModels that will connect the UI to the data layer, managing UI state and business logic.

### **Step 3.1: Define State and Event Classes**

*   **Location:** `ui/screens/dashboard/DashboardStateAndEvents.kt`, etc.
*   **Action:** For each screen, define a `data class` for its state and a `sealed class` for its events.

### **Step 3.2: Implement the ViewModels**

*   **Location:** `ui/screens/dashboard/DashboardViewModel.kt`, etc.
*   **Action:**
    1.  Create the `@HiltViewModel` class, injecting the required repositories.
    2.  Create a private `_uiState = MutableStateFlow(DashboardState())` and expose it as `val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()`.
    3.  Use an `init` block to launch a `viewModelScope` coroutine to fetch initial data.
    4.  Implement an `onEvent` function that uses a `when` statement to trigger business logic.

---

## **Phase 4: Intelligence Layer Implementation (The App's Soul)**

**Objective:** To integrate the on-device AI and the RAG pipeline.

### **Step 4.1: Implement the InferenceEngine**

*   **Location:** `domain/engine/InferenceEngine.kt`
*   **Action:**
    1.  Implement the class as a wrapper around the `com.google.android.ai:edge` library.
    2.  Initialize the Gemma model from assets, enabling the GPU delegate.
    3.  Implement `generateResponse(prompt: String): Result<String>`, wrapping output in a `Result` class.

### **Step 4.2: Implement the RAGService**

*   **Location:** `domain/service/RAGService.kt`
*   **Action:** Create the class, injecting the `KnowledgeRepository` and `InferenceEngine`. Implement the full RAG pipeline as a single `suspend` function.

### **Step 4.3: Integrate into the ChatViewModel**

*   **Action:** Inject the `RAGService` into the `ChatViewModel`. When the `SendMessage` event is triggered, call `ragService.generateResponse()` and handle the `Loading`, `Success`, and `Error` states.

---

## **Phase 5: Onboarding & Infrastructure (The First Impression)**

**Objective:** To build a seamless and reliable first-launch experience.

### **Step 5.1: Implement the SettingsRepository**

*   **Location:** `data/local/SettingsRepository.kt`
*   **Action:** Implement the class using Jetpack DataStore to manage the `isOnboardingComplete` flag.

### **Step 5.2: Implement the Main Navigation Logic**

*   **Action:** Create a `MainViewModel` that determines the correct start destination by checking the `isOnboardingComplete` flag. Use the Android 12+ `SplashScreen` API.

### **Step 5.3: Implement the Download Worker**

*   **Location:** `workers/DownloadWorker.kt`
*   **Action:** Create a `CoroutineWorker` that handles the download of the Knowledge Pack database and reports its progress.

---

## **Phase 6 & 7: Hardening, Polish, and Launch**

**Objective:** To transition the application from a feature-complete state to a production-ready, shippable product.

### **Step 6.1: Comprehensive Testing**

*   **Unit Tests:** Write JUnit tests for all ViewModels, mocking repositories.
*   **Integration Tests:** Write tests for DAOs and database migrations.
*   **AI Validation:** Execute a "Golden Set" of prompts to verify AI responses.
*   **Manual QA:** Perform extensive manual testing on physical devices.

### **Step 7.1: Pre-Launch Checklist**

*   **Asset Generation:** Finalize all production assets.
*   **ProGuard/R8:** Configure and test code shrinking.
*   **Generate Signed AAB:** Create the final, signed Android App Bundle.

### **Step 7.2: Staged Rollout via Play Console**

*   **Internal Testing:** Upload the AAB to the Internal Testing track.
*   **Production:** Promote the build to Production and monitor the Google Play Console.
