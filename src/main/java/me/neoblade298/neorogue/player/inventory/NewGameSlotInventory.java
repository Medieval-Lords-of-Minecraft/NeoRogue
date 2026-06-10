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
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.SessionSnapshot;
import me.neoblade298.neorogue.session.SessionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class NewGameSlotInventory extends CoreInventory {
	private static final int BACK = 22;
	private static final int SLOT_START = 11;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy h:mma");

	static {
		sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
	}

	private PlayerData pd;

	public NewGameSlotInventory(Player p, PlayerData pd) {
		super(p, Bukkit.createInventory(p, 27, Component.text("New Game - Select Slot", NamedTextColor.GREEN)));
		this.pd = pd;
		setupInventory();
	}

	private void setupInventory() {
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		ItemStack[] contents = inv.getContents();

		for (int i = 1; i <= pd.getSlots(); i++) {
			SessionSnapshot snap = pd.getSnapshot(i);
			int slot = SLOT_START + (i - 1);
			if (snap != null) {
				ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
				ItemMeta meta = item.getItemMeta();
				meta.displayName(Component.text("Slot " + i + " (Overwrite)", NamedTextColor.YELLOW));
				List<Component> lore = new ArrayList<>();
				lore.add(Component.text("Last saved: " + sdf.format(new Date(snap.getLastSaved())), NamedTextColor.GRAY));
				lore.add(Component.text("Area: " + snap.getRegionType().getDisplay(), NamedTextColor.GRAY));
				lore.add(Component.text("Nodes visited: " + snap.getNodesVisited(), NamedTextColor.GRAY));
				lore.add(Component.text("Party:", NamedTextColor.GRAY));
				for (Entry<String, EquipmentClass> ent : snap.getParty().entrySet()) {
					lore.add(Component.text("  " + ent.getKey() + " [" + ent.getValue().getDisplay() + "]", NamedTextColor.GRAY));
				}
				lore.add(Component.empty());
				lore.add(Component.text("Click to start a new game on this slot", NamedTextColor.GREEN));
				meta.lore(lore);
				item.setItemMeta(meta);
				contents[slot] = item;
			}
			else {
				contents[slot] = CoreInventory.createButton(Material.GREEN_STAINED_GLASS_PANE,
						Component.text("Slot " + i + " (Empty)", NamedTextColor.GREEN));
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
			if (SessionManager.getSession(p) != null) {
				Util.displayError(p, "You're already in a session!");
				return;
			}
			p.closeInventory();
			SessionManager.createSession(p, saveSlot, true);
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
