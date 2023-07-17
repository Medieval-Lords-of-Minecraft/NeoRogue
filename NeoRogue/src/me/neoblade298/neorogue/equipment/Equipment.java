package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

public abstract class Equipment {
	private static HashMap<String, Equipment> equipment = new HashMap<String, Equipment>();
	private String id;
	
	public String getId() {
		return id;
	}
	
	public abstract ItemStack getItem();
	
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
