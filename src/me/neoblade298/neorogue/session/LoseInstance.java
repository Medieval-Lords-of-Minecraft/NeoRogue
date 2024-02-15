package me.neoblade298.neorogue.session;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.PlayerSessionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class LoseInstance extends EditInventoryInstance {
	private static final double SPAWN_X = Session.LOSE_X + 2.5, SPAWN_Z = Session.LOSE_Z + 2.5;
	
	public LoseInstance(Session s) {
		super(s, SPAWN_X, SPAWN_Z);
	}

	@Override
	public void start() {
		for (PlayerSessionData data : s.getParty().values()) {
			data.getPlayer().teleport(spawn);
		}
		for (UUID uuid : s.getSpectators()) {
			Player p = Bukkit.getPlayer(uuid);
			p.teleport(spawn);
		}

		s.broadcast(Component.text("You lost!", NamedTextColor.RED));
		PlayerManager.getPlayerData(s.getHost()).removeSnapshot(s.getSaveSlot());
		s.deleteSave();
	}

	@Override
	public void cleanup() {
		
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		return null;
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		e.setCancelled(true);
	}
}
