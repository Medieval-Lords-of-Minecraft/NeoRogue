package me.neoblade298.neorogue.session.reward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.SpectateSelectInventory;
import me.neoblade298.neorogue.session.EditInventoryInstance;
import me.neoblade298.neorogue.session.NodeSelectInstance;
import me.neoblade298.neorogue.session.Session;
import net.kyori.adventure.text.Component;

public class RewardInstance extends EditInventoryInstance {
	private static final double SPAWN_X = Session.REWARDS_X + 7.5, SPAWN_Z = Session.REWARDS_Z + 3.5,
			HOLO_X = 0, HOLO_Y = 3, HOLO_Z = 6;
	private HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();
	private TextDisplay holo;
	
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
	public void setup() {
		for (PlayerSessionData data : s.getParty().values()) {
			Player p = data.getPlayer();
			p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
			teleportRandomly(p);
		}
		
		for (UUID uuid : s.getSpectators().keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			teleportRandomly(p);
			p.setAllowFlight(false);
			p.setFlying(false);
		}
		super.setup();
		
		// Setup hologram
		Component text = Component.text("Open the enderchest and").appendNewline().append(Component.text("collect your reward!"));
		holo = NeoRogue.createHologram(spawn.clone().add(HOLO_X, HOLO_Y, HOLO_Z), text);
	}

	@Override
	public void updateBoardLines() {
		playerLines.clear();
		playerLines.add(createBoardLine(s.getParty().get(s.getHost()), true));

		ArrayList<PlayerSessionData> sorted = new ArrayList<PlayerSessionData>();
		for (PlayerSessionData data : s.getParty().values()) {
			if (s.getHost() == data.getUniqueId()) continue;
			sorted.add(data);
		}
		Collections.sort(sorted);
		for (PlayerSessionData data : sorted) {
			playerLines.add(createBoardLine(data, false));
		}
	}

	private String createBoardLine(PlayerSessionData data, boolean isHost) {
		UUID uuid = data.getUniqueId();
		String line = rewards.get(uuid).isEmpty() ? "§a✓ §f" : "§c✗ §f";
		if (isHost) {
			line += "(Host) ";
		}
		line += data.getData().getDisplay();
		return line;
	}

	@Override
	public void cleanup() {
		super.cleanup();
		holo.remove();
	}

	@Override
	public void handleSpectatorInteractEvent(PlayerInteractEvent e) {
		e.setCancelled(true);
		if (e.getHand() != EquipmentSlot.HAND) return;
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.ENDER_CHEST) {
			new SpectateSelectInventory(s, e.getPlayer(), null, true);
		}
		else {
			super.handleSpectatorInteractEvent(e);
		}
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND) return;
		e.setCancelled(true);
		
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.ENDER_CHEST) {
			Player p = e.getPlayer();
			UUID uuid = p.getUniqueId();
			if (rewards.get(uuid).isEmpty()) {
				if (!onRewardClaim()) {
					new SpectateSelectInventory(s, e.getPlayer(), s.getParty().get(uuid), true);
				}
				return;
			}
			p.playSound(p, Sound.BLOCK_ENDER_CHEST_OPEN, 1F, 1F);
			new RewardInventory(s.getParty().get(uuid), rewards.get(uuid));
		}
		else {
			super.handleInteractEvent(e);
		}
	}
	
	public void spectateRewards(Player spectator, UUID viewed) {
		new RewardInventory(s.getParty().get(viewed), rewards.get(viewed), spectator);
	}
	
	public boolean onRewardClaim() {
		updateBoardLines();
		for (ArrayList<Reward> rewards : this.rewards.values()) {
			if (!rewards.isEmpty()) return false;
		}
		if (!s.isBusy()) {
			s.broadcast("Everyone's finished claiming rewards! Returning to node select...");
			s.setBusy(true);
			new BukkitRunnable() {
				public void run() {
					if (s.getNode().getRow() == 0) {
						// Should heal everyone upon new area
						s.getParty().values().forEach(data -> {
							data.healPercent(100);
						});
					}
					s.setInstance(new NodeSelectInstance(s));
					s.setBusy(false);
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

	@Override
	public void handlePlayerKickEvent(Player kicked) {
		rewards.remove(kicked.getUniqueId());
		onRewardClaim();
	}
}
