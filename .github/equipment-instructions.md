# NeoRogue Equipment Development Guide

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

**IMPORTANT - PLAYER_TICK Timing:**
- `Trigger.PLAYER_TICK` runs **once every 20 game ticks** (1 second), NOT every tick
- To trigger every 2 seconds, increment count by 1 every PLAYER_TICK. Run the rest of the code when count is 2.
- Example: `if (tickCounter++ >= 2)` for a 2-second interval

### Dynamic Equipment Icons

Equipment icons can be updated dynamically to display stack counts or charges. This is useful for abilities that accumulate stacks or have limited uses.

**IMPORTANT**: For icons to update properly, the EquipmentInstance must be attached to a trigger using `data.addTrigger()`. The instance action is where icon updates occur.

**Basic Pattern with Separate Triggers:**
```java
ActionMeta stacks = new ActionMeta();
ItemStack icon = item.clone();
EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
inst.setAction((pdata, in) -> {
    // Update icon here
    stacks.addCount(1);
    icon.setAmount(stacks.getCount());
    inst.setIcon(icon);
    return TriggerResult.keep();
});

// CRITICAL: Attach the instance to a trigger for it to function
data.addTrigger(id, Trigger.PLAYER_TICK, inst);
```

**Complete Example - Separate Triggers:**
```java
@Override
public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
    ActionMeta stacks = new ActionMeta();
    ItemStack icon = item.clone();
    EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
    
    // Set the action that updates the icon
    inst.setAction((pdata, in) -> {
        if (stacks.getCount() < MAX_STACKS) {
            stacks.addCount(1);
            icon.setAmount(stacks.getCount());
            inst.setIcon(icon);
        }
        return TriggerResult.keep();
    });
    
    // Attach to trigger for icon updates to work
    data.addTrigger(id, Trigger.PLAYER_TICK, inst);
    
    // Separate trigger to consume stacks
    data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
        if (stacks.getCount() > 0) {
            // Use stacks for effect
            stacks.setCount(0);
            icon.setAmount(1); // Reset to default
            inst.setIcon(icon);
        }
        return TriggerResult.keep();
    });
}
```

**Alternative Pattern - Single Trigger with EquipmentInstance constructor:**
```java
@Override
public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
    ActionMeta stacks = new ActionMeta();
    ItemStack icon = item.clone();
    
    // Icon updates happen inside the constructor action
    data.addTrigger(id, Trigger.PLAYER_TICK, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
        if (stacks.getCount() < MAX_STACKS) {
            stacks.addCount(1);
            icon.setAmount(stacks.getCount());
            // Note: Can't call setIcon here as we don't have the instance reference
        }
        return TriggerResult.keep();
    }));
}
```

**Key Points:**
- Clone the item: `ItemStack icon = item.clone()`
- Update amount: `icon.setAmount(count)`
- Set on instance: `inst.setIcon(icon)`
- **MUST attach instance to trigger**: `data.addTrigger(id, triggerType, inst)`
- Reset to 1 (not 0) when clearing stacks to show the base item
- Choose pattern based on whether you need icon updates in same or separate trigger

**Using Different Icons for Active States:**
When you want to change the icon material when active (e.g., different item when charged):
```java
ActionMeta stacks = new ActionMeta();
ItemStack icon = item.clone();
ItemStack activeIcon = icon.withType(Material.DIFFERENT_MATERIAL);  // Change material type

data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
    if (stacks.getCount() < MAX_STACKS) {
        stacks.addCount(1);
        ItemStack currentIcon = activeIcon.clone();
        currentIcon.setAmount(stacks.getCount());
        inst.setIcon(currentIcon);  // Show active icon with stack count
    }
    return TriggerResult.keep();
});

data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
    stacks.setCount(0);
    inst.setIcon(icon);  // Reset to base icon
    return TriggerResult.keep();
});
```

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

For detailed particle and animation patterns, see **[Particle Instructions](particle-instructions.md)**

