# Katalis

**An offline-first, on-device AI tutor for GCE students in low-connectivity environments.**

Katalis is an open-source project dedicated to democratizing education by providing a world-class, personal mentor on the Android devices that students already own. Our mission is to ensure that academic potential is limited only by curiosity, not by circumstances.

---

##  Core Mission & Principles

This project was born from the lived experience of struggling to access online educational resources in Cameroon. Our entire architecture is built to solve this specific problem.

- **100% Offline-First:** Every core learning feature works without an internet connection after a one-time setup.
- **On-Device Intelligence:** Using Google's Gemma 3n, all AI processing happens on the device, ensuring zero data costs and complete student privacy.
- **Empathetic & Performant:** A clean, encouraging, and intuitive user interface designed to build confidence, even on budget hardware.

##  Key Features

- **Structured Learning Paths:** Guided curriculum based on the official GCE syllabus.
- **AI-Powered Socratic Tutoring:** An interactive chat mentor that guides students to answers instead of just providing them.
- **"Smarter RAG" Architecture:** Our AI is enriched with insights from official GCE examiner reports to understand *why* students struggle with specific topics.
- **Exam Question Bank:** Practice with real past GCE questions, complete with step-by-step solutions and examiner notes.

##  Tech Stack & Architecture

- **Platform:** Native Android
- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Database:** Room (SQLite)
- **On-Device AI:** Google AI Edge SDK with a quantized Gemma 3n model
- **Dependency Injection:** Dagger Hilt

##  Getting Started

This section is for developers who wish to contribute to the project.

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/[YourUsername]/katalis-android.git
    ```
2.  **Open in Android Studio:** Open the project in the latest stable version of Android Studio.
3.  **API Key:** You will need a Google Gemini API key for the Content Factory scripts. Add it to your `local.properties` file: `GEMINI_API_KEY="YOUR_API_KEY"`
4.  **Build the project:** Let Gradle sync and build the initial project.

##  How to Contribute

We welcome contributions of all kinds, from bug fixes to feature ideas! Please read our `CONTRIBUTING.md` file to get started.

## 📄 License

This project is licensed under the MIT License. See the `LICENSE` file for details.
