# NeoRogue Development Guide for AI Assistants

## Architecture Overview

NeoRogue is a **session-based dungeon crawler** Minecraft plugin with a sophisticated equipment and trigger system built around **PlayerFightData** as the central combat state manager.

### Core Components

- **Sessions** (`/session/`): Manage dungeon runs with party members, progression tracking, and plot management
- **Equipment System** (`/equipment/`): Modular gear with trigger-based effects (abilities, accessories, artifacts, weapons, armor)
- **Fight System** (`/session/fight/`): Real-time combat with FightData, triggers, status effects, shields, and damage handling
- **Player Management** (`/player/`): Session data, inventories, and persistent storage
- **Map/Region System** (`/map/`, `/region/`): Procedural dungeon generation and node progression

## Equipment Development Patterns

### Equipment Structure
All equipment extends `Equipment` base class with this pattern:
```java
public class MyEquipment extends Equipment {
    private static final String ID = "MyEquipment";
    private int someValue;
    
    public MyEquipment(boolean isUpgraded) {
        super(ID, "Display Name", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF, EquipmentType.ACCESSORY);
        someValue = isUpgraded ? 5 : 3;
    }
    
    public static Equipment get() { return Equipment.get(ID, false); }
    
    @Override
    public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
        // Add triggers here - this is where the equipment's behavior is defined
    }
    
    @Override
    public void setupItem() {
        item = createItem(Material.ITEM, "Description with " + GlossaryTag.STATUS.tag(this) + " formatting.");
    }
}
```

### Equipment Registration
1. Create equipment file in appropriate subfolder (`abilities/`, `accessories/`, etc.)
2. Add import to `Equipment.java` 
3. Add instantiation in Equipment.java's constructor: `new MyEquipment(b);`

### Trigger System
Equipment behavior is driven by **triggers** attached to PlayerFightData:
```java
// Trigger when player applies poison
data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
    ApplyStatusEvent ev = (ApplyStatusEvent) in;
    if (!ev.isStatus(StatusType.POISON)) return TriggerResult.keep();
    // Equipment effect here
    return TriggerResult.keep();
});
```

Key triggers: `APPLY_STATUS`, `PRE_APPLY_STATUS`, `PRE_DEAL_DAMAGE`, `RECEIVE_DAMAGE`, `PLAYER_TICK`, `PRE_BASIC_ATTACK`

### Targeting & Area Effects System

#### TargetProperties
Used to define how abilities target entities and areas:
```java
// Radius targeting (most common)
TargetProperties tp = TargetProperties.radius(5, false, TargetType.ENEMY);

// Cone targeting  
TargetProperties tp = TargetProperties.cone(60, 5, false, TargetType.ENEMY); // 60° arc, 5 blocks range

// Line targeting
TargetProperties tp = TargetProperties.line(8, 2, TargetType.ENEMY); // 8 blocks range, 2 tolerance

// Block targeting (for ground-targeted abilities)
TargetProperties tp = TargetProperties.block(10, true); // 10 blocks range, stick to ground
```

**TargetType Options:**
- `TargetType.ENEMY` - Only targets hostile mobs
- `TargetType.ALLY` - Only targets players
- `TargetType.BOTH` - Targets both players and mobs

**Additional Properties:**
- `throughWall` - Whether targeting ignores line-of-sight
- `stickToGround` - For block targeting, whether to snap to ground level
- `tolerance` - Width/thickness for line targeting
- `arc` - Angle in degrees for cone targeting

#### Getting Entities with TargetHelper
```java
// Get entities in radius around player
for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
    // Apply effects to each entity
}

// Get entities in radius around specific location
for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, location, tp)) {
    // Apply effects
}

// Get entities in cone from player's facing direction
for (LivingEntity ent : TargetHelper.getEntitiesInCone(p, tp)) {
    // Apply effects
}

// Get entities in line from start to end location
for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, startLoc, endLoc, tp)) {
    // Apply effects
}

// Get nearest entity
LivingEntity nearest = TargetHelper.getNearest(p, tp);
```

#### Particle Shapes & Visual Effects