**Quick Reference:**
```java
// Circle for radius effects
private static final Circle circ = new Circle(5);
private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME)
    .count(25).spread(1, 0.5).offsetY(1);

// Play at location
circ.play(pc, location, LocalAxes.xz(), null);
pc.play(player, location);
```

**Note**: Shapes, particle containers, and target properties are typically `static final`.

#### Common Area Effect Patterns

See **[Particle Instructions](particle-instructions.md)** for detailed visual effect patterns.

**Basic delayed effect:**
```java
data.addTask(new BukkitRunnable() {
    public void run() {
        circ.play(pc, center, LocalAxes.xz(), null);
        for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, center, tp)) {
            FightInstance.applyStatus(ent, StatusType.POISON, data, stacks, duration);
        }
    }
}.runTaskLater(NeoRogue.inst(), 40L));
```

### Shield/Status Management
- **Shields**: `data.addSimpleShield(p.getUniqueId(), amount, decayDelay)` or `data.addPermanentShield(p.getUniqueId(), amount)`
- **Status Effects**: `FightInstance.applyStatus(target, StatusType.POISON, applier, stacks, duration)`
- **Buffs**: `data.addDamageBuff(DamageBuffType.of(category), buff, duration)`

### Custom Status Marking

For tracking/marking enemies without using HashMaps, use custom generic status effects:

```java
// Create unique status name per player
String statusName = p.getName() + "-equipmentid";

// Apply the mark to an enemy
FightData fd = FightInstance.getFightData(target);
Status s = Status.createByGenericType(GenericStatusType.BASIC, statusName, fd, true);
fd.applyStatus(s, data, 1, 160); // 1 stack, 8 seconds (160 ticks)

// Check if enemy is marked
if (fd.hasStatus(statusName)) {
    // Enemy is marked
}

// Consume/remove the mark
Status s = Status.createByGenericType(GenericStatusType.BASIC, statusName, fd, true);
fd.applyStatus(s, data, -1, -1); // Negative stacks removes it
```

**Benefits over HashMap tracking:**
- Automatic cleanup when enemy dies
- Duration-based expiration
- No need for manual cleanup tasks
- Integrates with status system

**Example use cases:**
- Mark targets for bonus damage (CollectionHex, BlightTendril)
- Track which enemies have been affected by an ability
- Conditional effects based on marked status

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

#### Understanding addUpgrades() - Tooltip Coloring System

**`properties.addUpgrades()` should ONLY be used when the property value actually changes between base and upgraded versions:**

```java
// CORRECT - Cooldown changes from 12 to 8 seconds
EquipmentProperties.ofUsable(0, isUpgraded ? 10 : 20, isUpgraded ? 8 : 12, 0)
properties.addUpgrades(PropertyType.COOLDOWN, PropertyType.STAMINA_COST);

// INCORRECT - Mana cost is same for both versions (15)
EquipmentProperties.ofUsable(15, 0, isUpgraded ? 8 : 12, 0)
properties.addUpgrades(PropertyType.MANA_COST); // DON'T do this - mana doesn't change!
```

**How it works:**
- `addUpgrades()` sets the `canUpgrade` boolean on Property objects
- In tooltips, upgradable properties show as `<yellow>value</yellow>`
- Non-upgradable properties show as `<white>value</white>`
- This visual distinction helps players see which values improve when upgrading

**Pattern to follow:**
1. Check if property values differ between `isUpgraded ? upgradeValue : baseValue`
2. Only call `addUpgrades()` for properties that actually change
3. Fixed values across both versions should NOT be marked as upgradable

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

#### Best Practices
- Always use `data.addTask()` for task registration
- Use `addCleanupTask()` for equipment that need tasks run when a fight ends
- Use `addGuaranteedTask()` for tasks that should run even if a fight ends
- Named tasks allow replacement/cancellation by ID
- Group related tasks with consistent ID prefixes

