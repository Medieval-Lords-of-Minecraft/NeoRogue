package me.neoblade298.neorogue.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.player.PlayerClass;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class LobbyInstance implements Instance {
	private static final int MAX_SIZE = 4;
	
	private String name;
	private HashSet<UUID> invited = new HashSet<UUID>();
	private HashMap<UUID, PlayerClass> players = new HashMap<UUID, PlayerClass>();
	private UUID host;
	private Session session;
	private static Clipboard clipboard;
	private Location loc;
	private boolean busy = false;
	
	private static final int X_OFFSET = -7, Z_OFFSET = 3;
	
	// schematics
	private static String NODE_SELECT = "classselect.schem";
	
	static {
		// Load the node select schematic
		File file = new File(NeoRogue.SCHEMATIC_FOLDER, NODE_SELECT);
		ClipboardFormat format = ClipboardFormats.findByFile(file);
		try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
			clipboard = reader.read();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(Session s) {
		
	}

	public LobbyInstance(String name, Player host, Session session) {
		this.host = host.getUniqueId();
		players.put(host.getUniqueId(), PlayerClass.SWORDSMAN);
		this.session = session;
		
		// Generate the starting area and add the host there
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(Area.world)) {
		    Operation operation = new ClipboardHolder(clipboard)
		            .createPaste(editSession)
		            .to(BlockVector3.at(session.getXOff(), 64, session.getZOff()))
		            .ignoreAirBlocks(true)
		            .build();
		    try {
				Operations.complete(operation);
			} catch (WorldEditException e) {
				e.printStackTrace();
			}
		}
		
		loc = new Location(Bukkit.getWorld(Area.WORLD_NAME), session.getXOff() + X_OFFSET, 64, session.getZOff() + Z_OFFSET);
		host.teleport(loc);
	}

	public void invitePlayer(Player inviter, String username) {
		if (!inviter.getUniqueId().equals(host)) {
			Util.msgRaw(inviter, "&cOnly the host may invite other players!");
			return;
		}
		
		if (busy) {
			Util.msgRaw(inviter, "&cYour game is generating! You can't do this right now!");
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
		
		if (busy) {
			Util.msgRaw(p, "&cYour game is generating! You can't do this right now!");
			return;
		}

		invited.remove(p.getUniqueId());
		players.put(p.getUniqueId(), PlayerClass.SWORDSMAN);
		p.teleport(loc);
		displayInfo(p);
		broadcast("&e" + p.getName() + " &7joined the lobby!");
	}

	public void kickPlayer(Player s, String name) {
		if (!s.getUniqueId().equals(host)) {
			Util.msgRaw(s, "&cOnly the host may kick other players!");
			return;
		}
		
		if (busy) {
			Util.msgRaw(s, "&cYour game is generating! You can't do this right now!");
			return;
		}
		
		Player p = Bukkit.getPlayer(name);

		if (!players.containsKey(p.getUniqueId())) {
			Util.msgRaw(s, "&cThat player isn't in your lobby!");
			return;
		}

		SessionManager.removeFromSession(p.getUniqueId());
		players.remove(p.getUniqueId());
		p.teleport(NeoRogue.spawn);
		broadcast("&e" + p.getName() + " &7was kicked from the lobby!");
	}

	public void leavePlayer(Player s) {
		if (busy) {
			Util.msgRaw(s, "&cYour game is generating! You can't do this right now!");
			return;
		}
		
		if (s.getUniqueId().equals(host)) {
			SessionManager.removeSession(session);
			broadcast("&e" + s.getName() + " &7disbanded the lobby!");
		}
		else {
			players.remove(s.getUniqueId());
			SessionManager.removeFromSession(s.getUniqueId());
			broadcast("&e" + s.getName() + " &7left the lobby!");
		}
		s.teleport(NeoRogue.spawn);
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
	
	public void startGame(Player s) {
		if (!s.getUniqueId().equals(host)) {
			Util.msgRaw(s, "&cOnly the host may start the game!");
			return;
		}
		
		if (busy) {
			Util.msgRaw(s, "&cYour game is generating! You can't do this right now!");
			return;
		}

		session.addPlayers(players);
		session.broadcast("&7Generating your game...");
		session.generateArea(AreaType.LOW_DISTRICT);
		session.setNode(session.getArea().getNodes()[0][2]);
		session.setInstance(new NodeSelectInstance(session));
		
		new BukkitRunnable() {
			public void run() {
				for (UUID uuid : players.keySet()) {
					Bukkit.getPlayer(uuid).teleport(session.getArea().getTeleport());
				}
			}
		}.runTaskLater(NeoRogue.inst(), 20L);
	}
	
	public void switchClass(UUID uuid, PlayerClass pc) {
		if (!players.containsKey(uuid)) {
			Bukkit.getLogger().warning("[NeoRogue] Player tried to switch class when not belonging to that session");
			return;
		}
		
		if (busy) {
			Util.msgRaw(Bukkit.getPlayer(uuid), "&cYour game is generating! You can't do this right now!");
			return;
		}
		
		broadcast("&e" + Bukkit.getPlayer(uuid).getName() + " &7switched to class &c" + pc.getDisplay());
		players.put(uuid, pc);
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}
}
