package me.neoblade298.neorogue.player.inventory;

import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import de.tr7zw.nbtapi.NBT;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.Mob;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class FightInfoInventory extends CoreInventory {
	public FightInfoInventory(Player viewer, Session s, @Nullable PlayerSessionData data, TreeSet<Mob> mobs, boolean isChance) {
		super(viewer, Bukkit.createInventory(viewer, mobs.size() + (9 - mobs.size() % 9) + 9, Component.text("Fight Info", NamedTextColor.BLUE)));
		if (data != null) InventoryListener.registerPlayerInventory(p, new PlayerSessionInventory(data));
		ItemStack[] contents = inv.getContents();
		
		int pos = 0;
		for (Mob mob : mobs) {
			contents[pos++] = mob.getItemDisplay(s, null, isChance);

			if (mob.getSummons() != null) {
				for (String summonStr : mob.getSummons()) {
					Mob summon = Mob.get(summonStr);
					if (summon == null) {
						Bukkit.getLogger().warning("[NeoRogue] Failed to load summon " + summonStr + " for mob " + mob.getId());
						continue;
					}
					contents[pos++] = summon.getItemDisplay(s, null, isChance);
				}
			}
		}
		
		inv.setContents(contents);
	}
	
	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.isRightClick() && e.getCurrentItem() != null) {
			String mobId = NBT.get(e.getCurrentItem(), nbt -> nbt.getKeys().contains("mobId") ? nbt.getString("mobId") : null);
			if (mobId == null) return;
			
			Mob mob = Mob.get(mobId);
			if (mob.getTags().isEmpty()) return;
			new MobGlossaryInventory(p, mob, this);
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
