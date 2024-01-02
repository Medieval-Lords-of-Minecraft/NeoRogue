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
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
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
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.chance.ChanceInstance;
import me.neoblade298.neorogue.session.reward.RewardInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class LobbyInstance extends Instance {
	private static final int MAX_SIZE = 4;
	
	private String name;
	private static String invPrefix = "<dark_gray>[<green><click:run_command:'/nr join ",
			invSuffix = "'><hover:show_text:'Click to accept invite'>Click here to accept the invite!</hover></click></green>]";
	private HashSet<UUID> invited = new HashSet<UUID>();
	private HashMap<UUID, EquipmentClass> players = new HashMap<UUID, EquipmentClass>();
	private UUID host;
	private static Clipboard classSelect, rewardsRoom, campfire, shop, chance;
	private boolean busy = false;
	private Component partyInfoHeader;
	
	private static final int LOBBY_X = -7, LOBBY_Z = 3;
	
	// Static error messages
	private static final TextComponent hostOnlyInvite = Component.text("Only the host may invite other players!", NamedTextColor.RED),
			hostOnlyKick = Component.text("Only the host may kick other players!", NamedTextColor.RED),
			hostOnlyStart = Component.text("Only the host may start the game!", NamedTextColor.RED),
			gameGenerating = Component.text("Your game is generating! You can't do this right now!", NamedTextColor.RED),
			playerNotOnline = Component.text("That player isn't online!", NamedTextColor.RED),
			playerNotInLobby = Component.text("That player isn't in the lobby!", NamedTextColor.RED),
			maxSizeError = Component.text("This lobby is full as it has a max of " + MAX_SIZE + " players!", NamedTextColor.RED);
			
	
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
		players.put(host.getUniqueId(), EquipmentClass.WARRIOR);
		this.s = session;
		
		// Generate the lobby and add the host there
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(Area.world)) {
			pasteSchematic(classSelect, editSession, session, 0, 0);
			pasteSchematic(rewardsRoom, editSession, session, 0, RewardInstance.REWARDS_Z - 2);
			pasteSchematic(campfire, editSession, session, 0, CampfireInstance.REST_Z - 2);
			pasteSchematic(shop, editSession, session, 0, ShopInstance.SHOP_Z - 4);
			pasteSchematic(chance, editSession, session, 0, ChanceInstance.CHANCE_Z - 3);
		}
		spawn = new Location(Bukkit.getWorld(Area.WORLD_NAME), session.getXOff() + LOBBY_X, 64, session.getZOff() + LOBBY_Z);
		host.teleport(spawn);
		partyInfoHeader = Component.text().content("<< ( ").color(NamedTextColor.GRAY)
				.append(Component.text(name, NamedTextColor.RED))
				.append(Component.text(" ) >>"))
				.append(Component.text("\nPlayers:")).build();
	}

	public void invitePlayer(Player inviter, String username) {
		if (!inviter.getUniqueId().equals(host)) {
			Util.msgRaw(inviter, hostOnlyInvite);
			return;
		}
		
		if (busy) {
			Util.msgRaw(inviter, gameGenerating);
			return;
		}

		Player recipient = Bukkit.getPlayer(username);
		if (recipient == null) {
			Util.msgRaw(inviter, playerNotOnline);
			return;
		}

		invited.add(recipient.getUniqueId());
		TextComponent tc = Component.text().content(recipient.getName()).color(NamedTextColor.YELLOW)
				.append(Component.text(" was invited to the lobby!", NamedTextColor.GRAY)).build();
		broadcast(tc);
		Util.msg(recipient, Component.text("You've been invited to the ")
				.append(Component.text(name, NamedTextColor.YELLOW))
				.append(Component.text(" party!")));

		recipient.sendMessage(NeoCore.miniMessage().deserialize(invPrefix + name + invSuffix));
	}

	public void addPlayer(Player p) {
		if (MAX_SIZE <= players.size()) {
			Util.msgRaw(p, maxSizeError);
		}
		
		if (busy) {
			Util.msgRaw(p, gameGenerating);
			return;
		}

		invited.remove(p.getUniqueId());
		players.put(p.getUniqueId(), EquipmentClass.WARRIOR);
		SessionManager.addToSession(p.getUniqueId(), this.s);
		p.teleport(spawn);
		displayInfo(p);
		TextComponent tc = Component.text().content(p.getName()).color(NamedTextColor.YELLOW)
				.append(Component.text(" joined the lobby!", NamedTextColor.GRAY)).build();
		broadcast(tc);
	}

	public void kickPlayer(Player s, String name) {
		if (!s.getUniqueId().equals(host)) {
			Util.msgRaw(s, hostOnlyKick);
			return;
		}
		
		if (busy) {
			Util.msgRaw(s, gameGenerating);
			return;
		}
		
		Player p = Bukkit.getPlayer(name);

		if (!players.containsKey(p.getUniqueId())) {
			Util.msgRaw(s, playerNotInLobby);
			return;
		}

		SessionManager.removeFromSession(p.getUniqueId());
		players.remove(p.getUniqueId());
		p.teleport(NeoRogue.spawn);
		TextComponent tc = Component.text().content(p.getName()).color(NamedTextColor.YELLOW)
				.append(Component.text(" was kicked from the lobby!", NamedTextColor.GRAY)).build();
		broadcast(tc);
	}

	public void leavePlayer(Player p) {
		if (busy) {
			Util.msgRaw(p, gameGenerating);
			return;
		}
		
		if (p.getUniqueId().equals(host)) {
			SessionManager.removeSession(this.s);
			TextComponent tc = Component.text().content(p.getName()).color(NamedTextColor.YELLOW)
					.append(Component.text(" disbanded the lobby!", NamedTextColor.GRAY)).build();
			broadcast(tc);
		}
		else {
			players.remove(p.getUniqueId());
			SessionManager.removeFromSession(p.getUniqueId());
			TextComponent tc = Component.text().content(p.getName()).color(NamedTextColor.YELLOW)
					.append(Component.text(" left the lobby!", NamedTextColor.GRAY)).build();
			broadcast(tc);
		}
		p.teleport(NeoRogue.spawn);
	}

	public void broadcast(TextComponent msg) {
		for (UUID uuid : players.keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			Util.msgRaw(p, msg);
		}
	}

	public void displayInfo(Player viewer) {
		Player h = Bukkit.getPlayer(host);
		boolean isHost = viewer.getUniqueId().equals(host);
		
		// Player list
		boolean first = true;
		Util.msgRaw(viewer, partyInfoHeader.replaceText(config -> {
			config.match("%");
			config.replacement(Component.text(name, NamedTextColor.RED));
		}));
		
		TextComponent hostText = Component.text().content("- ").color(NamedTextColor.GRAY)
				.append(Component.text(h.getName(), NamedTextColor.RED))
				.append(Component.text(" (")
						.append(Component.text(players.get(host).getDisplay(), NamedTextColor.YELLOW)))
				.append(Component.text(") ("))
				.append(Component.text("Host", NamedTextColor.YELLOW))
				.append(Component.text(")")).build();
		Util.msgRaw(viewer, hostText);
		String str = "";
		if (players.size() > 1) {
			first = true;
			for (UUID uuid : players.keySet()) {
				if (uuid.equals(host)) continue;
				if (!first) {
					str += "\n";
				}
				first = false;
				Player p = Bukkit.getPlayer(uuid);
				str += "<gray>- <red>" + p.getName() + "</red> (<yellow>" + players.get(uuid).getDisplay() + "</yellow>)</gray>";
				if (isHost) {
					str += "<dark_gray>[<red><click:run_command:'/nr kick " + p.getName() + "'><hover:show_text:'Click to kick " +
							p.getName() + "'>Click to kick</hover></click></red>]";
				}
			}
			viewer.sendMessage(NeoCore.miniMessage().deserialize(str));
		}

		if (viewer.getUniqueId().equals(host)) {
			str = "<dark_gray>[<red><click:run_command:'/nr start'><hover:show_text:'Click me to start!'>Click here to start!</hover></click></red>]";
			viewer.sendMessage(NeoCore.miniMessage().deserialize(str));
		}
	}

	public HashMap<UUID, EquipmentClass> getPlayers() {
		return players;
	}

	public HashSet<UUID> getInvited() {
		return invited;
	}
	
	public void startGame(Player p) {
		if (!p.getUniqueId().equals(host)) {
			Util.msgRaw(p, hostOnlyStart);
			return;
		}
		
		if (busy) {
			Util.msgRaw(p, gameGenerating);
			return;
		}

		s.addPlayers(players);
		s.broadcast("Generating your game...");
		s.generateArea(AreaType.LOW_DISTRICT);
		s.setNode(s.getArea().getNodes()[0][2]);
		
		new BukkitRunnable() {
			public void run() {
				for (UUID uuid : players.keySet()) {
					Player p = Bukkit.getPlayer(uuid);
					p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
				}
				s.setInstance(new NodeSelectInstance());
			}
		}.runTaskLater(NeoRogue.inst(), 20L);
	}
	
	public void switchClass(UUID uuid, EquipmentClass pc) {
		if (!players.containsKey(uuid)) {
			Bukkit.getLogger().warning("[NeoRogue] Player tried to switch class when not belonging to that session");
			return;
		}
		
		if (busy) {
			Util.msgRaw(Bukkit.getPlayer(uuid), gameGenerating);
			return;
		}

		TextComponent tc = Component.text().content(Bukkit.getPlayer(uuid).getName()).color(NamedTextColor.YELLOW)
				.append(Component.text(" switched to class ", NamedTextColor.GRAY))
				.append(Component.text(pc.getDisplay(), NamedTextColor.RED)).build();
		broadcast(tc);
		players.put(uuid, pc);
	}
	
	public String getName() {
		return name;
	}

	@Override
	public void cleanup() {
		
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK || !Tag.SIGNS.isTagged(e.getClickedBlock().getType())) return;
		if (e.getHand() != EquipmentSlot.HAND) return;
		
		UUID uuid = e.getPlayer().getUniqueId();
		Sign sign = (Sign) e.getClickedBlock().getState();
		char c = ((TextComponent)sign.getSide(Side.FRONT).line(2)).content().charAt(0);
		
		switch (c) {
		case 'W': switchClass(uuid, EquipmentClass.WARRIOR);
		break;
		case 'T': switchClass(uuid, EquipmentClass.THIEF);
		break;
		case 'A': switchClass(uuid, EquipmentClass.ARCHER);
		break;
		case 'M': switchClass(uuid, EquipmentClass.MAGE);
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
