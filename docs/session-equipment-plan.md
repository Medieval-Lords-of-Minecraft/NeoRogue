# Plan: Complete SessionEquipment Refactor

## TL;DR
Replace all `Equipment[]` storage, parameters, and return types with `SessionEquipment[]` throughout the codebase. No backward-compat dual API — `SessionEquipment` becomes **the** currency for owned/equipped items. `Equipment` remains the immutable template; `SessionEquipment` is the session-scoped instance with metadata. Every caller that accesses, stores, or passes equipped items will use `SessionEquipment` directly. Estimated ~150-180 change sites across ~40 files.

---

## Status: Phase 1 DONE

`SessionEquipment.java` already exists with metadata store, typed getters/setters, `upgrade()`, and full serialize/deserialize support (backward-compatible with legacy `equipId+` format).

---

## Phase 2: PlayerSessionData — Full Type Replacement (~70 changes)

### 2A. Fields & Internal Arrays
1. Change all 6 field declarations from `Equipment[]` to `SessionEquipment[]`:
   - `hotbar`, `armors`, `offhand`, `accessories`, `storage`, `otherBinds`
   - Array initialization: `new SessionEquipment[N]`

### 2B. Accessor Methods — Return `SessionEquipment[]` Directly
2. `getEquipment(EquipSlot)` → rename to `getSessionEquipment(EquipSlot)`, return `SessionEquipment[]`
   - **No backward-compat `Equipment[]` version** — all callers will be updated
   - Keep a convenience `getEquipmentTemplate(EquipSlot, int)` → returns `SessionEquipment[slot].getEquipment()` for rare cases needing raw template
3. `getArrayFromEquipSlot(EquipSlot)` → returns `SessionEquipment[]`
4. `getStorage()` → returns `SessionEquipment[]`
5. `setStorage(SessionEquipment[])` → accepts `SessionEquipment[]`
6. `getOtherBinds()` → returns `SessionEquipment[]`
7. `getOtherBind(KeyBind)` → returns `SessionEquipment`

### 2C. Mutator Methods
8. `setEquipment(EquipSlot, int, Equipment)` → **keep as convenience**, wraps in `new SessionEquipment(eq)` internally
   - Add overload: `setEquipment(EquipSlot, int, SessionEquipment)` for metadata-preserving moves
9. `removeEquipment(EquipSlot, int)` → returns `SessionEquipment` (was `Equipment`)
10. `upgradeEquipment(EquipSlot, int)` → calls `slots[slot] = slots[slot].upgrade()`

### 2D. Give Methods — Accept Both Types
11. `giveEquipment(Equipment eq)` → wraps in `new SessionEquipment(eq)`, delegates to new core method
12. Add `giveEquipment(SessionEquipment se)` as the core implementation
13. `giveEquipmentSilent(Equipment eq)` → same wrapping pattern
14. `giveEquipment(ArrayList<? extends Equipment>)` → wraps each item

### 2E. Storage Methods
15. `sendToStorage(Equipment eq)` → keep convenience, wraps in `SessionEquipment`
16. Add `sendToStorage(SessionEquipment se)` as core

### 2F. Query Methods
17. `aggregateEquipment(Predicate<EquipmentMetadata>)` → `EquipmentMetadata` now wraps `SessionEquipment` instead of `Equipment`
    - `EquipmentMetadata.getEquipment()` still available (delegates to `se.getEquipment()`)
    - Add `EquipmentMetadata.getSessionEquipment()` → returns the `SessionEquipment`
18. `countOwnedWeapons(Equipment[] storageOverride)` → `countOwnedWeapons(SessionEquipment[] storageOverride)`
19. `countOwnedUnlimitedAmmunition(Equipment[] storageOverride)` → same pattern
20. `getAllEquips()` → returns `SessionEquipment[][]`
21. `countOwnedEquipment(Predicate, SessionEquipment[])` → updated parameter

### 2G. Serialization
22. DB save: `Equipment.serialize(hotbar)` → `SessionEquipment.serialize(hotbar)` (×6 for each slot)
23. DB load: `Equipment.deserializeAsArray(...)` → `SessionEquipment.deserializeAsArray(...)` (×6)
24. Memory serialize: same replacement (×6 for serialize, ×6 for deserialize)
25. Format is backward-compatible: legacy strings without `|` parse correctly

