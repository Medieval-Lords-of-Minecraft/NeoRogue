package me.neoblade298.neorogue.session.shrine;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.nbtapi.NBT;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.PlayerSessionInventory;
import me.neoblade298.neorogue.player.inventory.ShiftClickableInventory;
import me.neoblade298.neorogue.session.instances.ShrineInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class ShrineUpgradeInventory extends CoreInventory implements ShiftClickableInventory {
	private ShrineInstance inst;
	private PlayerSessionData data;
	
	public ShrineUpgradeInventory(Player p, PlayerSessionData data, ShrineInstance inst) {
		super(p, Bukkit.createInventory(p, InventoryType.SMITHING, Component.text("Upgrade Equipment", NamedTextColor.BLUE)));
		this.inst = inst;
		this.data = data;
		// Render the player's equipment (and any spectator's view) as upgrade previews while this UI is open.
		data.setPreviewingUpgrades(true);
		InventoryListener.registerPlayerInventory(p, new PlayerSessionInventory(data));
		data.setupInventory();
		ItemStack[] contents = inv.getContents();
		contents[1] = CoreInventory.createButton(Material.PAPER, Component.text("Upgrade", NamedTextColor.BLUE),
				(TextComponent) NeoCore.miniMessage().deserialize("<gray>Place an item on the left to see what it upgrades into. "
				+ "<red>To skip upgrading, shift right click this paper."), 250, NamedTextColor.GRAY);
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		if (e.getAction() == InventoryAction.HOTBAR_SWAP) {
			e.setCancelled(true);
			return;
		}
		int slot = e.getSlot();
		
		if (slot == 0) {
			PlayerSessionInventory pinv = (PlayerSessionInventory) InventoryListener.getLowerInventory(p);
			if (e.isShiftClick()) {
				if (e.getCurrentItem() == null) return;
				if (!pinv.canShiftClickIn(inv.getItem(0))) return;
				pinv.handleShiftClickIn(e, inv.getItem(0));
				p.playSound(p, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1F, 1F);
				e.setCancelled(true);
				inv.setItem(0, null);
				updateOutput();
				return;
			}

			if (e.getCurrentItem() == null && e.getCursor() == null) return;
			
			if (e.getCursor() != null) {
				pinv.clearHighlights();
			}
			if (e.getCurrentItem() != null) {
				Equipment eq = Equipment.get(NBT.get(e.getCurrentItem(), nbt -> { return nbt.getString("equipId"); }), false);
				pinv.setHighlights(eq.getType());
			}
			p.playSound(p, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1F, 1F);
			// Has to be done with runnable to avoid manually coding swap cases and such
			new BukkitRunnable() {
				public void run() {
					updateOutput();
				}
			}.runTask(NeoRogue.inst());
		}
		else if (slot == 1) {
			e.setCancelled(true);
			if (e.isShiftClick() && e.isRightClick()) {
				inst.useUpgrade(p.getUniqueId());
			}
			p.closeInventory();
		}
		else if (slot == 2) {
			e.setCancelled(true);
			SessionEquipment input = SessionEquipment.fromItem(inv.getItem(0));
			if (input == null || input.getEquipment().isUpgraded() || !input.getEquipment().canUpgrade()) {
				Util.displayError(p, "Invalid upgrade!");
				updateOutput();
				return;
			}

			p.playSound(p, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1F, 1F);
			inv.setItem(0, null);
			inv.setItem(2, null);
			data.giveEquipment(input.upgrade(),
					SharedUtil.color("You upgraded to a(n) "),
					SharedUtil.color("<yellow>" + p.getName() + "</yellow> upgraded to a(n) "), false);
			p.playSound(p, Sound.BLOCK_ANVIL_USE, 1F, 1F);
			inst.useUpgrade(p.getUniqueId());
			p.closeInventory();
		}
		else {
			e.setCancelled(true);
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		// Stop previewing and restore the normal equipment display.
		data.setPreviewingUpgrades(false);
		if (inv.getItem(0) != null) {
			SessionEquipment placed = SessionEquipment.fromItem(inv.getItem(0));
			// Returning the item the player placed in - it was already owned, don't re-fire acquire
			if (placed != null) data.giveEquipment(placed, null, null, false);
		}
		data.setupInventory();
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
	
	@Override
	public boolean canShiftClickIn(ItemStack item) {
		return inv.getItem(0) == null;
	}

	@Override
	public void handleShiftClickIn(InventoryClickEvent ev, ItemStack item) {
		inv.setItem(0, item);
		updateOutput();
	}

	public void updateOutput() {
		ItemStack item = inv.getItem(0);
		if (item == null) {
			inv.setItem(2, null);
		}
		else {
			SessionEquipment input = SessionEquipment.fromItem(item);
			if (input == null) {
				inv.setItem(2, CoreInventory.createButton(Material.BARRIER, Component.text("This item is not equipment", NamedTextColor.RED)));
				return;
			}
			if (input.getEquipment().isUpgraded() || !input.getEquipment().canUpgrade()) {
				inv.setItem(2, CoreInventory.createButton(Material.BARRIER, Component.text("This item is already upgraded", NamedTextColor.RED)));
				return;
			}
			
			inv.setItem(2, input.upgrade().getItem());
		}
	}
}
