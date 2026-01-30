package me.neoblade298.neorogue.player.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.ShopInstance;
import me.neoblade298.neorogue.session.ShopInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class StorageInventory extends CoreInventory implements ShiftClickableInventory {
	private Player spectator;
	private PlayerSessionData data;
	private boolean isShop;
	
	private static final int SELL = 8;

	public StorageInventory(PlayerSessionData data) {
		super(data.getPlayer(), Bukkit.createInventory(data.getPlayer(), data.getMaxStorage() <= 9 ? 9 : 18, Component.text("Storage", NamedTextColor.GOLD)));
		new PlayerSessionInventory(data);
		this.data = data;
		isShop = data.getSession().getInstance() instanceof ShopInstance;
		setupInventory();
	}
	
	public StorageInventory(PlayerSessionData data, Player spectator) {
		super(spectator, Bukkit.createInventory(spectator, data.getMaxStorage() <= 9 ? 9 : 18, Component.text(data.getData().getDisplay() + "'s Storage", NamedTextColor.GOLD)));
		this.data = data;
		this.spectator = spectator;
		setupInventory();
	}
	
	private void setupInventory() {
		p.playSound(p, Sound.BLOCK_ENDER_CHEST_OPEN, 1F, 1F);
		ItemStack[] contents = inv.getContents();
		Equipment[] storage = data.getStorage();
		for (int i = 0; i < storage.length; i++) {
			if (storage[i] == null) continue;
			contents[i] = storage[i].getItem();
		}
		
		if (isShop) {
			contents[SELL] = CoreInventory.createButton(
					Material.GOLD_NUGGET, Component.text("Sell Items", NamedTextColor.RED),
					(TextComponent) NeoCore.miniMessage().deserialize(
							"Drag equipment here to sell it " + "for <yellow>" + ShopInventory.SELL_PRICE + " coins</yellow>."
					), 250, NamedTextColor.GRAY
			);
		}

		for (int i = data.getMaxStorage(); i < contents.length; i++) {
			if (contents[i] != null) continue;
			contents[i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
		}
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		if (spectator != null) {
			e.setCancelled(true);
			return;
		}
		PlayerSessionInventory pinv = (PlayerSessionInventory) InventoryListener.getLowerInventory(p);
		
		// First check for sells
		if (e.getSlot() == SELL) {
			e.setCancelled(true);
			if (e.getCurrentItem() == null) return;
			NBTItem nbti = new NBTItem(e.getCursor());
			Equipment eq = Equipment.get(nbti.getString("equipId"), false);
			if (eq.isCursed()) {
				Util.displayError(p, "Curses cannot be sold, they must be removed!");
				return;
			}
			data.addCoins(ShopInventory.SELL_PRICE);
			p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
			p.setItemOnCursor(null);
			data.getSession().broadcast(
					SharedUtil.color("<yellow>" + p.getName() + " </yellow>sold their ").append(eq.getHoverable())
							.append(Component.text("."))
			);
		}

		// Ignore gray panes
		if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE) {
			e.setCancelled(true);
			return;
		}

		ItemStack clicked = e.getCurrentItem();
		ItemStack cursor = e.getCursor();
		NBTItem nclicked = e.getCurrentItem() != null ? new NBTItem(e.getCurrentItem()) : null;
		NBTItem ncursor = !e.getCursor().isEmpty() ? new NBTItem(e.getCursor()) : null;
		
		// If right click with empty hand, open glossary, disgusting code due to if statement handling
		if (e.isRightClick() && nclicked != null && nclicked.hasTag("equipId") && e.getCursor().getType().isAir()) {
			e.setCancelled(true);
			new BukkitRunnable() {
				public void run() {
					handleInventoryClose();
					new EquipmentGlossaryInventory(p, Equipment.get(nclicked.getString("equipId"), false), null);
				}
			}.runTask(NeoRogue.inst());
			return;
		}
		// Handle shift clicks
		else if (e.isShiftClick()) {
			if (nclicked == null || !nclicked.hasTag("equipId")) return;
			e.setCancelled(true);
			if (!pinv.canShiftClickIn(inv.getItem(e.getSlot()))) return;
			pinv.handleShiftClickIn(e, inv.getItem(e.getSlot()));
			e.setCurrentItem(null);
			p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
		}
		// Handle left click
		else {
			// First check for curses in cursor
			if (ncursor != null && ncursor.hasTag("equipId")) {
				Equipment eq = Equipment.get(ncursor.getString("equipId"), ncursor.getBoolean("isUpgraded"));
				if (eq.isCursed()) {
					displayError("You can't unequip cursed items!", false);
					e.setCancelled(true);
					return;
				}
			}

			// Picking up an item
			if (nclicked != null && nclicked.hasTag("equipId") && ncursor == null) {
				e.setCancelled(true);
				Equipment eq = Equipment.get(nclicked.getString("equipId"), false);
				p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
				p.setItemOnCursor(e.getCurrentItem());
				inv.setItem(e.getSlot(), null);
				if (e.getSlot() == SELL) {
					pinv.clearHighlights();
				}
				else {
					pinv.setHighlights(eq.getType());
				}
			}
			// Swapping an item
			else if (ncursor != null && nclicked != null) {
				e.setCancelled(true);
				if (!nclicked.hasTag("equipId") || !ncursor.hasTag("equipId")) {
					return;
				}
				p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
				String eqId = ncursor.getString("equipId");
				String eqedId = nclicked.getString("equipId");
				Equipment eq = Equipment.get(eqId, ncursor.getBoolean("isUpgraded"));
				Equipment eqed = Equipment.get(eqedId, nclicked.getBoolean("isUpgraded"));
				// Reforged item check
				if (eq.containsReforgeOption(eqedId)) {
					if (!Equipment.canReforge(eq, eqed)) {
						displayError("At least one of the items must be upgraded to reforge!", true);
						return;
					}
					new BukkitRunnable() {
						public void run() {
							p.setItemOnCursor(null);
							inv.setItem(e.getSlot(), null);
							handleInventoryClose();
							new ReforgeOptionsInventory(data, eq, eqed);
						}
					}.runTask(NeoRogue.inst());
					return;
				}
				else if (eqed != null && eqed.containsReforgeOption(eqId)) {
					if (!Equipment.canReforge(eq, eqed)) {
						displayError("At least one of the items must be upgraded to reforge!", true);
						return;
					}
					new BukkitRunnable() {
						public void run() {
							p.setItemOnCursor(null);
							inv.setItem(e.getSlot(), null);
							handleInventoryClose();
							new ReforgeOptionsInventory(data, eqed, eq);
						}
					}.runTask(NeoRogue.inst());
					return;
				}
				// Prevent stacking
				else if (e.getCursor().isSimilar(e.getCurrentItem())) {
					p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
				}
				
				inv.setItem(e.getSlot(), cursor);
				p.setItemOnCursor(clicked);
			}
			// dropping an item
			else if (nclicked == null && ncursor != null) {
				e.setCancelled(true);
				p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
				pinv.clearHighlights();
				inv.setItem(e.getSlot(), cursor);
				p.setItemOnCursor(null);
			}
		}
	}

	private void displayError(String error, boolean closeInventory) {
		p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 0.7F);
		Util.msg(p, error);
		if (closeInventory) p.closeInventory();
	}
	
	public void handleInventoryClose() {
		if (spectator != null) return;
		// Save storage
		Equipment[] newSave = new Equipment[PlayerSessionData.MAX_STORAGE_SIZE];
		int iter = 0;
		ItemStack[] contents = inv.getContents();
		
		// Ignores the sell slot
		for (int i = 0; i < contents.length - 1; i++) {
			ItemStack item = contents[i];
			if (item == null || item.getType() == Material.GRAY_STAINED_GLASS_PANE) continue;
			NBTItem nbti = new NBTItem(item);
			Equipment eq = Equipment.get(nbti.getString("equipId"), nbti.getBoolean("isUpgraded"));
			if (eq == null) {
				Bukkit.getLogger().warning("[NeoRogue] Failed to save item " + nbti.getString("equipId") + " to storage of " + p.getName());
				continue;
			}
			newSave[iter++] = eq;
		}
		data.setStorage(newSave);
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		handleInventoryClose();
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}

	@Override
	public boolean canShiftClickIn(ItemStack item) {
		return inv.firstEmpty() != -1;
	}

	@Override
	public void handleShiftClickIn(InventoryClickEvent ev, ItemStack item) {
		inv.addItem(item);
	}
}
