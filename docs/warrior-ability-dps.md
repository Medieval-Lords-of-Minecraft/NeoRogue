# Warrior Ability DPS Reference

This report covers every live Warrior `ABILITY` that directly deals raw damage. It excludes abilities that only grant Strength, percentage damage, or statuses, and abilities such as Compounding Injury that repeat another source's damage without owning a concrete raw damage amount.

DPS is raw, pre-defense, per target, and assumes immediate reuse. AoE values are not multiplied by enemy count. Trigger-based abilities use formulas because they do not have fixed cooldowns.

Formula variables:

- $a$: basic attacks per second
- $B$: Berserk stacks before the cast
- $c$: usable ability casts per second
- $e$: qualifying trigger events per second
- $H$: current Shields
- $P$: post-activation Sanctified applied
- $q$: Sanctified stacks applied per second whose projectiles hit the target
- $S$: named status or accumulated stat value

## Common

| Ability | DPS base -> upgraded | Description |
|---|---:|---|
| [Cleave](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Cleave.java) | **14 -> 20** | 70 -> 100 Slashing every 5s to all enemies in a range-5 cone. |
| [Empowered Edge](../src/main/java/me/neoblade298/neorogue/equipment/abilities/EmpoweredEdge.java) | **7.1 -> 14** | Next basic adds 50 -> 70 Slashing; cooldown improves from 7s to 5s. Also grants 3 -> 4 Shields. |
| [Spark of Light](../src/main/java/me/neoblade298/neorogue/equipment/abilities/SparkOfLight.java) | **$6a$ -> $16.7a$** | 30 -> 50 Light in a narrow cone every 5 -> 3 basics. At one basic/s: 6 -> 16.7 DPS. |
| [Stone Uppercut](../src/main/java/me/neoblade298/neorogue/equipment/abilities/StoneUppercut.java) | **11.7 -> 17.5** | 70 -> 105 Earthen every 6s to the nearest frontal enemy; knocks up nearby enemies and applies 5 -> 8 Concussed. |
| [Tackle](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Tackle.java) | **5 -> 6.5 listed; 10 -> 13 on hit; 20 -> 26 shielded** | 100 -> 130 Blunt AoE. Its 20s cooldown becomes 10s on hit or 5s when hitting while shielded. |
| [Weapon Enchantment: Holy](../src/main/java/me/neoblade298/neorogue/equipment/abilities/WeaponEnchantmentHoly.java) | **13.3 -> 20** | Free range-8 projectile every 3s dealing 40 -> 60 Light and applying 3 -> 5 Sanctified. |

## Uncommon

