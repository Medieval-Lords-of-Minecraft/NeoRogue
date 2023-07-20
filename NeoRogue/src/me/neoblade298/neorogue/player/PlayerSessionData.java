package me.neoblade298.neorogue.player;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.equipment.*;
import net.md_5.bungee.api.ChatColor;

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
	private int abilitiesEquipped = 0, maxAbilities;
	
	public PlayerSessionData(UUID uuid) {
		data = PlayerManager.getPlayerData(uuid);
		maxHealth = 100;
		maxMana = 100;
		maxStamina = 100;
		maxAbilities = 1;
		health = maxHealth;
		
		// Need to give player a weapon at the start
		
		// Strictly debug purposes
		storage.add(Equipment.getEquipment("empoweredEdge", true));
		storage.add(Equipment.getEquipment("battleCry", false));
		setupInventory();
	}
	
	public void setupInventory() {
		Player p = data.getPlayer();
		PlayerInventory inv = p.getInventory();
		inv.clear();
		inv.setItemInOffHand(CoreInventory.createButton(Material.ENCHANTED_BOOK, "&eStorage Book",
				"Swap hands or click anywhere in your inventory to open your storage.", 200, ChatColor.GRAY));
		
		for (int i = 0; i < storage.size(); i++) {
			Equipment eq = storage.get(i);
			if (eq == null) continue;
			inv.setItem(i, eq.getItem());
		}
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
	
	public void setOffhand(Offhand offhand) {
		this.offhand = offhand;
	}
	
	public Usable[] getOtherBinds() {
		return otherBinds;
	}
	
	public Usable getOtherBind(KeyBind bind) {
		return otherBinds[bind.getDataSlot()];
	}
	
	public boolean canEquipAbility() {
		return abilitiesEquipped < maxAbilities;
	}
	
	public int getMaxAbilities() {
		return maxAbilities;
	}
	
	public void addAbilityEquipped(int num) {
		abilitiesEquipped += num;
	}

	public PlayerData getData() {
		return data;
	}

	public double getMaxHealth() {
		return maxHealth;
	}

	public double getMaxMana() {
		return maxMana;
	}

	public double getMaxStamina() {
		return maxStamina;
	}

	public double getHealth() {
		return health;
	}

	public Armor[] getArmors() {
		return armors;
	}

	public ArrayList<Artifact> getArtifacts() {
		return artifacts;
	}
}
