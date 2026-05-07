# NeoRogue Balance Audit

Full audit of all 4 classes, flagging unusually strong or weak equipment with rebalancing suggestions.

**Context**: Common costs are ground truth. Uncommon+ costs are not yet reliably balanced (resource progression system). This audit focuses on relative power within each rarity tier and cross-rarity dominance cases.

---

## Priority Legend
- 🔴 **HIGH** — Significantly warps balance; should be addressed first
- 🟠 **MEDIUM** — Notable outlier; adjust when tuning pass happens
- 🟡 **LOW** — Minor concern or build-specific edge case

---

# Warrior

## Critical Issues

### Atone (Common) — 🔴 Overpowered
- **Problem**: 2-3× the efficiency of peer common abilities
- **Suggestion**: Reduce damage or increase cost to bring efficiency in line with common median

### Fury Buffed State (Common) — 🔴 Overpowered
- **Problem**: Effectively infinite efficiency when buff conditions are met
- **Suggestion**: Add a real cost or cap the buff duration/stacks

### Crescent Axe (Weapon) — 🔴 Dominated
- **Problem**: Strictly dominated by Crimson Blade (same tier, higher damage, better effect)
- **Suggestion**: Give Crescent Axe a unique secondary (e.g., AoE cleave) or increase damage

### Earthen Tackle (Uncommon) — 🔴 Underpowered
- **Problem**: Worse than Break the Line in every metric (less damage, similar cost, same CD)
- **Suggestion**: Increase damage to 180→250 or add concussed stacks (8→12)

### Siphoning Strike (Rare) — 🔴 Underpowered
- **Problem**: Deals uncommon-tier damage at rare cost. Heal doesn't compensate for the damage gap
- **Suggestion**: Increase damage to 250→350 or double the heal component

### Hero's Landing (Rare) — 🔴 Overpowered
- **Problem**: Zero-cost ability with trivially met condition, delivering rare-tier value
- **Suggestion**: Add a stamina cost (15-20) or make the condition more restrictive

## Medium Issues

| Item | Rarity | Issue |
|------|--------|-------|
| Cleave | Common | Best-in-class efficiency (overtuned) |
| Burst | Common | Zero-cost output exceeds Battle Cry |
| Bide | Uncommon | Exceeds Brace II + bonus damage |
| Titan | Rare | Vastly exceeds Mortal Engine in value |
| Rampage | Rare | Uncapped stacking (no ceiling mentioned) |
| Windcutter | Uncommon | Strong passive with no downside |

## Weak Items

| Item | Rarity | Issue |
|------|--------|-------|
| Empowered Edge | Common | Below-median efficiency |
| Tackle | Common | Weak damage for cost/CD |
| Leather Gauntlets | Common | Negligible defensive value |
| Blessed Edge | Uncommon | Overcosted for effect |
| Ironskin | Uncommon | Shields too low vs cost |
| Bulldoze | Uncommon | Worse than peers at same cost |
| Pin | Uncommon | Condition too restrictive |
| Charge | Common | Damage/cost ratio bottom of tier |
| Cull | Rare | Below-rare performance |
| Dark Pact | Rare | Self-damage disproportionate to output |
| Limit Break | Rare | HP cost not justified by payoff |
| Iron Sword | Common wpn | Outclassed by other common weapons |

---

# Thief

## Critical Issues

### Blackspike (Rare) — 🔴 Overpowered
- **Problem**: 3×200→300 = 600→900 damage for 35 cost, 8s CD. Efficiency 17.1→25.7 dmg/cost and CD-DPS of 75→112.5 are 3-4× stronger than peer rare actives. Numbers are literally epic-tier (compare to Piercing Night: 35 cost, 8s CD, 600→900).
- **Suggestion**: Reduce per-projectile to 130→200 (total 390→600), or increase cost to 50, or increase CD to 12s.

