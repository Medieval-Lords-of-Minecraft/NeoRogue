# NeoRogue Cost Audit — Placeholder & Unbalanced Costs

Systematic review of mana/stamina/cooldown costs across all classes. Focuses on costs that appear to be forgotten placeholders, increase on upgrade (bugs), or form suspicious identical-cost clusters.

Common ability costs are treated as ground truth. Uncommon+ costs are the focus.

---

## Resource Progression Context

Drop tier = `regionsCompleted × 2` + score bonuses. 8 fights per region, 3 equipment seen per win (choose 1).

**Rarity acquisition timing:**
- **Uncommon**: 10% chance from tier 1 (fight 2+), dominant from tier 3 (~fight 4-5 of R1)
- **Rare**: 10% chance from tier 2 (R1 miniboss), dominant from tier 6 (R3 start)
- **Epic**: 10% chance from tier 5 (R2 late), dominant from tier 8 (R3 late)

**Resource progression** — players split gains between health, mana, and stamina (~60 total available per region, split ~3 ways for hybrid classes, ~2 ways for dominant-resource classes):

| Class | When first Uncommon (R1 early) | When Rares common (R2 end) | When Epics appear (R2 late) |
|-------|-------------------------------|---------------------------|----------------------------|
| Warrior (stam/hp split) | ~55-60 stam / ~25-28 mana | ~90-110 stam / ~30-40 mana | ~100-120 stam / ~35-45 mana |
| Thief (3-way split) | ~50-55 stam / ~33-38 mana | ~70-85 stam / ~45-55 mana | ~80-95 stam / ~50-60 mana |
| Archer (3-way split) | ~45-50 stam / ~38-43 mana | ~65-80 stam / ~50-65 mana | ~75-90 stam / ~60-75 mana |
| Mage (mana/hp split) | ~28-32 stam / ~55-65 mana | ~35-45 stam / ~90-110 mana | ~40-50 stam / ~100-120 mana |

## Key: Issue Types
- **BUG?** — Cost increases on upgrade (opposite of expected)
- **PLACEHOLDER** — Suspiciously round/identical to many peers, likely copy-pasted
- **OVERCOSTED** — Cost is wildly out of line with peer abilities at same rarity
- **UNDERCOSTED** — Cost is suspiciously low relative to output
- **TIGHT** — Castable but leaves almost nothing in pool (contextual concern, not necessarily wrong)

---

# Warrior

**Base Pool**: 50 stam / 25 mana / 2.0 sr / 1.0 mr  
**At Rare acquisition (~R2 end)**: ~90-110 stam / ~30-40 mana

## Tight Costs (castable but context matters)

| Ability | Rarity | Mana | Stam | Context |
|---------|--------|------|------|--------|
| Limit Break | Rare | 20 | **100** | Rares become common at tier 6 (R3 start). By then Warrior has ~100-120 stam. Castable but drains nearly entire pool. 40s CD = one-cast-per-fight design. Likely intentional. |
| Thorn Garden | Rare | **30** | 70→50 | 30 mana vs ~30-40 pool at rare acquisition. Tight on mana (75-100% of pool). Stamina 50→70 is reachable. Dual-resource strain is the real concern. |
| Lightfall | Rare | **30** | 30 | 30 mana vs ~30-40 pool. Castable by R2-R3 but barely. 12s CD means mana is the binding limiter (30s regen at 1.0 mr). |
| Ironskin | Uncommon | 0 | **50** | 50 stam when uncommons are first received (R1 early, pool ~55-60). This drains 83-91% of pool for just 10→15 perm shields. Very expensive for the effect at time of acquisition. |

## Placeholder Clusters

