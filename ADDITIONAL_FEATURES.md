# DiceMaster – Additional Features

This file is a living backlog of features to add, modify, or remove from the app.
Use the **Status** field to track progress. Remove entries once they are shipped and the `TECH_DEBT.md` / `README.md` are updated accordingly.

---

## Format

Each entry follows this structure:

```
### [FEAT-NNN] Short title
- **Type**: Add | Edit | Delete
- **Area**: screen, layer, or component affected
- **Added**: YYYY-MM-DD
- **Priority**: High | Medium | Low
- **Status**: Backlog | In Progress | Done
- **Description**: What the feature is and why it is valuable.
- **Acceptance Criteria**: Bullet list of observable behaviours that confirm the feature is complete.
```

---

## Features

### [FEAT-001] Edit assigned weapon from Character screen
- **Type**: Add
- **Area**: `ui/screens/CharacterScreen.kt`, `ui/screens/WeaponScreen.kt`, `MainActivity.kt`
- **Added**: 2026-05-22
- **Priority**: High
- **Status**: Done
- **Description**: When a player taps an assigned weapon chip on a character card, they are navigated to the Weapon Repository screen with the `AddEditWeaponDialog` auto-opened and pre-populated with that weapon's data.
- **Acceptance Criteria**:
  - Tapping a weapon chip on a character card opens `AddEditWeaponDialog` pre-populated with that weapon's fields. ✅
  - Saving the dialog calls `WeaponViewModel.updateWeapon()` and updates the weapon. ✅
  - Dismissing the dialog makes no changes. ✅

---

### [FEAT-002] Add advantage/disadvantage rolls for D20
- **Type**: Add
- **Area**: `ui/screens/DiceRollerScreen.kt`, `domain/model/`, `domain/usecase/`, `viewmodel/DiceViewModel.kt`, `di/AppModule.kt`
- **Added**: 2026-05-22
- **Priority**: Medium
- **Status**: Done
- **Description**: Add buttons to the Dice Roller screen when a D20 is selected, allowing the user to indicate that they wish to perform a roll for advantage or disadvantage. When tapped, this will cause two D20 results to be generated and displayed, with the highest result highlighted for advantage and the lowest result highlighted for disadvantage. This feature enhances the app's utility for D&D players by supporting a common game mechanic directly in the dice roller.
- **Acceptance Criteria**:
    - Tapping advantage roll produces two D20 results with the higher value highlighted. ✅
    - Tapping disadvantage roll produces two D20 results with the lower value highlighted. ✅
    - Advantage/Disadvantage buttons only appear when D20 is selected. ✅
    - Critical hit (20) and critical miss (1) styling applies to the selected die. ✅

---

### [FEAT-003] Add consumable item management with character assignment and quantity tracking
- **Type**: Add
- **Area**: `ui/screens/ItemScreen.kt`, `ui/screens/CharacterScreen.kt`, `viewmodel/ItemViewModel.kt`, `domain/`, `data/`, `di/AppModule.kt`
- **Added**: 2026-05-22
- **Priority**: Medium
- **Status**: Done
- **Description**: Introduces a new Item Repository screen for managing consumable items (potions, scrolls, etc.) with full CRUD. Items can be assigned to characters from the Character screen. Each assigned item shows a quantity with + / − buttons to adjust it. Decrementing to zero automatically unassigns the item.
- **Acceptance Criteria**:
    - A new `ItemScreen` (Items tab in bottom nav) allows users to create, edit, and delete consumable items. ✅
    - Users can assign items to characters via an "Add Item" button on each CharacterCard. ✅
    - Each assigned item displays its quantity on the character card. ✅
    - Increment and decrement buttons adjust quantity; decrement at 1 unassigns the item. ✅
    - Changes to item quantities persist across app sessions (Room database). ✅


<!-- Add new features below this line -->

