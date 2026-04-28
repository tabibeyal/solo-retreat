# WHAT - Project context

*   **High-level architecture**: The app follows a clean MVVM + Repository architecture. It is structured into three distinct retreat phases: **Preparation** (planning, schedule, downloads), **Retreat** (guided practice, timer, precepts), and **Integration** (journaling, summary, export).
*   **Tech stack and key libraries**:
    *   **UI**: Jetpack Compose
    *   **Language**: Kotlin
    *   **DI**: Hilt
    *   **Database**: Room (SQLite)
    *   **Background Work**: WorkManager
    *   **Media**: Media3 (ExoPlayer) for talks, MediaPlayer for Tibetan bowl sounds.
    *   **Widgets**: Glance
    *   **Serialization**: Kotlinx Serialization
*   **Code style and standards**:
    *   **Privacy-first & Offline-first**: No user tracking, no analytics, no proprietary dependencies.
    *   **Material 3**: Uses Material 3 components and theme.
    *   **Localization**: The project is in the process of externalizing strings (using `strings.xml`), though many UI strings are currently hardcoded.

# HOW - Commands and workflows

*   **Build / dev / typecheck commands**:
    *   `./gradlew assembleDebug`: Build the debug APK.
    *   `./gradlew clean`: Clean build artifacts.
*   **Test commands and rules**:
    *   `./gradlew test`: Run unit tests (includes Kotest Property-Based Tests).
    *   `./gradlew connectedAndroidTest`: Run instrumented tests on a device/emulator.
    *   **Property-Based Testing**: Use Kotest Property for complex logic (e.g., `TimeUtils`, `ScheduleValidator`).
*   **Lint / format commands**:
    *   Use Android Studio's built-in "Inspect Code" and "Reformat Code".
*   **Review checklists / pre-PR steps**:
    *   Ensure no new dependencies are added that require internet connectivity during the retreat phase.
    *   Verify that no tracking or proprietary SDKs are introduced.
    *   Check that any new UI strings are added to `strings.xml`.

# BEHAVE- Behavioral instructions

*   **Workflow preferences**:
    *   Follow the existing **MVVM + Repository** pattern.
    *   Use **Jetpack Compose** for all new UI.
    *   Prefer **Hilt** for dependency injection.
    *   Ensure all new features work fully offline (except for initial asset downloads).
*   **What Gemini should never do in this repo**:
    *   **NEVER** add proprietary dependencies or closed-source SDKs (e.g., Firebase Analytics, Google Maps).
    *   **NEVER** add any form of user tracking, telemetry, or analytics.
    *   **NEVER** add features that require a persistent internet connection (e.g., streaming YouTube videos).
    *   **NEVER** use `sed -i` or other tools that bypass the IDE's buffer management.
    *   **AVOID** hardcoding strings in Composables; use `res/values/strings.xml` instead.
