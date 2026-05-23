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

### [FEAT-006] Firebase sync with role-based access for DM and players
- **Type**: Add
- **Area**: `data/remote/`, `domain/repository/`, `di/AppModule.kt`, `ui/screens/`, `viewmodel/`, Firebase Console
- **Added**: 2026-05-23
- **Priority**: High
- **Status**: Backlog
- **Description**: Introduces real-time data synchronisation via **Firebase Firestore** (NoSQL) so that a Dungeon Master and their players can share a live view of campaign data from their own devices. On first launch (or from a Settings screen) the user identifies themselves as either the DM or a specific player character. The DM has full read/write access to all campaign data. Each player can only read and interact with the data belonging to their own character. Firestore Security Rules enforce these access boundaries server-side so no client-side workaround can expose another player's data.
- **Implementation Notes**:
  - Add `firebase-bom`, `firebase-firestore-ktx`, and `firebase-auth-ktx` dependencies.
  - Use **Firebase Anonymous Auth** (or Google Sign-In) to give every device a stable UID; map that UID to a role (`dm` or a `characterId`) in a top-level `roles/{uid}` Firestore document.
  - Firestore data model (top level):
    - `sessions/{sessionId}/characters/{characterId}` — character profile, stats, assigned weapons.
    - `sessions/{sessionId}/weapons/{weaponId}` — weapon definitions.
    - `sessions/{sessionId}/items/{itemId}` — consumable item definitions.
    - `sessions/{sessionId}/characterItems/{characterId}/entries/{itemId}` — per-character item quantities (cross-refs).
  - Security Rules: DM UID may read/write all paths under `sessions/{sessionId}/**`; a player UID may read/write only `characters/{their characterId}` and `characterItems/{their characterId}/**`; item and weapon definitions are readable by all session members but writable only by the DM.
  - Introduce a `RemoteDataSource` abstraction in `data/remote/` so the repository layer stays technology-agnostic and local-only mode (Room) continues to work offline or without a session.
  - On first launch show a **Session Setup screen** where the user enters a session code (or creates one as DM). The DM's device generates the session and shares the code; players enter it to join.
  - Changes made locally (by the DM or a player) propagate in real-time to all other connected devices via Firestore snapshot listeners.
- **Acceptance Criteria**:
  - A new Session Setup screen lets a user create a session (DM) or join an existing one (player) by entering a session code.
  - A player joining a session selects which character they are from a list provided by the DM; they can only see and interact with that character's card, items, and quantities.
  - The DM sees all characters, weapons, and items — identical to the current local experience — and all writes are reflected on player devices within a few seconds.
  - A player incrementing or decrementing an item quantity updates the shared Firestore document; the DM's device reflects the change in real-time without a manual refresh.
  - Firestore Security Rules are deployed alongside the app and block any attempt by a player UID to read another character's data or modify DM-only collections (weapons, item definitions).
  - The app continues to work fully offline using the local Room database when no session is active; syncing is opt-in, not required.
  - All new remote repository methods are covered by unit tests using a faked/mocked `RemoteDataSource`.

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



