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



