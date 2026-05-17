# Rare Equipment Nerf Plan

## Overview

Reduce all rare equipment power by ~10-20% across all 4 classes to bring rares from epic-tier power (4-6× common) into the intended 2.5-4× common range. Nerf output only — costs and cooldowns stay unchanged.

**Total files to modify: ~113 source files + 5 balance docs**

---

## General Rules

| Value Type | Reduction | Method |
|---|---|---|
| Damage | ~15% | Round to nearest 5 |
| Shields | ~15% | Round to nearest clean number |
| Status stacks | -1 to -2 | Per application instance |
| % multipliers | -3 to -5 pp | Flat reduction |
| Weapon damage | ~12% | Round to nearest 5 |
| Accessory effects | ~15% | Proportional |
| Costs/cooldowns | **No change** | Nerf output, not friction |

**Skip exceptions**: Items with major downsides (Dark Pact), build-defining + high cost (Limit Break), utility-only (Hearth, Nullify, Egoism, Ring of Night), already-low values (Corpse Explosion, Stormweaver's Promise), artifacts (Lionheart Bangle, Sigil, Merchant Spyglass, Tarot Card).

---

## Phase 1: Warrior Rare

### Abilities (19 changed, 2 skipped)

| Ability | Current (Base→Upg) | New (Base→Upg) | Notes |
|---|---|---|---|
| Blood Frenzy | str +15→20; atk spd +10→13% | str **+13→17**; atk spd **+8→11%** | Passive |
| Chosen of Light | +20→30% magical buff | **+15→25%** | Passive |
| Compounding Injury | 100→150% repeat mult | **80→125%** | Passive |
| Consecrate | 250→350 dmg; 15→20 shields | **210→300** dmg; **12→17** shields | Active |
| Cull | 200→300 dmg | **175→260** | Active |
| Feel No Pain | 5→8% DR per 10 berserk | **4→7%** | Passive |
| Fortify | 200 dmg | **175** | Active |
| Fortress | 40→60 shields | **35→50** | Passive |
| Guardian Spirit | shields +4→6; sanct +3→5 | shields **+3→5**; sanct **+2→4** | Passive |
| Herculean Strength | str +4→6; berserk 3→5; conc +3→5 | str **+3→5**; berserk **2→4**; conc **+2→4** | Passive |
| Lightfall | 400→600 dmg; sanct 15→25 | **350→500**; sanct **12→20** | Active |
| Light Pulse | 160→240 dmg | **135→200** | Passive |
| Mighty Swing | 300 dmg | **250** | Active |
| Rampage | 200→300 dmg; +50→75/cast | **175→250**; **+40→60** | Active |
| Reckless Swing | berserk ×15→20 | berserk **×13→17** | Active |
| Siphoning Strike | 180 dmg; str +15→25 | **150**; str **+12→20** | Active |
| Thorn Garden | +4→6 thorns per 3 shields | **+3→5** | Passive |
| Tireless | shields 10→15 | **8→12** | Passive |
| Hullbreaker | 100 wpn dmg; def -10→15 | **85**; def **-8→12** | Offhand |
| *Dark Pact* | — | SKIP | Has -50% dmg taken downside |
| *Limit Break* | — | SKIP | Build-defining, high cost |

### Weapons (9)

| Weapon | Current | New |
|---|---|---|
| Avalonian Mace | 100 dmg; str mult 5→8x | **85**; **4→7x** |
| Fracturer | 100→130 dmg; line 40→60 | **85→110**; **35→50** |
| Groundbreaker | 160→200 dmg; conc 3→5 | **140→175**; **2→4** |
| Iron Sword | 65→80 dmg | **55→70** |
| Righteous Flame | 70→90 dmg; sanct 3→5 | **60→75**; **2→4** |
| Righteous Hammer | 110 dmg; sanct 3→5 | **95**; **2→4** |
| Righteous Lance | 80 dmg | **70** |
| Rising Sun | 200→300 dmg | **175→250** |
| Valiant Pierce | 150→200 dmg; bonus 200→300 | **130→175**; bonus **175→250** |

### Accessories (3 changed, 2 skipped)

| Accessory | Current | New |
|---|---|---|
| Earthen Bracer | conc app +50→80% | **+40→65%** |
| Major Shielding Relic | +3→5% max HP as shields | **+2→4%** |
| Vermillion Belt | 1 berserk per 5→4 attacks | per **6→5** |
| *Lionheart Bangle* | — | SKIP (artifact) |
| *Sigil of Iron Legion* | — | SKIP (artifact) |

---

## Phase 2: Mage Rare

### Abilities (16 changed, 2 skipped)

| Ability | Current (Base→Upg) | New (Base→Upg) | Notes |
|---|---|---|---|
| Antimatter | 300→450 dmg | **250→375** | Active |
| Archmage's Tome | +30→45 mag dmg; mreg +0.5→0.8 | **+25→38**; **+0.4→0.7** | Passive |
| Brightest Flame | 80→120/proj (×5); burn 4→6 | **65→100**/proj; burn **3→5** | Active |
| Catalyst Crucible | +15→25% mag mult; mana +4→6 | **+12→20%**; mana **+3→5** | Passive |
| Chaos | 200→300 dmg; status 5→8 | **175→250**; status **4→7** | Active |
| Charge Bolt | 200→300 dmg; refund -20→30 | **175→250**; refund **-15→25** | Active |
| Corruption | -3→5 mana/stack | **-2→4** | Passive |
| Dematerialize | 200→300 dmg | **175→250** | Active |
| Dissonance | mana +6→9; shields 3→5 | mana **+5→8**; shields **2→4** | Active (once) |
| Dying Star | 300→450 dmg | **250→375** | Power |
| Energy Beam | 70→100 dmg; next cost -15→25 | **60→85**; **-12→20** | Active |
| Lightning Rod | 300→500 dmg; elec 6→9 | **250→425**; elec **5→7** | Active |
| Power Overwhelming | cost reduction -20→30 | **-15→25** | Power |
| Rock Tomb | 300 + 50→100 bonus | **250 + 40→80** | Active |
| Soul Battery | 150→225/bolt (×3); shield div 9→6 | **125→190**; div **10→7** | Active |
| Voltaics | 80→120/5s; base elec +3 | **65→100**; elec **+2** | Power |
| *Hearth* | — | SKIP | Utility/sustain only |
| *Nullify* | — | SKIP | Self-limiting (escalating cost) |

### Weapons (2)

| Weapon | Current | New |
|---|---|---|
| Grendel's Staff | 65→55 dmg; mana +4→6; int per 5→3 | **55→45**; mana **+3→5**; int per **6→4** |
| Rune Harvester | 40→55 dmg; CDR 4→6s | **35→45**; CDR **3→5s** |

### Accessories (1)

| Accessory | Current | New |
|---|---|---|
| Blighted Earrings | +30→50% dmg w/ Corruption | **+25→40%** |

---

## Phase 3: Thief Rare

### Abilities (25 changed, 2 skipped)

| Ability | Current (Base→Upg) | New (Base→Upg) | Notes |
|---|---|---|---|
| Analyze | 50→75 dmg/stack; 4 shields/stack | **40→65**; **3** shields | Passive |
| Assassinate | 180 dmg | **150** | Active |
| Blackspike | 150→225/proj (×3) | **125→190** | Active |
| Blight Tendril | 150→200 poison proj | **125→175** | Power |
| Constant Flux | 10→15 phys/stack | **8→12** | Active |
| Danger Close | +6→10% phys/evade | **+5→8%** | Power |
| Dread | +40→60% general dmg | **+30→50%** | Power |
| Finale | 360 base; +50 per stam threshold | **300**; **+40** | Active |
| Flash Mark | 180→250 dmg; elec 10→15 | **150→210**; elec **8→12** | Active |
| Flash Spark | 150→225 dmg; elec 6→9 | **125→190**; elec **5→7** | Active |
| Lord of the Night | +20→30%/stealth stack | **+15→25%** | Power |
| Mastermind | +20→30% base; +40→60% scaled | **+15→25%**; **+30→50%** | Passive |
| Obfuscation | insanity +20→30% | **+15→25%** | Power |
| Odin's Decree | 100→150 dmg; elec 8→12 | **85→125**; elec **6→10** | Power |
| Saboteur | +10→15% trap/close; injury +20→30% | **+8→12%**; **+15→25%** | Passive |
| Shadow Partner | 150→250 dmg | **125→210** | Power |
| Spark Trap | line 200→300; AoE 100 | **175→250**; AoE **85** | Active |
| Speed Blitz | 40→60/hit (×5) | **35→50** | Active |
| Static Surge | +30→50 lightning; elec 5→8 | **+25→40**; elec **4→7** | Power |
| Tempest | 400→600 AoE; elec 4→6 | **340→500**; elec **3→5** | Power |
| Thrive in Chaos | stealth per 10→7 insanity | per **12→8** | Power |
| Thunderclap & Flash | 150→200 + 100→150 line | **125→175** + **85→125** | Active |
| Twilight | dur +3→5s; stam +10 | dur **+2→4s**; stam **+8** | Power |
| Umbral Volley | 60→80/proj (×5) | **50→65** | Passive |
| WE: Darkness | 200→300 proj | **175→250** | Passive |
| Wall Jump | 200→300 line | **175→250** | Active |
| *Corpse Explosion* | — | SKIP | Already very low (4→5/s) |
| *Egoism* | — | SKIP | Small heal utility |

### Weapons (3)

| Weapon | Current | New |
|---|---|---|
| Iron Throwing Knife | 50→60 dmg | **45→50** |
| Noxian Falx | 55→65 dmg | **45→55** |
| Toxic Razor | 50 dmg; +5 per threshold | **45**; **+4** |

### Accessories (2 changed, 1 skipped)

| Accessory | Current | New |
|---|---|---|
| Cobra Crest | mag def -30→50 | **-25→40** |
| Plaguebearer | shields 7→10 | **6→8** |
| *Ring of Night* | — | SKIP (utility) |

---

## Phase 4: Archer Rare

### Abilities (26 changed, 1 skipped)

| Ability | Current (Base→Upg) | New (Base→Upg) | Notes |
|---|---|---|---|
| Absolute Zero | frost 8→12 AoE | **6→10** | Passive |
| Blizzard | 60→90/tick | **50→75** | Power |
| Conflagration | 150 fixed; transfer 1.0→1.5× | **125**; **0.8→1.2×** | Power |
| Crush the Weak | per 20→15 injury; shield dmg 40→60% | per **25→18**; **35→50%** | Power |
| Dangerous Game | bonus +50→70 | **+40→60** | Power |
| Deliberant Pace | per 5→4s; +5→6%/Focus | per **6→5s**; **+4→5%** | Power |
| Emberhail | +20→30 proj dmg; +20→30% status | **+15→25**; **+15→25%** | Passive |
| Entrench | +3→5 perm shields | **+2→4** | Power |
| Ferocious Draw | +60→80 piercing | **+50→65** | Power |
| Flashfire Volley | 100→150 blunt + 100→150 fire; burn 10→15 | **85→125** + **85→125**; burn **8→12** | Active |
| Frigid Wind | 100→150 ice; frost 10→15 | **85→125**; frost **8→12** | Active |
| Frozen Tomb | 400 burst; +20→30% frost; spread 10 | **340**; **+15→25%**; spread **8** | Passive |
| Hawkeye | 300→500 proj dmg | **250→425** | Power |
| Hellfire | +80→120 fire bonus | **+65→100** | Power |
| Infernal Warden | 120→180 fire; burn 8→12 | **100→150**; burn **6→10** | Active |
| Lacerating Wave | 40→60 AoE | **35→50** | Passive |
| Neckbreaker | 100→150 ×3; injury 3→5 | **85→125** ×3; injury **2→4** | Active |
| Pinning Shot | wall bonus 200→300 | **175→250** | Active |
| Predator Drive | per 3→2 close hits: -1s CDs | per **4→3** | Power |
| Relentless Hunt | +20→30%/Focus | **+15→25%** | Active |
| Ricochet | 120→180 ricochet dmg | **100→150** | Power |
| Saboteur (Archer) | +10→15% trap/close; injury +20→30% | **+8→12%**; **+15→25%** | Passive |
| Scavenger | stam +20→30; dmg +3→5% | stam **+15→25**; dmg **+2→4%** | Power |
| Steady Bleed | 10→15/stack; shield 20→40% | **8→12**; shield **15→35%** | Active |
| Trinity Force | 30 dmg/hit (×3 ×2→3) | **25** (total 150→225) | Active |
| Wings of Judgment | 50→100/arrow (×8) | **40→80** (total 320→640) | Active |
| *Stormweaver's Promise* | — | SKIP | Already low (1→2 shields) |

### Weapons (5)

| Weapon | Current | New |
|---|---|---|
| Athena's Longbow | 50 dmg; non-BA +50→100% | **45**; **+40→80%** |
| Divergent Piercer | 20→25 dmg; bonus +20→40 | **15→20**; **+15→35** |
| Enchanted Crystal Arrow | bonus +10→20 | **+8→15** |
| Iron Bolt | 30→40 dmg; status +50→100% | **25→35**; **+40→80%** |
| Pocket Ballista | 70 dmg; explode 40→80 | **60**; **35→65** |

### Accessories (2)

| Accessory | Current | New |
|---|---|---|
| Ring of Exploitation | DR per 10→8 injury; shields 5→7 | per **12→10**; shields **4→6** |
| Ring of the Eagle | per 12→10 hits: -1s CDs | per **14→12** |

---

## Phase 5: Shared Equipment

| Equipment | Current | New |
|---|---|---|
| Major Stamina Relic | regen +1→1.5; max stam +30→50 | regen **+0.8→1.2**; max stam **+25→40** |

---

## Phase 6: Update Balance References

After all code changes, update:
- `balance-warrior.instructions.md`
- `balance-mage.instructions.md`
- `balance-thief.instructions.md`
- `balance-archer.instructions.md`
- `balance-reference.instructions.md`

Also:
- Add 7 undocumented archer rares to reference (Flashfire Volley, Frigid Wind, Frozen Tomb, Infernal Warden, Lacerating Wave, Relentless Hunt, Saboteur)
- Correct Saboteur class assignment (Archer, not Thief)
- Update Rarity Power Budget table

---

## Verification

1. Build after each phase to catch compile errors
2. Spot-check the 5 biggest nerfs per class by re-reading the modified source
3. In-game tooltip audit: 3-4 rares per class to confirm values match
4. Power ratio sanity check — rare damage should average ~2.5-3.5× common baselines
5. Grep balance docs for old values to ensure consistency
