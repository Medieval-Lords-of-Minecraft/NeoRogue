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

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.EditInventoryInstance;
import me.neoblade298.neorogue.session.NodeSelectInstance;
import me.neoblade298.neorogue.session.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class RewardInstance extends EditInventoryInstance {
	private static final double SPAWN_X = Session.REWARDS_X + 4.5, SPAWN_Z = Session.REWARDS_Z + 2.5;
	private HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();
	
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
	}

	@Override
	public void cleanup() {
		
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock().getType() != Material.CHEST) return;
		if (e.getHand() != EquipmentSlot.HAND) return;
		e.setCancelled(true);
		
		
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		if (rewards.get(uuid).isEmpty()) {
			Util.displayError(p, "You don't have any rewards remaining!");
			return;
		}
		p.playSound(p, Sound.BLOCK_ENDER_CHEST_OPEN, 1F, 1F);
		
		new RewardInventory(s.getParty().get(uuid), Bukkit.createInventory(p, 9, Component.text("Rewards", NamedTextColor.BLUE)), rewards.get(uuid));
	}
	
	public void onRewardClaim() {
		for (ArrayList<Reward> rewards : this.rewards.values()) {
			if (!rewards.isEmpty()) return;
		}
		s.broadcast("Everyone's finished claiming rewards! Returning to node select.");
		s.setInstance(new NodeSelectInstance(s));
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

	@Override
	public void teleportPlayer(Player p) {
		p.teleport(spawn);
		onRewardClaim();
	}
}