### 2H. Reforge System
26. `ReforgePairData` inner class:
    - `results` field stays `Equipment[]` (reforge outputs are templates, not session instances)
    - `EquipmentMetadata` fields updated to hold `SessionEquipment`
27. `computeAvailableReforges()` → iterates `SessionEquipment[]`, extracts `.getEquipment()` for reforge lookup

---

## Phase 3: Fight Layer — Full Integration (~15 changes)

### 3A. EquipmentInstance
28. Add field: `protected SessionEquipment sessionEquipment`
29. Update all 3 constructors to accept `SessionEquipment` instead of `Equipment`:
    - `EquipmentInstance(PlayerFightData, SessionEquipment, int slot, EquipSlot)`
    - Derive `eq = sessionEquipment.getEquipment()` internally
30. Add `getSessionEquipment()` accessor
31. Update `init()` to store `sessionEquipment` reference

### 3B. Equipment.initialize() Signature
32. Change abstract signature: `initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot)`
    → `initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq)`
    - **All ~200 equipment subclasses** must update their `@Override` signature
    - Most subclasses simply add the parameter and ignore it (can pass to `EquipmentInstance` constructor)
    - Equipment that needs session state reads from `sessionEq` directly

### 3C. PlayerFightData Constructor
33. Update all initialization loops to use `SessionEquipment`:
    ```java
    for (SessionEquipment se : data.getSessionEquipment(EquipSlot.ACCESSORY)) {
        if (se == null) continue;
        se.getEquipment().initialize(this, null, EquipSlot.ACCESSORY, i++, se);
    }
    ```
34. Update cleanup loops to match:
    ```java
    for (SessionEquipment se : data.getSessionEquipment(EquipSlot.ACCESSORY)) {
        if (se == null) continue;
        se.getEquipment().cleanup(this);
    }
    ```

### 3D. PlayerFightData Session Access
35. Add `getSessionEquipment(EquipSlot, int)` → looks up `SessionEquipment` from `PlayerSessionData`
    - Allows triggers to access sibling equipment's session state

---

## Phase 4: Inventory UI — All Callers Updated (~25 changes)

### 4A. PlayerSessionInventory
36. `setupInventory()` — change all `data.getEquipment(...)` to `data.getSessionEquipment(...)`:
    - Armor loop: `SessionEquipment a = data.getSessionEquipment(EquipSlot.ARMOR)[iter]`
    - Accessories loop: same pattern
    - Hotbar loop: same pattern
    - Keybind loop: same pattern
    - Offhand access: `SessionEquipment o = data.getSessionEquipment(EquipSlot.OFFHAND)[0]`
    - Each calls `.getEquipment().getItem()` for the display ItemStack
37. Swap/move logic: use `removeEquipment()` (now returns `SessionEquipment`) + `setEquipment(slot, se)` to preserve metadata
38. All local variables: `Equipment eq` → `SessionEquipment se`, access template via `.getEquipment()`

### 4B. StorageInventory
39. `data.getStorage()` now returns `SessionEquipment[]` — update all local variables
40. `getLiveStorageSnapshot()` → returns `SessionEquipment[]`
41. `newSave` array → `SessionEquipment[]`
42. `data.setStorage(newSave)` works with updated type

### 4C. AvailableReforgesInventory
43. Local vars: `SessionEquipment[] arr1 = data.getSessionEquipment(m1.getEquipSlot())`
44. `removeEquipment` / `setEquipment` calls use `SessionEquipment`

### 4D. ReforgeConfirmInventory & ReforgeOptionsInventory
45. `giveEquipment` calls pass `Equipment` templates (from reforge results) — convenience overload wraps automatically
46. `reforgeOptions.get()` still returns `Equipment[]` (reforge outputs are templates)

### 4E. EquipmentChoiceInventory
47. `giveEquipment(eq)` calls keep passing `Equipment` — convenience wrapper handles it

### 4F. ShopInventory
48. `trySellItem(...)` parameter: `Equipment[] storageSnapshot` → `SessionEquipment[] storageSnapshot`
49. Purchase flow: `giveEquipment(eq)` wraps automatically

---

## Phase 5: Equipment Subclass Callers (~20 changes)

### 5A. Weapons Accessing Offhand
50. MirrorSickle, PhantasmalKiller, ShieldPike, TreeTrunk — all access `data.getSessionData().getEquipment(EquipSlot.OFFHAND)[0]`
    → `data.getSessionData().getSessionEquipment(EquipSlot.OFFHAND)[0]`
    - Then `.getEquipment()` for template comparison / null check

