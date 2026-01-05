package me.neoblade298.neorogue.equipment;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.droptables.DropTable;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.abilities.*;
import me.neoblade298.neorogue.equipment.accessories.ArmorStand;
import me.neoblade298.neorogue.equipment.accessories.AshenWreath;
import me.neoblade298.neorogue.equipment.accessories.BurningRing;
import me.neoblade298.neorogue.equipment.accessories.CailiricCrystal;
import me.neoblade298.neorogue.equipment.accessories.CeruleanBracelet;
import me.neoblade298.neorogue.equipment.accessories.DaedalusHammer;
import me.neoblade298.neorogue.equipment.accessories.DiscountCard;
import me.neoblade298.neorogue.equipment.accessories.EagleFeather;
import me.neoblade298.neorogue.equipment.accessories.EarthenBracer;
import me.neoblade298.neorogue.equipment.accessories.EarthenRing;
import me.neoblade298.neorogue.equipment.accessories.FlightRing;
import me.neoblade298.neorogue.equipment.accessories.FlintPendant;
import me.neoblade298.neorogue.equipment.accessories.ForceTrinket;
import me.neoblade298.neorogue.equipment.accessories.GripGloves;
import me.neoblade298.neorogue.equipment.accessories.LifeThief;
import me.neoblade298.neorogue.equipment.accessories.LionheartBangle;
import me.neoblade298.neorogue.equipment.accessories.Lockbox;
import me.neoblade298.neorogue.equipment.accessories.MagicQuiver;
import me.neoblade298.neorogue.equipment.accessories.MajorShieldingRelic;
import me.neoblade298.neorogue.equipment.accessories.MajorStaminaRelic;
import me.neoblade298.neorogue.equipment.accessories.ManaMagnifier;
import me.neoblade298.neorogue.equipment.accessories.MerchantSpyglass;
import me.neoblade298.neorogue.equipment.accessories.MinorManaRelic;
import me.neoblade298.neorogue.equipment.accessories.MinorPoisonRelic;
import me.neoblade298.neorogue.equipment.accessories.MinorShieldingRelic;
import me.neoblade298.neorogue.equipment.accessories.MinorStaminaRelic;
import me.neoblade298.neorogue.equipment.accessories.MinorStrengthRelic;
import me.neoblade298.neorogue.equipment.accessories.Plaguebearer;
import me.neoblade298.neorogue.equipment.accessories.PotOfGreed;
import me.neoblade298.neorogue.equipment.accessories.QuartzPrism;
import me.neoblade298.neorogue.equipment.accessories.RedRing;
import me.neoblade298.neorogue.equipment.accessories.RighteousRing;
import me.neoblade298.neorogue.equipment.accessories.RingOfAnger;
import me.neoblade298.neorogue.equipment.accessories.RingOfFortitude;
import me.neoblade298.neorogue.equipment.accessories.RingOfLight;
import me.neoblade298.neorogue.equipment.accessories.RingOfManaflow;
import me.neoblade298.neorogue.equipment.accessories.RingOfMentalism;
import me.neoblade298.neorogue.equipment.accessories.RingOfNature;
import me.neoblade298.neorogue.equipment.accessories.RingOfScalding;
import me.neoblade298.neorogue.equipment.accessories.RingOfSharpness;
import me.neoblade298.neorogue.equipment.accessories.SaboteursRing;
import me.neoblade298.neorogue.equipment.accessories.ShellTrinket;
import me.neoblade298.neorogue.equipment.accessories.SigilOfTheIronLegion;
import me.neoblade298.neorogue.equipment.accessories.SpiritShard;
import me.neoblade298.neorogue.equipment.accessories.TarotCard;
import me.neoblade298.neorogue.equipment.accessories.TopazRing;
import me.neoblade298.neorogue.equipment.accessories.VermillionBelt;
import me.neoblade298.neorogue.equipment.accessories.VoidBracelet;
import me.neoblade298.neorogue.equipment.accessories.WarpedAnvil;
import me.neoblade298.neorogue.equipment.accessories.YellowRing;
import me.neoblade298.neorogue.equipment.armor.AdaptiveChemvest;
import me.neoblade298.neorogue.equipment.armor.ArcheryGlove;
import me.neoblade298.neorogue.equipment.armor.AuricCape;
import me.neoblade298.neorogue.equipment.armor.BlindingCloak;
import me.neoblade298.neorogue.equipment.armor.BootsOfSpeed;
import me.neoblade298.neorogue.equipment.armor.Brightcrown;
import me.neoblade298.neorogue.equipment.armor.BurningMantle;
import me.neoblade298.neorogue.equipment.armor.CalmingHood;
import me.neoblade298.neorogue.equipment.armor.ClothBindings;
import me.neoblade298.neorogue.equipment.armor.ElbowBrace;
import me.neoblade298.neorogue.equipment.armor.ElectrostaticVest;
import me.neoblade298.neorogue.equipment.armor.EnchantedCloak;
import me.neoblade298.neorogue.equipment.armor.EngineersCap;
import me.neoblade298.neorogue.equipment.armor.EtherVeil;
import me.neoblade298.neorogue.equipment.armor.Footpads;
import me.neoblade298.neorogue.equipment.armor.Gauze;
import me.neoblade298.neorogue.equipment.armor.HuntersVest;
import me.neoblade298.neorogue.equipment.armor.IcyArmguard;
import me.neoblade298.neorogue.equipment.armor.IcySigil;
import me.neoblade298.neorogue.equipment.armor.IronCuirass;
import me.neoblade298.neorogue.equipment.armor.LeatherArmguard;
import me.neoblade298.neorogue.equipment.armor.LeatherChestplate;
import me.neoblade298.neorogue.equipment.armor.LeatherCowl;
import me.neoblade298.neorogue.equipment.armor.LeatherHelmet;
import me.neoblade298.neorogue.equipment.armor.LeatherHood;
import me.neoblade298.neorogue.equipment.armor.LightningCloak;
import me.neoblade298.neorogue.equipment.armor.MagiciansHood;
import me.neoblade298.neorogue.equipment.armor.NullMagicMantle;
import me.neoblade298.neorogue.equipment.armor.NullifyingCloak;
import me.neoblade298.neorogue.equipment.armor.PhoenixfireMantle;
import me.neoblade298.neorogue.equipment.armor.RedCloak;
import me.neoblade298.neorogue.equipment.armor.SaviorsHelm;
import me.neoblade298.neorogue.equipment.armor.SilversilkCowl;
import me.neoblade298.neorogue.equipment.armor.SpikedPauldrons;
import me.neoblade298.neorogue.equipment.armor.StarlightHood;
import me.neoblade298.neorogue.equipment.armor.StonyCloak;
import me.neoblade298.neorogue.equipment.artifacts.AlchemistBag;
import me.neoblade298.neorogue.equipment.artifacts.AmuletOfOffering;
import me.neoblade298.neorogue.equipment.artifacts.Anxiety;
import me.neoblade298.neorogue.equipment.artifacts.AthenianChalice;
import me.neoblade298.neorogue.equipment.artifacts.AurorBadge;
import me.neoblade298.neorogue.equipment.artifacts.AvalonianAnchor;
import me.neoblade298.neorogue.equipment.artifacts.AzureCutter;
import me.neoblade298.neorogue.equipment.artifacts.BagOfPreparation;
import me.neoblade298.neorogue.equipment.artifacts.BloodyTrinket;
import me.neoblade298.neorogue.equipment.artifacts.Bramblevine;
import me.neoblade298.neorogue.equipment.artifacts.Brightfeather;
import me.neoblade298.neorogue.equipment.artifacts.BurningCross;
import me.neoblade298.neorogue.equipment.artifacts.CharmOfGallus;
import me.neoblade298.neorogue.equipment.artifacts.ConcealingCloak;
import me.neoblade298.neorogue.equipment.artifacts.CrackedCrystal;
import me.neoblade298.neorogue.equipment.artifacts.CrossOfAntiquan;
import me.neoblade298.neorogue.equipment.artifacts.CrystalFeather;
import me.neoblade298.neorogue.equipment.artifacts.CrystallineFlask;
import me.neoblade298.neorogue.equipment.artifacts.DarkArtsTreatise;
import me.neoblade298.neorogue.equipment.artifacts.EarthenTome;
import me.neoblade298.neorogue.equipment.artifacts.EmeraldCluster;
import me.neoblade298.neorogue.equipment.artifacts.EmeraldGem;
import me.neoblade298.neorogue.equipment.artifacts.EmeraldShard;
import me.neoblade298.neorogue.equipment.artifacts.EnergyBattery;
import me.neoblade298.neorogue.equipment.artifacts.Exhaustion;
import me.neoblade298.neorogue.equipment.artifacts.FaerieDust;
import me.neoblade298.neorogue.equipment.artifacts.FaeriePendant;
import me.neoblade298.neorogue.equipment.artifacts.GlacialHammer;
import me.neoblade298.neorogue.equipment.artifacts.GoldIngot;
import me.neoblade298.neorogue.equipment.artifacts.GoldenVeil;
import me.neoblade298.neorogue.equipment.artifacts.GrendelsCrystalMirror;
import me.neoblade298.neorogue.equipment.artifacts.GrendelsFavoriteTome;
import me.neoblade298.neorogue.equipment.artifacts.HermesBoots;
import me.neoblade298.neorogue.equipment.artifacts.HiddenBlade;
import me.neoblade298.neorogue.equipment.artifacts.HolyScriptures;
import me.neoblade298.neorogue.equipment.artifacts.HuntersCompass;
import me.neoblade298.neorogue.equipment.artifacts.InfernalTome;
import me.neoblade298.neorogue.equipment.artifacts.ManaHaze;
import me.neoblade298.neorogue.equipment.artifacts.ManaflowBand;
import me.neoblade298.neorogue.equipment.artifacts.MercenaryHeadband;
import me.neoblade298.neorogue.equipment.artifacts.MiasmaInABottle;
import me.neoblade298.neorogue.equipment.artifacts.MistralVeil;
import me.neoblade298.neorogue.equipment.artifacts.NoxianBlight;
import me.neoblade298.neorogue.equipment.artifacts.NoxianSkull;
import me.neoblade298.neorogue.equipment.artifacts.ObsidianCharm;
import me.neoblade298.neorogue.equipment.artifacts.OmniGem;
import me.neoblade298.neorogue.equipment.artifacts.OpalHourglass;
import me.neoblade298.neorogue.equipment.artifacts.OpalNecklace;
import me.neoblade298.neorogue.equipment.artifacts.PracticeDummy;
import me.neoblade298.neorogue.equipment.artifacts.Pumped;
import me.neoblade298.neorogue.equipment.artifacts.RodOfAges;
import me.neoblade298.neorogue.equipment.artifacts.RubyCluster;
import me.neoblade298.neorogue.equipment.artifacts.RubyGem;
import me.neoblade298.neorogue.equipment.artifacts.RubyShard;
import me.neoblade298.neorogue.equipment.artifacts.SapphireCluster;
import me.neoblade298.neorogue.equipment.artifacts.SapphireGem;
import me.neoblade298.neorogue.equipment.artifacts.SapphireShard;
import me.neoblade298.neorogue.equipment.artifacts.ScrollOfFrost;
import me.neoblade298.neorogue.equipment.artifacts.StarlightVeil;
import me.neoblade298.neorogue.equipment.artifacts.StaticNecklace;
import me.neoblade298.neorogue.equipment.artifacts.StormSigil;
import me.neoblade298.neorogue.equipment.artifacts.TomeOfWisdom;
import me.neoblade298.neorogue.equipment.artifacts.TreatiseOnElectricity;
import me.neoblade298.neorogue.equipment.artifacts.TrickstersSigil;
import me.neoblade298.neorogue.equipment.consumables.MinorFirePotion;
import me.neoblade298.neorogue.equipment.consumables.MinorHealthPotion;
import me.neoblade298.neorogue.equipment.consumables.MinorMagicalPotion;
import me.neoblade298.neorogue.equipment.consumables.MinorManaPotion;
import me.neoblade298.neorogue.equipment.consumables.MinorPhysicalPotion;
import me.neoblade298.neorogue.equipment.consumables.MinorShieldsPotion;
import me.neoblade298.neorogue.equipment.consumables.MinorStaminaPotion;
import me.neoblade298.neorogue.equipment.cursed.DullDagger;
import me.neoblade298.neorogue.equipment.cursed.GnarledStaff;
import me.neoblade298.neorogue.equipment.cursed.MangledBow;
import me.neoblade298.neorogue.equipment.cursed.RustySword;
import me.neoblade298.neorogue.equipment.offhands.BatteringRam;
import me.neoblade298.neorogue.equipment.offhands.ChasingDagger;
import me.neoblade298.neorogue.equipment.offhands.CloudyCrest;
import me.neoblade298.neorogue.equipment.offhands.ConductiveArmguard;
import me.neoblade298.neorogue.equipment.offhands.EnderEye;
import me.neoblade298.neorogue.equipment.offhands.EnduranceShield;
import me.neoblade298.neorogue.equipment.offhands.ForceBracer;
import me.neoblade298.neorogue.equipment.offhands.HastyShield;
import me.neoblade298.neorogue.equipment.offhands.HavenTome;
import me.neoblade298.neorogue.equipment.offhands.IcicleTome;
import me.neoblade298.neorogue.equipment.offhands.InsanityPowder;
import me.neoblade298.neorogue.equipment.offhands.IronMaiden;
import me.neoblade298.neorogue.equipment.offhands.LeadingKnife;
import me.neoblade298.neorogue.equipment.offhands.LeatherBracer;
import me.neoblade298.neorogue.equipment.offhands.LeviathanAxe;
import me.neoblade298.neorogue.equipment.offhands.MirrorBracer;
import me.neoblade298.neorogue.equipment.offhands.PaladinsShield;
import me.neoblade298.neorogue.equipment.offhands.PalmBlast;
import me.neoblade298.neorogue.equipment.offhands.PocketWatch;
import me.neoblade298.neorogue.equipment.offhands.PoisonPowder;
import me.neoblade298.neorogue.equipment.offhands.RedFan;
import me.neoblade298.neorogue.equipment.offhands.RubyArmament;
import me.neoblade298.neorogue.equipment.offhands.SmallShield;
import me.neoblade298.neorogue.equipment.offhands.SpikyShield;
import me.neoblade298.neorogue.equipment.offhands.TomeOfScorchedEarth;
import me.neoblade298.neorogue.equipment.offhands.TomeOfWeakness;
import me.neoblade298.neorogue.equipment.offhands.VeiledHourglass;
import me.neoblade298.neorogue.equipment.offhands.VengefulShield;
import me.neoblade298.neorogue.equipment.offhands.WristBlade;
import me.neoblade298.neorogue.equipment.weapons.*;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryIcon;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.event.WeaponSwingEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public abstract class Equipment implements Comparable<Equipment> {
	private static HashMap<String, Equipment> equipment = new HashMap<String, Equipment>();
	private static HashMap<String, Equipment> upgraded = new HashMap<String, Equipment>();
	private static DropTableSet<Equipment> droptables = new DropTableSet<Equipment>();
	private static DropTableSet<Artifact> artifacts = new DropTableSet<Artifact>();
	private static DropTableSet<Consumable> consumables = new DropTableSet<Consumable>();

	private ArrayList<Equipment> reforgeParents = new ArrayList<Equipment>();
	private TreeMap<Equipment, Equipment[]> reforgeOptions = new TreeMap<Equipment, Equipment[]>();

	protected String id;
	protected Component display, hoverable;
	protected boolean isUpgraded, canDrop = true, isCursed, overrideReforgeDrop;
	protected ItemStack item;
	protected Rarity rarity;
	protected EquipmentClass[] ecs;
	protected EquipmentType type;
	protected EquipmentProperties properties;
	protected int cooldown = 0;
	protected TreeSet<GlossaryIcon> tags = new TreeSet<GlossaryIcon>(GlossaryIcon.comparator);

	public static void load() {
		equipment.clear();
		upgraded.clear();
		droptables.reload();
		artifacts.reload();
		for (boolean b : new boolean[] { false, true }) {
			// Abilities
			new Absorb(b);
			new AcidBomb(b);
			new AfterImage(b);
			new Adrenaline(b);
			new Advantage(b);
			new AgilityTraining(b);
			new AnchoringEarth(b);
			new AnchoringEarth2(b);
			new Analyze(b);
			new Analyze2(b);
			new BalefulStrike(b);
			new Assassinate(b);
			new ArcaneBlast(b);
			new ArrowRain(b);
			new Atone(b);
			new Atrophy(b);
			new AvatarState(b);
			new Backstep(b);
			new Backstep2(b);
			new BasicDarkArts(b);
			new BasicElementMastery(b);
			new BasicInfusionMastery(b);
			new BasicManaManipulation(b);
			new BattleCry(b);
			new BerserkersCall(b);
			new Bide(b);
			new BitterCold(b);
			new Blackspike(b);
			new BlackRain(b);
			new Blast(b);
			new BlastStep(b);
			new BlessedEdge(b);
			new BlightTendril(b);
			new Blind(b);
			new BloodFrenzy(b);
			new Bloodlust(b);
			new BodyDouble(b);
			new BowTrap(b);
			new Brace(b);
			new Brace2(b);
			new Brand(b);
			new BreakingPoint(b);
			new BreakTheLine(b);
			new Brightshell(b);
			new Bulldoze(b);
			new Bulwark(b);
			new Burst(b);
			new CalculatingGaze(b);
			new Cauterize(b);
			new Charge(b);
			new Chill(b);
			new Chokehold(b);
			new ChosenOfTheLight(b);
			new Cleave(b);
			new CollectionHex(b);
			new CompoundingInjury(b);
			new Concoct(b);
			new Condemn(b);
			new ConfidenceKill(b);
			new Consecrate(b);
			new ConstantFlux(b);
			new Contaminate(b);
			new CorpseExplosion(b);
			new Counter(b);
			new CreateEarth(b);
			new Cripple(b);
			new CripplingPoison(b);
			new Crystallize(b);
			new Cull(b);
			new CurseMark(b);
			new DangerClose(b);
			new DarkBolt(b);
			new DarkBolt2(b);
			new DarkLance(b);
			new Darkness(b);
			new DarkPact(b);
			new DarkPulse(b);
			new DarkShroud(b);
			new DarkTorrent(b);
			new Deliberation(b);
			new Demoralize(b);
			new DensityOrb(b);
			new Depletion(b);
			new Dexterity(b);
			new Disappear(b);
			new Discharge(b);
			new Discipline(b);
			new Dread(b);
			new Dismantle(b);
			new Disorient(b);
			new DodgeRoll(b);
			new DoubleStrike(b);
			new DrainLightning(b);
			new EarthenDomain(b);
			new EarthenTackle(b);
			new EarthenWall(b);
			new Egoism(b);
			new ElectricOrb(b);
			new Electrode(b);
			new Electrolysis(b);
			new Embolden(b);
			new EmpoweredEdge(b);
			new Endurance(b);
			new Engulf(b);
			new Enlighten(b);
			new EndlessVenom(b);
			new EnduranceTraining(b);
			new Energize(b);
			new Entropy(b);
			new Envenom(b);
			new Envenom2(b);
			new Erupt(b);
			new EscapePlan(b);
			new EscapePlan2(b);
			new Execute(b);
			new Exertion(b);
			new ExploitWeakness(b);
			new Expunge(b);
			new EyeOfTheStorm(b);
			new Fade(b);
			new Farewell(b);
			new FeelNoPain(b);
			new Ferocity(b);
			new Fireball(b);
			new Fireball2(b);
			new Fireblast(b);
			new FireBolt(b);
			new Firebomb(b);
			new Firewall(b);
			new Finale(b);
			new Fissure(b);
			new FivePointStrike(b);
			new FlashDraw(b);
			new Flashfire(b);
			new FlashMark(b);
			new FlashSpark(b);
			new FlowState(b);
			new FlowState2(b);
			new Flurry(b);
			new FocusedShot(b);
			new FormAPlan(b);
			new ForceCloak(b);
			new Fortify(b);
			new Fortress(b);
			new Frenzy(b);
			new FrostTrap(b);
			new Frostwalker(b);
			new Furor(b);
			new Fury(b);
			new Gambit(b);
			new GatheringShadows(b);
			new GatheringShadows2(b);
			new GetCentered(b);
			new GraniteShield(b);
			new Gravity(b);
			new Grit(b);
			new GroundLance(b);
			new GrowingHex(b);
			new GrowingSpark(b);
			new GuardianSpirit(b);
			new HailCloak(b);
			new HeadTrauma(b);
			new HeatRising(b);
			new Heartbeat(b);
			new HerculeanStrength(b);
			new HerosLanding(b);
			new HexCurse(b);
			new HexCurse2(b);
			new HexingShot(b);
			new HoldTheLine(b);
			new HolySpear(b);
			new InducePanic(b);
			new InducePanic2(b);
			new Inflame(b);
			new Inexorable(b);
			new Initiator(b);
			new Intuition(b);
			new IronCuirass(b);
			new Ironskin(b);
			new KeenSenses(b);
			new LayExplosive(b);
			new LayTrap(b);
			new Lethality(b);
			new Lightfall(b);
			new LightningBolt(b);
			new LightningRush(b);
			new LightningStrike(b);
			new LightPulse(b);
			new LimitBreak(b);
			new LordOfTheNight(b);
			new Mahoraga(b);
			new Maim(b);
			new Malice(b);
			new Manabending(b);
			new ManaArc(b);
			new ManaBlitz(b);
			new ManaCloak(b);
			new ManaGuard(b);
			new ManaInfusion(b);
			new ManaShell(b);
			new MarkTarget(b);
			new MarkTarget2(b);
			new Mastermind(b);
			new MightySwing(b);
			new MindBlast(b);
			new MindGrowth(b);
			new MindGrowth2(b);
			new MindShell(b);
			new Momentum(b);
			new MortalEngine(b);
			new NightShade(b);
			new Obfuscation(b);
			new OdinsDecree(b);
			new Overflow(b);
			new Overload(b);
			new PalmBlast(b);
			new Paranoia(b);
			new Parry(b);
			new PartingGift(b);
			new PiercingShot(b);
			new PiercingShot2(b);
			new PiercingVenom(b);
			new Pin(b);
			new Plague(b);
			new PointBlank(b);
			new PoolOfLight(b);
			new Posturing(b);
			new Posturing2(b);
			new PowerThrough(b);
			new Prayer(b);
			new Preparation(b);
			new Pressure(b);
			new PreySeeker(b);
			new RainOfSteel(b);
			new Rampage(b);
			new RapidFire(b);
			new RazorCloak(b);
			new RecklessApproach(b);
			new RecklessSwing(b);
			new Resourcefulness(b);
			new Revenge(b);
			new RisingSun(b);
			new Roar(b);
			new Rushdown(b);
			new Quake(b);
			new QuickFeet(b);
			new Quickfire(b);
			new Quicken(b);
			new ShadowImbuement(b);
			new ShadowPartner(b);
			new ShadowPartner2(b);
			new ShadowWalk(b);
			new ShardBlast(b);
			new ShatteringShot(b);
			new ShoulderBash(b);
			new Sear(b);
			new SelfDestruct(b);
			new Setup(b);
			new Sidestep(b);
			new SilentSteps(b);
			new SilentSteps2(b);
			new SiphoningStrike(b);
			new Skirmisher(b);
			new Smite(b);
			new SmokeBomb(b);
			new SparkTrap(b);
			new SpikeTrap(b);
			new SpiritOfTheDragoon(b);
			new Splinterstone(b);
			new SpeedBlitz(b);
			new StaticSurge(b);
			new Storm(b);
			new Study(b);
			new Study2(b);
			new Sturdy(b);
			new SunderingShot(b);
			new Surprise(b);
			new FirstStrike(b);
			new Flicker(b);
			new Tackle(b);
			new Tailwind(b);
			new TargetAcquisition(b);
			new Tempest(b);
			new ThrowPoison(b);
			new ThornGarden(b);
			new ThriveInChaos(b);
			new Tireless(b);
			new Titan(b);
			new ToAshes(b);
			new Torch(b);
			new TreeTrunk(b);
			new TwinBolt(b);
			new TwinShiv(b);
			new ValiantPierce(b);
			new Vanish(b);
			new VitalPierce(b);
			new Volley(b);
			new WallJump(b);
			new WarCry(b);
			new Warmup(b);
			new WarningShot(b);
			new WeaponEnchantmentDarkness(b);
			new WeaponEnchantmentElectrified(b);
			new WeaponEnchantmentHoly(b);
			new Windcall(b);
			new Windcutter(b);
			new WindSlash(b);
			new WindTrap(b);
			new Wound(b);
			new Zone(b);
			new Zone2(b);

			// Accessories
			new AshenWreath(b);
			new BurningRing(b);
			new CeruleanBracelet(b);
			new EagleFeather(b);
			new EarthenBracer(b);
			new EarthenRing(b);
			new FlightRing(b);
			new FlintPendant(b);
			new GripGloves(b);
			new LifeThief(b);
			new MagicQuiver(b);
			new MajorShieldingRelic(b);
			new ManaMagnifier(b);
			new MajorStaminaRelic(b);
			new MinorManaRelic(b);
			new MinorPoisonRelic(b);
			new MinorShieldingRelic(b);
			new MinorStaminaRelic(b);
			new MinorStrengthRelic(b);
			new Plaguebearer(b);
			new RedRing(b);
			new RighteousRing(b);
			new RingOfAnger(b);
			new RingOfFortitude(b);
			new RingOfLight(b);
			new RingOfManaflow(b);
			new RingOfMentalism(b);
			new RingOfNature(b);
			new RingOfScalding(b);
			new RingOfSharpness(b);
			new SaboteursRing(b);
			new SpiritShard(b);
			new TopazRing(b);
			new VermillionBelt(b);
			new VoidBracelet(b);
			new Wildfire(b);
			new YellowRing(b);

			// Armor
			new AdaptiveChemvest(b);
			new ArcheryGlove(b);
			new AuricCape(b);
			new BootsOfSpeed(b);
			new BurningMantle(b);
			new BlindingCloak(b);
			new Brightcrown(b);
			new CalmingHood(b);
			new ClothBindings(b);
			new ElbowBrace(b);
			new ElectrostaticVest(b);
			new EnchantedCloak(b);
			new EngineersCap(b);
			new EtherVeil(b);
			new Footpads(b);
			new Gauze(b);
			new HuntersVest(b);
			new IcyArmguard(b);
			new IcySigil(b);
			new LeatherArmguard(b);
			new LeatherCowl(b);
			new LeatherChestplate(b);
			new LeatherHelmet(b);
			new LeatherHood(b);
			new LightningCloak(b);
			new MagiciansHood(b);
			new NullifyingCloak(b);
			new NullMagicMantle(b);
			new PhoenixfireMantle(b);
			new RedCloak(b);
			new SaviorsHelm(b);
			new SilversilkCowl(b);
			new SpikedPauldrons(b);
			new StarlightHood(b);
			new StonyCloak(b);

			// Offhands
			new BatteringRam(b);
			new BlinkRune(b);
			new ChasingDagger(b);
			new CloudyCrest(b);
			new CopperFunnel(b);
			new ConductiveArmguard(b);
			new EnderEye(b);
			new EnduranceShield(b);
			new ForceBracer(b);
			new GuardingRune(b);
			new HastyShield(b);
			new HavenTome(b);
			new Hullbreaker(b);
			new IcicleTome(b);
			new InsanityPowder(b);
			new IronMaiden(b);
			new LeadingKnife(b);
			new LeatherBracer(b);
			new MirrorBracer(b);
			new PaladinsShield(b);
			new PocketWatch(b);
			new PoisonPowder(b);
			new QuickTrap(b);
			new RazorTome(b);
			new RedFan(b);
			new RubyArmament(b);
			new SmallShield(b);
			new SpikyShield(b);
			new TomeOfScorchedEarth(b);
			new TomeOfWeakness(b);
			new VeiledHourglass(b);
			new VengefulShield(b);
			new WardingRune(b);
			new WristBlade(b);

			// Weapons
			new AshenHeadhunter(b);
			new AshenWand(b);
			new AshenWand2(b);
			new AvalonianMace(b);
			new BarbedRocket(b);
			new BasicBow(b);
			new BasicCrossbow(b);
			new BasicShotbow(b);
			new BeamStaff(b);
			new Bloodthirster(b);
			new BluntedArrow(b);
			new BoltWand(b);
			new ButterflyKnife(b);
			new ButterflyKnife2(b);
			new ChainLightningWand(b);
			new ColdArrow(b);
			new CompositeBow(b);
			new CrescentAxe(b);
			new CrimsonBlade(b);
			new CripplingFencingSword(b);
			new DarkScepter(b);
			new DoubleTap(b);
			new EarthStaff(b);
			new EarthenLeatherGauntlets(b);
			new ElectromagneticKnife(b);
			new EnergizedRazor(b);
			new EnfeeblingWand(b);
			new EtherealKnife(b);
			new EvasiveKnife(b);
			new Excalibur(b);
			new ExplosiveArrow(b);
			new FencingSword(b);
			new Firefly(b);
			new FireStaff(b);
			new Flametongue(b);
			new ForcefulLeatherGauntlets(b);
			new Fracturer(b);
			new FrostbiteBow(b);
			new GlassArrow(b);
			new Groundbreaker(b);
			new Harpoon(b);
			new HibernianQuickblade(b);
			new HiddenRazor(b);
			new HuntersBow(b);
			new DrainWand(b);
			new IronSword(b);
			new Irritant(b);
			new LeatherGauntlets(b);
			new LeviathanAxe(b);
			new LightLeatherGauntlets(b);
			new LightningCutter(b);
			new LightningWand(b);
			new LitArrow(b);
			new MagicSpear(b);
			new ManaEater(b);
			new MassiveHalberd(b);
			new MechanicalBow(b);
			new MirrorSickle(b);
			new MonksHeadsplitter(b);
			new MonksStaff(b);
			new MonksStaff2(b);
			new MultiCrossbow(b);
			new Nightmare(b);
			new OldStaff(b);
			new Rapier(b);
			new Razor(b);
			new ReckoningOrb(b);
			new RedBaron(b);
			new RighteousFlame(b);
			new RighteousLance(b);
			new SearingArrow(b);
			new SerratedArrow(b);
			new SerratedRazor(b);
			new SilverFang(b);
			new ShadowyDagger(b);
			new ShieldbearerStaff(b);
			new ShieldPike(b);
			new SlowingOrb(b);
			new SoulHarvester(b);
			new SparkdrainKnife(b);
			new SparkKnife(b);
			new SparkStick(b);
			new StickyBomb(b);
			new StoneArrow(b);
			new StoneAxe(b);
			new StoneDagger(b);
			new StoneDriver(b);
			new StoneHammer(b);
			new StoneMace(b);
			new StoneShiv(b);
			new StoneSpear(b);
			new StoneSword(b);
			new StoneThrowingKnife(b);
			new StonyWand(b);
			new TacticiansDagger(b);
			new TheGreatDivide(b);
			new ToxicRazor(b);
			new UnderDarkness(b);
			new RighteousHammer(b);
			new WandOfIgnition(b);
			new WoodenArrow(b);
			new WoodenDagger(b);
			new WoodenSword(b);
			new WoodenWand(b);

			new Neoblade(b);

			// Consumables
			new MinorFirePotion(b);
			new MinorHealthPotion(b);
			new MinorStaminaPotion(b);
			new MinorManaPotion(b);
			new MinorShieldsPotion(b);
			new MinorPhysicalPotion(b);
			new MinorMagicalPotion(b);
		}

		// Artifacts
		new AlchemistBag();
		new AmuletOfOffering();
		new Anxiety();
		new ArmorStand();
		new AthenianChalice();
		new AurorBadge();
		new AvalonianAnchor();
		new AzureCutter();
		new BagOfPreparation();
		new BloodyTrinket();
		new BurningCross();
		new Bramblevine();
		new Brightfeather();
		new CailiricCrystal();
		new CharmOfGallus();
		new ConcealingCloak();
		new CrackedCrystal();
		new CrossOfAntiquan();
		new CrystalFeather();
		new CrystallineFlask();
		new DaedalusHammer();
		new DarkArtsTreatise();
		new DiscountCard();
		new EarthenTome();
		new EnergyBattery();
		new Exhaustion();
		new FaerieDust();
		new FaeriePendant();
		new ForceTrinket();
		new GlacialHammer();
		new GoldIngot();
		new GoldenVeil();
		new GrendelsCrystalMirror();
		new GrendelsFavoriteTome();
		new HermesBoots();
		new HiddenBlade();
		new HolyScriptures();
		new HuntersCompass();
		new InfernalTome();
		new LionheartBangle();
		new Lockbox();
		new ManaflowBand();
		new ManaHaze();
		new MercenaryHeadband();
		new MerchantSpyglass();
		new MiasmaInABottle();
		new MistralVeil();
		new NoxianBlight();
		new NoxianSkull();
		new ObsidianCharm();
		new OmniGem();
		new OpalHourglass();
		new OpalNecklace();
		new PotOfGreed();
		new PracticeDummy();
		new Pumped();
		new QuartzPrism();
		new RodOfAges();
		new ScrollOfFrost();
		new SigilOfTheIronLegion();
		new ShellTrinket();
		new StarlightVeil();
		new StaticNecklace();
		new StormSigil();
		new TarotCard();
		new TomeOfWisdom();
		new TreatiseOnElectricity();
		new TrickstersSigil();
		new WarpedAnvil();

		// Levelup artifacts
		new EmeraldCluster();
		new EmeraldGem();
		new EmeraldShard();
		new RubyCluster();
		new RubyGem();
		new RubyShard();
		new SapphireCluster();
		new SapphireGem();
		new SapphireShard();

		// Curses
		new DullDagger();
		new GnarledStaff();
		new MangledBow();
		new RustySword();

		HashMap<EquipmentType, Integer> counts = new HashMap<EquipmentType, Integer>();
		int cursed = 0;
		for (EquipmentType type : EquipmentType.values()) {
			counts.put(type, 0);
		}

		// Setup equipment
		for (Equipment eq : equipment.values()) {
			eq.setup();
			Equipment up = eq.getUpgraded();
			if (up != null) {
				up.setup();
			}

			if (eq.isCursed) {
				cursed++;
			} else {
				counts.put(eq.getType(), counts.get(eq.getType()) + 1);
			}

			// Check for reforges being wrong rarity
			if (eq.reforgeParents != null) {
				for (Equipment parent : eq.reforgeParents) {
					if (parent.getRarity() == eq.getRarity()) {
						Bukkit.getLogger().warning("[NeoRogue] " + eq.getId() + " has a reforge parent of "
								+ parent.getId() + " with same rarity");
						break;
					}
				}
			}
		}

		// Setup item now that all reforge options exist
		for (Equipment eq : equipment.values()) {
			eq.setupItem();
			Equipment up = eq.getUpgraded();
			if (up != null) {
				up.setupItem();
			}
		}

		for (Equipment eq : equipment.values()) {
			eq.setupDroptable(); // Only add unupgraded equipment to droptable
			eq.postSetup();
			Equipment up = eq.getUpgraded();
			if (up != null) {
				up.postSetup();
			}
		}

		for (EquipmentType type : EquipmentType.values()) {
			Bukkit.getLogger().info("[NeoRogue] Loaded " + counts.get(type) + " " + type.getDisplay());
		}
		Bukkit.getLogger().info("[NeoRogue] Loaded " + cursed + " Cursed");
		
		// Validate that all ability classes are initialized
		validateClassInitialization();
	}

	public Equipment(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec,
			EquipmentType type, EquipmentProperties props) {
		this(id, display, isUpgraded, rarity, new EquipmentClass[] { ec }, type, props);
	}

	public Equipment(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass[] ecs,
			EquipmentType type, EquipmentProperties props) {
		this.id = id;
		this.rarity = rarity;
		this.isUpgraded = isUpgraded;
		this.ecs = ecs;
		this.type = type;
		this.properties = props;

		// Just make sure not to close any of the tags in display string or the upgraded
		// sign will break it
		this.display = rarity.applyDecorations(SharedUtil.color(display + (isUpgraded ? "+" : "")));

		if (equipment.containsKey(id) && !isUpgraded) {
			Bukkit.getLogger().warning("[NeoRogue] Duplicate id of " + id + " found while loading equipment");
		}

		if (isUpgraded)
			upgraded.put(id, this);
		else
			equipment.put(id, this);
	}

	public Equipment(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec,
			EquipmentType type) {
		this(id, display, isUpgraded, rarity, new EquipmentClass[] { ec }, type, EquipmentProperties.none());
	}

	public Equipment(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass[] ecs,
			EquipmentType type) {
		this(id, display, isUpgraded, rarity, ecs, type, EquipmentProperties.none());
	}

	// For curses
	public Equipment(String id, String display, EquipmentType type) {
		this.id = id;
		this.rarity = Rarity.COMMON;
		this.isUpgraded = false;
		this.ecs = new EquipmentClass[] { EquipmentClass.CLASSLESS };
		this.type = type;
		this.display = SharedUtil.color("<red>" + display);
		this.properties = EquipmentProperties.none();
		this.isCursed = true;
		this.canDrop = false;

		if (equipment.containsKey(id)) {
			Bukkit.getLogger().warning("[NeoRogue] Duplicate id of " + id + " found while loading equipment");
		}

		equipment.put(id, this);
	}

	// For materials
	public Equipment(String id, String display, Rarity rarity, EquipmentClass ec) {
		this.id = id;
		this.rarity = rarity;
		this.isUpgraded = false;
		this.ecs = new EquipmentClass[] { ec };
		this.type = EquipmentType.MATERIAL;
		this.display = rarity.applyDecorations(SharedUtil.color(display));
		this.properties = EquipmentProperties.none();
		this.canDrop = false;

		if (equipment.containsKey(id)) {
			Bukkit.getLogger().warning("[NeoRogue] Duplicate id of " + id + " found while loading equipment");
		}

		equipment.put(id, this);
	}

	public static boolean canReforge(Equipment eq, Equipment eqed) {
		boolean hasUpgrade = eq.isUpgraded() || eqed.isUpgraded();
		boolean hasCurse = eq.isCursed() || eqed.isCursed();
		return hasUpgrade || hasCurse;
	}

	public EquipmentProperties getProperties() {
		return properties;
	}

	public abstract void setupItem();

	// Basically just used for rusty sword to get an item
	public void postSetup() {
	}

	public void setupReforges() {
	}

	private void setup() {
		if (isUpgraded) {
			Equipment base = getUnupgraded();
			reforgeParents = base.getReforgeParents();
			reforgeOptions = base.getReforgeOptions();
		}
		setupReforges();
	}

	public boolean canUpgrade() {
		return upgraded.containsKey(id);
	}

	private void setupDroptable() {
		if (!canDrop)
			return;
		if (!reforgeParents.isEmpty() && !overrideReforgeDrop)
			return;

		if (type == EquipmentType.ARTIFACT) {
			artifacts.add(ecs, (Artifact) this);
		}
		else if (type == EquipmentType.CONSUMABLE) {
			consumables.add(ecs, (Consumable) this);
		}
		else {
			droptables.add(ecs, this);
		}
	}

	public void addTags(GlossaryTag... tags) {
		for (GlossaryTag tag : tags) {
			this.tags.add(tag);
		}
	}

	public boolean overridesReforgeDrop() {
		return overrideReforgeDrop;
	}

	// Run at the start of a fight to initialize Fight Data
	public abstract void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot);

	// Run at the end of a fight if needed
	public void cleanup(Player p, PlayerFightData data) {

	}

	public TreeSet<GlossaryIcon> getTags() {
		return tags;
	}

	public String getId() {
		return id;
	}

	public ItemStack getItem() {
		return item.clone();
	}

	public boolean isUpgraded() {
		return isUpgraded;
	}

	public Rarity getRarity() {
		return rarity;
	}

	public static Equipment get(String id, boolean upgrade) {
		return upgrade ? upgraded.get(id) : equipment.get(id);
	}

	public static String serialize(ArrayList<Equipment> arr) {
		String str = "";
		for (int i = 0; i < arr.size(); i++) {
			str += arr.get(i).serialize() + ";";
		}
		return str;
	}

	public static String serialize(Equipment[] arr) {
		String str = "";
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == null) {
				str += " ;";
				continue;
			}
			str += arr[i].serialize() + ";";
		}
		return str;
	}

	public String serialize() {
		return id + (isUpgraded ? "+" : "");
	}

	public static Equipment deserialize(String str) {
		if (str.isBlank())
			return null;
		boolean isUpgraded = false;
		if (str.endsWith("+")) {
			isUpgraded = true;
			str = str.substring(0, str.length() - 1);
		}
		return get(str, isUpgraded);
	}

	public static Equipment[] deserializeAsArray(String str) {
		String[] separated = str.split(";");
		Equipment[] arr = new Equipment[separated.length];
		try {
			for (int i = 0; i < separated.length; i++) {
				if (str.isBlank())
					continue;
				arr[i] = Equipment.deserialize(separated[i]);
			}
		}
		catch (Exception e) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to deserialize equipment as an array: " + str);
			e.printStackTrace();
		}
		return arr;
	}

	public static ArrayList<Equipment> deserializeAsArrayList(String str) {
		if (str.isBlank())
			return new ArrayList<Equipment>();
		String[] separated = str.split(";");
		ArrayList<Equipment> arr = new ArrayList<Equipment>(separated.length);
		for (String s : separated) {
			if (str.isBlank())
				continue;
			arr.add(Equipment.deserialize(s));
		}
		return arr;
	}

	public ItemStack createItem(Material mat) {
		return createItem(mat, null, null);
	}

	public ItemStack createItem(Material mat, String loreLine) {
		return createItem(mat, null, loreLine);
	}

	public ItemStack createItem(Material mat, String[] preLoreLine, String loreLine) {
		// Spell check only needs to happen with unupgraded
		if (loreLine != null) {
			char prev = 0;
			for (char c : loreLine.toCharArray()) {
				if (c == ' ' && prev == ' ') {
					Bukkit.getLogger()
							.warning("[NeoRogue] Duplicate space found in equipment " + id + (isUpgraded ? "+" : ""));
				}
				prev = c;
			}
		}
		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();

		meta.displayName(display.decoration(TextDecoration.ITALIC, State.FALSE));
		ArrayList<Component> loreItalicized = new ArrayList<Component>();
		loreItalicized.add(Component.text("Right click for glossary", NamedTextColor.GRAY));
		if (isCursed) {
			loreItalicized.add(Component.text("Cursed " + type.getDisplay(), NamedTextColor.RED));
		} else {
			loreItalicized.add(rarity.getDisplay(true).append(Component.text(" " + type.getDisplay())));
		}
		loreItalicized.addAll(properties.generateLore(this));
		if (preLoreLine != null) {
			for (String l : preLoreLine) {
				loreItalicized.add(NeoCore.miniMessage().deserialize(l).color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC));
			}
		}
		if (isCursed) {
			loreItalicized.add(
					Component.text("This item is cursed. It must be equipped to continue.", NamedTextColor.DARK_RED));
		}
		if (!reforgeOptions.isEmpty()) {
			loreItalicized.add(Component.text("Reforgeable with:", TextColor.fromHexString(EquipmentProperties.PROPERTY_COLOR)));
			for (Equipment eq : reforgeOptions.keySet()) {
				boolean noPostfix = isCursed || eq.isCursed;
				String postfix = "";
				if (!noPostfix) {
					postfix = isUpgraded ? "(+)" : "+";
				}
				loreItalicized.add(Component.text("- ", TextColor.fromHexString(EquipmentProperties.PROPERTY_COLOR))
						.append(eq.getDisplay().append(Component.text(postfix))));
			}
		}
		if (loreLine != null) {
			for (TextComponent tc : SharedUtil.addLineBreaks(
					(TextComponent) NeoCore.miniMessage().deserialize(loreLine).colorIfAbsent(NamedTextColor.GRAY),
					200)) {
				loreItalicized.add(tc);
			}
		}
		ArrayList<Component> lore = new ArrayList<Component>(loreItalicized.size());
		for (Component c : loreItalicized) {
			lore.add(c.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		}
		meta.lore(lore);

		if (item.getType() == Material.BOW) {
			meta.addEnchant(Enchantment.INFINITY, 1, true); // Needed for now
		}

		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DYE, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ARMOR_TRIM);
		meta.setEnchantmentGlintOverride(isUpgraded);
		meta.setUnbreakable(true);
		properties.modifyItemMeta(item, meta);
		item.setItemMeta(meta);

		this.hoverable = this.display.decorate(TextDecoration.UNDERLINED).hoverEvent(item.asHoverEvent())
				.clickEvent(ClickEvent.runCommand("/nr glossary " + this.id));

		NBTItem nbti = new NBTItem(item);
		nbti.setString("equipId", id);
		nbti.setString("type", type.getDisplay());
		nbti.setBoolean("isUpgraded", isUpgraded);
		return nbti.getItem();
	}

	public TreeMap<Equipment, Equipment[]> getReforgeOptions() {
		return reforgeOptions;
	}

	public EquipmentClass[] getEquipmentClasses() {
		return ecs;
	}

	public boolean containsReforgeOption(String id) {
		for (Equipment option : reforgeOptions.keySet()) {
			if (option.id.equals(id))
				return true;
		}
		return false;
	}

	// Should only ever be called with unupgraded parameters
	protected void addReforge(Equipment combineWith, Equipment... options) {
		if (!isUpgraded) {
			// For every option, add the components as parents and place options
			for (int i = 0; i < options.length; i++) {
				options[i].addReforgeParent(combineWith);
				options[i].addReforgeParent(this);
			}

			// Set item into reforge options
			this.reforgeOptions.put(combineWith, options);
			if (this != combineWith) {
				combineWith.reforgeOptions.put(this, options);
			}
		}
	}

	protected void addSelfReforge(Equipment... options) {
		addReforge(this.getUnupgraded(), options);
	}

	public Equipment getUnupgraded() {
		return equipment.get(id);
	}

	public Equipment getUpgraded() {
		return upgraded.get(id);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Equipment))
			return false;
		Equipment eq = (Equipment) o;
		return eq.id.equals(this.id) && eq.isUpgraded == this.isUpgraded;
	}

	public boolean isSimilar(Equipment eq) {
		return eq.id.equals(this.id);
	}

	public static ArrayList<Artifact> getArtifact(DropTableSet<Artifact> set, int value, int numDrops,
			EquipmentClass... ec) {
		return set.getMultiple(value, numDrops, ec);
	}

	public static ArrayList<Consumable> getConsumable(int value, int numDrops, EquipmentClass... ec) {
		return consumables.getMultiple(value, numDrops, ec);
	}

	public static ArrayList<Equipment> getDrop(int value, int numDrops, EquipmentClass... ec) {
		return droptables.getMultiple(value, numDrops, ec);
	}

	public static ArrayList<Equipment> getDrop(int value, int numDrops, ArrayList<Equipment> exclusions, EquipmentClass... ec) {
		return droptables.getMultiple(value, numDrops, true, exclusions, ec);
	}

	public static Equipment getDrop(int value, EquipmentClass... ec) {
		return getDrop(value, 1, ec).get(0);
	}

	public static Consumable getConsumable(int value, EquipmentClass... ec) {
		return getConsumable(value, 1, ec).get(0);
	}

	private void addReforgeParent(Equipment reforgeParent) {
		this.reforgeParents.add(reforgeParent);
	}

	public ArrayList<Equipment> getReforgeParents() {
		return reforgeParents;
	}

	public Component getDisplay() {
		return display;
	}

	public Component getHoverable() {
		return hoverable;
	}

	public int getCooldown() {
		return cooldown;
	}

	public EquipmentType getType() {
		return type;
	}

	public boolean canEquip(EquipSlot es) {
		return type.canEquip(es);
	}

	public boolean canDrop() {
		return canDrop;
	}

	public boolean canUseWeapon(PlayerFightData data) {
		double manaCost = properties.get(PropertyType.MANA_COST);
		if (data.getMana() < manaCost && manaCost > 0) {
			Util.displayError(data.getPlayer(), "Not enough mana!");
			return false;
		}

		double staminaCost = properties.get(PropertyType.STAMINA_COST);
		if (data.getStamina() < staminaCost && staminaCost > 0) {
			Util.displayError(data.getPlayer(), "Not enough stamina!");
			return false;
		}
		return true;
	}

	public void weaponSwing(Player p, PlayerFightData data) {
		weaponSwing(p, data, properties.get(PropertyType.ATTACK_SPEED));
	}

	public void weaponSwing(Player p, PlayerFightData data, double attackSpeed) {
		if (properties.has(PropertyType.MANA_COST))
			data.addMana(-properties.get(PropertyType.MANA_COST));
		if (properties.has(PropertyType.STAMINA_COST))
			data.addStamina(-properties.get(PropertyType.STAMINA_COST));
		properties.getSwingSound().play(p, p);
		WeaponSwingEvent ev = new WeaponSwingEvent(this, attackSpeed);
		FightInstance.trigger(p, Trigger.WEAPON_SWING, ev);
		data.setBasicAttackCooldown(type.getSlots()[0], ev.getAttackSpeedBuffList().apply(attackSpeed));
		if (type.getSlots()[0] == EquipSlot.OFFHAND)
			p.swingOffHand();
	}

	public void weaponSwingAndDamage(Player p, PlayerFightData data, LivingEntity target) {
		weaponSwing(p, data);
		weaponDamage(p, data, target);
	}

	public void weaponSwingAndDamage(Player p, PlayerFightData data, LivingEntity target, double damage) {
		weaponSwing(p, data);
		weaponDamage(p, data, target, damage);
	}

	public void weaponDamage(Player p, PlayerFightData data, LivingEntity target) {
		weaponDamage(p, data, target, properties.get(PropertyType.DAMAGE));
	}

	public void weaponDamage(Player p, PlayerFightData data, LivingEntity target, double damage) {
		DamageMeta dm = new DamageMeta(data, damage, properties.getType(), DamageStatTracker.of(id, this));
		dm.setKnockback(properties.get(PropertyType.KNOCKBACK));
		dm.isBasicAttack(this, true);
		FightInstance.dealDamage(dm, target);
	}

	public boolean isCursed() {
		return isCursed;
	}

	// Only happens for curses at shops
	public void onPurify(PlayerSessionData data) {
	}

	@Override
	public String toString() {
		return id + (isUpgraded ? "+" : "");
	}

	public static enum EquipmentClass {
		WARRIOR("Warrior"), THIEF("Thief"), ARCHER("Archer"), MAGE("Mage"), SHOP("Shop"), CLASSLESS("Classless");

		private String display;

		private EquipmentClass(String display) {
			this.display = display;
		}

		public String getDisplay() {
			return display;
		}
	}

	public static enum EquipmentType {
		WEAPON("Weapon", "me.neoblade298.neorogue.equipment.weapons", new EquipSlot[] { EquipSlot.HOTBAR }),
		ARMOR("Armor", "me.neoblade298.neorogue.equipment.armor", new EquipSlot[] { EquipSlot.ARMOR }),
		ACCESSORY("Accessory", "me.neoblade298.neorogue.equipment.accessories",
				new EquipSlot[] { EquipSlot.ACCESSORY }),
		OFFHAND("Offhand", "me.neoblade298.neorogue.equipment.offhands", new EquipSlot[] { EquipSlot.OFFHAND }),
		ABILITY("Ability", "me.neoblade298.neorogue.equipment.abilities",
				new EquipSlot[] { EquipSlot.HOTBAR, EquipSlot.KEYBIND }),
		CONSUMABLE("Consumable", "me.neoblade298.neorogue.equipment.consumables",
				new EquipSlot[] { EquipSlot.HOTBAR, EquipSlot.KEYBIND }),
		MATERIAL("Material", "me.neoblade298.neorogue.equipment.materials", new EquipSlot[0]),
		ARTIFACT("Artifact", "me.neoblade298.neorogue.equipment.artifacts", new EquipSlot[0]);

		private String display, pkg;
		private EquipSlot[] slots;

		private EquipmentType(String display, String pkg, EquipSlot[] slots) {
			this.display = display;
			this.slots = slots;
		}

		public String getDisplay() {
			return display;
		}

		public EquipSlot[] getSlots() {
			return slots;
		}

		public boolean canEquip(EquipSlot es) {
			for (EquipSlot slot : slots) {
				if (slot == es)
					return true;
			}
			return false;
		}

		public String getPackage() {
			return pkg;
		}
	}

	public static DropTableSet<Artifact> copyArtifactsDropSet(EquipmentClass... ecs) {
		return artifacts.clone(ecs);
	}

	public static enum EquipSlot {
		ARMOR("Armor"), ACCESSORY("Accessory"), OFFHAND("Offhand"), HOTBAR("Hotbar"), KEYBIND("Keybind"), // Hotbar +
		// other
		// binds
		STORAGE("Storage");

		private String display;

		private EquipSlot(String display) {
			this.display = display;
		}

		public String getDisplay() {
			return display;
		}

		public static int convertSlot(EquipSlot es, int slot) {
			switch (es) {
			case ACCESSORY:
				return slot + 21;
			case ARMOR:
				return slot + 18;
			case HOTBAR:
				return slot;
			case KEYBIND:
				return slot + 27;
			case OFFHAND:
				return 40;
			default:
				return -1;
			}
		}
	}

	public static class DropTableSet<E extends Equipment> {
		protected static final int TIER_MAX = 10;
		private static final int[] EQUIPMENT_VALUES = new int[Rarity.values().length * TIER_MAX];
		protected HashMap<EquipmentClass, ArrayList<DropTable<E>>> droptables = new HashMap<EquipmentClass, ArrayList<DropTable<E>>>();

		static {
			EQUIPMENT_VALUES[getIndex(Rarity.COMMON, 0)] = 10;

			EQUIPMENT_VALUES[getIndex(Rarity.COMMON, 1)] = 9;
			EQUIPMENT_VALUES[getIndex(Rarity.UNCOMMON, 1)] = 1;

			EQUIPMENT_VALUES[getIndex(Rarity.COMMON, 2)] = 5;
			EQUIPMENT_VALUES[getIndex(Rarity.UNCOMMON, 2)] = 4;
			EQUIPMENT_VALUES[getIndex(Rarity.RARE, 2)] = 1;

			EQUIPMENT_VALUES[getIndex(Rarity.COMMON, 3)] = 1;
			EQUIPMENT_VALUES[getIndex(Rarity.UNCOMMON, 3)] = 8;
			EQUIPMENT_VALUES[getIndex(Rarity.RARE, 3)] = 2;

			EQUIPMENT_VALUES[getIndex(Rarity.UNCOMMON, 4)] = 7;
			EQUIPMENT_VALUES[getIndex(Rarity.RARE, 4)] = 3;

			EQUIPMENT_VALUES[getIndex(Rarity.UNCOMMON, 5)] = 5;
			EQUIPMENT_VALUES[getIndex(Rarity.RARE, 5)] = 4;
			EQUIPMENT_VALUES[getIndex(Rarity.EPIC, 5)] = 1;

			EQUIPMENT_VALUES[getIndex(Rarity.UNCOMMON, 6)] = 3;
			EQUIPMENT_VALUES[getIndex(Rarity.RARE, 6)] = 6;
			EQUIPMENT_VALUES[getIndex(Rarity.EPIC, 6)] = 1;

			EQUIPMENT_VALUES[getIndex(Rarity.UNCOMMON, 7)] = 1;
			EQUIPMENT_VALUES[getIndex(Rarity.RARE, 7)] = 7;
			EQUIPMENT_VALUES[getIndex(Rarity.EPIC, 7)] = 2;

			EQUIPMENT_VALUES[getIndex(Rarity.RARE, 8)] = 6;
			EQUIPMENT_VALUES[getIndex(Rarity.EPIC, 8)] = 4;
		}

		private static int getIndex(Rarity rarity, int tier) {
			return rarity.ordinal() * TIER_MAX + tier;
		}

		private static int getValue(Rarity rarity, int tier) {
			return EQUIPMENT_VALUES[getIndex(rarity, tier)];
		}

		public DropTableSet() {
			reload();
		}

		private DropTableSet(DropTableSet<E> original, EquipmentClass... ecs) {
			for (EquipmentClass ec : original.droptables.keySet()) {
				ArrayList<DropTable<E>> list = new ArrayList<DropTable<E>>(TIER_MAX);
				ArrayList<DropTable<E>> originalList = original.droptables.get(ec);
				for (int i = 0; i < originalList.size(); i++) {
					list.add(originalList.get(i).clone());
				}
				droptables.put(ec, list);
			}
		}

		public DropTableSet<E> clone(EquipmentClass... ecs) {
			return new DropTableSet<E>(this, ecs);
		}

		public void reload() {
			for (EquipmentClass ec : EquipmentClass.values()) {
				ArrayList<DropTable<E>> tables = new ArrayList<DropTable<E>>(TIER_MAX);
				for (int i = 0; i < TIER_MAX; i++) {
					tables.add(new DropTable<E>());
				}
				droptables.put(ec, tables);
			}
		}

		public void remove(E drop) {
			for (ArrayList<DropTable<E>> list : droptables.values()) {
				for (DropTable<E> table : list) {
					table.remove(drop);
				}
			}
		}

		public void add(EquipmentClass[] ecs, E drop) {
			for (EquipmentClass ec : ecs) {
				for (int i = 0; i < TIER_MAX; i++) {
					ArrayList<DropTable<E>> table = droptables.get(ec);
					int value = getValue(drop.rarity, i);
					if (value > 0) table.get(i).add(drop, value);
				}
			}
		}

		/*
		public void addLenientWeight(EquipmentClass[] ecs, int value, E drop) {
			for (EquipmentClass ec : ecs) {
				ArrayList<DropTable<E>> table = droptables.get(ec);
				if (value >= 4) {
					table.get(value - 4).add(drop, 1);
				}
				if (value >= 3) {
					table.get(value - 3).add(drop, 2);
				}
				if (value >= 2) {
					table.get(value - 2).add(drop, 3);
				}
				if (value >= 1) {
					table.get(value - 1).add(drop, 4);
				}
				table.get(value).add(drop, 5);
				table.get(value + 1).add(drop, 4);
				table.get(value + 2).add(drop, 3);
				table.get(value + 3).add(drop, 2);
				table.get(value + 4).add(drop, 1);
			}
		}
		*/

		public ArrayList<E> getMultiple(int value, int numDrops, EquipmentClass... ec) {
			return getMultiple(value, numDrops, true, null, ec);
		}

		public ArrayList<E> getMultiple(int value, int numDrops, boolean unique, ArrayList<E> exclusions, EquipmentClass... ec) {
			DropTable<E> table;
			if (ec.length > 1) {
				// If more than 1 equipment class, choose from all of them equally
				DropTable<DropTable<E>> tables = new DropTable<DropTable<E>>();
				for (int j = 0; j < ec.length; j++) {
					if (!droptables.containsKey(ec[j]) || value >= droptables.get(ec[j]).size())
						continue;
					DropTable<E> temp = droptables.get(ec[j]).get(value);
					tables.add(temp, temp.getTotalWeight());
				}
				table = tables.get();
			} else {
				table = droptables.get(ec[0]).get(value);
			}
			
			if (table.size() < numDrops) {
				Bukkit.getLogger().warning("[NeoRogue] Failed to find " + numDrops + " equipment of value " + value + " for equip classes " + ec + ", falling back to lower value");
				return getMultiple(value - 1, numDrops, unique, exclusions, ec);
			}

			return table.getMultiple(numDrops, unique, exclusions);
		}

		@Override
		public String toString() {
			return droptables.toString();
		}
	}

	@Override
	public int compareTo(Equipment o) {
		int comp = this.id.compareTo(o.id);
		if (comp != 0)
			return comp;
		return Boolean.compare(this.isUpgraded, o.isUpgraded);
	}

	public static Set<String> getEquipmentIds() {
		return equipment.keySet();
	}

	public static Collection<Equipment> getAll() {
		return equipment.values();
	}
	
	/**
	 * Validates that all classes in all equipment packages have been properly initialized.
	 * Logs warnings for any missing classes that should be registered.
	 */
	private static void validateClassInitialization() {
		// Define all equipment packages to validate
		String[] packageNames = {
			"me.neoblade298.neorogue.equipment.abilities",
			"me.neoblade298.neorogue.equipment.accessories", 
			"me.neoblade298.neorogue.equipment.armor",
			"me.neoblade298.neorogue.equipment.artifacts",
			"me.neoblade298.neorogue.equipment.consumables",
			"me.neoblade298.neorogue.equipment.cursed",
			"me.neoblade298.neorogue.equipment.materials",
			"me.neoblade298.neorogue.equipment.mechanics",
			"me.neoblade298.neorogue.equipment.offhands",
			"me.neoblade298.neorogue.equipment.weapons"
		};
		
		try {
			int totalClasses = 0;
			int totalMissing = 0;
			
			for (String packageName : packageNames) {
				// Get all classes in the current package
				List<Class<?>> equipmentClasses = getClassesInPackage(packageName);
				
				// Track missing classes for this package
				List<String> missingClasses = new ArrayList<>();
				int validClassCount = 0;
				
				for (Class<?> clazz : equipmentClasses) {
					// Skip abstract classes, interfaces, inner classes, and non-Equipment classes
					if (Modifier.isAbstract(clazz.getModifiers()) || 
						clazz.isInterface() || 
						clazz.isMemberClass() ||
						!Equipment.class.isAssignableFrom(clazz)) {
						continue;
					}
					
					validClassCount++;
					
					// Check if this class has been registered
					if (!equipment.containsKey(clazz.getSimpleName())) {
						missingClasses.add(clazz.getSimpleName());
					}
				}
				
				totalClasses += validClassCount;
				totalMissing += missingClasses.size();
				
				// Report results for this package
				String packageShortName = packageName.substring(packageName.lastIndexOf('.') + 1);
				if (missingClasses.isEmpty() && validClassCount > 0) {
					Bukkit.getLogger().info("[NeoRogue] ✓ All " + validClassCount + " " + packageShortName + " classes are properly initialized!");
				} else if (!missingClasses.isEmpty()) {
					Bukkit.getLogger().warning("[NeoRogue] ⚠ Found " + missingClasses.size() + " uninitialized " + packageShortName + " classes:");
					for (String missing : missingClasses) {
						Bukkit.getLogger().warning("[NeoRogue]   - " + missing);
					}
				}
			}
			
			// Summary report
			if (totalMissing == 0 && totalClasses > 0) {
				Bukkit.getLogger().info("[NeoRogue] ✓ All " + totalClasses + " equipment classes across all packages are properly initialized!");
			} else if (totalMissing > 0) {
				Bukkit.getLogger().warning("[NeoRogue] Equipment validation summary: " + (totalClasses - totalMissing) + "/" + totalClasses + " classes initialized (" + totalMissing + " missing)");
			}
			
		} catch (Exception e) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to validate equipment class initialization: " + e.getMessage());
		}
	}
	
	/**
	 * Gets all classes in a given package using reflection.
	 * Works both in development (file system) and production (JAR) environments.
	 */
	public static List<Class<?>> getClassesInPackage(String packageName) throws Exception {
		List<Class<?>> classes = new ArrayList<>();
		String path = packageName.replace('.', '/');
		
		// Get the class loader
		ClassLoader classLoader = Equipment.class.getClassLoader();
		Enumeration<URL> resources = classLoader.getResources(path);
		
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			
			if (resource.getProtocol().equals("file")) {
				// Development environment - files in filesystem
				File directory = new File(resource.getFile());
				if (directory.exists()) {
					for (File file : directory.listFiles()) {
						if (file.getName().endsWith(".class")) {
							String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
							try {
								classes.add(Class.forName(className));
							} catch (ClassNotFoundException e) {
								// Skip classes that can't be loaded
							}
						}
					}
				}
			} else if (resource.getProtocol().equals("jar")) {
				// Production environment - classes in JAR file
				String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
				try (JarFile jar = new JarFile(jarPath)) {
					Enumeration<JarEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						if (entry.getName().startsWith(path) && entry.getName().endsWith(".class")) {
							String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
							if (className.startsWith(packageName)) {
								try {
									classes.add(Class.forName(className));
								} catch (ClassNotFoundException e) {
									// Skip classes that can't be loaded
								}
							}
						}
					}
				}
			}
		}
		
		return classes;
	}
}