### [FEAT-007] Hit Points tracking with death saving throws
- **Type**: Add
- **Area**: `domain/model/Character.kt`, `data/local/entity/CharacterEntity.kt`, `data/local/dao/CharacterDao.kt`, `data/repository/CharacterRepositoryImpl.kt`, `ui/screens/CharacterScreen.kt`, `viewmodel/CharacterViewModel.kt`
- **Added**: 2026-05-24
- **Priority**: High
- **Status**: Done
- **Description**: Add Hit Points (HP) to each character, including a maximum HP value set at creation/edit and a current HP value adjusted in the Character screen via increment/decrement buttons. When current HP reaches zero the character is marked as "Down" and a death saving throw tracker (three checkboxes) appears. If all three are checked the app confirms whether the character should be marked as Dead.
- **Acceptance Criteria**:
  - `Character` domain model gains `maxHitPoints: Int` and `currentHitPoints: Int` fields.
  - `AddEditCharacterDialog` includes a "Max HP" numeric field (integer ≥ 1); new characters default `currentHitPoints` to `maxHitPoints`.
  - Each `CharacterCard` shows current / max HP (e.g. `12 / 20 HP`).
  - An increment (+) button heals the character; the value cannot exceed `maxHitPoints`.
  - A decrement (−) button deals damage; the value cannot go below 0.
  - When `currentHitPoints == 0`, the character card displays a **"Down"** label and shows a row of three death-saving-throw checkboxes.
  - Checking the third death-saving-throw checkbox triggers a confirmation dialog: "Has [name] failed all saving throws and should be declared Dead?".
  - Confirming marks the character as `isDead = true` and displays a **"Dead"** label in place of the Down state.
  - All HP and death-throw state is persisted in Room and synced to Firestore when a session is active.
  - Unit tests cover ViewModel increment/decrement clamping, Down state transitions, and Death confirmation logic.
  - `./gradlew assembleDebug testDebugUnitTest` passes.

---

### [FEAT-008] Soft-delete characters with confirmation dialog
- **Type**: Edit
- **Area**: `domain/model/Character.kt`, `data/local/entity/CharacterEntity.kt`, `data/local/dao/CharacterDao.kt`, `data/repository/CharacterRepositoryImpl.kt`, `ui/screens/CharacterScreen.kt`, `viewmodel/CharacterViewModel.kt`
- **Added**: 2026-05-24
- **Priority**: High
- **Status**: Backlog
- **Description**: Replace the current hard-delete of characters with a soft-delete pattern. Deleting a character first presents a confirmation dialog; if confirmed the character is marked `isDeleted = true` in the database rather than being physically removed, preserving historical data and enabling future restore functionality.
- **Acceptance Criteria**:
  - Tapping the delete icon on a `CharacterCard` opens a confirmation dialog: "Delete [name]? This character will be removed from the active roster but can be restored later."
  - Cancelling the dialog performs no action.
  - Confirming sets `isDeleted = true` on the `CharacterEntity` in Room (no row is deleted).
  - `CharacterDao.getAllCharacters()` filters out soft-deleted characters (`WHERE isDeleted = 0`).
  - Soft-deleted characters are not synced to Firestore as active characters; remote sync marks the Firestore document with `deleted: true`.
  - The `CharacterRepository` interface retains `deleteCharacter(character)` but its implementation performs the soft-delete.
  - Unit tests cover: confirmation dialog → soft-delete path; no-confirm → no change; deleted characters are excluded from the active list.
  - `./gradlew assembleDebug testDebugUnitTest` passes.

---

