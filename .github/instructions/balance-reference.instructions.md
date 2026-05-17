---
applyTo: "**/equipment/**"
description: "Balance reference tables for NeoRogue equipment. Use when: evaluating equipment numbers, comparing costs/damage/cooldowns across classes and rarities, checking upgrade scaling, or tuning numeric values."
---

# NeoRogue Balance Reference (Cross-Class Summary)

For detailed per-class data, see:
- `balance-warrior.instructions.md` — 80 abilities, 36 weapons, 13 accessories
- `balance-thief.instructions.md` — 113 abilities, 26 weapons, 11 accessories
- `balance-archer.instructions.md` — 82 abilities, 38 weapons, 6 accessories
- `balance-mage.instructions.md` — 89 abilities, 34 weapons, 14 accessories

## Class Starting Resources

| Class | Max Stamina | Max Mana | Stam Regen/s | Mana Regen/s | Resource Identity |
|-------|-------------|----------|--------------|--------------|-------------------|
| **Warrior** | 50 | 25 | 2.0 | 1.0 | Stamina-dominant |
| **Thief** | 45 | 30 | 1.8 | 1.2 | Hybrid (stam-leaning) |
| **Archer** | 40 | 35 | 1.6 | 1.4 | Balanced hybrid |
| **Mage** | 25 | 50 | 1.0 | 2.0 | Mana-dominant |

## Equipment Slot Progression

Players start with limited equipment slots and gain more as they progress through regions.

| Slot Type | Base | R1 end | R2 end | R3 end | Source |
|-----------|------|--------|--------|--------|--------|
| Abilities | 4 | 4 | 5 | 6 | Tome of Wisdom (boss drop, +1 per boss) |
| Armor | 1 | 1 | 2 | 3 | Armor Stand (shop artifact, +1 each) |
| Accessories | 2 | 2 | 3 | 4 | Lockbox (shop artifact, +1 each) |

**Implications for cost evaluation:**
- At R1, players have 4 abilities competing for the same resource pool. Total resource demand = sum of all ability costs across a fight.
- By R3 with 6 abilities, the resource pool must support 50% more abilities. Regen progression (~3–4× base by R3) compensates.
- Rare/Epic abilities acquired later compete with 5–6 slots, not 4. Their costs should assume higher total demand but also higher regen.

## Region Structure & Combat Encounters

Standard region: 16 rows. Player traverses one node per row from Start to Boss.

**Row layout** (rowCount=16):
- Row 0: START
- Row 1: ENTRY (60% Fight, 40% Chance)
- Rows 2–6: EARLY (55% Fight, 30% Chance, 10% Miniboss, 2% Shop, 3% Shrine)
- Rows 7–9: MIDDLE (5% Fight, 5% Chance, 53% Miniboss, 12% Shop, 25% Shrine)
- Rows 10–12: LATE (32% Fight, 12% Chance, 15% Miniboss, 15% Shop, 26% Shrine)
- Row 13: EXIT (50% Fight, 40% Chance, 10% Shop)
- Row 14: PRE-BOSS SHRINE (always)
- Row 15: BOSS (always)

**Expected combat encounters per path through one region:**
- Standard Fights: ~5 (from entry, early, late, exit rows)
- Minibosses: ~2–3 (guaranteed ≥1 by verification; middle rows are 53% miniboss)
- Boss: 1
- **Total combat encounters per region: ~8–9**

## Resource Reward Drops

After each combat, the player **chooses one** of three gems (Ruby/Sapphire/Emerald):

| Source | Stamina option | Mana option | Health option |
|--------|---------------|-------------|---------------|
| Standard Fight | Emerald Shard: +5 max stam, +0.2 sr | Sapphire Shard: +5 max mana, +0.2 mr | Ruby Shard: +health |
| Miniboss | Emerald Cluster: +10 max stam, +0.4 sr | Sapphire Cluster: +10 max mana, +0.4 mr | Ruby Cluster: +health |
| Boss | Emerald Gem: +25 max stam, +1.0 sr | Sapphire Gem: +25 max mana, +1.0 mr | Ruby Gem: +health |

**Maximum possible per region (all picks → one resource):** +70 max, +2.8 regen
**Realistic primary-resource investment (with some health picks):** +45–55 max, +2.0–2.4 regen
**Secondary resource (occasional picks):** +5–15 max, +0.2–0.6 regen

## Resource Progression

