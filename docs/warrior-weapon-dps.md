# Warrior Weapon DPS Reference

This report covers all 39 live Warrior `WEAPON` items. It excludes `Condemn`, `Rising Sun`, and `Valiant Pierce` because they are abilities despite being located in the weapons package.

DPS is raw, pre-defense, per target, and excludes external buffs unless a formula says otherwise. Conventional weapon DPS is:

$$
\text{DPS}=\text{damage per hit}\times\text{attacks per second}
$$

The displayed cooldown is the nominal inverse of attacks per second. The live click gate subtracts 50ms for tick tolerance. AoE total DPS is reported separately from per-target DPS.

Formula variables:

- $A$: Sanctified applied by the player during the fight
- $B$: current Berserk
- $C$: target Concussed after the current hit
- $H$: current Shields
- $K$: kills made by the weapon
- $N$: targets hit
- $Q$: target Sanctified before the hit
- $S$: current Strength

## Common

| Weapon | DPS base -> upgraded | Description |
|---|---:|---|
| [Fencing Sword](../src/main/java/me/neoblade298/neorogue/equipment/weapons/FencingSword.java) | **30 -> 35** | 30 -> 35 Piercing at 1 attack/s (1s cooldown), single target. Every third hit grants 2 -> 4 Shields for 3s. |
| [Leather Gauntlets](../src/main/java/me/neoblade298/neorogue/equipment/weapons/LeatherGauntlets.java) | **30 -> 37.5** | 20 -> 25 Blunt at 1.5 attacks/s (0.67s cooldown), single target. |
| [Martial Staff](../src/main/java/me/neoblade298/neorogue/equipment/weapons/MartialStaff.java) | **30 -> 30** | 30 Blunt at 1 attack/s (1s cooldown), single target. Every fifth hit applies 3 -> 5 Concussed. |
| [Wooden Axe](../src/main/java/me/neoblade298/neorogue/equipment/weapons/WoodenAxe.java) | **30 -> 30 normally; 40 -> 45 with Berserk** | 60 Slashing at 0.5 attacks/s (2s cooldown). While Berserk is present, hit damage becomes 80 -> 90. |
| [Wooden Greataxe](../src/main/java/me/neoblade298/neorogue/equipment/weapons/WoodenGreataxe.java) | **40 -> 56 per target; 80 -> 112 at two targets** | 80 Slashing at 0.5 -> 0.7 attacks/s (2s -> 1.43s cooldown), hitting the nearest two enemies in a range-3 cone. Requires an empty offhand. |
| [Wooden Sword](../src/main/java/me/neoblade298/neorogue/equipment/weapons/WoodenSword.java) | **25 -> 35** | 25 -> 35 Slashing at 1 attack/s (1s cooldown), single target. Non-droppable starter weapon. |

## Uncommon

