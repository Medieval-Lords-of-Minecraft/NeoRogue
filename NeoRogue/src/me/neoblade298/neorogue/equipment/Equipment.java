package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.equipment.abilities.*;
import me.neoblade298.neorogue.equipment.offhands.*;
import me.neoblade298.neorogue.equipment.weapons.*;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.FightInstance;

public abstract class Equipment {
	private static HashMap<String, Equipment> equipment = new HashMap<String, Equipment>();
	private static HashMap<String, Equipment> upgraded = new HashMap<String, Equipment>();
	protected String id, display;
	protected boolean isUpgraded;
	protected ItemStack item;
	protected Rarity rarity;
	
	static {
		for (boolean b : new boolean[] {false, true}) {
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
}
