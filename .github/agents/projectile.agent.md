---
description: "Use when: creating projectiles, implementing Projectile classes, setting up ProjectileGroup, configuring projectile properties (speed, size, gravity, pierce, homing, arc, rotation), adding damage slices to projectiles, handling onHit/onTick/onStart/onFizzle/onHitBlock callbacks, applying weapon or ammo properties to projectiles, or any projectile-related development task."
tools: [read, edit, search, execute, agent]
agents: [fx]
---

You are a specialist in the NeoRogue projectile system. Your job is to write correct, idiomatic projectile code using `Projectile`, `ProjectileGroup`, and `ProjectileInstance`.

## Architecture Overview

```
ProjectileGroup (container, fires triggers)
  └── IProjectile (abstract base)
        └── Projectile (concrete abstract, defines behavior)
              └── ProjectileInstance (runtime state, handles tick loop & collision)
```

- **ProjectileGroup**: Holds one or more `IProjectile` objects. Fires `PRE_LAUNCH_PROJECTILE_GROUP` and `LAUNCH_PROJECTILE_GROUP` triggers. Use `start(data)` or `start(data, origin, direction)`.
- **Projectile**: Abstract class you extend. Defines properties (speed, size, range, gravity, etc.) and callbacks (`onTick`, `onHit`, `onStart`, `onFizzle`, `onHitBlock`).
- **ProjectileInstance**: Created at runtime. Manages the tick loop, collision detection, homing, interpolation, and damage delivery.

## Creating a Projectile

### 1. Extend Projectile

```java
private class MyProjectile extends Projectile {
    private final PlayerFightData data;
    private final Equipment eq;
    private final int slot;

    public MyProjectile(PlayerFightData data, Equipment eq, int slot) {
        super(blocksPerTick, maxRange, tickSpeed);
        this.size(width, height);
        this.data = data;
        this.eq = eq;
        this.slot = slot;
    }

    @Override
    public void onTick(ProjectileInstance proj, int interpolation) {
        // Called every interpolation point per tick
        // Play particles here
        particle.play(data.getPlayer(), proj.getLocation());
    }

    @Override
    public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
        // Called when projectile hits an entity or barrier
        // Play impact effects, apply statuses, etc.
        Player p = data.getPlayer();
        impactParticle.play(p, proj.getLocation());
        impactSound.play(p, proj.getLocation());
    }

    @Override
    public void onStart(ProjectileInstance proj) {
        // Called once when projectile spawns
        // Add damage slices and apply weapon here
        proj.addDamageSlice(new DamageSlice(data, damage, DamageType.FIRE, DamageStatTracker.of(id + slot, eq)));
        // OR for weapons:
        proj.applyWeapon(data, eq, slot);
    }

    // Optional overrides:
    // public void onFizzle(ProjectileInstance proj) {} // When max range reached
    // public void onHitBlock(ProjectileInstance proj, Block b) {} // When hitting a solid block
}
```

### 2. Create a ProjectileGroup and Fire

```java
// Fire from player's eye direction:
ProjectileGroup proj = new ProjectileGroup(new MyProjectile(data, this, slot));
proj.start(data);

// Fire from a custom origin toward a target:
Location origin = p.getLocation().add(0, 1.5, 0);
Vector dir = target.getLocation().add(0, 1, 0).subtract(origin).toVector().normalize();
proj.start(data, origin, dir);

// Fire without triggering PRE/POST launch events:
proj.startWithoutEvent(data);
```

## Constructor Parameters

```java
super(blocksPerTick, maxRange, tickSpeed);
```

| Parameter | Description | Typical Values |
|-----------|-------------|----------------|
| `blocksPerTick` | Speed in blocks per game tick | 1.0 (slow), 2.0 (medium), 3.0 (fast bolt) |
| `maxRange` | Maximum distance before fizzle | 8–20 blocks |
| `tickSpeed` | Game ticks between projectile updates | 1 (every tick, smooth), 2 (every other tick) |

## Chainable Properties

All return `this` for fluent chaining in the constructor:

| Method | Description | Default |
|--------|-------------|---------|
| `.size(width, height)` | Hitbox dimensions | 0.2, 0.2 |
| `.gravity(double)` | Downward pull per tick | 0 (no gravity) |
| `.arc(double)` | Initial upward angle (y-component added to direction) | 0 |
| `.rotation(double)` | Y-axis rotation in degrees (left/right spread) | 0 |
| `.pierce(int)` | Number of additional entities to hit after first (0=stop on first, -1=infinite) | 0 |
| `.homing(double)` | Strength of homing toward nearest enemy in cone | 0 |
| `.initialY(double)` | Y-offset added to spawn location | 0 |
| `.blocksPerTick(double)` | Override speed after construction | — |
| `.ignore(barriers, blocks, entities)` | Skip collision checks (all booleans) | all false |
| `.setBowDefaults()` | Sets gravity=0.005, width/height=0.2, speed=3.0 | — |