### Preparation (Uncommon) — 🔴 Underpowered
- **Problem**: 40 cost, 25s CD, 5s charge for only 70→100 damage + 10→15 shields. Worst efficiency in entire uncommon tier (1.75→2.5 dmg/cost). Below common Twin Shiv's 130→180 for 15 cost.
- **Suggestion**: Rework — reduce cost to 25, CD to 15s, remove charge, increase damage to 150→200 + 15→25 shields.

### Assassinate (Rare) — 🔴 Underpowered
- **Problem**: 180 piercing for 30→40 stamina = common-tier damage at rare cost. Upgraded cost appears to INCREASE (30→40) which may be a data bug.
- **Suggestion**: Increase damage to 300→400, or reduce cost to 20. Verify upgrade cost direction.

### Pandemic vs Corrode (Epic) — 🔴 Strict Dominance
- **Problem**: Pandemic exceeds Corrode in every metric (+60→90% vs +40→60%, +120→180 vs +80→120 poison, AoE spread vs none).
- **Suggestion**: Buff Corrode to +50→80% and add unique mechanic (e.g., resistance bypass or dark conversion on poison ticks).

## Medium Issues

| Priority | Item | Rarity | Issue |
|----------|------|--------|-------|
| 🟠 | Speed Blitz | Rare | 26.7→40 dmg/cost, highest in tier; auto-target removes skill gate |
| 🟠 | First Strike | Uncommon | 50-125% above median + CD resets on stealth/evade |
| 🟠 | Escape Plan II | Uncommon | Worse efficiency than its own COMMON version |
| 🟠 | Flash Spark | Rare | Common-tier DPS (12.5→16.7 CD-DPS) at rare rarity |
| 🟠 | Butterfly Knife | Common wpn | 75%+ DPS over other commons (43.75 vs 25) |
| 🟠 | Tempest | Rare | Self-refueling AoE loop; no internal CD |
| 🟠 | Shadow Partner | Rare | Free 150→250 DPS, no condition |

## Low Priority

| Item | Rarity | Issue |
|------|--------|-------|
| Quick Feet | Common | 25 cost, 20s CD for just 1→2 evade |
| Escape Plan | Common | Below-median efficiency |
| Danger Close | Rare | Uncapped permanent +6→10% per evade |
| Maim | Uncommon | 6× common Cripple's cost for less vuln |
| WE: Electrified | Common | Zero-cost 13-20 DPS passive |
| Analyze II vs Paranoia | Epic | 33-50% power gap, same archetype |
| Flash Mark vs Blackspike | Rare | Flash Mark costs more for 70-75% less damage |
| Body Double | Uncommon | Zero damage, high cost, short duration |
| Spark Knife | Common wpn | Lowest DPS weapon |

---

# Archer

## Critical Issues

### Mana Infusion — Upgraded (Uncommon Toggle) — 🔴 Overpowered
- **Problem**: Costs 3 mana/shot but regens 4 mana/hit = net +1 mana/shot while dealing +30 damage. Permanent +42 DPS at 1.4 hits/s with ZERO downside (gains resources). Doesn't use the standard -15 BA framework.
- **Comparisons**: Cauterize (-21 initially, slow ramp), Hexing Shot (-21 + kill-gated AoE), Wound (-21 + Injury)
- **Suggestion**: Reduce mana regen to 1→2/hit (net drain always), OR reduce damage to +15→20.

### Ricochet (Rare Passive) — 🔴 Overpowered
- **Problem**: Every close-range BA fires 120→180 damage projectile. At 1.4 hits/s = 168→252 passive DPS with no cost/cooldown. 2× stronger than peer rare passives (Ferocious Draw 84→112, Dangerous Game 70→98).
- **Suggestion**: Reduce to 80→120 per proc (112→168 DPS, still best-in-class but gap reduced).