| Ability | DPS base -> upgraded | Description |
|---|---:|---|
| [Atone](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Atone.java) | **$25+0.875S$ -> $25+1.25S$** | Radius-5 Light AoE every 8s dealing $200+7S$ -> $200+10S$, where $S$ is target Sanctified. |
| [Blessed Edge](../src/main/java/me/neoblade298/neorogue/equipment/abilities/BlessedEdge.java) | **16.7 -> 16.7** | Next basic causes a separate 100 Light hit every 6s and applies 5 -> 8 Sanctified. |
| [Break the Line](../src/main/java/me/neoblade298/neorogue/equipment/abilities/BreakTheLine.java) | **14.7 -> 14.7** | 220 Earthen radius-4 slam every 15s with knockback, Slow, and 10 -> 15 Concussed. |
| [Bright Javelin](../src/main/java/me/neoblade298/neorogue/equipment/abilities/BrightJavelin.java) | **$18.75+1.25S$ -> same** | Projectile every 8s dealing $150+10S$ Light. It currently has no upgraded damage branch. |
| [Brightshell](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Brightshell.java) | **10 -> 10** | Grants 10 -> 15 Shields, then after 3s deals 100 Light in radius 4 and applies 8 -> 12 Sanctified. |
| [Bulldoze](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Bulldoze.java) | **$7.7+H/13$ -> $10+H/13$** | During a 3s charge, each enemy touched once takes $100+H$ -> $130+H$ Blunt, where $H$ is current Shields. |
| [Cratering Blows](../src/main/java/me/neoblade298/neorogue/equipment/abilities/CrateringBlows.java) | **$60e$ -> $90e$** | Every Concussed application event creates a piercing orbital dealing 60 -> 90 Earthen once per target. |
| [Crusade](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Crusade.java) | **$60q$ -> $90q$** | After activation, every Sanctified stack applied creates a 60 -> 90 Light projectile. |
| [Earthen Tackle](../src/main/java/me/neoblade298/neorogue/equipment/abilities/EarthenTackle.java) | **13.3 -> 20 listed; 80 -> 120 on hit** | 160 -> 240 Earthen radius-4 dash. A hit reduces its 12s cooldown to 2s and applies 10 -> 15 Concussed. |
| [Embolden](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Embolden.java) | **10 -> 10** | Every 10s, grants 3 -> 5 permanent Shields and makes the next basic cause a separate 100 Slashing hit. |
| [Execute](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Execute.java) | **8 -> 12** | Next airborne basic causes 120 -> 180 Piercing every 15s; a kill grants 10 -> 15 Strength. |
| [Fissure](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Fissure.java) | **12.9 -> 22.7** | 180 -> 250 Earthen in a line; cooldown improves from 14s to 11s and Concussed increases from 15 to 22. |
| [Fury](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Fury.java) | **24 -> 24; 30 at threshold** | Next basic adds 120 Slashing. At 18 -> 12 Berserk, cooldown becomes 4s and cost falls. |
| [Parry](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Parry.java) | **48 -> 72 total** | A successful parry queues both +120 -> 180 flat basic damage and a separate 120 -> 180 Slashing hit every 5s. |
| [Pin](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Pin.java) | **13.3 -> 16.7 on wall hit** | Drags enemies during a dash; hitting a wall deals 160 -> 200 Blunt to every dragged enemy. Otherwise deals no damage. |
| [Quake](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Quake.java) | **16.3 -> 23.8** | Immediate radius-5 AoE every 8s dealing 130 -> 190 Earthen and applying 10 -> 15 Concussed. |
| [Requiem](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Requiem.java) | **Up to 50 -> 75** | After the first kill activates it, subsequent kills create a 150 -> 225 Light explosion with a 3s internal cooldown. |
| [Smite](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Smite.java) | **20 -> 28.6** | 140 -> 200 Slashing every 7s in a range-5 cone, applying 7 -> 10 Sanctified. |
| [Windcutter](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Windcutter.java) | **$20a$ -> $30a$** | After activation, every third basic fires five projectiles, but each enemy can take only one 60 -> 90 Slashing hit per volley. |
| [Wind Slash](../src/main/java/me/neoblade298/neorogue/equipment/abilities/WindSlash.java) | **17.5 -> 22.5 normally; 52.5 -> 112.5 theoretical maximum** | Fires 3x140 -> 5x180 Slashing projectiles every 8s. A target can technically intersect multiple projectiles. |

## Rare

