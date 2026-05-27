# Mage Active Ability Cooldown & Mana Reduction Plan

## Overview

This document audits all **active mage abilities** (abilities with cast/use mechanics) and proposes aggressive cooldown reductions (targeting **3-9s** range) and modest mana cost decreases (~10-20%) to improve castability and combat pacing.

**All current values are sourced from the Java source code**, not the balance reference tables (some discrepancies exist).

### Mage Resource Profile
| Max Stamina | Max Mana | Stam Regen/s | Mana Regen/s |
|-------------|----------|--------------|--------------|
| 25          | 50       | 1.0          | 2.0          |

### Key Context
- Mage is mana-dominant; stamina costs are very punishing (1.0/s regen, sprint costs 4/s)
- Players start fights at 0 mana/stamina and accumulate via regen
- 1s channel/charge is an implicit cost on many abilities (reduces DPS window)
- Higher-rarity abilities should retain higher costs (players have ~2-3× regen by uncommon/rare acquisition)
- "Once per fight" and "power activation" abilities have no CD/cost to reduce
- **Target CD range: 3-9s** for all abilities with cooldowns

### Notation
- Values shown as `base/upg` when they change with upgrade, otherwise single value
- `→` indicates proposed new value
- Percentage in parentheses is the reduction amount

---

## Common Active Abilities (8)

| # | Ability | Curr Mana | New Mana | Curr Stam | New Stam | Curr CD | New CD | Effect Summary |
|---|---------|-----------|----------|-----------|----------|---------|--------|----------------|
| 1 | Hex Curse | 5 | 5 (0%) | 0 | 0 | 14s | **6s** (-57%) | 70/105 hex dmg on basics (5s) |
| 2 | Lightning Bolt | 10 | **8** (-20%) | 0 | 0 | 7s | **4s** (-43%) | 70 + 70/140 bonus lightning; ≥30/40 mana threshold; 1s channel |
| 3 | Mana Blitz | 0 | 0 | 0 | 0 | 16s | **7s** (-56%) | +30/50 magical dmg buff (8s) |
| 4 | Mana Shell | 9 | **7** (-22%) | 0 | 0 | 5s | **3s** (-40%) | 4/6 shields (5s) |
| 5 | Overflow | 0 | 0 | 0 | 0 | once | once | Set mana to max; -0.3/0.1 mreg perm |
| 6 | Sear | 15 | **12** (-20%) | 0 | 0 | 13s | **5s** (-62%) | 40/60 fire piercing + 5/8 Burn; 1s charge |
| 7 | Slowing Orb | 5 | 5 (0%) | 0 | 0 | 15/12s | **7/5s** (-53/58%) | Slow II (3s); AoE 3/5 |
| 8 | Windcall | 0 | 0 | 15 | **12** (-20%) | 10s | **5s** (-50%) | -10/15 enemy dmg (5/7s) + knockback; cone(5) |

### Common Rationale

- **Hex Curse**: 14s was far too long for a simple hex buff. 6s lets it be applied meaningfully during fights. Mana stays at 5 (already cheap).
- **Lightning Bolt**: Best common ability — 4s CD with 1s channel = 5s effective cycle. Mana 10→8 to match the faster pace. Conditional bonus keeps it balanced.
- **Mana Blitz**: No mana cost means CD is the only limiter. 7s gives near-100% uptime on the 8s buff — now a reliable damage steroid rather than a rarely-felt spike.
- **Mana Shell**: Spammable shield at 3s. Low shield values (4/6) mean frequency is needed for relevance. Mana 9→7.
- **Overflow**: Once per fight — no changes applicable.
- **Sear**: Low damage DoT applicator — needs to be fast to fill its role. 5s CD with 1s charge = 6s effective cycle. Mana 15→12.
- **Slowing Orb**: Utility CC. 7/5s lets mages contribute meaningful slows. Mana stays at 5.
- **Windcall**: Pure stamina cost. 5s CD with 20% stam reduction. Defensive debuff + knockback on a faster rotation.

---

## Uncommon Active Abilities (29)