| Class | Base Stam / sr | ~R1 end | ~R2 end | ~R3 end |
|-------|----------------|---------|---------|---------|
| Warrior | 50 / 2.0 | ~95-105 / 3.8-4.2 | ~140-160 / 5.8-6.4 | ~185-215 / 7.8-8.6 |
| Thief | 45 / 1.8 | ~75-85 / 3.0-3.4 | ~110-130 / 4.4-5.0 | ~145-175 / 5.8-6.6 |
| Archer | 40 / 1.6 | ~65-75 / 2.6-3.0 | ~95-115 / 3.8-4.4 | ~125-155 / 5.0-5.8 |
| Mage | 25 / 1.0 | ~30-40 / 1.2-1.4 | ~35-55 / 1.4-1.8 | ~40-70 / 1.6-2.2 |

| Class | Base Mana / mr | ~R1 end | ~R2 end | ~R3 end |
|-------|----------------|---------|---------|---------|
| Warrior | 25 / 1.0 | ~30-35 / 1.2-1.4 | ~35-45 / 1.4-1.8 | ~40-55 / 1.6-2.2 |
| Thief | 30 / 1.2 | ~45-55 / 1.8-2.2 | ~65-80 / 2.6-3.2 | ~85-105 / 3.4-4.2 |
| Archer | 35 / 1.4 | ~60-70 / 2.4-2.8 | ~90-110 / 3.6-4.2 | ~120-145 / 4.8-5.6 |
| Mage | 50 / 2.0 | ~95-105 / 3.8-4.2 | ~140-160 / 5.8-6.4 | ~185-215 / 7.8-8.6 |

## In-Fight Resource Economy

**Critical: Players start every fight at 0 mana and 0 stamina.** Resources accumulate via regen over the fight duration. The max pool acts as a cap/reservoir, not as a starting budget.

**Target fight durations:**
- Standard fight: 70–100s (average ~85s)
- Miniboss: ~100s
- Boss: ~120s

**Sprint cost:** 4 stamina/s while sprinting (subtracted from stam regen each tick). Effective net stamina regen while sprinting = sr – 4. Estimated ~20% of fight time sprinting.

### Total Resource Budget Per Fight (base stats, 85s fight, 20% sprint)

| Class | Gross Stam (sr×85) | Sprint Tax (0.2×85×4) | **Net Stam for Abilities** | **Total Mana (mr×85)** |
|-------|-------------------|-----------------------|---------------------------|----------------------|
| Warrior | 170 | 68 | **~102** | **85** |
| Thief | 153 | 68 | **~85** | **102** |
| Archer | 136 | 68 | **~68** | **119** |
| Mage | 85 | 68 | **~17** | **170** |

### Time-to-First-Cast (cost ÷ regen rate, from 0)

| Cost | Warrior | Thief | Archer | Mage |
|------|---------|-------|--------|------|
| 10 stam | 5s | 5.6s | 6.3s | 10s |
| 15 stam | 7.5s | 8.3s | 9.4s | 15s |
| 20 stam | 10s | 11.1s | 12.5s | 20s |
| 30 stam | 15s | 16.7s | 18.8s | 30s |
| 50 stam | 25s | 27.8s | 31.3s | 50s |
| 10 mana | 10s | 8.3s | 7.1s | 5s |
| 15 mana | 15s | 12.5s | 10.7s | 7.5s |
| 20 mana | 20s | 16.7s | 14.3s | 10s |
| 30 mana | 30s | 25s | 21.4s | 15s |
| 50 mana | 50s | 41.7s | 35.7s | 25s |

### Number of Casts Per Fight (85s, base regen, ignoring CD)

Formula: `(regen × 85) / cost` — actual casts also limited by CD and pool cap.

| Cost | Warrior stam | Warrior mana | Thief stam | Thief mana | Archer stam | Archer mana | Mage stam | Mage mana |
|------|-------------|-------------|-----------|-----------|------------|------------|----------|----------|
| 10 | 10.2 | 8.5 | 8.5 | 8.5 | 6.8 | 11.9 | 1.7 | 17 |
| 15 | 6.8 | 5.7 | 5.7 | 5.7 | 4.5 | 7.9 | 1.1 | 11.3 |
| 20 | 5.1 | 4.3 | 4.3 | 4.3 | 3.4 | 6.0 | 0.9 | 8.5 |
| 30 | 3.4 | 2.8 | 2.8 | 2.8 | 2.3 | 4.0 | 0.6 | 5.7 |
| 50 | 2.0 | 1.7 | 1.7 | 1.7 | 1.4 | 2.4 | 0.3 | 3.4 |

### Design Principle: Both Resources Should Matter

