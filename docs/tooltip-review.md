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

### Phase 5: Raw tags → `DescUtil` helpers (all non-ability equipment)
All non-ability equipment files converted. Categories completed:
- **Accessories** (~16 files): CobraCrest, FlightRing, GripGloves, LifeThief, MajorStaminaRelic, MinorManaRelic, MinorPoisonRelic, MinorStaminaRelic, MinorShieldingRelic, MinorStrengthRelic, RingOfNature, RingOfSharpness, TopazRing, RingOfTheEagle, TarotCard, VoidBracelet, YellowRing, DiscountCard
- **Weapons** (~30 files): AshenHeadhunter, ButterflyKnife, ButterflyKnife2, ChainLightningWand, CrimsonBlade, DarkBolt2, DrainWand, ElectromagneticKnife, EnergizedRazor, EtherealKnife, EvasiveKnife, Irritant, LightLeatherGauntlets, LightningWand, SerratedRazor, ShadowyDagger, StoneShiv, StoneSword, BarbedRocket, BoltWand, BurningSun, Condemn, EnfeeblingWand, PhantasmalKiller, TheGreatDivide, FrostbiteBow, Equalizer, RapidFire, BasicShotbow, CrescentAxe, CripplingFencingSword, DaedalusStormbow, DarkBolt, DarkTorrent, EdgeOfHorizon, FencingSword, Groundbreaker, GrowingSpark, Harpoon, HiddenRazor, HibernianQuickblade, IronSword, IronThrowingKnife, MirrorSickle, MechanicalBow, MultiCrossbow, OldStaff, Rapier, Razor, RighteousLance, Snareweaver, SparkStick, StickyBomb, StoneMace, TacticiansDagger, Volley, WarningShot
- **Armor** (~25 files): AuricCape, BootsOfSpeed, Brightcrown, BurningMantle, ClothBindings, ElbowBrace, EnchantedCloak, Footpads, Gauze, IcyArmguard, IcySigil, LeatherArmguard, LeatherChestplate, LeatherCowl, LeatherHelmet, LeatherHood, NullMagicMantle, ElectrostaticVest, HuntersVest, LightningCloak, SilversilkCowl, SpikedPauldrons, StonyCloak, NullifyingCloak, SaviorsHelm
- **Offhands** (~17 files): BatteringRam, EnduranceShield, HastyShield, IronMaiden, LeadingKnife, LeatherBracer, PaladinsShield, PocketWatch, SmallShield, VeiledHourglass, WristBlade, PoisonPowder, HavenTome, RubyArmament, TomeOfGravity, SpikyShield, VengefulShield, TomeOfScorchedEarth
- **Consumables** (~11 files): MinorHealthPotion, MinorMagicalPotion, MinorManaPotion, MinorPhysicalPotion, MinorShieldsPotion, MinorStaminaPotion, SeraphsPotion, AlchemistsPotion, ForcePotion, MirrorPotion, AegisPotion
- **Artifacts** (~55 files): AlchemistBag, Anxiety, AurorBadge, AvalonianAnchor, BagOfPreparation, BloodyTrinket, Brightfeather, BurningCross, CharmOfGallus, CrackedCrystal, CrystallineFlask, DarkArtsTreatise, EarthenTome, EmeraldCluster, EmeraldGem, EmeraldShard, Enderchest, EnergyBattery, Exhaustion, FaerieDust, FaeriePendant, ForgemastersMark, GiantSlayer, GlacialHammer, GoldenVeil, GoldIngot, GrendelsCrystalMirror, HallowedEmbers, HermesBoots, HiddenBlade, HolyScriptures, HuntersCompass, InfernalTome, ManaHaze, ManaflowBand, MercenaryHeadband, MiasmaInABottle, NoxianBlight, NoxianSkull, OmniGem, PracticeDummy, Pumped, RodOfAges, RubyCluster, RubyGem, RubyShard, SapphireCluster, SapphireGem, SapphireShard, ScrollOfFrost, StarlightVeil, StaticNecklace, TempestSigil, TomeOfWisdom, TreatiseOnElectricity, ArmorStand, Lockbox

**Convention**: `[<white>Xs</white>]` bracket durations left as-is across all files.

---

## Notable Edge Cases

- **DaedalusHammer.java**: Uses `<yellow>` in a chat message (`broadcastOthers`), not a tooltip. Intentionally skipped.
- **HastyShield.java**: Has `"5 second cooldown."` — plain text duration that should use `DescUtil.white("5s")`.
- **Gauze.java (armor)**: Has `<white>2</white> seconds` — mixed pattern.
- **PoisonPowder.java**: Has `<white>5</white> seconds` — mixed pattern.
