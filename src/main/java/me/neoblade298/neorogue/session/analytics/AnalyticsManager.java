package me.neoblade298.neorogue.session.analytics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.shared.io.SQLManager;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder.SQLAction;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.analytics.ChanceChoiceSnapshot.ChoiceRow;
import me.neoblade298.neorogue.session.analytics.FightSnapshot.EquipRow;
import me.neoblade298.neorogue.session.analytics.FightSnapshot.MobRow;
import me.neoblade298.neorogue.session.analytics.FightSnapshot.StatusRow;
import me.neoblade298.neorogue.session.analytics.OfferSnapshot.OfferRow;
import me.neoblade298.neorogue.session.fight.DamageType;

// Persists per-fight equipment effectiveness analytics into three normalized fact tables on the
// shared "NeoRogue" session SQL pool. Writes are batched and run asynchronously.
public class AnalyticsManager {
	// Bump this whenever equipment/balance changes meaningfully so analytics can be sliced per
	// balance pass. Older rows keep their original stamp. New rows are always written at this version.
	public static final int BALANCE_VERSION = 1;

	// The balance version that /nrlytics report queries read from. Defaults to the latest version but
	// can be changed at runtime (via /nrlytics balanceversion) to inspect older balance passes.
	private static int queryBalanceVersion = BALANCE_VERSION;

	public static int getQueryBalanceVersion() {
		return queryBalanceVersion;
	}

	public static void setQueryBalanceVersion(int version) {
		queryBalanceVersion = version;
	}

	// Master switch for analytics collection. Disable to skip all schema/recording work.
	public static final boolean ENABLED = true;

	private static boolean initialized = false;