Even for classes with a dominant resource, the secondary resource should still be used for some ability costs. Guidelines:
- **Warrior mana costs (10–20m)**: Castable 4–8× per fight at base. Creates meaningful gating without being uncastable. Mana becomes the effective cooldown for abilities with short CDs.
- **Mage stamina costs (5–15s)**: Very scarce (~17 net at base). Even 10 stam is a serious cost. Use sparingly to create "sprint vs ability" tension.
- **Hybrid classes**: Can use both resources freely at 15–30 per cost depending on the resource.

**Cost implications for secondary resources:**
- A Warrior with 15m cost on a 5s CD ability: mana (15s to regen) is the real limiter, not the CD. This is intentional design.
- A Mage with 10s cost: forces the mage to stop sprinting to accumulate, creating risk/reward tradeoff.

## Equipment Drop Tiers

Drop tier = `regionsCompleted × 2` + fight score bonuses. Miniboss = base+2, Boss = base+4.

| Tier | Typical Context | Common% | Uncommon% | Rare% | Epic% |
|------|----------------|---------|-----------|-------|-------|
| 0 | R1 first fights | 100% | — | — | — |
| 1 | R1 early (score bonus) | 90% | 10% | — | — |
| 2 | R1 miniboss / R2 start | 50% | 40% | 10% | — |
| 3 | R1 late / R2 early (score) | 9% | 73% | 18% | — |
| 4 | R2 midgame / R2 miniboss | — | 70% | 30% | — |
| 5 | R2 late | — | 50% | 40% | 10% |
| 6 | R3 start / R2 boss | — | 30% | 60% | 10% |
| 7 | R3 mid | — | 10% | 70% | 20% |
| 8 | R3 late / R3 boss | — | — | 60% | 40% |

**When evaluating costs by rarity:**
- **Uncommon**: First appears at tier 1 (10%), becomes dominant at tier 3-4. Players have ~2-4 fights of resource progression when they receive their first uncommon.
- **Rare**: First appears at tier 2 (10%), becomes dominant at tier 6-7. Players have typically completed Region 1 (8 fights) when rares become common.
- **Epic**: First appears at tier 5 (10%), becomes dominant at tier 8. Players are mid-to-late Region 2 minimum.

Costs should be evaluated against the expected pool at the tier where the rarity becomes available/common, NOT against base pools.

## Rarity Tiers

| Tier | Power Multiple vs. Common |
|------|--------------------------|
| COMMON | 1.0× (baseline) |
| UNCOMMON | ~1.5–2.0× |
| RARE | ~2.5–4.0× |
| EPIC | ~4–6× (passive/transformative) |
| LEGENDARY | Build-defining |

---

## Warrior Abilities

### Common

| Equipment | Mana | Stam | CD(s) | Damage | Shields | AoE | Stam Eff | CD-DPS |
|-----------|------|------|-------|--------|---------|-----|----------|--------|
| Empowered Edge | 0 | 15 | 7→5 | 50→70 | 3→4 | - | 3.3→4.7 | 7.1→14 |
| Cleave | 0 | 10 | 5 | 70→100 | - | 90° cone | 7→10 | 14→20 |
| Tackle | 0 | 15 | 20 | 100→130 | - | 2.5 rad | 6.7→8.7 | 5→6.5 |
| Brace | 0 | 15 | 10 | - | 20→30 | - | 1.3→2.0 sh/stam | 2→3 sh/s |
| Battle Cry | 0 | 20 | 15 | +14→20 str (10s) | - | - | 0.7→1.0 str/stam | - |

**Baseline:** 0 mana, 10–20 stamina, 5–20s CD, 50–130 damage, 5–10 dmg/stam

### Uncommon

| Equipment | Mana | Stam | CD(s) | Damage | Shields | Notes |
|-----------|------|------|-------|--------|---------|-------|
| Brace II | 0 | 30 | 15 | - | 35→50 | Pure shields |
| Break the Line | 0 | 40 | 15 | 220 | - | + concussion |
| Burst | 0 | 0 | 25 | - | - | +50→80 stam + 20→30 str (15s) |

**Baseline:** 0 mana, 0–40 stamina, 15–25s CD, 200–220 damage, 35–50 shields

### Rare

| Equipment | Mana | Stam | CD(s) | Damage | Shields | Notes |
|-----------|------|------|-------|--------|---------|-------|
| Mighty Swing | 0 | 25→35 | 10 | 250 | - | Conditional (airborne) |
| Cull | 0 | 50 | 15 | 175→260 | - | Stacking multiplier |
| Fortify | 0 | 30 | 5→7 | 175 | - | + fortitude |
| Fortress | 0 | 0 | 0 | - | 35→50 | Passive recurring |