**Basic Shape Objects (typically static):**
```java
// Circle for radius effects
private static final Circle circ = new Circle(5); // 5 block radius

// Cone for directional effects  
private static final Cone cone = new Cone(5, 60); // 5 blocks range, 60° arc

// TargetProperties are also commonly static
private static final TargetProperties tp = TargetProperties.radius(5, false, TargetType.ENEMY);
```

**ParticleContainer Setup (typically static):**
```java
private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME)
    .count(25)          // Number of particles
    .spread(1, 0.5)     // X/Z spread, Y spread
    .offsetY(1)         // Y offset from base location
    .speed(0.1);        // Particle speed

// Sound containers are also typically static
private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_BLAZE_SHOOT);
```

**Note**: Shapes, particle containers, sound containers, and target properties are usually declared as `static final` since they don't change per equipment instance and can be shared across all instances of the equipment class.

**Playing Particle Effects:**
```java
// Play particles at location with shape
circ.play(pc, location, LocalAxes.xz(), null);

// Play particles following entity
cone.play(pc, player, player.getLocation(), LocalAxes.fromDirection(player.getEyeLocation().getDirection()));

// Simple particle play at location
pc.play(player, location);
```

**LocalAxes for Shape Orientation:**
- `LocalAxes.xz()` - Horizontal plane (most common)
- `LocalAxes.fromDirection(vector)` - Oriented from direction vector
- Used to orient shapes like circles and cones in 3D space

#### Common Area Effect Patterns

**Delayed Area Effect:**
```java
Bukkit.getScheduler().runTaskLater(NeoRogue.inst(), () -> {
    Location center = player.getLocation();
    circ.play(pc, center, LocalAxes.xz(), null);
    for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, center, tp)) {
        FightInstance.applyStatus(ent, StatusType.POISON, data, stacks, duration);
    }
}, 40L); // 2 second delay
```

**Line/Projectile Effects:**
```java
Vector direction = player.getEyeLocation().getDirection();
Location end = player.getLocation().add(direction.multiply(tp.range));
ParticleUtil.drawLine(player, pc, player.getLocation(), end, 0.5); // 0.5 particle spacing

for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, player.getLocation(), end, tp)) {
    // Apply effects along line
}
```

**Ground-Targeted Abilities:**
```java
Block targetBlock = player.getTargetBlockExact((int) range);
if (targetBlock != null) {
    Location center = targetBlock.getLocation().add(0, 1, 0);
    circ.play(pc, center, LocalAxes.xz(), null);
    for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, center, tp)) {
        // Apply effects at target location
    }
}
```

### Shield/Status Management
- **Shields**: `data.addSimpleShield(uuid, amount, decayDelay)` or `data.addPermanentShield(uuid, amount)`
- **Status Effects**: `FightInstance.applyStatus(target, StatusType.POISON, applier, stacks, duration)`
- **Buffs**: `data.addDamageBuff(DamageBuffType.of(category), buff, duration)`

### Equipment Properties System

Equipment behavior is controlled through `EquipmentProperties` which define costs, cooldowns, ranges, and other mechanical attributes.

#### Property Types
```java
// All available PropertyType values
PropertyType.MANA_COST        // Mana consumption per use
PropertyType.STAMINA_COST     // Stamina consumption per use  
PropertyType.COOLDOWN         // Seconds between uses
PropertyType.RANGE            // Maximum targeting/effect range
PropertyType.AREA_OF_EFFECT   // Radius for area effects
PropertyType.DAMAGE           // Base weapon damage
PropertyType.KNOCKBACK        // Knockback strength
PropertyType.ATTACK_SPEED     // Attacks per second
```

#### EquipmentProperties Factory Methods

**For Abilities (most common):**
```java
// Basic usable equipment: mana, stamina, cooldown, range
EquipmentProperties.ofUsable(20, 0, 15, 0)

// Usable with area of effect: mana, stamina, cooldown, range, aoe
EquipmentProperties.ofUsable(5, 0, 12, 0, 3)
```

