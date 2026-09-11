package me.neoblade298.neorogue.session.shrine;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.PlayerSessionInventory;
import me.neoblade298.neorogue.session.instances.ShrineInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ShrineChoiceInventory extends CoreInventory {
	private ShrineInstance inst;

	public ShrineChoiceInventory(Player p, @Nullable PlayerSessionData data, ShrineInstance inst) {
		super(p, Bukkit.createInventory(p, 9, Component.text("Shrine Choice", NamedTextColor.BLUE)));
		this.inst = inst;
		if (data != null) InventoryListener.registerPlayerInventory(p, new PlayerSessionInventory(data));
		ItemStack[] contents = inv.getContents();
		for (int i = 0; i < 4; i++) {
			contents[i] = CoreInventory.createButton(Material.SOUL_LANTERN, Component.text("Rest", NamedTextColor.GREEN), 
					"Heal for 35% of your max health.", 250, NamedTextColor.GRAY);
			contents[5 + i] = CoreInventory.createButton(Material.ANVIL, Component.text("Upgrade", NamedTextColor.GOLD), 
					"Upgrade 1 of your equipment.", 250, NamedTextColor.GRAY);
		}
		contents[4] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		Inventory iclicked = e.getClickedInventory();
		if (iclicked == null || iclicked.getType() != InventoryType.CHEST) return;
		e.setCancelled(true);
		
		int slot = e.getSlot();
		if (slot < 4) {
			inst.chooseState(p, true);
			new BukkitRunnable() {
				public void run() {
					p.closeInventory();
				}
			}.runTask(NeoRogue.inst());
		}
		else if (slot > 4) {
			inst.chooseState(p, false);
			new BukkitRunnable() {
				public void run() {
					p.closeInventory();
					new ShrineUpgradeInventory(p, data, inst);
				}
			}.runTask(NeoRogue.inst());
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
