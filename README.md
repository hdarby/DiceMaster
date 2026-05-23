# DiceMaster

An Android application for tabletop RPG players to roll dice, manage characters, track weapon inventories, and manage consumable items.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture (domain / data / ui) |
| DI | Koin 4.2.1 |
| Database | Room (KSP-generated) |
| Navigation | Jetpack Navigation Compose |
| Testing | JUnit 4, MockK, Turbine, kotlinx-coroutines-test |
| Coverage | Kover 0.9.8 |
| Min SDK | 30 |
| Target SDK | 36 |

---

## Getting Started

### Prerequisites

- Android Studio Ladybug (2024.2) or later
- JDK 17+

### Opening the project

1. Clone the repository.
2. Open the **root** `DiceMaster/` folder in Android Studio.
3. **Run a Gradle sync** — `File → Sync Project with Gradle Files`.

> **Important:** `.idea/modules.xml` is excluded from version control (it is Gradle-derived and
> regenerated automatically). Android Studio will not show the **Android** project view, and may
> not recognise the project as an Android project, until the first Gradle sync completes.

---

## Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device / emulator
./gradlew installDebug
```

---

## Testing

```bash
# Unit tests
./gradlew testDebugUnitTest

# Instrumentation / UI tests (requires connected device or emulator)
./gradlew connectedDebugAndroidTest

# Coverage report (Kover)
./gradlew koverReportDebug
```

---

## Project Structure

```
app/src/
├── main/java/com/hdarby/dicemaster/
│   ├── data/
│   │   ├── local/          # Room database, DAOs, entities
│   │   └── repository/     # Repository implementations
│   ├── di/                 # Koin DI module (AppModule.kt)
│   ├── domain/
│   │   ├── model/          # Domain models (Character, Weapon, …)
│   │   ├── repository/     # Repository interfaces
│   │   └── usecase/        # Use cases (one operation each)
│   ├── ui/
│   │   ├── components/     # Reusable Compose components
│   │   ├── navigation/     # Nav graph + Screen sealed class
│   │   ├── screens/        # Screen-level composables
│   │   └── theme/          # Material 3 theme
│   └── viewmodel/          # ViewModels + UI state classes
├── test/                   # Unit tests
└── androidTest/            # Instrumentation / UI tests
```

---

## Documentation

| File | Purpose |
|------|---------|
| `CLAUDE.md` | AI agent rules and project conventions |
| `COVERAGE_STRATEGY.md` | Test coverage goals, tooling notes, and thresholds |
| `TECH_DEBT.md` | Running log of known shortcuts and deferred improvements |
| `ADDITIONAL_FEATURES.md` | Backlog of features to add, edit, or delete in the app |