### Channeling & Charging

**CRITICAL: Use PlayerFightData's built-in channel() and charge() methods instead of manual implementation.**

When the user requests "channel" or "charge" mechanics, use these methods which handle slowness, movement restrictions, and ability lockout automatically.

#### Channel vs Charge
- **channel()**: Applies slowness based on duration, prevents ability usage during channeling
- **charge()**: Same as channel but also prevents jumping

#### Basic Patterns

**Channel (slowness + ability lockout):**
```java
data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
    data.channel(20).then(new Runnable() {
        public void run() {
            // Execute effect after 1 second channel
        }
    });
    return TriggerResult.keep();
}));
```

**Charge (slowness + ability lockout + no jumping):**
```java
data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
    data.charge(20).then(new Runnable() {
        public void run() {
            // Execute effect after 1 second charge
        }
    });
    return TriggerResult.keep();
}));
```

**Charge with custom slowness level:**
```java
// charge(duration, slownessLevel)
data.charge(20, 2).then(new Runnable() {
    public void run() {
        // Charge with Slowness 2 instead of default
    }
});
```

**Without callback (just apply channel/charge state):**
```java
data.channel(40); // 2 second channel, no follow-up action
data.charge(20);  // 1 second charge, no follow-up action
```

#### Key Points
- Duration is in ticks (20 ticks = 1 second)
- `.then(Runnable)` is optional - use when you need action after channel/charge completes
- Channel/charge automatically prevents other ability usage during duration
- Charge additionally prevents jumping
- Default slowness level is based on duration, or specify custom level with second parameter

### Cancellable Casts (POST_TRIGGER Pattern)

Some abilities have delays before execution and can fail due to conditions not being met after the delay (e.g., block targeting abilities). These require proper cost refunding.

#### When to Use POST_TRIGGER
Use `CastType.POST_TRIGGER` when:
- Ability has a charge/channel delay before execution
- Cast can fail after the delay due to environmental conditions (block targeting, range checks)
- Need to refund costs (mana/stamina/cooldown) on failure

#### Core Pattern

**1. Set CastType to POST_TRIGGER:**
```java
public MyAbility(boolean isUpgraded) {
    super(ID, "Name", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
        EquipmentType.ABILITY, EquipmentProperties.ofUsable(30, 0, 12, 10));
    properties.setCastType(CastType.POST_TRIGGER);
}
```

**2. Manual CAST_USABLE Triggering with Cost Refunds:**
```java
@Override
public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
    EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
    inst.setAction((pdata, in) -> {
        data.charge(40).then(new Runnable() {
            public void run() {
                // Check if cast conditions are still valid
                Block b = p.getTargetBlockExact((int) properties.get(PropertyType.RANGE));
                CastUsableEvent last = inst.getLastCastEvent();
                
                // FAILED CAST - Refund costs
                if (b == null) {
                    data.addMana(last.getManaCost());
                    data.addStamina(last.getStaminaCost());
                    inst.setCooldown(0);
                    Sounds.error.play(p, p);
                    return;
                }
                
                // SUCCESSFUL CAST - Manually trigger CAST_USABLE
                data.runActions(data, Trigger.CAST_USABLE, 
                    new CastUsableEvent(inst, CastType.POST_TRIGGER, 
                        last.getManaCost(), last.getStaminaCost(), 
                        last.getCooldown(), last.getTags()));
                
                // Execute ability effect
                Location loc = b.getLocation().add(0, 1, 0);
                // ... ability logic here
            }
        });
        return TriggerResult.keep();
    });
    data.addTrigger(id, bind, inst);
}
```

#### Complete Example - Ground-Targeted Delayed Ability (Gravity.java)

