# Full Equipment Outlier Audit

Date: 2026-05-16

## Scope

This pass reviewed all equipment categories in code:

- Abilities: 374 files (C:77 U:175 R:93 E:29)
- Weapons: 138 files (C:25 U:74 R:19 E:19, O:1)
- Armor: 37 files (C:19 U:13 R:5)
- Accessories: 56 files (C:22 U:20 R:14)
- Offhands: 33 files (C:7 U:17 R:9)
- Consumables: 15 files (C:6 U:6 R:3)
- Cursed: 4 files (placeholder/no rarity constructors)
- Artifacts: 70 files (C:12 U:40 R:15 E:3)

Total reviewed: 727 equipment files (category files only under equipment/* folders).

## Methodology

- Static code review of current source values (post rare nerf updates).
- Numeric scan of costs, cooldowns, damage/shields/status values, proc thresholds, and trigger cadence.
- Condition-weighted evaluation (easy vs hard activation, uptime, reset loops, kill-gating, passive opportunity cost).
- Resource-context check against class economies (base regen/pools and run scaling).
- Reforge dominance heuristic pass (source vs output option candidates using current literals).

## Constraint Checks

### Lower rarity should not be strictly stronger than higher rarity

- No high-confidence strict dominance remained active in current code for same-role comparisons.
- Some medium-confidence condition-driven edge cases remain where lower-rarity options can overperform in niche loops (see watchlist).

### Reforge source should not be strictly stronger than reforged outputs

- Heuristic automated pass found 0 direct source>output numeric dominance candidates.
- No high-confidence strict reforge dominance remained after manual spot checks.

---

## High-Confidence Outliers: Too Strong

### 1) Dying Star (Rare Mage Ability)
- File: src/me/neoblade298/neorogue/equipment/abilities/DyingStar.java
- Why strong:
  - Declared as a power but uses zero mana, zero stamina, zero cooldown in properties.
  - Activates after 2 rifts and then chains repeated pull/explode behavior with kill-spawned rift continuation.
  - High AoE payload per cycle (250/375 dark) with no real cast friction.
- Suggestion:
  - Add a real gate: 20-30 mana, 5-10 stamina, 8-12s cooldown.
  - Or keep free but reduce payload to ~150-220 and stop kill-loop rift recursion.

### 2) Amulet of Offering (Uncommon Classless Artifact)
- File: src/me/neoblade298/neorogue/equipment/artifacts/AmuletOfOffering.java
- Why strong:
  - First health hit grants +1000 mana, +1000 stamina, and +50% general damage for 15s.
  - This massively exceeds normal fight resource and burst envelopes.
- Suggestion:
  - Choose one package:
    - Economy package: +120 to +250 mana/stamina, no damage amp.
    - Burst package: +25% to +35% damage for 8-12s, no full refill.

### 3) Practice Dummy (Rare Classless Artifact)
- File: src/me/neoblade298/neorogue/equipment/artifacts/PracticeDummy.java
- Why strong:
  - Activates after 8 basic attacks with same weapon.
  - Then provides +50% general basic damage until weapon swap.
- Suggestion:
  - Reduce bonus to +25% to +35%.
  - Or keep +40% to +50% and raise activation to 12-15 consecutive hits.

### 4) Force Potion (Uncommon Consumable)
- File: src/me/neoblade298/neorogue/equipment/consumables/ForcePotion.java
- Why strong:
  - +100% general damage for 20s is effectively a full damage doubling window.
- Suggestion:
  - Lower to +40% to +60% for 20s.
  - Alternative: keep +100% and shorten to 8-10s.

### 5) Hasty Shield (Uncommon Warrior Offhand)
- File: src/me/neoblade298/neorogue/equipment/offhands/HastyShield.java
- Why strong:
  - On raised block: 15 flat reduction plus 18/25 mana and 18/25 stamina every 5s.
  - Combines defense and dual-resource acceleration too efficiently.
- Suggestion:
  - Reduce resource return to 8-14 each.
  - Or keep returns and lower reduction from 15 to 8-10.

### 6) Minor Fire Potion (Common Consumable)
- File: src/me/neoblade298/neorogue/equipment/consumables/MinorFirePotion.java
- Why strong:
  - Common-tier consumable with high frontload (300/500 instant splash).
- Suggestion:
  - Reduce to ~180-240 base and ~300-380 upgraded.
  - Or keep damage and add cast delay/telegraph/clearer setup risk.

---

## High-Confidence Outliers: Too Weak

### 1) Piercing Shot (Common Archer Ability)
- File: src/me/neoblade298/neorogue/equipment/abilities/PiercingShot.java
- Why weak:
  - Cost profile 15 mana + 15 stamina at 8s CD for 35/55 bonus damage.
  - Efficiency is below peer common archer actives even accounting for pierce utility.
- Suggestion:
  - If cost unchanged, raise damage toward 55-80.
  - Or lower cost envelope to about 10 mana + 5 stamina.

### 2) Energy Beam (Rare Mage Ability)
- File: src/me/neoblade298/neorogue/equipment/abilities/EnergyBeam.java
- Why weak:
  - 25 mana + 10 stamina, 8s CD, only 60/85 direct line damage.
  - Next-ability cost reduction rider is useful but not enough for rare slot pressure.
- Suggestion:
  - Raise damage toward 100-140.
  - Or reduce cost to about 15-20 mana and 5-8 stamina.

---

## Implementation Defects Impacting Balance

### Minor Mana Relic uses wrong stat
- File: src/me/neoblade298/neorogue/equipment/accessories/MinorManaRelic.java
- Issue:
  - Tooltip says mana regen, but initialize adds stamina regen.
- Impact:
  - The item underperforms its intended role and skews accessory comparisons.
- Suggestion:
  - Change to add mana regen instead of stamina regen.

---

## Medium-Confidence Watchlist

### Abilities
- Rod of Ages: uncapped max-mana growth can snowball long encounters.
  - File: src/me/neoblade298/neorogue/equipment/artifacts/RodOfAges.java
- Storm and Mana Arc: trigger density may push from weak to overtuned depending on enemy count and hit frequency.
  - Files:
    - src/me/neoblade298/neorogue/equipment/abilities/Storm.java
    - src/me/neoblade298/neorogue/equipment/abilities/ManaArc.java

### Weapons
- Grendel's Staff: upgraded variant lowers base weapon damage while adding utility; can feel like non-upgrade in direct DPS contexts.
  - File: src/me/neoblade298/neorogue/equipment/weapons/GrendelsStaff.java

### Cursed Category
- Current cursed items are placeholder/no-op in combat logic, so full balance evaluation is deferred until mechanics are implemented.
  - Files:
    - src/me/neoblade298/neorogue/equipment/cursed/RustySword.java
    - src/me/neoblade298/neorogue/equipment/cursed/DullDagger.java
    - src/me/neoblade298/neorogue/equipment/cursed/MangledBow.java
    - src/me/neoblade298/neorogue/equipment/cursed/GnarledStaff.java

---

## Recently Resolved (No Longer Active Outliers)

- Blackspike rare overperformance was reduced and no longer appears as a top outlier.
  - File: src/me/neoblade298/neorogue/equipment/abilities/Blackspike.java
- Soul Battery normalization landed and no longer appears in top-priority overflow set.
  - File: src/me/neoblade298/neorogue/equipment/abilities/SoulBattery.java
- Mana Infusion now has real tradeoff and no longer sits in previous critical bucket.
  - File: src/me/neoblade298/neorogue/equipment/abilities/ManaInfusion.java

---

## Recommended Implementation Order

1. Fix implementation defect first: Minor Mana Relic stat mismatch.
2. Nerf top swing artifacts/consumables: Amulet of Offering, Force Potion, Practice Dummy.
3. Nerf Dying Star loop behavior (or add real cast gate).
4. Tune Hasty Shield resource/mitigation package.
5. Buff weak floor items: Piercing Shot and Energy Beam.
6. Re-test medium watchlist with run telemetry (encounter density, boss vs trash split, average fight length).