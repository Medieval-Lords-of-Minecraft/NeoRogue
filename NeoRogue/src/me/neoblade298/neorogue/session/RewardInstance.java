package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import me.neoblade298.neorogue.session.fights.FightScore;

public class RewardInstance implements Instance {
	private HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();
	private Session s;
	
	public RewardInstance(HashMap<UUID, ArrayList<Reward>> rewards) {
		this.rewards = rewards;
	}

	@Override
	public void start(Session s) {
		this.s = s;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() != Material.CHEST) return;
		e.setCancelled(true);
		
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		p.playSound(p, Sound.BLOCK_ENDER_CHEST_OPEN, 1F, 1F);
		
		new RewardInventory(s.getParty().get(uuid), Bukkit.createInventory(p, 9, "§9Rewards"), rewards.get(uuid));
	}
}
