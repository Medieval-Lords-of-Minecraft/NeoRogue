package me.neoblade298.neorogue.player.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ReforgeConfirmInventory extends CoreInventory {
	private PlayerSessionData data;
	private Equipment toReforge, reforgeWith, result;
	private CoreInventory prev;
	private boolean confirmed = false;

	private static final int EQ1_SLOT = 3, EQ2_SLOT = 5, RESULT_SLOT = 13;

	public ReforgeConfirmInventory(PlayerSessionData data, Equipment toReforge, Equipment reforgeWith, Equipment result, CoreInventory prev) {
		super(data.getPlayer(), Bukkit.createInventory(data.getPlayer(), 18, Component.text("Confirm Reforge", NamedTextColor.BLUE)));
		this.data = data;
		this.toReforge = toReforge;
		this.reforgeWith = reforgeWith;
		this.result = result;
		this.prev = prev;

		ItemStack[] contents = inv.getContents();

		contents[EQ1_SLOT] = toReforge.getItem();
		contents[EQ2_SLOT] = reforgeWith.getItem();

		for (int i = 0; i < 9; i++) {
			if (contents[i] != null) continue;
			contents[i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
		}
		contents[RESULT_SLOT] = result.getItem();
		for (int i = 9; i < 18; i++) {
			if (contents[i] != null) continue;
			contents[i] = CoreInventory.createButton(Material.RED_STAINED_GLASS_PANE, Component.text("Cancel", NamedTextColor.RED));
		}

		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		Inventory iclicked = e.getClickedInventory();
		if (iclicked == null || iclicked.getType() != InventoryType.CHEST) return;
		if (e.getCurrentItem() == null) return;

		int slot = e.getSlot();

		// Right-click on items for glossary
		if (e.isRightClick()) {
			Equipment eq = null;
			if (slot == EQ1_SLOT) eq = toReforge;
			else if (slot == EQ2_SLOT) eq = reforgeWith;
			else if (slot == RESULT_SLOT) eq = result;
			if (eq != null) {
				final Equipment glossaryEq = eq;
				new BukkitRunnable() {
					public void run() {
						new EquipmentGlossaryInventory(p, glossaryEq, ReforgeConfirmInventory.this);
					}
				}.runTask(NeoRogue.inst());
			}
			return;
		}

		// Confirm: click result
		if (slot == RESULT_SLOT) {
			confirmed = true;
			p.playSound(p, Sound.BLOCK_ANVIL_USE, 1F, 1F);
			Component cmp = SharedUtil.color("<yellow>" + p.getName() + "</yellow> reforged their ").append(toReforge.getHoverable());
			if (!toReforge.getId().equals(reforgeWith.getId())) cmp = cmp.append(Component.text(", ").append(reforgeWith.getHoverable()));
			cmp = cmp.append(Component.text(" into a(n) ").append(result.getHoverable().append(Component.text("!"))));
			data.getSession().broadcast(cmp);

			toReforge = null;
			reforgeWith = null;
			data.giveEquipment(result, (Component) null, null);
			new BukkitRunnable() {
				public void run() {
					p.closeInventory();
				}
			}.runTask(NeoRogue.inst());
			return;
		}

		// Cancel: click any red pane
		if (slot >= 9) {
			p.closeInventory();
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		if (!confirmed && toReforge != null) {
			data.giveEquipment(toReforge, (Component) null, null);
			data.giveEquipment(reforgeWith, (Component) null, null);
			if (prev != null) {
				new BukkitRunnable() {
					public void run() {
						prev.openInventory();
					}
				}.runTask(NeoRogue.inst());
			}
		}
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