| # | Ability | Curr Mana | New Mana | Curr Stam | New Stam | Curr CD | New CD | Effect Summary |
|---|---------|-----------|----------|-----------|----------|---------|--------|----------------|
| 9 | Anchoring Earth II | 20 | **16** (-20%) | 10 | **8** (-20%) | 15s | **7s** (-53%) | 250 earthen proj; +60/90 resist reduce; 1s charge |
| 10 | Arcane Blast | 25 | **20** (-20%) | 0 | 0 | 10s | **5s** (-50%) | 80/120 fire AoE(4) ×2 storable; 1s charge |
| 11 | Avatar State | 0 | 0 | 0 | 0 | auto | auto | Power: activates after 5 casts; +1.5/2.5 mreg, +shields |
| 12 | Collection Hex | 10 | **8** (-20%) | 0 | 0 | 14s | **6s** (-57%) | 120 hex dmg; +10/20 mana + 10/15 shields on proc |
| 13 | Density Orb | 20 | **16** (-20%) | 0 | 0 | 16s | **7s** (-56%) | Slow II + Rift [15s] + 3/5 shields; AoE 3/5 |
| 14 | Drain Lightning | 20 | **16** (-20%) | 0 | 0 | 9s | **5s** (-44%) | 150 lightning line; free +3/5 CDR if mana≥50; 1s channel |
| 15 | Earthen Domain | 20 | **16** (-20%) | 10 | **8** (-20%) | 15s | **7s** (-53%) | 100/150 earthen AoE(5); +10/15 concussed; 1s charge |
| 16 | Earthen Wall | 25 | **20** (-20%) | 10 | **8** (-20%) | 15s | **7s** (-53%) | 250/300 earthen line; barrier +2/4 def +10/15 conc; 1s charge |
| 17 | Electric Orb | 20 | **16** (-20%) | 0 | 0 | 16s | **7s** (-56%) | 40/60 per-pass lightning; +2/3 Elec; passthrough; range 12 |
| 18 | Electrode | 25 | **20** (-20%) | 5 | **4** (-20%) | 12s | **6s** (-50%) | 200/300 lightning proj; +6/9 Electrified; range 16; 1s charge |
| 19 | Eye of the Storm | 30 | **24** (-20%) | 0 | 0 | 16s | **7s** (-56%) | 80/120 ×3 pulse lightning AoE(4); +3/4 Elec/pulse; 1s charge |
| 20 | Fireball II | 25 | **20** (-20%) | 10 | **8** (-20%) | 12s | **6s** (-50%) | 240/360 fire proj AoE(2); +10 Burn; 1s channel |
| 21 | Fireblast | 35 | **28** (-20%) | 10 | **8** (-20%) | 15s | **7s** (-53%) | 240/360 ×3 fan fire; +3 Corruption; 1s channel |
| 22 | Fire Bolt | 25 | **20** (-20%) | 10 | **8** (-20%) | 10s | **5s** (-50%) | 200 ×(1→2) fire+lightning; 2nd shot if mana≥60/50; 1s channel |
| 23 | Firewall | 20 | **16** (-20%) | 5 | **4** (-20%) | 22s | **9s** (-59%) | 50/75 per-tick fire; +3/5 Burn + 1 Corruption; piercing line |
| 24 | Flashfire | 20 | **16** (-20%) | 5 | **4** (-20%) | 12s | **6s** (-50%) | 100/150 fire AoE(6); +5/10 Burn; doubles Burn if >50% mana |
| 25 | Ground Lance | 15 | **12** (-20%) | 10 | **8** (-20%) | 10s | **5s** (-50%) | 120/150 earthen AoE(2); ground-target; 1s charge |
| 26 | Growing Hex | 15 | **12** (-20%) | 0 | 0 | 14s | **6s** (-57%) | 120 (+25/50 per proc) hex dmg; scaling |
| 27 | Heartbeat | 10 | **8** (-20%) | 0 | 0 | 10s | **5s** (-50%) | 40/60 dark per Rift every 3s; AoE(5); creates/repositions Rift |
| 28 | Lightning Strike | 20 | **16** (-20%) | 0 | 0 | 12s | **6s** (-50%) | 100+100/200 lightning AoE(3); if mana≥50/40; 1s channel |
| 29 | Mana Arc | 15 | **12** (-20%) | 0 | 0 | 25s | **9s** (-64%) | Toggle: 60/90 dps lightning, drains 6/5 mana/s; +8/12 Elec |
| 30 | Mana Cloak | 30/20 | **24/16** (-20%) | 10 | **8** (-20%) | 10s | **5s** (-50%) | 12 shields [10s] + Protect + Shell |
| 31 | Overgrowth | 20 | **16** (-20%) | 0 | 0 | 12s | **6s** (-50%) | 150/225 earthen AoE(5); arc projectile |
| 32 | Reckoning Orb | 15 | **12** (-20%) | 0 | 0 | 15s | **7s** (-53%) | Slow II + 50/100% dmg taken debuff [5s]; AoE(5) |
| 33 | Splinterstone | 15 | **12** (-20%) | 5 | **4** (-20%) | 13s | **6s** (-54%) | 160/240 earthen cone(60°); +60/90 pierce +8/12 conc; 1s channel |
| 34 | Storm | 0 (6/tick) | 0 (6/tick) | 0 | 0 | 1s (toggle) | 1s | Toggle: 20/30 per-tick lightning AoE(4); drains 6 mana/tick |
| 35 | To Ashes | 30 | **24** (-20%) | 10 | **8** (-20%) | 12s | **6s** (-50%) | 300 (+50/100 per cast) fire cone; +3/5 Burn; +1 Corr; barrier |
| 36 | Torch | 20 | **16** (-20%) | 5 | **4** (-20%) | 10s | **5s** (-50%) | 160/240 fire AoE(5); +1 Corruption; 1s channel |
| 37 | To Ruins | 15 | **12** (-20%) | 0 | 0 | 10s | **5s** (-50%) | Convert Burn→Intellect (1 per 5/3 stacks); range 10 |

