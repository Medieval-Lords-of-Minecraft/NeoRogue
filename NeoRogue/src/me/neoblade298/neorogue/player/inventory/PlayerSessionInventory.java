package me.neoblade298.neorogue.player.inventory;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.trigger.KeyBind;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class PlayerSessionInventory extends CoreInventory {
	private static final int[] ARMOR = new int[] { 0, 1, 2 };
	private static final int[] ACCESSORIES = new int[] { 3, 4, 5, 6, 7, 8 };
	private static final int[] HOTBAR = new int[] { 18, 19, 20, 21, 22, 23, 24, 25, 26 };
	private static final int[] FILLER = new int[] { 16, 28, 29, 30, 32, 33, 34 };
	private static final int STATS = 27;
	private static final int TRASH = 35;
	private static final int OFFHAND = 17;
	private static final int ARTIFACTS = 31;
	private static HashMap<Integer, EquipSlot> slotTypes = new HashMap<Integer, EquipSlot>();
	
	private static final TextComponent instruct = Component.text("Drag a weapon, ability, or consumable", NamedTextColor.GRAY);
	private static final TextComponent instruct2 = Component.text("here to bind it!", NamedTextColor.GRAY);
	private static final TextComponent statsText = Component.text("Your stats:", NamedTextColor.GOLD);

	private PlayerSessionData data;

	public PlayerSessionInventory(PlayerSessionData data) {
		super(data.getPlayer(), Bukkit.createInventory(data.getPlayer(), 36, Component.text("Equipment", NamedTextColor.BLUE)));
		this.data = data;
		ItemStack[] contents = inv.getContents();

		// Import data from session data
		int iter = 0;
		for (int i : ARMOR) {
			slotTypes.put(i, EquipSlot.ARMOR);
			Equipment a = data.getEquipment(EquipSlot.ARMOR)[iter];
			contents[i] = a != null ? addNbt(a.getItem(), a.getId(), a.isUpgraded(), iter)
					: createArmorIcon(iter);
			iter++;
		}

		iter = 0;
		for (int i : ACCESSORIES) {
			slotTypes.put(i, EquipSlot.ACCESSORY);
			Equipment a = data.getEquipment(EquipSlot.ACCESSORY)[iter];
			contents[i] = a != null ? addNbt(a.getItem(), a.getId(), a.isUpgraded(), iter)
					: createAccessoryIcon(iter);
			iter++;
		}

		for (KeyBind bind : KeyBind.values()) {
			slotTypes.put(bind.getInventorySlot(), EquipSlot.KEYBIND);
			Equipment a = data.getEquipment(EquipSlot.KEYBIND)[bind.getDataSlot()];
			contents[bind.getInventorySlot()] = a != null
					? addNbt(addBindLore(a.getItem(), bind.getInventorySlot(), bind.getDataSlot()), a.getId(), a.isUpgraded(), bind.getDataSlot())
					: addNbt(bind.getItem(), bind.getDataSlot());
		}

		slotTypes.put(OFFHAND, EquipSlot.OFFHAND);
		Equipment o = data.getEquipment(EquipSlot.OFFHAND)[0];
		contents[OFFHAND] = o != null ? addNbt(o.getItem(), o.getId(), o.isUpgraded(), 0)
				: createOffhandIcon();

		for (int i : HOTBAR) {
			slotTypes.put(i, EquipSlot.HOTBAR);
			Equipment eq = data.getEquipment(EquipSlot.HOTBAR)[i - 18];
			contents[i] = eq != null ? addNbt(addBindLore(eq.getItem(), i, i - 18), eq.getId(), eq.isUpgraded(), i - 18)
					: createHotbarIcon(i - 18);
		}

		for (int i : FILLER) {
			contents[i] = CoreInventory.createButton(Material.BLACK_STAINED_GLASS_PANE, Component.text(" "));
		}

		contents[STATS] = createStatsIcon();
		contents[TRASH] = addNbt(CoreInventory.createButton(Material.HOPPER, Component.text("Trash", NamedTextColor.GOLD),
				"Click to open a trash can!", 250, NamedTextColor.GRAY), 0);

		contents[ARTIFACTS] = addNbt(CoreInventory.createButton(Material.NETHER_STAR, Component.text("Artifacts", NamedTextColor.GOLD),
				"Click here to view all your artifacts!", 250, NamedTextColor.GRAY), 0);
		inv.setContents(contents);
	}
	
	private static ItemStack createArmorIcon(int dataSlot) {
		return addNbt(CoreInventory.createButton(Material.YELLOW_STAINED_GLASS_PANE, Component.text("Armor Slot", NamedTextColor.YELLOW),
			"Drag an armor here to equip it!", 250, NamedTextColor.GRAY), dataSlot);
	}
	
	public static ItemStack createIcon(EquipSlot slot, int dataSlot) {
		switch (slot) {
		case ACCESSORY: return createAccessoryIcon(dataSlot);
		case ARMOR: return createArmorIcon(dataSlot);
		case HOTBAR: return createHotbarIcon(dataSlot);
		case KEYBIND: return KeyBind.getBindFromSlot(dataSlot).getItem();
		case OFFHAND: return createOffhandIcon();
		default: return null;
		}
	}
	
	public static ItemStack createAccessoryIcon(int dataSlot) {
		return addNbt(CoreInventory.createButton(Material.LIME_STAINED_GLASS_PANE, Component.text("Accessory Slot", NamedTextColor.GREEN),
				"Drag an accessory here to equip it!", 250, NamedTextColor.GRAY), dataSlot);
	}
	
	public static ItemStack createOffhandIcon() {
		return addNbt(CoreInventory.createButton(Material.WHITE_STAINED_GLASS_PANE, Component.text("Offhand Slot", NamedTextColor.WHITE),
				"Drag an offhand here to equip it!", 250, NamedTextColor.GRAY ), 0);
	}
	
	private static ItemStack createHotbarIcon(int dataSlot) {
		TextComponent bound = Component.text("Bound to hotbar #" + (dataSlot + 1), NamedTextColor.YELLOW);
		return addNbt(CoreInventory.createButton(Material.RED_STAINED_GLASS_PANE, Component.text("Hotbar Slot", NamedTextColor.RED),
				bound, instruct, instruct2), dataSlot);
	}
	
	private ItemStack createStatsIcon() {
		TextComponent health = Component.text("Health: ", NamedTextColor.GOLD)
				.append(Component.text(data.getHealth() + " / " + data.getMaxHealth(), NamedTextColor.WHITE));
		TextComponent mana = Component.text("Mana: ", NamedTextColor.GOLD)
				.append(Component.text(data.getMaxMana(), NamedTextColor.WHITE));
		TextComponent coins = Component.text("Coins: ", NamedTextColor.GOLD)
				.append(Component.text(data.getCoins(), NamedTextColor.WHITE));
		return CoreInventory.createButton(Material.ARMOR_STAND, statsText,
			health, mana, coins);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		ItemStack cursor = e.getCursor();
		ItemStack clicked = e.getCurrentItem();
		if (cursor.getType().isAir() && clicked == null) return;

		int slot = e.getRawSlot();
		// First check for specific cases: Trash and Artifact view
		if (slot == TRASH) {
			e.setCancelled(true);
			p.openInventory(Bukkit.createInventory(p, 27, Component.text("Trash Can", NamedTextColor.RED)));
			return;
		}
		if (slot == ARTIFACTS) {
			e.setCancelled(true);
			new ArtifactsInventory(data);
			return;
		}
		
		InventoryAction action = e.getAction();
		if (action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.HOTBAR_MOVE_AND_READD) {
			handleInventorySwap(e);
			return;
		}
		else if (action == InventoryAction.DROP_ONE_SLOT || action == InventoryAction.DROP_ALL_SLOT) {
			handleInventoryDrop(e);
			return;
		}
		else if (action == InventoryAction.COLLECT_TO_CURSOR) {
			e.setCancelled(true);
			return;
		}

		NBTItem ncursor = !cursor.getType().isAir() ? new NBTItem(cursor) : null;
		Inventory iclicked = e.getClickedInventory();
		NBTItem nclicked = clicked != null ? new NBTItem(clicked) : null;
		boolean onChest = iclicked != null && e.getClickedInventory().getType() == InventoryType.CHEST;

		if (cursor.getType().isAir() && clicked != null) {
			// Only allow picking up equipment
			if (!nclicked.hasTag("equipId")) {
				e.setCancelled(true);
			}
			// Remove gear
			else if (onChest) {
				e.setCancelled(true);
				EquipSlot type = slotTypes.get(slot);
				removeEquipment(type, nclicked.getInteger("dataSlot"), slot, e.getClickedInventory());
				if (isBindable(type)) clicked = removeBindLore(clicked);
				p.setItemOnCursor(clicked);
				p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
			}
		}

		// Swap an item with another item
		else if (!cursor.getType().isAir() && clicked != null) {
			e.setCancelled(true);
			
			String eqId = ncursor.getString("equipId");
			String eqedId = nclicked.getString("equipId");
			Equipment eq = Equipment.get(eqId, ncursor.getBoolean("isUpgraded"));
			Equipment eqed = Equipment.get(eqedId, nclicked.getBoolean("isUpgraded"));
			
			// Reforged item check
			if (eq.getReforgeOptions().containsKey(eqedId)) {
				p.setItemOnCursor(null);
				new ReforgeOptionsInventory(this, e.getSlot(), onChest, onChest ? slotTypes.get(e.getSlot()) : null, nclicked.getInteger("dataSlot"),
						eq, eqed, cursor);
				return;
			}
			else if (eqed != null && eqed.getReforgeOptions().containsKey(eqId)) {
				p.setItemOnCursor(null);
				new ReforgeOptionsInventory(this, e.getSlot(), onChest, onChest ? slotTypes.get(e.getSlot()) : null, nclicked.getInteger("dataSlot"),
						eqed, eq, cursor);
			}
			
			if (onChest) {
				if (!nclicked.hasTag("dataSlot")) return;
				if (eq.getType() == EquipmentType.ABILITY &&
						(eqed == null || eqed.getType() != EquipmentType.ABILITY) && !data.canEquipAbility()) {
					displayError("You can only equip " + data.getMaxAbilities() + " abilities!", true);
					return;
				}

				EquipSlot type = slotTypes.get(slot);
				if (!eq.canEquip(type)) {
					displayError("You can't equip this item in this slot!", false);
					return;
				}
				
				// If swapping equipment with equipment, remove that equipment
				if (!nclicked.hasTag("equipId")) {
					p.setItemOnCursor(null);
				}
				else {
					if (isBindable(type)) clicked = removeBindLore(clicked);
					data.removeEquipment(type, nclicked.getInteger("dataSlot"));
					p.setItemOnCursor(clicked);
				}
				
				data.setEquipment(type, nclicked.getInteger("dataSlot"), eq);
				p.playSound(p, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1F, 1F);
				if (isBindable(type)) cursor = addBindLore(cursor, slot, nclicked.getInteger("dataSlot"));
				inv.setItem(slot, addNbt(cursor, nclicked.getInteger("dataSlot")));
			}
		}
	}
	
	private void handleInventorySwap(InventoryClickEvent e) {
		int swapNum = e.getHotbarButton();
		int slot = e.getRawSlot();
		ItemStack swapped = p.getInventory().getContents()[swapNum];
		ItemStack clicked = e.getCurrentItem();
		NBTItem nswapped = swapped != null ? new NBTItem(swapped) : null;
		NBTItem nclicked = clicked != null ? new NBTItem(clicked) : null;
		Inventory iclicked = e.getClickedInventory();
		boolean onChest = iclicked != null && iclicked.getType() == InventoryType.CHEST;

		if (clicked != null && onChest) {
			e.setCancelled(true);
			if (swapped == null) {
				if (!nclicked.hasTag("equipId")) return;
				EquipSlot type = slotTypes.get(slot);
				if (isBindable(type)) clicked = removeBindLore(clicked);
				p.getInventory().setItem(swapNum, clicked);
				removeEquipment(type, nclicked.getInteger("dataSlot"), slot, e.getClickedInventory());
				p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
			}
			else {
				if (!nclicked.hasTag("dataSlot")) return;
				Equipment eq = Equipment.get(nswapped.getString("equipId"), nswapped.getBoolean("isUpgraded"));
				Equipment eqed = Equipment.get(nclicked.getString("equipId"), nclicked.getBoolean("isUpgraded"));
				if (eq.getType() == EquipmentType.ABILITY &&
						(eqed == null || eqed.getType() != EquipmentType.ABILITY) && !data.canEquipAbility()) {
					displayError("You can only equip " + data.getMaxAbilities() + " abilities!", true);
					return;
				}

				EquipSlot type = slotTypes.get(slot);
				// If swapping equipment with equipment, remove that equipment
				if (!nclicked.hasTag("equipId")) {
					p.getInventory().setItem(swapNum, null);
				}
				else {
					data.removeEquipment(type, nclicked.getInteger("dataSlot"));
					if (isBindable(type)) clicked = removeBindLore(clicked);
					p.setItemOnCursor(clicked);
				}
				
				data.setEquipment(type, nclicked.getInteger("dataSlot"), eq);
				p.playSound(p, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1F, 1F);
				if (isBindable(type)) swapped = addBindLore(swapped, slot, nclicked.getInteger("dataSlot"));
				inv.setItem(slot, addNbt(swapped, nclicked.getInteger("dataSlot")));
			}
		}
	}
	
	private void handleInventoryDrop(InventoryClickEvent e) {
		ItemStack clicked = e.getCurrentItem();
		int slot = e.getRawSlot();
		
		if (clicked == null) return;
		Inventory iclicked = e.getClickedInventory();
		if (iclicked == null || iclicked.getType() != InventoryType.CHEST) return;
		NBTItem nclicked = new NBTItem(clicked);
		
		if (!nclicked.hasTag("equipId")) {
			e.setCancelled(true);
			return;
		}
		else {
			e.setCancelled(true);
			EquipSlot type = slotTypes.get(slot);
			if (isBindable(type)) clicked = removeBindLore(clicked);
			p.getWorld().dropItem(p.getLocation(), clicked).setPickupDelay(40);
			removeEquipment(type, nclicked.getInteger("dataSlot"), slot, e.getClickedInventory());
			p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		if (p.getItemOnCursor().getType().isAir()) return;
		p.getInventory().addItem(p.getItemOnCursor());
		p.updateInventory();
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {

	}

	private static ItemStack addNbt(ItemStack item, int dataSlot) {
		NBTItem nbti = new NBTItem(item);
		nbti.setInteger("dataSlot", dataSlot);
		return nbti.getItem();
	}

	private static ItemStack addNbt(ItemStack item, String equipId, boolean isUpgraded, int dataSlot) {
		NBTItem nbti = new NBTItem(item);
		nbti.setString("equipId", equipId);
		nbti.setInteger("dataSlot", dataSlot);
		nbti.setBoolean("isUpgraded", isUpgraded);
		return nbti.getItem();
	}

	private void removeEquipment(EquipSlot type, int dataSlot, int invSlot, Inventory inv) {
		ItemStack icon = createIcon(type, dataSlot);
		data.removeEquipment(type, dataSlot);
		inv.setItem(invSlot, icon);
	}
	
	private ItemStack addBindLore(ItemStack item, int invSlot, int dataSlot) {
		ItemMeta meta = item.getItemMeta();
		List<Component> lore = meta.lore();
		EquipSlot slotType = slotTypes.get(invSlot);
		if (slotType == EquipSlot.KEYBIND) {
			lore.add(1, Component.text("Bound to ", NamedTextColor.YELLOW).append(KeyBind.getBindFromSlot(invSlot).getDisplay())
					.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		}
		else if (slotType == EquipSlot.HOTBAR) {
			lore.add(1, Component.text("Bound to Hotbar #" + (dataSlot + 1), NamedTextColor.YELLOW)
					.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		}
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}
	
	private ItemStack removeBindLore(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		List<Component> lore = meta.lore();
		lore.remove(1);
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}
	
	private void displayError(String error, boolean closeInventory) {
		p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 0.7F);
		Util.msg(p, error);
		if (closeInventory) p.closeInventory();
	}
	
	private boolean isBindable(EquipSlot type) {
		return type == EquipSlot.KEYBIND || type == EquipSlot.HOTBAR;
	}
	
	public PlayerSessionData getData() {
		return data;
	}
}
