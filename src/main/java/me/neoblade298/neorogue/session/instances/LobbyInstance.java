package me.neoblade298.neorogue.session.instances;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public abstract class LobbyInstance extends Instance {
	public static final int MAX_SIZE = 4;

	protected Session session;
	protected HashSet<UUID> joinRequests = new HashSet<UUID>(), inLobby = new HashSet<UUID>();
	protected HashSet<UUID> pendingRemovals = new HashSet<UUID>();
	protected boolean hostLeft = false;
	protected UUID host;
	protected Component partyInfoHeader;
	protected TextDisplay holo;

	// Clickable prompt sent to players so they can request to join a lobby
	protected static String joinPrefix = "<dark_gray>[<green><click:run_command:'/nr join ",
			joinSuffix = "'><hover:show_text:'Click to request to join'>Click here to join!</hover></click></green>]";
	// Static error messages
	protected static final TextComponent gameGenerating = Component.text("Your game is generating! You can't do this right now!",
					NamedTextColor.RED),
			hostOnlyKick = Component.text("Only the host may kick other players!", NamedTextColor.RED),
			hostOnlyRespond = Component.text("Only the host may respond to join requests!", NamedTextColor.RED),
			playerNotInLobby = Component.text("That player isn't in the lobby!", NamedTextColor.RED);
	protected String name;

	@Override
	public void setup() {

	}

	public LobbyInstance(Player host, Session session, double spawnX, double spawnZ) {
		super(session, spawnX, spawnZ, new PlayerFlags(PlayerFlag.CAN_FLY));
		this.session = session;
		this.host = host.getUniqueId();
		host.setGameMode(GameMode.SURVIVAL);
		host.teleport(spawn);
		spectatorLines = playerLines;
		inLobby.add(host.getUniqueId());
		name = host.getName() + "'s Lobby";

		partyInfoHeader = Component.text().content("<< ( ").color(NamedTextColor.GRAY)
				.append(Component.text(name, NamedTextColor.RED)).append(Component.text(" ) >>"))
				.append(Component.text("\nPlayers:")).build();
	}

	public abstract void addPlayer(Player p);

	public abstract void kickPlayer(Player s, String name);

	public abstract void startGame();

	public abstract void displayInfo(Player viewer);

	public EquipmentClass getHostClass() {
		return null;
	}

	public void leavePlayer(Player p) {
		if (s.isBusy()) {
			// Session is loading — mark for deferred cleanup
			UUID uuid = p.getUniqueId();
			pendingRemovals.add(uuid);
			inLobby.remove(uuid);
			SessionManager.removeFromSession(uuid);
			if (uuid.equals(host)) {
				hostLeft = true;
			}
			return;
		}

		if (p.getUniqueId().equals(host)) {
			TextComponent tc = Component.text().content(p.getName()).color(NamedTextColor.YELLOW)
					.append(Component.text(" disbanded the lobby!", NamedTextColor.GRAY)).build();
			broadcast(tc);
			// Lobby joiners aren't in the session party yet (that only happens once the game
			// starts), so Session.cleanup won't reset them. Reset each of them here so they get
			// their menu compass back and are sent to spawn before the session ends.
			for (UUID uuid : inLobby) {
				if (uuid.equals(host)) continue;
				Player other = Bukkit.getPlayer(uuid);
				if (other == null) continue;
				SessionManager.removeFromSession(uuid);
				SessionManager.resetPlayer(other);
				other.teleport(NeoRogue.spawn);
			}
			SessionManager.endSession(s);
			SessionManager.resetPlayer(p);
			p.teleport(NeoRogue.spawn);
			return;
		} else {
			SessionManager.removeFromSession(p.getUniqueId());
			TextComponent tc = Component.text().content(p.getName()).color(NamedTextColor.YELLOW)
					.append(Component.text(" left the lobby!", NamedTextColor.GRAY)).build();
			broadcast(tc);
			inLobby.remove(p.getUniqueId());
		}
		SessionManager.resetPlayer(p);
		p.teleport(NeoRogue.spawn);
		updateBoardLines();
	}

	public void broadcast(Component msg) {
		for (UUID uuid : inLobby) {
			Player p = Bukkit.getPlayer(uuid);
			Util.msgRaw(p, msg);
		}

		for (UUID uuid : s.getSpectators().keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			Util.msgRaw(p, msg);
		}
	}

	public void broadcast(String msg) {
		broadcast(NeoCore.miniMessage().deserialize(msg).colorIfAbsent(NamedTextColor.GRAY));
	}

	public HashSet<UUID> getInLobby() {
		return inLobby;
	}

	public boolean hasHostLeft() {
		return hostLeft;
	}

	public HashSet<UUID> getJoinRequests() {
		return joinRequests;
	}

	// Whether a player is allowed to join this lobby at all. New lobbies allow anyone;
	// loaded lobbies restrict joining to the saved party.
	protected boolean canJoin(Player requester) {
		return true;
	}

	// Whether an eligible requester joins immediately without host approval (e.g. returning
	// party members of a loaded game).
	protected boolean autoAccept(UUID requester) {
		return false;
	}

	// Called when a player runs /nr join <someone in this lobby>. Sends the host a request
	// they can accept or decline, unless the requester is eligible to auto-join.
	public void requestJoin(Player requester) {
		UUID uuid = requester.getUniqueId();
		if (s.isBusy()) {
			Util.msgRaw(requester, gameGenerating);
			return;
		}
		if (inLobby.contains(uuid)) {
			Util.displayError(requester, "You're already in this lobby!");
			return;
		}
		if (MAX_SIZE <= inLobby.size()) {
			Util.displayError(requester, "That lobby is full!");
			return;
		}
		if (!canJoin(requester)) {
			Util.displayError(requester, "You can't join this lobby!");
			return;
		}
		if (autoAccept(uuid)) {
			addPlayer(requester);
			return;
		}
		if (!joinRequests.add(uuid)) {
			Util.displayError(requester, "You've already requested to join this lobby!");
			return;
		}

		Util.msgRaw(requester, Component.text("Your request to join ", NamedTextColor.GRAY)
				.append(Component.text(name, NamedTextColor.YELLOW))
				.append(Component.text(" was sent!", NamedTextColor.GRAY)));
		Player hostp = Bukkit.getPlayer(host);
		if (hostp == null) return;
		String rn = requester.getName();
		Util.msgRaw(hostp, NeoCore.miniMessage().deserialize(
				"<gray>" + rn + " wants to join your lobby! "
				+ "<dark_gray>[<green><click:run_command:'/nr accept " + rn + "'><hover:show_text:'Accept " + rn
				+ "'>Accept</hover></click></green>]</dark_gray> "
				+ "<dark_gray>[<red><click:run_command:'/nr decline " + rn + "'><hover:show_text:'Decline " + rn
				+ "'>Decline</hover></click></red>]</dark_gray>"));
	}

	// Host accepts a pending join request, adding the player to the lobby.
	public void acceptRequest(Player hostPlayer, String username) {
		if (!hostPlayer.getUniqueId().equals(host)) {
			Util.msgRaw(hostPlayer, hostOnlyRespond);
			return;
		}
		if (s.isBusy()) {
			Util.msgRaw(hostPlayer, gameGenerating);
			return;
		}
		Player target = Bukkit.getPlayer(username);
		if (target == null || !joinRequests.contains(target.getUniqueId())) {
			Util.displayError(hostPlayer, "There's no pending join request from " + username + "!");
			return;
		}
		if (SessionManager.getSession(target) != null) {
			joinRequests.remove(target.getUniqueId());
			Util.displayError(hostPlayer, username + " is already in another session!");
			return;
		}
		joinRequests.remove(target.getUniqueId());
		addPlayer(target);
	}

	// Host declines a pending join request.
	public void declineRequest(Player hostPlayer, String username) {
		if (!hostPlayer.getUniqueId().equals(host)) {
			Util.msgRaw(hostPlayer, hostOnlyRespond);
			return;
		}
		Player target = Bukkit.getPlayer(username);
		if (target == null || !joinRequests.remove(target.getUniqueId())) {
			Util.displayError(hostPlayer, "There's no pending join request from " + username + "!");
			return;
		}
		Util.msgRaw(hostPlayer, Component.text("You declined ", NamedTextColor.GRAY)
				.append(Component.text(username, NamedTextColor.YELLOW))
				.append(Component.text("'s request to join.", NamedTextColor.GRAY)));
		Util.msgRaw(target, Component.text("Your request to join ", NamedTextColor.GRAY)
				.append(Component.text(name, NamedTextColor.YELLOW))
				.append(Component.text(" was declined.", NamedTextColor.GRAY)));
	}

	@Override
	public void cleanup(boolean pluginDisable) {
		holo.remove();
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		Bukkit.getLogger().warning("[NeoRogue] LobbyInstance attempted to save, this should never happen");
		return null;
	}

	@Override
	public void handlePlayerLeaveParty(OfflinePlayer p) {

	}
}
