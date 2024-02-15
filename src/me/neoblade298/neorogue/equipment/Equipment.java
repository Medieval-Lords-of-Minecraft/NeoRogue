package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
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
import me.neoblade298.neocore.shared.droptables.DropTable;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.abilities.*;
import me.neoblade298.neorogue.equipment.accessories.*;
import me.neoblade298.neorogue.equipment.armor.*;
import me.neoblade298.neorogue.equipment.artifacts.*;
import me.neoblade298.neorogue.equipment.offhands.*;
import me.neoblade298.neorogue.equipment.weapons.*;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.equipment.cursed.*;
import me.neoblade298.neorogue.equipment.consumables.*;
import me.neoblade298.neorogue.equipment.materials.*;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public abstract class Equipment implements Comparable<Equipment> {
	private static HashMap<String, Equipment> equipment = new HashMap<String, Equipment>();
	private static HashMap<String, Equipment> upgraded = new HashMap<String, Equipment>();
	private static DropTableSet<Equipment> droptables = new DropTableSet<Equipment>();
	private static DropTableSet<Artifact> artifacts = new DropTableSet<Artifact>();
	private static DropTableSet<Consumable> consumables = new DropTableSet<Consumable>();

	private TreeMap<Equipment, Equipment[]> reforgeOptions = new TreeMap<Equipment, Equipment[]>();

	protected String id;
	protected Component display;
	protected boolean isUpgraded, canDrop = true, isCursed, isReforged;
	protected ItemStack item;
	protected Rarity rarity;
	protected EquipmentClass ec;
	protected EquipmentType type;
	protected EquipmentProperties properties;
	protected int cooldown = 0;
	protected TreeSet<GlossaryTag> tags = new TreeSet<GlossaryTag>();

	public static void load() {
		equipment.clear();
		upgraded.clear();
		droptables.reload();
		artifacts.reload();
		for (boolean b : new boolean[] { false, true }) {
			// Abilities
			new Adrenaline(b).addSelfReforge(new Burst(b), new Discipline(b), new Ferocity(b));
			new BattleCry(b).addSelfReforge(new WarCry(b), new BerserkersCall(b));
			new Brace(b).addSelfReforge(new Brace2(b), new Parry(b), new Bide(b)).addReforge(new Provoke(b),
					new Challenge(b));
			new Cleave(b).addSelfReforge(new Quake(b), new Smite(b), new WindSlash(b));
			new DarkPact(b);
			new EmpoweredEdge(b).addSelfReforge(new RecklessSwing(b), new BlessedEdge(b), new Fury(b));
			new Execute(b).addSelfReforge(new SiphoningStrike(b), new MightySwing(b), new Fortify(b));
			new Sturdy(b).addSelfReforge(new GraniteShield(b), new Bulwark(b), new Endurance(b));
			new Tackle(b).addSelfReforge(new EarthenTackle(b), new Bulldoze(b), new Pin(b));
			new Thornguard(b);
			new Titan(b);

			// Accessories
			new EarthenRing(b);
			new GripGloves(b);
			new MinorShieldingRelic(b);
			new MinorStaminaRelic(b);
			new MinorStrengthRelic(b);
			new RingOfAnger(b);

			// Armor
			new ClothBindings(b);
			new Footpads(b);
			new LeatherChestplate(b);
			new LeatherHelmet(b);
			new NullMagicMantle(b);
			new SpikedPauldrons(b);

			// Offhands
			new ChasingDagger(b);
			new LeatherBracer(b);
			new SmallShield(b).addSelfReforge(new HastyShield(b), new SpikyShield(b));
			new TowerShield(b).addSelfReforge(new CaptainsTowerShield(b));
			new WristBlade(b);

			// Weapons
			new FencingSword(b).addSelfReforge(new Rapier(b), new SerratedFencingSword(b));
			new LeatherGauntlets(b).addSelfReforge(new ForcefulLeatherGauntlets(b), new LightLeatherGauntlets(b),
					new EarthenLeatherGauntlets(b));
			new StoneHammer(b);
			new WoodenSword(b).addSelfReforge(new StoneSword(b), new StoneSpear(b), new StoneAxe(b));
			new WoodenWand(b);

			// Consumables
			new MinorHealthPotion(b);
			new MinorStaminaPotion(b);
			new MinorManaPotion(b);
			new MinorShieldsPotion(b);
			new MinorPhysicalPotion(b);
			new MinorMagicalPotion(b);
		}

		// Artifacts
		new AvalonianAnchor();
		new BurningCross();
		new CharmOfGallus();
		new EnergyBattery();
		new FaerieCirclet();
		new GlacialHammer();
		new GrendelsCrystalMirror();
		new PracticeDummy();
		new TomeOfWisdom();
		new MercenaryHeadband();

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
		new CurseOfInexperience();
		new DullDagger();
		new GnarledWand();
		new MangledBow();
		new RustySword();

		// Materials
		new DullGem();

		HashMap<EquipmentType, Integer> counts = new HashMap<EquipmentType, Integer>();
		for (EquipmentType type : EquipmentType.values()) {
			counts.put(type, 0);
		}

		// Setup equipment
		for (Equipment eq : equipment.values()) {
			eq.setupDroptable();
			eq.setupItem();
			counts.put(eq.getType(), counts.get(eq.getType()) + 1);
			Equipment up = eq.getUpgraded();
			if (up != null) {
				up.setupDroptable();
				up.setupItem();
			}
		}

		for (EquipmentType type : EquipmentType.values()) {
			Bukkit.getLogger().info("[NeoRogue] Loaded " + counts.get(type) + " " + type.getDisplay());
		}
	}

	public Equipment(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec,
			EquipmentType type, EquipmentProperties props) {
		this.id = id;
		this.rarity = rarity;
		this.isUpgraded = isUpgraded;
		this.ec = ec;
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
		this(id, display, isUpgraded, rarity, ec, type, EquipmentProperties.none());
	}

	// For curses
	public Equipment(String id, String display, EquipmentType type) {
		this.id = id;
		this.rarity = Rarity.COMMON;
		this.isUpgraded = false;
		this.ec = EquipmentClass.CLASSLESS;
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
		this.ec = ec;
		this.type = EquipmentType.MATERIAL;
		this.display = rarity.applyDecorations(SharedUtil.color(display));
		this.properties = EquipmentProperties.none();
		this.canDrop = false;

		if (equipment.containsKey(id)) {
			Bukkit.getLogger().warning("[NeoRogue] Duplicate id of " + id + " found while loading equipment");
		}

		equipment.put(id, this);
	}

	public EquipmentProperties getProperties() {
		return properties;
	}

	public abstract void setupItem();

	public boolean hasUpgrade() {
		return upgraded.containsKey(id);
	}

	public void setupDroptable() {
		int value = rarity.getValue() + (isUpgraded ? 1 : 0);
		if (!canDrop) return;
		if (isReforged) return;

		// Artifacts and consumables get their own special droptable with special weight
		// due to reduced amount
		if (this instanceof Artifact) {
			artifacts.addLenientWeight(ec, value, (Artifact) this);
		}
		else if (this instanceof Consumable) {
			consumables.addLenientWeight(ec, value, (Consumable) this);
		}
		else {
			droptables.add(ec, value, this);
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

	public TreeSet<GlossaryTag> getTags() {
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
			arr[i] = (Equipment) Equipment.deserialize(separated[i]);
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
			loreItalicized
					.add(Component.text("This item is cursed. It must be equipped continue.", NamedTextColor.DARK_RED));
		}
		if (!reforgeOptions.isEmpty()) {
			loreItalicized.add(Component.text("Reforgeable with:", NamedTextColor.GOLD));
			for (Equipment eq : reforgeOptions.keySet()) {
				loreItalicized.add(Component.text("- ", NamedTextColor.GOLD)
						.append(eq.getDisplay().append(Component.text(isUpgraded ? "(+)" : "+"))));
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
		meta.setUnbreakable(true);
		properties.modifyItemMeta(item, meta);
		item.setItemMeta(meta);

		NBTItem nbti = new NBTItem(item);
		nbti.setString("equipId", id);
		nbti.setString("type", type.getDisplay());
		nbti.setBoolean("isUpgraded", isUpgraded);
		return nbti.getItem();
	}

	public TreeMap<Equipment, Equipment[]> getReforgeOptions() {
		return reforgeOptions;
	}

	public boolean containsReforgeOption(String id) {
		for (Equipment option : reforgeOptions.keySet()) {
			if (option.id.equals(id)) return true;
		}
		return false;
	}

	protected Equipment addReforge(Equipment combineWith, Equipment... options) {
		Equipment[] unupgraded = new Equipment[options.length];
		for (int i = 0; i < options.length; i++) {
			options[i].setReforged();
			unupgraded[i] = options[i].getUnupgraded();
		}
		this.reforgeOptions.put(combineWith.getUnupgraded(), unupgraded);
		if (!this.id.equals(combineWith.id)) {
			combineWith.reforgeOptions.put(this.getUnupgraded(), unupgraded);
		}
		return this;
	}

	protected Equipment addSelfReforge(Equipment... options) {
		return addReforge(this.getUnupgraded(), options);
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

	private void setReforged() {
		this.isReforged = true;
	}

	public Component getDisplay() {
		return display;
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

	// Used for weapons that start cooldown on swing, not hit
	public void weaponSwing(Player p, PlayerFightData data) {
		properties.getSwingSound().play(p);
		data.setBasicAttackCooldown(type.getSlots()[0], properties);
		if (type.getSlots()[0] == EquipSlot.OFFHAND) p.swingOffHand();
	}

	// Both swings and hits enemy
	public void weaponSwingAndDamage(Player p, PlayerFightData data, LivingEntity target) {
		weaponSwing(p, data);
		weaponDamage(p, data, target);
	}

	// Both swings and hits enemy
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
		FightInstance.dealDamage(dm, target);
		if (knockback != 0) {
			FightInstance.knockback(p, target, knockback);
		}
	}

	public void weaponDamageProjectile(LivingEntity target, ProjectileInstance proj) {
		weaponDamageProjectile(target, proj, null);
	}

	public void weaponDamageProjectile(LivingEntity target, ProjectileInstance proj, Barrier hitBarrier) {
		PlayerFightData data = (PlayerFightData) proj.getOwner();
		DamageMeta dm = new DamageMeta(data, properties.get(PropertyType.DAMAGE), properties.getType());
		if (!proj.getBuffs().isEmpty()) {
			dm.addBuffs(proj.getBuffs(), BuffOrigin.PROJECTILE, true);
		}
		if (hitBarrier != null) {
			dm.addBuffs(hitBarrier.getBuffs(), BuffOrigin.BARRIER, false);
		}
		BasicAttackEvent ev = new BasicAttackEvent(target, dm, properties.get(PropertyType.KNOCKBACK), this, null);
		data.runActions(data, Trigger.BASIC_ATTACK, ev);
		FightInstance.dealDamage(dm, target);
		if (properties.contains(PropertyType.KNOCKBACK)) {
			FightInstance.knockback(proj.getVector(), target, properties.get(PropertyType.KNOCKBACK));
		}
	}

	public void damageProjectile(LivingEntity target, ProjectileInstance proj, DamageMeta meta) {
		damageProjectile(target, proj, meta, null);
	}

	public void damageProjectile(LivingEntity target, ProjectileInstance proj, DamageMeta meta, Barrier hitBarrier) {
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

		public void add(EquipmentClass ec, int value, E drop) {
			HashMap<Integer, DropTable<E>> table = droptables.get(ec);
			if (value >= 2) {
				table.get(value - 2).add(drop, 1);
			}
			if (value >= 1) {
				table.get(value - 1).add(drop, 3);
			}
			table.get(value).add(drop, 8);
		}

		public void addLenientWeight(EquipmentClass ec, int value, E drop) {
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
}
