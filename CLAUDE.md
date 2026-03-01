# CLAUDE.md

Dies ist das Repository für die RechenStar Android Native App. Hier wird ausschliesslich Android-spezifische Dokumentation abgelegt.

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
- **Tests:** JUnit5, MockK, Turbine, Compose UI Testing

## Konventionen

- Umlaute verwenden (ä, ö, ü) — aber kein ß (immer ss)
- Single Activity Architektur mit Compose Navigation
- Pure Domain Layer ohne Android-Abhängigkeiten (`domain/model/`, `domain/service/`)
- StateFlow statt LiveData
- Stateless Services als `object`, stateful als Hilt-Klasse
- Coroutines für alle async Room-Operationen
- `applicationId = "ch.rechenstar.app"`
- minSdk 26, targetSdk 35

## Build & Test

Alle Befehle über das Wrapper-Skript `scripts/android.sh`:

```bash
./scripts/android.sh build          # assembleDebug
./scripts/android.sh test           # Unit Tests
./scripts/android.sh uitest         # UI Tests auf Emulator
./scripts/android.sh install        # App auf Emulator installieren
./scripts/android.sh run            # App starten
./scripts/android.sh screenshot     # Screenshot vom Emulator
./scripts/android.sh gradle <args>  # Beliebiger Gradle-Befehl
```

Das Skript setzt automatisch `JAVA_HOME` und `ANDROID_SDK_ROOT`.

### Umgebung

- **JAVA_HOME:** `/usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`
- **ANDROID_SDK_ROOT:** `/usr/local/share/android-commandlinetools`
- **ADB:** `~/Library/Android/sdk/platform-tools/adb`
- **Emulator:** Pixel7 AVD, Android 15 (API 35), x86_64

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
│   └── settings/         # SettingsScreen, HelpScreen, ParentDashboardScreen + ViewModels
├── ui/
│   ├── components/       # AppButton, AppCard, ExerciseCard, NumberPad, ProgressBar, AchievementCard, ConfettiAnimation
│   ├── navigation/       # RechenStarNavigation, BottomNavBar, Screen sealed class
│   └── theme/            # Color, Type, Theme, Shape
└── util/                 # HapticFeedback, DateUtils, PreferencesManager
```