### [FEAT-009] Armor Class field on characters
- **Type**: Add
- **Area**: `domain/model/Character.kt`, `data/local/entity/CharacterEntity.kt`, `data/local/dao/CharacterDao.kt`, `data/repository/CharacterRepositoryImpl.kt`, `ui/screens/CharacterScreen.kt`, `viewmodel/CharacterViewModel.kt`
- **Added**: 2026-05-24
- **Priority**: Medium
- **Status**: Backlog
- **Description**: Add an editable Armor Class (AC) field to each character in line with D&D rules. AC is set in the `AddEditCharacterDialog` and displayed prominently on the `CharacterCard`. It can be updated inline on the card without opening the full edit dialog.
- **Acceptance Criteria**:
  - `Character` domain model gains `armorClass: Int` (default 10, per D&D base AC rules).
  - `AddEditCharacterDialog` includes an "Armor Class" numeric field (integer ≥ 0).
  - Each `CharacterCard` displays the AC value (e.g. `AC 15`) alongside the character's stats.
  - The AC value is editable directly on the card via a small edit control (tap-to-edit or stepper), without requiring the full edit dialog.
  - `armorClass` is persisted in Room and synced to Firestore when a session is active.
  - Unit tests cover ViewModel AC update logic and mapping.
  - `./gradlew assembleDebug testDebugUnitTest` passes.

---

<!--
  FEAT-006 is a multi-session feature broken into 8 sequential sub-tasks (a–h).
  Each sub-task is independently completable and leaves the app in a fully
  buildable, runnable state. Implement them in order; do not start a sub-task
  until the previous one is committed and passing tests.
-->

### [FEAT-006] Firebase sync with role-based access for DM and players *(parent — see sub-tasks below)*
- **Type**: Add
- **Area**: `data/remote/`, `domain/`, `di/AppModule.kt`, `ui/screens/`, `viewmodel/`, Firebase Console
- **Added**: 2026-05-23
- **Priority**: High
- **Status**: In Progress
- **Description**: Real-time Firestore sync so the DM and players share live campaign data. The DM has full read/write access; each player sees and edits only their own character. Broken into sub-tasks FEAT-006a through FEAT-006h — track progress there.

---

### [FEAT-006a] Firebase project setup and Gradle dependencies
- **Type**: Add
- **Area**: `app/build.gradle.kts`, `build.gradle.kts`, `google-services.json`, `libs.versions.toml`
- **Added**: 2026-05-23
- **Priority**: High
- **Status**: Done
- **Depends on**: nothing
- **Description**: Wire Firebase into the build system. No runtime behaviour changes — the app continues to work exactly as before. This step exists solely to land the dependency changes in isolation so subsequent sub-tasks have clean ground to build on.
- **Acceptance Criteria**:
  - `google-services.json` is added to `app/` (obtained from the Firebase Console after creating a project).
  - `com.google.gms.google-services` plugin is applied in both `build.gradle.kts` files.
  - `libs.versions.toml` declares `firebase-bom`, `firebase-firestore-ktx`, and `firebase-auth-ktx` version refs.
  - `app/build.gradle.kts` adds the three Firebase dependencies via the BOM.
  - `./gradlew assembleDebug` passes with no errors.
  - `google-services.json` is added to `.gitignore`; a `google-services.json.example` placeholder is committed instead.

---

### [FEAT-006b] Firebase Anonymous Auth + session/role domain model
- **Type**: Add
- **Area**: `domain/model/`, `data/local/` (DataStore or SharedPreferences), `di/AppModule.kt`
- **Added**: 2026-05-23
- **Priority**: High
- **Status**: Done
- **Depends on**: FEAT-006a
- **Description**: Establish the identity foundation. Every device signs in anonymously via Firebase Auth to receive a stable UID. A `UserRole` sealed class (`DungeonMaster`, `Player(characterId: Long)`) and a `Session` domain model (sessionId, role) are added to the domain layer. The current role and session ID are persisted locally (Jetpack DataStore) so the user is not asked again after the first setup. No UI changes yet — the app still opens directly to the existing screens.
- **Acceptance Criteria**:
  - `UserRole` sealed class exists in `domain/model/` with `DungeonMaster` and `Player(characterId: Long)` variants.
  - `Session` data class exists in `domain/model/` with `sessionId: String` and `role: UserRole`.
  - `SessionRepository` interface exists in `domain/repository/` with `suspend fun getActiveSession(): Session?` and `suspend fun saveSession(session: Session)`.
  - `SessionRepositoryImpl` persists the session to DataStore; `FirebaseAuthDataSource` handles anonymous sign-in and exposes the current UID.
  - Koin provides `SessionRepository`, `FirebaseAuthDataSource`.
  - Unit tests cover `SessionRepositoryImpl` read/write and the anonymous sign-in path (mockked `FirebaseAuth`).
  - `./gradlew assembleDebug testDebugUnitTest` passes.

