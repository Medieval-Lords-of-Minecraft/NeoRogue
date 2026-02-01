package me.neoblade298.neorogue.session;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.region.NodeType;
import me.neoblade298.neorogue.region.Region;
import me.neoblade298.neorogue.session.chance.ChanceInstance;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.reward.RewardInstance;
import net.kyori.adventure.util.TriState;

public abstract class Instance {
	protected Session s;
	protected Location spawn;
	protected ArrayList<String> playerLines = new ArrayList<String>(), spectatorLines = new ArrayList<String>();
	protected PlayerFlags playerFlags, spectatorFlags = new PlayerFlags(PlayerFlag.CAN_FLY, PlayerFlag.INVISIBLE, PlayerFlag.INVULNERABLE);
	protected abstract void setup();
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
		spawn = new Location(Bukkit.getWorld(Region.WORLD_NAME), -(s.getXOff() + spawnX), 64, s.getZOff() + spawnZ);
		this.playerFlags = playerFlags;
	}
	
	public Instance(Session s, double spawnX, double spawnZ, PlayerFlags playerFlags, PlayerFlags spectatorFlags) {
		this.s = s;
		spawn = new Location(Bukkit.getWorld(Region.WORLD_NAME), -(s.getXOff() + spawnX), 64, s.getZOff() + spawnZ);
		this.playerFlags = playerFlags;
		this.spectatorFlags = spectatorFlags;
	}
	
	public void handleSpectatorInteractEvent(PlayerInteractEvent e) {
		
	}

	public void start() {
		for (Player p : s.getOnlinePlayers()) {
			playerFlags.applyFlags(p);
		}
		for (UUID uuid : s.getSpectators().keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			spectatorFlags.applyFlags(p);
		}
		setup();
	}
	
	public void handlePlayerRejoin(Player p) {
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
	
	public void handlePlayerLeave(Player p) {
		PlayerFlags.applyDefaults(p);
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
	
	public static Instance deserialize(Session s, ResultSet row, HashMap<UUID, PlayerSessionData> party) throws SQLException {
		String data = row.getString("instanceData");
		
		if (data.startsWith("SHRINE")) {
			return new ShrineInstance(s, data, party);
		}
		else if (data.startsWith("NODESELECT")) {
			return new NodeSelectInstance(s);
		}
		else if (data.startsWith("REWARD")) {
			String type = data.substring(data.indexOf(":") + 1);
			return new RewardInstance(s, party, NodeType.valueOf(type), false); // boolean is literally just to differentiate constructors
		}
		else if (data.startsWith("SHOP")) {
			return new ShopInstance(s, party);
		}
		else if (data.startsWith("CHANCE")) {
			return new ChanceInstance(s, data, party);
		}
		return null;
	}

	public ArrayList<String> getPlayerLines() {
		return playerLines;
	}

	public ArrayList<String> getSpectatorLines() {
		return spectatorLines;
	}

	public abstract void updateBoardLines();

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
		INVULNERABLE, CAN_FLY, INVISIBLE, ZERO_DAMAGE_TICKS, ALLOW_FLIGHT_FALL;

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
				case ZERO_DAMAGE_TICKS:
					p.setMaximumNoDamageTicks(0);
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
				case ZERO_DAMAGE_TICKS:
					p.setMaximumNoDamageTicks(20);
					break;
				case ALLOW_FLIGHT_FALL:
					p.setFlyingFallDamage(TriState.NOT_SET);
					break;
			}
		}
	}
}
