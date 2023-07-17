package me.neoblade298.neorogue.player;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.equipment.*;
import me.neoblade298.neorogue.equipment.abilities.Ability;

public class PlayerSessionInventory extends CoreInventory {
	private static final int[] ARMOR = new int[] {0, 1, 2};
	private static final int[] ACCESSORIES = new int[] {3, 4, 5, 6, 7, 8};
	private static final int[] HOTBAR = new int[] {18, 19, 20, 21, 22, 23, 24, 25, 26};
	private static final int OFFHAND = 18;
	private static final int ARTIFACTS = 31;
	
	public PlayerSessionInventory(PlayerSessionData data) {
		super(data.getPlayer(), Bukkit.createInventory(data.getPlayer(), 36));
		ItemStack[] contents = inv.getContents();
		
		// Import data from session data
		int iter = 0;
		for (int i : ARMOR) {
			Armor a = data.getArmor()[iter++];
			contents[i] = a != null ? a.getItem() :
				CoreInventory.createButton(Material.YELLOW_STAINED_GLASS_PANE, "&eArmor Slot", "&7Drag an armor here to equip it!");
		}
		
		iter = 0;
		for (int i : ACCESSORIES) {
			Accessory a = data.getAccessories()[iter++];
			contents[i] = a != null ? a.getItem() :
				CoreInventory.createButton(Material.LIME_STAINED_GLASS_PANE, "&aAccessory Slot", "&7Drag an accessory here to equip it!");
		}
		
		for (KeyBind bind : KeyBind.values()) {
			Ability a = data.getAbilities()[bind.getDataSlot()];
			contents[bind.getInventorySlot()] = a != null ? a.getItem() : bind.getItem();
		}
		
		Offhand o = data.getOffhand();
		contents[OFFHAND] = o != null ? o.getItem() :
			CoreInventory.createButton(Material.WHITE_STAINED_GLASS_PANE, "&fOffhand Slot", "&7Drag an offhand here to equip it!");
		
		for (int i : HOTBAR) {
			Equipment eq = data.getHotbar()[i - 18]
			contents[i] = eq != null ? eq.getItem() :
				CoreInventory.createButton(Material.RED_STAINED_GLASS_PANE, "&cHotbar Slot", "&7Drag a weapon or consumable here to", "&7equip it!");
		}
		
		contents[ARTIFACTS] = CoreInventory.createButton(Material.NETHER_STAR, "&6Artifacts", "&7Click here to view all your artifacts!");
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
