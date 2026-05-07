# NeoRogue Cost Audit — Placeholder & Unbalanced Costs

Systematic review of mana/stamina/cooldown costs across all classes. Focuses on costs that appear to be forgotten placeholders, increase on upgrade (bugs), or form suspicious identical-cost clusters.

Common ability costs are treated as ground truth. Uncommon+ costs are the focus.

---

## Resource Economy Context

### Region Structure
Standard region: 16 rows, ~5 fights + ~2–3 minibosses + 1 boss per path.

### Resource Rewards (player chooses ONE per combat)
| Source | Stamina (Emerald) | Mana (Sapphire) | Health (Ruby) |
|--------|-------------------|-----------------|---------------|
| Fight | +5 max, +0.2 sr | +5 max, +0.2 mr | +health |
| Miniboss | +10 max, +0.4 sr | +10 max, +0.4 mr | +health |
| Boss | +25 max, +1.0 sr | +25 max, +1.0 mr | +health |

Realistic primary resource gain per region: +45–55 max, +2.0–2.4 regen.
Secondary resource gain (occasional picks): +5–15 max, +0.2–0.6 regen.

### In-Fight Resource Economy (CRITICAL)
**Players start every fight at 0 mana and 0 stamina.** The max pool is a cap, not a starting budget. Total resource available = regen × fight duration.

**Target fight durations:** Standard 70–100s (avg ~85s), Miniboss ~100s, Boss ~120s.
**Sprint cost:** 4/s while sprinting (~20% of fight time) → net stamina loss of ~68 over a fight.

### Total Resource Budget Per Fight (85s, base stats)

| Class | Net Stam Budget | Mana Budget | Secondary Resource |
|-------|----------------|-------------|-------------------|
| Warrior | ~102 | **85** | Mana: 85 total, enough for 4–5 casts of 15–20m abilities |
| Thief | ~85 | **102** | Both resources well-supplied |
| Archer | ~68 | **119** | Both resources well-supplied |
| Mage | ~17 | **170** | Stamina: very scarce, 10–15s costs are meaningful |

### Regen Progression (by region end)

| Class | R1 end sr/mr | R2 end sr/mr | Net Stam Budget R2 (85s) | Mana Budget R2 (85s) |
|-------|-------------|-------------|-------------------------|---------------------|
| Warrior | 3.8–4.2 / 1.2–1.4 | 5.8–6.4 / 1.4–1.8 | ~425–476 | ~119–153 |
| Thief | 3.0–3.4 / 1.8–2.2 | 4.4–5.0 / 2.6–3.2 | ~306–357 | ~221–272 |
| Archer | 2.6–3.0 / 2.4–2.8 | 3.8–4.4 / 3.6–4.2 | ~255–306 | ~306–357 |
| Mage | 1.2–1.4 / 3.8–4.2 | 1.4–1.8 / 5.8–6.4 | ~51–85 | ~493–544 |

### Design Principle: Both Resources Should Be Relevant
Mana costs on stamina-dominant classes and stamina costs on mana-dominant classes are **intentional**. A 15m cost on a Warrior ability (1.0 mr base) creates a 15s effective cooldown via regen gating — this is a valid design tool. A 10s cost on a Mage ability forces sprint-vs-ability tradeoffs. Avoid making all costs just one resource; whenever thematically reasonable, use both.

### Equipment Slot Progression
Players gain ability slots over the run: 4 base → 5 at R2 → 6 at R3 (via Tome of Wisdom boss drop). Armor and accessory slots also increase by ~1 per region (via shop artifacts). With 4–6 abilities sharing the same resource pool, total per-fight resource demand scales with slot count. Regen progression (~3–4× base by R3) compensates for the increased demand.

## Key: Issue Types
- **BUG?** — Cost increases on upgrade (opposite of expected)
- **PLACEHOLDER** — Suspiciously round/identical to many peers, likely copy-pasted
- **OVERCOSTED** — Cost is wildly out of line with peer abilities at same rarity
- **UNDERCOSTED** — Cost is suspiciously low relative to output
- **TIGHT** — Castable but leaves almost nothing in pool (contextual concern, not necessarily wrong)

---

# Warrior

**Base Pool**: 50 stam / 25 mana / 2.0 sr / 1.0 mr
**Fight budget (base, 85s):** ~102 net stam / 85 mana
**At Rare acquisition (~R2 end):** ~140-160 stam, 5.8-6.4 sr / ~35-45 mana, 1.4-1.8 mr

