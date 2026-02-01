# NeoRogue Particle & Visual Effects Guide

This guide covers particle systems, visual effects, and animations in NeoRogue. For general equipment development, see **[Equipment Instructions](equipment-instructions.md)**.

## Particle Container System

### ParticleContainer Basics

`ParticleContainer` defines how particles are spawned. Typically declared as `static final` to share across equipment instances.

```java
private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME)
    .count(25)          // Number of particles per spawn
    .spread(1, 0.5)     // X/Z spread, Y spread (blocks)
    .offsetY(1)         // Y offset from base location
    .speed(0.1);        // Particle speed/motion
```

### Common Particle Types
```java
Particle.FLAME          // Fire particles
Particle.CLOUD          // Smoke/cloud particles
Particle.FIREWORK       // Firework explosion particles
Particle.END_ROD        // Beam/rod particles
Particle.DUST           // Colored dust (requires DustOptions)
Particle.SOUL           // Soul particles
Particle.ENCHANT        // Enchanting table particles
Particle.CRIT           // Critical hit particles
Particle.BLOCK          // Block break particles (requires blockData)
Particle.SPORE_BLOSSOM_AIR  // Spore particles
```

### ParticleContainer Methods
```java
// Basic configuration
.count(int)                 // Number of particles
.spread(double xz, double y) // Spread area
.offsetY(double)            // Y-axis offset
.speed(double)              // Particle velocity

// Special particle data
.dustOptions(DustOptions)   // For colored dust particles
.blockData(BlockData)       // For block break particles

// Cloning
pc.clone()                  // Create independent copy with same settings
pc.clone().particle(Particle.SOUL)  // Clone and change particle type
```

### Colored Dust Particles
```java
import org.bukkit.Color;
import org.bukkit.Particle.DustOptions;

private static final ParticleContainer pc = new ParticleContainer(Particle.DUST)
    .dustOptions(new DustOptions(Color.GREEN, 1F))  // Color, size
    .count(50).spread(2, 0.5);
```

### Block Break Particles
```java
import org.bukkit.Material;

private static final ParticleContainer pc = new ParticleContainer(Particle.BLOCK)
    .blockData(Material.DIRT.createBlockData())
    .count(5).spread(0.1, 0.1);
```

### Playing Particles
```java
// Simple particle play at location
pc.play(player, location);

// Play at entity location
pc.play(player, entity.getLocation());

// Play with offset
Location loc = player.getLocation().add(0, 1, 0);  // 1 block up
pc.play(player, loc);
```

## Particle Shapes

Particle shapes define geometric patterns for visual effects. All shapes are from the NeoCore library.

### Available Shapes

#### Circle
Circular/ring patterns in 3D space.
```java
private static final Circle circ = new Circle(5);  // 5 block radius
```

#### Cone
Cone-shaped patterns for directional effects.
```java
private static final Cone cone = new Cone(5, 60);  // 5 blocks range, 60° arc
```

#### Rectangle  
Rectangular/barrier patterns.
```java
private static final Rectangle rect = new Rectangle(length, height, metersPerParticle);
```

### LocalAxes - Shape Orientation

`LocalAxes` defines how shapes are oriented in 3D space:

```java
// Horizontal plane (most common for ground effects)
LocalAxes.xz()

// Oriented from a direction vector
LocalAxes.fromDirection(player.getEyeLocation().getDirection())

// Using entity eye location (for barriers/shields)
LocalAxes.usingEyeLocation(entity)
```

### Playing Shapes

#### Basic Shape Play
```java
// Play circle on horizontal plane at location
circ.play(pc, location, LocalAxes.xz(), null);

// Play cone in player's facing direction
cone.play(pc, player, player.getLocation(), 
    LocalAxes.fromDirection(player.getEyeLocation().getDirection()));

// Play rectangle
rect.play(player, pc, center, axes, fillParticle);
```

#### Shape with Edge and Fill
Shapes can have different particles for edges vs fill:

**IMPORTANT - Particle Density Guidelines:**
- **Edge particles**: Use `count(1)` and `spread(0, 0)` for clear, crisp outlines
- **Fill particles**: Use `count(1)` with minimal spread (`spread(0.1, 0)`) 
- **Fill density**: Approximately 1 particle per 0.5 square meters of area
  - For a radius 4 circle: area = π × 4² = ~50m², so ~100 fill particles total
  - For a radius 5 circle: area = π × 5² = ~78m², so ~156 fill particles total
  - The shape system automatically distributes fill particles across the area

```java
private static final ParticleContainer edge = new ParticleContainer(Particle.CLOUD)
    .count(1).spread(0, 0);
private static final ParticleContainer fill = new ParticleContainer(Particle.CLOUD)
    .count(1).spread(0.1, 0);

// Play with both edge and fill particles
hitShape.play(edge, location, LocalAxes.xz(), fill);
```

**Common mistake to avoid:**
```java
// ❌ WRONG - High count/spread makes edges blurry and unclear
private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD)
    .count(20).spread(1, 0.5);
circ.play(pc, location, LocalAxes.xz(), null);

// ✓ CORRECT - Separate edge and fill with low spread/count
private static final ParticleContainer edge = new ParticleContainer(Particle.CLOUD)
    .count(1).spread(0, 0);
private static final ParticleContainer fill = new ParticleContainer(Particle.CLOUD)
    .count(1).spread(0.1, 0);
circ.play(edge, location, LocalAxes.xz(), fill);
```

### ParticleShapeMemory

For stationary/repeated shape displays, pre-calculate particle locations:

```java
// Calculate once
ParticleShapeMemory mem = circ.calculate(location, LocalAxes.xz());

// Play multiple times (in tick loop, etc.)
mem.play(player, edgeParticle, fillParticle);

// Get specific locations
LinkedList<Location> edges = mem.getEdges();
LinkedList<Location> fill = mem.getFill();
```

**Example from Atone.java:**
```java
static {
    anim = new ParticleAnimation(circPart, (loc, tick) -> {
        // Calculate circle at descending height
        ParticleShapeMemory mem = circ.calculate(loc.add(0, 2 - (0.5 * tick), 0), LocalAxes.xz());
        LinkedList<Location> partLocs = mem.getEdges();
        // Add fill on final tick
        if (tick == 4) partLocs.addAll(mem.getFill());
        return partLocs;
    }, 5);
}
```

## Particle Animations

`ParticleAnimation` creates multi-tick animated effects using lambda functions.

**IMPORTANT**: Each `ParticleAnimation` can only use **one** `ParticleContainer`. If you need multiple particle types/colors in a single animation, create separate animations for each particle type and play them simultaneously at the same location.

### Basic ParticleAnimation Structure
```java
private static final ParticleAnimation anim;

static {
    anim = new ParticleAnimation(particleContainer, (location, tick) -> {
        // Lambda that returns LinkedList<Location> for particle spawns
        LinkedList<Location> partLocs = new LinkedList<Location>();
        
        // Calculate particle positions based on tick
        // tick ranges from 0 to totalTicks-1
        
        return partLocs;
    }, totalTicks);
}
```

### Animation Examples

#### Descending Circle (from Atone.java)
Circle that descends and fills on final tick:
```java
private static final ParticleAnimation anim;
private static final Circle circ = new Circle(5);

static {
    anim = new ParticleAnimation(circPart, (loc, tick) -> {
        ParticleShapeMemory mem = circ.calculate(
            loc.add(0, 2 - (0.5 * tick), 0),  // Descend 0.5 blocks per tick
            LocalAxes.xz()
        );
        LinkedList<Location> partLocs = mem.getEdges();
        if (tick == 4) partLocs.addAll(mem.getFill());  // Fill on final tick
        return partLocs;
    }, 5);  // 5 ticks total
}
```

