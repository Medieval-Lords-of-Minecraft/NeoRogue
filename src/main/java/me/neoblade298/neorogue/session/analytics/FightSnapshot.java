package me.neoblade298.neorogue.session.analytics;

import java.util.ArrayList;
import java.util.HashMap;

import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;

// Immutable, Bukkit-free snapshot of a single completed fight, built on the main thread at fight
// finalization and handed to AnalyticsManager for asynchronous persistence.
public class FightSnapshot {
	public final String fightId;
	public final long timestamp;
	public final int balanceVersion;
	public final String host;
	public final int slot;
	public final String runId;
	public final String regionType;
	public final String nodeType;
	public final int level;
	public final int regionsCompleted;
	public final int partySize;
	public final int notoriety;
	public final boolean endless;
	public final long durationMs;
	public final boolean won;
	public final double partyDamageDealt;
	public final double partyDamageTaken;
	public final String mobs;

	public final ArrayList<EquipRow> equipRows = new ArrayList<EquipRow>();
	public final ArrayList<StatusRow> statusRows = new ArrayList<StatusRow>();
	public final ArrayList<MobRow> mobRows = new ArrayList<MobRow>();

	public FightSnapshot(String fightId, long timestamp, int balanceVersion, String host, int slot, String runId,
			String regionType, String nodeType, int level, int regionsCompleted, int partySize, int notoriety,
			boolean endless, long durationMs, boolean won, double partyDamageDealt, double partyDamageTaken, String mobs) {
		this.fightId = fightId;
		this.timestamp = timestamp;
		this.balanceVersion = balanceVersion;
		this.host = host;
		this.slot = slot;
		this.runId = runId;
		this.regionType = regionType;
		this.nodeType = nodeType;
		this.level = level;
		this.regionsCompleted = regionsCompleted;
		this.partySize = partySize;
		this.notoriety = notoriety;
		this.endless = endless;
		this.durationMs = durationMs;
		this.won = won;
		this.partyDamageDealt = partyDamageDealt;
		this.partyDamageTaken = partyDamageTaken;
		this.mobs = mobs;
	}

	// A single (player, equipment-variant) row of value-added totals.
	public static class EquipRow {
		public final String playerUuid;
		public final String equipmentId;
		public final boolean upgraded;
		public final String rarity;
		public final String equipType;
		public final String equipClass;
		public final double damageDealt;
		public final double damageBuffAdded;
		public final double damageMitigated;
		public final double shieldsApplied;
		public final double healingDone;
		public final int statusTotal;

		public EquipRow(String playerUuid, String equipmentId, boolean upgraded, String rarity, String equipType,
				String equipClass, double damageDealt, double damageBuffAdded, double damageMitigated,
				double shieldsApplied, double healingDone, int statusTotal) {
			this.playerUuid = playerUuid;
			this.equipmentId = equipmentId;
			this.upgraded = upgraded;
			this.rarity = rarity;
			this.equipType = equipType;
			this.equipClass = equipClass;
			this.damageDealt = damageDealt;
			this.damageBuffAdded = damageBuffAdded;
			this.damageMitigated = damageMitigated;
			this.shieldsApplied = shieldsApplied;
			this.healingDone = healingDone;
			this.statusTotal = statusTotal;
		}
	}

	// A single (player, equipment-variant, status) row of stacks applied.
	public static class StatusRow {
		public final String playerUuid;
		public final String equipmentId;
		public final boolean upgraded;
		public final StatusType statusType;
		public final int stacks;

		public StatusRow(String playerUuid, String equipmentId, boolean upgraded, StatusType statusType, int stacks) {
			this.playerUuid = playerUuid;
			this.equipmentId = equipmentId;
			this.upgraded = upgraded;
			this.statusType = statusType;
			this.stacks = stacks;
		}
	}

	// A single mob's damage dealt to one player during this fight. byType holds the per-damage-type
	// breakdown; damageDealt is the total across all types. playerClass is denormalized so analytics
	// can slice by class without a join, mirroring how EquipRow stores equipClass.
	public static class MobRow {
		public final String mobId;
		public final String playerUuid;
		public final String playerClass;
		public final double damageDealt;
		public final HashMap<DamageType, Double> byType;

		public MobRow(String mobId, String playerUuid, String playerClass, double damageDealt,
				HashMap<DamageType, Double> byType) {
			this.mobId = mobId;
			this.playerUuid = playerUuid;
			this.playerClass = playerClass;
			this.damageDealt = damageDealt;
			this.byType = byType;
		}
	}
}