	public static void init() {
		if (!ENABLED) return;
		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = SQLManager.getConnection("NeoRogue"); Statement stmt = con.createStatement()) {
					stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_analytics_fights ("
							+ "fightId VARCHAR(36) NOT NULL PRIMARY KEY,"
							+ "ts BIGINT NOT NULL,"
							+ "balanceVersion INT NOT NULL,"
							+ "host VARCHAR(36) NOT NULL,"
							+ "slot INT NOT NULL,"
							+ "runId VARCHAR(36) NOT NULL DEFAULT '',"
							+ "regionType VARCHAR(50) NOT NULL,"
							+ "nodeType VARCHAR(20) NOT NULL,"
							+ "level INT NOT NULL,"
							+ "regionsCompleted INT NOT NULL,"
							+ "partySize INT NOT NULL,"
							+ "notoriety INT NOT NULL,"
							+ "endless TINYINT NOT NULL,"
							+ "durationMs BIGINT NOT NULL,"
							+ "outcome TINYINT NOT NULL,"
							+ "partyDamageDealt DOUBLE NOT NULL,"
							+ "partyDamageTaken DOUBLE NOT NULL,"
							+ "mobs VARCHAR(255) NOT NULL"
							+ ");");

					// Run-level outcome fact: one row per finished run (win or loss). Lets any per-run
					// analytics (e.g. chance-choice winrate) join on runId to the run's final result.
					stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_analytics_runs ("
							+ "runId VARCHAR(36) NOT NULL PRIMARY KEY,"
							+ "ts BIGINT NOT NULL,"
							+ "balanceVersion INT NOT NULL,"
							+ "host VARCHAR(36) NOT NULL,"
							+ "slot INT NOT NULL,"
							+ "sessionType VARCHAR(40) NOT NULL,"
							+ "regionType VARCHAR(50) NOT NULL,"
							+ "regionsCompleted INT NOT NULL,"
							+ "level INT NOT NULL,"
							+ "partySize INT NOT NULL,"
							+ "notoriety INT NOT NULL,"
							+ "endless TINYINT NOT NULL,"
							+ "won TINYINT NOT NULL"
							+ ");");

					stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_analytics_fight_equipment ("
							+ "fightId VARCHAR(36) NOT NULL,"
							+ "playerUuid VARCHAR(36) NOT NULL,"
							+ "equipmentId VARCHAR(64) NOT NULL,"
							+ "upgraded TINYINT NOT NULL,"
							+ "rarity VARCHAR(20) NOT NULL,"
							+ "equipType VARCHAR(20) NOT NULL,"
							+ "equipClass VARCHAR(40) NOT NULL,"
							+ "outcome TINYINT NOT NULL,"
							+ "balanceVersion INT NOT NULL,"
							+ "damageDealt DOUBLE NOT NULL,"
							+ "damageBuffAdded DOUBLE NOT NULL,"
							+ "damageMitigated DOUBLE NOT NULL,"
							+ "shieldsApplied DOUBLE NOT NULL,"
							+ "healingDone DOUBLE NOT NULL,"
							+ "statusTotal INT NOT NULL,"
							+ "PRIMARY KEY (fightId, playerUuid, equipmentId, upgraded)"
							+ ");");

					stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_analytics_fight_equipment_status ("
							+ "fightId VARCHAR(36) NOT NULL,"
							+ "playerUuid VARCHAR(36) NOT NULL,"
							+ "equipmentId VARCHAR(64) NOT NULL,"
							+ "upgraded TINYINT NOT NULL,"
							+ "statusType VARCHAR(30) NOT NULL,"
							+ "stacks INT NOT NULL,"
							+ "balanceVersion INT NOT NULL,"
							+ "outcome TINYINT NOT NULL,"
							+ "PRIMARY KEY (fightId, playerUuid, equipmentId, upgraded, statusType)"
							+ ");");

					stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_analytics_equipment_offers ("
							+ "offerId VARCHAR(36) NOT NULL,"
							+ "slotIndex INT NOT NULL,"
							+ "ts BIGINT NOT NULL,"
							+ "balanceVersion INT NOT NULL,"
							+ "playerUuid VARCHAR(36) NOT NULL,"
							+ "host VARCHAR(36) NOT NULL,"
							+ "slot INT NOT NULL,"
							+ "source VARCHAR(20) NOT NULL,"
							+ "regionType VARCHAR(50) NOT NULL,"
							+ "nodeType VARCHAR(20) NOT NULL,"
							+ "level INT NOT NULL,"
							+ "equipmentId VARCHAR(64) NOT NULL,"
							+ "upgraded TINYINT NOT NULL,"
							+ "rarity VARCHAR(20) NOT NULL,"
							+ "equipType VARCHAR(20) NOT NULL,"
							+ "equipClass VARCHAR(40) NOT NULL,"
							+ "picked TINYINT NOT NULL,"
							+ "price INT NOT NULL,"
							+ "PRIMARY KEY (offerId, slotIndex)"
							+ ");");

					stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_analytics_fight_mobs ("
							+ "fightId VARCHAR(36) NOT NULL,"
							+ "mobId VARCHAR(64) NOT NULL,"
							+ "playerUuid VARCHAR(36) NOT NULL,"
							+ "playerClass VARCHAR(40) NOT NULL,"
							+ "ts BIGINT NOT NULL,"
							+ "balanceVersion INT NOT NULL,"
							+ "host VARCHAR(36) NOT NULL,"
							+ "slot INT NOT NULL,"
							+ "regionType VARCHAR(50) NOT NULL,"
							+ "nodeType VARCHAR(20) NOT NULL,"
							+ "level INT NOT NULL,"
							+ "outcome TINYINT NOT NULL,"
							+ "damageDealt DOUBLE NOT NULL,"
							+ "PRIMARY KEY (fightId, mobId, playerUuid)"
							+ ");");

					stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_analytics_fight_mob_damage ("
							+ "fightId VARCHAR(36) NOT NULL,"
							+ "mobId VARCHAR(64) NOT NULL,"
							+ "playerUuid VARCHAR(36) NOT NULL,"
							+ "playerClass VARCHAR(40) NOT NULL,"
							+ "damageType VARCHAR(30) NOT NULL,"
							+ "amount DOUBLE NOT NULL,"
							+ "balanceVersion INT NOT NULL,"
							+ "outcome TINYINT NOT NULL,"
							+ "PRIMARY KEY (fightId, mobId, playerUuid, damageType)"
							+ ");");

					stmt.execute("CREATE TABLE IF NOT EXISTS neorogue_analytics_chance_choices ("
							+ "pickId VARCHAR(36) NOT NULL,"
							+ "choiceIndex INT NOT NULL,"
							+ "ts BIGINT NOT NULL,"
							+ "balanceVersion INT NOT NULL,"
							+ "playerUuid VARCHAR(36) NOT NULL,"
							+ "playerClass VARCHAR(40) NOT NULL,"
							+ "host VARCHAR(36) NOT NULL,"
							+ "slot INT NOT NULL,"
							+ "setId VARCHAR(64) NOT NULL,"
							+ "stageId VARCHAR(64) NOT NULL,"
							+ "regionType VARCHAR(50) NOT NULL,"
							+ "nodeType VARCHAR(20) NOT NULL,"
							+ "level INT NOT NULL,"
							+ "individual TINYINT NOT NULL,"
							+ "choiceLabel VARCHAR(100) NOT NULL,"
							+ "valid TINYINT NOT NULL,"
							+ "picked TINYINT NOT NULL,"
							+ "runId VARCHAR(36) NOT NULL DEFAULT '',"
							+ "PRIMARY KEY (pickId, choiceIndex)"
							+ ");");


					createIndex(stmt, "idx_fights_balance", "neorogue_analytics_fights", "balanceVersion");
					createIndex(stmt, "idx_fights_region_node", "neorogue_analytics_fights", "regionType, nodeType");
					createIndex(stmt, "idx_fightequip_lookup", "neorogue_analytics_fight_equipment", "equipmentId, upgraded, balanceVersion");
					createIndex(stmt, "idx_fightstatus_lookup", "neorogue_analytics_fight_equipment_status", "equipmentId, statusType, balanceVersion");
					createIndex(stmt, "idx_offers_lookup", "neorogue_analytics_equipment_offers", "equipmentId, upgraded, balanceVersion");
					createIndex(stmt, "idx_offers_source", "neorogue_analytics_equipment_offers", "source, balanceVersion");
					createIndex(stmt, "idx_chance_lookup", "neorogue_analytics_chance_choices", "setId, stageId, balanceVersion");
					createIndex(stmt, "idx_chance_balance", "neorogue_analytics_chance_choices", "balanceVersion");
					createIndex(stmt, "idx_chance_class", "neorogue_analytics_chance_choices", "setId, playerClass, balanceVersion");
					createIndex(stmt, "idx_chance_run", "neorogue_analytics_chance_choices", "runId");
					createIndex(stmt, "idx_fights_run", "neorogue_analytics_fights", "runId");
					createIndex(stmt, "idx_runs_balance", "neorogue_analytics_runs", "balanceVersion");

					createIndex(stmt, "idx_fightmobs_lookup", "neorogue_analytics_fight_mobs", "mobId, balanceVersion");
					createIndex(stmt, "idx_fightmobs_class", "neorogue_analytics_fight_mobs", "mobId, playerClass, balanceVersion");
					createIndex(stmt, "idx_fightmobs_region", "neorogue_analytics_fight_mobs", "regionType, nodeType, balanceVersion");
					createIndex(stmt, "idx_fightmobdmg_lookup", "neorogue_analytics_fight_mob_damage", "mobId, damageType, balanceVersion");
					createIndex(stmt, "idx_fightmobdmg_class", "neorogue_analytics_fight_mob_damage", "mobId, playerClass, damageType, balanceVersion");

					initialized = true;
				}
				catch (SQLException ex) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to initialize analytics tables");
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	private static void createIndex(Statement stmt, String name, String table, String columns) {
		try {
			stmt.execute("CREATE INDEX " + name + " ON " + table + " (" + columns + ");");
		}
		catch (SQLException ignore) {
			// Index already exists
		}
	}

