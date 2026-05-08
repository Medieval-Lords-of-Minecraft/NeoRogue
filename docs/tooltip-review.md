# Tooltip Formatting Review

## Completed Work

### Phase 1: Plain `"X seconds"` → `DescUtil.white/yellow`
5 ability files converted:
- Brace.java, Brace2.java, Bulldoze.java, HoldTheLine.java, ConfidenceKill.java

### Phase 2: `<white>X</white> seconds` → `DescUtil.white()`
28 files converted (abilities + non-abilities).

### Phase 3+4: Raw `<yellow>`/`<white>` tags → `DescUtil.yellow()`/`DescUtil.white()` (abilities only)
All ability files under `equipment/abilities/` now use `DescUtil` helpers instead of raw tags.
- Zero `<yellow>` tags remain in abilities
- `<white>` in abilities only appears inside `[...]` duration brackets (correct format)

---

## Remaining: Non-Ability Equipment Files

The following non-ability files still use raw `<yellow>` and `<white>` tags instead of `DescUtil` helpers.

### Accessories (~16 files with `<yellow>`)
- CobraCrest.java
- DaedalusHammer.java (chat message, not tooltip — may be intentional)
- FlightRing.java
- GripGloves.java
- LifeThief.java
- MajorStaminaRelic.java
- MinorManaRelic.java
- MinorPoisonRelic.java
- MinorStaminaRelic.java
- MinorShieldingRelic.java
- MinorStrengthRelic.java
- RingOfNature.java
- RingOfSharpness.java
- TopazRing.java

### Weapons (~20 files with `<yellow>`)
- AshenHeadhunter.java
- ButterflyKnife.java
- ButterflyKnife2.java
- ChainLightningWand.java
- CrimsonBlade.java
- DarkBolt2.java
- DrainWand.java
- ElectromagneticKnife.java
- EnergizedRazor.java
- EtherealKnife.java
- EvasiveKnife.java
- Irritant.java
- LightLeatherGauntlets.java
- LightningWand.java
- SerratedRazor.java
- ShadowyDagger.java
- StoneShiv.java
- StoneSword.java

### Armor (~20 files with `<yellow>`)
- AuricCape.java
- BootsOfSpeed.java
- Brightcrown.java
- BurningMantle.java
- ClothBindings.java
- ElbowBrace.java
- EnchantedCloak.java
- Footpads.java
- Gauze.java
- IcyArmguard.java
- IcySigil.java
- IronCuirass.java
- LeatherArmguard.java
- LeatherChestplate.java
- LeatherCowl.java
- LeatherHelmet.java
- LeatherHood.java (also has `<yellow>` for duration — should be `DescUtil.yellow()`)
- NullMagicMantle.java

### Offhands (~19 files with `<yellow>`)
- BatteringRam.java
- EnduranceShield.java
- HastyShield.java (also has plain `"5 second cooldown"` — needs `DescUtil.white("5s")`)
- IronMaiden.java
- LeadingKnife.java
- LeatherBracer.java
- PaladinsShield.java
- PocketWatch.java
- RubyArmament.java
- SmallShield.java
- SpikyShield.java
- TomeOfGravity.java
- VeiledHourglass.java
- VengefulShield.java
- WristBlade.java

### Consumables (~7 files with `<yellow>`)
- MinorHealthPotion.java
- MinorMagicalPotion.java
- MinorManaPotion.java
- MinorPhysicalPotion.java
- MinorShieldsPotion.java
- MinorStaminaPotion.java
- SeraphsPotion.java

---

## Notable Edge Cases

- **DaedalusHammer.java**: Uses `<yellow>` in a chat message (`broadcastOthers`), not a tooltip. May be intentional.
- **RubyArmament.java**: Uses `<white>Power</white>` and `<white>Patience</white>` as stance names (not numeric values). DescUtil conversion is still applicable but these are label names, not values.
- **HastyShield.java**: Has `"5 second cooldown."` — plain text duration that should use `DescUtil.white("5s")`.
- **Gauze.java (armor)**: Has `<white>2</white> seconds` — mixed pattern.
- **PoisonPowder.java**: Has `<white>5</white> seconds` — mixed pattern.