---

### [FEAT-006c] Session Setup screen — create (DM) and join (player)
- **Type**: Add
- **Area**: `ui/screens/SessionSetupScreen.kt`, `viewmodel/SessionViewModel.kt`, `ui/navigation/`, `MainActivity.kt`
- **Added**: 2026-05-23
- **Priority**: High
- **Status**: Done
- **Depends on**: FEAT-006b
- **Description**: A new Session Setup screen shown on first launch (or when no active session is stored). The DM taps "Create Session", which generates a short alphanumeric session code and saves their role. A player taps "Join Session", enters the code, and is shown a list of characters that the DM has created; they pick one and their `Player(characterId)` role is saved. After setup the user is taken to the normal app flow. A "Leave Session" option in a Settings screen clears the stored session so setup is shown again.
- **Acceptance Criteria**:
  - On fresh install (or after leaving a session), `SessionSetupScreen` is the first screen shown.
  - "Create Session" generates a UUID-based session code, displays it for sharing, and writes the session to Firestore (`sessions/{sessionId}`) with `createdBy: uid`.
  - "Join Session" accepts a session code, validates it against Firestore, and fetches the character list for that session.
  - The player selects a character; the app saves `Session(sessionId, Player(characterId))` to DataStore and navigates to the main screen.
  - If a session is already stored, the Setup screen is skipped entirely on subsequent launches.
  - A "Leave Session" button (accessible from a Settings icon in any top app bar) clears DataStore and returns to `SessionSetupScreen`.
  - `SessionViewModel` exposes `uiState: StateFlow<SessionUiState>` and is covered by unit tests.
  - `./gradlew assembleDebug testDebugUnitTest` passes.

---

### [FEAT-006d] `RemoteDataSource` abstraction + Firestore character and weapon sync
- **Type**: Add
- **Area**: `data/remote/`, `domain/repository/`, `data/repository/`, `di/AppModule.kt`
- **Added**: 2026-05-23
- **Priority**: High
- **Status**: Done
- **Depends on**: FEAT-006c
- **Description**: Introduce a `RemoteDataSource` interface in `data/remote/` so the Firestore implementation is swappable and testable. Extend `CharacterRepository` and `WeaponRepository` to write to (and listen from) Firestore in addition to Room whenever an active session exists. Local-only mode (no session) remains unchanged. The Firestore data model for characters and weapons is established here: `sessions/{sessionId}/characters/{characterId}` and `sessions/{sessionId}/weapons/{weaponId}`.
- **Acceptance Criteria**:
  - `RemoteDataSource` interface in `data/remote/` declares CRUD + snapshot-listener methods for characters and weapons.
  - `FirestoreRemoteDataSource` implements it using `firebase-firestore-ktx`.
  - `CharacterRepositoryImpl` and `WeaponRepositoryImpl` accept an optional `RemoteDataSource`; when a session is active, writes go to both Room and Firestore, and a snapshot listener merges remote changes back into Room.
  - When no session is active, behaviour is identical to the pre-FEAT-006 code path.
  - `FirestoreRemoteDataSource` is bound in Koin; a `FakeRemoteDataSource` exists in `test/` for unit tests.
  - Unit tests cover: write propagates to both Room and Firestore; remote snapshot triggers Room update; no-session path skips Firestore entirely.
  - DM device changes to a character are visible on a second device (manual smoke test).
  - `./gradlew assembleDebug testDebugUnitTest` passes.

