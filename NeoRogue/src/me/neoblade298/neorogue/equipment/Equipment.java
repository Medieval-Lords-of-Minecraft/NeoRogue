package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.equipment.abilities.builtin.*;
import me.neoblade298.neorogue.session.instance.FightData;

public abstract class Equipment {
	private static HashMap<String, Equipment> equipment = new HashMap<String, Equipment>();
	private static HashMap<String, Equipment> upgraded = new HashMap<String, Equipment>();
	protected String id;
	protected boolean isUpgraded;
	protected ItemStack item;
	protected Rarity rarity;
	
	static {
		for (boolean b : new boolean[] {false, true}) {
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
	public abstract void initialize(FightData data);
	
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
}
