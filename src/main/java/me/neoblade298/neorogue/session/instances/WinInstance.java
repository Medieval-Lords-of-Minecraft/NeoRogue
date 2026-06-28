package me.neoblade298.neorogue.session.instances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class WinInstance extends EditInventoryInstance {
	private static final double SPAWN_X = Session.LOSE_X + 8.5, SPAWN_Z = Session.LOSE_Z + 7.5;
	private TextDisplay holo, leaveHolo;

	public WinInstance(Session s) {
		super(s, SPAWN_X, SPAWN_Z);
	}

	@Override
	public void setup() {
		for (PlayerSessionData data : s.getParty().values()) {
			data.getPlayer().teleport(spawn);
		}
		for (UUID uuid : s.getSpectators().keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			teleportRandomly(p);
		}
		super.setup();

		int sessionNotoriety = s.getNotoriety();
		for (PlayerSessionData psd : s.getParty().values()) {
			PlayerData pd = PlayerManager.getPlayerData(psd.getUniqueId());
			if (pd != null && sessionNotoriety >= pd.getMaxNotoriety(psd.getPlayerClass())) {
				pd.increaseNotorietyMax(psd.getPlayerClass());
			}
		}

		for (PlayerSessionData data : s.getParty().values()) {
			data.trigger(SessionTrigger.FINISH_RUN, true);
		}
		holo = NeoRogue.createHologram(spawn.clone().add(0, 2, 4),
				Component.text("Right click to view stats!", NamedTextColor.GOLD));
		leaveHolo = NeoRogue.createHologram(spawn.clone().add(0, 1, -4),
				Component.text("Click to leave", NamedTextColor.GOLD));
		s.broadcast(Component.text("Congratulations! You won!", NamedTextColor.GOLD));
		PlayerManager.getPlayerData(s.getHost()).removeSnapshot(s.getSaveSlot());
		s.deleteSave();
	}

	@Override
	public void updateBoardLines() {
		playerLines.clear();
		playerLines.add(createBoardLine(s.getParty().get(s.getHost()), true));

		ArrayList<PlayerSessionData> sorted = new ArrayList<PlayerSessionData>();
		for (PlayerSessionData data : s.getParty().values()) {
			if (s.getHost().equals(data.getUniqueId())) continue;
			sorted.add(data);
		}
		Collections.sort(sorted);
		for (PlayerSessionData data : sorted) {
			playerLines.add(createBoardLine(data, false));
		}
	}

	private String createBoardLine(PlayerSessionData data, boolean isHost) {
		String line = "";
		if (isHost) {
			line += "★ ";
		}
		line += data.getData().getDisplay();
		return line;
	}

	@Override
	public void cleanup(boolean pluginDisable) {
		super.cleanup(pluginDisable);
		holo.remove();
		leaveHolo.remove();
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		return null;
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		e.setCancelled(true);
		if (e.getHand() == EquipmentSlot.HAND && e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null) {
			Player p = e.getPlayer();
			if (e.getClickedBlock().getType() == Material.BEACON) {
				s.leavePlayer(p);
				return;
			}
			if (e.getClickedBlock().getType() == Material.LECTERN) {
				PlayerSessionData data = s.getData(p.getUniqueId());
				if (data != null) {
					data.getSessionStats().sendTo(p);
				}
				return;
			}
		}
		super.handleInteractEvent(e);
	}

	@Override
	public void handlePlayerLeaveParty(OfflinePlayer p) {

	}
}