---

### [FEAT-006e] Firestore item and character-item quantity sync
- **Type**: Add
- **Area**: `data/remote/`, `data/repository/ItemRepositoryImpl.kt`, `di/AppModule.kt`
- **Added**: 2026-05-23
- **Priority**: High
- **Status**: Done
- **Depends on**: FEAT-006d
- **Description**: Extend the `RemoteDataSource` interface and `FirestoreRemoteDataSource` to cover consumable items and per-character item quantities. The Firestore paths are `sessions/{sessionId}/items/{itemId}` for item definitions and `sessions/{sessionId}/characterItems/{characterId}/entries/{itemId}` for per-character quantities. `ItemRepositoryImpl` adopts the same dual-write + snapshot-listener pattern established in FEAT-006d. A player adjusting their item count writes directly to the `characterItems/{their characterId}/entries/{itemId}` path; the DM's device reflects the change in real-time.
- **Acceptance Criteria**:
  - `RemoteDataSource` gains methods for items and character-item cross-refs.
  - `ItemRepositoryImpl` writes item definitions and cross-ref quantities to Firestore when a session is active.
  - Snapshot listeners on item definitions and character-item entries propagate remote changes back into Room.
  - A player incrementing/decrementing their quantity updates Firestore; the DM sees the change within a few seconds.
  - Unit tests cover dual-write, remote snapshot → Room update, and no-session bypass for items and cross-refs.
  - `./gradlew assembleDebug testDebugUnitTest` passes.

---

### [FEAT-006f] Firestore Security Rules
- **Type**: Add
- **Area**: `firestore.rules` (project root), Firebase Console
- **Added**: 2026-05-23
- **Priority**: High
- **Status**: Done
- **Depends on**: FEAT-006e
- **Description**: Write and deploy Firestore Security Rules that enforce the DM/player access boundary server-side. Rules are committed to the repo as `firestore.rules` and deployed via the Firebase CLI. No Android code changes are required; this is a pure backend/infra step.
- **Acceptance Criteria**:
  - `firestore.rules` is committed to the project root.
  - Rules allow the DM UID (stored in `sessions/{sessionId}.createdBy`) full read/write to all documents under `sessions/{sessionId}/**`.
  - Rules allow a player UID to read/write only `characters/{their characterId}` and `characterItems/{their characterId}/**` within the session.
  - Rules make item and weapon definitions *readable* by any session member but *writable* only by the DM.
  - Rules are validated with `firebase emulators:exec` or the Rules Playground in the Firebase Console — all expected allow/deny cases pass.
  - `firebase deploy --only firestore:rules` succeeds without errors.

---

### [FEAT-006g] UI role filtering — player view vs. DM view
- **Type**: Add
- **Area**: `ui/screens/CharacterScreen.kt`, `ui/screens/ItemScreen.kt`, `ui/screens/WeaponScreen.kt`, `viewmodel/`
- **Added**: 2026-05-23
- **Priority**: High
- **Status**: Backlog
- **Depends on**: FEAT-006f
- **Description**: Adapt the existing screens to respect the active `UserRole`. A player should see only the card for their own character and cannot access the Item Repository or Weapon Repository management screens (read-only or hidden). The DM sees the full, unfiltered experience as today. Role is read from `SessionRepository` in each ViewModel and exposed via the relevant `UiState`.
- **Acceptance Criteria**:
  - When `UserRole` is `Player(characterId)`, `CharacterScreen` shows only the card matching `characterId`; all other cards are hidden.
  - The Add Character FAB and edit/delete icon buttons are hidden for players.
  - Weapon chip taps are read-only for players (no navigation to the edit screen).
  - The Item Repository tab in the bottom nav is hidden for players (or navigates to a read-only view with no add/edit/delete controls).
  - When `UserRole` is `DungeonMaster`, all screens and controls are visible and functional as before.
  - No session active → app behaves exactly as before FEAT-006 (all screens accessible, local-only).
  - Unit tests cover the conditional UI state logic in each affected ViewModel.
  - `./gradlew assembleDebug testDebugUnitTest` passes.

