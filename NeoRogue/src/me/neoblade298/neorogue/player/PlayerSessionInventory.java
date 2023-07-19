package me.neoblade298.neorogue.player;

import java.util.HashMap;

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
	private static final int[] ARMOR = new int[] { 0, 1, 2 };
	private static final int[] ACCESSORIES = new int[] { 3, 4, 5, 6, 7, 8 };
	private static final int[] HOTBAR = new int[] { 18, 19, 20, 21, 22, 23, 24, 25, 26 };
	private static final int[] FILLER = new int[] { 27, 28, 29, 30, 32, 33, 34 };
	private static final int SELL = 35;
	private static final int OFFHAND = 17;
	private static final int ARTIFACTS = 31;
	private static HashMap<Integer, String> slotTypes = new HashMap<Integer, String>();

	private PlayerSessionData data;

	public PlayerSessionInventory(PlayerSessionData data) {
		super(data.getPlayer(), Bukkit.createInventory(data.getPlayer(), 36, "§9Your Inventory"));
		this.data = data;
		ItemStack[] contents = inv.getContents();

		// Import data from session data
		int iter = 0;
		for (int i : ARMOR) {
			Armor a = data.getArmor()[iter];
			contents[i] = a != null ? addNbt(a.getItem(), "ARMOR", a.getId(), a.isUpgraded(), iter)
					: addNbt(CoreInventory.createButton(Material.YELLOW_STAINED_GLASS_PANE, "&eArmor Slot",
							"&7Drag an armor here to equip it!"), iter++);
			slotTypes.put(i, "ARMOR");
		}

		iter = 0;
		for (int i : ACCESSORIES) {
			Accessory a = data.getAccessories()[iter];
			contents[i] = a != null ? addNbt(a.getItem(), "ACCESSORY", a.getId(), a.isUpgraded(), iter)
					: addNbt(CoreInventory.createButton(Material.LIME_STAINED_GLASS_PANE, "&aAccessory Slot",
							"&7Drag an accessory here to equip it!"), iter++);
			slotTypes.put(i, "ACCESSORY");
		}

		for (KeyBind bind : KeyBind.values()) {
			Usable a = data.getOtherBinds()[bind.getDataSlot()];
			contents[bind.getInventorySlot()] = a != null
					? addNbt(a.getItem(), "OTHERBINDS", a.getId(), a.isUpgraded(), bind.getDataSlot())
					: addNbt(bind.getItem(), bind.getDataSlot());
			slotTypes.put(bind.getInventorySlot(), "OTHERBINDS");
		}

		Offhand o = data.getOffhand();
		contents[OFFHAND] = o != null ? addNbt(o.getItem(), "OFFHAND", o.getId(), o.isUpgraded(), 0)
				: addNbt(CoreInventory.createButton(Material.WHITE_STAINED_GLASS_PANE, "&fOffhand Slot",
						"&7Drag an offhand here to equip it!"), 0);
		slotTypes.put(OFFHAND, "OFFHAND");

		for (int i : HOTBAR) {
			Usable eq = data.getHotbar()[i - 18];
			contents[i] = eq != null ? addNbt(eq.getItem(), "HOTBAR", eq.getId(), eq.isUpgraded(), i - 18)
					: addNbt(CoreInventory.createButton(Material.RED_STAINED_GLASS_PANE, "&cHotbar Slot",
							"&eBound to Hotbar #" + (i - 17), "&7Drag a weapon, skill, or consumable",
							"&7here to bind it!"), i - 18);
			slotTypes.put(i, "HOTBAR");
		}

		for (int i : FILLER) {
			contents[i] = CoreInventory.createButton(Material.BLACK_STAINED_GLASS_PANE, " ");
		}

		contents[SELL] = addNbt(CoreInventory.createButton(Material.ORANGE_STAINED_GLASS_PANE, "&6Sell",
				"&7Place an item here to sell it!"), 0);

		contents[ARTIFACTS] = addNbt(CoreInventory.createButton(Material.NETHER_STAR, "&6Artifacts",
				"&7Click here to view all your artifacts!"), 0);
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		// First do not allow
		ItemStack cursor = e.getCursor();
		ItemStack clicked = e.getCurrentItem();
		if (cursor.getType().isAir() && clicked == null) return;

		int slot = e.getSlot();
		// First check for specific cases: Sell and Artifact view
		if (slot == SELL) {
			e.setCancelled(true);
			// TODO: Create sell confirmation
		}
		else if (slot == ARTIFACTS) {
			e.setCancelled(true);
			// TODO: Create artifact inventory
		}

		NBTItem ncursor = !cursor.getType().isAir() ? new NBTItem(cursor) : null;
		NBTItem nclicked = clicked != null ? new NBTItem(clicked) : null;
		boolean onChest = e.getClickedInventory().getType() == InventoryType.CHEST;

		if (cursor.getType().isAir() && clicked != null) {
			// Only allow picking up equipment
			if (!nclicked.hasTag("equipId")) {
				e.setCancelled(true);
			}
			// Update player session data if removing equipped gear
			else if (onChest) {
				removeEquipment(slotTypes.get(slot), nclicked.getInteger("dataSlot"));
				p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
			}
		}

		// Most important, only way to equip an item
		else if (!cursor.getType().isAir() && clicked != null) {
			// Don't allow swapping with filler panes
			if (!nclicked.hasTag("type")) {
				e.setCancelled(true);
			}
			if (onChest) {
				boolean canSet = setEquipment(slotTypes.get(slot), ncursor.getInteger("dataSlot"),
						ncursor.getString("equipId"), ncursor.getBoolean("isUpgraded"));
				System.out.println("Can set: " + canSet);
				e.setCancelled(!canSet);
				if (canSet) {
					p.playSound(p, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1F, 1F);
					p.setItemOnCursor(null);
				}
			}
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {

	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {

	}

	private ItemStack addNbt(ItemStack item, int dataSlot) {
		NBTItem nbti = new NBTItem(item);
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

	private boolean setEquipment(String slotType, int dataSlot, String equipId, boolean isUpgraded) {
		Equipment eq = Equipment.getEquipment(equipId, isUpgraded);
		switch (slotType) {
		case "ARMOR":
			if (!(eq instanceof Armor)) return false;
			data.getArmor()[dataSlot] = (Armor) eq;
			break;
		case "ACCESSORY":
			System.out.println("0");
			if (!(eq instanceof Accessory)) return false;
			System.out.println("1");
			data.getAccessories()[dataSlot] = (Accessory) eq;
			break;
		case "OTHERBINDS":
			if (!(eq instanceof Usable)) return false;
			data.getOtherBinds()[dataSlot] = (Usable) eq;
			break;
		case "OFFHAND":
			if (!(eq instanceof Offhand)) return false;
			data.setOffhand((Offhand) eq);
			break;
		case "HOTBAR":
			if (!(eq instanceof Usable)) return false;
			data.getHotbar()[dataSlot] = (Usable) eq;
			break;
		default:
			return false; // Just in case null pointer
		}
		return true;
	}
}
