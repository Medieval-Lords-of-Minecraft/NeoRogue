# CastType Usage Guide

## Overview
CastTypes control when the `CAST_USABLE` trigger fires for abilities. This allows other equipment to react to ability casts (e.g., "On cast, deal bonus damage"). The system automatically handles when to trigger `CAST_USABLE` based on the CastType.

## Setting CastType

Set the CastType in your equipment constructor:

```java
public MyAbility(boolean isUpgraded) {
    super(ID, "My Ability", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
            EquipmentProperties.ofUsable(15, 15, 15, 12));
    properties.setCastType(CastType.POST_TRIGGER);
}
```

## Available CastTypes

### `CastType.STANDARD` (Default)
**Behavior:** Automatically triggers `CAST_USABLE` when the ability is cast.

**When to use:**
- Single-cast instant abilities
- One-time effects that trigger immediately
- Most standard abilities (dash, damage skills, etc.)

**How it works:**
- System automatically calls `runActions(data, Trigger.CAST_USABLE, ...)` when ability is triggered
- No manual `CAST_USABLE` trigger needed
- This is the **default** - you don't need to call `setCastType()` at all

**Example:**
```java
public Backstep(boolean isUpgraded) {
    super(ID, "Backstep", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
            EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 5, 8, 0));
    // No setCastType needed - defaults to STANDARD
}

@Override
public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
    data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
        // Ability effect here
        p.setVelocity(...); // Jump backwards
        // CAST_USABLE automatically triggered by system
        return TriggerResult.keep();
    }));
}
```

### `CastType.TOGGLE`
**Behavior:** Does NOT automatically trigger `CAST_USABLE`. Used for abilities that toggle on/off.

**When to use:**
- Toggle abilities (turn on/off with same keybind)
- Sustained abilities that consume resources over time
- Abilities that maintain an active state

**Why no auto-trigger:**
- Prevents double-triggering when toggling on AND off
- Allows toggle state changes without triggering other equipment

**Example - Storm (toggle on/off):**
```java
public Storm(boolean isUpgraded) {
    super(ID, "Storm", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
            EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 1, 8, 5));
    properties.setCastType(CastType.TOGGLE);
}

@Override
public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
    ActionMeta am = new ActionMeta();
    EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
    inst.setAction((pdata, in) -> {
        am.toggleBool(); // Toggle on/off
        if (am.getBool()) {
            inst.setIcon(activeIcon); // Show active state
        } else {
            inst.setIcon(item); // Show inactive state
        }
        // No CAST_USABLE triggered - just changes toggle state
        return TriggerResult.keep();
    });
    
    // Actual effect happens in PLAYER_TICK when toggled on
    data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
        if (!am.getBool()) return TriggerResult.keep();
        // Do periodic effect while toggled on
        return TriggerResult.keep();
    });
}
```

**Example - Cauterize (toggle changes basic attack behavior):**
```java
properties.setCastType(CastType.TOGGLE);

// Toggle on/off
inst.setAction((pdata, in) -> {
    if (inst.getCount() == 0) {
        inst.setCount(1); // Toggle on
        inst.setIcon(activeIcon);
    } else {
        inst.setCount(0); // Toggle off
        inst.setIcon(item);
    }
    return TriggerResult.keep();
});

// Basic attacks behave differently when toggled
data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
    if (inst.getCount() == 1) {
        // Apply special effect
    }
    return TriggerResult.keep();
});
```

### `CastType.POST_TRIGGER`
**Behavior:** Does NOT automatically trigger `CAST_USABLE`. You **MUST** manually call it after the ability successfully executes.

**When to use:**
- Channeled/charged abilities (cast after delay)
- Abilities that can fail after delay (e.g., require targeted block)
- Abilities with complex preconditions

**Why manual trigger:**
- Ability might fail after initial cast (no block targeted, etc.)
- Effect happens after channel/charge completes
- Only want to trigger `CAST_USABLE` if ability actually succeeds

