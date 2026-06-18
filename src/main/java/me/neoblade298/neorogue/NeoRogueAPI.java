package me.neoblade298.neorogue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.instances.LobbyInstance;

public class NeoRogueAPI {

	public static boolean isInSession(Player player) {
		Session s = SessionManager.getSession(player);
		return s != null;
	}

	public static Collection<Player> getSessionPlayers(Player player) {
		Session s = SessionManager.getSession(player);
		if (s == null) return Collections.emptyList();
		if (s.getInstance() instanceof LobbyInstance lobby) {
			ArrayList<Player> players = new ArrayList<>();
			for (UUID uuid : lobby.getInLobby()) {
				Player p = Bukkit.getPlayer(uuid);
				if (p != null) players.add(p);
			}
			return players;
		}
		return s.getOnlinePlayers();
	}

	public static String getSessionName(Player player) {
		Session s = SessionManager.getSession(player);
		if (s == null) return "";
		return s.getName();
	}
}