### "30 stam, 15s CD" pattern (Uncommon)
8 abilities share near-identical costs, suggesting batch placeholder assignment:
| Ability | Stam | CD | Effect |
|---------|------|----|--------|
| Berserker's Call | 30 | 15 | +4 str + 1 berserk |
| Brace II | 30 | 15 | 35→50 shields |
| Pin | 30 | 15 | 130→160 blunt |
| Roar | 30 | 15 | +5→8 str + 1 berserk |
| Spirit of Dragoon | 30 | 15 | Double jump shields |
| War Cry (25 stam) | 25 | 15 | +10→15 str + shields |
| Hold the Line (25 stam) | 25 | 15 | Shields + concussed buff |
| Flurry (25 stam) | 25 | 10 | +attack speed |

**Recommendation**: These abilities have very different power levels and utility — they shouldn't all cost the same. Tune individually.

## Mana on a Stamina Class

Warrior's identity is stamina-dominant (1.0 mr = very slow mana regen). Several uncommon abilities have mana costs that take 10-20s to regen:

| Ability | Mana | Stam | Mana Regen Time |
|---------|------|------|-----------------|
| Charge | 20 | 30→15 | 20s (80% of pool) |
| Atone | 15 | 10 | 15s (60% of pool) |
| Blessed Edge | 15 | 25 | 15s (60% of pool) |
| Earthen Tackle | 15 | 25 | 15s (60% of pool) |
| Pool of Light | 20 | 10 | 20s (80% of pool) |
| Razor Cloak | 15 | 5 | 15s (60% of pool) |

**Question**: Are these mana costs intentional (creating a deliberate secondary resource constraint) or placeholders? Atone's 15 mana on a 5s CD means the CD recovers 6× before the mana does — the mana is the true limiter, not the CD.

## Other Overcosted

| Ability | Rarity | Cost | CD | Issue |
|---------|--------|------|----|-------|
| Bulldoze | Uncommon | 25s | **20s** | Longest CD in uncommon tier for a simple dash-attack. Peers do similar for 15s. |
| Earthen Tackle | Uncommon | 15m+25s | **20s** | Uses both resources AND has longest CD. Break the Line does more for 0m+40s, 15s CD. |

---

# Thief

**Resource Pool**: 45 stam / 30 mana / 1.8 sr / 1.2 mr

## Bug: Cost Increases on Upgrade

| Ability | Rarity | Base Cost | Upgraded Cost | Issue |
|---------|--------|-----------|---------------|-------|
| Assassinate | Rare | 0m + 30s | 0m + **40s** | Stamina INCREASES on upgrade. Every other ability reduces or maintains. Almost certainly a bug. |
| Double Strike | Uncommon | 10s, **2s** CD | 5s, **6s** CD | Cooldown INCREASES from 2s to 6s on upgrade. Stamina drops 10→5, but a 3× CD increase is extreme. Verify this is intentional. |

## Placeholder Cluster: "30 mana" (Uncommon)

8 uncommon abilities all cost exactly 30 mana — which is 100% of thief's base mana pool:

| Ability | Mana | Stam | CD | Effect |
|---------|------|------|----|--------|
| Body Double | 30 | 0 | 15 | Taunt decoy (no dmg) |
| Contaminate | 30 | 30 | 18 | 450 piercing + poison mult |
| Dark Lance | 30 | 0 | 8 | 300→400 dark |
| Dark Pulse | 30 | 0 | 15 | Zone pulse |
| Darkness | 30 | 0 | 12 | Zone dark DoT |
| Disorient | 30 | 0 | 12 | 100→150 dark + vuln |
| Farewell | 30 | 0 | 15 | 60→100/s dark + execute |
| Piercing Venom | 30 | 10 | 12 | 3 basics w/ poison |

**Problem**: Body Double costs the same as Contaminate (which deals 450 damage). Dark Lance and Disorient cost the same despite Dark Lance doing 3× the damage. These clearly weren't individually tuned.

## Extreme Total Costs (Uncommon)

These uncommon abilities consume 60%+ of ALL available resources (45s + 30m = 75 total):