## Remaining Issues

| Ability | Rarity | Cost | CD | Recommendation |
|---------|--------|------|----|---------------|
| Bulldoze | Uncommon | 25s | **20s** | Reduce CD to 15s. Or add 10m + reduce to 20s stam + 12s CD. |
| Earthen Tackle | Uncommon | 15m+25s | **20s** | Reduce CD to 15s (keep dual resource as intended). |
| Ironskin | Uncommon | 0m+50s | 20s | Reduce to 35s + 10m, 18s CD. 50s at base 2.0sr = 25s wait for just 10→15 perm shields. |

---

# Thief

**Base Pool:** 45 stam / 30 mana / 1.8 sr / 1.2 mr
**Fight budget (base, 85s):** ~85 net stam / 102 mana

## Bug: Cost Increases on Upgrade

| Ability | Rarity | Base Cost | Upgraded Cost | Issue |
|---------|--------|-----------|---------------|-------|
| Assassinate | Rare | 0m + 30s | 0m + **40s** | Stamina INCREASES on upgrade. **BUG.** Should be 30→20s or 30→25s. |
| Double Strike | Uncommon | 10s, **2s** CD | 5s, **6s** CD | CD INCREASES 2→6. **Verify** — likely should be 2→1.5s or kept at 2s. |

## Remaining Overcosted

| Ability | Rarity | Cost | Recommendation |
|---------|--------|------|---------------|
| Escape Plan II | Unc | 15m+25s, 15s CD | **Reduce to 15m+15s, 12s CD** — Common version costs 10m+10s; uncommon should improve. |
| Preparation | Unc | 15m+25s, 25s CD | **Reduce to 10m+15s, 15s CD** — Worst-in-tier by efficiency (high cost + long CD + charge req). |
| Flash Mark | Rare | 30m+10s | **Reduce to 20m+10s** — 70% less damage than peer Blackspike for same cost. |

## Undercosted

| Ability | Rarity | Cost | CD | Issue |
|---------|--------|------|----|-------|
| First Strike | Unc | 5m+5s=10 | 10 | Lowest cost uncommon active, highest efficiency, AND resets on stealth/evade. Consider 10m+5s. |

---

# Archer

**Base Pool:** 40 stam / 35 mana / 1.6 sr / 1.4 mr
**Fight budget (base, 85s):** ~68 net stam / 119 mana

## Overcosted: Near-Pool-Draining Costs (Uncommon) — REVISED

With fight budget context, mana costs are less concerning than previously flagged:

| Ability | Mana | Stam | Revised Assessment | Recommendation |
|---------|------|------|-------------------|---------------|
| Crystallize | **35** | 0 | First cast at 25s. 2 casts per fight. Still the MOST expensive + longest CD (20s) — double-gated. | **Reduce to 25m + 5s, 15s CD** |
| Shattering Shot | **30** | 10 | 40 total. At 119+68=187 fight budget, this is 21%. **Less concerning now.** | **Reduce to 20m+10s** (still meaningful) |
| Hail Cloak | **30** | 5 | 35 total for channeled. Reasonable for a strong channeled ability. | **FINE as-is** |
| Brand | **30** | 0 | Pure mana, 30/119 = 25% of mana budget. Reasonable for burn manipulation. | **FINE as-is** (or 25m+5s for resource diversity) |
| Sundering Shot | 0 | **30** | 30/68 = 44% of net stam. + 2s charge. | **Reduce to 20s + 10m, 12s CD** (add mana, reduce stam) |

## Placeholder Cluster: "15s CD" (Uncommon) — Recommendations

| Ability | Current Cost | Recommended |
|---------|-------------|-------------|
| Blind | 20m, 15s | 15m + 5s, 12s CD |
| Chokehold | 25m, 15s | 20m + 10s, 15s CD |
| Pressure | 25m, 15s | 25m + 5s, 12s CD |
| Shard Blast | 20m, 15s | 20m + 5s, 12s CD |
| Sundering Shot | 30s, 15s | 20s + 10m, 12s CD |
| Zone II | 20m, 15s | 15m + 10s, 15s CD |
| Demoralize | 25m+5s, 16s | 20m + 10s, 15s CD |

**Principle applied:** Add minor stamina costs to previously pure-mana abilities for resource diversity.