#### Hammer Swing (from StoneHammer.java)
Rotating weapon swing animation:
```java
swing = new ParticleAnimation(swingPart, (loc, tick) -> {
    LinkedList<Location> partLocs = new LinkedList<Location>();
    
    // Calculate rotation angle based on tick
    double rotation = 2 + Math.min(4, tick) * 8;
    if (tick >= 5) rotation += (tick - 5) * 14;
    
    // Get perpendicular vector for rotation axis
    Vector cross = loc.getDirection().setY(0).rotateAroundY(Math.toRadians(90)).normalize();
    
    // Create rotated position vector
    Vector v = new Vector(0, DISTANCE, 0).rotateAroundAxis(cross, Math.toRadians(rotation));
    partLocs.add(loc.add(v));
    
    return partLocs;
}, 10);  // 10 ticks
```

#### Multi-Color Animations
When you need different colored particles in one animation, create separate animations:
```java
private static final ParticleContainer red = new ParticleContainer(Particle.DUST)
    .dustOptions(new DustOptions(Color.RED, 1F));
private static final ParticleContainer blue = new ParticleContainer(Particle.DUST)
    .dustOptions(new DustOptions(Color.BLUE, 1F));

private static final ParticleAnimation redAnim, blueAnim;

static {
    // Separate animation for red particles
    redAnim = new ParticleAnimation(red, (loc, tick) -> {
        LinkedList<Location> locs = new LinkedList<>();
        // Calculate red particle positions
        return locs;
    }, 20);
    
    // Separate animation for blue particles
    blueAnim = new ParticleAnimation(blue, (loc, tick) -> {
        LinkedList<Location> locs = new LinkedList<>();
        // Calculate blue particle positions
        return locs;
    }, 20);
}

// Play both at same location
redAnim.play(player, location);
blueAnim.play(player, location);
```

### Playing Animations

#### Play at Location
```java
// One-time play
anim.play(player, location);

// Store instance for cancellation
ParticleAnimationInstance inst = anim.play(player, location);
inst.cancel();  // Stop animation early
```

#### Play Following Entity
```java
// Animation follows entity
anim.play(player, entity);
```

#### Managed Animation (Auto-cleanup)
Use `FightData.runAnimation()` for automatic cleanup when fight ends:
```java
data.runAnimation(id, player, anim, location);
// or
data.runAnimation(id, player, anim, entity);
```

### Animation Math Helpers

**Vector rotation:**
```java
// Rotate around axis
Vector v = new Vector(x, y, z).rotateAroundAxis(axis, Math.toRadians(angle));

// Rotate around Y axis (horizontal rotation)
Vector v = direction.setY(0).rotateAroundY(Math.toRadians(90));

// Normalize to unit vector
Vector normalized = vector.normalize();
```

**Common patterns:**
```java
// Perpendicular horizontal vector
Vector cross = direction.setY(0).rotateAroundY(Math.toRadians(90)).normalize();

// Progressive rotation
double rotation = baseAngle + (tick * anglePerTick);

// Conditional rotation speed
double rotation = baseAngle + Math.min(maxTick, tick) * normalSpeed;
if (tick >= maxTick) rotation += (tick - maxTick) * increasedSpeed;
```

## Drawing Lines (ParticleUtil)

For linear particle effects, use `ParticleUtil.drawLine`:

```java
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;

// Basic line
ParticleUtil.drawLine(player, particleContainer, startLoc, endLoc, spacing);

// With cache (optimization for multiple players)
LinkedList<Player> cache = Effect.calculateCache(location);
ParticleUtil.drawLineWithCache(cache, particleContainer, startLoc, endLoc, spacing);
```

**Common use cases:**
```java
// Projectile trail
Vector direction = player.getEyeLocation().getDirection();
Location end = player.getLocation().add(direction.multiply(range));
ParticleUtil.drawLine(player, pc, player.getLocation(), end, 0.5);

// Lightning bolt
Location start = target.getLocation().add(0, 10, 0);  // Above target
Location end = target.getLocation();
ParticleUtil.drawLine(player, lightning, start, end, 0.3);

// Ground lance/spike
Location base = location;
Location top = location.clone().add(0, 4, 0);
ParticleUtil.drawLine(player, pc, base, top, 1);
```