### Piercing Shot (Common) — 🔴 Underpowered
- **Problem**: 30 total cost for +35→55 bonus = 1.2→1.8 dmg/cost. Worst efficiency of any common by 5-8×. Even with pierce utility and 8s CD, far too weak.
- **Comparisons**: Arrow Rain (9→12 dmg/cost), Quick Trap (10→15), Lay Explosive (2.5→3.0)
- **Suggestion**: Reduce cost to 10m+5s (15 total) OR increase bonus to +60→90.

### Cauterize (Uncommon Toggle) — 🔴 Overpowered
- **Problem**: Uncapped exponential scaling. -15 BA → Injury 15/hit + 1.2→1.8 dmg per Injury stack. After 13 stacks (1 second), already exceeds -15 sacrifice. At 50 stacks: +60→90/hit net.
- **Suggestion**: Cap damage bonus at +30→45/hit, OR reduce scaling to +0.6→1.0/Injury stack.

## Medium Issues

| Priority | Item | Rarity | Issue |
|----------|------|--------|-------|
| 🟠 | Pressure | Uncommon | 22→33 dmg/mana (3-5× peer efficiency). Needs both-in-zone but 8-rad is generous |
| 🟠 | Neckbreaker | Rare | ×2→3 multiplier on same-target; trivially easy vs bosses |
| 🟠 | Magic Quiver | Uncommon acc | +9.5→14.3 DPS — weaker than common Saboteur's Ring (+28-42 DPS at 50 Injury) |
| 🟠 | Shattering Shot | Uncommon | 40 cost for 60→90 = 1.5→2.25 eff. BELOW common Piercing Shot |
| 🟠 | Stormweaver's Promise | Rare | 4→6 shields per hit = 5.6→8.4 sh/s permanently, exceeds Warrior sustain |
| 🟠 | Dangerous Game | Rare | Outclassed by Ferocious Draw (same trigger, less damage, no pierce) |
| 🟠 | Zone | Common | 14.9→22.4 dmg/cost, 2-6× better than peer commons |

## Low Priority

| Item | Rarity | Issue |
|------|--------|-------|
| Lay Explosive | Common | 50 dmg at same cost/CD as Lay Trap (120 dmg + CC + shields) |
| Backstep (upgraded) | Common | 1 stam for 6 shields = 6.0 sh/stam |
| Tailwind | Common | Overcosted vs Backstep and Lay Trap |
| Wind Trap | Common | No damage, weaker CC than Lay Trap at same price |
| Spike Trap | Uncommon | Up to 800 dmg/20 mana if enemies stay (situational) |
| Prey Seeker | Uncommon | 15→22.5 dmg/stam at 2s CD |
| Momentum | Uncommon | +10→20 movement condition; weaker than common Point Blank |
| Eagle Feather | Common acc | No combat value vs Flint Pendant/Saboteur's Ring |
| Predator Drive | Rare | 47-71% effective CDR from close-range |
| Entrench | Rare | +3→5 shields/trap vs Stormweaver's 5.6→8.4/s |

### Structural Issue: Close-Range Archetype
The close-range archer build (Ricochet + Ferocious Draw + Predator Drive + Grit + Dangerous Game) stacks to absurd combined levels. Each individually strong; together they create a dominant archetype with no comparable long-range counterpart. Consider buffing long-range passives or adding wider internal variance to close-range options.

---

# Mage

## Critical Issues

### Soul Battery (Rare) — 🔴 Overpowered
- **Problem**: **0 mana, 0 stamina**, 12s CD → 3×150→225 = 450→675 AoE3 damage. FREE rare-tier damage. CD-DPS = 37.5→56.25. Condition (Electrified stacks dealt) is trivially met in any lightning build.
- **Comparisons**: Charge Bolt (200→300 for 20 mana), Lightning Rod (300→500 for 80 cost), Dying Star (300→450 for 70 cost)
- **Suggestion**: Add 20-25 mana cost, OR reduce to 2 strikes (300→450), OR increase CD to 18s.

