package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GlossaryViewInventory extends GlossaryInventory {
	private final ArrayList<Equipment> equips;

	public GlossaryViewInventory(Player p, ArrayList<Equipment> equips, Component title, CoreInventory prev) {
		super(p, calculateSize(equips.size()), title, prev);
		this.equips = equips;

		ItemStack[] contents = inv.getContents();
		for (int i = 0; i < equips.size() && i < contents.length; i++) {
			contents[i] = equips.get(i).getItem();
		}
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getCurrentItem() == null) return;
		if (!e.isRightClick()) return;
		NBTItem nbti = new NBTItem(e.getCurrentItem());
		if (!nbti.getKeys().contains("equipId")) return;
		openOther = false;
		new BukkitRunnable() {
			public void run() {
				new EquipmentGlossaryInventory(p, Equipment.get(nbti.getString("equipId"), false), GlossaryViewInventory.this);
			}
		}.runTask(NeoRogue.inst());
	}

	protected static int calculateSize(int numItems) {
		int size = numItems + (9 - numItems % 9);
		if (size < 9) size = 9;
		if (size > 54) size = 54;
		return size;
	}
}
