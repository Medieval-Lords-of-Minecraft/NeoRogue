# Electrified Equipment Audit

Audit date: 2026-08-05

This audit covers equipment that directly applies `StatusType.ELECTRIFIED`. Equipment that only consumes, checks, increases, or otherwise reacts to Electrified is excluded. Amounts are listed as base to upgraded unless noted otherwise.

## Electrified Appliers

| Equipment | Rarity | Category | Amount | Trigger and conditions |
| --- | --- | --- | ---: | --- |
| [Spark Knife](../src/main/java/me/neoblade298/neorogue/equipment/weapons/SparkKnife.java#L42) | Common | Weapon | 2 to 3 | Every fifth basic attack applies Electrified. Basic attacks also deal 5 to 10 additional damage to enemies that are already Electrified. |
| [Weapon Enchantment: Electrified](../src/main/java/me/neoblade298/neorogue/equipment/abilities/WeaponEnchantmentElectrified.java#L51) | Common | Ability | 3 to 5 | Left click fires a projectile, limited to once every 3 seconds. Each projectile hit applies Electrified. |
| [Yellow Ring](../src/main/java/me/neoblade298/neorogue/equipment/accessories/YellowRing.java#L34) | Common | Accessory | 3 to 5 | Dealing non-basic Lightning damage while strictly above 50% maximum mana. |
| [Bolt Wand](../src/main/java/me/neoblade298/neorogue/equipment/weapons/BoltWand.java#L56) | Uncommon | Weapon | 5 in code | Every projectile hit from a valid wand basic attack. See the code/tooltip mismatch below. |
| [Discharge](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Discharge.java#L50) | Uncommon | Ability | 5 to 8 | Once the power is active, a kill primes the next basic attack. That attack applies Electrified and consumes the trigger. |
| [Electric Orb](../src/main/java/me/neoblade298/neorogue/equipment/abilities/ElectricOrb.java#L99) | Uncommon | Ability | 2 to 3 | Intended to apply when the cast projectile passes through an enemy, once per enemy. See the unreachable-condition finding below. |
| [Electrode](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Electrode.java#L83) | Uncommon | Ability | 6 to 9 | The projectile records pierced enemies. When it hits a block, recorded enemies in the resulting line receive Electrified. |
| [Electromagnetic Knife](../src/main/java/me/neoblade298/neorogue/equipment/weapons/ElectromagneticKnife.java#L39) | Uncommon | Weapon | 2 | Every basic attack applies Electrified. The weapon also deals 10 to 15 additional damage when the enemy already has at least 5 Electrified. |
| [Energize](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Energize.java#L59) | Uncommon | Ability | 3 to 5 | Casting primes the next basic attack to mark an enemy for 5 seconds. Dealing any Lightning damage while the mark exists applies Electrified to the marked enemy. |
| [Eye of the Storm](../src/main/java/me/neoblade298/neorogue/equipment/abilities/EyeOfTheStorm.java#L53) | Uncommon | Ability | 3 to 4 per pulse | After casting and charging for 1 second, applies Electrified to nearby enemies on each of three pulses. |
| [Lightning Cloak](../src/main/java/me/neoblade298/neorogue/equipment/armor/LightningCloak.java#L44) | Uncommon | Armor | 8 to 12 | Receiving damage applies Electrified to the damager, without a damage-type restriction. |
| [Lightning Rush](../src/main/java/me/neoblade298/neorogue/equipment/abilities/LightningRush.java#L65) | Uncommon | Ability | 6 to 9 | For 3 seconds after casting, a basic attack applies Electrified once per enemy and extends the active duration by 2 seconds. |
| [Mana Arc](../src/main/java/me/neoblade298/neorogue/equipment/abilities/ManaArc.java#L72) | Uncommon | Ability | 8 to 12 | While active, each non-secondary dealt-damage event launches an arc at that event's target. The arc applies Electrified on hit. |
| [Sparkdrain Knife](../src/main/java/me/neoblade298/neorogue/equipment/weapons/SparkdrainKnife.java#L38) | Uncommon | Weapon | 3 | Every basic attack applies Electrified unless the target already has more than 12 stacks; above the threshold it grants shields instead. |
| [Chaos](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Chaos.java#L50) | Rare | Ability | 4 to 7 | On cast, when the random damage/status selection chooses the Lightning and Electrified branch. |
| [Flash Mark](../src/main/java/me/neoblade298/neorogue/equipment/abilities/FlashMark.java#L98) | Rare | Ability | 8 to 12 | When the projectile reaches a block, applies Electrified to every enemy in the line between the player and block. |
| [Flash Spark](../src/main/java/me/neoblade298/neorogue/equipment/abilities/FlashSpark.java#L125) | Rare | Ability | 5 to 7 | One second after casting, the player must be within the strike radius. Their basic attacks then apply Electrified for 10 seconds. |
| [Lightning Rod](../src/main/java/me/neoblade298/neorogue/equipment/abilities/LightningRod.java#L62) | Rare | Ability | 5 to 7 | Every 10 to 7 dealt Lightning-damage events grants a cast charge. Consuming a charge and completing the channel applies Electrified along a line. |
| [Noxian Falx](../src/main/java/me/neoblade298/neorogue/equipment/weapons/NoxianFalx.java#L38) | Rare | Weapon | 2 to 3 | A basic attack applies Electrified only when the target already has Electrified. |
| [Odin's Decree](../src/main/java/me/neoblade298/neorogue/equipment/abilities/OdinsDecree.java#L54) | Rare | Ability | 6 to 10 | Activates after dealing Lightning damage while at least 50% mana. Once active, periodically strikes and electrifies the nearest enemy. |
| [Static Surge](../src/main/java/me/neoblade298/neorogue/equipment/abilities/StaticSurge.java#L68) | Rare | Ability | 4 to 7 | Activates after three basic attacks against Electrified enemies. Once active, basic attacks apply Electrified after sprinting continuously for at least 1 second. |
| [Tempest](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Tempest.java#L53) | Rare | Ability | 3 to 5 | Activates after five Electrified applications. Once active, Electrified damage applies stacks to nearby enemies other than the original target. |
| [Voltaics](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Voltaics.java#L121) | Rare | Ability | Starts at 2 | After its one-time activation, automatic projectiles apply Electrified on hit. The amount increases by 2 for every 20 to 15 total stacks previously applied. |
| [Stormspike](../src/main/java/me/neoblade298/neorogue/equipment/abilities/Stormspike.java#L96) | Epic | Ability | 6 to 9 | Hitting two enemies with one cast grants 6 seconds of basic-attack empowerment. See the captured-target finding below. |

Total: **24 equipment implementations**.

## Yellow Ring Change

Yellow Ring previously applied Electrified to any dealt-damage event containing Lightning damage while the player was above 50% maximum mana. Its `DEAL_DAMAGE` trigger now also requires `!ev.getMeta().isBasicAttack()`, so Lightning basic attacks no longer activate it. The item description now states "non-basic Lightning damage" to match the implementation.

Expected behavior:

- Lightning basic-attack damage adds no Electrified stacks from Yellow Ring.
- Non-basic Lightning damage adds 3 to 5 Electrified while the player is strictly above 50% maximum mana.
- Damage at exactly 50% maximum mana does not activate Yellow Ring.

## Notable Findings

### Bolt Wand implementation and tooltip disagree

[Bolt Wand's projectile hit](../src/main/java/me/neoblade298/neorogue/equipment/weapons/BoltWand.java#L89) hardcodes 5 Electrified stacks. Its `elec` field and item description report 3, so gameplay currently differs from the displayed value.

## Validation

After the Yellow Ring change, `mvn package -DskipTests` completed successfully and rebuilt `target/NeoRogue.jar`. VS Code reported no errors in `YellowRing.java`.
