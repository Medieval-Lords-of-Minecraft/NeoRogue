# NeoRogue Development Guide for AI Assistants

## Architecture Overview

NeoRogue is a **session-based dungeon crawler** Minecraft plugin with a sophisticated equipment and trigger system built around **PlayerFightData** as the central combat state manager.

### Core Components

- **Sessions** (`/session/`): Manage dungeon runs with party members, progression tracking, and plot management
- **Equipment System** (`/equipment/`): Modular gear with trigger-based effects (abilities, accessories, artifacts, weapons, armor)
- **Fight System** (`/session/fight/`): Real-time combat with FightData, triggers, status effects, shields, and damage handling
- **Player Management** (`/player/`): Session data, inventories, and persistent storage
- **Map/Region System** (`/map/`, `/region/`): Procedural dungeon generation and node progression

## Equipment Development

For detailed equipment development patterns, see **[Equipment Instructions](equipment-instructions.md)**

For particle effects and animations, see **[Particle Instructions](particle-instructions.md)**

Key equipment concepts:
- Equipment extends base `Equipment` class with trigger-based behavior
- Registration required in `Equipment.java` constructor
- Trigger system drives all equipment functionality through `PlayerFightData`
- Properties system handles costs, cooldowns, ranges, and upgrade tooltips

## Development Workflow

### Build & Test
- Uses Eclipse IDE (`.classpath`, `.project` files present)
- Dependencies in `/dependencies/` folder
- Compiled output in `/bin/` 
- Built jar: `NeoRogue.jar`
- After routine code edits, use normal incremental validation (`mvn compile`, the relevant task, or editor diagnostics). Do not delete generated `.class` files, run `mvn clean`, or otherwise force recompilation by default.
- Only force a rebuild when incremental validation reports a concrete stale-output problem, when a normal compile result is genuinely insufficient for the risk of the change, or when the user explicitly requests it.

### Dependencies
- **NeoCore**: Base framework (required)
- **MythicMobs**: Mob management (soft dependency)
- **WorldEdit**: Schematic loading for maps
- **PlaceholderAPI**: Variable expansion

### Key File Patterns

#### Player Data Flow
1. `Session` manages dungeon progression
2. `PlayerSessionData` holds equipment/stats outside combat  
3. `PlayerFightData` manages active combat state with triggers
4. Equipment `initialize()` attaches triggers to PlayerFightData

#### Combat Event Flow
```
Player Action → Trigger Event → Equipment Triggers → Status/Damage/Effects → FightData Updates
```

#### Equipment Categories
- **Abilities**: Active skills (cast triggers, cooldowns)
- **Accessories**: Passive effects (status triggers) 
- **Artifacts**: Meta-progression items
- **Weapons/Armor**: Stat modifications + effects
- **Consumables**: Single-use items

#### Chance Event Data
- For **per-player chance state** (e.g., individual offers/costs), store data in `PlayerSessionData.instanceData` instead of `ChanceInstance.eventData` UUID-keyed entries.
- In chance instances, keep stage IDs and payload together in `instanceData` as `stageId::payload` so save/load preserves both stage and player-specific chance data.
- Reserve `ChanceInstance.eventData` for event-wide/shared values that are not player-specific.

## Project-Specific Conventions

### Naming
- Equipment IDs: PascalCase matching class name
- Trigger actions: Lambda expressions with `(pdata, in) ->`
- Static get methods: `public static Equipment get() { return Equipment.get(ID, false); }`

### Timing
- **Player Tick Duration**: One player tick (`Trigger.PLAYER_TICK`) = 20 game ticks = 1 second
- Example: To trigger every 2 seconds, check if tick count >= 40

### Mage Wand Charge Time
Mage wands created with `EquipmentProperties.ofWand(...)` store a `chargeTime` property (in seconds) as `PropertyType.CHARGE_TIME`. In `initialize()`, always reference this property instead of hardcoding ticks:
```java
data.charge(properties.get(PropertyType.CHARGE_TIME)).then(() -> proj.start(data));
```
`PlayerFightData.charge(double seconds)` converts seconds to ticks automatically. Never write `data.charge(20)` for a wand — use the property.

### Player Reference Safety (CRITICAL)
**NEVER store Player references across trigger executions.** Bukkit recreates Player objects when players relog, making stored references stale.

