package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.SessionSettingsInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class LobbyInstance extends Instance {
	private static final int MAX_SIZE = 4;
	private static final double SPAWN_X = Session.LOBBY_X + 6.5, SPAWN_Z = Session.LOBBY_Z + 3.5,
			HOLO_X = 0, HOLO_Y = 3, HOLO_Z = 10;

	private String name;
	private static String invPrefix = "<dark_gray>[<green><click:run_command:'/nr join ",
			invSuffix = "'><hover:show_text:'Click to accept invite'>Click here to accept the invite!</hover></click></green>]";
	private HashSet<UUID> invited = new HashSet<UUID>();
	private HashMap<UUID, EquipmentClass> players = new HashMap<UUID, EquipmentClass>();
	private UUID host;
	private Component partyInfoHeader;
	private HashSet<UUID> ready = new HashSet<UUID>();
	private TextDisplay holo;

	private static final Comparator<Player> comp = new Comparator<Player>() {
		@Override
		public int compare(Player p1, Player p2) {
			return p1.getName().compareToIgnoreCase(p2.getName());
		}
	};

	// Static error messages
	private static final TextComponent hostOnlyInvite = Component.text("Only the host may invite other players!",
			NamedTextColor.RED),
			hostOnlyKick = Component.text("Only the host may kick other players!", NamedTextColor.RED),
			gameGenerating = Component.text("Your game is generating! You can't do this right now!",
					NamedTextColor.RED),
			playerNotOnline = Component.text("That player isn't online!", NamedTextColor.RED),
			playerNotInLobby = Component.text("That player isn't in the lobby!", NamedTextColor.RED),
			maxSizeError = Component.text("This lobby is full as it has a max of " + MAX_SIZE + " players!",
					NamedTextColor.RED);


	@Override
	public void setup() {

	}

	public LobbyInstance(String name, Player host, Session session) {
		super(session, SPAWN_X, SPAWN_Z, new PlayerFlags(PlayerFlag.CAN_FLY));
		this.name = name;
		this.host = host.getUniqueId();
		host.setGameMode(GameMode.ADVENTURE);
		players.put(host.getUniqueId(), EquipmentClass.WARRIOR);
		host.teleport(spawn);
		spectatorLines = playerLines;
		updateBoardLines();
		partyInfoHeader = Component.text().content("<< ( ").color(NamedTextColor.GRAY)
				.append(Component.text(name, NamedTextColor.RED)).append(Component.text(" ) >>"))
				.append(Component.text("\nPlayers:")).build();

		

		// Setup hologram
		Component text = Component.text("Invite players with /nr invite {name/all}").appendNewline()
			.append(Component.text("Choose a class then hit the button")).appendNewline()
			.append(Component.text("when you're ready!"));
		holo = NeoRogue.createHologram(spawn.clone().add(HOLO_X, HOLO_Y, HOLO_Z), text);
	}

	public void invitePlayer(Player inviter, String username) {
		if (!inviter.getUniqueId().equals(host)) {
			Util.msgRaw(inviter, hostOnlyInvite);
			return;
		}

		if (s.isBusy()) {
			Util.msgRaw(inviter, gameGenerating);
			return;
		}

		Player recipient = Bukkit.getPlayer(username);
		if (recipient == null) {
			Util.msgRaw(inviter, playerNotOnline);
			return;
		}

		
		if (SessionManager.getSession(recipient) != null) {
			Util.msg(inviter, "<red>That player is already in a session!");
			return;
		}
		
		if (players.containsKey(recipient.getUniqueId())) return;

		invited.add(recipient.getUniqueId());
		TextComponent tc = Component.text().content(recipient.getName()).color(NamedTextColor.YELLOW)
				.append(Component.text(" was invited to the lobby!", NamedTextColor.GRAY)).build();
		broadcast(tc);
		Util.msg(recipient, Component.text("You've been invited to the ")
				.append(Component.text(name, NamedTextColor.YELLOW)).append(Component.text(" party by "))
				.append(Component.text(inviter.getName(), NamedTextColor.YELLOW)).append(Component.text("!")));

		Util.msg(recipient, NeoCore.miniMessage().deserialize(invPrefix + name + invSuffix));
	}

	public void addPlayer(Player p) {
		if (MAX_SIZE <= players.size()) {
			Util.msgRaw(p, maxSizeError);
		}

		if (s.isBusy()) {
			Util.msgRaw(p, gameGenerating);
			return;
		}

		invited.remove(p.getUniqueId());
		p.setGameMode(GameMode.ADVENTURE);
		players.put(p.getUniqueId(), EquipmentClass.WARRIOR);
		SessionManager.addToSession(p.getUniqueId(), this.s);
		p.teleport(spawn);
		displayInfo(p);
		TextComponent tc = Component.text().content(p.getName()).color(NamedTextColor.YELLOW)
				.append(Component.text(" joined the lobby!", NamedTextColor.GRAY)).build();
		broadcast(tc);
		updateBoardLines();
	}

	@Override
	public void updateBoardLines() {
		playerLines.clear();
		Player hostp = Bukkit.getPlayer(host);
		playerLines.add(createBoardLine(hostp, true));

		ArrayList<Player> sorted = new ArrayList<Player>();
		for (UUID uuid : players.keySet()) {
			if (host == uuid) continue;
			sorted.add(Bukkit.getPlayer(uuid));
		}
		sorted.sort(comp);
		for (Player p : sorted) {
			playerLines.add(createBoardLine(p, false));
		}
	}

	private String createBoardLine(Player p, boolean isHost) {
		UUID uuid = p.getUniqueId();
		String line = ready.contains(uuid) ? "§a✓ §f" : "§c✗ §f";
		if (isHost) {
			line += "★ ";
		}
		line += p.getName() + "§7 - §e" + players.get(p.getUniqueId()).getDisplay();
		return line;
	}

	public void kickPlayer(Player s, String name) {
		if (!s.getUniqueId().equals(host)) {
			Util.msgRaw(s, hostOnlyKick);
			return;
		}

		if (this.s.isBusy()) {
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
		updateBoardLines();
	}

	public void leavePlayer(Player p) {
		if (s.isBusy()) {
			Util.msgRaw(p, gameGenerating);
			return;
		}

		if (p.getUniqueId().equals(host)) {
			SessionManager.endSession(s);
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
		updateBoardLines();
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
				.append(Component.text(") (")).append(Component.text("Host", NamedTextColor.YELLOW))
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
				str += "<gray>- <red>" + p.getName() + "</red> (<yellow>" + players.get(uuid).getDisplay()
						+ "</yellow>)</gray>";
				if (isHost) {
					str += "<dark_gray>[<red><click:run_command:'/nr kick " + p.getName()
							+ "'><hover:show_text:'Click to kick " + p.getName()
							+ "'>Click to kick</hover></click></red>]";
				}
			}
			Util.msgRaw(viewer, NeoCore.miniMessage().deserialize(str));
		}

		if (viewer.getUniqueId().equals(host)) {
			str = "<dark_gray>[<red><click:run_command:'/nr start'><hover:show_text:'Click me to start!'>Click here to start!</hover></click></red>]";
			Util.msg(viewer, NeoCore.miniMessage().deserialize(str));
		}
	}

	public HashMap<UUID, EquipmentClass> getPlayers() {
		return players;
	}

	public HashSet<UUID> getInvited() {
		return invited;
	}

	public void broadcast(String msg) {
		for (UUID uuid : players.keySet()) {
			Util.msgRaw(Bukkit.getPlayer(uuid), NeoCore.miniMessage().deserialize(msg).colorIfAbsent(NamedTextColor.GRAY));
		}
	}

	public void startGame() {
		s.setBusy(true);
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
				s.setInstance(new NodeSelectInstance(s));
				s.setBusy(false);
			}
		}.runTaskLater(NeoRogue.inst(), 20L);
	}

	public void switchClass(UUID uuid, EquipmentClass pc) {
		if (!players.containsKey(uuid)) {
			Bukkit.getLogger().warning("[NeoRogue] Player tried to switch class when not belonging to that session");
			return;
		}

		if (s.isBusy()) {
			Util.msgRaw(Bukkit.getPlayer(uuid), gameGenerating);
			return;
		}

		TextComponent tc = Component.text().content(Bukkit.getPlayer(uuid).getName()).color(NamedTextColor.YELLOW)
				.append(Component.text(" switched to class ", NamedTextColor.GRAY))
				.append(Component.text(pc.getDisplay(), NamedTextColor.RED)).build();
		broadcast(tc);
		players.put(uuid, pc);
		updateBoardLines();
	}

	public String getName() {
		return name;
	}

	@Override
	public void cleanup() {
		holo.remove();
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (e.getHand() != EquipmentSlot.HAND) return;
		
		if (e.getClickedBlock().getType() == Material.STONE_BUTTON) {
			readyPlayer(e.getPlayer());
			return;
		}

		if (!Tag.SIGNS.isTagged(e.getClickedBlock().getType())) return;
		UUID uuid = e.getPlayer().getUniqueId();
		Sign sign = (Sign) e.getClickedBlock().getState();

		char c = ((TextComponent) sign.getSide(Side.FRONT).line(1)).content().charAt(0);

		switch (c) {
		case 'W':
			switchClass(uuid, EquipmentClass.WARRIOR);
			break;
		case 'T':
			switchClass(uuid, EquipmentClass.THIEF);
			break;
		case 'A':
			switchClass(uuid, EquipmentClass.ARCHER);
			break;
		case 'M':
			switchClass(uuid, EquipmentClass.MAGE);
			break;
		case 'R':
			new SessionSettingsInventory(e.getPlayer(), s, this);
			break;
		}
	}

	private void readyPlayer(Player p) {
		UUID uuid = p.getUniqueId();
		if (s.isBusy()) return;
		if (!ready.contains(uuid)) {
			ready.add(uuid);
			broadcast("<yellow>" + p.getName() + "</yellow> is ready!");
			if (ready.size() == players.size()) {
				startGame();
			}
		}
		else {
			ready.remove(uuid);
			broadcast("<yellow>" + p.getName() + "</yellow> is no longer ready!");
		}
		updateBoardLines();
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		Bukkit.getLogger().warning("[NeoRogue] LobbyInstance attempted to save, this should never happen");
		return null;
	}

	@Override
	public void handlePlayerLeaveParty(Player p) {
		
	}
}