### Ground Lance (Uncommon) — 🔴 Underpowered
- **Problem**: 120→150 for 25 cost, 10s CD. Efficiency 4.8→6.0 is BELOW common median. A common ability (Create Earth) does more damage with status for similar cost.
- **Comparisons**: Splinterstone (160→240 for 20 cost + status), Overgrowth (150→225 for 20), Create Earth (common: 120→160 for 30)
- **Suggestion**: Increase damage to 180→250, OR reduce cost to 10m+5s (15), OR add concussed (8→12).

### Lightning Bolt — Upgraded (Common) — 🟠 Overtuned
- **Problem**: 210 damage for 10 mana, 7s CD = 21 dmg/mana and 30 CD-DPS. That's 2-3× the common median. The ≥40 mana condition is trivial at 50 base pool.
- **Suggestion**: Reduce bonus to 70→105 (total 175), OR raise threshold to ≥50 mana.

### Energy Beam (Rare) — 🟠 Underpowered
- **Problem**: 70→100 damage for 35 cost, 8s CD. Even accounting for -15→25 mana cost reduction on next ability, net value is common-tier performance at rare rarity.
- **Comparisons**: Charge Bolt (200→300 for 20 mana), Chaos (200→300 for 20 mana + status)
- **Suggestion**: Increase damage to 150→200, OR increase mana reduction to 25→40.

### Sear (Common) — 🟠 Underpowered
- **Problem**: 40→60 damage for 15 mana, 13s CD = 2.7→4.0 dmg/mana. Less than HALF the common median. Even with 5→8 Burn stacks, effective efficiency barely reaches bottom quartile.
- **Comparisons**: Fireball (160→240 for 20), Erupt (100→150 for 15), Hex Curse (70→105 for 5)
- **Suggestion**: Increase damage to 70→100, OR reduce cost to 10, OR increase Burn to 8→12.

## Medium Issues

| Priority | Item | Rarity | Issue |
|----------|------|--------|-------|
| 🟡 | Mana Blitz | Common | Free 150-400 bonus damage per 16s cycle |
| 🟡 | Dark Scepter | Common wpn | 20-27 dmg/mana on basics (120-160 sustained DPS) |
| 🟡 | Mana Shell | Common | 4-6 shields negligible in practice |
| 🟡 | Storm vs Mana Arc | Uncommon | Storm deals 20→30/tick for 6 drain; Mana Arc does 60→90/s for same drain = 3× worse |
| 🟡 | Splinterstone | Uncommon | Common-tier cost (20) for uncommon output + concussed + pierce |
| 🟡 | Dying Star | Rare | 50 stamina cost exceeds mage's max pool (25 base) — literally uncastable without heavy investment |
| 🟡 | Antimatter | Rare | 2.4 dmg/mana initial = worst rare efficiency |
| 🟡 | Absorb | Common | 4 dmg/mana with unreliable kill condition |
| 🟡 | Blast | Common | 2s charge for mediocre 80 damage |
| 🟡 | Electric Orb | Uncommon | Low DPS vs Electrode at same tier |
| 🟡 | Old Staff | Common wpn | <25% mana condition anti-synergizes with threshold abilities |

### Structural Issue: Lightning Rare Tier
Soul Battery (free), Charge Bolt (conditionally free), and Lightning Rod (insane DPS once unlocked) are all top-tier with minimal tradeoff differentiation. The archetype needs cost/gating spread so they don't all go online simultaneously.

### Structural Issue: Earthen Uncommon Underpowered
Ground Lance, Earthen Domain, and Overgrowth are all notably weaker per-cost than fire/lightning peers at uncommon. Only Splinterstone and Earthen Wall are competitive.

### Dominance Case: Storm vs Mana Arc
If both tick at the same rate (1s), Mana Arc does 60→90 damage per tick for 6→5 mana/s while Storm does 20→30 per tick for 6 mana/s. Mana Arc is strictly 3× better. One must be adjusted (increase Storm to 40→60/tick, or reduce drain to 3-4/tick).

