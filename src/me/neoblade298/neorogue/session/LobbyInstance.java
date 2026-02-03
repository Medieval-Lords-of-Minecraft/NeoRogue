package me.neoblade298.neorogue.session;

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
import me.neoblade298.neorogue.player.PlayerSessionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public abstract class LobbyInstance extends Instance {
	public static final int MAX_SIZE = 4;

	protected Session session;
	protected HashSet<UUID> invited = new HashSet<UUID>(), inLobby = new HashSet<UUID>();
	protected UUID host;
	protected Component partyInfoHeader;
	protected TextDisplay holo;

	// Static error messages
	protected static String invPrefix = "<dark_gray>[<green><click:run_command:'/nr join ",
			invSuffix = "'><hover:show_text:'Click to accept invite'>Click here to accept the invite!</hover></click></green>]";
	protected static final TextComponent gameGenerating = Component.text("Your game is generating! You can't do this right now!",
					NamedTextColor.RED),
			hostOnlyKick = Component.text("Only the host may kick other players!", NamedTextColor.RED),
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

	public void leavePlayer(Player p) {
		if (s.isBusy()) {
			Util.msgRaw(p, gameGenerating);
			return;
		}

		if (p.getUniqueId().equals(host)) {
			TextComponent tc = Component.text().content(p.getName()).color(NamedTextColor.YELLOW)
					.append(Component.text(" disbanded the lobby!", NamedTextColor.GRAY)).build();
			broadcast(tc);
			SessionManager.endSession(s);
		} else {
			SessionManager.removeFromSession(p.getUniqueId());
			TextComponent tc = Component.text().content(p.getName()).color(NamedTextColor.YELLOW)
					.append(Component.text(" left the lobby!", NamedTextColor.GRAY)).build();
			broadcast(tc);
			inLobby.remove(p.getUniqueId());
		}
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

	public HashSet<UUID> getInvited() {
		return invited;
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