**For Weapons:**
```java
// Basic weapon: damage, attack_speed, damage_type, sound
EquipmentProperties.ofWeapon(50, 1, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_SWEEP)

// Weapon with costs: mana, stamina, damage, attack_speed, damage_type, sound
EquipmentProperties.ofWeapon(10, 5, 100, 0.8, DamageType.FIRE, Sound.ITEM_FLINTANDSTEEL_USE)

// Ranged weapon: damage, attack_speed, knockback, range, damage_type, sound
EquipmentProperties.ofRangedWeapon(75, 1.2, 50, 12, DamageType.LIGHTNING, Sound.ENTITY_ARROW_SHOOT)

// Bow: damage, attack_speed, knockback, range, mana_cost, stamina_cost
EquipmentProperties.ofBow(60, 1, 0, 15, 0, 2)
```

**For Ammunition:**
```java
// Ammunition: damage, knockback, damage_type
EquipmentProperties.ofAmmunition(80, 40, DamageType.FIRE)
```

#### Adding and Modifying Properties
```java
// Chain additional properties with .add()
EquipmentProperties.ofWeapon(damage, speed, type, sound)
    .add(PropertyType.RANGE, 8)
    .add(PropertyType.AREA_OF_EFFECT, 3)

// Mark properties as upgradable (shows yellow values in tooltips)
properties.addUpgrades(PropertyType.COOLDOWN, PropertyType.AREA_OF_EFFECT);
```

#### Accessing Properties in Equipment
```java
// Get property values
double range = properties.get(PropertyType.RANGE);
int cooldown = (int) properties.get(PropertyType.COOLDOWN);

// Check if property exists
if (properties.has(PropertyType.MANA_COST)) {
    // Property exists and has value > 0
}

// Using property values for TargetProperties
tp = TargetProperties.radius(properties.get(PropertyType.AREA_OF_EFFECT), false);
```

#### Cost Management Integration
Equipment properties automatically integrate with the cost/cooldown system:
- **Mana/Stamina costs** are checked in `canTrigger()` and deducted in `trigger()`
- **Cooldowns** are automatically managed by `EquipmentInstance`
- **Ranges** should be used consistently with `TargetProperties` and item descriptions

#### Common Patterns
```java
// Area effect ability with upgradable properties
super(ID, "Name", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
    EquipmentProperties.ofUsable(manaCost, 0, cooldown, 0, areaRadius));
properties.addUpgrades(PropertyType.COOLDOWN, PropertyType.AREA_OF_EFFECT);

// Use property in TargetProperties
tp = TargetProperties.radius(properties.get(PropertyType.AREA_OF_EFFECT), false);

// Reference property in item description
"affects enemies within <white>" + (int)properties.get(PropertyType.AREA_OF_EFFECT) + "</white> blocks."
```

#### Equipment Properties Best Practices
- Use `EquipmentProperties.none()` for passive equipment with no costs
- Always use `.add()` to extend base properties rather than creating custom constructors
- Mark upgradable properties with `addUpgrades()` - these show yellow in tooltips
- Reference property values consistently in descriptions, TargetProperties, and implementation
- Use appropriate factory methods (`ofUsable`, `ofWeapon`, etc.) based on equipment type

### Task Management & Scheduling

**CRITICAL: BukkitRunnables must be managed through FightData task system - NEVER schedule them directly with Bukkit.**

#### Task Management Methods
```java
// Basic task registration (auto-generates UUID)
data.addTask(bukkitTaskReturned);

// Named task registration (can cancel/replace by ID)
data.addTask("myTaskId", bukkitTaskReturned);

// Cleanup task - runs when FightData cleanup occurs
data.addCleanupTask("myTaskId", () -> { /* cleanup code */ });

// Guaranteed task - runs even if FightData is cleaned up early
data.addGuaranteedTask(UUID.randomUUID(), () -> { /* task code */ }, delayTicks);
```

#### Proper Task Patterns

**Basic Delayed Task:**
```java
data.addTask(new BukkitRunnable() {
    public void run() {
        // Task code here
    }
}.runTaskLater(NeoRogue.inst(), 40L)); // 2 seconds
```

**Repeating Task:**
```java
data.addTask(new BukkitRunnable() {
    public void run() {
        // Repeating task code
    }
}.runTaskTimer(NeoRogue.inst(), 0L, 20L)); // Every 1 second
```

