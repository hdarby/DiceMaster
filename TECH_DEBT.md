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
- **Description**: The Room database builder uses `fallbackToDestructiveMigration()`, which silently wipes all user data on any schema version bump instead of running a proper migration. The DB is now on version 11 (1→2, 2→3 FEAT-003, 3→4 FEAT-005 totalQuantity, 4→5 schema flatten removing `character_weapon_cross_ref`, 10→11 added `armorClass` column to `characters` table).
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

### [DEBT-006] Error state in ViewModels is never cleared
- **Area**: `viewmodel/CharacterViewModel.kt`, `viewmodel/WeaponViewModel.kt`, `viewmodel/ItemViewModel.kt`, `viewmodel/state/CharacterUiState.kt`, `viewmodel/state/WeaponUiState.kt`, `viewmodel/state/ItemUiState.kt`
- **Added**: 2026-05-19 (extended 2026-05-23 to include ItemViewModel)
- **Description**: `CharacterViewModel`, `WeaponViewModel`, and `ItemViewModel` all set `error: String?` in their UI state on failures, but none of them expose a `clearError()` function. Once an error is set it persists indefinitely — it is never dismissible by the user and is only replaced by the next successful data load.
- **Resolution**: Add `fun clearError()` to all three ViewModels. Call it from the UI when the user dismisses the error, or automatically after a successful operation.

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

### [DEBT-013] No Koin module verification test
- **Area**: `di/AppModule.kt`
- **Added**: 2026-05-20
- **Description**: The `COVERAGE_STRATEGY.md` calls for a Koin `checkModules` test to validate that all DI bindings are resolvable at runtime. The `koin-test-android` library is not currently in the dependency list, so this test cannot be written without first adding the dependency.
- **Resolution**: Add `androidTestImplementation("io.insert-koin:koin-test-android:<version>")` and `androidTestImplementation("io.insert-koin:koin-test-junit4:<version>")` to `app/build.gradle.kts` and write a `KoinModuleTest` class that calls `checkModules { modules(appModule) }`.

---

### [DEBT-018] Nav argument sentinel value `-1L` for optional `editWeaponId`
- **Area**: `MainActivity.kt`, `ui/screens/WeaponScreen.kt`
- **Added**: 2026-05-22
- **Description**: Jetpack Navigation `navArgument` for primitive types (`NavType.LongType`) requires a non-null `defaultValue`. The sentinel `-1L` is used to represent "no weapon selected". This is a leaky abstraction — the sentinel leaks into `WeaponScreen` via `.takeIf { it != -1L }`.
- **Resolution**: Migrate to the `@Serializable` safe-args approach available in Navigation 2.8+ (type-safe routes with nullable parameters), which natively handles `null` for optional arguments without a sentinel value.

## Resolved Items

### [DEBT-019] Inventory quantity semantics were incorrect ✅
- **Area**: `viewmodel/ItemViewModel.kt`, `domain/usecase/item/AdjustItemStockUseCase.kt`, `data/local/dao/ItemDao.kt`, `data/repository/ItemRepositoryImpl.kt`
- **Resolved**: 2026-05-23
- **Resolution applied**: Removed `AdjustItemStockUseCase` entirely. `totalQuantity` on an item now acts purely as the initial quantity preset. `assignItem` reads `totalQuantity` from the ViewModel's existing state to seed `CharacterItemCrossRef.quantity` (instead of always defaulting to 1). `incrementQuantity` and `decrementQuantity` update only the cross-ref quantity — `totalQuantity` is never mutated after creation. All related tests updated.

### [DEBT-005] Cross-ref DAO methods duplicated across `CharacterDao` and `WeaponDao` ✅
- **Area**: `data/local/dao/CharacterDao.kt`, `data/local/dao/WeaponDao.kt`
- **Resolved**: 2026-05-23
- **Resolution applied**: Flattened the `character_weapon_cross_ref` many-to-many junction table into a direct `characterId: Long?` FK on `WeaponEntity`. `CharacterWeaponCrossRef` entity deleted. Both DAOs' `insertCharacterWeaponCrossRef` / `deleteCharacterWeaponCrossRef` methods removed and replaced with `weaponDao.assignToCharacter(weaponId, characterId)` and `weaponDao.unassignFromCharacter(weaponId)` SQL UPDATE queries. `CharacterRepositoryImpl` now accepts `WeaponDao` as a second constructor param. DB version bumped 4→5.

### [DEBT-014] `createComposeRule()` used where `createAndroidComposeRule()` was needed ✅
- **Area**: `ui/screens/CharacterScreenTest.kt`, `ui/screens/WeaponScreenTest.kt`
- **Resolved**: 2026-05-20
- **Resolution applied**: Changed the test rule in both files from `createComposeRule()` to `createAndroidComposeRule<ComponentActivity>()` and added the `androidx.activity.ComponentActivity` import. This resolves compile-time "Unresolved reference: activity" errors that prevented the androidTest module from building.

---

### [DEBT-015] Pre-existing unit test compile errors ✅
- **Area**: `data/repository/WeaponRepositoryErrorHandlingTest.kt`, `viewmodel/DiceViewModelErrorHandlingTest.kt`, `data/repository/CharacterRepositoryErrorHandlingTest.kt`
- **Resolved**: 2026-05-20
- **Resolution applied**:
  - `WeaponRepositoryErrorHandlingTest` — replaced calls to non-existent methods `getWeapons()`, `assignWeaponToCharacter()`, `unassignWeaponFromCharacter()` with the actual `WeaponRepository` API (`getAllWeapons()`); removed three tests for methods that belong to `CharacterRepository`; removed unused `CharacterWeaponCrossRef` import.
  - `DiceViewModelErrorHandlingTest` — removed non-existent `faces` named parameter from three `RollResult(...)` constructor calls.
  - `CharacterRepositoryErrorHandlingTest` — replaced `updateCharacter with null ID throws error` (which expected validation logic that doesn't exist in production code) with a correct test verifying the DAO is called.

---

### [DEBT-016] Hardcoded stat abbreviation strings in `CharacterCard` ✅
- **Area**: `ui/screens/CharacterScreen.kt`
- **Resolved**: 2026-05-20
- **Resolution applied**: Replaced six hardcoded string literals ("STR", "DEX", "CON", "INT", "WIS", "CHA") passed to `StatItem()` calls inside `CharacterCard` with `stringResource(R.string.label_stat_*)` — the resources already existed in `strings.xml`.

---

### [DEBT-017] Hardcoded `"D"` prefix in `DiceConfigurationSection` ✅
- **Area**: `ui/screens/DiceRollerScreen.kt`
- **Resolved**: 2026-05-20
- **Resolution applied**: Added `<string name="label_dice_face_prefix">D</string>` to `strings.xml` and replaced the hardcoded `prefix = "D"` in `DropdownSelector(...)` with `prefix = stringResource(R.string.label_dice_face_prefix)`.

---

### [DEBT-010] `AssignWeaponDialog` used a fully-qualified type instead of an import ✅
- **Area**: `ui/screens/WeaponScreen.kt`
- **Resolved**: 2026-05-19
- **Resolution applied**: Added `import com.hdarby.dicemaster.domain.model.Character` and replaced the inline fully-qualified reference with the short type name.

---

### [DEBT-012] Sealed class `Screen` used `object` instead of `data object` ✅
- **Area**: `ui/navigation/Screen.kt`
- **Resolved**: 2026-05-19
- **Resolution applied**: Changed all four `object` declarations inside the `Screen` sealed class to `data object`.


