# Warrior DPS Balance Recommendations

This report considers the complete Warrior equipment catalog, including direct attacks, indirect amplifiers, status converters, offhands, armor, accessories, artifacts, and consumables.

Reference data:

- [Warrior Ability DPS Reference](warrior-ability-dps.md)
- [Warrior Weapon DPS Reference](warrior-weapon-dps.md)

## Progression Targets

| Stage | Target DPS | Normal mob HP multiplier | Relative TTK |
|---|---:|---:|---:|
| Common | 83 | 1.0x | 1.00x |
| Uncommon | 180 | 2.5x | 1.15x |
| Rare | 255 | 4.0x | 1.30x |
| Epic/endgame | 315 | 5.5x | 1.45x |

The ordinary direct-damage curve is broadly reasonable. Late-game excess comes primarily from permanent growth with steep slopes, unrestricted damage repeaters, secondary weapons, and cumulative resource reduction. Those systems should be rate-tuned before applying broad direct-damage buffs.

## Infinite-Scaling Standard

Permanent growth should generally remain uncapped. Tune it around a **60s miniboss** and **120s boss** instead:

- Linear growth is appropriate when its trigger rate is naturally low and predictable.
- Square-root or diminishing-stack scaling is preferred for statuses, stats, health, Shields, and duplicate artifacts that can reach unusually high values.
- Multiplicative reduction of remaining cost is preferred over flat cumulative cost reduction; it can improve forever without reaching zero.
- Per-cast hit deduplication and short internal cooldowns remain valid. They prevent one event from duplicating damage and do not stop fight-long growth.
- A scaling item's 120s output should normally remain below roughly **20-25%** of the 255 Rare or 315 Epic whole-build DPS target unless it is the build's primary payoff.

## P0: Correctness And Exploit Fixes

