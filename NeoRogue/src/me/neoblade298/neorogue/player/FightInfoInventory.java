package me.neoblade298.neorogue.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

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

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fights.Mob;
import me.neoblade298.neorogue.session.fights.MobModifier;

public class FightInfoInventory extends CoreInventory {
	public FightInfoInventory(Player viewer, TreeMap<Mob, ArrayList<MobModifier>> mobs) {
		super(viewer, Bukkit.createInventory(viewer, mobs.size() + (9 - mobs.size() % 9) + 9, "§9Fight Info"));

		ItemStack[] contents = inv.getContents();
		
		int pos = 0;
		for (Entry<Mob, ArrayList<MobModifier>> ent : mobs.entrySet()) {
			contents[pos++] = ent.getKey().getItemDisplay(ent.getValue());
		}
		
		inv.setContents(contents);
	}
	
	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
	}
	
	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		
	}
	
	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