| Ability | Total Cost | % of Total Pool | CD |
|---------|------------|-----------------|-----|
| Night Shade | 25m + 35s = 60 | 80% | 12 |
| Contaminate | 30m + 30s = 60 | 80% | 18 |
| Sidestep | 15m + 35s = 50 | 67% | 15 |
| Escape Plan II | 15m + 25s = 40 | 53% | 15 |
| Flicker | 15m + 25s = 40 | 53% | 15 |
| Preparation | 15m + 25s = 40 | 53% | 25 |

**Question**: Are Night Shade and Contaminate intentionally "dump all resources for one big play" abilities? If so, their costs are fine but should be differentiated from each other (both at exactly 60). If not, 80% of pool is extreme for uncommon — rare territory.

## Overcosted Relative to Peers

| Ability | Rarity | Cost | Peer | Peer Cost | Issue |
|---------|--------|------|------|-----------|-------|
| Escape Plan II | Unc | 15m+25s=40 | Escape Plan (Common) | 10m+10s=20 | Uncommon costs 2× common for less/equal damage |
| Body Double | Unc | 30m | — | — | 30 mana (100% pool) for a 3→5s taunt with no damage |
| Preparation | Unc | 15m+25s=40 | First Strike | 5m+5s=10 | 4× the cost, lower damage, 5s charge requirement, 2.5× the CD |
| Flash Mark | Rare | 30m+10s=40 | Blackspike | 25m+10s=35 | Costs MORE for 70% less damage |

## Undercosted

| Ability | Rarity | Cost | CD | Issue |
|---------|--------|------|----|-------|
| First Strike | Unc | 5m+5s=10 | 10 | Lowest cost uncommon active, highest efficiency, AND resets on stealth/evade |

---

# Archer

**Resource Pool**: 40 stam / 35 mana / 1.6 sr / 1.4 mr

## Overcosted: Near-Pool-Draining Costs (Uncommon)

| Ability | Mana | Stam | Total | % of Pool | CD | Issue |
|---------|------|------|-------|-----------|----|-------|
| Crystallize | **35** | 0 | 35 | 100% mana | **20** | Drains entire mana pool + longest CD in tier |
| Shattering Shot | **30** | 10 | 40 | 86% mana | 8 | Highest total cost for +60→90 bonus damage |
| Hail Cloak | **30** | 5 | 35 | 86% mana | 12 | 86% mana for a channeled (locked-down) ability |
| Brand | **30** | 0 | 30 | 86% mana | 12 | Burn manipulation, not direct damage |
| Sundering Shot | 0 | **30** | 30 | 75% stam | 15 | + 2s charge. Very expensive for a single shot |

**Crystallize** at 35m + 20s CD is especially suspect — it's both the most expensive AND the longest cooldown in the tier. If it's an execute-type ability, the cost + CD double-gates it unnecessarily.

## Placeholder Cluster: "15s CD" (Uncommon)

7 uncommon abilities share 15s CD:
| Ability | Cost | CD |
|---------|------|----|
| Blind | 20m | 15 |
| Chokehold | 25m | 15 |
| Pressure | 25m | 15 |
| Shard Blast | 20m | 15 |
| Sundering Shot | 30s | 15 |
| Zone II | 20m | 15 |
| Demoralize | 25m+5s | 16 |

Less suspicious than Warrior/Thief (15s is a natural tier), but 7 at the same value still suggests some weren't individually set.

## Undercosted

| Ability | Rarity | Cost | CD | Issue |
|---------|--------|------|----|-------|
| Prey Seeker | Unc | 10s | **2** | 2s CD is 4× shorter than the next shortest uncommon (Shoulder Bash 5s). Likely placeholder. |
| Shoulder Bash | Unc | 5s | 5 | Lowest cost + lowest CD for +10→20 damage debuff. Fine if melee risk is intentional. |

---

# Mage

**Resource Pool**: 25 stam / 50 mana / 1.0 sr / 2.0 mr