| Ability | DPS base -> upgraded | Description |
|---|---:|---|
| [Consecrate](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Consecrate.java) | **15 -> 21.4** | After five qualifying basics, deals 210 -> 300 Light in radius 5 every 14s, applies Sanctified, and grants Shields. |
| [Dawnbringer](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Dawnbringer.java) | **40 -> 60** | Range-10 piercing wave dealing 200 -> 300 Light every 5s. Qualifying hits permanently increase all Light damage. |
| [Fortify](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Fortify.java) | **25 -> 35** | Next basic causes a separate 175 Piercing hit; cooldown improves from 7s to 5s and it builds Fortitude/Shields. |
| [Lightfall](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Lightfall.java) | **29.2 -> 41.7** | Radius-5 landing AoE every 12s dealing 350 -> 500 Light and applying 12 -> 20 Sanctified. |
| [Light Pulse](../src/main/java/me/neoblade298/neorogue/equipment/abilities/LightPulse.java) | **$45a$ -> $66.7a$** | After activation, every third basic above 50% mana fires a volley dealing 135 -> 200 Light once per target. |
| [Mighty Swing](../src/main/java/me/neoblade298/neorogue/equipment/abilities/MightySwing.java) | **25 -> 25 below 50% HP** | Next airborne basic deals 250 Piercing only when the target is at or below 50% HP. Above that, it only refunds cooldown. |
| [Radiance](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Radiance.java) | **$10a\lfloor P/8\rfloor$ -> $10a\lfloor P/6\rfloor$** | After activation, every basic gains permanent scaling Light damage based on subsequent Sanctified application. |
| [Rampage](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Rampage.java) | **29.2 -> 41.7 initially** | Every 6s queues 175 -> 250 Slashing. Each successful proc permanently adds another 40 -> 60 damage to future procs. |
| [Reckless Swing](../src/main/java/me/neoblade298/neorogue/equipment/abilities/RecklessSwing.java) | **$1.625(B+1)$ -> $2.125(B+1)$** | Next basic deals $13(B+1)$ -> $17(B+1)$ Blunt every 8s, where $B$ is pre-cast Berserk. |
| [Siphoning Strike](../src/main/java/me/neoblade298/neorogue/equipment/abilities/SiphoningStrike.java) | **12.5 -> 12.5; 25 on kill** | Next airborne basic causes 150 Piercing every 12s. A kill refunds half the cooldown and grants 12 -> 20 Strength. |
| [Spellsword](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Spellsword.java) | **$175\min(c,a)$ -> $260\min(c,a)$** | After activation, casting a usable ability empowers the next basic with 175 -> 260 Light. Empowerments do not stack. |
| [Rising Sun](../src/main/java/me/neoblade298/neorogue/equipment/weapons/RisingSun.java) | **17.5 -> 25** | Six rotating cones collectively damage each nearby enemy once for 175 -> 250 Piercing every 10s. |
| [Valiant Pierce](../src/main/java/me/neoblade298/neorogue/equipment/weapons/ValiantPierce.java) | **16.3 -> 21.9 normally; 38.1 -> 53.1 first target in multi-hit** | Range-6 line dealing 130 -> 175 Piercing. If multiple enemies are hit, the first takes another 175 -> 250. |

## Epic

| Ability | DPS base -> upgraded | Description |
|---|---:|---|
| [Condemn](../src/main/java/me/neoblade298/neorogue/equipment/weapons/Condemn.java) | **25 -> 25 normally; 62.5 -> 62.5 with wall** | Range-6 line every 8s dealing 200 Piercing, plus 300 if aimed through a wall. Upgrade improves the defense debuff, not damage. |
| [Hero's Landing](../src/main/java/me/neoblade298/neorogue/equipment/abilities/HerosLanding.java) | **37.5 -> 50 from double-jump cadence** | Fall events deal 300 -> 400 Fire in radius 4, apply 20 -> 30 Concussed, grant Strength, and cancel fall damage. Natural falls can bypass the 8s jump cooldown. |
| [Judgment](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Judgment.java) | **20 -> 30 per component; 180 -> 270 theoretical same-target maximum** | Deals 600 -> 900 Light centrally plus eight 600 -> 900 Earthen waves every 30s. Each basic removes 1s cooldown. A nearby target can potentially take all nine components. |
| [Titan's Verdict](../src/main/java/me/neoblade298/neorogue/equipment/abilities/TitansVerdict.java) | **$S/18$ -> $S/13$** | Traveling line deals Blunt equal to all Shields applied that fight. At 300 Shields: 16.7 -> 23.1 DPS. |

## Interpretation Notes

- Fixed DPS is calculated as raw damage divided by effective cooldown.
- Trigger formulas intentionally preserve attack speed, status rate, or accumulated-stat dependence.
- Multi-projectile theoretical maxima are shown separately from expected one-hit-per-target output.
- Resource regeneration, animation time, misses, target availability, defenses, external buffs, and status multipliers are not included.
