package me.neoblade298.neorogue.session.instances;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.MapViewer;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.region.Region;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.FightInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;

public abstract class Instance {
	protected Session s;
	protected Location spawn;
	protected ArrayList<String> playerLines = new ArrayList<String>(), spectatorLines = new ArrayList<String>();
	protected PlayerFlags playerFlags, spectatorFlags = new PlayerFlags(PlayerFlag.CAN_FLY, PlayerFlag.INVISIBLE, PlayerFlag.INVULNERABLE);
	public abstract void setup();
	public abstract void cleanup(boolean pluginDisable);
	public abstract void handleInteractEvent(PlayerInteractEvent e);
	public abstract void handlePlayerLeaveParty(OfflinePlayer p);
	public abstract String serialize(HashMap<UUID, PlayerSessionData> party);
	public Instance(Session s, PlayerFlags playerFlags) {
		this.s = s;
		this.playerFlags = playerFlags;
	}

	public Instance(Session s, PlayerFlags playerFlags, PlayerFlags spectatorFlags) {
		this.s = s;
		this.playerFlags = playerFlags;
		this.spectatorFlags = spectatorFlags;
	}

	public Instance(Session s, double spawnX, double spawnZ, PlayerFlags playerFlags) {
		this.s = s;
		spawn = new Location(Bukkit.getWorld(Region.WORLD_NAME), -(s.getXOff() + spawnX), 64.5, s.getZOff() + spawnZ);
		this.playerFlags = playerFlags;
	}
	
	public Instance(Session s, double spawnX, double spawnZ, PlayerFlags playerFlags, PlayerFlags spectatorFlags) {
		this(s, spawnX, spawnZ, playerFlags);
		this.spectatorFlags = spectatorFlags;
	}
	
	public void handleSpectatorInteractEvent(PlayerInteractEvent e) {
		
	}

	public void start() {
		loadChunks();
		for (Player p : s.getOnlinePlayers()) {
			playerFlags.applyFlags(p);
		}
		for (UUID uuid : s.getSpectators().keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			spectatorFlags.applyFlags(p);
			s.setupSpectatorInventory(p);
		}
		setup();
		s.getSessionType().onInstanceSetup(s, this);
	}

	public void loadChunks() {
		if (spawn == null) return;
		World world = spawn.getWorld();
		int chunkX = spawn.getBlockX() >> 4;
		int chunkZ = spawn.getBlockZ() >> 4;
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				world.addPluginChunkTicket(chunkX + dx, chunkZ + dz, NeoRogue.inst());
			}
		}
	}

	public void unloadChunks() {
		if (spawn == null) return;
		World world = spawn.getWorld();
		int chunkX = spawn.getBlockX() >> 4;
		int chunkZ = spawn.getBlockZ() >> 4;
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				world.removePluginChunkTicket(chunkX + dx, chunkZ + dz, NeoRogue.inst());
			}
		}
	}
	
	public void handlePlayerLogin(Player p) {
		if (s.isSpectator(p.getUniqueId())) {
			spectatorFlags.applyFlags(p);
		}
		else {
			playerFlags.applyFlags(p);
		}

		// Do not teleport if it's a player in a fight instance
		if (!(s.getInstance() instanceof FightInstance && !s.isSpectator(p.getUniqueId()))) {
			p.teleport(spawn);
		}
	}
	
	public void handlePlayerLogout(Player p) {
		PlayerFlags.applyDefaults(p);
	}

	public void handleSpectatorJoin(Player p) {
		
	}

	public void handleSpectatorLeave(Player p) {

	}
	
	public Location getSpawn() {
		return spawn;
	}

	// Done so that players don't push each other around
	public void teleportRandomly(Player p) {
		double dx = NeoRogue.gen.nextDouble(-1, 1);
		double dz = NeoRogue.gen.nextDouble(-1, 1);
		p.teleport(spawn.clone().add(dx, 0, dz));
	}

	public PlayerFlags getPlayerFlags() {
		return playerFlags;
	}

	public PlayerFlags getSpectatorFlags() {
		return spectatorFlags;
	}

	public static Instance deserialize(Session s, String data, HashMap<UUID, PlayerSessionData> party) throws SQLException {
		InstanceType type = InstanceType.fromData(data);
		if (type == null) {
			Bukkit.getLogger().warning("[NeoRogue] Cannot deserialize instance, unknown data prefix: " + data);
			return null;
		}
		return type.deserialize(s, data, party);
	}

	public ArrayList<String> getPlayerLines() {
		return playerLines;
	}

	public ArrayList<String> getSpectatorLines() {
		return spectatorLines;
	}

	public abstract void updateBoardLines();

	// Refreshes the action bar for this instance's party and spectators. FightInstance overrides this to
	// render each player's PlayerFightData bar; other instances use the PlayerSessionData bar below.
	public void updateActionBar() {
		for (PlayerSessionData data : s.getParty().values()) {
			Player p = data.getPlayer();
			if (p == null) continue;
			Component bar = getActionBar(data);
			if (bar != null) p.sendActionBar(bar);
		}
		updateSpectatorActionBars();
	}

	// Sends the spectator action bar to every spectator. Shared by all instances.
	protected void updateSpectatorActionBars() {
		for (MapViewer viewer : s.getSpectators().values()) {
			Player p = Bukkit.getPlayer(viewer.getUniqueId());
			if (p == null) continue;
			Component bar = getSpectatorActionBar(viewer);
			if (bar != null) p.sendActionBar(bar);
		}
	}

	public Component getActionBar(PlayerSessionData data) {
		return Component.text(data.getCoins() + " " + PlayerSessionData.CURRENCY, NamedTextColor.YELLOW);
	}

	protected Component getSpectatorActionBar(MapViewer viewer) {
		return null;
	}

	public static class PlayerFlags {
		private HashSet<PlayerFlag> flags = new HashSet<PlayerFlag>();

		public PlayerFlags(PlayerFlag... flags) {
			for (PlayerFlag flag : flags) {
				this.flags.add(flag);
			}
		}

		public void applyFlags(Player p) {
			for (PlayerFlag flag : PlayerFlag.values()) {
				if (flags.contains(flag)) {
					flag.applyFlag(p);
				}
				else {
					flag.applyDefault(p);
				}
			}
		}

		public static void applyDefaults(Player p) {
			for (PlayerFlag flag : PlayerFlag.values()) {
				flag.applyDefault(p);
			}
		}
	}

	public enum PlayerFlag {
		INVULNERABLE, CAN_FLY, INVISIBLE, SHORTER_IFRAMES, ALLOW_FLIGHT_FALL;

		public void applyFlag(Player p) {
			switch (this) {
				case INVULNERABLE:
					p.setInvulnerable(true);
					break;
				case CAN_FLY:
					p.setAllowFlight(true);
					break;
				case INVISIBLE:
					p.setInvisible(true);
					break;
				case SHORTER_IFRAMES:
					p.setMaximumNoDamageTicks(5);
					break;
				case ALLOW_FLIGHT_FALL:
					p.setFlyingFallDamage(TriState.TRUE);
					break;
			}
		}

		public void applyDefault(Player p) {
			switch (this) {
				case INVULNERABLE:
					p.setInvulnerable(false);
					break;
				case CAN_FLY:
					p.setAllowFlight(false);
					p.setFlying(false);
					break;
				case INVISIBLE:
					p.setInvisible(false);
					break;
				case SHORTER_IFRAMES:
					p.setMaximumNoDamageTicks(20);
					break;
				case ALLOW_FLIGHT_FALL:
					p.setFlyingFallDamage(TriState.NOT_SET);
					break;
			}
		}
	}
}
