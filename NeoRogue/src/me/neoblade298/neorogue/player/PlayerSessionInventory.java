package me.neoblade298.neorogue.player;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.plugin.NBTAPI;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.equipment.*;

public class PlayerSessionInventory extends CoreInventory {
	private static final int[] ARMOR = new int[] {0, 1, 2};
	private static final int[] ACCESSORIES = new int[] {3, 4, 5, 6, 7, 8};
	private static final int[] HOTBAR = new int[] {18, 19, 20, 21, 22, 23, 24, 25, 26};
	private static final int[] FILLER = new int[] {27, 28, 29, 30, 32, 33, 34};
	private static final int SELL = 35;
	private static final int OFFHAND = 17;
	private static final int ARTIFACTS = 31;
	
	private PlayerSessionData data;
	
	public PlayerSessionInventory(PlayerSessionData data) {
		super(data.getPlayer(), Bukkit.createInventory(data.getPlayer(), 36, "&9Your Inventory"));
		this.data = data;
		ItemStack[] contents = inv.getContents();
		
		// Import data from session data
		int iter = 0;
		for (int i : ARMOR) {
			Armor a = data.getArmor()[iter];
			contents[i] = a != null ? addNbt(a.getItem(), "ARMOR", a.getId(), a.isUpgraded(), iter) :
				addNbt(CoreInventory.createButton(Material.YELLOW_STAINED_GLASS_PANE, "&eArmor Slot", "&7Drag an armor here to equip it!"),
						"ARMOR", iter++);
		}
		
		iter = 0;
		for (int i : ACCESSORIES) {
			Accessory a = data.getAccessories()[iter];
			contents[i] = a != null ? addNbt(a.getItem(), "ACCESSORY", a.getId(), a.isUpgraded(), iter) :
				addNbt(CoreInventory.createButton(Material.LIME_STAINED_GLASS_PANE, "&aAccessory Slot", "&7Drag an accessory here to equip it!"),
						"ACCESSORY", iter++);
		}
		
		for (KeyBind bind : KeyBind.values()) {
			Usable a = data.getOtherBinds()[bind.getDataSlot()];
			contents[bind.getInventorySlot()] = a != null ? addNbt(a.getItem(), "OTHERBINDS", a.getId(), a.isUpgraded(), bind.getDataSlot()) :
				addNbt(bind.getItem(), "OTHERBINDS", bind.getDataSlot());
		}
		
		Offhand o = data.getOffhand();
		contents[OFFHAND] = o != null ? addNbt(o.getItem(), "OFFHAND", o.getId(), o.isUpgraded(), 0) :
			addNbt(CoreInventory.createButton(Material.WHITE_STAINED_GLASS_PANE, "&fOffhand Slot", "&7Drag an offhand here to equip it!"),
					"OFFHAND", 0);
		
		for (int i : HOTBAR) {
			Usable eq = data.getHotbar()[i - 18];
			contents[i] = eq != null ? addNbt(eq.getItem(), "HOTBAR", eq.getId(), eq.isUpgraded(), i - 18) :
				addNbt(CoreInventory.createButton(Material.RED_STAINED_GLASS_PANE, "&cHotbar Slot", "&eBound to Hotbar #" + (i - 17),
						"&7Drag a weapon, skill, or consumable", "&7here to bind it!"), "HOTBAR", i - 18);
		}
		
		for (int i : FILLER) {
			contents[i] = CoreInventory.createButton(Material.BLACK_STAINED_GLASS_PANE, " ");
		}
		
		contents[SELL] = addNbt(CoreInventory.createButton(Material.ORANGE_STAINED_GLASS_PANE, "&6Sell", "&7Place an item here to sell it!"),
				"SELL", 0);
		
		contents[ARTIFACTS] = addNbt(CoreInventory.createButton(Material.NETHER_STAR, "&6Artifacts", "&7Click here to view all your artifacts!"),
				"ARTIFACTS", 0);
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		// First do not allow 
		ItemStack cursor = e.getCursor();
		ItemStack clicked = e.getCurrentItem();
		if (cursor == null && clicked == null) return;
		
		int slot = e.getSlot();
		// First check for specific cases: Sell and Artifact view
		if (slot == SELL) {
			// TODO: Create sell confirmation
		}
		else if (slot == ARTIFACTS) {
			// TODO: Create artifact inventory
		}

		NBTItem ncursor = cursor != null ? new NBTItem(cursor) : null;
		NBTItem nclicked = clicked != null ? new NBTItem(clicked) : null;
		boolean onChest = e.getClickedInventory().getType() == InventoryType.CHEST;
		
		if (cursor == null && clicked != null) {
			// Only allow picking up equipment
			if (!nclicked.hasTag("equipId")) {
				e.setCancelled(true);
			}
			// Update player session data if removing equipped gear
			else if (onChest) {
				removeEquipment(nclicked.getString("type"), nclicked.getInteger("dataSlot"));
				p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
			}
		}
		
		// Most important, only way to equip an item
		else if (cursor != null && clicked != null) {
			if (!nclicked.hasTag("equipId")) {
				e.setCancelled(true);
			}
			else if (onChest) {
				setEquipment(ncursor.getString("type"), ncursor.getInteger("dataSlot"), ncursor.getString("equipId"), ncursor.getBoolean("isUpgraded"));
				p.playSound(p, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1F, 1F);
			}
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
	
	private ItemStack addNbt(ItemStack item, String type, int dataSlot) {
		NBTItem nbti = new NBTItem(item);
		nbti.setString("type", type);
		nbti.setInteger("dataSlot", dataSlot);
		return nbti.getItem();
	}
	
	private ItemStack addNbt(ItemStack item, String type, String equipId, boolean isUpgraded, int dataSlot) {
		NBTItem nbti = new NBTItem(item);
		nbti.setString("equipId", equipId);
		nbti.setString("type", type);
		nbti.setInteger("dataSlot", dataSlot);
		nbti.setBoolean("isUpgraded", isUpgraded);
		return nbti.getItem();
	}
	
	private void removeEquipment(String type, int dataSlot) {
		switch (type) {
		case "ARMOR":
			data.getArmor()[dataSlot] = null;
			break;
		case "ACCESSORY":
			data.getAccessories()[dataSlot] = null;
			break;
		case "OTHERBINDS":
			data.getOtherBinds()[dataSlot] = null;
			break;
		case "OFFHAND":
			data.setOffhand(null);
			break;
		case "HOTBAR":
			data.getHotbar()[dataSlot] = null;
			break;
		}
	}
	
	private boolean setEquipment(String type, int dataSlot, String equipId, boolean isUpgraded) {
		Equipment eq = Equipment.getEquipment(equipId, isUpgraded);
		switch (type) {
		case "ARMOR":
			if (!(eq instanceof Armor) && eq != null) return false;
			data.getArmor()[dataSlot] = (Armor) eq;
			break;
		case "ACCESSORY":
			if (!(eq instanceof Accessory) && eq != null) return false;
			data.getAccessories()[dataSlot] = (Accessory) eq;
			break;
		case "OTHERBINDS":
			if (!(eq instanceof Usable) && eq != null) return false;
			data.getOtherBinds()[dataSlot] = (Usable) eq;
			break;
		case "OFFHAND":
			if (!(eq instanceof Offhand) && eq != null) return false;
			data.setOffhand((Offhand) eq);
			break;
		case "HOTBAR":
			if (!(eq instanceof Usable) && eq != null) return false;
			data.getHotbar()[dataSlot] = (Usable) eq;
			break;
		}
		return true;
	}
}