```java
public class Gravity extends Equipment {
    private static final String ID = "Gravity";
    private int damage;
    
    public Gravity(boolean isUpgraded) {
        super(ID, "Gravity", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
            EquipmentType.ABILITY, EquipmentProperties.ofUsable(30, 0, 12, 10));
        damage = isUpgraded ? 300 : 200;
        properties.setCastType(CastType.POST_TRIGGER);  // CRITICAL
    }
    
    @Override
    public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
        EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
        inst.setAction((pdata, in) -> {
            // 2 second charge
            data.charge(40).then(new Runnable() {
                public void run() {
                    Block b = p.getTargetBlockExact((int) properties.get(PropertyType.RANGE));
                    CastUsableEvent last = inst.getLastCastEvent();
                    
                    // Check if still targeting valid block
                    if (b == null) {
                        // Refund buffed costs (not base costs!)
                        data.addMana(last.getManaCost());
                        data.addStamina(last.getStaminaCost());
                        inst.setCooldown(0);
                        Sounds.error.play(p, p);
                        return;
                    }
                    
                    // Manually trigger CAST_USABLE for other equipment to react
                    data.runActions(data, Trigger.CAST_USABLE, 
                        new CastUsableEvent(inst, CastType.POST_TRIGGER,
                            last.getManaCost(), last.getStaminaCost(),
                            last.getCooldown(), last.getTags()));
                    
                    // Execute ability
                    Location loc = b.getLocation().add(0, 1, 0);
                    data.addRift(new Rift(data, loc, 160));
                    // ... rest of ability logic
                }
            });
            return TriggerResult.keep();
        });
        data.addTrigger(id, bind, inst);
    }
}
```

#### Complete Example - Multi-Stage Ability (ArcaneBlast.java)

```java
public class ArcaneBlast extends Equipment {
    private static final String ID = "ArcaneBlast";
    
    public ArcaneBlast(boolean isUpgraded) {
        super(ID, "Arcane Blast", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
            EquipmentType.ABILITY, EquipmentProperties.ofUsable(25, 0, 10, 14, 4));
        properties.setCastType(CastType.POST_TRIGGER);
    }
    
    @Override
    public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
        ActionMeta am = new ActionMeta();  // Track multi-stage state
        EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
        
        // Condition: either first cast with valid target, or recast
        inst.setCondition((pl, pdata, in) -> {
            return am.getBool() || p.getTargetBlockExact((int) properties.get(PropertyType.RANGE)) != null;
        });
        
        inst.setAction((pdata, in) -> {
            // First cast - charge and place marker
            if (!am.getBool()) {
                data.charge(20).then(new Runnable() {
                    public void run() {
                        Block b = p.getTargetBlockExact((int) properties.get(PropertyType.RANGE));
                        CastUsableEvent last = inst.getLastCastEvent();
                        
                        // Failed - no valid target
                        if (b == null) {
                            data.addMana(last.getManaCost());
                            data.addStamina(last.getStaminaCost());
                            inst.setCooldown(0);
                            Sounds.error.play(p, p);
                            return;
                        }
                        
                        // Success - trigger CAST_USABLE
                        data.runActions(data, Trigger.CAST_USABLE,
                            new CastUsableEvent(inst, CastType.POST_TRIGGER,
                                last.getManaCost(), last.getStaminaCost(),
                                last.getCooldown(), last.getTags()));
                        
                        Location loc = b.getLocation().add(0, 1, 0);
                        am.setBool(true);  // Enter second stage
                        am.setLocation(loc);
                        // ... start charge accumulation
                    }
                });
            }
            // Recast - detonate
            else {
                // ... execute detonation
                am.setBool(false);  // Reset state
                inst.setIcon(item);
            }
            return TriggerResult.keep();
        });
        data.addTrigger(id, bind, inst);
    }
}
```

#### Key Points for POST_TRIGGER Pattern

**Why use getLastCastEvent():**
- Costs are calculated with buffs applied in the hotkey trigger system
- `inst.getLastCastEvent()` stores the **buffed** mana/stamina/cooldown costs
- Refunding base costs (`properties.get(PropertyType.MANA_COST)`) is incorrect when cost reduction buffs exist
- `CastUsableEvent` contains: `getManaCost()`, `getStaminaCost()`, `getCooldown()`, `getTags()`