## Common Visual Effect Patterns

### Area of Effect Indicator
Circle that appears before area damage:
```java
private static final Circle circ = new Circle(5);
private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME);

// Show area
circ.play(pc, center, LocalAxes.xz(), null);

// Then apply effects to entities in radius
for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, center, tp)) {
    // Apply damage/status
}
```

### Delayed Circle Effect
```java
data.addTask(new BukkitRunnable() {
    public void run() {
        Location center = player.getLocation();
        circ.play(pc, center, LocalAxes.xz(), null);
        // Apply effects
    }
}.runTaskLater(NeoRogue.inst(), 40L));  // 2 second delay
```

### Repeated Tick Effect
```java
data.addTask(new BukkitRunnable() {
    int ticks = 0;
    public void run() {
        circ.play(pc, center, LocalAxes.xz(), null);
        ticks++;
        if (ticks >= maxTicks) {
            this.cancel();
        }
    }
}.runTaskTimer(NeoRogue.inst(), 0L, 20L));  // Every second
```

### Ground-Targeted Circle
```java
Block targetBlock = player.getTargetBlockExact((int) range);
if (targetBlock != null) {
    Location center = targetBlock.getLocation().add(0, 1, 0);
    circ.play(pc, center, LocalAxes.xz(), null);
}
```

### Projectile Line Trail
```java
Vector direction = player.getEyeLocation().getDirection();
Location end = player.getLocation().add(direction.multiply(range));

// Draw line
ParticleUtil.drawLine(player, pc, player.getLocation(), end, 0.5);

// Hit entities along line
for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, 
    player.getLocation(), end, targetProperties)) {
    // Apply effects
}
```

### Impact Burst
```java
// Large spread burst at impact point
private static final ParticleContainer burst = new ParticleContainer(Particle.EXPLOSION)
    .count(25).spread(2.5, 0.1);

burst.play(player, impactLocation);
```

## Best Practices

### Performance
- **Declare as static final**: Shapes, ParticleContainers, and animations should be static to avoid recreation
- **Use ParticleShapeMemory**: Pre-calculate shapes for repeated display
- **Limit particle counts**: Balance visual quality with server performance
- **Use appropriate spacing**: Smaller spacing = more particles, higher cost

### Visual Consistency
- **Match particles to damage type**: Fire for fire damage, lightning for electric, etc.
- **Use GlossaryTag colors**: Reference status/damage types with matching particle colors
- **Edge + Fill patterns**: Use sparse edge particles (spread 0, count 1) and denser fill particles
- **Timing with effects**: Sync particle animations with damage/status application

### Code Organization
```java
// Group related particle components together
private static final TargetProperties tp = TargetProperties.radius(5, false);
private static final Circle circ = new Circle(tp.range);
private static final ParticleContainer edge = new ParticleContainer(Particle.CLOUD).count(1);
private static final ParticleContainer fill = edge.clone().spread(0.1, 0);
```

### Common Mistakes to Avoid
- ❌ Creating new ParticleContainers in instance methods (creates garbage)
- ❌ Playing animations without cleanup (memory leaks)
- ❌ Using hardcoded values instead of referencing shape radius/properties
- ❌ Forgetting LocalAxes orientation (particles appear rotated incorrectly)
- ❌ Not using `data.runAnimation()` for fight-based animations (no auto-cleanup)

## Integration with Equipment

Particles typically integrate with equipment through these patterns:

```java
@Override
public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
    data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
        // Play particle effect
        circ.play(pc, p.getLocation(), LocalAxes.xz(), null);
        
        // Or play animation
        data.runAnimation(id, p, anim, p);
        
        // Then apply game effects
        for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
            FightInstance.applyStatus(ent, StatusType.POISON, data, stacks, duration);
        }
        
        return TriggerResult.keep();
    }));
}
```

For more on equipment triggers and mechanics, see **[Equipment Instructions](equipment-instructions.md)**.