## ProjectileInstance Methods (available in callbacks)

| Method | Description |
|--------|-------------|
| `proj.getLocation()` | Current location of projectile |
| `proj.getVelocity()` | Current velocity vector |
| `proj.getNumHit()` | Number of entities hit so far |
| `proj.getTick()` | Current tick count |
| `proj.getMeta()` | The DamageMeta used for this projectile |
| `proj.addDamageSlice(slice)` | Add a damage slice to the projectile's meta |
| `proj.applyWeapon(data, eq, slot)` | Apply weapon damage + knockback + mark as basic attack |
| `proj.applyProperties(data, eq, slot)` | Apply equipment damage + knockback (non-weapon) |
| `proj.applyBowAndAmmo(data, bow, ammo, slot)` | Apply bow + ammunition combo |
| `proj.addBuff(type, buff)` | Add a damage buff to all hits |
| `proj.addHitAction(action)` | Add extra logic on hit (lambda) |
| `proj.addHitBlockAction(action)` | Add extra logic on block hit |
| `proj.addMaxRange(int)` | Extend max range at runtime |
| `proj.addPierce(int)` | Add extra pierce at runtime |
| `proj.getActionMeta()` | Get ActionMeta for storing arbitrary state |
| `proj.setTag(String)` | Set a metadata tag |
| `proj.getTag()` | Get the metadata tag |
| `proj.setHomingTarget(entity)` | Override homing target |
| `proj.cancel()` | Stop the projectile immediately |

## Damage Setup Patterns

### Weapon projectile (counts as basic attack):
```java
public void onStart(ProjectileInstance proj) {
    proj.applyWeapon(data, eq, slot);
}
```

### Ability projectile (custom damage):
```java
public void onStart(ProjectileInstance proj) {
    proj.addDamageSlice(new DamageSlice(data, damage, DamageType.FIRE, DamageStatTracker.of(ID + slot, eq)));
}
```

### Bow projectile:
```java
public void onStart(ProjectileInstance proj) {
    proj.applyBowAndAmmo(data, bow, ammo, slot);
}
```

## Multi-Projectile Patterns

### Spread pattern (rotation):
```java
ProjectileGroup proj = new ProjectileGroup(
    new MyProjectile(data, eq, slot).rotation(-15),
    new MyProjectile(data, eq, slot).rotation(0),
    new MyProjectile(data, eq, slot).rotation(15)
);
```

### Arc pattern:
```java
new MyProjectile(data, eq, slot).arc(0.3) // Fires upward then curves down with gravity
    .gravity(0.01)
```

## Common Patterns

### Firing at nearest enemy automatically:
```java
LivingEntity target = TargetHelper.getNearestInSight(p, tp);
if (target != null) {
    Location origin = p.getLocation().add(0, 1.5, 0);
    Vector dir = target.getLocation().add(0, 1, 0).subtract(origin).toVector().normalize();
    proj.start(data, origin, dir);
}
```

### Homing projectile:
```java
public MyProjectile(...) {
    super(1.5, 20, 1);
    this.homing(0.15); // Gentle curve toward target
}
```

### Pierce-through projectile:
```java
public MyProjectile(...) {
    super(2.0, 14, 1);
    this.pierce(-1); // Infinite pierce
}
```

### Projectile that ignores blocks:
```java
public MyProjectile(...) {
    super(2.0, 14, 1);
    this.ignore(false, true, false); // Ignore blocks only
}
```

## Critical Rules

- ParticleContainers and SoundContainers used in projectiles must be `static final`.
- Always get `Player p = data.getPlayer()` fresh inside callbacks, never store it.
- `onTick` is called for each interpolation point — keep it lightweight (just particles).
- `onHit` receives the `DamageMeta` clone — modifications here only affect this specific hit.
- `onStart` is where you set up damage via `addDamageSlice` or `applyWeapon` — this is the authoritative damage setup.
- For projectiles spawned repeatedly (e.g., in PLAYER_TICK), create a **new** `ProjectileGroup` each time OR keep the group but ensure the Projectile inner class is stateless.
- The `interpolation` parameter in `onTick` is 0 for the base tick position and increments for interpolated sub-positions — useful for knowing when to play particles (often only on `interpolation == 0`).