---

# Cross-Class Observations

## Recurring Patterns

1. **Zero-cost passives with no cap** are the most frequent source of overtuning:
   - Warrior: Fury, Burst
   - Thief: WE:Electrified, Shadow Partner, Tempest, Danger Close
   - Archer: Mana Infusion (net gain), Ricochet, Predator Drive
   - Mage: Soul Battery, Mana Blitz

2. **Uncommon abilities priced below commons** create confusion:
   - Mage: Splinterstone costs 20 (common-tier) for uncommon output
   - Archer: Prey Seeker costs 10 for uncommon-tier trap damage
   - Thief: First Strike costs 10 for uncommon-tier burst + resets

3. **Rare abilities at common efficiency** indicate forgotten items:
   - Warrior: Siphoning Strike, Cull
   - Thief: Assassinate, Flash Spark
   - Archer: Dangerous Game (dominated by Ferocious Draw)
   - Mage: Energy Beam, Antimatter

4. **Uncapped scaling** creates degenerate late-fight situations:
   - Thief: Danger Close (+6→10% per evade, permanent)
   - Archer: Cauterize (+1.2→1.8 per Injury, no cap)
   - Warrior: Rampage (stacking, no cap mentioned)

## Master Priority Table

| Priority | Class | Item | Issue |
|----------|-------|------|-------|
| 🔴 | Mage | Soul Battery | 450-675 FREE AoE damage |
| 🔴 | Archer | Mana Infusion (upg) | Net mana gain + free +42 DPS |
| 🔴 | Archer | Ricochet | 2.4× stronger than peer rare passives |
| 🔴 | Thief | Blackspike | Epic-level damage at rare cost |
| 🔴 | Archer | Cauterize | Uncapped exponential scaling |
| 🔴 | Warrior | Atone | 2-3× common efficiency |
| 🔴 | Warrior | Fury | Infinite efficiency in buff state |
| 🔴 | Archer | Piercing Shot | 5-8× worse than common peers |
| 🔴 | Thief | Preparation | Worst uncommon efficiency; below commons |
| 🔴 | Thief | Assassinate | Common-tier at rare cost; upgrade cost bug? |
| 🔴 | Thief | Pandemic vs Corrode | Strict dominance at epic |
| 🔴 | Mage | Ground Lance | Below-common performance at uncommon |
| 🔴 | Warrior | Crescent Axe | Dominated by Crimson Blade |
| 🔴 | Warrior | Siphoning Strike | Uncommon damage at rare |
| 🔴 | Warrior | Hero's Landing | Zero-cost, trivial condition |
| 🟠 | Thief | Speed Blitz | Highest rare dmg/cost (26.7→40) |
| 🟠 | Archer | Pressure | 3-5× peer efficiency |
| 🟠 | Mage | Lightning Bolt (upg) | 2-3× common median |
| 🟠 | Thief | First Strike | 50-125% above median + resets |
| 🟠 | Archer | Neckbreaker | ×3 trivially achievable vs bosses |
| 🟠 | Mage | Energy Beam | Common-tier at rare |
| 🟠 | Mage | Sear | Half the common median |
| 🟠 | Archer | Magic Quiver | Uncommon weaker than common Saboteur's |
| 🟠 | Archer | Shattering Shot | Below common Piercing Shot efficiency |
| 🟠 | Thief | Escape Plan II | Worse than own common version |
| 🟠 | Thief | Shadow Partner | Free 150-250 DPS |
| 🟠 | Thief | Tempest | Self-refueling loop |
| 🟠 | Warrior | Titan | Vastly exceeds Mortal Engine |
| 🟠 | Warrior | Rampage | Uncapped stacking |
| 🟠 | Archer | Stormweaver's | 5.6-8.4 shields/s permanently |
| 🟠 | Archer | Zone | 2-6× common peers |