### 5B. Consumables
51. AlchemistsPotion: `sdata.getEquipment(EquipSlot.HOTBAR)` → `sdata.getSessionEquipment(EquipSlot.HOTBAR)`
    - `.initialize()` call gets extra `sessionEq` parameter
52. Consumable.java base: `removeEquipment` now returns `SessionEquipment`

### 5C. Accessories  
53. DaedalusHammer: uses `aggregateEquipment()` — EquipmentMetadata updated, no change needed at call site

---

## Phase 6: Chance Events & Rewards (~20 changes)

### 6A. Chance Events Using aggregateEquipment
54. FaerieGroveChance, LabChance, LostRelicChance, ShiningLightChance — all call `aggregateEquipment(filter)`
    - Filter predicates may reference `.getEquipment()` for template properties
    - FaerieGroveChance also calls `removeEquipment` and `setEquipment` — updated types

### 6B. Chance Events Calling giveEquipment
55. ~15 chance files call `giveEquipment(eq)` with `Equipment` templates — convenience wrapper handles all of these, no change needed at call sites

### 6C. Reward Files
56. EquipmentReward: `giveEquipment(eq)` — convenience wrapper, no change needed
57. EquipmentChoiceReward: uses `Equipment.deserializeAsArrayList` / `Equipment.serialize(ArrayList)` — these serialize **template** equipment for reward definitions, NOT session instances. Keep using `Equipment` serialization here.

### 6D. Cursed Equipment
58. RustySword, MangledBow, GnarledStaff, DullDagger — call `giveEquipment(eq)` — convenience wrapper, no change needed

---

## Phase 7: Static/Constant Equipment[] — No Change Needed

59. `Equipment.getReforgeOptions()` returns `TreeMap<Equipment, Equipment[]>` — these are **template** references for the reforge system, not session instances. **Keep as `Equipment[]`**.
60. `ShopContents.GEMS = new Equipment[]` — template constants. **Keep as `Equipment[]`**.
61. `Equipment.serialize(Equipment[])` / `Equipment.deserializeAsArray()` — keep for template serialization (rewards, shops, reforges). `SessionEquipment` has its own parallel methods.
62. Drop table methods (`getDrop`, `getWeapon`, etc.) return `Equipment` / `ArrayList<Equipment>` — templates. **No change**.

---

## Phase 8: Cleanup.initialize() Signature Migration (~200 subclasses)

This is the largest mechanical change. Every `Equipment` subclass overrides `initialize()`.

### Strategy: Two-pass approach
63. **Pass 1 — Change abstract signature** in `Equipment.java`:
    ```java
    public abstract void initialize(PlayerFightData data, Trigger bind, 
                                     EquipSlot es, int slot, SessionEquipment sessionEq);
    ```
64. **Pass 2 — Batch-update all subclasses** using find-and-replace:
    - Pattern: `initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot)`
    - Replace: `initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq)`
    - Most subclasses can ignore `sessionEq` for now
    - Subclasses that create `EquipmentInstance` pass `sessionEq` to the constructor

65. Similarly update `cleanup()` if it needs `SessionEquipment` access (likely not needed initially)

---

## Implementation Order

| Step | Phase | Files | Estimated Changes |
|------|-------|-------|-------------------|
| 1 | ✅ Done | `SessionEquipment.java` | Already exists |
| 2 | Phase 2A-2B | `PlayerSessionData.java` | ~25 (fields, accessors) |
| 3 | Phase 2C-2F | `PlayerSessionData.java` | ~20 (mutators, queries) |
| 4 | Phase 2G | `PlayerSessionData.java` | ~24 (serialization) |
| 5 | Phase 3A | `EquipmentInstance.java` | ~8 (constructors, field) |
| 6 | Phase 3B | `Equipment.java` + all subclasses | ~200 (signature change) |
| 7 | Phase 3C-3D | `PlayerFightData.java` | ~15 (init/cleanup loops) |
| 8 | Phase 4A | `PlayerSessionInventory.java` | ~10 |
| 9 | Phase 4B | `StorageInventory.java` | ~6 |
| 10 | Phase 4C-4F | Other inventory files | ~8 |
| 11 | Phase 5 | Weapon/consumable/accessory files | ~8 |
| 12 | Phase 6 | Chance events + rewards | ~10 |