## Undercosted

| Ability | Rarity | Cost | CD | Issue |
|---------|--------|------|----|-------|
| Prey Seeker | Unc | 10s | **2** | 2s CD is 4× shorter than next shortest. Likely placeholder. Recommend 5s CD. |
| Shoulder Bash | Unc | 5s | 5 | Lowest cost for +10→20 damage debuff. Fine if melee-risk is intentional. |

---

# Mage

**Base Pool:** 25 stam / 50 mana / 1.0 sr / 2.0 mr
**Fight budget (base, 85s):** ~17 net stam / 170 mana
**At R2:** ~51-85 net stam / ~493-544 mana

**Important Context**: Mage has cost-reduction mechanics (Corruption -3→5/stack, Power Overwhelming -20→30, Toll of the Arcane -50→70%, Void Form -15→25 per Rift). **Mage stamina has NO reduction path** — every stamina point is precious.

## High Stamina Costs — REVISED

| Ability | Rarity | Mana | Stam | Revised Assessment |
|---------|--------|------|------|-------------------|
| Dying Star | Rare | 20 | **50** | At R2 mage net stam ~51-85. This is 59-98% of fight stam budget. Player cannot sprint AND cast this. **Intentional "sacrifice all mobility" design** if once-per-fight. Recommend noting as "mobility sacrifice" not reducing. Alternatively: 30m + 30s to distribute load. |
| Avatar State | Unc | 0 | **20** | At base net stam ~17. **UNCASTABLE** without stam investment OR not sprinting at all. By R1 end (~30 net), barely castable. Recommend 10m + 10s to share the load. |

## Placeholder Cluster: "30m + 10s, 12s CD" (Uncommon Fire) — Recommendations

| Ability | Current | Recommended | Rationale |
|---------|---------|-------------|-----------|
| Torch | 30m+10s, 12s | **20m + 5s, 10s CD** | Lowest damage in cluster (160–240 AoE). Cheapest. |
| Fireball II | 30m+10s, 12s | **25m + 10s, 12s CD** | Standard single-target + burn. Middle cost. |
| To Ashes | 30m+10s, 12s | **30m + 10s, 12s CD** | Scaling damage. Keep as-is (already appropriate). |
| Fire Bolt | 30m+10s, 12s | **25m + 10s, 10s CD** | Multi-hit but requires two types. Moderate. |
| Fireblast | 30m+10s, 12s | **35m + 10s, 15s CD** | Highest damage (3×240–360 fan). Most expensive + longest CD. |

## Placeholder Cluster: "18s CD" (Uncommon) — Recommendations

| Ability | Current Cost | Recommended |
|---------|-------------|-------------|
| Anchoring Earth II | 20m+10s, 18s | 20m+10s, 15s CD |
| Earthen Domain | 30m+15s, 18s | 20m+10s, 15s CD (see below) |
| Earthen Wall | 25m+10s, 18s | 25m+10s, 15s CD |
| Electrode | 25m, 18s | 25m+5s, 12s CD |
| Flashfire | 20m+5s, 18s | 20m+5s, 12s CD |

## Extreme Overcosted (Uncommon) — REVISED

| Ability | Current | Recommendation | Rationale |
|---------|---------|---------------|-----------|
| Earthen Domain | 30m+15s=45, 18s CD | **20m+10s, 15s CD** | Currently worse AND costlier than Earthen Wall (25m+10s for more damage+shields). Must be cheaper. |
| Avatar State | 0m+20s, 40s CD | **10m+10s, 35s CD** | 20 stam is functionally uncastable for base mage. Split cost across resources. |

## "Once Per Fight" Costs Above Base Pool (REVISED — mostly fine)

With fight-duration context and progression:
| Ability | Rarity | Cost | Budget at acquisition | Assessment |
|---------|--------|------|----------------------|-----------|
| Toll of the Arcane | Epic | 130→100m + 15s | R2+ mana budget: ~493-544 | **FINE.** Well within budget. Pool cap is the only gate. |
| Power Overwhelming | Rare | 100→80m + 25s | R2 mana budget: ~493+ | **FINE.** Needs full pool but regen refills fast at 5.8+ mr. |
| Hearth | Rare | 80→60m | R2 mana budget: ~493+ | **FINE.** Very affordable at acquisition time. |
| Voltaics | Rare | 70→50m | R2 mana budget: ~493+ | **FINE.** |
| Lightning Rod | Rare | 60m + 20s, 4s CD | R2 mana budget: ~493+ | **FINE.** Can spam if pool supports it. Regen-gated is good. |

