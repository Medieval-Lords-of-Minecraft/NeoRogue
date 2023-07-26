package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.equipment.abilities.*;
import me.neoblade298.neorogue.equipment.armor.*;
import me.neoblade298.neorogue.equipment.offhands.*;
import me.neoblade298.neorogue.equipment.weapons.*;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.FightInstance;
import net.md_5.bungee.api.ChatColor;

public abstract class Equipment {
	private static HashMap<String, Equipment> equipment = new HashMap<String, Equipment>();
	private static HashMap<String, Equipment> upgraded = new HashMap<String, Equipment>();
	protected String id, display;
	protected ArrayList<String> reforgeOptions = new ArrayList<String>();
	protected boolean isUpgraded;
	protected ItemStack item;
	protected Rarity rarity;
	
	static {
		// Reforge options must be added AFTER their base option
		for (boolean b : new boolean[] {false, true}) {
			// Armor
			new LeatherHelmet(b);
			
			// Weapons
			new WoodenSword(b);
			
			// Offhands
			new RicketyShield(b);
			
			new BattleCry(b);
			new EmpoweredEdge(b);
		}
	}
	
	public Equipment(String id, boolean isUpgraded, Rarity rarity) {
		this.id = id;
		this.rarity = rarity;
		this.isUpgraded = isUpgraded;
		
		if (isUpgraded) upgraded.put(id, this);
		else equipment.put(id, this);
	}
	
	// Run at the start of a fight to initialize Fight Data
	public abstract void initialize(Player p, FightData data, Trigger bind);
	
	public String getId() {
		return id;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public boolean isUpgraded() {
		return isUpgraded;
	}
	
	public Rarity getRarity() {
		return rarity;
	}
	
	public static Equipment getEquipment(String id, boolean upgrade) {
		return upgrade ? upgraded.get(id) : equipment.get(id);
	}
	
	public static String serialize(ArrayList<Equipment> arr) {
		if (arr.isEmpty()) return null;
		
		String str = "";
		for (int i = 0; i < arr.size(); i++) {
			str += arr.get(i);
			if (i + 1 < arr.size()) {
				str += ",";
			}
		}
		return str;
	}
	
	public static ArrayList<Equipment> deserialize(String str) {
		String[] separated = str.split(",");
		ArrayList<Equipment> arr = new ArrayList<Equipment>(separated.length);
		for (String s : separated) {
			arr.add(equipment.get(s));
		}
		return arr;
	}
	
	public static ItemStack createItem(Equipment eq, Material mat, String type, ArrayList<String> preLoreLine, String loreLine, String nameOverride) {
		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		
		if (nameOverride == null) {
			meta.setDisplayName(eq.rarity.getColor() + eq.display + (eq.isUpgraded ? "+" : ""));
		}
		else {
			meta.setDisplayName(nameOverride);
		}
		
		ArrayList<String> lore = new ArrayList<String>();
		lore.add(eq.rarity.getDisplay(true) + " " + type);
		if (!eq.reforgeOptions.isEmpty()) lore.add("§eReforgeable (Combine 2 of this item)");
		if (preLoreLine != null) {
			for (String l : preLoreLine) {
				lore.add(SharedUtil.translateColors(l));
			}
		}
		if (loreLine != null) {
			lore.addAll(SharedUtil.addLineBreaks(SharedUtil.translateColors(loreLine), 200, ChatColor.GRAY));
		}
		meta.setLore(lore);
		
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		meta.setUnbreakable(true);
		item.setItemMeta(meta);
		
		NBTItem nbti = new NBTItem(item);
		nbti.setString("equipId", eq.id);
		nbti.setString("type", type.toUpperCase());
		nbti.setBoolean("isUpgraded", eq.isUpgraded);
		return nbti.getItem();
	}
	
	public ArrayList<String> getReforgeOptions() {
		return reforgeOptions;
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
}