**Critical Requirements:**
1. Set `properties.setCastType(CastType.POST_TRIGGER)` in constructor
2. Access costs via `inst.getLastCastEvent()` NOT base property values
3. Manually call `data.runActions(data, Trigger.CAST_USABLE, ...)` on success
4. Refund all three: mana, stamina, AND cooldown on failure
5. Play error sound for user feedback when cast fails

**Common Failure Conditions:**
- Block targeting: `p.getTargetBlockExact()` returns null
- Range checks: Target moved out of range during delay
- Entity targeting: Target died or despawned during charge
- Environmental: Required game state changed (rift disappeared, etc.)

**Cost Refund Pattern:**
```java
CastUsableEvent last = inst.getLastCastEvent();
if (conditionFailed) {
    data.addMana(last.getManaCost());      // Refund buffed mana
    data.addStamina(last.getStaminaCost()); // Refund buffed stamina  
    inst.setCooldown(0);                    // Remove cooldown
    Sounds.error.play(p, p);                // User feedback
    return;
}
```

**When NOT to use POST_TRIGGER:**
- Instant cast abilities (use default `CastType.STANDARD`)
- Abilities that cannot fail after trigger
- Toggle abilities (use their own cast type patterns)

### Recast Abilities (resourceUsageCondition Pattern)

For abilities that can be recast without paying mana/stamina/cooldown costs, use the `resourceUsageCondition` field on `EquipmentInstance`.

#### When to Use resourceUsageCondition
Use this pattern when:
- An ability can be cast a second time under certain conditions
- The recast should be free (no mana/stamina cost, no cooldown)
- Both casts execute in the same action logic (just different branches)

#### Basic Pattern

```java
private class MyAbilityInstance extends EquipmentInstance {
    private boolean isInitialCast = true;
    
    public MyAbilityInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
        super(data, eq, slot, es);
        
        action = (pdata, in) -> {
            Player p = data.getPlayer();
            
            // Recast logic
            if (!isInitialCast) {
                // Execute recast effect (free - no costs)
                // ...
                
                // Reset to initial state
                isInitialCast = true;
                setIcon(item);
                return TriggerResult.keep();
            }
            
            // Initial cast logic
            // ... execute initial effect
            
            // Enable recast under certain conditions
            if (someCondition) {
                isInitialCast = false;
                ItemStack recastIcon = item.clone().withType(Material.DIFFERENT_MATERIAL);
                setIcon(recastIcon);
            }
            
            return TriggerResult.keep();
        };
        
        // CRITICAL: Only consume resources on initial cast
        resourceUsageCondition = (pl, pdata, in) -> {
            return isInitialCast;
        };
    }
}
```

#### Complete Example - TwinShiv.java

```java
public class TwinShiv extends Equipment {
    private static final String ID = "TwinShiv";
    private int damage, bonus;
    
    public TwinShiv(boolean isUpgraded) {
        super(ID, "Twin Shiv", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
            EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, 10, 10));
        damage = isUpgraded ? 100 : 80;
        bonus = isUpgraded ? 80 : 50;
    }
    
    @Override
    public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
        data.addTrigger(id, bind, new TwinShivInstance(data, this, slot, es));
    }
    
    private class TwinShivInstance extends EquipmentInstance {
        public UUID firstHit;
        public boolean isFirstProj = true;

        public TwinShivInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
            super(data, eq, slot, es);
            
            ProjectileGroup proj = new ProjectileGroup(new TwinShivProjectile(data, this, slot, eq));
            
            action = (pdata, in) -> {
                if (isFirstProj) {
                    // Initial cast - fire first projectile
                    firstHit = null;
                    this.setCooldown(1);  // Short cooldown between casts
                    proj.start(pdata);
                    isFirstProj = false;
                }
                else {
                    // Recast - fire second projectile (free)
                    proj.start(pdata);
                    isFirstProj = true;
                }
                return TriggerResult.keep();
            };
            
            // Only pay costs on first projectile
            resourceUsageCondition = (pl, pdata, in) -> {
                return isFirstProj;
            };
        }
    }
}
```