**Important Context**: Mage has cost-reduction mechanics (Corruption -3→5/stack, Power Overwhelming -20→30, Toll of the Arcane -50→70%, Void Form -15→25 per Rift). Costs above 50 mana may be intentionally designed to require cost reduction before casting. However, stamina costs above 25 have NO reduction path.

## High Stamina Costs

| Ability | Rarity | Mana | Stam | Context |
|---------|--------|------|------|--------|
| Dying Star | Rare | 20 | **50** | Mage stam at rare acquisition: ~35-45. This is 111-143% of their pool — still uncastable without specific stam investment. Even at R3 start (~40-50 stam), it's borderline. Since mage has NO stam reduction mechanic, this is a genuine concern. Players must specifically invest in stamina over other stats to enable this. |

## Placeholder Cluster: "30m + 10s, 12s CD" (Uncommon Fire)

5 fire-archetype uncommon abilities have identical costs:
| Ability | Mana | Stam | CD | Effect |
|---------|------|------|----|--------|
| Fireball II | 30 | 10 | 12 | 240→360 fire + Burn |
| Fireblast | 30 | 10 | 12 | 3×240→360 fan + Corruption |
| Fire Bolt | 30 | 10 | 12 | 200×1→2 fire+lightning |
| To Ashes | 30 | 10 | 12 | 300+scaling fire |
| Torch | 30 | 10 | 12 | 160→240 AoE fire |

**Problem**: These have wildly different power levels (Torch does 160 AoE, Fireblast does up to 1080 multi-target) but identical costs. Clearly batch-assigned.

## Placeholder Cluster: "18s CD" (Uncommon)

5 abilities share 18s CD:
| Ability | Cost | CD | Type |
|---------|------|----|------|
| Anchoring Earth II | 20m+10s | 18 | Earthen |
| Earthen Domain | 30m+15s | 18 | Earthen |
| Earthen Wall | 25m+10s | 18 | Earthen |
| Electrode | 25m | 18 | Lightning |
| Flashfire | 20m+5s | 18 | Fire |

Earthen abilities sharing 18s CD is somewhat justified as an archetype pattern (slow/heavy), but Electrode and Flashfire matching suggests copy-paste.

## Extreme Overcosted (Uncommon)

| Ability | Cost | Total | Issue |
|---------|------|-------|-------|
| Earthen Domain | 30m+15s | 45 | 60% of ALL mage resources for 100→150 earthen + concussed. Earthen Wall costs 25m+10s=35 for 250→300 earthen + barrier + def + concussed. Earthen Domain is strictly worse AND costs more. |
| Avatar State | 0m+**20s** | 20 | 20 stam vs mage's ~28-32 stam at uncommon acquisition = 63-71% of pool. 40s CD (longest in tier). At 1.0 sr, 20s to regen. Expensive for the class at time of receipt. |

## "Once Per Fight" Costs Above Base Pool

These are intentionally gated by cost reduction and/or mana progression. At rare acquisition (~R2 end), mage has ~90-110 mana:
| Ability | Rarity | Cost | vs Base (50m) | vs R2 end (~100m) | Notes |
|---------|--------|------|---------------|-------------------|-------|
| Toll of the Arcane | Epic | 130→100m + 15s | 260→200% | 130→100% | Epic (R2 late+). At 100m pool, upgraded is castable from full. |
| Power Overwhelming | Rare | 100→80m + 25s | 200→160% | 100→80% | Rare. Upgraded castable at R2 end from near-full pool. |
| Power Overwhelming II | Epic | 100→80m + 15s | 200→160% | 100→80% | Same as above. |
| Hearth | Rare | 80→60m | 160→120% | 80→60% | Upgraded is 60% of R2 pool. Reasonable. |
| Voltaics | Rare | 70→50m | 140→100% | 70→50% | Upgraded is very affordable at R2 end. |
| Brightest Flame | Rare | 60→45m + 10s | 120→90% | 60→45% | Upgraded affordable. Base needs near-full pool. |
| Lightning Rod | Rare | 60m + 20s | 120% | 60% | Repeatable (4s CD). Two casts = 120m, needs max pool or reduction. |

