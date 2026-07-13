package me.neoblade298.neorogue.player.inventory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.SessionSnapshot;
import me.neoblade298.neorogue.session.SessionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

// Unified host menu: empty slots start a new game (left click), filled slots load (left click) or
// delete (right click). Replaces the separate New/Load slot inventories.
public class HostGameInventory extends CoreInventory {
	private static final int BACK = 22;
	private static final int SLOT_START = 11;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy h:mma");

	static {
		sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
	}

	private PlayerData pd;

	public HostGameInventory(Player p, PlayerData pd) {
		super(p, Bukkit.createInventory(p, 27, Component.text("Host Game - Select Slot", NamedTextColor.GOLD)));
		this.pd = pd;
		setupInventory();
	}

	private void setupInventory() {
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		ItemStack[] contents = new ItemStack[inv.getSize()];

		for (int i = 1; i <= pd.getSlots(); i++) {
			SessionSnapshot snap = pd.getSnapshot(i);
			int slot = SLOT_START + (i - 1);
			if (snap != null) {
				ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
				ItemMeta meta = item.getItemMeta();
				meta.displayName(Component.text("Slot " + i, NamedTextColor.GOLD));
				List<Component> lore = new ArrayList<>();
				lore.add(Component.text("Last saved: " + sdf.format(new Date(snap.getLastSaved())), NamedTextColor.GRAY));
				lore.add(Component.text("Region: " + snap.getRegionType().getDisplay(), NamedTextColor.GRAY));
				lore.add(Component.text("Nodes visited: " + snap.getNodesVisited(), NamedTextColor.GRAY));
				lore.add(Component.text("Party:", NamedTextColor.GRAY));
				for (Entry<String, EquipmentClass> ent : snap.getParty().entrySet()) {
					lore.add(Component.text("  " + ent.getKey() + " [" + ent.getValue().getDisplay() + "]", NamedTextColor.GRAY));
				}
				lore.add(Component.empty());
				lore.add(Component.text("Left click to load this game", NamedTextColor.GOLD));
				lore.add(Component.text("Right click to delete this save", NamedTextColor.RED));
				meta.lore(lore);
				item.setItemMeta(meta);
				contents[slot] = item;
			}
			else {
				ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
				ItemMeta meta = item.getItemMeta();
				meta.displayName(Component.text("Slot " + i + " (Empty)", NamedTextColor.GREEN));
				meta.lore(List.of(Component.text("Left click to start a new game", NamedTextColor.GREEN)));
				item.setItemMeta(meta);
				contents[slot] = item;
			}
		}

		contents[BACK] = CoreInventory.createButton(Material.BARRIER, Component.text("Back", NamedTextColor.RED));
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.CHEST) return;
		if (e.getCurrentItem() == null) return;

		int clickedSlot = e.getSlot();
		if (clickedSlot == BACK) {
			new MainMenuInventory(p);
			return;
		}

		if (clickedSlot >= SLOT_START && clickedSlot < SLOT_START + pd.getSlots()) {
			int saveSlot = clickedSlot - SLOT_START + 1;
			boolean hasData = pd.getSnapshot(saveSlot) != null;

			if (!hasData) {
				// Empty slot: left click starts a new game
				if (e.isRightClick()) return;
				p.closeInventory();
				SessionManager.tryNewGame(p, saveSlot);
				return;
			}

			if (e.isRightClick()) {
				// Confirm before deleting
				final int fSlot = saveSlot;
				ItemStack display = e.getCurrentItem() != null ? e.getCurrentItem().clone() : null;
				new ConfirmInventory(p, Component.text("Delete Save Slot " + fSlot + "?", NamedTextColor.RED), display,
						() -> {
							SessionManager.deleteSave(p, fSlot);
							p.playSound(p, Sound.ENTITY_ITEM_BREAK, 1F, 1F);
							new HostGameInventory(p, pd);
						},
						() -> new HostGameInventory(p, pd));
			}
			else {
				p.closeInventory();
				SessionManager.tryLoadGame(p, saveSlot);
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
}
