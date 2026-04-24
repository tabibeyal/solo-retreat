# SoloRetreat

A single-user, offline-first Android application for planning and executing solo meditation retreats. Built with Kotlin, Jetpack Compose, Room, Hilt, and WorkManager.

**Privacy Focus**: This app is fully offline-first, contains no proprietary dependencies, and performs no user tracking.

## Architecture Overview

The app follows a clean MVVM + Repository architecture with three distinct phases:

- **Preparation** — Create retreat plan, build schedule, download talks, complete checklist
- **Retreat** — Guided daily practice with timer, precept tracking, meal logging
- **Integration** — Journal entries and retreat summary with export

## Project Structure

```
com.soloretreat/
├── HomeRetreatApplication.kt          # Application class with Hilt
├── di/
│   └── AppModule.kt                   # Hilt dependency injection
├── data/
│   ├── local/
│   │   ├── HomeRetreatDatabase.kt     # Room database
│   │   ├── Converters.kt              # Type converters
│   │   ├── dao/                       # 8 DAOs
│   │   └── entity/                    # 8 entities
│   ├── repository/                    # 7 repositories
│   └── model/                         # Enums & Precept data
├── service/
│   ├── RetreatSessionService.kt       # Foreground service
│   ├── BellManager.kt                 # Tibetan bowl sounds
│   ├── TalkDownloadWorker.kt          # WorkManager downloader
│   └── BootCompletedReceiver.kt       # Auto-restart on boot
├── ui/
│   ├── theme/                         # Colors, Typography, Theme
│   ├── components/                    # Reusable Compose components
│   ├── preparation/                   # Prep phase screens + VMs
│   ├── retreat/                       # Retreat phase screens + VMs
│   ├── integration/                   # Integration screens + VMs
│   ├── navigation/                    # NavHost + Screen routes
│   └── MainActivity.kt
├── util/
│   ├── Constants.kt
│   ├── TimeUtils.kt
│   └── NotificationHelper.kt
└── widget/
    └── RetreatWidget.kt               # Glance home screen widget
```

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM + Repository |
| DI | Hilt |
| Database | Room (SQLite) |
| Background Work | WorkManager |
| Foreground Service | LifecycleService |
| Audio Playback | ExoPlayer (talks), MediaPlayer (bells) |
| Widgets | Glance |
| Serialization | Kotlinx Serialization |

## Building the Project

1. Open in Android Studio Hedgehog or later
2. Sync Gradle files
3. Build → Make Project (Ctrl+F9)
4. Run on device or emulator (minSdk 26)

## Key Features Implemented

### Phase 1: Preparation
- [x] Retreat creation with date range picker
- [x] Daily schedule builder with drag-and-drop time pickers
- [x] Dhamma talk catalog with WorkManager download
- [x] Preparation checklist with progress tracking
- [x] Validation gates (no overlap, meal before noon, talks downloaded)

### Phase 2: Retreat
- [x] RetreatSessionService foreground service with persistent notification
- [x] Block transition notifications
- [x] Full-screen meditation timer with Tibetan singing bowl bells
- [x] 8 Precepts tracker with Pali/English text
- [x] Meal cutoff enforcer with noon countdown
- [x] Thin mattress reminder in checklist

### Phase 3: Integration
- [x] Journal with tags and CRUD operations
- [x] Retreat summary generator
- [x] Plain text export via Android share sheet

### Supporting Infrastructure
- [x] 4 notification channels (service, reminders, meal, precepts)
- [x] Glance home screen widget
- [x] Boot completed receiver for service restart
- [x] Warm, non-clinical theme (sage green, ochre, cream)
- [x] Adaptive app icon

## Support the Project

If you find this tool helpful for your practice, you can support its development:
- **PayPal**: [Donate via PayPal](https://www.paypal.com/donate?business=eyaltabib82@gmail.com&currency_code=USD)

## Links

- **Website**: [https://github.com/tabibeyal/soloretreat](https://github.com/tabibeyal/soloretreat)
- **Changelog**: [https://github.com/tabibeyal/soloretreat/releases](https://github.com/tabibeyal/soloretreat/releases)

## Known Limitations / Notes

- YouTube talks are explicitly excluded per architecture requirements to ensure full offline reliability.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

All dhammatalks.org content remains under their respective licenses.
