package me.neoblade298.neorogue.session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.player.PlayerClass;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class LobbyInstance implements Instance {
	private static final int MAX_SIZE = 4;
	
	private String name;
	private HashSet<UUID> invited = new HashSet<UUID>();
	private HashMap<UUID, PlayerClass> players = new HashMap<UUID, PlayerClass>();
	private UUID host;
	private Session session;

	@Override
	public void start(Session s) {
		// TODO Auto-generated method stub
		
	}

	public LobbyInstance(String name, UUID host, Session session) {
		this.host = host;
		players.put(host, PlayerClass.SWORDSMAN);
		this.session = session;
	}

	public void invitePlayer(Player inviter, String username) {
		if (!inviter.getUniqueId().equals(host)) {
			Util.msgRaw(inviter, "&cOnly the host may invite other players!");
			return;
		}

		Player recipient = Bukkit.getPlayer(username);
		if (recipient == null) {
			Util.msgRaw(inviter, "&cThat player isn't online!");
			return;
		}

		invited.add(recipient.getUniqueId());
		broadcast("&e" + recipient.getName() + " &7was invited to the lobby!");

		Util.msg(recipient, "You've been invited to the &e" + name + " &7party!");
		
		recipient.spigot().sendMessage(
				SharedUtil.createText("&8[&aClick here to accept the invite!&8]", "Click to accept invite", "/nr join " + name).create());
	}

	public void addPlayer(Player p) {
		if (MAX_SIZE <= players.size()) {
			Util.msgRaw(p, "&cThis lobby is full as it has a max of &e" + MAX_SIZE + " &cplayers!");
		}

		invited.remove(p.getUniqueId());
		players.put(p.getUniqueId(), PlayerClass.SWORDSMAN);
		displayInfo(p);
		broadcast("&e" + p.getName() + " &7joined the lobby!");
	}

	public void kickPlayer(Player s, String name) {
		if (!s.getUniqueId().equals(host)) {
			Util.msgRaw(s, "&cOnly the host may kick other players!");
			return;
		}
		
		Player p = Bukkit.getPlayer(name);

		if (!players.containsKey(p.getUniqueId())) {
			Util.msgRaw(s, "&cThat player isn't in your lobby!");
			return;
		}

		players.remove(p.getUniqueId());
		broadcast("&e" + p.getName() + " &7was kicked from the lobby!");
	}

	public void leavePlayer(Player s) {
		if (s.getUniqueId().equals(host)) {
			SessionManager.removeSession(session);
			broadcast("&e" + s.getName() + " &7disbanded the lobby!");
		}
		else {
			players.remove(s.getUniqueId());
			broadcast("&e" + s.getName() + " &7left the lobby!");
		}
	}

	public void broadcast(String msg) {
		for (UUID uuid : players.keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			Util.msgRaw(p, msg);
		}
	}

	public void displayInfo(Player viewer) {
		Util.msgRaw(viewer, "&7<< &c" + name + " &7) >>");
		Player h = Bukkit.getPlayer(host);
		boolean isHost = viewer.getUniqueId().equals(host);
		
		// Player list
		boolean first = true;
		Util.msgRaw(viewer, "&7Players:");
		Util.msgRaw(viewer, "&7- &c" + h.getName() + " (&e" + players.get(host).getDisplay() + "&7) (&eHost&7)");
		ComponentBuilder b;
		if (players.size() > 1) {
			b = new ComponentBuilder();
			first = true;
			for (UUID uuid : players.keySet()) {
				if (uuid.equals(host)) continue;
				if (!first) {
					SharedUtil.appendText(b, "\n");
				}
				first = false;
				Player p = Bukkit.getPlayer(uuid);
				SharedUtil.appendText(b, "&7- &c" + p.getName() + " &7(&e" + players.get(uuid).getDisplay() + "&7)");
				if (isHost) {
					SharedUtil.appendText(b, " &8[&cClick to kick&8]", "Click to kick " + p.getName(), "/nr kick " + p.getName());
					SharedUtil.appendText(b, " &8[&cClick to give host&8]", "Click to give host to " + p.getName(), "/nr sethost " + p.getName());
				}
			}
			viewer.spigot().sendMessage(b.create());
		}

		b = new ComponentBuilder();
		if (viewer.getUniqueId().equals(host)) {
			SharedUtil.appendText(b, "&8[&aClick here to start!&8] ", "Click me to start!", "/nr start");
		}
		viewer.spigot().sendMessage(b.create());
	}

	public HashMap<UUID, PlayerClass> getPlayers() {
		return players;
	}

	public HashSet<UUID> getInvited() {
		return invited;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}
}