**Task with Cleanup:**
```java
String taskId = "myEquipmentTask";
data.addTask(taskId, new BukkitRunnable() {
    public void run() {
        // Task code
    }
}.runTaskTimer(NeoRogue.inst(), 0L, 20L));

// Ensure cleanup when equipment is removed
data.addCleanupTask(taskId + "-cleanup", () -> {
    data.removeAndCancelTask(taskId);
});
```

**Equipment Hitbox Checking Pattern:**
```java
// Common pattern for projectile/ability hit checking
private ArrayList<BukkitTask> tasks = new ArrayList<BukkitTask>();

for (long delay = 1; delay <= 10; delay++) {
    tasks.add(new BukkitRunnable() {
        public void run() {
            // Hit checking logic
        }
    }.runTaskLater(NeoRogue.inst(), delay));
}

// Register cleanup to cancel all hit checking tasks
data.addCleanupTask(id, () -> { cancelTasks(); });

private void cancelTasks() {
    for (BukkitTask task : tasks) {
        task.cancel();
    }
}
```

#### Buff Management with Duration
**Temporary Damage/Defense Buffs:**
```java
// Damage buff with timed removal
data.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), 
                   new Buff(data, increaseAmount, 0, StatTracker.damageBuffAlly(id, this)), 
                   durationTicks);

// Defense buff with timed removal  
data.addDefenseBuff(DamageBuffType.of(DamageCategory.PHYSICAL),
                    new Buff(data, defenseIncrease, 0, StatTracker.defenseBuffAlly(id, this)),
                    durationTicks);
```

**Permanent Buffs (no auto-removal):**
```java
data.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), 
                   new Buff(data, amount, 0, StatTracker.damageBuffAlly(id, this)));
```

#### Task Chain Pattern (Alternative to manual scheduling)
```java
// Use TaskChain for sequential operations
new TaskChain(data, 0L) // Start immediately
    .then(() -> { /* first action */ }, 20L)  // After 1 second
    .then(() -> { /* second action */ }, 40L); // After 2 more seconds
```

#### Anti-Patterns (DO NOT DO)
```java
// NEVER - bypasses FightData task management
Bukkit.getScheduler().runTaskLater(NeoRogue.inst(), () -> {
    // This won't be cleaned up properly!
}, 40L);

// NEVER - won't be cleaned up on fight end
new BukkitRunnable() {
    public void run() {
        // Orphaned task!
    }
}.runTaskTimer(NeoRogue.inst(), 0L, 20L);
```

#### Best Practices
- Always use `data.addTask()` for task registration
- Use `addCleanupTask()` for equipment that need tasks run when a fight ends
- Use `addGuaranteedTask()` for tasks that should run even if a fight ends
- Named tasks allow replacement/cancellation by ID
- Group related tasks with consistent ID prefixes
- Use `TaskChain` for simple sequential operations

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

### Status/Effect Integration  
- Use `GlossaryTag` for consistent descriptions
- Status application through `FightInstance.applyStatus()`
- Shields managed via `FightData` methods
- Particle/sound effects via `ParticleContainer`/`SoundContainer`

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

**Common Patterns:**
```java
// Status with stacks and duration
"applies " + GlossaryTag.POISON.tag(this, poisonStacks, true) + " [<white>5s</white>]"

// Damage values
GlossaryTag.FIRE.tag(this, damage, true) + " damage"

// Defensive values  
"reduces damage by <yellow>" + reduction + "</yellow>"
"gain " + GlossaryTag.SHIELDS.tag(this, shields, true)

// Thresholds and conditions
"while above <white>50%</white> mana"
"every <white>3rd</white> hit"
```

**Note**: Always call `GlossaryTag.tag(this)` to register the tag with the equipment for glossary tracking.

### Error Patterns
- Missing equipment registration causes lookup failures
- Incorrect trigger types lead to ClassCastException on event objects
- Shield/status timing issues with decay/duration parameters

Focus on the trigger system when implementing equipment - most functionality flows through PlayerFightData trigger attachment in the `initialize()` method.