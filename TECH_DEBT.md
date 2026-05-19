# DiceMaster – Tech Debt Register

This file tracks known shortcuts, deferred improvements, and areas of the codebase that need attention.
Items are added here during commits when a full fix is out of scope. Resolved items should be removed.

---

## Format

Each entry follows this structure:

```
### [DEBT-NNN] Short title
- **Area**: file or layer affected
- **Added**: YYYY-MM-DD
- **Description**: What the debt is and why it was deferred.
- **Resolution**: What a proper fix would look like.
```

---

## Open Items

### [DEBT-001] `fallbackToDestructiveMigration()` used in production database builder
- **Area**: `di/AppModule.kt`
- **Added**: 2026-05-19
- **Description**: The Room database builder uses `fallbackToDestructiveMigration()`, which silently wipes all user data on any schema version bump instead of running a proper migration. The DB is already on version 2, meaning data loss has already occurred once.
- **Resolution**: Write explicit `Migration` objects for each version transition and register them via `.addMigrations(...)` in the `databaseBuilder`. Remove `fallbackToDestructiveMigration()`.

---

### [DEBT-002] `GetCharactersUseCase` is dead code
- **Area**: `domain/usecase/character/GetCharactersUseCase.kt`
- **Added**: 2026-05-19
- **Description**: The use case exists but is never registered in `AppModule.kt` and never injected or called anywhere. `GetCharactersWithWeaponsUseCase` is used everywhere instead.
- **Resolution**: Delete the file, or identify a screen where it belongs and wire it up through DI.

---

### [DEBT-003] Stat modifiers are stored rather than derived
- **Area**: `domain/model/Character.kt`, `data/local/entity/CharacterEntity.kt`, `data/repository/CharacterRepositoryImpl.kt`
- **Added**: 2026-05-19
- **Description**: `Stats` and `CharacterEntity` both store a raw stat value (e.g., `strength = 14`) and its modifier (e.g., `strengthModifier = 2`) as independent fields. In D&D, the modifier is a derived value: `(stat - 10) / 2`. Storing them separately doubles the DB columns and allows the two values to become inconsistent.
- **Resolution**: Remove all modifier columns from the DB and `CharacterEntity`. Compute the modifier as a derived property (`val strengthModifier get() = (strength - 10) / 2`) on the `Stats` domain model.

---

### [DEBT-004] `DebugViewModel.runSimulations()` executes on the main thread
- **Area**: `viewmodel/DebugViewModel.kt`
- **Added**: 2026-05-19
- **Description**: `runSimulations()` is called synchronously in `init {}` with no coroutine dispatcher. At 1000× per face, the d100 simulation generates 100,000 random numbers on the main thread, which can cause a visible UI hitch on screen entry.
- **Resolution**: Wrap the computation in `viewModelScope.launch(Dispatchers.Default) { ... }` and expose a loading state while the simulation runs.

---

### [DEBT-005] Cross-ref DAO methods duplicated across `CharacterDao` and `WeaponDao`
- **Area**: `data/local/dao/CharacterDao.kt`, `data/local/dao/WeaponDao.kt`
- **Added**: 2026-05-19
- **Description**: `insertCharacterWeaponCrossRef` and `deleteCharacterWeaponCrossRef` exist in both DAOs. These are character–weapon relationship operations and belong solely in `CharacterDao`, violating separation of concerns and creating duplication.
- **Resolution**: Remove the cross-ref methods from `WeaponDao`. Update `WeaponRepositoryImpl` if needed to route cross-ref operations through `CharacterDao` or a dedicated repository method.

---

### [DEBT-006] Error state in ViewModels is never cleared
- **Area**: `viewmodel/CharacterViewModel.kt`, `viewmodel/WeaponViewModel.kt`, `viewmodel/state/CharacterUiState.kt`, `viewmodel/state/WeaponUiState.kt`
- **Added**: 2026-05-19
- **Description**: Both ViewModels set `error: String?` in their UI state on failures, but neither exposes a `clearError()` function. Once an error is set it persists indefinitely — it is never dismissible by the user and is only replaced by the next successful data load.
- **Resolution**: Add `fun clearError()` to both ViewModels. Call it from the UI when the user dismisses the error, or automatically after a successful operation.

---

### [DEBT-007] `DiceUiState` and debug state types co-located with their ViewModels
- **Area**: `viewmodel/DiceViewModel.kt`, `viewmodel/DebugViewModel.kt`
- **Added**: 2026-05-19
- **Description**: `DiceUiState` is defined at the top of `DiceViewModel.kt`. `DieStats` and `DebugUiState` are both defined inside `DebugViewModel.kt`. `CharacterUiState` and `WeaponUiState` correctly live in dedicated files under `viewmodel/state/`. This inconsistency makes state types harder to locate and test in isolation.
- **Resolution**: Move `DiceUiState`, `DieStats`, and `DebugUiState` into their own files under `viewmodel/state/`.

---

### [DEBT-008] No input validation in `AddEditCharacterDialog` or `AddEditWeaponDialog`
- **Area**: `ui/screens/CharacterScreen.kt`, `ui/screens/WeaponScreen.kt`
- **Added**: 2026-05-19
- **Description**: Both dialogs allow the Confirm button to be tapped with blank required fields (name, race, weapon type, etc.) or otherwise invalid data. No inline validation or user feedback is provided before saving.
- **Resolution**: Disable the Confirm button while required fields are empty, or show inline error messages beneath each field. Add a validation function to the respective ViewModel if business-rule validation is needed.

---

### [DEBT-009] `ResultsContent` uses a hardcoded `height(320.dp)`
- **Area**: `ui/screens/DiceRollerScreen.kt`
- **Added**: 2026-05-19
- **Description**: The results bottom sheet content is constrained to a fixed `Modifier.height(320.dp)`. This may truncate content on small screens and wastes space on large screens or tablets. It is also a magic number with no named constant.
- **Resolution**: Replace with adaptive sizing — e.g., `Modifier.fillMaxHeight(0.5f)` — or remove the fixed height entirely and allow the `ModalBottomSheet` to size itself based on content.

---

### [DEBT-011] Room schema export is disabled
- **Area**: `data/local/DiceMasterDatabase.kt`, `app/build.gradle.kts`
- **Added**: 2026-05-19
- **Description**: `@Database(exportSchema = false)` means no schema JSON is generated or committed to source control. Without a schema history it is impossible to validate migrations against a known baseline.
- **Resolution**: Set `exportSchema = true`, configure the `room.schemaLocation` KSP argument in `build.gradle.kts`, commit the generated schema JSON files to source control, and add the output path to `.gitignore` for build artefacts only.

---

## Resolved Items

### [DEBT-010] `AssignWeaponDialog` used a fully-qualified type instead of an import ✅
- **Area**: `ui/screens/WeaponScreen.kt`
- **Resolved**: 2026-05-19
- **Resolution applied**: Added `import com.hdarby.dicemaster.domain.model.Character` and replaced the inline fully-qualified reference with the short type name.

---

### [DEBT-012] Sealed class `Screen` used `object` instead of `data object` ✅
- **Area**: `ui/navigation/Screen.kt`
- **Resolved**: 2026-05-19
- **Resolution applied**: Changed all four `object` declarations inside the `Screen` sealed class to `data object`.