**Baseline:** 0 mana, 25–80 stamina, 5–40s CD, 150–500 damage

---

## Thief Abilities

### Common

| Equipment | Mana | Stam | CD(s) | Damage | AoE | Mana Eff | Stam Eff | CD-DPS |
|-----------|------|------|-------|--------|-----|----------|----------|--------|
| Shadow Walk | 10 | 20 | 21→17 | 80×3=240 | - | 24 | 12 | 11.4→14.1 |
| Twin Shiv | 0 | 15 | 10 | 80→100 + 50→80 | - | ∞ | 8.7→12 | 13→18 |
| Envenom | 10 | 10 | 12 | 20→30 poison/hit (7s) | - | - | - | - |

**Baseline:** 0–10 mana, 10–20 stamina, 10–21s CD, 130–240 total damage, 8–12 dmg/stam

### Uncommon

| Equipment | Mana | Stam | CD(s) | Damage | AoE | Notes |
|-----------|------|------|-------|--------|-----|-------|
| Acid Bomb | 25 | 0 | 12 | 60→90 poison stacks | 5 rad | AoE DoT |
| Dark Lance | 30 | 0 | 8 | 300→400 | 3 rad | High burst AoE |
| Dodge Roll | 0 | 10→20 | 8→12 | - | - | Stealth + Evade |

**Baseline:** 0–30 mana, 0–20 stamina, 8–12s CD, 300–400 burst

### Rare

| Equipment | Mana | Stam | CD(s) | Damage | Notes |
|-----------|------|------|-------|--------|-------|
| Assassinate | 0 | 30→40 | 12 | 150 | Refund on kill |
| Blackspike | 25 | 10 | 8 | 125→190 (×3) | Cone spread |
| Finale | 0 | 30→40 | 12 | 300 + 40/stam | Scaling burst |

**Baseline:** 0–25 mana, 10–40 stamina, 8–12s CD, 125–500

---

## Archer Abilities

### Common

| Equipment | Mana | Stam | CD(s) | Damage | AoE | Mana Eff | CD-DPS | Notes |
|-----------|------|------|-------|--------|-----|----------|--------|-------|
| Piercing Shot | 15 | 15 | 8 | 35→55 bonus | - | 2.3→3.7 | 4.4→6.9 | Pierces |
| Arrow Rain | 15 | 5 | 12 | 60×3→4=180→240 | 2 rad | 12→16 | 15→20 | Channel |
| Focused Shot | 10 | 10 | 15 | +100%→140% dmg | - | - | - | Multiplier |
| Chill | 15 | 0 | 12 | 6→9 frost stacks | 5 rad | - | - | Status AoE |

**Baseline:** 0–15 mana, 0–15 stamina, 8–15s CD, 35–240 total. Many abilities modify bow shots.

### Uncommon

| Equipment | Mana | Stam | CD(s) | Damage | Notes |
|-----------|------|------|-------|--------|-------|
| Shoulder Bash | 0 | 5 | 5 | 20 + 10→20 debuff | Passive on left-click |
| Bow Trap | 15 | 5 | 8 | 30→45 | Trap-based |
| Chokehold | 25 | 0 | 15 | 30%→50% dmg dealt | Utility (%) |

### Rare

| Equipment | Mana | Stam | CD(s) | Damage | Notes |
|-----------|------|------|-------|--------|-------|
| Hellfire | 0 | 0 | 0 | 65→100 | Passive on fire hits |
| Conflagration | 0 | 0 | 0 | 125 + burn mult (0.8→1.2×) | On-kill chain |
| Blizzard | 0 | 0 | 0 | 50→75 per tick (4 rad) | Frost threshold |

---

## Mage Abilities

### Common

| Equipment | Mana | Stam | CD(s) | Damage | AoE | Mana Eff | CD-DPS | Notes |
|-----------|------|------|-------|--------|-----|----------|--------|-------|
| Mana Blitz | 0 | 0 | 16 | +30→50 magical (8s) | - | ∞ | - | Self-buff |
| Fireball | 15 | 5 | 12 | 160→240 | - | 10.7→16 | 13.3→20 | Projectile, 1s channel |
| Lightning Bolt | 10 | 0 | 7 | 70 + 70→140 | line | 7→21 | 10→30 | Mana threshold bonus |
| Mana Shell | 9 | 0 | 5 | 4→6 shields | - | 0.44→0.67 sh/mana | 0.8→1.2 sh/s | Quick defense |