### Dependency Graph
```
Phase 2 (PlayerSessionData types)
  ├─→ Phase 3 (Fight layer: EquipmentInstance + initialize signature)
  │     └─→ Phase 8 (all subclass signatures — can be batched)
  ├─→ Phase 4 (Inventory UI callers)
  ├─→ Phase 5 (Equipment subclass callers)
  └─→ Phase 6 (Chance events & rewards)
```
Phases 4-6 are independent of each other but all depend on Phase 2.
Phase 8 (subclass signatures) depends on Phase 3B (abstract signature change) and should be done as a single batch.

---

## Key Design Decisions

1. **No dual API**: `getEquipment()` is renamed to `getSessionEquipment()` — forces all callers to update. Cleaner than maintaining two parallel accessor sets.
2. **Convenience overloads for Equipment**: `giveEquipment(Equipment)`, `setEquipment(EquipSlot, int, Equipment)`, and `sendToStorage(Equipment)` auto-wrap in `SessionEquipment`. This keeps chance events, rewards, and shops clean since they deal in templates.
3. **Reforge/drop/shop systems stay Equipment**: Template-level systems don't need session state. Only owned/equipped items become `SessionEquipment`.
4. **initialize() gets sessionEq parameter**: Rather than looking it up indirectly, pass it directly. Most subclasses ignore it; those that need session state have it immediately.
5. **EquipmentInstance stores SessionEquipment**: Any trigger can access session metadata via `inst.getSessionEquipment()`.

17. Update `EquipmentChoiceReward` / `EquipmentReward`:
    - Same as shops — rewards give template `Equipment`, wrapping happens in `giveEquipment()`
    - No change needed unless rewards should carry metadata

18. Update `PlayerSessionData` admin serialize/deserialize (used in `CmdAdminEquipment`):
    - Update to use `SessionEquipment` format

---

## Relevant Files

| File | Change |
|------|--------|
| `equipment/SessionEquipment.java` | **NEW** — wrapper + metadata + serialization |
| `equipment/EquipmentInstance.java` | Add `SessionEquipment` field + constructors |
| `player/PlayerSessionData.java` | Internal array swap, new accessors, serialization update |
| `session/fight/PlayerFightData.java` | Pass `SessionEquipment` during init loop |
| `player/inventory/PlayerSessionInventory.java` | Swap logic preserves metadata |
| `equipment/Equipment.java` | Reference only (no changes) |

---

## Verification

1. **Compile check**: `mvn package -Pdeploy-local` passes with no errors
2. **Backward compat**: All existing `getEquipment(EquipSlot)` callers still compile and return correct `Equipment[]`
3. **Serialization round-trip**: Serialize a `SessionEquipment` with metadata → deserialize → verify metadata preserved
4. **Legacy deserialization**: Old save data without `|` metadata still loads correctly
5. **Upgrade preservation**: Call `upgradeEquipment()`, verify metadata carries over
6. **Fight initialization**: Enter a fight, verify `EquipmentInstance.getSessionEquipment()` is non-null and metadata is accessible
7. **Slot swap**: Move equipment between inventory slots, verify metadata persists

---

## Decisions

- **Minimal API break**: `getEquipment()` still returns `Equipment[]`; new code uses `getSessionEquipment()`
- **String-keyed metadata**: Flexible NBT-like store with typed getters (`getInt`, `getDouble`, `getString`)
- **Inline serialization**: Same SQL columns, pipe-delimited metadata appended to equipment ID
- **Upgrades carry metadata**: `SessionEquipment.upgrade()` copies metadata map to upgraded variant
- **Shops/rewards stay template-based**: Only wrap in `SessionEquipment` at point of giving to player

---

## Further Considerations

1. **getEquipment() performance**: Building `Equipment[]` from `SessionEquipment[]` on every call adds allocation. Consider caching or having callers migrate to `getSessionEquipment()` over time.
2. **Metadata display on items**: Phase 4 step 14 is scaffolded but actual lore overlay (showing durability bar, enchantment names) is a separate follow-up feature once the data layer is in place.
3. **ArtifactInstance alignment**: `ArtifactInstance` already has instance-level state. Consider whether it should also get a metadata map for consistency, or leave it as-is since artifacts have a fixed schema (amount).
