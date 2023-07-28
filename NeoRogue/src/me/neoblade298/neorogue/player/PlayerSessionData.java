package me.neoblade298.neorogue.player;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.*;
import net.md_5.bungee.api.ChatColor;

public class PlayerSessionData {
	private PlayerData data;
	private double maxHealth, maxMana, maxStamina, health;
	private HotbarCompatible[] hotbar = new HotbarCompatible[9];
	private Armor[] armors = new Armor[3];
	private Offhand offhand;
	private Accessory[] accessories = new Accessory[6];
	private ArrayList<Artifact> artifacts = new ArrayList<Artifact>();
	private ArrayList<Equipment> storage = new ArrayList<Equipment>(9);
	private Usable[] otherBinds = new Usable[8];
	private int abilitiesEquipped = 0, maxAbilities = 2, maxStorage = 9;
	
	public PlayerSessionData(UUID uuid) {
		data = PlayerManager.getPlayerData(uuid);
		maxHealth = 100;
		maxMana = 100;
		maxStamina = 100;
		health = maxHealth;
		
		// Need to give player a weapon at the start
		
		// Strictly debug purposes
		hotbar[0] = (HotbarCompatible) Equipment.getEquipment("woodenWand", true);
		hotbar[1] = (HotbarCompatible) Equipment.getEquipment("empoweredEdge", true);
		hotbar[2] = (HotbarCompatible) Equipment.getEquipment("battleCry", false);
		hotbar[3] = (HotbarCompatible) Equipment.getEquipment("serratedFencingSword", true);
		offhand = (Offhand) Equipment.getEquipment("ricketyShield", false);
		armors[0] = (Armor) Equipment.getEquipment("leatherHelmet", true);
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
	
	public HotbarCompatible[] getHotbar() {
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
	
	public int getMaxStorage() {
		return maxStorage;
	}
	
	public boolean saveStorage() {
		Player p = data.getPlayer();
		int max = maxStorage;
		ArrayList<ItemStack> toSave = new ArrayList<ItemStack>(max);
		p.getInventory().setItemInOffHand(null);
		for (ItemStack item : p.getInventory().getContents()) {
			if (item == null) continue;
			
			toSave.add(item);
		}
		
		if (toSave.size() > max) {
			Util.msg(p, "You have too many items in storage! Drop or sell some!");
			return false;
		}
		
		storage.clear();
		
		for (ItemStack item : toSave) {
			NBTItem nbti = new NBTItem(item);
			String id = nbti.getString("equipId");
			boolean isUpgraded = nbti.getBoolean("isUpgraded");
			Equipment eq = Equipment.getEquipment(id, isUpgraded);
			if (eq == null) {
				String display = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name();
				Bukkit.getLogger().warning("[NeoRogue] " + p.getName() + " could not save " + display + " to their storage");
				continue;
			}
			else {
				storage.add(eq);
			}
		}
		return true;
	}
}
