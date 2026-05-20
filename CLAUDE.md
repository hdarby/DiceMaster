# DiceMaster – Claude Agent Guidelines

This file defines the rules and workflows Claude must follow when working on this project.

---

## Project Overview

**DiceMaster** is an Android application built with:
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material3
- **Architecture**: MVVM with Clean Architecture (domain / data / ui layers)
- **DI**: Koin (`4.2.1`)
- **Database**: Room (KSP-generated)
- **Navigation**: Jetpack Navigation Compose
- **Testing**: JUnit4, MockK, Turbine, kotlinx-coroutines-test
- **Coverage**: Kover (`0.9.8`)
- **Min SDK**: 30 | **Target SDK**: 36

---

## Pre-Commit Checklist

Before finalising any commit or declaring a task complete, Claude **must**:

1. **Optimise imports** — remove all unused imports from every file that was touched. Kotlin files must not contain wildcard imports (`import foo.*`) unless unavoidable (e.g. generated code).
2. **Clean code standards** — review all touched files against clean code principles: meaningful names, single-responsibility functions, no magic numbers/strings, no dead code, consistent formatting, and no overly complex logic that should be extracted.
3. **Update documentation** — update any relevant `.md` files to reflect the change (e.g. `COVERAGE_STRATEGY.md`, `CLAUDE.md` itself, or any new docs created during the task).
4. **Track tech debt** — if any shortcuts, workarounds, or known issues are introduced or discovered during the task, log them in `TECH_DEBT.md` with context. If existing items in `TECH_DEBT.md` are resolved by the change, mark them as done and remove or archive them.
5. **Ask about unit tests** — once the implementation plan is complete and code changes are done, **ask the user** whether they want unit tests written or updated to cover the new/changed behaviour before closing the task.
6. **Ask about string resources** — ask the user whether they want a scan for hardcoded strings that could be moved to `strings.xml`. If the user agrees, identify all hardcoded UI strings in touched (and related) files and extract them into `app/src/main/res/values/strings.xml`, replacing each literal with the appropriate `stringResource(R.string.*)` call.
7. **Ask about dead code** — ask the user whether they want a scan for dead code (unused functions, properties, classes, parameters, imports, and unreachable branches) in the touched files. If the user agrees, identify all dead code and remove it, then verify the build still passes.

---

## Code Style & Conventions

### General
- Follow the [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Prefer `val` over `var`; use immutable data structures where possible.
- Use `data class` for models and UI state objects.
- Keep functions small and single-purpose; extract helpers freely.

### Architecture
- **Domain layer** (`domain/`) must have zero Android dependencies — plain Kotlin only.
- **Use cases** live in `domain/usecase/` and wrap a single operation each.
- **ViewModels** live in `viewmodel/` and expose `StateFlow<UiState>` via `uiState`.
- **Repository implementations** live in `data/repository/` and depend on DAOs, not directly on Room APIs in the ViewModel.
- Never call `runBlocking` in production code; use `viewModelScope.launch` or `viewModelScope.async`.

### Jetpack Compose
- All screen-level composables live in `ui/screens/`.
- Reusable UI components live in `ui/components/`.
- Pass lambdas for events (e.g. `onSave: () -> Unit`) rather than passing ViewModels directly into child composables.
- Annotate every composable preview with `@Preview` + a descriptive name.
- Use `@OptIn(ExperimentalMaterial3Api::class)` only at the file or function level — never at the module level.

### Dependency Injection (Koin)
- All DI bindings live in `di/AppModule.kt`.
- Use `single { }` for repositories and DAOs; use `viewModel { }` for ViewModels.
- Do not use `koinInject()` inside composables if a `koinViewModel()` call is sufficient.

---

## Testing Guidelines

- Unit tests live in `app/src/test/`.
- Instrumentation/UI tests live in `app/src/androidTest/`.
- Use **MockK** for mocking (`mockk<Type>(relaxed = true)`).
- Use **Turbine** (`flow.test { }`) to assert `StateFlow` / `Flow` emissions in ViewModel tests.
- Use `kotlinx.coroutines.test.runTest` for all coroutine-based tests.
- Mirror the production source tree in the test tree (e.g. `viewmodel/DiceViewModelTest.kt`).
- Every new use case, repository method, and ViewModel action should have a corresponding test.
- Coverage target: **100%** for ViewModel & Repository logic; **≥ 90%** for UI composables.
- Run coverage with: `./gradlew koverReportDebug`

---

## Build & Verification

- Build debug APK: `./gradlew assembleDebug`
- Run unit tests: `./gradlew testDebugUnitTest`
- Run coverage: `./gradlew koverReportDebug`
- After any code change, always verify with `assembleDebug` before declaring the task done.

---

## Documentation

| File | Purpose |
|------|---------|
| `CLAUDE.md` | Agent rules and project conventions (this file) |
| `COVERAGE_STRATEGY.md` | Test coverage goals, tooling notes, and thresholds |
| `TECH_DEBT.md` | Running log of known tech debt, shortcuts, and deferred improvements |

When adding a new major feature, a new screen, or changing architecture patterns, update the relevant `.md` file(s) to keep documentation current.

