# Dice Master - Test Coverage Strategy

This document outlines the strategy for achieving and maintaining 100% test coverage for the Dice Master application logic.

## Current Status
- **Core Logic Coverage**: Target 100% for ViewModel and Repository.
- **Integration / Instrumentation Tests**: Enhanced — see inventory below.
- **Kover Notes**: Kover plugin integration is experiencing "No sources" issues with the current AGP/Kotlin combination. Troubleshooting is ongoing.

## Integration Test Inventory (`app/src/androidTest/`)

| File | Layer | What is covered |
|------|-------|-----------------|
| `data/local/DiceMasterDatabaseTest.kt` | DB | Insert, update, delete characters & weapons; assign/unassign cross-refs; many-to-many integrity |
| `data/local/DiceMasterDatabaseEdgeCasesTest.kt` | DB | Empty-table queries; auto-generated IDs; multi-record inserts; update isolation; cascading delete behaviour; shared-weapon/multiple-weapon edge cases |
| `ui/screens/CharacterScreenTest.kt` | UI | `CharacterCard`, `StatItem`, `AddEditCharacterDialog` composables |
| `ui/screens/WeaponScreenTest.kt` | UI | `WeaponCard`, `AddEditWeaponDialog`, `AssignWeaponDialog` composables |
| `ui/screens/DiceRollerScreenTest.kt` | UI | `DiceMasterScreen` (initial state, with results, results hidden); `DiceConfigurationSection`; `ResultsContent` (no/positive/negative modifier); `ResultItem` for every die face type (d3/d4/d6/d8/d10/d12/d20/d100) including critical hit and miss states |
| `ui/screens/DebugRngScreenTest.kt` | UI | `DieStatsCard` and `StatsGrid` composables for multiple die types |
| `DiceMasterUiTest.kt` | E2E | Full-activity navigation, roll-dice→results flow, `ResultsContent` isolation |
| `DiceRollingIntegrationTest.kt` | E2E | Dice rolling workflows, debug screen navigation, consecutive rolls, state after navigation |
| `CharacterWorkflowIntegrationTest.kt` | E2E | Character create/edit/delete workflows, tab navigation |
| `WeaponAssignmentIntegrationTest.kt` | E2E | Weapon create/edit/delete, single & multiple weapon assignment to characters |

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
- Use `createAndroidComposeRule<ComponentActivity>()` for tests that require `getString()` from string resources; use `createComposeRule()` for tests using string literals only.

### 3. Dependency Injection (Koin)
- **Koin Module Verification**:
    - Use `checkModules` from Koin Test to ensure all dependencies are correctly provided and can be injected.
    - Requires adding `koin-test-android` dependency — deferred (see TECH_DEBT).

### 4. Integration & Regression
- **Instrumentation Tests**:
    - End-to-end flow: Open app → Configure dice → Roll → View results.
- **Coverage Reporting**:
    - Fix Kover integration to correctly pick up Android source sets.
    - Fallback to JaCoCo if Kover remains incompatible with current AGP.

## Maintenance
- **CI Integration**: (Future) Run coverage reports on every PR.
- **Thresholds**: Set a minimum coverage threshold (e.g., 90% for UI, 100% for Logic) to prevent regressions.
