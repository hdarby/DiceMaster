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

## [FEAT-003] Add consumable item management with ability to assign to characters with the ability to track quantity as well as add a simple increment/decrement button to the character screen for each assigned item
- **Type**: Add
- **Area**: `ui/screens/CharacterScreen.kt`, `ui/screens/ItemScreen.kt`, `viewmodel/ItemViewModel.kt`
- **Added**: 2026-05-22
- **Priority**: Medium
- **Status**: Not Started
- **Description**: Introduce a new feature to manage consumable items (e.g., potions, scrolls) within the app. This includes creating a new screen for item management, allowing users to create, edit, and delete items, and enabling the assignment of these items to characters. Each assigned item should have a quantity that can be tracked and modified directly from the character screen using increment/decrement buttons. This feature will enhance the app's functionality for players who want to keep track of their consumable resources in D&D.
- **Acceptance Criteria**:
    - A new `ItemScreen` allows users to create, edit, and delete consumable items.
    - Users can assign items to characters from the `CharacterScreen`.
    - Each assigned item displays its quantity on the character card.
    - Increment and decrement buttons on the character card allow users to adjust the quantity of each assigned item.
    - Changes to item quantities are reflected immediately in the UI and persist across app sessions.


<!-- Add new features below this line -->



