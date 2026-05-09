# Dice Master - Test Coverage Strategy

This document outlines the strategy for achieving and maintaining 100% test coverage for the Dice Master application logic.

## Current Status
- **Core Logic Coverage**: Target 100% for ViewModel and Repository.
- **Current Issues**: Kover plugin integration is experiencing "No sources" issues with the current AGP/Kotlin combination. Troubleshooting is ongoing.

## Strategy to Reach 100% Coverage

### 1. Business Logic (Repository & ViewModel)
- **Unit Tests**: Ensure every public method and private logic branch is tested.
- **Edge Cases**: 
    - Test D3, D100, and boundary values for dice sides.
    - Test maximum (10) and minimum (1) number of dice.
    - Test empty results or error states if applicable.
- **Coroutines**: Use `kotlinx-coroutines-test` and `Turbine` to verify Flow emissions and asynchronous operations in `DiceViewModel`.

### 2. UI Components (Jetpack Compose)
- **Compose UI Tests**:
    - Verify that the correct number of dice results are displayed.
    - Test the "Roll" button interaction.
    - Verify that the `ModalBottomSheet` (Results Popup) displays sorted results correctly.
    - Test theme switching and Edge-to-Edge padding.

### 3. Dependency Injection (Koin)
- **Koin Module Verification**:
    - Use `checkModules` from Koin Test to ensure all dependencies are correctly provided and can be injected.

### 4. Integration & Regression
- **Instrumentation Tests**:
    - End-to-end flow: Open app -> Configure dice -> Roll -> View results.
- **Coverage Reporting**:
    - Fix Kover integration to correctly pick up Android source sets.
    - Fallback to JaCoCo if Kover 0.9.1 remains incompatible with AGP 9.1.1.

## Maintenance
- **CI Integration**: (Future) Run coverage reports on every PR.
- **Thresholds**: Set a minimum coverage threshold (e.g., 90% for UI, 100% for Logic) to prevent regressions.
