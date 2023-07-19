package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

public abstract class Equipment {
	private static HashMap<String, Equipment> equipment = new HashMap<String, Equipment>();
	private static HashMap<String, Equipment> upgraded = new HashMap<String, Equipment>();
	private String id;
	private boolean isUpgraded;
	private ItemStack item;
	private Rarity rarity;
	
	public Equipment(String id, ItemStack item, Rarity rarity) {
		this.id = id;
		this.item = item;
		this.rarity = rarity;
	}
	
	public String getId() {
		return id;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public boolean isUpgraded() {
		return isUpgraded;
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
}