### Uncommon Rationale

- **Anchoring Earth II**: Resist shred projectile on a 7s cycle (8s effective with charge). Dual resource cost is the real gate now.
- **Arcane Blast**: Storable ×2 mechanic — at 5s CD, two charges build up fast. 20% mana cut keeps it mana-hungry (20m per cast).
- **Avatar State**: Power activation — no costs to adjust.
- **Collection Hex**: Mana-generating hex on 6s rotation feeds sustain builds. Self-funding at 8 mana makes it a core engine piece.
- **Density Orb**: Utility orb with Rift generation at 7s. Mana cost (16) is the primary gate now.
- **Drain Lightning**: 5s CD with conditional free cast at ≥50 mana. Channel time (1s) keeps effective cycle at 6s.
- **Earthen Domain / Wall**: Both at 7s with dual resource costs. 1s charge makes effective cycle 8s. Costs (16-20m + 8s) keep them honest.
- **Electric Orb**: Passthrough Electrified applicator at 7s. Faster stacking enables lightning synergies.
- **Electrode**: High single-target + Electrified at 6s (7s effective). Dual cost (20m + 4s) gates it.
- **Eye of the Storm**: 3-pulse AoE at 7s (8s effective). 24 mana is still substantial.
- **Fireball II**: Upgraded Fireball at 6s (7s effective). AoE splash + Burn application on faster rotation.
- **Fireblast**: Triple fan at 7s (8s effective). Highest mana cost (28m) among uncommons keeps it premium.
- **Fire Bolt**: Conditional double-shot at 5s (6s effective). Mana threshold for 2nd shot is the real gate.
- **Firewall**: Zone control at 9s — still the longest uncommon CD, but no longer oppressively slow. Down from 22s.
- **Flashfire**: Burn synergy at 6s (effective). Conditional Burn doubling rewards mana management.
- **Ground Lance**: Clean ground-target AoE at 5s (6s effective). Dual cost gates it.
- **Growing Hex**: Scaling damage on 6s cycle — ramps up ~twice as fast now. Scaling per-proc keeps later casts relevant.
- **Heartbeat**: Rift repositioner at 5s. Cheap (8m) — enables dark/Rift builds.
- **Lightning Strike**: Conditional AoE burst at 6s (7s effective). Mana threshold still limits power.
- **Mana Arc**: Toggle CD 25→9s. Was absurdly long — toggle's real cost is 6/5 mana/s drain, not the CD.
- **Mana Cloak**: Defensive shields + Protect + Shell at 5s. High mana (24/16m) + dual resource keeps it balanced.
- **Overgrowth**: Clean earthen AoE at 6s. Mana (16) is the gate.
- **Reckoning Orb**: +50/100% dmg taken debuff at 7s. Very strong effect warrants slightly longer CD in the uncommon tier.
- **Splinterstone**: Multi-effect cone at 6s (7s effective). Moderate costs.
- **Storm**: Toggle — no CD change. Mana drain rate is the real cost.
- **To Ashes**: Infinite scaling at 6s (7s effective). Mana (24m) + stamina (8s) + Corruption make rapid casting expensive. Scaling is the payoff.
- **Torch**: Straightforward fire AoE at 5s (6s effective). Corruption cost limits spamming.
- **To Ruins**: Burn→Intellect converter at 5s. Utility ability — faster conversion rewards Burn investment.

---

## Rare Active Abilities (14)