**Note**: With progression context, most of these are fine. The only real concern remains **Lightning Rod** being repeatable at 4s CD — two casts back-to-back requires 120 mana which even at R2 end needs full pool. This is likely intentional (you alternate Lightning Rod with cheaper abilities while regen ticks).

## Suspicious Cost Reduction on Upgrade

| Ability | Base → Upgraded Cost | Reduction % | Typical % |
|---------|---------------------|-------------|-----------|
| Mana Cloak | 30→10 mana | **67%** | 25-35% |

67% mana reduction on upgrade is 2× the typical upgrade discount. Either 30 is too high at base or 10 is too low upgraded.

## Storm vs Mana Arc: Identical Drain, 3× Damage Gap

| Toggle | Drain | Damage/tick | Ratio |
|--------|-------|-------------|-------|
| Storm | 6 mana/tick | 20→30 | 3.3→5 dmg/mana |
| Mana Arc | 6→5 mana/s | 60→90 | 10→18 dmg/mana |

If both tick at the same rate, Mana Arc is 3× more efficient. One of these costs is wrong.

---

# Cross-Class Summary

## Most Likely Bugs (verify in code)
| Class | Ability | Issue |
|-------|---------|-------|
| Thief | Assassinate | Stamina cost increases 30→40 on upgrade |
| Thief | Double Strike | Cooldown increases 2→6 on upgrade |

## Largest Placeholder Clusters (most likely batch-assigned)
| Class | Pattern | Count | Abilities |
|-------|---------|-------|-----------|
| Mage | 30m+10s, 12s CD | 5 | Fireball II, Fireblast, Fire Bolt, To Ashes, Torch |
| Thief | 30m exact | 8 | Body Double, Contaminate, Dark Lance, Dark Pulse, Darkness, Disorient, Farewell, Piercing Venom |
| Warrior | ~30s, 15s CD | 8 | Berserker's Call, Brace II, Pin, Roar, Spirit of Dragoon, War Cry, Hold the Line, Flurry |
| Mage | 18s CD | 5 | Anchoring Earth II, Earthen Domain, Earthen Wall, Electrode, Flashfire |

## Top Priority Cost Fixes

| # | Class | Ability | Current Cost | Suggested Action |
|---|-------|---------|-------------|------------------|
| 1 | Thief | Assassinate | 30→40s | **Fix bug**: should be 30→20s or 30→25s |
| 2 | Thief | Double Strike | 2→6s CD | **Verify**: CD tripling on upgrade seems wrong |
| 3 | Thief | Body Double | 30m | **Reduce to 15m** (no-damage utility shouldn't drain pool) |
| 4 | Thief | Escape Plan II | 15m+25s | **Reduce to 10m+10s** (match or beat own common version) |
| 5 | Mage | Earthen Domain | 30m+15s | **Reduce to 20m+10s** (currently worse AND costlier than Earthen Wall) |
| 6 | Archer | Crystallize | 35m, 20s CD | **Reduce to 25m, 15s CD** (drains full base mana + longest CD) |
| 7 | Mage | 5 fire uncommons | All 30m+10s, 12s | **Differentiate**: e.g., Torch 20m+5s, Fireblast 35m+10s, etc. |
| 8 | Thief | 8 "30m" uncommons | All 30m | **Differentiate**: Body Double 15m, Darkness 20m, etc. |
| 9 | Mage | Storm toggle | 6 mana/tick, 20-30 dmg | **Either**: increase to 40→60/tick, or reduce drain to 2-3/tick |
| 10 | Archer | Shattering Shot | 30m+10s | **Reduce to 15m+5s** (40 total for +60-90 bonus is extreme) |
| 11 | Thief | Preparation | 15m+25s, 25s CD | **Reduce to 10m+15s, 15s CD** (currently worst-in-tier by far) |
