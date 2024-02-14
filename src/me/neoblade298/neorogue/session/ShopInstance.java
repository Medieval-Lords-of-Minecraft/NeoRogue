package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerSessionData;

public class ShopInstance extends EditInventoryInstance {
	private static final double SPAWN_X = Session.SHOP_X + 5.5, SPAWN_Z = Session.SHOP_Z + 2.5,
			HOLO_X = 0, HOLO_Y = 1, HOLO_Z = 4;
	static final int NUM_ITEMS = 10;
	
	private HashMap<UUID, ArrayList<ShopItem>> shops = new HashMap<UUID, ArrayList<ShopItem>>();
	private HashSet<UUID> ready = new HashSet<UUID>();
	private Hologram holo;
	
	public ShopInstance(Session s) {
		super(s, SPAWN_X, SPAWN_Z);
	}
	
	public ShopInstance(Session s, HashMap<UUID, PlayerSessionData> party) {
		this(s);
		for (Entry<UUID, PlayerSessionData> ent : party.entrySet()) {
			shops.put(ent.getKey(), ShopItem.deserializeShopItems(ent.getValue().getInstanceData()));
		}
	}

	@Override
	public void start() {
		for (PlayerSessionData data : s.getParty().values()) {
			Player p = data.getPlayer();
			EquipmentClass ec = data.getPlayerClass();
			p.teleport(spawn);
			
			// Create shop contents
			ArrayList<Equipment> equips = new ArrayList<Equipment>();
			equips.addAll(Equipment.getDrop(s.getAreasCompleted() + 1, NUM_ITEMS / 2, ec, EquipmentClass.SHOP));
			equips.addAll(Equipment.getDrop(s.getAreasCompleted() + 2, NUM_ITEMS / 2, ec, EquipmentClass.SHOP));
			
			// Generate 2 random unique sale slots
			HashSet<Integer> saleSlots = new HashSet<Integer>(2);
			while (saleSlots.size() < 2) {
				saleSlots.add(NeoRogue.gen.nextInt(equips.size()));
			}

			// Turn equipment into shop items
			ArrayList<ShopItem> shopItems = new ArrayList<ShopItem>();
			for (int i = 0; i < equips.size(); i++) {
				shopItems.add(new ShopItem(equips.get(i), ShopInventory.SLOT_ORDER[i], i >= NUM_ITEMS / 2, saleSlots.contains(i)));
			}
			shops.put(p.getUniqueId(), shopItems);
		}
		for (UUID uuid : s.getSpectators()) {
			Player p = Bukkit.getPlayer(uuid);
			p.teleport(spawn);
		}

		// Setup hologram
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("Open the chest, then click the");
		lines.add("stone button when you're ready!");
		Plot plot = s.getPlot();
		holo = DHAPI.createHologram(plot.getXOffset() + "-" + plot.getZOffset() + "-shop", spawn.clone().add(HOLO_X, HOLO_Y, HOLO_Z), lines);
	}

	@Override
	public void cleanup() {
		holo.delete();
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.CHEST) {
			e.setCancelled(true);
			
			if (ready.contains(uuid)) {
				Util.displayError(p, "You've already been marked as ready! Press the button again to un-ready!");
				return;
			}
			
			if (shops.get(uuid).isEmpty()) {
				Util.displayError(p, "You don't have any shop items remaining!");
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
			s.broadcast("<yellow>" + p.getName() + " <gray>is <green>ready</green>!");
			ready.add(uuid);
			
			if (ready.size() == s.getParty().size()) {
				s.broadcast("Everyone is ready! Teleporting back to node select...");
				s.broadcastSound(Sound.ENTITY_PLAYER_LEVELUP);
				new BukkitRunnable() {
					public void run() {
						s.setInstance(new NodeSelectInstance(s));
					}
				}.runTaskLater(NeoRogue.inst(), 60L);
			}
		}
		else {
			s.broadcast("<yellow>" + p.getName() + " <gray>is no longer ready!");
			ready.remove(uuid);
		}
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		for (Entry<UUID, ArrayList<ShopItem>> ent : shops.entrySet()) {
			PlayerSessionData data = party.get(ent.getKey());
			data.setInstanceData(ShopItem.serializeShopItems(ent.getValue()));
		}
		return "SHOP:";
	}
}