| # | Ability | Curr Mana | New Mana | Curr Stam | New Stam | Curr CD | New CD | Effect Summary |
|---|---------|-----------|----------|-----------|----------|---------|--------|----------------|
| 38 | Antimatter | ALL | ALL | 0 | 0 | 5s | **3s** (-40%) | 250/375 dark line; consumes ALL mana; repeatable |
| 39 | Brightest Flame | 60/45 | **48/36** (-20%) | 10 | **8** (-20%) | 0s | 0s | 5×(65/100) fire fan; +3/5 Burn/hit; +2 Corruption; 1s charge |
| 40 | Chaos | 20 | **16** (-20%) | 0 | 0 | 8s | **4s** (-50%) | 175/250 random-type dmg + 4/7 random status |
| 41 | Charge Bolt | 20 | **16** (-20%) | 0 | 0 | 8s | **4s** (-50%) | 175/250 lightning line; -15/25 mana if Elec on target |
| 42 | Dematerialize | 20 | **16** (-20%) | 0 | 0 | 15s | **7s** (-53%) | 175/250 dark AoE(5); explode all Rifts + new Rift [15s] |
| 43 | Dying Star | 0 | 0 | 0 | 0 | power | power | Power: 250/375 dark AoE on Rift expiry; activates after 2 Rifts |
| 44 | Energy Beam | 25 | **20** (-20%) | 10 | **8** (-20%) | 8s | **4s** (-50%) | 140/210 lightning line; reduces next ability mana by 12/20 |
| 45 | Hearth | 80/60 | **64/48** (-20%) | 0 | 0 | once | once | Once: every 10s remove 1 Corruption + heal 3 |
| 46 | Lightning Rod | 60 | **48** (-20%) | 20 | **16** (-20%) | 4s | **3s** (-25%) | 250/425 lightning line; +5/7 Elec; unlocks after 10/7 lightning hits |
| 47 | Nullify | 20 | **16** (-20%) | 0 | 0 | 0s | 0s | Protect + Shell 3/5; cost +30/20 mana per subsequent cast |
| 48 | Power Overwhelming | 0 | 0 | 0 | 0 | power | power | Power: -15/25 mana cost (max 50%); activates after 2 casts |
| 49 | Rock Tomb | 25 | **20** (-20%) | 0 | 0 | 14s | **6s** (-57%) | 250 +40/80 bonus earthen AoE; scales w/ concussed dealt |
| 50 | Soul Battery | 30 | **24** (-20%) | 5 | **4** (-20%) | 12s | **5s** (-58%) | Shields from Elec + 3×(125/190) lightning strikes |
| 51 | Voltaics | 30/15 | **24/12** (-20%) | 5 | **4** (-20%) | once | once | Once: auto-fires 65/100 lightning/2s; bonus at 20/15 Elec threshold |

### Rare Rationale

- **Antimatter**: Mana-dump mechanic at 3s CD. The "consume ALL mana" cost means you need regen time between casts anyway. 3s matches natural mana recovery pace.
- **Brightest Flame**: No CD (0s already). 20% mana cut (60→48 / 45→36) makes the 5-projectile fan more accessible while still expensive.
- **Chaos / Charge Bolt**: Both 8→4s. Random element (Chaos) and mana-refund conditional (Charge Bolt) both benefit from faster cycling. 16m keeps them gated.
- **Dematerialize**: Rift explosion at 7s. Build-defining mechanic cycles much faster — Rift placement (15s duration) is now the bottleneck, not the CD.
- **Dying Star**: Power activation — no direct costs.
- **Energy Beam**: Mana refund ability at 4s (5s effective). Weaving Energy Beam between other casts becomes a core mana management pattern.
- **Hearth**: Once per fight. 20% mana cut (80→64 / 60→48). Still requires significant mana pool investment.
- **Lightning Rod**: Gated ability (10/7 hits to unlock). 4→3s CD is modest because the unlock gate limits true cast rate. Still the most expensive per-cast (48m + 16s).
- **Nullify**: No CD (0s). Escalating +30/20 mana per cast is the gate. Base mana 20→16.
- **Power Overwhelming**: Power activation — no direct costs.
- **Rock Tomb**: Concussed-scaling AoE at 6s. High damage ceiling justifies keeping it at 6s rather than lower. 20m mana gate.
- **Soul Battery**: Triple lightning strike + Electrified shields at 5s. Very powerful but 24m + 4s dual cost per cast.
- **Voltaics**: Once per fight auto-fire. 20% mana cut only.

---

## Epic Active Abilities (3)

| # | Ability | Curr Mana | New Mana | Curr Stam | New Stam | Curr CD | New CD | Effect Summary |
|---|---------|-----------|----------|-----------|----------|---------|--------|----------------|
| 52 | Power Overwhelming II | 0 | 0 | 0 | 0 | power | power | Power: -20/30 mana +2/3 CDR +4/6 shields per cast |
| 53 | Toll of the Arcane | 130/100 | **104/80** (-20%) | 15 | **12** (-20%) | once | once | Once: -50/70% mana cost mult; +3 Corruption |
| 54 | Void Warden | 20 | **16** (-20%) | 0 | 0 | 0s | 0s | Protect/Shell 3/5; +30/20 per cast; passive shields from Rift count |

