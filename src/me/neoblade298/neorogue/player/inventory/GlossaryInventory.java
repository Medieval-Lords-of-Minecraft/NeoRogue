package me.neoblade298.neorogue.player.inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import net.kyori.adventure.text.Component;

public class GlossaryInventory extends CoreInventory {
	protected CoreInventory prev;
	protected boolean openOther = true;
	
	protected static final int BASIC = 4, UPGRADED = 5, TAGS = 3, PARENTS = 8, CUSTOM = 0;

	public GlossaryInventory(Player viewer, int size, Component title, CoreInventory prev) {
		super(viewer, Bukkit.createInventory(viewer, size, Component.text("Glossary: ").append(title)));
		this.prev = prev;
		Sounds.turnPage.play(p, p);
	}
	
	protected static int calculateSize(int numTags) {
		return numTags + (9 - numTags % 9);
	}
	
	protected static int calculateSize(Equipment eq) {
		return eq.getReforgeOptions().isEmpty() ? 9 : 54;
	}
	
	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (!e.isRightClick()) return;
		if (e.getCurrentItem() == null) return;
		NBTItem nbti = new NBTItem(e.getCurrentItem());
		if (!nbti.getKeys().contains("equipId")) return;
		openOther = false;
		new BukkitRunnable() {
			public void run() {
				new EquipmentGlossaryInventory(p, Equipment.get(nbti.getString("equipId"), false), prev);
			}
		}.runTask(NeoRogue.inst());
	}
	
	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		// Don't open the previous inventory if we're opening another glossary page
		if (openOther) {
			new BukkitRunnable() {
				public void run() {
					if (prev != null) prev.openInventory();
				}
			}.runTask(NeoRogue.inst());
		}
	}
	
	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