#### Complete Example - SparkTrap.java

```java
private class SparkTrapInstance extends EquipmentInstance {
    private Location trapLocation = null;
    private boolean isInitialCast = true;
    
    public SparkTrapInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
        super(data, eq, slot, es);
        
        action = (pdata, in) -> {
            Player p = data.getPlayer();
            
            // Recast: teleport to bomb and deal line damage
            if (!isInitialCast && trapLocation != null) {
                // Execute free recast
                p.teleport(trapLocation);
                // ... deal damage in line
                
                // Reset state
                isInitialCast = true;
                trapLocation = null;
                setIcon(item);
                return TriggerResult.keep();
            }
            
            // Initial cast: drop trap
            trapLocation = p.getLocation().clone();
            
            // Setup trap that may enable recast
            data.addTrap(new Trap(data, trapLocation, 40) {
                @Override
                public void tick() {
                    // Trap ticking logic
                }
                
                private void explode() {
                    // Deal damage
                    boolean hitElectrified = checkForElectrifiedEnemies();
                    
                    // Enable recast if condition met
                    if (hitElectrified) {
                        isInitialCast = false;
                        setIcon(item.clone().withType(Material.LIGHTNING_ROD));
                    }
                }
            });
            
            return TriggerResult.keep();
        };
        
        // Only consume resources on initial cast
        resourceUsageCondition = (pl, pdata, in) -> {
            return isInitialCast;
        };
    }
}
```

#### Key Points for resourceUsageCondition Pattern

**How it works:**
- `resourceUsageCondition` is a lambda that returns `true` when costs should be consumed
- Checked before mana/stamina deduction and cooldown application
- When it returns `false`, the ability executes for free

**Pattern checklist:**
1. Track cast state with boolean field (e.g., `isInitialCast`, `isFirstProj`)
2. Branch logic in `action` based on cast state
3. Set `resourceUsageCondition` to check the cast state boolean
4. Toggle state when enabling/disabling recast
5. Update icon to show recast is available (optional but recommended)
6. Reset to initial state after recast completes

**Common use cases:**
- Double-cast abilities (TwinShiv - fires two projectiles)
- Conditional recasts (SparkTrap - teleport if hit electrified enemy)
- Charge-up abilities (cast multiple times to build up effect)
- Toggle abilities with free toggle-off

**When to use vs POST_TRIGGER:**
- Use `resourceUsageCondition` for same-ability recasts (free second cast)
- Use `POST_TRIGGER` for delayed casts that can fail (refund costs on failure)
- Can combine both patterns if needed (delayed cast with free recast)

## Equipment Categories

#### Equipment Types
- **Abilities**: Active skills (cast triggers, cooldowns)
- **Accessories**: Passive effects (status triggers) 
- **Artifacts**: Meta-progression items
- **Weapons/Armor**: Stat modifications + effects
- **Consumables**: Single-use items

## Equipment-Specific Conventions

### Naming
- Equipment IDs: PascalCase matching class name
- Trigger actions: Lambda expressions with `(pdata, in) ->`
- Static get methods: `public static Equipment get() { return Equipment.get(ID, false); }`

### Status/Effect Integration  
- Use `GlossaryTag` for consistent descriptions
- Status application through `FightInstance.applyStatus()`
- Shields managed via `FightData` methods with `p.getUniqueId()` as applier
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
- **Potion effects**: Use `DescUtil.potion("Speed", 0, 5)` for "Speed 1 [5s]" formatting (name, amplifier, duration)

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