	public static void recordFight(FightSnapshot snap) {
		if (!ENABLED || !initialized || snap == null) return;
		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = SQLManager.getConnection("NeoRogue")) {
					writeFight(con, snap);
					writeEquipment(con, snap);
					writeStatuses(con, snap);
					writeMobs(con, snap);
					writeMobDamage(con, snap);
				}
				catch (SQLException ex) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to record fight analytics " + snap.fightId);
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	private static void writeFight(Connection con, FightSnapshot snap) throws SQLException {
		SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.INSERT, "neorogue_analytics_fights")
				.addValue("fightId", snap.fightId)
				.addValue("ts", snap.timestamp)
				.addValue("balanceVersion", snap.balanceVersion)
				.addValue("host", snap.host)
				.addValue("slot", snap.slot)
				.addValue("runId", snap.runId)
				.addValue("regionType", snap.regionType)
				.addValue("nodeType", snap.nodeType)
				.addValue("level", snap.level)
				.addValue("regionsCompleted", snap.regionsCompleted)
				.addValue("partySize", snap.partySize)
				.addValue("notoriety", snap.notoriety)
				.addValue("endless", snap.endless ? 1 : 0)
				.addValue("durationMs", snap.durationMs)
				.addValue("outcome", snap.won ? 1 : 0)
				.addValue("partyDamageDealt", snap.partyDamageDealt)
				.addValue("partyDamageTaken", snap.partyDamageTaken)
				.addValue("mobs", snap.mobs);
		PreparedStatement ps = sql.build(con);
		ps.executeBatch();
		ps.close();
	}

	private static void writeEquipment(Connection con, FightSnapshot snap) throws SQLException {
		if (snap.equipRows.isEmpty()) return;
		SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.INSERT, "neorogue_analytics_fight_equipment");
		for (EquipRow row : snap.equipRows) {
			sql.addValue("fightId", snap.fightId)
					.addValue("playerUuid", row.playerUuid)
					.addValue("equipmentId", row.equipmentId)
					.addValue("upgraded", row.upgraded ? 1 : 0)
					.addValue("rarity", row.rarity)
					.addValue("equipType", row.equipType)
					.addValue("equipClass", row.equipClass)
					.addValue("outcome", snap.won ? 1 : 0)
					.addValue("balanceVersion", snap.balanceVersion)
					.addValue("damageDealt", row.damageDealt)
					.addValue("damageBuffAdded", row.damageBuffAdded)
					.addValue("damageMitigated", row.damageMitigated)
					.addValue("shieldsApplied", row.shieldsApplied)
					.addValue("healingDone", row.healingDone)
					.addValue("statusTotal", row.statusTotal)
					.addRow();
		}
		if (sql.getRowCount() > 0) {
			PreparedStatement ps = sql.build(con);
			ps.executeBatch();
			ps.close();
		}
	}

	public static void recordRun(RunSnapshot snap) {
		if (!ENABLED || !initialized || snap == null || snap.runId == null || snap.runId.isEmpty()) return;
		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = SQLManager.getConnection("NeoRogue")) {
					writeRun(con, snap);
				}
				catch (SQLException ex) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to record run analytics " + snap.runId);
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	private static void writeRun(Connection con, RunSnapshot snap) throws SQLException {
		SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_analytics_runs")
				.addValue("runId", snap.runId)
				.addValue("ts", snap.timestamp)
				.addValue("balanceVersion", snap.balanceVersion)
				.addValue("host", snap.host)
				.addValue("slot", snap.slot)
				.addValue("sessionType", snap.sessionType)
				.addValue("regionType", snap.regionType)
				.addValue("regionsCompleted", snap.regionsCompleted)
				.addValue("level", snap.level)
				.addValue("partySize", snap.partySize)
				.addValue("notoriety", snap.notoriety)
				.addValue("endless", snap.endless ? 1 : 0)
				.addValue("won", snap.won ? 1 : 0);
		PreparedStatement ps = sql.build(con);
		ps.executeBatch();
		ps.close();
	}

	public static void recordOffer(OfferSnapshot snap) {
		if (!ENABLED || !initialized || snap == null || snap.rows.isEmpty()) return;
		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = SQLManager.getConnection("NeoRogue")) {
					writeOffer(con, snap);
				}
				catch (SQLException ex) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to record offer analytics " + snap.offerId);
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	public static void recordChanceChoice(ChanceChoiceSnapshot snap) {
		if (!ENABLED || !initialized || snap == null || snap.rows.isEmpty()) return;
		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = SQLManager.getConnection("NeoRogue")) {
					writeChanceChoices(con, snap);
				}
				catch (SQLException ex) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to record chance choice analytics " + snap.pickId);
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	private static void writeChanceChoices(Connection con, ChanceChoiceSnapshot snap) throws SQLException {
		if (snap.rows.isEmpty()) return;
		SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.INSERT, "neorogue_analytics_chance_choices");
		for (ChoiceRow row : snap.rows) {
			sql.addValue("pickId", snap.pickId)
					.addValue("choiceIndex", row.choiceIndex)
					.addValue("ts", snap.timestamp)
					.addValue("balanceVersion", snap.balanceVersion)
					.addValue("playerUuid", snap.playerUuid)
					.addValue("playerClass", snap.playerClass)
					.addValue("host", snap.host)
					.addValue("slot", snap.slot)
					.addValue("setId", snap.setId)
					.addValue("stageId", snap.stageId)
					.addValue("regionType", snap.regionType)
					.addValue("nodeType", snap.nodeType)
					.addValue("level", snap.level)
					.addValue("individual", snap.individual ? 1 : 0)
					.addValue("choiceLabel", row.label)
					.addValue("valid", row.valid ? 1 : 0)
					.addValue("picked", row.picked ? 1 : 0)
					.addValue("runId", snap.runId)
					.addRow();
		}
		if (sql.getRowCount() > 0) {
			PreparedStatement ps = sql.build(con);
			ps.executeBatch();
			ps.close();
		}
	}

	private static void writeOffer(Connection con, OfferSnapshot snap) throws SQLException {
		if (snap.rows.isEmpty()) return;
		SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.INSERT, "neorogue_analytics_equipment_offers");
		for (OfferRow row : snap.rows) {
			sql.addValue("offerId", snap.offerId)
					.addValue("slotIndex", row.slotIndex)
					.addValue("ts", snap.timestamp)
					.addValue("balanceVersion", snap.balanceVersion)
					.addValue("playerUuid", snap.playerUuid)
					.addValue("host", snap.host)
					.addValue("slot", snap.slot)
					.addValue("source", snap.source)
					.addValue("regionType", snap.regionType)
					.addValue("nodeType", snap.nodeType)
					.addValue("level", snap.level)
					.addValue("equipmentId", row.equipmentId)
					.addValue("upgraded", row.upgraded ? 1 : 0)
					.addValue("rarity", row.rarity)
					.addValue("equipType", row.equipType)
					.addValue("equipClass", row.equipClass)
					.addValue("picked", row.picked ? 1 : 0)
					.addValue("price", row.price)
					.addRow();
		}
		if (sql.getRowCount() > 0) {
			PreparedStatement ps = sql.build(con);
			ps.executeBatch();
			ps.close();
		}
	}

	private static void writeMobs(Connection con, FightSnapshot snap) throws SQLException {
		if (snap.mobRows.isEmpty()) return;
		SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.INSERT, "neorogue_analytics_fight_mobs");
		for (MobRow row : snap.mobRows) {
			sql.addValue("fightId", snap.fightId)
					.addValue("mobId", row.mobId)
					.addValue("playerUuid", row.playerUuid)
					.addValue("playerClass", row.playerClass)
					.addValue("ts", snap.timestamp)
					.addValue("balanceVersion", snap.balanceVersion)
					.addValue("host", snap.host)
					.addValue("slot", snap.slot)
					.addValue("regionType", snap.regionType)
					.addValue("nodeType", snap.nodeType)
					.addValue("level", snap.level)
					.addValue("outcome", snap.won ? 1 : 0)
					.addValue("damageDealt", row.damageDealt)
					.addRow();
		}
		if (sql.getRowCount() > 0) {
			PreparedStatement ps = sql.build(con);
			ps.executeBatch();
			ps.close();
		}
	}

	private static void writeMobDamage(Connection con, FightSnapshot snap) throws SQLException {
		if (snap.mobRows.isEmpty()) return;
		SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.INSERT, "neorogue_analytics_fight_mob_damage");
		for (MobRow row : snap.mobRows) {
			for (Entry<DamageType, Double> ent : row.byType.entrySet()) {
				if (ent.getValue() == null || ent.getValue() <= 0) continue;
				sql.addValue("fightId", snap.fightId)
						.addValue("mobId", row.mobId)
						.addValue("playerUuid", row.playerUuid)
						.addValue("playerClass", row.playerClass)
						.addValue("damageType", ent.getKey().name())
						.addValue("amount", ent.getValue())
						.addValue("balanceVersion", snap.balanceVersion)
						.addValue("outcome", snap.won ? 1 : 0)
						.addRow();
			}
		}
		if (sql.getRowCount() > 0) {
			PreparedStatement ps = sql.build(con);
			ps.executeBatch();
			ps.close();
		}
	}

	private static void writeStatuses(Connection con, FightSnapshot snap) throws SQLException {
		if (snap.statusRows.isEmpty()) return;
		SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.INSERT, "neorogue_analytics_fight_equipment_status");
		for (StatusRow row : snap.statusRows) {
			sql.addValue("fightId", snap.fightId)
					.addValue("playerUuid", row.playerUuid)
					.addValue("equipmentId", row.equipmentId)
					.addValue("upgraded", row.upgraded ? 1 : 0)
					.addValue("statusType", row.statusType.name())
					.addValue("stacks", row.stacks)
					.addValue("balanceVersion", snap.balanceVersion)
					.addValue("outcome", snap.won ? 1 : 0)
					.addRow();
		}
		if (sql.getRowCount() > 0) {
			PreparedStatement ps = sql.build(con);
			ps.executeBatch();
			ps.close();
		}
	}
}
