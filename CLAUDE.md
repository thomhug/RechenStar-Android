# CLAUDE.md

Dies ist das Repository fuer die RechenStar Android Native App. Hier wird ausschliesslich Android-spezifische Dokumentation abgelegt.

Alle Business-Dokumentation (fachliche Anforderungen, Konzepte, etc.) befindet sich im [RechenStar Hauptrepository](../RechenStar/CLAUDE.md).

## Rolle

Senior Android-Entwickler, spezialisiert auf iOS→Android-Portierung.

## Tech-Stack

- **Sprache:** Kotlin
- **UI:** Jetpack Compose
- **Persistenz:** Room
- **DI:** Hilt
- **Navigation:** Navigation Compose
- **Async:** Coroutines + StateFlow
- **Einstellungen:** DataStore
- **Tests:** JUnit5, MockK, Turbine

## Konventionen

- Kein ss → immer ss (kein Eszett/ss)
- Single Activity Architektur mit Compose Navigation
- Pure Domain Layer ohne Android-Abhaengigkeiten (`domain/model/`, `domain/service/`)
- StateFlow statt LiveData
- Stateless Services als `object`, stateful als Hilt-Klasse
- Coroutines fuer alle async Room-Operationen
- `applicationId = "ch.rechenstar.app"`
- minSdk 26, targetSdk 35

## Projektstruktur

```
app/src/main/java/ch/rechenstar/app/
├── data/
│   ├── local/
│   │   ├── dao/          # Room DAOs
│   │   └── entity/       # Room Entities + Relationship-Klassen
│   └── repository/       # UserRepository, SessionRepository, ProgressRepository
├── di/                   # Hilt-Module (DatabaseModule, RepositoryModule, AppModule)
├── domain/
│   ├── model/            # Pure Kotlin: Exercise, ExerciseCategory, Difficulty, Level, AchievementType...
│   └── service/          # ExerciseGenerator, ExerciseViewModel, MetricsService, EngagementService, SoundService
├── features/
│   ├── achievements/     # AchievementsScreen + ViewModel
│   ├── exercise/         # ExerciseScreen, SessionCompleteScreen
│   ├── home/             # HomeScreen + ViewModel
│   ├── profile/          # ProfileSelectionScreen + ViewModel
│   ├── progress/         # ProgressScreen + ViewModel
│   └── settings/         # SettingsScreen, HelpScreen, ParentGateScreen, ParentDashboardScreen + ViewModels
├── ui/
│   ├── components/       # AppButton, AppCard, ExerciseCard, NumberPad, ProgressBar, AchievementCard, ConfettiAnimation
│   ├── navigation/       # RechenStarNavigation, BottomNavBar, Screen sealed class
│   └── theme/            # Color, Type, Theme, Shape
└── util/                 # HapticFeedback, DateUtils, PreferencesManager
```

## Build

```bash
export JAVA_HOME=/usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
./gradlew assembleDebug   # Build
./gradlew test            # Unit Tests
```