---

### [FEAT-006h] Remote repository unit tests and integration smoke tests
- **Type**: Add
- **Area**: `app/src/test/`, `app/src/androidTest/`
- **Added**: 2026-05-23
- **Priority**: High
- **Status**: Backlog
- **Depends on**: FEAT-006g
- **Description**: Close test coverage for the full FEAT-006 feature set. Unit tests use `FakeRemoteDataSource` to cover all remote code paths. Instrumentation tests use the **Firebase Local Emulator Suite** to run end-to-end sync scenarios against a real (emulated) Firestore instance without hitting production.
- **Acceptance Criteria**:
  - Unit test coverage for `SessionRepositoryImpl`, `FirebaseAuthDataSource`, `SessionViewModel`, and all remote paths in `CharacterRepositoryImpl`, `WeaponRepositoryImpl`, and `ItemRepositoryImpl` reaches 100%.
  - An instrumentation test (`SyncIntegrationTest`) runs against the Firebase Emulator: DM creates a character → player device observes the change; player adjusts item quantity → DM device observes the change.
  - Security Rules are validated in the emulator: player UID attempting to read another character's document receives `PERMISSION_DENIED`.
  - `COVERAGE_STRATEGY.md` is updated to reflect remote layer coverage targets and emulator test instructions.
  - All tests pass in `./gradlew testDebugUnitTest` and `./gradlew connectedDebugAndroidTest` (with emulator running).

---

### [FEAT-004] Instrumentation tests for Item screen and item section on Character screen
- **Type**: Add
- **Area**: `androidTest/`, `ui/screens/ItemScreen.kt`, `ui/screens/CharacterScreen.kt`
- **Added**: 2026-05-23
- **Priority**: Medium
- **Status**: Backlog
- **Description**: FEAT-003 introduced `ItemScreen` composables (`ItemCard`, `AddEditItemDialog`, `ItemScreenContent`) and item-related sections on `CharacterScreen` (`ItemQuantityRow`, `AssignItemDialog`). These currently have no instrumentation test coverage. Dedicated UI tests and/or integration tests should be written to close the gap noted in `COVERAGE_STRATEGY.md`.
- **Acceptance Criteria**:
    - `ItemScreenTest` covers `ItemCard` render, edit and delete actions, the FAB → `AddEditItemDialog` flow, the edit dialog pre-population, and error/loading states.
    - `ItemWorkflowIntegrationTest` covers end-to-end add → assign to character → increment/decrement → auto-unassign at zero flow.
    - All new tests pass in CI (`connectedDebugAndroidTest`).
    - `COVERAGE_STRATEGY.md` integration test inventory is updated to reflect the new files.

---

### [FEAT-005] Total quantity field on consumable items
- **Type**: Add
- **Area**: `ui/screens/ItemScreen.kt`, `domain/model/ConsumableItem.kt`, `data/local/entity/ItemEntity.kt`, `data/repository/ItemRepositoryImpl.kt`
- **Added**: 2026-05-23
- **Priority**: Medium
- **Status**: Done
- **Description**: Each consumable item can now have a total stock quantity set at creation (or editing). This is distinct from the per-character assigned quantity — `totalQuantity` represents global stock of the item, while the cross-ref quantity tracks how many a particular character is carrying.
- **Acceptance Criteria**:
    - `AddEditItemDialog` includes a numeric "Total Quantity" field (default 1, accepts only integers ≥ 1). ✅
    - Editing an existing item pre-populates the Total Quantity field with the saved value. ✅
    - `ItemCard` in the Item Repository screen shows the stock quantity as "Stock: N". ✅
    - `totalQuantity` is persisted in the Room database (`consumable_items.totalQuantity` column). ✅



