package me.neoblade298.neorogue.session.reward;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.SpectateSelectInventory;
import me.neoblade298.neorogue.session.EditInventoryInstance;
import me.neoblade298.neorogue.session.NodeSelectInstance;
import me.neoblade298.neorogue.session.Plot;
import me.neoblade298.neorogue.session.Session;

public class RewardInstance extends EditInventoryInstance {
	private static final double SPAWN_X = Session.REWARDS_X + 7.5, SPAWN_Z = Session.REWARDS_Z + 3.5,
			HOLO_X = 0, HOLO_Y = 3, HOLO_Z = 6;
	private HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();
	private Hologram holo;
	private boolean busy = false;
	
	public RewardInstance(Session s, HashMap<UUID, ArrayList<Reward>> rewards) {
		super(s, SPAWN_X, SPAWN_Z);
		this.rewards = rewards;
	}
	
	public RewardInstance(Session s, HashMap<UUID, PlayerSessionData> party, boolean useless) {
		super(s, SPAWN_X, SPAWN_Z);
		for (Entry<UUID, PlayerSessionData> ent : party.entrySet()) {
			rewards.put(ent.getKey(), Reward.deserializeArray(ent.getValue().getInstanceData()));
		}
	}

	@Override
	public void start() {
		for (PlayerSessionData data : s.getParty().values()) {
			Player p = data.getPlayer();
			p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
			p.teleport(spawn);
		}
		
		for (UUID uuid : s.getSpectators()) {
			Player p = Bukkit.getPlayer(uuid);
			p.teleport(spawn);
		}
		
		// Setup hologram
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("Open the enderchest and");
		lines.add("collect your reward!");
		Plot plot = s.getPlot();
		holo = DHAPI.createHologram(plot.getXOffset() + "-" + plot.getZOffset() + "-rewards", spawn.clone().add(HOLO_X, HOLO_Y, HOLO_Z), lines);
	}

	@Override
	public void cleanup() {
		holo.delete();
	}

	@Override
	public void handleSpectatorInteractEvent(PlayerInteractEvent e) {
		e.setCancelled(true);
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock().getType() != Material.ENDER_CHEST) return;
		if (e.getHand() != EquipmentSlot.HAND) return;
		new SpectateSelectInventory(s, e.getPlayer(), true);
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock().getType() != Material.ENDER_CHEST) return;
		if (e.getHand() != EquipmentSlot.HAND) return;
		e.setCancelled(true);
		
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		if (rewards.get(uuid).isEmpty()) {
			if (!onRewardClaim()) {
				Util.displayError(p, "You don't have any rewards remaining!");
			}
			return;
		}
		p.playSound(p, Sound.BLOCK_ENDER_CHEST_OPEN, 1F, 1F);
		
		new RewardInventory(s.getParty().get(uuid), rewards.get(uuid));
	}
	
	public void spectateRewards(Player spectator, UUID viewed) {
		new RewardInventory(s.getParty().get(viewed), rewards.get(viewed), spectator);
	}
	
	public boolean onRewardClaim() {
		for (ArrayList<Reward> rewards : this.rewards.values()) {
			if (!rewards.isEmpty()) return false;
		}
		if (!busy) {
			s.broadcast("Everyone's finished claiming rewards! Returning to node select...");
			busy = true;
			new BukkitRunnable() {
				public void run() {
					busy = false;
					s.setInstance(new NodeSelectInstance(s));
				}
			}.runTaskLater(NeoRogue.inst(), 40L);
		}
		return true;
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		for (Entry<UUID, ArrayList<Reward>> ent : rewards.entrySet()) {
			String serialized = "";
			for (Reward r : ent.getValue()) {
				serialized += r.serialize() + ",";
			}
			
			PlayerSessionData data = party.get(ent.getKey());
			data.setInstanceData(serialized);
		}
		return "REWARD";
	}
}
