package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import java.util.HashMap;

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
import me.neoblade298.neorogue.equipment.armor.*;
import me.neoblade298.neorogue.equipment.artifacts.*;
import me.neoblade298.neorogue.equipment.offhands.*;
import me.neoblade298.neorogue.equipment.weapons.*;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.PlayerFightData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public abstract class Equipment {
	private static HashMap<String, Equipment> equipment = new HashMap<String, Equipment>();
	private static HashMap<String, Equipment> upgraded = new HashMap<String, Equipment>();
	
	private static HashMap<EquipmentClass, HashMap<Integer, DropTable<Equipment>>> droptables =
			new HashMap<EquipmentClass, HashMap<Integer, DropTable<Equipment>>>();
	protected String id, display;
	protected ArrayList<String> reforgeOptions = new ArrayList<String>();
	protected boolean isUpgraded, canDrop;
	protected ItemStack item;
	protected Rarity rarity;
	protected EquipmentClass ec;
	
	public static void load() {
		for (EquipmentClass ec : EquipmentClass.values()) {
			HashMap<Integer, DropTable<Equipment>> tables = new HashMap<Integer, DropTable<Equipment>>();
			for (int i = 0; i < 10; i++) {
				tables.put(i, new DropTable<Equipment>());
			}
			droptables.put(ec, tables);
		}
		
		for (boolean b : new boolean[] {false, true}) {
			// Armor
			new LeatherHelmet(b);
			
			// Weapons
			new EarthenLeatherGauntlets(b);
			new FencingSword(b);
			new ForcefulLeatherGauntlets(b);
			new IronAxe(b);
			new IronDagger(b);
			new IronSword(b);
			new LeatherGauntlets(b);
			new LightLeatherGauntlets(b);
			new Rapier(b);
			new SerratedFencingSword(b);
			new StoneAxe(b);
			new StoneDagger(b);
			new StoneSword(b);
			new WoodenSword(b);
			new WoodenWand(b);
			
			// Offhands
			new SmallShield(b);
			
			// Abilities
			new BattleCry(b);
			new Berserk(b);
			new EmpoweredEdge(b);
			
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
		}
	}
	
	public Equipment(String id, boolean isUpgraded, Rarity rarity, EquipmentClass ec) {
		int value = rarity.getValue() + (isUpgraded ? 1 : 0);
		this.id = id;
		this.rarity = rarity;
		this.isUpgraded = isUpgraded;
		this.ec = ec;
		

		if (equipment.containsKey(id) && !isUpgraded) {
			Bukkit.getLogger().warning("[NeoRogue] Duplicate id of " + id + " found while loading equipment");
		}
		
		if (isUpgraded) upgraded.put(id, this);
		else equipment.put(id, this);
		
		if (!canDrop) return;
		if (value >= 2) {
			droptables.get(ec).get(value - 2).add(this, 1); // Rare drop for the value
		}
		if (value >= 1) {
			droptables.get(ec).get(value - 1).add(this, 3); // Uncommon drop for the value
		}
		droptables.get(ec).get(value).add(this, 8);
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
		return createItem(mat, type, preLoreLine, loreLine, null);
	}
	
	public ItemStack createItem(Material mat, String type, ArrayList<String> preLoreLine, String loreLine, String nameOverride) {
		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		
		if (nameOverride == null) {
			meta.displayName(rarity.applyDecorations(Component.text(display + (isUpgraded ? "+" : ""))));
		}
		else {
			meta.displayName(NeoCore.miniMessage().deserialize(nameOverride));
		}
		
		ArrayList<Component> lore = new ArrayList<Component>();
		lore.add(rarity.getDisplay(true).append(Component.text(" " + type)));
		if (!reforgeOptions.isEmpty()) lore.add(Component.text("Reforgeable (Combine 2 of this item)", NamedTextColor.YELLOW));
		if (preLoreLine != null) {
			for (String l : preLoreLine) {
				lore.add(NeoCore.miniMessage().deserialize(l));
			}
		}
		if (loreLine != null) {
			lore.addAll(SharedUtil.addLineBreaks((TextComponent) NeoCore.miniMessage().deserialize(loreLine).colorIfAbsent(NamedTextColor.GRAY), 200));
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
	
	public ArrayList<String> getReforgeOptions() {
		return reforgeOptions;
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
	
	public static ArrayList<Equipment> getDrop(int value, int numDrops, EquipmentClass... ec) {
		ArrayList<Equipment> list = new ArrayList<Equipment>();
		DropTable<Equipment> table;
		for (int i = 0; i < numDrops; i++) {
			if (ec.length > 1) {
				DropTable<DropTable<Equipment>> tables = new DropTable<DropTable<Equipment>>();
				for (int j = 0; j < ec.length; j++) {
					DropTable<Equipment> temp = droptables.get(ec[j]).get(value);
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
	
	public static Equipment getDrop(int value, EquipmentClass... ec) {
		return getDrop(value, 1, ec).get(0);
	}
	
	public String getDisplay() {
		return display;
	}
}
