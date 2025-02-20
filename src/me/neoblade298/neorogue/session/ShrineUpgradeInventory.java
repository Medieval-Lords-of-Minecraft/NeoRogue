package me.neoblade298.neorogue.session;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
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
import me.neoblade298.neorogue.player.inventory.PlayerSessionInventory;
import me.neoblade298.neorogue.player.inventory.ShiftClickableInventory;
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
		InventoryListener.registerPlayerInventory(p, new PlayerSessionInventory(data));
		ItemStack[] contents = inv.getContents();
		contents[1] = CoreInventory.createButton(Material.PAPER, Component.text("Upgrade", NamedTextColor.BLUE),
				(TextComponent) NeoCore.miniMessage().deserialize("<gray>Place an item on the left to see what it upgrades into. "
				+ "<red>To skip upgrading, shift left click this paper."), 250, NamedTextColor.GRAY);
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		int slot = e.getSlot();
		
		if (slot == 0) {
			PlayerSessionInventory pinv = (PlayerSessionInventory) InventoryListener.getLowerInventory(p);
			if (e.isShiftClick()) {
				if (e.getCurrentItem() == null) return;
				if (!pinv.canShiftClickIn(inv.getItem(0))) return;
				pinv.handleShiftClickIn(inv.getItem(0));
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
				NBTItem nbti = new NBTItem(e.getCurrentItem());
				Equipment eq = Equipment.get(nbti.getString("equipId"), false);
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
			if (e.isShiftClick()) {
				inst.useUpgrade(p.getUniqueId());
			}
			p.closeInventory();
		}
		else {
			e.setCancelled(true);
			ItemStack item = e.getCurrentItem();
			NBTItem nbti = new NBTItem(item);
			String id = nbti.getString("equipId");
			if (id.isBlank()) {
				Util.displayError(p, "Invalid upgrade!");
				return;
			}

			p.playSound(p, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1F, 1F);
			inv.setItem(0, null);
			Equipment eq = Equipment.get(id, true);
			data.giveEquipment(eq,
					SharedUtil.color("You upgraded to a(n) "),
					SharedUtil.color("<red>" + p.getName() + "</red> upgraded to a(n) "));
			p.playSound(p, Sound.BLOCK_ANVIL_USE, 1F, 1F);
			inst.useUpgrade(p.getUniqueId());
			p.closeInventory();
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		if (inv.getItem(0) != null) {
			NBTItem nbti = new NBTItem(inv.getItem(0));
			Equipment eq = Equipment.get(nbti.getString("equipId"), nbti.getBoolean("isUpgraded"));
			data.giveEquipmentSilent(eq);
		}
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
	public void handleShiftClickIn(ItemStack item) {
		inv.setItem(0, item);
		updateOutput();
	}

	public void updateOutput() {
		ItemStack item = inv.getItem(0);
		if (item == null) {
			inv.setItem(2, null);
		}
		else {
			NBTItem nbti = new NBTItem(item);
			String id = nbti.getString("equipId");
			boolean isUpgraded = nbti.getBoolean("isUpgraded");
			if (id.isBlank()) {
				inv.setItem(2, CoreInventory.createButton(Material.BARRIER, Component.text("This item is not equipment", NamedTextColor.RED)));
				return;
			}
			if (isUpgraded) {
				inv.setItem(2, CoreInventory.createButton(Material.BARRIER, Component.text("This item is already upgraded", NamedTextColor.RED)));
				return;
			}
			
			inv.setItem(2, Equipment.get(id, true).getItem());
		}
	}
}
