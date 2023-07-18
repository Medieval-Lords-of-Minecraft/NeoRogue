package me.neoblade298.neorogue.player;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.*;

public class PlayerSessionData {
	private PlayerData data;
	private double maxHealth, maxMana, maxStamina, health;
	private Usable[] hotbar = new Usable[9];
	private Armor[] armors = new Armor[3];
	private Offhand offhand;
	private Accessory[] accessories = new Accessory[6];
	private ArrayList<Artifact> artifacts = new ArrayList<Artifact>();
	private ArrayList<Equipment> storage = new ArrayList<Equipment>(9);
	private Usable[] otherBinds = new Usable[8];
	
	
	public PlayerSessionData(UUID uuid) {
		data = PlayerManager.getPlayerData(uuid);
		maxHealth = 100;
		maxMana = 100;
		maxStamina = 100;
		health = maxHealth;
		
		// Need to give player a weapon at the start
	}
	
	public Player getPlayer() {
		return data.getPlayer();
	}
	
	public Armor[] getArmor() {
		return armors;
	}
	
	public Usable[] getHotbar() {
		return hotbar;
	}
	
	public Accessory[] getAccessories() {
		return accessories;
	}
	
	public ArrayList<Equipment> getStorage() {
		return storage;
	}
	
	public Offhand getOffhand() {
		return offhand;
	}
	
	public Usable[] getOtherBinds() {
		return otherBinds;
	}
	
	public Usable getOtherBind(KeyBind bind) {
		return otherBinds[bind.getDataSlot()];
	}
}
