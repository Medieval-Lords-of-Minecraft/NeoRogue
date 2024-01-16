package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.shared.droptables.DropTable;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.equipment.abilities.*;
import me.neoblade298.neorogue.equipment.accessories.*;
import me.neoblade298.neorogue.equipment.armor.*;
import me.neoblade298.neorogue.equipment.artifacts.*;
import me.neoblade298.neorogue.equipment.offhands.*;
import me.neoblade298.neorogue.equipment.weapons.*;
import me.neoblade298.neorogue.equipment.cursed.*;
import me.neoblade298.neorogue.equipment.consumables.*;
import me.neoblade298.neorogue.equipment.materials.*;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public abstract class Equipment {
	private static HashMap<String, Equipment> equipment = new HashMap<String, Equipment>();
	private static HashMap<String, Equipment> upgraded = new HashMap<String, Equipment>();
	private static HashSet<String> reforged = new HashSet<String>();
	private static DropTableSet<Equipment> droptables = new DropTableSet<Equipment>();
	private static DropTableSet<Artifact> artifactTables = new DropTableSet<Artifact>();
	
	private TreeMap<String, String[]> reforgeOptions = new TreeMap<String, String[]>();
	
	protected String id;
	protected Component display;
	protected boolean isUpgraded, canDrop = true, isCursed;
	protected ItemStack item;
	protected Rarity rarity;
	protected EquipmentClass ec;
	protected EquipmentType type;
	protected EquipmentProperties properties;
	protected int cooldown = 0;
	
	public static void load() {
		equipment.clear();
		upgraded.clear();
		droptables.reload();
		artifactTables.reload();
		
		for (boolean b : new boolean[] {false, true}) {
			// Abilities
			new BattleCry(b);
			new DarkPact(b);
			new Bide(b);
			new BlessedEdge(b);
			new Brace(b);
			new Brace2(b);
			new Bulldoze(b);
			new EarthenTackle(b);
			new EarthenWall(b);
			new EmpoweredEdge(b);
			new Fury(b);
			new Glare(b);
			new Parry(b);
			new Provoke(b);
			new RecklessSwing(b);
			new SavageCry(b);
			new Tackle(b);
			
			// Accessories
			new EarthenRing(b);
			new MinorStaminaRelic(b);
			new MinorStrengthRelic(b);
			
			// Armor
			new ClothBindings(b);
			new Footpads(b);
			new LeatherChestplate(b);
			new LeatherHelmet(b);
			new NullMagicMantle(b);
			new SpikedPauldrons(b);
			
			// Offhands
			new CaptainsTowerShield(b);
			new ChasingDagger(b);
			new HastyShield(b);
			new LeatherBracer(b);
			new SmallShield(b);
			new SpikyShield(b);
			new TowerShield(b);
			new WristBlade(b);
			
			// Weapons
			new EarthenLeatherGauntlets(b);
			new FencingSword(b);
			new ForcefulLeatherGauntlets(b);
			new LeatherGauntlets(b);
			new LightLeatherGauntlets(b);
			new Rapier(b);
			new SerratedFencingSword(b);
			new StoneAxe(b);
			new StoneDagger(b);
			new StoneHammer(b);
			new StoneSword(b);
			new WoodenSword(b);
			new WoodenWand(b);
			
			// Consumables
			new MinorHealthPotion(b);
			new MinorStaminaPotion(b);
			new MinorManaPotion(b);
		}
		
		// Artifacts
		new BurningCross();
		new CharmOfGallus();
		new EmeraldCluster();
		new EmeraldGem();
		new EmeraldShard();
		new EnergyBattery();
		new FaerieCirclet();
		new PracticeDummy();
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
		
		// Check for missing reforge items
		for (String id : reforged) {
			if (!equipment.containsKey(id)) {
				Bukkit.getLogger().warning("[NeoRogue] Reforged item id " + id + " was not found in equipment list");
			}
		}
		
		for (Equipment eq : equipment.values()) {
			eq.setupDroptable();
			eq.setupItem();
			Equipment up = eq.getUpgraded();
			if (up != null) {
				up.setupDroptable();
				up.setupItem();
			}
		}
	}
	
	public Equipment(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec, EquipmentType type, EquipmentProperties props) {
		this.id = id;
		this.rarity = rarity;
		this.isUpgraded = isUpgraded;
		this.ec = ec;
		this.type = type;
		this.properties = props;
		
		// Just make sure not to close any of the tags in display string or the upgraded sign will break it
		this.display = rarity.applyDecorations(SharedUtil.color(display + (isUpgraded ? "+" : "")));
		

		if (equipment.containsKey(id) && !isUpgraded) {
			Bukkit.getLogger().warning("[NeoRogue] Duplicate id of " + id + " found while loading equipment");
		}
		
		if (isUpgraded) upgraded.put(id, this);
		else equipment.put(id, this);
	}
	
	public Equipment(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec, EquipmentType type) {
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
	
	public void setupDroptable() {
		int value = rarity.getValue() + (isUpgraded ? 1 : 0);
		if (!canDrop) return;
		if (reforged.contains(id)) return;
		
		// Artifacts get their own special droptable with special weight due to reduced amount
		if (this instanceof Artifact) {
			artifactTables.addLenientWeight(ec, value, (Artifact) this);
		}
		else {
			droptables.add(ec, value, this);
		}
	}
	
	// Run at the start of a fight to initialize Fight Data
	public abstract void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot);
	
	// Run at the end of a fight if needed
	public void cleanup(Player p, PlayerFightData data) {
		
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
		loreItalicized.addAll(properties.generateLore());
		if (preLoreLine != null) {
			for (String l : preLoreLine) {
				loreItalicized.add(NeoCore.miniMessage().deserialize(l));
			}
		}
		if (isCursed) {
			loreItalicized.add(Component.text("This item is cursed. It must be equipped continue.", NamedTextColor.DARK_RED));
		}
		if (!reforgeOptions.isEmpty()) {
			loreItalicized.add(Component.text("Reforgeable with:" , NamedTextColor.GOLD));
			for (String id : reforgeOptions.keySet()) {
				loreItalicized.add(Component.text("- ", NamedTextColor.GOLD).append(Equipment.get(id, false).getDisplay()));
			}
		}
		if (loreLine != null) {
			for (TextComponent tc : SharedUtil.addLineBreaks((TextComponent) NeoCore.miniMessage().deserialize(loreLine).colorIfAbsent(NamedTextColor.GRAY), 200)) {
				loreItalicized.add(tc);
			}
		}
		ArrayList<Component> lore = new ArrayList<Component>(loreItalicized.size());
		for (Component c : loreItalicized) {
			lore.add(c.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		}
		meta.lore(lore);
		
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		meta.setUnbreakable(true);
		properties.modifyItemMeta(item, meta);
		item.setItemMeta(meta);
		
		NBTItem nbti = new NBTItem(item);
		nbti.setString("equipId", id);
		nbti.setString("type", type.getDisplay());
		nbti.setBoolean("isUpgraded", isUpgraded);
		return nbti.getItem();
	}
	
	public TreeMap<String, String[]> getReforgeOptions() {
		return reforgeOptions;
	}
	
	protected void addReforgeOption(String base, String... options) {
		reforgeOptions.put(base, options);
		for (String option : options) {
			reforged.add(option);
		}
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
	
	public static ArrayList<Artifact> getArtifact(int value, int numDrops, EquipmentClass... ec) {
		return artifactTables.getMultiple(value, numDrops, ec);
	}
	
	public static ArrayList<Equipment> getDrop(int value, int numDrops, EquipmentClass... ec) {
		return droptables.getMultiple(value, numDrops, ec);
	}
	
	public static void addReforgedItem(String id) {
		reforged.add(id);
	}
	
	public static Equipment getDrop(int value, EquipmentClass... ec) {
		return getDrop(value, 1, ec).get(0);
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
		weaponDamage(p, data, target, properties.getDamage(), properties.getKnockback());
	}
	
	public void weaponDamage(Player p, PlayerFightData data, LivingEntity target, double damage) {
		weaponDamage(p, data, target, damage, properties.getKnockback());
	}
	
	public void weaponDamage(Player p, PlayerFightData data, LivingEntity target, double damage, double knockback) {
		BasicAttackEvent ev = new BasicAttackEvent(target, damage, knockback, properties.getType(), this, null);
		data.runActions(data, Trigger.BASIC_ATTACK, ev);
		FightInstance.dealDamage(p, properties.getType(), ev.getDamage(), target);
		if (knockback != 0) {
			FightInstance.knockback(p, target, knockback);
		}
	}
	
	// for projectiles
	public void weaponDamageProjectile(LivingEntity target, Projectile proj) {
		weaponDamageProjectile(target, proj, null);
	}
	
	public void weaponDamageProjectile(LivingEntity target, Projectile proj, Barrier hitBarrier) {
		double damage = properties.getDamage();
		if (proj.getBuffs() != null) {
			damage = Buff.applyOffenseBuffs(proj.getBuffs(), properties.getType(), damage);
		}
		if (hitBarrier != null) {
			damage = hitBarrier.applyDefenseBuffs(properties.getType(), damage);
		}
		BasicAttackEvent ev = new BasicAttackEvent(target, damage, properties.getKnockback(), properties.getType(), this, null);
		PlayerFightData data = (PlayerFightData) proj.getOwner();
		data.runActions(data, Trigger.BASIC_ATTACK, ev);
		FightInstance.dealDamage(data.getEntity(), properties.getType(), ev.getDamage(), target);
		if (properties.getKnockback() != 0) {
			FightInstance.knockback(proj.getVector(), target, properties.getKnockback());
		}
	}
	
	public boolean isCursed() {
		return isCursed;
	}
	
	public static enum EquipmentClass {
		WARRIOR("Warrior"),
		THIEF("Thief"),
		ARCHER("Archer"),
		MAGE("Mage"),
		SHOP("Shop"),
		CLASSLESS("Classless");
		
		private String display;
		private EquipmentClass(String display) {
			this.display = display;
		}
		
		public String getDisplay() {
			return display;
		}
	}
	
	public static enum EquipmentType {
		WEAPON("Weapon", new EquipSlot[] {EquipSlot.HOTBAR}),
		ARMOR("Armor", new EquipSlot[] {EquipSlot.ARMOR}),
		ACCESSORY("Accessory", new EquipSlot[] {EquipSlot.ACCESSORY}),
		OFFHAND("Offhand", new EquipSlot[] {EquipSlot.OFFHAND}),
		ABILITY("Ability", new EquipSlot[] {EquipSlot.HOTBAR, EquipSlot.KEYBIND}),
		CONSUMABLE("Consumable", new EquipSlot[] {EquipSlot.HOTBAR, EquipSlot.KEYBIND}),
		MATERIAL("Material", new EquipSlot[0]),
		ARTIFACT("Artifact", new EquipSlot[0]);
		
		private String display;
		private EquipSlot[] slots;
		private EquipmentType(String display, EquipSlot[] slots) {
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
	}
	
	public static enum EquipSlot {
		ARMOR,
		ACCESSORY,
		OFFHAND,
		HOTBAR,
		KEYBIND, // Hotbar + other binds
		STORAGE;
	}
	
	private static class DropTableSet<E> {
		private HashMap<EquipmentClass, HashMap<Integer, DropTable<E>>> droptables =
				new HashMap<EquipmentClass, HashMap<Integer, DropTable<E>>>();
		
		public DropTableSet() {
			reload();
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
	}
}
