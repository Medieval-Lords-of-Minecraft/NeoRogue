package me.neoblade298.neorogue.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
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
import me.neoblade298.neorogue.player.PlayerSessionData;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class LobbyInstance extends Instance {
	private static final int MAX_SIZE = 4;
	
	private String name;
	private HashSet<UUID> invited = new HashSet<UUID>();
	private HashMap<UUID, PlayerClass> players = new HashMap<UUID, PlayerClass>();
	private UUID host;
	private static Clipboard classSelect, rewardsRoom, campfire, shop, chance;
	private boolean busy = false;
	
	private static final int LOBBY_X = -7, LOBBY_Z = 3;
	private static final int REWARDS_Z = 76, CAMPFIRE_Z = 82, SHOP_Z = 90, CHANCE_Z = 98;
	
	// schematics
	private static String CLASS_SELECT = "classselect.schem",
			REWARDS_ROOM = "rewardsroom.schem",
			CAMPFIRE = "campfire.schem",
			SHOP = "shop.schem",
			CHANCE = "chance.schem";
	
	static {
		classSelect = loadClipboard(CLASS_SELECT);
		rewardsRoom = loadClipboard(REWARDS_ROOM);
		campfire = loadClipboard(CAMPFIRE);
		shop = loadClipboard(SHOP);
		chance = loadClipboard(CHANCE);
	}
	
	private static Clipboard loadClipboard(String schematic) {
		File file = new File(NeoRogue.SCHEMATIC_FOLDER, schematic);
		ClipboardFormat format = ClipboardFormats.findByFile(file);
		try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
			return reader.read();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static void pasteSchematic(Clipboard clipboard, EditSession editSession, Session session, int xOff, int zOff) {
	    Operation operation = new ClipboardHolder(clipboard)
	            .createPaste(editSession)
	            .to(BlockVector3.at(-(session.getXOff() + xOff + 1), 64, session.getZOff() + zOff))
	            .ignoreAirBlocks(true)
	            .build();
	    try {
			Operations.complete(operation);
		} catch (WorldEditException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(Session s) {
		
	}

	public LobbyInstance(String name, Player host, Session session) {
		this.name = name;
		this.host = host.getUniqueId();
		players.put(host.getUniqueId(), PlayerClass.SWORDSMAN);
		this.s = session;
		
		// Generate the lobby and add the host there
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(Area.world)) {
			pasteSchematic(classSelect, editSession, session, 0, 0);
			pasteSchematic(rewardsRoom, editSession, session, 0, REWARDS_Z);
			pasteSchematic(campfire, editSession, session, 0, CAMPFIRE_Z);
			pasteSchematic(shop, editSession, session, 0, SHOP_Z);
			pasteSchematic(chance, editSession, session, 0, CHANCE_Z);
		}
		spawn = new Location(Bukkit.getWorld(Area.WORLD_NAME), session.getXOff() + LOBBY_X, 64, session.getZOff() + LOBBY_Z);
		host.teleport(spawn);
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
		SessionManager.addToSession(p.getUniqueId(), this.s);
		p.teleport(spawn);
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

	public void leavePlayer(Player p) {
		if (busy) {
			Util.msgRaw(p, "&cYour game is generating! You can't do this right now!");
			return;
		}
		
		if (p.getUniqueId().equals(host)) {
			SessionManager.removeSession(this.s);
			broadcast("&e" + p.getName() + " &7disbanded the lobby!");
		}
		else {
			players.remove(p.getUniqueId());
			SessionManager.removeFromSession(p.getUniqueId());
			broadcast("&e" + p.getName() + " &7left the lobby!");
		}
		p.teleport(NeoRogue.spawn);
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
	
	public void startGame(Player p) {
		if (!p.getUniqueId().equals(host)) {
			Util.msgRaw(p, "&cOnly the host may start the game!");
			return;
		}
		
		if (busy) {
			Util.msgRaw(p, "&cYour game is generating! You can't do this right now!");
			return;
		}

		s.addPlayers(players);
		s.broadcast("&7Generating your game...");
		s.generateArea(AreaType.LOW_DISTRICT);
		s.setNode(s.getArea().getNodes()[0][2]);
		
		new BukkitRunnable() {
			public void run() {
				for (UUID uuid : players.keySet()) {
					Player p = Bukkit.getPlayer(uuid);
					p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
					s.setInstance(new NodeSelectInstance());
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
	
	public String getName() {
		return name;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK || !Tag.SIGNS.isTagged(e.getClickedBlock().getType())) return;
		
		UUID uuid = e.getPlayer().getUniqueId();
		Sign sign = (Sign) e.getClickedBlock().getState();
		char c = ChatColor.stripColor(sign.getLine(2)).charAt(0);
		
		switch (c) {
		case 'S': switchClass(uuid, PlayerClass.SWORDSMAN);
		break;
		case 'T': switchClass(uuid, PlayerClass.THIEF);
		break;
		case 'A': switchClass(uuid, PlayerClass.ARCHER);
		break;
		case 'M': switchClass(uuid, PlayerClass.MAGE);
		break;
		}
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		Bukkit.getLogger().warning("[NeoRogue] LobbyInstance attempted to save, this should never happen");
		return null;
	}

	@Override
	public void teleportPlayer(Player p) {
		p.teleport(spawn);
	}
}