| Weapon | DPS base -> upgraded | Description |
|---|---:|---|
| [Crescent Axe](../src/main/java/me/neoblade298/neorogue/equipment/weapons/CrescentAxe.java) | **$30+1.5B$ -> $40+1.5B$** | 60 -> 80 Blunt plus $3B$ per hit at 0.5 attacks/s (2s cooldown), single target. |
| [Crimson Blade](../src/main/java/me/neoblade298/neorogue/equipment/weapons/CrimsonBlade.java) | **35 -> 40** | 35 -> 40 Slashing at 1 attack/s (1s cooldown). During the first 30s, every fifth hit heals 2 -> 3. |
| [Crippling Fencing Sword](../src/main/java/me/neoblade298/neorogue/equipment/weapons/CripplingFencingSword.java) | **45 -> 45** | 45 Piercing at 1 attack/s. Every third hit grants 5 Shields and applies 3 -> 5 Concussed. |
| [Earthen Leather Gauntlets](../src/main/java/me/neoblade298/neorogue/equipment/weapons/EarthenLeatherGauntlets.java) | **45 -> 45** | 30 Blunt at 1.5 attacks/s (0.67s cooldown). Every third hit applies 5 -> 8 Concussed. Live damage type is Blunt, not Earthen. |
| [Flametongue](../src/main/java/me/neoblade298/neorogue/equipment/weapons/Flametongue.java) | **45 -> 45 normally; 55 -> 65 vs. Sanctified** | 45 Fire at 1 attack/s. Adds 10 -> 20 raw damage when the target has Sanctified. |
| [Forceful Leather Gauntlets](../src/main/java/me/neoblade298/neorogue/equipment/weapons/ForcefulLeatherGauntlets.java) | **52.5 -> 60** | 35 -> 40 Blunt at 1.5 attacks/s, single target. No additional live coded effect. |
| [Harpoon](../src/main/java/me/neoblade298/neorogue/equipment/weapons/Harpoon.java) | **40 -> 50 melee; 20 -> 25 thrown per target** | 40 -> 50 Piercing. Melee/reach attacks at 1/s hit the first target within 4. Throws at 0.5/s hit every target in a range-6 line for total $N(20 -> 25)$ DPS. |
| [Light Leather Gauntlets](../src/main/java/me/neoblade298/neorogue/equipment/weapons/LightLeatherGauntlets.java) | **37.5 -> 37.5** | 25 Blunt at 1.5 attacks/s. Restores 2 -> 3 stamina per hit, nominally 3 -> 4.5 stamina/s. |
| [Massive Halberd](../src/main/java/me/neoblade298/neorogue/equipment/weapons/MassiveHalberd.java) | **45 -> 60** | 90 -> 120 Blunt at 0.5 attacks/s (2s cooldown). Costs 3 stamina per hit. |
| [Rapier](../src/main/java/me/neoblade298/neorogue/equipment/weapons/Rapier.java) | **45 -> 55** | 45 -> 55 Piercing at 1 attack/s. Every third hit grants 6 -> 9 Shields for 4s. |
| [Shield Pike](../src/main/java/me/neoblade298/neorogue/equipment/weapons/ShieldPike.java) | **22.5 -> 26.25 normally; 45 -> 52.5 with shield** | 30 -> 35 Piercing at 0.75 attacks/s (1.33s cooldown), hitting the first target within 4. Grants 7 -> 10 Thorns; an offhand shield doubles damage. |
| [Silver Fang](../src/main/java/me/neoblade298/neorogue/equipment/weapons/SilverFang.java) | **45 -> 55** | 45 -> 55 Slashing at 1 attack/s and applies 2 -> 3 Sanctified. Cannot drop. |
| [Stone Axe](../src/main/java/me/neoblade298/neorogue/equipment/weapons/StoneAxe.java) | **40 -> 50 per target; $N(40 -> 50)$ at 10+ Berserk** | 80 -> 100 Blunt at 0.5 attacks/s. Below 10 Berserk it is single-target; at 10+, it hits an uncapped range-3 cone. |
| [Stone Hammer](../src/main/java/me/neoblade298/neorogue/equipment/weapons/StoneHammer.java) | **35 -> 50 per target; $N(35 -> 50)$ total** | 70 -> 100 Blunt at 0.5 attacks/s, hitting an uncapped radius-2 area centered four blocks ahead after 0.5s. |
| [Stone Mace](../src/main/java/me/neoblade298/neorogue/equipment/weapons/StoneMace.java) | **$0.75(50+5C)$ -> $0.75(65+5C)$** | 50 -> 65 Blunt at 0.75 attacks/s. Applies 2 -> 3 Concussed before adding five damage per resulting Concussed stack. First isolated hit is 45 -> 60 DPS. |
| [Stone Spear](../src/main/java/me/neoblade298/neorogue/equipment/weapons/StoneSpear.java) | **40 -> 40 base; throw $18+0.6S$ -> $24+0.6S$ per target** | 40 Piercing at 1 attack/s. Direct melee adds $3S$ -> $4S$, though the reach path remains $3S$. Its 5s throw deals $90+3S$ -> $120+3S$ through an uncapped line. |
| [Stone Sword](../src/main/java/me/neoblade298/neorogue/equipment/weapons/StoneSword.java) | **45 -> 50 normally; 50 -> 60 while shielded** | 45 -> 50 Slashing at 1 attack/s. Starts with 10 -> 15 permanent Shields and gains 5 -> 10 damage while any Shields remain. |
| [Tree Trunk](../src/main/java/me/neoblade298/neorogue/equipment/weapons/TreeTrunk.java) | **55 -> 70 line; 27.5 -> 35 radial** | Requires an empty offhand. Left click deals 110 -> 140 Blunt at 0.5 attacks/s through an uncapped line. Right click at 0.25/s requires Berserk and hits an uncapped radius 4. |

## Rare

