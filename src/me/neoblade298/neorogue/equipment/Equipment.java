package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
import me.neoblade298.neorogue.equipment.accessories.EarthenRing;
import me.neoblade298.neorogue.equipment.accessories.GripGloves;
import me.neoblade298.neorogue.equipment.accessories.MinorManaRelic;
import me.neoblade298.neorogue.equipment.accessories.MinorPoisonRelic;
import me.neoblade298.neorogue.equipment.accessories.MinorShieldingRelic;
import me.neoblade298.neorogue.equipment.accessories.MinorStaminaRelic;
import me.neoblade298.neorogue.equipment.accessories.MinorStrengthRelic;
import me.neoblade298.neorogue.equipment.accessories.RingOfAnger;
import me.neoblade298.neorogue.equipment.accessories.RingOfFortitude;
import me.neoblade298.neorogue.equipment.accessories.RingOfMentalism;
import me.neoblade298.neorogue.equipment.accessories.RingOfSharpness;
import me.neoblade298.neorogue.equipment.accessories.TopazRing;
import me.neoblade298.neorogue.equipment.armor.BlindingCloak;
import me.neoblade298.neorogue.equipment.armor.Brightcrown;
import me.neoblade298.neorogue.equipment.armor.ClothBindings;
import me.neoblade298.neorogue.equipment.armor.Footpads;
import me.neoblade298.neorogue.equipment.armor.Gauze;
import me.neoblade298.neorogue.equipment.armor.IronCuirass;
import me.neoblade298.neorogue.equipment.armor.LeatherArmguard;
import me.neoblade298.neorogue.equipment.armor.LeatherChestplate;
import me.neoblade298.neorogue.equipment.armor.LeatherCowl;
import me.neoblade298.neorogue.equipment.armor.LeatherHelmet;
import me.neoblade298.neorogue.equipment.armor.LeatherHood;
import me.neoblade298.neorogue.equipment.armor.LightningCloak;
import me.neoblade298.neorogue.equipment.armor.NullMagicMantle;
import me.neoblade298.neorogue.equipment.armor.SpikedPauldrons;
import me.neoblade298.neorogue.equipment.artifacts.AlchemistBag;
import me.neoblade298.neorogue.equipment.artifacts.AmuletOfOffering;
import me.neoblade298.neorogue.equipment.artifacts.AvalonianAnchor;
import me.neoblade298.neorogue.equipment.artifacts.AzureCutter;
import me.neoblade298.neorogue.equipment.artifacts.BloodyTrinket;
import me.neoblade298.neorogue.equipment.artifacts.Brightfeather;
import me.neoblade298.neorogue.equipment.artifacts.BurningCross;
import me.neoblade298.neorogue.equipment.artifacts.CharmOfGallus;
import me.neoblade298.neorogue.equipment.artifacts.ConcealingCloak;
import me.neoblade298.neorogue.equipment.artifacts.CrystallineFlask;
import me.neoblade298.neorogue.equipment.artifacts.EmeraldCluster;
import me.neoblade298.neorogue.equipment.artifacts.EmeraldGem;
import me.neoblade298.neorogue.equipment.artifacts.EmeraldShard;
import me.neoblade298.neorogue.equipment.artifacts.EnergyBattery;
import me.neoblade298.neorogue.equipment.artifacts.GlacialHammer;
import me.neoblade298.neorogue.equipment.artifacts.GoldIngot;
import me.neoblade298.neorogue.equipment.artifacts.GrendelsCrystalMirror;
import me.neoblade298.neorogue.equipment.artifacts.HermesBoots;
import me.neoblade298.neorogue.equipment.artifacts.HiddenBlade;
import me.neoblade298.neorogue.equipment.artifacts.ManaflowBand;
import me.neoblade298.neorogue.equipment.artifacts.MercenaryHeadband;
import me.neoblade298.neorogue.equipment.artifacts.NoxianBlight;
import me.neoblade298.neorogue.equipment.artifacts.NoxianSkull;
import me.neoblade298.neorogue.equipment.artifacts.OpalHourglass;
import me.neoblade298.neorogue.equipment.artifacts.PracticeDummy;
import me.neoblade298.neorogue.equipment.artifacts.RubyCluster;
import me.neoblade298.neorogue.equipment.artifacts.RubyGem;
import me.neoblade298.neorogue.equipment.artifacts.RubyShard;
import me.neoblade298.neorogue.equipment.artifacts.SapphireCluster;
import me.neoblade298.neorogue.equipment.artifacts.SapphireGem;
import me.neoblade298.neorogue.equipment.artifacts.SapphireShard;
import me.neoblade298.neorogue.equipment.artifacts.StaticNecklace;
import me.neoblade298.neorogue.equipment.artifacts.TomeOfWisdom;
import me.neoblade298.neorogue.equipment.consumables.MinorHealthPotion;
import me.neoblade298.neorogue.equipment.consumables.MinorMagicalPotion;
import me.neoblade298.neorogue.equipment.consumables.MinorManaPotion;
import me.neoblade298.neorogue.equipment.consumables.MinorPhysicalPotion;
import me.neoblade298.neorogue.equipment.consumables.MinorShieldsPotion;
import me.neoblade298.neorogue.equipment.consumables.MinorStaminaPotion;
import me.neoblade298.neorogue.equipment.cursed.CurseOfBurden;
import me.neoblade298.neorogue.equipment.cursed.CurseOfInexperience;
import me.neoblade298.neorogue.equipment.cursed.DullDagger;
import me.neoblade298.neorogue.equipment.cursed.GnarledWand;
import me.neoblade298.neorogue.equipment.cursed.MangledBow;
import me.neoblade298.neorogue.equipment.cursed.RustySword;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.equipment.offhands.ChasingDagger;
import me.neoblade298.neorogue.equipment.offhands.HastyShield;
import me.neoblade298.neorogue.equipment.offhands.InsanityPowder;
import me.neoblade298.neorogue.equipment.offhands.LeadingKnife;
import me.neoblade298.neorogue.equipment.offhands.LeatherBracer;
import me.neoblade298.neorogue.equipment.offhands.PaladinsShield;
import me.neoblade298.neorogue.equipment.offhands.PocketWatch;
import me.neoblade298.neorogue.equipment.offhands.PoisonPowder;
import me.neoblade298.neorogue.equipment.offhands.RubyArmament;
import me.neoblade298.neorogue.equipment.offhands.SmallShield;
import me.neoblade298.neorogue.equipment.offhands.SpikyShield;
import me.neoblade298.neorogue.equipment.offhands.WristBlade;
import me.neoblade298.neorogue.equipment.weapons.BasicBow;
import me.neoblade298.neorogue.equipment.weapons.BasicCrossbow;
import me.neoblade298.neorogue.equipment.weapons.BasicShotbow;
import me.neoblade298.neorogue.equipment.weapons.BoltWand;
import me.neoblade298.neorogue.equipment.weapons.ButterflyKnife;
import me.neoblade298.neorogue.equipment.weapons.ButterflyKnife2;
import me.neoblade298.neorogue.equipment.weapons.ChainLightningWand;
import me.neoblade298.neorogue.equipment.weapons.ColdArrow;
import me.neoblade298.neorogue.equipment.weapons.CrescentAxe;
import me.neoblade298.neorogue.equipment.weapons.CrimsonBlade;
import me.neoblade298.neorogue.equipment.weapons.CripplingFencingSword;
import me.neoblade298.neorogue.equipment.weapons.DarkScepter;
import me.neoblade298.neorogue.equipment.weapons.EarthStaff;
import me.neoblade298.neorogue.equipment.weapons.EarthenLeatherGauntlets;
import me.neoblade298.neorogue.equipment.weapons.ElectromagneticKnife;
import me.neoblade298.neorogue.equipment.weapons.EnergizedRazor;
import me.neoblade298.neorogue.equipment.weapons.EtherealKnife;
import me.neoblade298.neorogue.equipment.weapons.EvasiveKnife;
import me.neoblade298.neorogue.equipment.weapons.FencingSword;
import me.neoblade298.neorogue.equipment.weapons.FireStaff;
import me.neoblade298.neorogue.equipment.weapons.Flametongue;
import me.neoblade298.neorogue.equipment.weapons.ForcefulLeatherGauntlets;
import me.neoblade298.neorogue.equipment.weapons.Fracturer;
import me.neoblade298.neorogue.equipment.weapons.GlassArrow;
import me.neoblade298.neorogue.equipment.weapons.Harpoon;
import me.neoblade298.neorogue.equipment.weapons.HiddenRazor;
import me.neoblade298.neorogue.equipment.weapons.IceWand;
import me.neoblade298.neorogue.equipment.weapons.Irritant;
import me.neoblade298.neorogue.equipment.weapons.LeatherGauntlets;
import me.neoblade298.neorogue.equipment.weapons.LightLeatherGauntlets;
import me.neoblade298.neorogue.equipment.weapons.LightningCutter;
import me.neoblade298.neorogue.equipment.weapons.LightningWand;
import me.neoblade298.neorogue.equipment.weapons.LitArrow;
import me.neoblade298.neorogue.equipment.weapons.MassiveHalberd;
import me.neoblade298.neorogue.equipment.weapons.MirrorSickle;
import me.neoblade298.neorogue.equipment.weapons.Neoblade;
import me.neoblade298.neorogue.equipment.weapons.Nightmare;
import me.neoblade298.neorogue.equipment.weapons.Quickfire;
import me.neoblade298.neorogue.equipment.weapons.Rapier;
import me.neoblade298.neorogue.equipment.weapons.Razor;
import me.neoblade298.neorogue.equipment.weapons.RighteousHammer;
import me.neoblade298.neorogue.equipment.weapons.SerratedArrow;
import me.neoblade298.neorogue.equipment.weapons.SerratedRazor;
import me.neoblade298.neorogue.equipment.weapons.ShadowyDagger;
import me.neoblade298.neorogue.equipment.weapons.ShieldPike;
import me.neoblade298.neorogue.equipment.weapons.SilverFang;
import me.neoblade298.neorogue.equipment.weapons.SparkKnife;
import me.neoblade298.neorogue.equipment.weapons.SparkStick;
import me.neoblade298.neorogue.equipment.weapons.SparkdrainKnife;
import me.neoblade298.neorogue.equipment.weapons.StoneArrow;
import me.neoblade298.neorogue.equipment.weapons.StoneAxe;
import me.neoblade298.neorogue.equipment.weapons.StoneDagger;
import me.neoblade298.neorogue.equipment.weapons.StoneDriver;
import me.neoblade298.neorogue.equipment.weapons.StoneHammer;
import me.neoblade298.neorogue.equipment.weapons.StoneMace;
import me.neoblade298.neorogue.equipment.weapons.StoneShiv;
import me.neoblade298.neorogue.equipment.weapons.StoneSpear;
import me.neoblade298.neorogue.equipment.weapons.StoneSword;
import me.neoblade298.neorogue.equipment.weapons.StoneThrowingKnife;
import me.neoblade298.neorogue.equipment.weapons.TacticiansDagger;
import me.neoblade298.neorogue.equipment.weapons.TreeTrunk;
import me.neoblade298.neorogue.equipment.weapons.WoodenArrow;
import me.neoblade298.neorogue.equipment.weapons.WoodenDagger;
import me.neoblade298.neorogue.equipment.weapons.WoodenSword;
import me.neoblade298.neorogue.equipment.weapons.WoodenWand;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryIcon;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.WeaponSwingEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
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
	protected boolean isUpgraded, canDrop = true, isCursed;
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
			new Adrenaline(b);
			new AcidBomb(b);
			new Assassinate(b);
			new ArrowRain(b);
			new Atone(b);
			new Atrophy(b);
			new BasicFireMastery(b);
			new BasicIceMastery(b);
			new BattleCry(b);
			new BerserkersCall(b);
			new BitterCold(b);
			new BlessedEdge(b);
			new Blind(b);
			new Bloodlust(b);
			new Bide(b);
			new Brace(b);
			new Brace2(b);
			new BreakTheLine(b);
			new Brightcrown(b);
			new Bulldoze(b);
			new Bulwark(b);
			new Burst(b);
			new Challenge(b);
			new Charge(b);
			new Cleave(b);
			new Concoct(b);
			new ConfidenceKill(b);
			new Contaminate(b);
			new Cripple(b);
			new CripplingPoison(b);
			new CurseMark(b);
			new DarkLance(b);
			new Darkness(b);
			new DarkPact(b);
			new DarkPulse(b);
			new Deliberation(b);
			new Discipline(b);
			new Disorient(b);
			new DodgeRoll(b);
			new DoubleStrike(b);
			new EarthenTackle(b);
			new EtherealKnife(b);
			new Embolden(b);
			new EmpoweredEdge(b);
			new Endure(b);
			new Enlighten(b);
			new EndlessVenom(b);
			new Energize(b);
			new Envenom(b);
			new Envenom2(b);
			new EscapePlan(b);
			new EscapePlan2(b);
			new Execute(b);
			new ExploitWeakness(b);
			new Expunge(b);
			new Fade(b);
			new Farewell(b);
			new Ferocity(b);
			new Firebomb(b);
			new Finale(b);
			new Fissure(b);
			new FivePointStrike(b);
			new FlowState(b);
			new FlowState2(b);
			new Flurry(b);
			new FocusedShot(b);
			new FormAPlan(b);
			new Fortify(b);
			new Frenzy(b);
			new Fury(b);
			new GatheringShadows(b);
			new GatheringShadows2(b);
			new GetCentered(b);
			new GraniteShield(b);
			new Grit(b);
			new HoldTheLine(b);
			new InducePanic(b);
			new InducePanic2(b);
			new Initiator(b);
			new IronCuirass(b);
			new Ironskin(b);
			new LayTrap(b);
			new Lethality(b);
			new LightningRush(b);
			new Maim(b);
			new ManaInfusion(b);
			new MarkTarget(b);
			new MightySwing(b);
			new MortalEngine(b);
			new NightShade(b);
			new Overload(b);
			new Parry(b);
			new PartingGift(b);
			new PiercingShot(b);
			new PiercingVenom(b);
			new Pin(b);
			new Plague(b);
			new PoolOfLight(b);
			new Prayer(b);
			new Preparation(b);
			new Provoke(b);
			new RainOfSteel(b);
			new Revenge(b);
			new Roar(b);
			new Rushdown(b);
			new Quake(b);
			new QuickFeet(b);
			new Quickfire(b);
			new ShadowImbuement(b);
			new ShadowWalk(b);
			new Sidestep(b);
			new SilentSteps(b);
			new SilentSteps2(b);
			new SiphoningStrike(b);
			new Skirmisher(b);
			new Smite(b);
			new SmokeBomb(b);
			new SpikeTrap(b);
			new SpiritOfTheDragoon(b);
			new Sturdy(b);
			new FirstStrike(b);
			new Flicker(b);
			new Tackle(b);
			new ThrowPoison(b);
			new Thornguard(b);
			new Titan(b);
			new TreeTrunk(b);
			new TwinShiv(b);
			new Vanish(b);
			new VitalPierce(b);
			new WarCry(b);
			new Warmup(b);
			new WeaponEnchantmentElectrified(b);
			new WeaponEnchantmentHoly(b);
			new Windcutter(b);
			new WindSlash(b);

			// Accessories
			new EarthenRing(b);
			new GripGloves(b);
			new MinorManaRelic(b);
			new MinorPoisonRelic(b);
			new MinorShieldingRelic(b);
			new MinorStaminaRelic(b);
			new MinorStrengthRelic(b);
			new RingOfAnger(b);
			new RingOfFortitude(b);
			new RingOfMentalism(b);
			new RingOfSharpness(b);
			new TopazRing(b);

			// Armor
			new BlindingCloak(b);
			new ClothBindings(b);
			new Footpads(b);
			new Gauze(b);
			new LeatherArmguard(b);
			new LeatherCowl(b);
			new LeatherChestplate(b);
			new LeatherHelmet(b);
			new LeatherHood(b);
			new LightningCloak(b);
			new NullMagicMantle(b);
			new SpikedPauldrons(b);

			// Offhands
			new ChasingDagger(b);
			new HastyShield(b);
			new InsanityPowder(b);
			new LeadingKnife(b);
			new LeatherBracer(b);
			new PaladinsShield(b);
			new PocketWatch(b);
			new PoisonPowder(b);
			new RubyArmament(b);
			new SmallShield(b);
			new SpikyShield(b);
			new WristBlade(b);

			// Weapons
			new BasicBow(b);
			new BasicCrossbow(b);
			new BasicShotbow(b);
			new ColdArrow(b);
			new WoodenArrow(b);
			new BoltWand(b);
			new ButterflyKnife(b);
			new ButterflyKnife2(b);
			new ChainLightningWand(b);
			new CrescentAxe(b);
			new CrimsonBlade(b);
			new CripplingFencingSword(b);
			new DarkScepter(b);
			new EarthStaff(b);
			new EarthenLeatherGauntlets(b);
			new ElectromagneticKnife(b);
			new EnergizedRazor(b);
			new EvasiveKnife(b);
			new FencingSword(b);
			new FireStaff(b);
			new Flametongue(b);
			new ForcefulLeatherGauntlets(b);
			new Fracturer(b);
			new GlassArrow(b);
			new Harpoon(b);
			new HiddenRazor(b);
			new IceWand(b);
			new Irritant(b);
			new LeatherGauntlets(b);
			new LightLeatherGauntlets(b);
			new LightningCutter(b);
			new LightningWand(b);
			new LitArrow(b);
			new MassiveHalberd(b);
			new MirrorSickle(b);
			new Nightmare(b);
			new Rapier(b);
			new Razor(b);
			new SerratedArrow(b);
			new SerratedRazor(b);
			new SilverFang(b);
			new ShadowyDagger(b);
			new ShieldPike(b);
			new SparkdrainKnife(b);
			new SparkKnife(b);
			new SparkStick(b);
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
			new TacticiansDagger(b);
			new TargetAcquisition(b);
			new UnderDarkness(b);
			new RighteousHammer(b);
			new WoodenDagger(b);
			new WoodenSword(b);
			new WoodenWand(b);
			
			new Neoblade(b);

			// Consumables
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
		new AvalonianAnchor();
		new AzureCutter();
		new BloodyTrinket();
		new BurningCross();
		new Brightfeather();
		new CharmOfGallus();
		new ConcealingCloak();
		new CrystallineFlask();
		new EnergyBattery();
		new GlacialHammer();
		new GoldIngot();
		new GrendelsCrystalMirror();
		new HermesBoots();
		new HiddenBlade();
		new PracticeDummy();
		new ManaflowBand();
		new MercenaryHeadband();
		new NoxianBlight();
		new NoxianSkull();
		new OpalHourglass();
		new StaticNecklace();
		new TomeOfWisdom();

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
		new CurseOfBurden();
		new CurseOfInexperience();
		new DullDagger();
		new GnarledWand();
		new MangledBow();
		new RustySword();

		// Materials

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
			}
			else {
				counts.put(eq.getType(), counts.get(eq.getType()) + 1);
			}
		}
		
		for (Equipment eq : equipment.values()) {
			eq.setupDroptable();
			eq.postSetup();
			Equipment up = eq.getUpgraded();
			if (up != null) {
				up.setupDroptable();
				up.postSetup();
			}
		}

		for (EquipmentType type : EquipmentType.values()) {
			Bukkit.getLogger().info("[NeoRogue] Loaded " + counts.get(type) + " " + type.getDisplay());
		}
		Bukkit.getLogger().info("[NeoRogue] Loaded " + cursed + " Cursed");
	}

	public Equipment(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec,
			EquipmentType type, EquipmentProperties props) {
		this(id, display, isUpgraded, rarity, new EquipmentClass[] {ec}, type, props);
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
		this(id, display, isUpgraded, rarity, new EquipmentClass[] {ec}, type, EquipmentProperties.none());
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
		this.ecs = new EquipmentClass[] {EquipmentClass.CLASSLESS};
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
		this.ecs = new EquipmentClass[] {ec};
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
	public void postSetup() {} // Basically only used for RustySword glossary tag atm
	public void setupReforges() {}

	private void setup() {
		if (isUpgraded) {
			Equipment base = getUnupgraded();
			reforgeParents = base.getReforgeParents();
			reforgeOptions = base.getReforgeOptions();
		}
		setupReforges();
		setupItem();
	}

	public boolean canUpgrade() {
		return upgraded.containsKey(id);
	}

	private void setupDroptable() {
		int value = rarity.getValue() + (isUpgraded ? 1 : 0);
		if (!canDrop) return;
		if (!reforgeParents.isEmpty()) return;

		// Artifacts and consumables get their own special droptable with special weight
		// due to reduced amount
		if (this instanceof Artifact) {
			artifacts.addLenientWeight(ecs, value, (Artifact) this);
		}
		else if (this instanceof Consumable) {
			consumables.addLenientWeight(ecs, value, (Consumable) this);
		}
		else {
			droptables.add(ecs, value, this);
		}
	}

	public void addTags(GlossaryTag... tags) {
		for (GlossaryTag tag : tags) {
			this.tags.add(tag);
		}
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
		if (str.isBlank()) return null;
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
		for (int i = 0; i < separated.length; i++) {
			if (str.isBlank()) continue;
			arr[i] = Equipment.deserialize(separated[i]);
		}
		return arr;
	}

	public static ArrayList<Equipment> deserializeAsArrayList(String str) {
		if (str.isBlank()) return new ArrayList<Equipment>();
		String[] separated = str.split(";");
		ArrayList<Equipment> arr = new ArrayList<Equipment>(separated.length);
		for (String s : separated) {
			if (str.isBlank()) continue;
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
					Bukkit.getLogger().warning("[NeoRogue] Duplicate space found in equipment " + id + (isUpgraded ? "+" : ""));
				}
				prev = c;
			}
		}
		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();

		meta.displayName(display.decoration(TextDecoration.ITALIC, State.FALSE));
		ArrayList<Component> loreItalicized = new ArrayList<Component>();
		if (isCursed) {
			loreItalicized.add(Component.text("Cursed " + type.getDisplay(), NamedTextColor.RED));
		}
		else {
			loreItalicized.add(rarity.getDisplay(true).append(Component.text(" " + type.getDisplay())));
		}
		loreItalicized.addAll(properties.generateLore(this));
		if (preLoreLine != null) {
			for (String l : preLoreLine) {
				loreItalicized.add(NeoCore.miniMessage().deserialize(l));
			}
		}
		if (isCursed) {
			loreItalicized.add(
					Component.text("This item is cursed. It must be equipped to continue.", NamedTextColor.DARK_RED));
		}
		if (!reforgeOptions.isEmpty()) {
			loreItalicized.add(Component.text("Reforgeable with:", NamedTextColor.GOLD));
			for (Equipment eq : reforgeOptions.keySet()) {
				boolean noPostfix = isCursed || eq.isCursed;
				String postfix = "";
				if (!noPostfix) {
					postfix = isUpgraded ? "(+)" : "+";
				}
				loreItalicized.add(Component.text("- ", NamedTextColor.GOLD)
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

		if (isUpgraded) {
			meta.addEnchant(Enchantment.LUCK, 1, true);
		}

		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addItemFlags(ItemFlag.HIDE_DYE);
		meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
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
			if (option.id.equals(id)) return true;
		}
		return false;
	}

	// Should only ever be called with unupgraded parameters
	protected void addReforge(Equipment combineWith, Equipment... options) {
		if (!isUpgraded) {
			// For every option, add the components as parents and place options
			for (int i = 0; i < options.length; i++) {
				options[i].addReforgeParent(combineWith);
				if (this != combineWith) options[i].addReforgeParent(this);
			}

			// Set item into reforge options
			this.reforgeOptions.put(combineWith, options);
			if (this != combineWith) combineWith.reforgeOptions.put(this, options);
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
		if (!(o instanceof Equipment)) return false;
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

	public boolean canUseWeapon(PlayerFightData data) {
		if (data.getMana() < properties.get(PropertyType.MANA_COST)) {
			Util.displayError(data.getPlayer(), "Not enough mana!");
			return false;
		}

		if (data.getStamina() < properties.get(PropertyType.STAMINA_COST)) {
			Util.displayError(data.getPlayer(), "Not enough stamina!");
			return false;
		}
		return true;
	}

	public void weaponSwing(Player p, PlayerFightData data) {
		weaponSwing(p, data, properties.get(PropertyType.ATTACK_SPEED));
	}

	public void weaponSwing(Player p, PlayerFightData data, double attackSpeed) {
		if (properties.has(PropertyType.MANA_COST)) data.addMana(-properties.get(PropertyType.MANA_COST));
		if (properties.has(PropertyType.STAMINA_COST)) data.addStamina(-properties.get(PropertyType.STAMINA_COST));
		properties.getSwingSound().play(p, p);
		WeaponSwingEvent ev = new WeaponSwingEvent(this, attackSpeed);
		FightInstance.trigger(p, Trigger.WEAPON_SWING, ev);
		data.setBasicAttackCooldown(type.getSlots()[0], ev.getAttackSpeedBuff().apply(attackSpeed));
		if (type.getSlots()[0] == EquipSlot.OFFHAND) p.swingOffHand();
	}

	public void weaponSwingAndDamage(Player p, PlayerFightData data, LivingEntity target) {
		weaponSwing(p, data);
		weaponDamage(p, data, target);
	}

	public void weaponSwingAndDamage(Player p, PlayerFightData data, LivingEntity target, DamageMeta dm) {
		weaponSwing(p, data);
		weaponDamage(p, data, target, dm);
	}

	public void weaponSwingAndDamage(Player p, PlayerFightData data, LivingEntity target, double damage) {
		weaponSwing(p, data);
		weaponDamage(p, data, target, damage);
	}

	public void weaponDamage(Player p, PlayerFightData data, LivingEntity target) {
		weaponDamage(p, data, target, properties.get(PropertyType.DAMAGE), properties.get(PropertyType.KNOCKBACK));
	}

	public void weaponDamage(Player p, PlayerFightData data, LivingEntity target, double damage) {
		weaponDamage(p, data, target, damage, properties.get(PropertyType.KNOCKBACK));
	}

	public void weaponDamage(Player p, PlayerFightData data, LivingEntity target, double damage, double knockback) {
		DamageMeta dm = new DamageMeta(data, damage, properties.getType());
		BasicAttackEvent ev = new BasicAttackEvent(target, dm, knockback, this, null);
		data.runActions(data, Trigger.BASIC_ATTACK, ev);
		if (knockback != 0) {
			FightInstance.knockback(p, target, knockback);
		}
		FightInstance.dealDamage(dm, target);
	}

	public void weaponDamage(Player p, PlayerFightData data, LivingEntity target, DamageMeta dm) {
		double knockback = properties.get(PropertyType.KNOCKBACK);
		BasicAttackEvent ev = new BasicAttackEvent(target, dm, knockback, this, null);
		data.runActions(data, Trigger.BASIC_ATTACK, ev);
		if (knockback != 0) {
			FightInstance.knockback(p, target, knockback);
		}
		FightInstance.dealDamage(dm, target);
	}

	public void weaponDamageProjectile(LivingEntity target, ProjectileInstance proj) {
		weaponDamageProjectile(target, proj, null, true);
	}

	public void weaponDamageProjectile(LivingEntity target, ProjectileInstance proj, Barrier hitBarrier) {
		weaponDamageProjectile(target, proj, hitBarrier, true);
	}

	public void weaponDamageProjectile(LivingEntity target, ProjectileInstance proj, Barrier hitBarrier, boolean basicAttack) {
		weaponDamageProjectile(target, proj, hitBarrier, true, properties.get(PropertyType.KNOCKBACK));
	}

	public void weaponDamageProjectile(LivingEntity target, ProjectileInstance proj, Barrier hitBarrier, boolean basicAttack, double knockback) {
		PlayerFightData data = (PlayerFightData) proj.getOwner();
		DamageMeta dm = new DamageMeta(data, properties.get(PropertyType.DAMAGE), properties.getType(), DamageOrigin.PROJECTILE, proj);
		if (!proj.getBuffs().isEmpty()) {
			dm.addBuffs(proj.getBuffs(), BuffOrigin.PROJECTILE, true);
		}
		if (hitBarrier != null) {
			dm.addBuffs(hitBarrier.getBuffs(), BuffOrigin.BARRIER, false);
		}
		
		if (basicAttack) {
			BasicAttackEvent ev = new BasicAttackEvent(target, dm, knockback, this, null);
			data.runActions(data, Trigger.BASIC_ATTACK, ev);
		}
		if (properties.contains(PropertyType.KNOCKBACK)) {
			FightInstance.knockback(target,
					proj.getVelocity().normalize().multiply(knockback));
		}
		FightInstance.dealDamage(dm, target);
	}
	
	public void weaponDamageProjectile(LivingEntity target, ProjectileInstance proj, DamageMeta dm, Barrier hitBarrier) {
		weaponDamageProjectile(target, proj, dm, hitBarrier, true);
	}

	public void weaponDamageProjectile(LivingEntity target, ProjectileInstance proj, DamageMeta dm, Barrier hitBarrier, boolean basicAttack) {
		PlayerFightData data = (PlayerFightData) proj.getOwner();
		if (!proj.getBuffs().isEmpty()) {
			dm.addBuffs(proj.getBuffs(), BuffOrigin.PROJECTILE, true);
		}
		if (hitBarrier != null) {
			dm.addBuffs(hitBarrier.getBuffs(), BuffOrigin.BARRIER, false);
		}
		if (basicAttack) {
			BasicAttackEvent ev = new BasicAttackEvent(target, dm, properties.get(PropertyType.KNOCKBACK), this, null);
			data.runActions(data, Trigger.BASIC_ATTACK, ev);
		}
		if (properties.contains(PropertyType.KNOCKBACK)) {
			FightInstance.knockback(target,
					proj.getVelocity().normalize().multiply(properties.get(PropertyType.KNOCKBACK)));
		}
		FightInstance.dealDamage(dm, target);
	}

	public void damageProjectile(LivingEntity target, ProjectileInstance proj) {
		damageProjectile(target, proj, null);
	}

	public void damageProjectile(LivingEntity target, ProjectileInstance proj, Barrier hitBarrier) {
		DamageMeta meta = proj.getMeta();
		if (!proj.getBuffs().isEmpty()) {
			meta.addBuffs(proj.getBuffs(), BuffOrigin.PROJECTILE, true);
		}
		if (hitBarrier != null) {
			meta.addBuffs(hitBarrier.getBuffs(), BuffOrigin.BARRIER, false);
		}
		FightInstance.dealDamage(meta, target);
	}

	public boolean isCursed() {
		return isCursed;
	}
	
	// Only happens for curses at shops
	public void onPurify(PlayerSessionData data) {}

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
				if (slot == es) return true;
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
	}

	public static class DropTableSet<E> {
		protected HashMap<EquipmentClass, HashMap<Integer, DropTable<E>>> droptables = new HashMap<EquipmentClass, HashMap<Integer, DropTable<E>>>();

		public DropTableSet() {
			reload();
		}

		private DropTableSet(DropTableSet<E> original, EquipmentClass... ecs) {
			for (EquipmentClass ec : ecs) {
				if (!original.droptables.containsKey(ec)) continue;
				HashMap<Integer, DropTable<E>> map = new HashMap<Integer, DropTable<E>>();
				for (Entry<Integer, DropTable<E>> ent2 : original.droptables.get(ec).entrySet()) {
					map.put(ent2.getKey(), ent2.getValue().clone());
				}
				droptables.put(ec, map);
			}
		}

		public DropTableSet<E> clone(EquipmentClass... ecs) {
			return new DropTableSet<E>(this, ecs);
		}

		public void reload() {
			for (EquipmentClass ec : EquipmentClass.values()) {
				HashMap<Integer, DropTable<E>> tables = new HashMap<Integer, DropTable<E>>();
				for (int i = 0; i < 10; i++) {
					tables.put(i, new DropTable<E>());
				}
				droptables.put(ec, tables);
			}
		}

		public void remove(E drop) {
			for (HashMap<Integer, DropTable<E>> map : droptables.values()) {
				for (DropTable<E> table : map.values()) {
					table.remove(drop);
				}
			}
		}

		public void add(EquipmentClass[] ecs, int value, E drop) {
			for (EquipmentClass ec : ecs) {
				HashMap<Integer, DropTable<E>> table = droptables.get(ec);
				if (value >= 2) {
					table.get(value - 2).add(drop, 1);
				}
				if (value >= 1) {
					table.get(value - 1).add(drop, 8);
				}
				table.get(value).add(drop, 32);
				table.get(value + 1).add(drop, 8);
				table.get(value + 2).add(drop, 1);
			}
		}

		public void addLenientWeight(EquipmentClass[] ecs, int value, E drop) {
			for (EquipmentClass ec : ecs) {
				HashMap<Integer, DropTable<E>> table = droptables.get(ec);
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

		public ArrayList<E> getMultiple(int value, int numDrops, EquipmentClass... ec) {
			ArrayList<E> list = new ArrayList<E>();
			DropTable<E> table;
			for (int i = 0; i < numDrops; i++) {
				if (ec.length > 1) {
					DropTable<DropTable<E>> tables = new DropTable<DropTable<E>>();
					for (int j = 0; j < ec.length; j++) {
						DropTable<E> temp = droptables.get(ec[j]).get(value);
						tables.add(temp, temp.getTotalWeight());
					}
					table = tables.get();
				}
				else {
					table = droptables.get(ec[0]).get(value);
				}

				list.add(table.get());
			}
			return list;
		}

		@Override
		public String toString() {
			return droptables.toString();
		}
	}

	@Override
	public int compareTo(Equipment o) {
		int comp = this.id.compareTo(o.id);
		if (comp != 0) return comp;
		return Boolean.compare(this.isUpgraded, o.isUpgraded);
	}

	public static Set<String> getEquipmentIds() {
		return equipment.keySet();
	}

	public static Collection<Equipment> getAll() {
		return equipment.values();
	}
}