### Epic Rationale

- **Power Overwhelming II**: Power activation — no direct costs.
- **Toll of the Arcane**: Once per fight mega-buff. 20% mana cut (130→104 / 100→80) + 20% stam cut. The fight-long -50/70% mana multiplier is the payoff for a large upfront investment.
- **Void Warden**: Same structure as Nullify with Rift-based passive shields. Base mana 20→16.

---

## Excluded: Passive Abilities (no cast costs)

The following mage abilities are purely passive (trigger on conditions, no direct mana/stamina/CD) and are excluded from this audit:

**Common**: Absorb, Anchoring Earth, Blast, Create Earth, Erupt, Exertion, Fireball, Force Cloak, Heat Rising, Calculating Gaze, Intuition, Manabending, Mind Growth, Study  
**Uncommon**: Discharge, Electrolysis, Engulf, Head Trauma, Hex Curse II, Mana Guard, Mind Blast, Mind Growth II, Mind Shell, Stone Plating, Study II, Wildfire  
**Rare**: Archmage's Tome, Catalyst Crucible, Corruption, Dissonance, Entropy  
**Epic**: Brilliance, Convergence, I Am Atomic, Void Form

---

## Summary Statistics

### Average CD Reduction by Rarity
| Rarity | Avg Current CD | Avg New CD | Avg Reduction |
|--------|---------------|------------|---------------|
| Common (excl. once) | 11.4s | 5.0s | ~56% |
| Uncommon (excl. auto/toggle) | 13.0s | 6.1s | ~53% |
| Rare (excl. power/once/0s) | 9.3s | 4.4s | ~53% |

### Average Mana Reduction by Rarity
| Rarity | Avg Current Mana | Avg New Mana | Avg Reduction |
|--------|-----------------|-------------|---------------|
| Common (excl. 0-cost) | 9.8 | 8.0 | ~18% |
| Uncommon (excl. 0/toggle) | 20.5 | 16.4 | ~20% |
| Rare (excl. power/ALL) | 33.0 | 26.4 | ~20% |

### Abilities With No Changes
| Ability | Rarity | Reason |
|---------|--------|--------|
| Overflow | Common | Once per fight |
| Avatar State | Uncommon | Power activation (5 casts) |
| Storm | Uncommon | Toggle; mana drain is the real cost |
| Dying Star | Rare | Power activation (2 Rifts) |
| Power Overwhelming | Rare | Power activation (2 casts) |
| Power Overwhelming II | Epic | Power activation (2 casts) |

---

## Potential Concerns

1. **Lightning Bolt at 4s CD**: With 1s channel, effective cycle is 5s. Very fast for a common — but the ≥30/40 mana threshold means it's only at full power when you have resources. Monitor for early-fight dominance.

2. **Mana Blitz near-permanent uptime**: 7s CD on an 8s buff means ~87% uptime with no mana cost. The +30/50 magical damage is now nearly a permanent passive. Consider if this trivializes other damage buffs.

3. **Sear at 5s CD**: Burn applicator that can now stack Burn very quickly (every 6s effective). Combined with Erupt or Flashfire, this could create overwhelming Burn stacks.

4. **To Ashes at 6s CD**: Infinite scaling (+50/100 per cast) on a 6s cycle means damage ramps extremely fast. Combined cost of 24m + 8s + Corruption per cast is the only gate. This will be the highest-DPS uncommon by far in long fights.

5. **Mana Arc 25→9s**: Dramatically faster toggle re-activation. The mana drain (6/5 per second) is now the only real gate. Players can toggle on/off tactically much more often.

6. **Firewall at 9s**: Zone control that was previously limited by 22s CD can now be reapplied frequently. Burn stacking from overlapping Firewalls could be very strong.

7. **Energy Beam at 4s CD**: With the built-in mana refund (-12/20 on next ability), this becomes a core weave ability. At 4s CD, players can reduce the cost of every other cast. Very efficient mana loop.

8. **Soul Battery at 5s CD**: Triple lightning strike + shield generation every 5s is a lot of output. 24m + 4s cost per cast limits spam, but in long fights with good mana regen this will dominate.

9. **General pacing shift**: Moving from 10-16s CDs to 5-7s CDs roughly doubles the number of ability casts per fight. Mana will become the primary bottleneck. Fights will feel much faster and more active.