**CORRECT Pattern - Fetch fresh Player each trigger:**
```java
data.addTrigger(id, Trigger.LEFT_CLICK_HIT, (pdata, in) -> {
    Player p = data.getPlayer();  // ✓ Fresh reference every trigger
    LeftClickHitEvent ev = (LeftClickHitEvent) in;
    weaponSwingAndDamage(p, data, ev.getTarget());
    return TriggerResult.keep();
});
```

**INCORRECT Pattern - Storing Player reference:**
```java
// ❌ WRONG - Don't capture Player in initialize()
public void initialize(PlayerFightData data, ...) {
    Player p = data.getPlayer();  // ❌ Becomes stale on relog
    data.addTrigger(id, Trigger.LEFT_CLICK_HIT, (pdata, in) -> {
        p.playSound(...);  // ❌ Uses stale reference
    });
}

// ❌ WRONG - Don't store Player in fields
private Player p;  // ❌ Never store Player as field
```

**Key Rules:**
- Always call `data.getPlayer()` inside trigger lambdas
- Never pass Player from `initialize()` into trigger actions
- Never store Player as a field in equipment or trigger classes
- Initialize() signature has NO Player parameter: `initialize(PlayerFightData data, ...)`

### Status/Effect Integration  
- Use `GlossaryTag` for consistent descriptions
- Status application through `FightInstance.applyStatus()`
- Shields managed via `FightData` methods with `p.getUniqueId()` as applier
- Particle/sound effects via `ParticleContainer`/`SoundContainer` (see [Particle Instructions](particle-instructions.md))

### Equipment Icon Restrictions
- Equipment with `EquipmentType.ARTIFACT` is exempt from all equipment icon material restrictions.
- `Material.SHIELD` is restricted to equipment with `EquipmentType.OFFHAND`. This restriction concerns the icon material, not equipment that grants the Shields combat effect.
- Helmet, chestplate, leggings, and boots materials are restricted to equipment with `EquipmentType.ARMOR`.
- Weapon materials, including swords, tridents, and spears, are restricted to equipment with `EquipmentType.WEAPON` or `EquipmentType.OFFHAND`.
- Potion and bottle icons, including `Material.POTION`, `Material.SPLASH_POTION`, `Material.LINGERING_POTION`, and `Material.GLASS_BOTTLE`, are restricted to equipment with `EquipmentType.CONSUMABLE`. The default water-bottle appearance of `Material.POTION` is included in this restriction.

### Particle Tuning Guidelines
- For most projectile visuals, keep particle randomness subtle: prefer `spread` values at or below `0.1`.
- For most projectile visuals, keep motion subtle: prefer `speed` values at or below `0.01`.
- Only exceed these values for intentional large-impact visuals (e.g., explosions or special telegraphs).

### Item Description Formatting
When creating equipment descriptions in `setupItem()`, follow these patterns:

**GlossaryTag Usage:**
- For new equipment, usually prefix damage amounts or damage modifiers with `GlossaryTag.GENERAL`, or with a more specific applicable glossary tag such as `PHYSICAL`, `MAGICAL`, or an elemental damage tag.
- Omit that prefix when the wording already names a distinct damage concept, such as "basic attack damage", or when a glossary prefix would make the description redundant or misleading.
```java
// Basic status/effect tags
GlossaryTag.POISON.tag(this)  // Just the tag name
GlossaryTag.SHIELDS.tag(this, amount)  // Tag with auto-colored, preview-aware value

// Status application format
"applies " + GlossaryTag.POISON.tag(this, stacks) + " [" + DescUtil.val("5s") + "]"
"gain " + GlossaryTag.SHIELDS.tag(this, amount) + " [" + DescUtil.val("10s") + "]"
```

Amount-bearing glossary tags automatically compare base and upgraded equipment. Changed values render yellow and show `base » upgraded` in upgrade previews; unchanged values render white.

**Color Formatting:**
- **Use `DescUtil.val(...)` for every displayed value in `setupItem()` descriptions** instead of writing raw color tags.
- `DescUtil.val(...)` automatically renders a value yellow when it changes in the upgraded description and white when unchanged.
- **Duration format**: Use `DescUtil.duration(seconds, isUpgradable)` for time values in descriptions

### Error Patterns
- Missing equipment registration causes lookup failures
- Incorrect trigger types lead to ClassCastException on event objects
- Shield/status timing issues with decay/duration parameters

Focus on the trigger system when implementing equipment - most functionality flows through PlayerFightData trigger attachment in the `initialize()` method.