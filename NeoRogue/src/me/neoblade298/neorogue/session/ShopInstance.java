package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerSessionData;

public class ShopInstance implements EditInventoryInstance {
	private static final int SHOP_X = 4, SHOP_Z = 94, NUM_ITEMS = 10;
	
	private HashMap<UUID, ArrayList<Equipment>> shops = new HashMap<UUID, ArrayList<Equipment>>();
	private HashSet<UUID> ready = new HashSet<UUID>();
	private Session s;
	private Entity trader;
	
	public ShopInstance() {}
	
	public ShopInstance(HashMap<UUID, PlayerSessionData> party) {
		for (Entry<UUID, PlayerSessionData> ent : party.entrySet()) {
			shops.put(ent.getKey(), Equipment.deserializeAsArrayList(ent.getValue().getInstanceData()));
		}
	}

	@Override
	public void start(Session s) {
		this.s = s;
		Location loc = new Location(Bukkit.getWorld(Area.WORLD_NAME), -(s.getXOff() + SHOP_X), 64, s.getZOff() + SHOP_Z);
		Location mob = loc.clone().add(0, 0, 3);
		this.trader = mob.getWorld().spawnEntity(mob, EntityType.WANDERING_TRADER);
		for (PlayerSessionData data : s.getParty().values()) {
			Player p = data.getPlayer();
			EquipmentClass ec = data.getPlayerClass().toEquipmentClass();
			p.teleport(loc);
			ArrayList<Equipment> shopItems = new ArrayList<Equipment>();
			shopItems.addAll(Equipment.getDrop(s.getAreasCompleted() + 2, NUM_ITEMS / 2, ec, EquipmentClass.SHOP));
			shopItems.addAll(Equipment.getDrop(s.getAreasCompleted() + 3, NUM_ITEMS / 2, ec, EquipmentClass.SHOP));
			shops.put(p.getUniqueId(), shopItems);
		}
	}

	@Override
	public void cleanup() {
		trader.remove();
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.CHEST) {
			e.setCancelled(true);
			
			if (ready.contains(uuid)) {
				Util.displayError(p, "&cYou've already been marked as ready! Press the button again to un-ready!");
				return;
			}
			
			if (shops.get(uuid).isEmpty()) {
				Util.displayError(p, "&cYou don't have any shop items remaining!");
				return;
			}
			p.playSound(p, Sound.BLOCK_ENDER_CHEST_OPEN, 1F, 1F);
			new ShopInventory(s.getParty().get(uuid), shops.get(uuid));
		}

		else if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.STONE_BUTTON) {
			handleReady(p);
		}
	}
	
	public void handleReady(Player p) {
		UUID uuid = p.getUniqueId();
		if (!ready.contains(uuid)) {
			s.broadcast("&e" + p.getName() + " &7is &aready&7!");
			ready.add(uuid);
			
			if (ready.size() == s.getParty().size()) {
				s.broadcast("&7Everyone is ready! Teleporting back to node select...");
				s.broadcastSound(Sound.ENTITY_PLAYER_LEVELUP);
				new BukkitRunnable() {
					public void run() {
						s.setInstance(new NodeSelectInstance());
					}
				}.runTaskLater(NeoRogue.inst(), 60L);
			}
		}
		else {
			s.broadcast("&e" + p.getName() + " &7is no longer ready&7!");
			ready.remove(uuid);
		}
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		for (Entry<UUID, ArrayList<Equipment>> ent : shops.entrySet()) {
			PlayerSessionData data = party.get(ent.getKey());
			data.setInstanceData(Equipment.serialize(ent.getValue()));
		}
		return "SHOP:";
	}
}