**All previously flagged "once per fight" abilities are fine with fight-duration budgeting.** The concern was comparing to base pool (50m) which is irrelevant since players DON'T start with a full pool — they start at 0 and regen into it.

## Suspicious Cost Reduction on Upgrade

| Ability | Base → Upgraded Cost | Reduction % | Typical % |
|---------|---------------------|-------------|-----------|
| Mana Cloak | 30→10 mana | **67%** | 25-35% |

67% mana reduction on upgrade is 2× the typical upgrade discount. Either 30 is too high at base or 10 is too low upgraded. Recommend: 25→15 mana.

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

## Remaining Placeholder Clusters
| Class | Pattern | Count | Abilities |
|-------|---------|-------|-----------|
| Mage | 30m+10s, 12s CD | 5 | Fireball II, Fireblast, Fire Bolt, To Ashes, Torch |
| Mage | 18s CD | 5 | Anchoring Earth II, Earthen Domain, Earthen Wall, Electrode, Flashfire |

## Top Priority Cost Fixes

| # | Class | Ability | Current Cost | Recommended Cost | Rationale |
|---|-------|---------|-------------|-----------------|-----------|
| 1 | Thief | Assassinate | 30→**40s** | **30→20s** | BUG: cost increases on upgrade |
| 2 | Thief | Double Strike | 2→**6s** CD | **2→1.5s** CD | BUG: CD triples on upgrade |
| 3 | Mage | Avatar State | 0m+20s, 40s CD | **10m+10s, 35s CD** | 20s is uncastable for base mage; split across resources |
| 4 | Mage | Earthen Domain | 30m+15s, 18s CD | **20m+10s, 15s CD** | Strictly worse AND costlier than Earthen Wall |
| 5 | Thief | Preparation | 15m+25s, 25s CD | **10m+15s, 15s CD** | Worst-in-tier by efficiency (high cost + long CD + charge req) |
| 6 | Thief | Escape Plan II | 15m+25s, 15s CD | **15m+15s, 12s CD** | Common version costs 10m+10s; uncommon should improve |
| 7 | Thief | Flash Mark | 30m+10s | **20m+10s** | 70% less damage than peer Blackspike for same cost |
| 8 | Archer | Crystallize | 35m, 20s CD | **25m+5s, 15s CD** | Double-gated (most expensive + longest CD) |
| 9 | Warrior | Ironskin | 0m+50s, 20s CD | **10m+35s, 18s CD** | 50s at base 2.0sr = 25s wait; add mana, reduce stam |
| 10 | Archer | Sundering Shot | 30s, 15s CD | **20s+10m, 12s CD** | 75% stam + 2s charge; add mana for diversity |
| 11 | Warrior | Bulldoze | 25s, 20s CD | **20s+10m, 12s CD** or **25s, 15s CD** | Overlong CD for its effect |
| 12 | Warrior | Earthen Tackle | 15m+25s, 20s CD | **15m+25s, 15s CD** | Longest CD in tier |
| 13 | Mage | 5 fire uncommons | All 30m+10s, 12s | See per-ability table above | Differentiate by damage output |
| 14 | Mage | 5 "18s CD" uncommons | All 18s CD | See per-ability table above | Differentiate by role/power |
| 15 | Mage | Mana Cloak | 30→10m | **25→15m** | 67% cost reduction on upgrade (2× typical) |
| 16 | Thief | First Strike | 5m+5s, 10s CD | **10m+5s, 10s CD** | Undercosted: lowest cost + highest efficiency + resets on stealth |

## Design Principles Applied

1. **Start at 0**: All resource evaluation uses total-generated-over-fight-duration, not pool-as-starting-budget.
2. **Both resources matter**: Recommendations add secondary resource costs where previously all-one-resource. This creates meaningful variety in resource pressure.
3. **Sprint tax**: Stamina costs on ranged classes (Mage, Archer) are more expensive than they appear due to sprint competition.
4. **Regen-gating is valid design**: A 15m cost on a 5s CD Warrior ability is intentional — the mana makes it effectively a 15s CD. This should not be "fixed."
5. **Differentiate within clusters**: Abilities with different power levels must have different costs, even when they share a theme or archetype.