**CRITICAL:** You must manually call:
```java
data.runActions(data, Trigger.CAST_USABLE, new CastUsableEvent(inst, CastType.POST_TRIGGER));
```

**Example - Ground Lance (can fail if no block targeted):**
```java
public GroundLance(boolean isUpgraded) {
    super(ID, "Ground Lance", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
            EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 10, 10, 14, 3));
    properties.setCastType(CastType.POST_TRIGGER);
}

@Override
public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
    EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
    inst.setAction((pdata, in) -> {
        Block b = p.getTargetBlockExact((int) properties.get(PropertyType.RANGE));
        if (b == null) {
            // FAILED - refund mana, reset cooldown, NO CAST_USABLE
            data.addMana(properties.get(PropertyType.MANA_COST));
            inst.setCooldown(0);
            Sounds.error.play(p, p);
            return TriggerResult.keep();
        }
        
        data.charge(20).then(new Runnable() {
            public void run() {
                Block b = p.getTargetBlockExact((int) properties.get(PropertyType.RANGE));
                if (b == null) {
                    // FAILED AFTER CHARGE - refund mana, reset cooldown
                    data.addMana(properties.get(PropertyType.MANA_COST));
                    inst.setCooldown(0);
                    Sounds.error.play(p, p);
                    return;
                }
                
                // SUCCESS - trigger CAST_USABLE manually
                data.runActions(data, Trigger.CAST_USABLE, 
                    new CastUsableEvent(inst, CastType.POST_TRIGGER));
                
                // Now do the actual effect
                Location loc = b.getLocation();
                for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
                    FightInstance.dealDamage(...);
                }
            }
        });
        return TriggerResult.keep();
    });
}
```

**Example - Create Earth (channeled ability):**
```java
properties.setCastType(CastType.POST_TRIGGER);

inst.setAction((pdata, in) -> {
    // Show initial visual
    circ.play(pc, targetLocation, LocalAxes.xz(), null);
    
    data.charge(20).then(new Runnable() {
        public void run() {
            // SUCCESS - trigger CAST_USABLE after charge completes
            data.runActions(data, Trigger.CAST_USABLE, 
                new CastUsableEvent(inst, CastType.POST_TRIGGER));
            
            // Now apply the effect
            circ.play(pc, loc, LocalAxes.xz(), earth);
            for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
                FightInstance.dealDamage(...);
            }
        }
    });
    return TriggerResult.keep();
});
```

---

## Why Use CastTypes?

CastTypes allow other equipment to trigger when you cast abilities:
- Accessories that boost damage on spell cast
- Effects that reduce cooldowns when casting
- Stacking mechanics based on ability usage

By calling `runActions` with `CAST_USABLE`, you notify all listening equipment that this ability was cast, allowing them to apply their effects.

---

## Decision Tree

**Should I use STANDARD?**
- ✅ Instant, one-time cast abilities
- ✅ No toggle, no delay, no failure conditions
- ✅ **This is the default - just don't call setCastType()**

**Should I use TOGGLE?**
- ✅ Ability turns on/off with same keybind
- ✅ Maintains an active state
- ✅ You don't want other equipment triggering on toggle state changes

**Should I use POST_TRIGGER?**
- ✅ Ability has channel/charge time
- ✅ Ability can fail after initial cast
- ✅ Effect happens after a delay
- ✅ You want to manually control when CAST_USABLE triggers

---

## Common Mistakes

❌ **DON'T** forget to call `runActions` after POST_TRIGGER abilities execute - other equipment won't know you cast the ability

❌ **DON'T** manually trigger `CAST_USABLE` for STANDARD abilities - it's already automatic

❌ **DON'T** use TOGGLE for abilities that just have a delay - use POST_TRIGGER

✅ **DO** call `runActions` with the matching CastType after your ability's main effect (POST_TRIGGER only)

✅ **DO** refund costs and reset cooldown when POST_TRIGGER abilities fail

✅ **DO** use TOGGLE only when you need persistent on/off state
