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

## Project-Specific Conventions

### Naming
- Equipment IDs: PascalCase matching class name
- Trigger actions: Lambda expressions with `(pdata, in) ->`
- Static get methods: `public static Equipment get() { return Equipment.get(ID, false); }`

### Timing
- **Player Tick Duration**: One player tick (`Trigger.PLAYER_TICK`) = 20 game ticks = 1 second
- Example: To trigger every 2 seconds, check if tick count >= 40

### Status/Effect Integration  
- Use `GlossaryTag` for consistent descriptions
- Status application through `FightInstance.applyStatus()`
- Shields managed via `FightData` methods with `p.getUniqueId()` as applier
- Particle/sound effects via `ParticleContainer`/`SoundContainer` (see [Particle Instructions](particle-instructions.md))

### Item Description Formatting
When creating equipment descriptions in `setupItem()`, follow these patterns:

**GlossaryTag Usage:**
```java
// Basic status/effect tags
GlossaryTag.POISON.tag(this)  // Just the tag name
GlossaryTag.SHIELDS.tag(this, amount, true)  // Tag with value (true = yellow if upgradable)

// Status application format
"applies " + GlossaryTag.POISON.tag(this, stacks, true) + " [<white>5s</white>]"
"gain " + GlossaryTag.SHIELDS.tag(this, amount, true) + " [<white>10s</white>]"
```

**Color Formatting:**
- **Yellow values** (`<yellow>` or `DescUtil.yellow()`): Used for values that change with upgrades
- **White values** (`<white>` or `DescUtil.white()`): Used for fixed values, durations, thresholds
- **Duration format**: `[<white>5s</white>]` for fixed durations, `[<yellow>10s</yellow>]` if upgradable

### Error Patterns
- Missing equipment registration causes lookup failures
- Incorrect trigger types lead to ClassCastException on event objects
- Shield/status timing issues with decay/duration parameters

Focus on the trigger system when implementing equipment - most functionality flows through PlayerFightData trigger attachment in the `initialize()` method.