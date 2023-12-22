package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
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
	
	protected String id;
	protected Component display;
	private TreeMap<String, String[]> reforgeOptions = new TreeMap<String, String[]>();
	protected boolean isUpgraded, canDrop = true;
	protected ItemStack item;
	protected Rarity rarity;
	protected EquipmentClass ec;
	
	public static void load() {
		
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
			//new FuriousSwing(b);
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
			
			// Artifacts
			new RubyShard(b);
			new RubyCluster(b);
			new RubyGem(b);
			new SapphireShard(b);
			new SapphireCluster(b);
			new SapphireGem(b);
			new EmeraldShard(b);
			new EmeraldCluster(b);
			new EmeraldGem(b);
			
			// Armor
			new ClothBindings(b);
			new Footpads(b);
			new LeatherChestplate(b);
			new LeatherHelmet(b);
			new NullMagicMantle(b);
			new SpikedPauldrons(b);
			
			// Offhands
			new CaptainsTowerShield(b);
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
		}
		
		for (Equipment eq : equipment.values()) {
			Equipment up = eq.getUpgraded();
			eq.setupDroptable();
			up.setupDroptable();
			eq.setupItem();
			up.setupItem();
		}
	}
	
	public Equipment(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec) {
		this.id = id;
		this.rarity = rarity;
		this.isUpgraded = isUpgraded;
		this.ec = ec;
		
		// Just make sure not to close any of the tags in display string
		this.display = rarity.applyDecorations(SharedUtil.color(display + (isUpgraded ? "+" : "")));
		

		if (equipment.containsKey(id) && !isUpgraded) {
			Bukkit.getLogger().warning("[NeoRogue] Duplicate id of " + id + " found while loading equipment");
		}
		
		if (isUpgraded) upgraded.put(id, this);
		else equipment.put(id, this);
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
	public abstract void initialize(Player p, PlayerFightData data, Trigger bind, int slot);
	
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
	
	public static HotbarCompatible[] deserializeHotbar(String str) {
		String[] separated = str.split(";");
		HotbarCompatible[] arr = new HotbarCompatible[separated.length];
		for (int i = 0; i < separated.length; i++) {
			if (str.isBlank()) continue;
			arr[i] = (HotbarCompatible) Equipment.deserialize(separated[i]);
		}
		return arr;
	}
	
	public static Armor[] deserializeArmor(String str) {
		String[] separated = str.split(";");
		Armor[] arr = new Armor[separated.length];
		for (int i = 0; i < separated.length; i++) {
			if (str.isBlank()) continue;
			arr[i] = (Armor) Equipment.deserialize(separated[i]);
		}
		return arr;
	}
	
	public static Accessory[] deserializeAccessories(String str) {
		String[] separated = str.split(";");
		Accessory[] arr = new Accessory[separated.length];
		for (int i = 0; i < separated.length; i++) {
			if (str.isBlank()) continue;
			arr[i] = (Accessory) Equipment.deserialize(separated[i]);
		}
		return arr;
	}
	
	public static Usable[] deserializeUsables(String str) {
		String[] separated = str.split(";");
		Usable[] arr = new Usable[separated.length];
		for (int i = 0; i < separated.length; i++) {
			if (str.isBlank()) continue;
			arr[i] = (Usable) Equipment.deserialize(separated[i]);
		}
		return arr;
	}
	
	public ItemStack createItem(Material mat, String type, ArrayList<String> preLoreLine, String loreLine) {
		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();

		meta.displayName(display.decoration(TextDecoration.ITALIC, State.FALSE));
		ArrayList<Component> loreItalicized = new ArrayList<Component>();
		loreItalicized.add(rarity.getDisplay(true).append(Component.text(" " + type)));
		if (preLoreLine != null) {
			for (String l : preLoreLine) {
				loreItalicized.add(NeoCore.miniMessage().deserialize(l));
			}
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
		item.setItemMeta(meta);
		
		NBTItem nbti = new NBTItem(item);
		nbti.setString("equipId", id);
		nbti.setString("type", type.toUpperCase());
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
	
	private static class DropTableSet<E> {
		private HashMap<EquipmentClass, HashMap<Integer, DropTable<E>>> droptables =
				new HashMap<EquipmentClass, HashMap<Integer, DropTable<E>>>();
		
		public DropTableSet() {
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
