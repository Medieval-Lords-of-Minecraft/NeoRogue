# Plan: SessionEquipment Metadata Wrapper

## TL;DR
Introduce a `SessionEquipment` class as a lightweight wrapper around `Equipment` that holds a general-purpose string-keyed metadata store (int, double, string). `PlayerSessionData` internally stores `SessionEquipment[]` arrays but preserves existing `Equipment[]` API for backward compatibility. `EquipmentInstance` gains a reference to the `SessionEquipment` at fight time so triggers can read/modify session-level state.

---

## Phase 1: Core Wrapper Class

**Steps:**
1. Create `SessionEquipment.java` in `me.neoblade298.neorogue.equipment`
   - Fields: `Equipment equipment`, `HashMap<String, Object> metadata`
   - Constructor: `SessionEquipment(Equipment eq)` — empty metadata
   - Typed getters: `getInt(String key)`, `getDouble(String key)`, `getString(String key)`, `has(String key)`
   - Typed setters: `setInt(String key, int value)`, `setDouble(String key, double value)`, `setString(String key, String value)`, `remove(String key)`
   - Delegate: `getEquipment()` returns the wrapped template
   - Upgrade support: `upgrade()` returns new `SessionEquipment(equipment.getUpgraded())` preserving metadata map (shallow copy)

2. Add serialization to `SessionEquipment`:
   - `serialize()` → format: `equipId+|key1=type:value,key2=type:value` (pipe delimiter separates equipment id from metadata; no pipe = legacy format)
   - `deserialize(String)` → parses either legacy (`equipId+`) or extended format
   - Static helpers: `serialize(SessionEquipment[])`, `deserializeAsArray(String)` — mirror existing `Equipment.serialize/deserialize` signatures

---

## Phase 2: PlayerSessionData Integration (Minimal Breaking Changes)

**Steps:**
3. Replace internal `Equipment[]` fields with `SessionEquipment[]` in `PlayerSessionData`:
   - `hotbar`, `armors`, `offhand`, `accessories`, `storage`, `otherBinds` become `SessionEquipment[]`
   - Keep `getEquipment(EquipSlot)` returning `Equipment[]` — build array on the fly from `SessionEquipment[].getEquipment()` (or cache)
   - Add `getSessionEquipment(EquipSlot)` returning `SessionEquipment[]` for new code

4. Update `setEquipment(EquipSlot, int, Equipment)` to wrap in fresh `SessionEquipment`:
   - Internally: `slots[slot] = new SessionEquipment(eq)`
   - Add overload: `setEquipment(EquipSlot, int, SessionEquipment)` for when metadata should be preserved (e.g. moving between slots)

5. Update `removeEquipment(EquipSlot, int)` to return `SessionEquipment` (or keep returning `Equipment` and add `removeSessionEquipment()`)
   - Recommended: return `Equipment` from existing method (no break), add `removeSessionEquipment()` that returns `SessionEquipment`

6. Update `upgradeEquipment(EquipSlot, int)`:
   - Call `slots[slot] = slots[slot].upgrade()` which preserves metadata

7. Update serialization in `save(Connection)`:
   - Replace `Equipment.serialize(hotbar)` calls with `SessionEquipment.serialize(hotbar)` etc.
   - Update `deserializeAsArray` calls in the ResultSet constructor to use `SessionEquipment.deserializeAsArray()`
   - Format is backward-compatible: entries without `|` parse as legacy (no metadata)

8. Update `PlayerSessionData.serialize()` (admin command) similarly.

---

## Phase 3: Fight Layer Integration

**Steps:**
9. Add `SessionEquipment` field to `EquipmentInstance`:
   - New field: `protected SessionEquipment sessionEquipment`
   - Updated `init()`: accept `SessionEquipment` parameter, store reference
   - Add accessor: `getSessionEquipment()` for triggers to read/modify metadata

10. Update `EquipmentInstance` constructors to accept `SessionEquipment`:
    - Add parallel constructors or change existing ones to take `SessionEquipment` instead of `Equipment`
    - Derive `eq` from `sessionEquipment.getEquipment()` internally
    - Keep old constructors as overloads wrapping in a transient `SessionEquipment` for backward compat

11. Update `PlayerFightData` constructor to pass `SessionEquipment` into equipment initialization:
    - Change iteration from `for (Equipment acc : data.getEquipment(EquipSlot.ACCESSORY))` to `for (SessionEquipment se : data.getSessionEquipment(EquipSlot.ACCESSORY))`
    - Pass `SessionEquipment` where `EquipmentInstance` is created (or store it in a lookup map on PlayerFightData)
    - Add `PlayerFightData.getSessionEquipment(int slot, EquipSlot es)` so equipment can look up sibling session data

12. Update `Equipment.initialize()` signature (optional, can be deferred):
    - If desired, add overload: `initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq)`
    - Or: equipment reads from `EquipmentInstance.getSessionEquipment()` after creating the instance

---

## Phase 4: Inventory & Give Flow

**Steps:**
13. Update `giveEquipment()` in `PlayerSessionData`:
    - Currently calls `setEquipment(es, slot, eq)` — the updated `setEquipment` auto-wraps, so this is already handled
    - Add `giveSessionEquipment(SessionEquipment se)` for cases where pre-existing metadata should be preserved

14. Update `PlayerSessionInventory`:
    - `setupInventory()` currently reads `data.getEquipment(EquipSlot)` and calls `eq.getItem()` — still works via backward-compat API
    - For showing metadata on items (future): use `data.getSessionEquipment()` and overlay lore for durability/enchantments
    - Equipment swap/move logic uses NBT `equipId` + `dataSlot` — this still works since we look up by slot index

15. Update `PlayerSessionInventory` swap logic to preserve `SessionEquipment` when moving between slots:
    - When moving equipment from slot A to slot B, use `removeSessionEquipment()` + `setEquipment(slot, sessionEquip)` to keep metadata

---

## Phase 5: Secondary Serialization Points

**Steps:**
16. Update `ShopItem.java` serialization — shops store equipment for sale:
    - If shop items should not carry session metadata (they're templates), keep using `Equipment.serialize()` for shop persistence
    - When purchased, wrap in fresh `SessionEquipment` via normal `giveEquipment()` path

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
