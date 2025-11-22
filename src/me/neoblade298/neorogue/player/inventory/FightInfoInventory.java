package me.neoblade298.neorogue.player.inventory;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.Mob;
import me.neoblade298.neorogue.session.fight.MobModifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class FightInfoInventory extends CoreInventory {
	public FightInfoInventory(Player viewer, @Nullable PlayerSessionData data, AbstractMap<Mob, ArrayList<MobModifier>> mobs, boolean hasCustomMobInfo) {
		super(viewer, Bukkit.createInventory(viewer, mobs.size() + (9 - mobs.size() % 9) + 9, Component.text("Fight Info", NamedTextColor.BLUE)));
		if (data != null) InventoryListener.registerPlayerInventory(p, new PlayerSessionInventory(data));
		ItemStack[] contents = inv.getContents();
		
		int pos = 0;
		for (Entry<Mob, ArrayList<MobModifier>> ent : mobs.entrySet()) {
			Mob mob = ent.getKey();
			contents[pos++] = mob.getItemDisplay(ent.getValue());

			// Only show summons if there's no custom mob order, since the mob order overrides everything
			if (!hasCustomMobInfo && mob.getSummons() != null) {
				for (String summonStr : mob.getSummons()) {
					Mob summon = Mob.get(summonStr);
					if (summon == null) {
						Bukkit.getLogger().warning("[NeoRogue] Failed to load summon " + summonStr + " for mob " + ent.getKey().getId());
						continue;
					}
					contents[pos++] = summon.getItemDisplay(ent.getValue());
				}
			}
		}
		
		inv.setContents(contents);
	}
	
	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.isRightClick() && e.getCurrentItem() != null) {
			NBTItem nbti = new NBTItem(e.getCurrentItem());
			if (!nbti.getKeys().contains("mobId")) return;
			
			Mob mob = Mob.get(nbti.getString("mobId"));
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