| Equipment | Current problem | Recommended correction |
|---|---|---|
| [The Great Divide](../src/main/java/me/neoblade298/neorogue/equipment/weapons/TheGreatDivide.java) | Its counter never resets, so every hit from the third onward adds 250 -> 325 AoE damage. | Reset after each third-hit proc. Retain the existing **250 -> 325** proc damage. |
| [Parry](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Parry.java) | A successful parry appears to add both flat basic damage and a separate 120 -> 180 hit. | Apply only one **120 -> 180** damage component, producing 24 -> 36 cooldown DPS. |
| [Burning Cross](../src/main/java/me/neoblade298/neorogue/equipment/artifacts/BurningCross.java) | The tooltip says 15 damage per three Sanctified, but the implementation deals 15 per stack. | Accumulate groups of three and deal **15 per three stacks**. |
| [Noxian Blight](../src/main/java/me/neoblade298/neorogue/equipment/artifacts/NoxianBlight.java) | It originally advertised permanent Strength and Intellect but only played feedback; the first correction capped each at 20. | Implemented uncapped: grant **2 Strength or Intellect on the first qualifying cast and every third qualifying cast thereafter**. At one qualifying cast per 8s, this grants about 6 at 60s and 10 at 120s. |
| [Mortal Engine](../src/main/java/me/neoblade298/neorogue/equipment/abilities/MortalEngine.java) and [Tireless](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Tireless.java) | Flat cumulative reduction can eventually remove the resource model; the first correction used a cap and cost floor. | Implemented uncapped: each qualifying cast reduces the **remaining base cost** multiplicatively by **3 -> 4%** for Mortal Engine or **4 -> 5%** for Tireless. At 7/15 casts, reduction is about 19/37% -> 25/46% and 25/46% -> 30/54%, respectively. |
| [Titan](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Titan.java) | Flat 10 -> 15 reduction can permanently zero common ability costs; the first correction added a reduction cap and cost floor. | Implemented uncapped: use a static **25 -> 35% reduction**. Percentage reduction cannot reduce a positive cost to zero. |
| [Hero's Landing](../src/main/java/me/neoblade298/neorogue/equipment/abilities/HerosLanding.java) | Natural falls can trigger its damage outside the double-jump cooldown. | Put every fall activation behind the same 8s cooldown. Keep 300 -> 400 damage. |
| [Wind Slash](../src/main/java/me/neoblade298/neorogue/equipment/abilities/WindSlash.java) | One target can intersect multiple full-damage projectiles. | Permit one damage registration per target per cast. Preserve multi-target spread. |
| [Judgment](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Judgment.java) | Separate wave hit sets allow one target to take the center and many full-damage waves. | Permit the center plus at most one wave hit per target. Keep 600 -> 900 damage. |
| [Endurance Shield](../src/main/java/me/neoblade298/neorogue/equipment/offhands/EnduranceShield.java) | Every blocked hit can queue 150 -> 250 damage without an internal cooldown. | Allow one queued empowerment, add a 1s block ICD, and use **100 -> 150** damage. |
| [Force Bracer](../src/main/java/me/neoblade298/neorogue/equipment/offhands/ForceBracer.java) and [Bloody Trinket](../src/main/java/me/neoblade298/neorogue/equipment/artifacts/BloodyTrinket.java) | They retain a `Player` reference across trigger executions. | Fetch `data.getPlayer()` inside each trigger. This is a correctness fix rather than a balance change. |

## P1: Major Scaling Outliers

| Equipment | Current behavior | Change | Recommended change and 60s / 120s result |
|---|---|---|---|
| [Compounding Injury](../src/main/java/me/neoblade298/neorogue/equipment/abilities/CompoundingInjury.java) | At 30 -> 20 Concussed, repeats **80 -> 125%** of damage with no secondary-damage restriction. | Nerf | Repeat **35 -> 50%** of eligible primary damage at **30 -> 25 Concussed**. Never repeat secondary damage. |
| [Chosen of the Light](../src/main/java/me/neoblade298/neorogue/equipment/abilities/ChosenOfTheLight.java) | Each Sanctified application heals 1 and grants **15 -> 25% Magical damage for 10s**, freely stacking during that window. | Nerf | Grant **2 -> 3% Magical damage per application event for 10s**, freely stacking. At one event per 2s, its steady bonus is about **10 -> 15%** at both horizons. |
| [Breaking Point](../src/main/java/me/neoblade298/neorogue/equipment/abilities/BreakingPoint.java) | When its 60 Shields are reduced by half, grants **100 -> 150% permanent direct damage**. | Nerf | Reduce the permanent direct-damage bonus to **40 -> 60%**. |
| [Berserk](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Berserk.java) | At 40 -> 30 Berserk, permanently grants **100% Physical damage** and **50% damage reduction**. | Nerf | Grant **50 -> 65% Physical damage** and **30 -> 40% damage reduction** at the existing 40 -> 30 thresholds. |
| [Seismic Shard](../src/main/java/me/neoblade298/neorogue/equipment/accessories/SeismicShard.java) | Casting an ability deals **80 -> 120 Earthen damage** nearby with no internal cooldown. | Nerf | Deal **40 -> 60** damage with a **1.5s internal cooldown**. |
| [Obsidian Idol](../src/main/java/me/neoblade298/neorogue/equipment/accessories/ObsidianIdol.java) | Applying Concussed deals **10 -> 15 Earthen damage per stack applied**. | Scaling nerf | For an event applying $S$ Concussed, deal **$(12 -> 18)\sqrt{S}$** Earthen damage. This remains uncapped while reducing extreme batched applications. |
| [Radiant Chassis](../src/main/java/me/neoblade298/neorogue/equipment/armor/RadiantChassis.java) | Returns **120 -> 180 Light damage** whenever damage is received, with no internal cooldown. | Nerf | Return **60 -> 90** damage with a **2s internal cooldown**. |
| [Leviathan Axe](../src/main/java/me/neoblade298/neorogue/equipment/offhands/LeviathanAxe.java) | After 45 -> 35 negative-status stacks, right click performs a second basic using **150 -> 200 damage at 0.7 attacks/s**. | Nerf | Use **90 -> 120 damage at 0.6 attacks/s**, producing 54 -> 72 DPS. |
| [Bloodrazor](../src/main/java/me/neoblade298/neorogue/equipment/offhands/Bloodrazor.java) | While above 60% stamina after activation, automatically attacks the nearest enemy for **80 -> 120 damage at 0.5 attacks/s**. | Nerf | Use **60 -> 90 damage at 0.5 attacks/s**, producing 30 -> 45 DPS. |
| [Dawnbringer](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Dawnbringer.java) | Deals **200 -> 300 Light damage** in a piercing wave; every qualifying enemy hit permanently grants **20 -> 30 Light damage**. | Scaling nerf | Grant **3 -> 4 Light once per cast** if at least one target qualifies. On a 5s cooldown, this reaches about **36 -> 48 at 60s** and **72 -> 96 at 120s**. |
| [Radiance](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Radiance.java) | Every 8 -> 6 Sanctified permanently adds **10 Light damage** to basic attacks after activation. | Scaling nerf | Every **20 -> 15 Sanctified** permanently adds **4 -> 5** basic damage. At one Sanctified/s after activation, this adds about **12 -> 20 by 60s** and **24 -> 40 by 120s**. |
| [Rampage](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Rampage.java) | Empowers the next basic for **175 -> 250 Slashing damage**; each landed use permanently adds **40 -> 60 damage**. | Scaling nerf | Keep the initial hit, but permanently add **5 -> 8 per landed use**. On a 6s cooldown, the final proc is about **225 -> 330 at 60s** and **275 -> 410 at 120s**. |
| [Crusade](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Crusade.java) | After activation, every Sanctified stack creates a **60 -> 90 damage** Light projectile with no creation-rate limit. | Nerf | Deal **15 -> 20 per Sanctified stack**, with at most five projectiles created per second. |
| [Cratering Blows](../src/main/java/me/neoblade298/neorogue/equipment/abilities/CrateringBlows.java) | Every Concussed application creates a **60 -> 90 damage** Earthen orbital with no per-target internal cooldown. | Nerf | Deal **45 -> 65 per application event** with a **0.5s per-target ICD**. |
| [Goliath Gauntlet](../src/main/java/me/neoblade298/neorogue/equipment/offhands/GoliathGauntlet.java) | Right click deals 200 plus **2 -> 3 damage** and grants **2 Shields** per Concussed applied during the fight. | Scaling nerf | Use **0.5 -> 0.75 damage** and **0.5 Shields** per Concussed. At one stack/s, this produces **230 -> 245 damage and 30 Shields at 60s**, or **260 -> 290 and 60 Shields at 120s**. |

## Direct Ability Normalization

Most direct attacks can retain their current live values after the surrounding amplifiers are constrained. Cleave, Empowered Edge, Spark of Light, Stone Uppercut, Tackle, Weapon Enchantment: Holy, Blessed Edge, Break the Line, Brightshell, Bulldoze, Embolden, Execute, Fissure, Fury, Pin, Quake, Requiem, Smite, Windcutter, Consecrate, Fortify, Lightfall, Light Pulse, Mighty Swing, Reckless Swing, Siphoning Strike, Spellsword, Rising Sun, Valiant Pierce, Condemn, and Titan's Verdict require no immediate direct-damage adjustment.

| Ability | Recommended adjustment |
|---|---|
| [Atone](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Atone.java) | Use **$200 + (30 -> 40)\sqrt{S}$**, where $S$ is target Sanctified. At 60/120 stacks this deals about **432 -> 510 / 529 -> 638** per target. |
| [Bright Javelin](../src/main/java/me/neoblade298/neorogue/equipment/abilities/BrightJavelin.java) | Use **150 -> 210 base + $(35 -> 45)\sqrt{S}$**. At 60/120 Sanctified this deals about **421 -> 559 / 533 -> 703**. |
| [Earthen Tackle](../src/main/java/me/neoblade298/neorogue/equipment/abilities/EarthenTackle.java) | Keep 160 -> 240 damage, but make the successful-hit cooldown **6s**, producing 26.7 -> 40 DPS. |

## Weapon Normalization

Recommended sustained single-target weapon bands:

| Rarity | Target DPS band |
|---|---:|
| Common | 30-35 |
| Uncommon | 42-52 |
| Rare | 55-75 |
| Epic | 80-95 |

Weapons not listed below can retain their current damage and attack speed.

| Rarity | Recommended adjustments |
|---|---|
| Common | Wooden Sword **30 -> 35**; Martial Staff **30 -> 35**; Wooden Axe **60 -> 70**, with **80 -> 90** Berserk damage; Wooden Greataxe **70 -> 90 at 0.5 attacks/s**. |
| Uncommon | Forceful Leather Gauntlets **30 -> 35 at 1.5 attacks/s**; Tree Trunk **110 -> 130** line damage. |
| Rare | Avalonian Mace Strength multiplier **3x -> 4x**, uncapped; Groundbreaker **140 -> 160**; Righteous Hammer **105 -> 125**. |
| Epic | Bloodthirster bonus becomes **1.0x Berserk + 0.75x Strength -> 1.5x Berserk + 1.0x Strength**; Excalibur keeps 100 base and gains **0.25 -> 0.4 per Sanctified**; Hibernian Quickblade **55 -> 60 at 1.6 attacks/s**; Holy Spear **250 base plus 500 -> 650 threshold damage on a 10s cooldown**; Shieldbearer Staff gains **0.5x -> 0.75x current Shields**; Soul Harvester keeps 100 base and gains **5 -> 8 Strength per kill**. All scaling remains uncapped. |

## Other Damage Contributors

| System | Recommended adjustment |
|---|---|
| Strength | Burst becomes **18 -> 24 Strength and 45 -> 65 stamina**. Dark Pact grants **2 -> 3 Strength every five basics**; at 1 attack/s this is 24 -> 36 at 60s and 48 -> 72 at 120s. Limit Break adds **50 -> 75% of current Strength** once per fight. Keep Battle Cry. |
| Attack speed | Frenzy's Berserk group $i$ grants **$(7 -> 10\%)/\sqrt{i}$ attack speed**, with unlimited groups. This totals about 19.5 -> 27.8% at 20 Berserk and 30.4 -> 43.4% at 40. Ring of Ferocity becomes **15 -> 20%**. Keep Flurry and Tempest Sigil. |
| Accessories | Ring of Fortitude adds **25 -> 35** damage; Fury Infuser grants **20 -> 25%**. Lionheart Bangle grants **$\lfloor\sqrt{\text{maximum health}}\rfloor$ Strength**. Keep Chain Necklace and Major Strength Relic. |
| Status equipment | Keep Earthen Ring, Earthen Bracer, Righteous Ring, Ring of Light, Vermillion Belt, and Sigil of Destruction individually. Price their converted damage into the converter. Ring of the Devastator should use a **30 -> 20** threshold. |
| Offhands | Keep Chasing Dagger, Southpaw, and Ruby Armament. Pure Ember becomes **5 -> 7 attacks at 40 -> 50 damage**. Guiding Light becomes **60 -> 80 damage across seven attacks**. Vengeful Shield becomes **125 -> 175** reactive damage. Iron Maiden's queued hit deals **$(2 -> 3)\sqrt{\text{current Thorns}}$** while retaining its 1s ICD. |
| Artifacts | Earthen Tome and Holy Scriptures copy $i$ each grant **$20\%/\sqrt{i}$** damage, with unlimited copies. Total bonuses at 1/2/4/8 copies are approximately 20/34/56/87%. Keep Bloody Trinket, Avalonian Anchor, Giant Slayer, and Pumped after correctness fixes. |
| Consumables | Force Potion becomes **30 -> 45% direct damage**. Keep Minor Physical Potion. Do not count one-use AoE consumables as sustained build DPS. |

## Change Classification And Old Descriptions

This ledger classifies each proposed adjustment and records its pre-change item description for reference. `Mixed` means the recommendation improves one part of the item while reducing another. `Scaling nerf` lowers an uncapped growth rate without removing infinite progression. `Safety fix` is reserved for non-balance correctness guards. P0 descriptions describe behavior before the implemented correction.

### P0 Ledger

| Equipment | Change | Old description |
|---|---|---|
| The Great Divide | Bug fix | Every 3rd hit deals 250 -> 325 Earthen damage, applies Concussed, and knocks enemies up in a line. |
| Parry | Bug fix | Gain 15 Shields. Taking damage during this increases the next basic attack's damage by 120 -> 180 once per cast. |
| Burning Cross | Bug fix | For every 3 Sanctified applied, also deal 15 Fire damage. |
| Noxian Blight | Scaling nerf | Skills costing at least 20 stamina grant 2 Strength; skills costing at least 20 mana grant 2 Intellect. |
| Mortal Engine | Rework | Qualifying casts permanently reduce all stamina costs by 1 -> 2. |
| Tireless | Rework | Qualifying casts permanently reduce all stamina costs by 2 -> 3 and grant 8 -> 12 Shields for 5s. |
| Titan | Rework | Abilities costing at least 15 stamina have their cost reduced by a flat 10 -> 15. |
| Hero's Landing | Bug fix | Grants double jump. Falling from a small height deals 300 -> 400 damage nearby, applies 20 -> 30 Concussed, and grants 10 -> 15 Strength. |
| Wind Slash | Bug fix | Fire 3 -> 5 projectiles in a cone, each dealing 140 -> 180 Slashing damage. No per-target hit limit was described. |
| Judgment | Bug fix | Slam for 600 -> 900 Light damage and launch eight 600 -> 900 Earthen shockwaves. No shared per-target wave limit was described. |
| Endurance Shield | Mixed | Blocking reduces damage and each blocked hit queues 150 -> 250 Blunt damage for a future basic. No queue limit or ICD was described. |
| Force Bracer | Safety fix | Reduce the first two Direct-damage instances by 15. On breaking, gain a damage buff, Strength, and Berserk. No tooltip text concerned retained player references. |
| Bloody Trinket | Safety fix | Being below 50% health grants 20 Strength. No tooltip text concerned retained player references. |

### P1 Ledger

| Equipment | Change | Old description |
|---|---|---|
| Compounding Injury | Nerf | After activation, damage against enemies at 30 -> 20 Concussed repeats at 80 -> 125%. No secondary-damage restriction was described. |
| Chosen of the Light | Nerf | Each Sanctified application heals 1 and grants 15 -> 25% Magical damage for 10s. |
| Breaking Point | Nerf | When its 60 Shields are reduced by half, gain 100 -> 150% damage for the fight. |
| Berserk | Nerf | At 40 -> 30 Berserk, permanently take 50% less damage and deal 100% more Physical damage. |
| Seismic Shard | Nerf | Casting an ability deals 80 -> 120 Earthen damage to nearby enemies. No internal cooldown was described. |
| Obsidian Idol | Scaling nerf | Applying Concussed deals 10 -> 15 Earthen damage per stack applied. |
| Radiant Chassis | Nerf | Reduce incoming damage and return 120 -> 180 Light damage whenever damage is received. No ICD was described. |
| Leviathan Axe | Nerf | After applying enough negative statuses, right click performs a second basic attack using a 150 -> 200 damage weapon at 0.7 attacks/s. |
| Bloodrazor | Nerf | After activation, automatically attacks the nearest enemy while above 60% stamina using 80 -> 120 damage at 0.5 attacks/s. |
| Dawnbringer | Scaling nerf | Deal 200 -> 300 Light in a piercing wave. Each qualifying hit permanently increases Light damage. |
| Radiance | Scaling nerf | After activation, every 8 -> 6 Sanctified permanently adds 10 Light damage to basics. |
| Rampage | Scaling nerf | Empower the next basic for 175 -> 250 Slashing; each use permanently adds 40 -> 60 damage. |
| Crusade | Nerf | After activation, every Sanctified stack creates a 60 -> 90 damage Light projectile, with no creation-rate limit. |
| Cratering Blows | Nerf | Every Concussed application creates a 60 -> 90 damage Earthen orbital, with no per-target ICD. |
| Goliath Gauntlet | Scaling nerf | Right click deals 200 plus scaling damage from all Concussed applied and grants scaling Shields. |

### Direct Ability Ledger

| Equipment | Change | Old description |
|---|---|---|
| Atone | Scaling nerf | Charge, then deal 200 Light nearby plus 7 -> 10 damage per Sanctified on each target. |
| Bright Javelin | Mixed | Charge and throw a javelin dealing 150 plus 10 damage per Sanctified. The upgraded values did not change. |
| Earthen Tackle | Nerf | Dash into an enemy for 160 -> 240 Earthen AoE and 10 -> 15 Concussed. A hit reduced the cooldown by 10s. |

### Weapon Ledger

| Equipment | Change | Old description |
|---|---|---|
| Wooden Sword | Buff | No unique effect text; shared stats supplied 25 -> 35 Slashing damage at 1 attack/s. |
| Martial Staff | Buff | Every 5th hit applies 3 -> 5 Concussed. Shared stats supplied 30 Blunt damage at 1 attack/s. |
| Wooden Axe | Buff | Deal 20 -> 30 additional damage while Berserk. Shared stats supplied 60 Slashing damage at 0.5 attacks/s. |
| Wooden Greataxe | Mixed | Requires an empty offhand and hits the nearest two enemies. Shared stats supplied 80 damage at 0.5 -> 0.7 attacks/s. |
| Forceful Leather Gauntlets | Nerf | No unique effect text; shared stats supplied 35 -> 40 Blunt damage at 1.5 attacks/s. |
| Tree Trunk | Buff | Requires an empty offhand. Left click damages in a line; its slower right click damages in a circle while Berserk. |
| Avalonian Mace | Scaling nerf | Damage additionally scales with 4x -> 7x current Strength. |
| Groundbreaker | Nerf | Striking a block deals 140 -> 175 Blunt in an area after 1s and applies 2 -> 4 Concussed. |
| Righteous Hammer | Buff | Deal 95 Blunt in a small frontal area and apply 2 -> 4 Sanctified to every target. |
| Bloodthirster | Scaling nerf | Damage gains stat-derived bonus damage from Strength and Berserk. |
| Excalibur | Scaling nerf | Deal 100 Slashing and permanently gain damage from every Sanctified applied. |
| Hibernian Quickblade | Nerf | Deal 60 Slashing at 1.6 attacks/s; every 3rd hit grants Speed, Shields, and knockback. |
| Holy Spear | Buff | Charge and deal Piercing damage in a line; targets above the Sanctified threshold take bonus damage. |
| Shieldbearer Staff | Scaling nerf | Deal 100 Piercing plus 1x -> 1.5x current Shields per hit. |
| Soul Harvester | Scaling nerf | Deal 100 Slashing at 1 attack/s and gain 10 -> 20 permanent Strength on basic-attack kills. |

### Other Contributor Ledger

| Equipment | Change | Old description |
|---|---|---|
| Burst | Nerf | Gain 50 -> 80 stamina and 20 -> 30 Strength for 15s. |
| Dark Pact | Scaling nerf | Every 3 basics grants 2 Strength; take 50% more damage for the first 40 -> 25s. |
| Limit Break | Nerf | Double current Strength once per fight after its delay. |
| Frenzy | Nerf | Every 5 Berserk up to 20 grants 7 -> 10% attack speed, for up to 28 -> 40%. |
| Ring of Ferocity | Nerf | Increase attack speed by 15 -> 25%. |
| Ring of Fortitude | Nerf | While shielded, basics deal 30 -> 45 additional Blunt damage. |
| Fury Infuser | Nerf | At 10 -> 7 Berserk, increase Earthen, Blunt, and Light damage by 30%. |
| Lionheart Bangle | Scaling nerf | Gain 1 Strength per 10 maximum health at fight start. |
| Ring of the Devastator | Buff | Every 30 combined Sanctified or Concussed grants 1 Berserk; every 10 Berserk grants 1 mana regeneration. |
| Pure Ember | Nerf | The next 5 -> 7 basics deal 40 -> 60 additional Light damage. |
| Guiding Light | Nerf | The next seven basics deal additional Light damage in a cone; the first also applies Sanctified. |
| Vengeful Shield | Nerf | Once per second, blocking grants Berserk and queues 150 -> 250 Blunt damage for the next basic. |
| Iron Maiden | Scaling nerf | Blocking grants Thorns and queues damage equal to half current Thorns. |
| Earthen Tome | Scaling nerf | Increase Earthen damage by 20% per copy. |
| Holy Scriptures | Scaling nerf | Increase Light damage by 20% per copy. |
| Force Potion | Nerf | Increase Direct damage by 40 -> 60% for 20s. |

## Projected Whole-Build Curve

After the correctness fixes and scaling-rate changes, representative sustained single-target budgets at the 60s miniboss horizon are:

| Stage | Weapon | Abilities | Offhand | Armor, accessories, and passives | Total | Relative TTK |
|---|---:|---:|---:|---:|---:|---:|
| Common | 32 | 38 | 6 | 7 | **83** | **1.00x** |
| Uncommon | 47 | 72 | 22 | 39 | **180** | **1.15x** |
| Rare | 65 | 112 | 35 | 43 | **255** | **1.30x** |
| Epic/endgame | 87 | 130 | 40 | 58 | **315** | **1.45x** |

The relative TTK calculation uses the Common build as its baseline:

$$
\text{relative TTK}_t=\frac{\text{HP multiplier}_t\times83}{\text{DPS}_t}
$$

These targets should be validated with complete **60s miniboss** and **120s boss** benchmark builds. Track both average DPS and end-of-fight DPS so uncapped growth slopes are visible. Track weapon, active ability, proc, and status-conversion damage separately; also test three-target AoE independently from sustained single-target output.