**Baseline:** 0–15 mana, 0–5 stamina, 5–16s CD, 70–240 damage, 7–16 dmg/mana

### Uncommon

| Equipment | Mana | Stam | CD(s) | Damage | AoE | Notes |
|-----------|------|------|-------|--------|-----|-------|
| Arcane Blast | 25 | 0 | 10 | 80→120 | 4 rad | Ground AoE, channel |
| Density Orb | 20 | 0 | 16 | 3→5 shields + control | 3→5 rad | Utility |

### Rare

| Equipment | Mana | Stam | CD(s) | Damage | Notes |
|-----------|------|------|-------|--------|-------|
| Charge Bolt | 20 | 0 | 8 | 175→250 | + mana CDR |
| Dying Star | 20 | 50 | 30 | 250→375 | Big nuke, high cost |
| Energy Beam | 25 | 10 | 8 | 60→85 | Reduces next ability cost |

---

## Weapons by Rarity

| Rarity | Warrior (Sword) | Thief (Dagger) | Archer (Bow) | Mage (Wand) |
|--------|-----------------|----------------|--------------|-------------|
| Common | 25→35 | 20→25 | 35→45 | 20→30 |
| Uncommon | 45→50 | 25→30 | 50 | 35→45 |
| Rare | 55→175 | 45→55 | 15→60 | 35→55 |
| Epic | 100 | 75→90 | — | 80–100 |

**Scaling (tier to tier):** ~1.4–1.8× per rarity step

---

## Armor Scaling (Damage Reduction)

| Rarity | Physical DR |
|--------|-------------|
| Common | 2–4 |
| Uncommon | 5–7 |
| Rare | ~8–12 |

---

## Upgrade Scaling Patterns

| Metric | Typical Increase |
|--------|-----------------|
| Direct damage | +40–50% |
| Shields granted | +50% |
| Cooldown reduction | -25–30% |
| Status stacks | +50% |
| Cost reduction | -25–50% |
| Weapon damage | +25–50% |
| Buff magnitude | +50–67% |

---

## Cross-Class Efficiency (Common Tier)

| Class | Avg Dmg/Cast | Avg Total Cost | Avg CD(s) | Efficiency (Dmg/Cost) | CD-DPS |
|-------|-------------|----------------|-----------|----------------------|--------|
| Warrior | ~90 | ~15 stam | ~9s | 6.0 | 10 |
| Thief | ~170 | ~25 (10m+15s) | ~14s | 6.8 | 12 |
| Archer | ~140 | ~22 (12m+10s) | ~11s | 6.4 | 13 |
| Mage | ~165 | ~14 (12m+2s) | ~10s | 11.8 | 16.5 |

**Note:** Mage has highest mana efficiency because they have the largest mana pool. Warrior balances via infinite mana efficiency and fast combat uptime. Archer combines ability damage with constant bow DPS baseline.

---

## Resource Progression

Players scale mana/stamina regen throughout a run via shards (post-fight) and clusters (post-miniboss, shops):
- **Uncommon tier:** Players have ~1.5–2× base regen
- **Rare tier:** Players have ~2–3× base regen
- **Epic tier:** Players have ~3×+ base regen

**Important:** Current uncommon+ costs are NOT reliably balanced. Common costs are the ground truth baseline. When evaluating higher-rarity costs, expect them to scale proportionally with progression.

## Rarity Power Budget

| Rarity | Damage (single target) | Shields | Status Stacks | Typical Cost | CD Range |
|--------|------------------------|---------|---------------|--------------|----------|
| Common | 50–160 | 4–30 | 6–30 | 10–20 total | 5–20s |
| Uncommon | 80–400 | 35–50 | 60–90 | 20–40 total* | 8–25s |
| Rare | 125–500 | 35–50 | — | 30–80 total* | 5–40s |
| Epic | 150–200 (passive) | 60+ | — | Often 0 (passive) | — |

*Costs marked with * are current values but not yet balanced. Intended to scale with player regen progression.

---

## Class-Specific Notes

- **Warrior:** Purely stamina. Shield synergy. Strength stacking. Medium CDs.
- **Thief:** Hybrid costs. Reset/refund on kill. Stealth synergy. Poison/DoT. Short CDs.
- **Archer:** Balanced costs. Bow-modification buffs. AoE via arrow systems. Frost/Burn. Many passives at rare+.
- **Mage:** Mana-dominant. 1s channel standard. Conditional bonuses (mana thresholds). Intellect/corruption stacking.