| Weapon | DPS base -> upgraded | Description |
|---|---:|---|
| [Avalonian Mace](../src/main/java/me/neoblade298/neorogue/equipment/weapons/AvalonianMace.java) | **$42.5+2S$ -> $42.5+3.5S$** | 85 Blunt at 0.5 attacks/s. Strength is amplified to 4x -> 7x, producing $85+4S$ -> $85+7S$ per hit. |
| [Fracturer](../src/main/java/me/neoblade298/neorogue/equipment/weapons/Fracturer.java) | **42.5 -> 55 base AoE; 48.3 -> 63.3 if also in line** | 85 -> 110 Blunt at 0.5 attacks/s in an uncapped area. Every third swing adds 35 -> 50 Earthen through a line and applies 2 -> 4 Concussed. |
| [Groundbreaker](../src/main/java/me/neoblade298/neorogue/equipment/weapons/Groundbreaker.java) | **70 -> 87.5 per target; $N(70 -> 87.5)$ total** | Clicking a block produces a delayed uncapped radius-2 hit for 140 -> 175 Blunt at 0.5 attacks/s and applies 2 -> 4 Concussed. |
| [Gungnir](../src/main/java/me/neoblade298/neorogue/equipment/weapons/Gungnir.java) | **49 -> 63** | 70 -> 90 Earthen at 0.7 attacks/s (1.43s cooldown), single target. Every third hit applies 3 -> 5 Concussed. |
| [Iron Sword](../src/main/java/me/neoblade298/neorogue/equipment/weapons/IronSword.java) | **55 -> 70** | 55 -> 70 Slashing at 1 attack/s. Every third valid hit removes 1s from all active equipment cooldowns. |
| [Righteous Flame](../src/main/java/me/neoblade298/neorogue/equipment/weapons/RighteousFlame.java) | **60 -> 75** | 60 -> 75 damage at 1 attack/s, split equally between Light and Fire, and applies 2 -> 4 Sanctified. |
| [Righteous Hammer](../src/main/java/me/neoblade298/neorogue/equipment/weapons/RighteousHammer.java) | **47.5 -> 47.5 per target; $47.5N$ total** | 95 Blunt at 0.5 attacks/s in an uncapped area, applying 2 -> 4 Sanctified to every target. |
| [Righteous Lance](../src/main/java/me/neoblade298/neorogue/equipment/weapons/RighteousLance.java) | **49 -> 49 normally; throw $49+2.1Q$ -> $49+3.5Q$** | 70 Piercing at 0.7 attacks/s. Melee applies 2 -> 3 Sanctified; the single-target throw adds $3Q$ -> $5Q$ Light damage per hit. |

## Epic

| Weapon | DPS base -> upgraded | Description |
|---|---:|---|
| [Bloodthirster](../src/main/java/me/neoblade298/neorogue/equipment/weapons/Bloodthirster.java) | **$75+S+B$ -> $75+1.5(S+B)$** | 150 Piercing at 0.5 attacks/s. Each hit adds twice -> three times the sum of Strength and Berserk. |
| [Excalibur](../src/main/java/me/neoblade298/neorogue/equipment/weapons/Excalibur.java) | **$100+0.5A$ -> $100+0.8A$** | 100 Slashing at 1 attack/s. Permanently gains 0.5 -> 0.8 damage per Sanctified stack applied during the fight. |
| [Hibernian Quickblade](../src/main/java/me/neoblade298/neorogue/equipment/weapons/HibernianQuickblade.java) | **96 -> 96** | 60 Slashing at 1.6 attacks/s (0.625s cooldown). Every third hit grants Speed, 4 -> 6 Shields, and radial knockback. |
| [Holy Spear](../src/main/java/me/neoblade298/neorogue/equipment/weapons/HolySpear.java) | **16.7 -> 16.7 normally; 45.8 -> 58.3 at threshold per target** | A 12s cast, not a conventional basic attack. Deals 200 Piercing through an uncapped line; at 25+ Sanctified, adds 350 -> 500 damage and then applies 8 -> 12 Sanctified. |
| [Shieldbearer Staff](../src/main/java/me/neoblade298/neorogue/equipment/weapons/ShieldbearerStaff.java) | **$50+0.5H$ -> $50+0.75H$** | 100 Piercing at 0.5 attacks/s. Hit damage is $100+H$ -> $100+1.5H$. |
| [Soul Harvester](../src/main/java/me/neoblade298/neorogue/equipment/weapons/SoulHarvester.java) | **$100+10K$ -> $100+20K$ after its kills** | 100 Slashing at 1 attack/s. Each basic-attack kill grants 10 -> 20 permanent Strength. Formula assumes no other Strength source. |
| [The Great Divide](../src/main/java/me/neoblade298/neorogue/equipment/weapons/TheGreatDivide.java) | **50 -> 50 base; 175 -> 212.5 primary overlap after warmup** | 100 Blunt at 0.5 attacks/s. From the third hit onward, every hit also deals 250 -> 325 Earthen through an uncapped line and applies 10 -> 15 Concussed; the live counter never resets. |

## Interpretation Notes

- Conventional DPS uses declared damage and attacks per second, not the 50ms click-tolerance adjustment.
- Multi-target DPS is shown per target and, where useful, as $N$ times that value.
- Conditional formulas preserve Strength, Shields, statuses, kills, and target-count dependence.
- Resource costs, target availability, misses, defenses, external buffs, and status damage are not included.
- Several live descriptions and implementations differ; the table describes implementation behavior and calls out consequential mismatches.
