package me.neoblade298.neorogue.player.inventory;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.PlayerSessionData.EquipmentMetadata;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

// A reusable, paginated UI for selecting one piece of a player's owned equipment.
// Generic: not coupled to chance events. The caller supplies the candidate list and two
// callbacks. onSelect fires with the chosen (still-owned) equipment; onCancel fires when the
// player clicks the cancel button or closes the inventory.
public class EquipmentSelectInventory extends CoreInventory {
	private static final int PAGE_SIZE = 45, PREV = 48, CANCEL = 49, NEXT = 50;

	private final PlayerSessionData data;
	private final List<EquipmentMetadata> options;
	private final Consumer<EquipmentMetadata> onSelect;
	private final Runnable onCancel;
	private int page;
	private boolean resolved;

	public EquipmentSelectInventory(PlayerSessionData data, Component title, List<EquipmentMetadata> options,
			Consumer<EquipmentMetadata> onSelect, Runnable onCancel) {
		super(data.getPlayer(), Bukkit.createInventory(data.getPlayer(), 54, title));
		this.data = data;
		this.options = options;
		this.onSelect = onSelect;
		this.onCancel = onCancel;
		setup();
	}

	private void setup() {
		inv.clear();
		ItemStack[] contents = inv.getContents();

		int start = page * PAGE_SIZE;
		int maxItems = Math.min(options.size() - start, PAGE_SIZE);
		for (int i = 0; i < maxItems; i++) {
			int idx = start + i;
			ItemStack item = options.get(idx).getSessionEquipment().getItem();
			NBTItem nbti = new NBTItem(item);
			nbti.setInteger("selIdx", idx + 1);
			contents[i] = nbti.getItem();
		}

		if (options.isEmpty()) {
			contents[22] = CoreInventory.createButton(Material.BARRIER,
					Component.text("No eligible equipment!", NamedTextColor.RED));
		}

		if (page > 0)
			contents[PREV] = CoreInventory.createButton(ArtifactsInventory.PREV_HEAD, Component.text("Previous Page"));
		if (start + PAGE_SIZE < options.size())
			contents[NEXT] = CoreInventory.createButton(ArtifactsInventory.NEXT_HEAD, Component.text("Next Page"));
		contents[CANCEL] = CoreInventory.createButton(Material.BARRIER, Component.text("Cancel", NamedTextColor.RED));

		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.CHEST) return;
		ItemStack item = e.getCurrentItem();
		if (item == null) return;

		int slot = e.getSlot();
		if (slot == CANCEL) {
			cancel();
			return;
		}
		if (slot == PREV && page > 0) {
			page--;
			setup();
			return;
		}
		if (slot == NEXT && (page + 1) * PAGE_SIZE < options.size()) {
			page++;
			setup();
			return;
		}
		if (slot >= PAGE_SIZE) return;

		int idx = new NBTItem(item).getInteger("selIdx") - 1;
		if (idx < 0 || idx >= options.size()) return;
		EquipmentMetadata meta = options.get(idx);

		// Right-click: open glossary for this equipment
		if (e.isRightClick()) {
			new BukkitRunnable() {
				public void run() {
					new EquipmentGlossaryInventory(p, meta.getEquipment(), EquipmentSelectInventory.this);
				}
			}.runTask(NeoRogue.inst());
			return;
		}

		// Validate the item is still owned in the same slot before committing
		SessionEquipment[] arr = data.getSessionEquipment(meta.getEquipSlot());
		if (arr[meta.getSlot()] != meta.getSessionEquipment()) {
			Util.displayError(p, "That equipment is no longer available!");
			return;
		}

		resolved = true;
		new BukkitRunnable() {
			public void run() {
				onSelect.accept(meta);
			}
		}.runTask(NeoRogue.inst());
	}

	private void cancel() {
		if (resolved) return;
		resolved = true;
		new BukkitRunnable() {
			public void run() {
				onCancel.run();
			}
		}.runTask(NeoRogue.inst());
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		// Closing (e.g. Escape) does not advance the stage; the player can re-open the
		// chance pillar to resume the same choice. Cancel is handled by the Cancel button.
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
