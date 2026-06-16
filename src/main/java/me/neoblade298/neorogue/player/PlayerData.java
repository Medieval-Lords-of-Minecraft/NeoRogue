package me.neoblade298.neorogue.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder.SQLAction;
import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.DropTableSet;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.unlock.UnlockRegistry;
import me.neoblade298.neorogue.session.NodeSelectInstance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class PlayerData {
	private static class ClassProgression {
		int level = 1;
		int exp = 0;
		int points = 0;
	}

	private ClassProgression getOrCreateProgression(EquipmentClass ec) {
		return progression.computeIfAbsent(ec, k -> new ClassProgression());
	}

	private void initializeDefaultProgression() {
		getOrCreateProgression(null);
		getOrCreateProgression(EquipmentClass.WARRIOR);
		getOrCreateProgression(EquipmentClass.THIEF);
		getOrCreateProgression(EquipmentClass.MAGE);
		getOrCreateProgression(EquipmentClass.ARCHER);
	}

	private HashMap<EquipmentClass, ClassProgression> progression = new HashMap<>();
	private UUID uuid;
	private Player p;
	private String display;
	private HashMap<Integer, SessionSnapshot> snapshots = new HashMap<Integer, SessionSnapshot>();
	private HashSet<String> unlockNodes = new HashSet<String>();
	private HashSet<String> flags = new HashSet<String>();
	private DropTableSet<Equipment> equipmentDroptable;
	private boolean equipmentDroptableDirty;
	private HashMap<String, AchievementProgress> globalAchievements = new HashMap<>();
	private EnumMap<EquipmentClass, HashMap<String, AchievementProgress>> classAchievements = new EnumMap<>(EquipmentClass.class);
	private int slotsAvailable, maxNotoriety;
	private BukkitTask unlockNodesSaveTask;
	private BukkitTask flagsSaveTask;
	
	// Create new one if one doesn't exist
	public PlayerData(Player p) {
		this.uuid = p.getUniqueId();
		this.p = p;
		this.display = p.getName();

		initializeDefaultProgression();
		if (p.hasPermission("donator.diamond")) {
			slotsAvailable = 5;
		}
		else if (p.hasPermission("donator.emerald")) {
			slotsAvailable = 3;
		}
		else if (p.hasPermission("donator.sapphire")) {
			slotsAvailable = 2;
		}
		else {
			slotsAvailable = 1;
		}
		initializeEquipmentDroptable();
	}
	
	public PlayerData(Player p, Statement stmt) {
		this(p);
		try (Connection con = NeoCore.getConnection("NeoRogue-PlayerManager");
				PreparedStatement baseStmt = con.prepareStatement("SELECT * FROM neorogue_playerdata WHERE uuid = ?;");
				PreparedStatement unlockStmt = con.prepareStatement("SELECT * FROM neorogue_unlocknodes WHERE uuid = ?;");
				PreparedStatement flagsStmt = con.prepareStatement("SELECT * FROM neorogue_playerflags WHERE uuid = ?;");
				PreparedStatement savesStmt = con.prepareStatement("SELECT * FROM neorogue_sessions WHERE host = ?;");
				PreparedStatement partyStmt = con.prepareStatement("SELECT * FROM neorogue_playersessiondata WHERE host = ? AND slot = ?;")) {
			UUID uuid = p.getUniqueId();
			String uuidStr = uuid.toString();
			baseStmt.setString(1, uuidStr);
			try (ResultSet base = baseStmt.executeQuery()) {
				if (!base.next()) {
					saveDisplayAsync();
					return;
				}
			}

			// Load class progression from separate table
			try (PreparedStatement classStmt = con.prepareStatement("SELECT * FROM neorogue_playerclass WHERE uuid = ?;")) {
				classStmt.setString(1, uuidStr);
				try (ResultSet classRs = classStmt.executeQuery()) {
					while (classRs.next()) {
						String classKey = classRs.getString("class");
						EquipmentClass ec = classKey.equals("GLOBAL") ? null : EquipmentClass.valueOf(classKey);
						ClassProgression prog = progression.get(ec);
						if (prog == null) continue;
						prog.level = Math.max(1, classRs.getInt("level"));
						prog.exp = classRs.getInt("exp");
						prog.points = classRs.getInt("points");
					}
				}
			}

			unlockStmt.setString(1, uuidStr);
			try (ResultSet unlocks = unlockStmt.executeQuery()) {
				while (unlocks.next()) {
					unlockNodes.add(UnlockRegistry.normalizeNodeId(unlocks.getString("node")));
				}
			}

			flagsStmt.setString(1, uuidStr);
			try (ResultSet flagsRs = flagsStmt.executeQuery()) {
				while (flagsRs.next()) {
					flags.add(flagsRs.getString("flag"));
				}
			}

			// Load achievements
			try (PreparedStatement achStmt = con.prepareStatement("SELECT * FROM neorogue_achievements WHERE uuid = ?;")) {
				achStmt.setString(1, uuidStr);
				try (ResultSet achRs = achStmt.executeQuery()) {
					while (achRs.next()) {
						String achId = achRs.getString("achievement");
						int prog = achRs.getInt("progress");
						String scope = achRs.getString("scope");
						String data = achRs.getString("data");
						Achievement ach = AchievementManager.get(achId);
						if (ach == null) continue;
						if (scope == null || scope.equals("GLOBAL")) {
							globalAchievements.put(achId, new AchievementProgress(ach, prog, null, data));
						} else {
							try {
								EquipmentClass ec = EquipmentClass.valueOf(scope);
								classAchievements.computeIfAbsent(ec, k -> new HashMap<>())
										.put(achId, new AchievementProgress(ach, prog, ec, data));
							} catch (IllegalArgumentException ex) {
								// Unknown scope, treat as global
								globalAchievements.put(achId, new AchievementProgress(ach, prog, null, data));
							}
						}
					}
				}
			}
			
			// Load snapshots
			savesStmt.setString(1, uuidStr);
			try (ResultSet saves = savesStmt.executeQuery()) {
				while (saves.next()) {
					int slot = saves.getInt("slot");
					partyStmt.setString(1, uuidStr);
					partyStmt.setInt(2, slot);
					try (ResultSet party = partyStmt.executeQuery()) {
						snapshots.put(slot, new SessionSnapshot(saves, party));
					}
				}
			}
			for (String defaultNode : UnlockRegistry.getDefaultNodes()) {
				unlockNodes.add(defaultNode);
			}
			markEquipmentDroptableDirty();
			initializeEquipmentDroptable();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		saveDisplayAsync();
	}
	
	public Player getPlayer() {
		if (p == null) {
			p = Bukkit.getPlayer(uuid);
		}
		return p;
	}
	
	public void updatePlayer() {
		p = Bukkit.getPlayer(uuid);
		if (p != null && !p.getName().equals(display)) {
			display = p.getName();
			saveDisplayAsync();
		}
	}

	public void initializeEquipmentDroptable() {
		equipmentDroptable = UnlockRegistry.buildEquipmentDroptable(this);
		equipmentDroptableDirty = false;
		Bukkit.getLogger().fine("[NeoRogue] Rebuilt equipment droptable for " + uuid + " (" + unlockNodes.size() + " unlock nodes)");
	}

	private void markEquipmentDroptableDirty() {
		equipmentDroptableDirty = true;
	}

	public DropTableSet<Equipment> getEquipmentDroptable() {
		if (equipmentDroptable == null || equipmentDroptableDirty) {
			initializeEquipmentDroptable();
		}
		else if (equipmentDroptable.isEmpty()) {
			Bukkit.getLogger().warning("[NeoRogue] Reinitializing empty equipment droptable for " + uuid);
			initializeEquipmentDroptable();
		}
		return equipmentDroptable;
	}
	
	public String getDisplay() {
		return display;
	}
	
	public int getLevel() {
		return getOrCreateProgression(null).level;
	}
	
	public int getExp() {
		return getOrCreateProgression(null).exp;
	}
	
	public int getSlots() {
		return slotsAvailable;
	}

	public int getLevel(EquipmentClass ec) {
		return getOrCreateProgression(ec).level;
	}

	public int getExp(EquipmentClass ec) {
		return getOrCreateProgression(ec).exp;
	}

	public int getPoints(EquipmentClass ec) {
		return getOrCreateProgression(ec).points;
	}

	public void setLevel(EquipmentClass ec, int value) {
		getOrCreateProgression(ec).level = value;
	}

	public void setExp(EquipmentClass ec, int value) {
		getOrCreateProgression(ec).exp = value;
	}

	public void setPoints(EquipmentClass ec, int value) {
		getOrCreateProgression(ec).points = value;
	}

	public void addPoints(EquipmentClass ec, int amount) {
		getOrCreateProgression(ec).points += amount;
	}

	public static int getXpRequired(int currentLevel) {
		if (currentLevel >= 50) return 2500;
		if (currentLevel < 5) return 20 * currentLevel * currentLevel;
		return 200 * currentLevel - 500;
	}

	public void addExp(EquipmentClass ec, int amount) {
		// Add to class
		addExpInternal(ec, amount);
		// Add to global
		if (ec != null) {
			addExpInternal(null, amount);
		}
		saveClassProgressionAsync();
	}

	private void addExpInternal(EquipmentClass ec, int amount) {
		int currentExp = getExp(ec) + amount;
		int currentLevel = getLevel(ec);
		int required = getXpRequired(currentLevel);
		while (currentExp >= required) {
			currentExp -= required;
			currentLevel++;
			onLevelUp(ec, currentLevel);
			required = getXpRequired(currentLevel);
		}
		setExp(ec, currentExp);
		setLevel(ec, currentLevel);
	}

	private void onLevelUp(EquipmentClass ec, int newLevel) {
		addPoints(ec, Math.min(newLevel, 10));
		Player player = getPlayer();
		if (player == null || !player.isOnline()) return;
		String category = ec == null ? "Global" : ec.getDisplay();
		Title title = Title.title(
			Component.text("Level Up!", NamedTextColor.GOLD),
			Component.text(category + " Level " + newLevel, NamedTextColor.YELLOW)
		);
		player.showTitle(title);
		player.playSound(player, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
	}

	public void updateSnapshot(Session s, int saveSlot) {
		snapshots.put(saveSlot, new SessionSnapshot(s));
	}
	
	public void removeSnapshot(int saveSlot) {
		snapshots.remove(saveSlot);
	}

	public int getMaxNotoriety() {
		return maxNotoriety;
	}

	public void addMaxNotoriety(int amount) {
		maxNotoriety += amount;
	}

	private void saveDisplayAsync() {
		final String uuidStr = uuid.toString();
		final String name = display;
		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = NeoCore.getConnection("NeoRogue-PlayerData")) {
					try (PreparedStatement ps = con.prepareStatement(
							"REPLACE INTO neorogue_playerdata (uuid, display) VALUES (?, ?);")) {
						ps.setString(1, uuidStr);
						ps.setString(2, name);
						ps.executeUpdate();
					}
				} catch (SQLException ex) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to save display name for " + uuidStr);
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	private void saveClassProgressionAsync() {
		final String uuidStr = uuid.toString();
		final HashMap<EquipmentClass, ClassProgression> snapshot = new HashMap<>();
		for (var entry : progression.entrySet()) {
			ClassProgression orig = entry.getValue();
			ClassProgression copy = new ClassProgression();
			copy.level = orig.level;
			copy.exp = orig.exp;
			copy.points = orig.points;
			snapshot.put(entry.getKey(), copy);
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = NeoCore.getConnection("NeoRogue-PlayerData")) {
					try (PreparedStatement clear = con.prepareStatement(
							"DELETE FROM neorogue_playerclass WHERE uuid = ?;")) {
						clear.setString(1, uuidStr);
						clear.executeUpdate();
					}
					SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_playerclass");
					for (var entry : snapshot.entrySet()) {
						String classKey = entry.getKey() == null ? "GLOBAL" : entry.getKey().name();
						ClassProgression prog = entry.getValue();
						sql.addValue("uuid", uuidStr)
								.addValue("class", classKey)
								.addValue("level", prog.level)
								.addValue("exp", prog.exp)
								.addValue("points", prog.points)
								.addRow();
					}
					try (PreparedStatement ps = sql.build(con)) {
						ps.executeBatch();
					}
				} catch (SQLException ex) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to save class progression for " + uuidStr);
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	public void saveAchievementsAsync() {
		final String uuidStr = uuid.toString();
		final List<Object[]> rows = new ArrayList<>();
		for (var entry : globalAchievements.entrySet()) {
			rows.add(new Object[]{entry.getKey(), entry.getValue().getProgress(), "GLOBAL", entry.getValue().getData()});
		}
		for (var classEntry : classAchievements.entrySet()) {
			for (var entry : classEntry.getValue().entrySet()) {
				rows.add(new Object[]{entry.getKey(), entry.getValue().getProgress(), classEntry.getKey().name(), entry.getValue().getData()});
			}
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = NeoCore.getConnection("NeoRogue-PlayerData")) {
					try (PreparedStatement clear = con.prepareStatement(
							"DELETE FROM neorogue_achievements WHERE uuid = ?;")) {
						clear.setString(1, uuidStr);
						clear.executeUpdate();
					}
					if (!rows.isEmpty()) {
						SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_achievements");
						for (Object[] row : rows) {
							sql.addValue("uuid", uuidStr)
									.addValue("achievement", (String) row[0])
									.addValue("progress", (int) row[1])
									.addValue("scope", (String) row[2])
									.addValue("data", (String) row[3])
									.addRow();
						}
						try (PreparedStatement ps = sql.build(con)) {
							ps.executeBatch();
						}
					}
				} catch (SQLException ex) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to save achievements for " + uuidStr);
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	private void saveUnlockNodesDebounced() {
		if (unlockNodesSaveTask != null) {
			unlockNodesSaveTask.cancel();
		}
		final String uuidStr = uuid.toString();
		final HashSet<String> snapshot = new HashSet<>(unlockNodes);
		unlockNodesSaveTask = new BukkitRunnable() {
			@Override
			public void run() {
				unlockNodesSaveTask = null;
				try (Connection con = NeoCore.getConnection("NeoRogue-PlayerData")) {
					try (PreparedStatement clear = con.prepareStatement("DELETE FROM neorogue_unlocknodes WHERE uuid = ?;")) {
						clear.setString(1, uuidStr);
						clear.executeUpdate();
					}
					if (!snapshot.isEmpty()) {
						SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_unlocknodes");
						for (String nodeId : snapshot) {
							sql.addValue("uuid", uuidStr).addValue("node", nodeId).addRow();
						}
						try (PreparedStatement ps = sql.build(con)) {
							ps.executeBatch();
						}
					}
				} catch (SQLException ex) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to save unlock nodes for " + uuidStr);
					ex.printStackTrace();
				}
			}
		}.runTaskLaterAsynchronously(NeoRogue.inst(), 20L);
	}

	private void saveFlagsDebounced() {
		if (flagsSaveTask != null) {
			flagsSaveTask.cancel();
		}
		final String uuidStr = uuid.toString();
		final HashSet<String> snapshot = new HashSet<>(flags);
		flagsSaveTask = new BukkitRunnable() {
			@Override
			public void run() {
				flagsSaveTask = null;
				try (Connection con = NeoCore.getConnection("NeoRogue-PlayerData")) {
					try (PreparedStatement clear = con.prepareStatement("DELETE FROM neorogue_playerflags WHERE uuid = ?;")) {
						clear.setString(1, uuidStr);
						clear.executeUpdate();
					}
					if (!snapshot.isEmpty()) {
						SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_playerflags");
						for (String flag : snapshot) {
							sql.addValue("uuid", uuidStr).addValue("flag", flag).addRow();
						}
						try (PreparedStatement ps = sql.build(con)) {
							ps.executeBatch();
						}
					}
				} catch (SQLException ex) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to save flags for " + uuidStr);
					ex.printStackTrace();
				}
			}
		}.runTaskLaterAsynchronously(NeoRogue.inst(), 20L);
	}

	public void save(Connection con, List<PreparedStatement> stmts) throws Exception {
		// Only saves player data and ascension tree, session saving is handled elsewhere
		// neorogue_playerdata is saved in real-time via saveDisplayAsync()
		// neorogue_unlocknodes and neorogue_playerflags are saved in real-time via debounced saves

		// Save class progression
		PreparedStatement clearClass = con.prepareStatement("DELETE FROM neorogue_playerclass WHERE uuid = ?;");
		clearClass.setString(1, uuid.toString());
		stmts.add(clearClass);

		SQLInsertBuilder classSql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_playerclass");
		for (var entry : progression.entrySet()) {
			String classKey = entry.getKey() == null ? "GLOBAL" : entry.getKey().name();
			ClassProgression prog = entry.getValue();
			classSql.addValue("uuid", uuid.toString())
					.addValue("class", classKey)
					.addValue("level", prog.level)
					.addValue("exp", prog.exp)
					.addValue("points", prog.points)
					.addRow();
		}
		stmts.add(classSql.build(con));

		PreparedStatement clearAch = con.prepareStatement("DELETE FROM neorogue_achievements WHERE uuid = ?;");
		clearAch.setString(1, uuid.toString());
		stmts.add(clearAch);

		SQLInsertBuilder achSql = null;
		if (!globalAchievements.isEmpty()) {
			achSql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_achievements");
			for (var entry : globalAchievements.entrySet()) {
				achSql.addValue("uuid", uuid.toString())
						.addValue("achievement", entry.getKey())
						.addValue("progress", entry.getValue().getProgress())
						.addValue("scope", "GLOBAL")
						.addValue("data", entry.getValue().getData())
						.addRow();
			}
		}
		for (var classEntry : classAchievements.entrySet()) {
			if (achSql == null) achSql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_achievements");
			for (var entry : classEntry.getValue().entrySet()) {
				achSql.addValue("uuid", uuid.toString())
						.addValue("achievement", entry.getKey())
						.addValue("progress", entry.getValue().getProgress())
						.addValue("scope", classEntry.getKey().name())
						.addValue("data", entry.getValue().getData())
						.addRow();
			}
		}
		if (achSql != null) {
			stmts.add(achSql.build(con));
		}
	}
	
	// Should only ever be displayed to the owner
	public void displayLoadButtons(CommandSender s) {
		Util.msgRaw(s, Component.text("Save slots (Click one to load):", NamedTextColor.GRAY));
		for (int i = 1; i <= slotsAvailable; i++) {
			if (snapshots.containsKey(i)) {
				snapshots.get(i).displayLoadButton(s, i);
			}
			else {
				Util.msgRaw(s, NeoCore.miniMessage().deserialize("<gray><bold>[" + i + "] </bold>Empty"));
			}
		}
	}
	
	// Should only ever be displayed to the owner
	public void displayNewButtons(CommandSender s) {
		Util.msgRaw(s, Component.text("Save slots (Click one to start a new game with):", NamedTextColor.GRAY));
		for (int i = 1; i <= slotsAvailable; i++) {
			if (snapshots.containsKey(i)) {
				snapshots.get(i).displayNewButton(s, i);
			}
			else {
				SessionSnapshot.displayEmptyNewButton(s, i);
			}
		}
	}
	
	public SessionSnapshot getSnapshot(int saveSlot) {
		return snapshots.get(saveSlot);
	}

	public boolean hasSlot(int saveSlot) {
		return saveSlot >= 1 && saveSlot <= slotsAvailable;
	}
	
	public Set<String> getUnlockNodes() {
		return Collections.unmodifiableSet(unlockNodes);
	}

	public boolean hasUnlockNode(String id) {
		return unlockNodes.contains(UnlockRegistry.normalizeNodeId(id));
	}

	public boolean grant(String id) {
		String nodeId = UnlockRegistry.normalizeNodeId(id);
		if (!UnlockRegistry.hasNode(nodeId)) {
			return false;
		}
		boolean added = unlockNodes.add(nodeId);
		if (added) {
			Bukkit.getLogger().fine("[NeoRogue] Unlock node granted: " + nodeId + " -> " + display);
			markEquipmentDroptableDirty();
			initializeEquipmentDroptable();
			saveUnlockNodesDebounced();
		}
		return added;
	}

	public boolean revoke(String id) {
		String nodeId = UnlockRegistry.normalizeNodeId(id);
		boolean removed = unlockNodes.remove(nodeId);
		if (removed) {
			Bukkit.getLogger().fine("[NeoRogue] Unlock node revoked: " + nodeId + " -> " + display);
			markEquipmentDroptableDirty();
			initializeEquipmentDroptable();
			saveUnlockNodesDebounced();
		}
		return removed;
	}

	public boolean hasFlag(String flag) {
		return flags.contains(flag);
	}

	public void addFlag(String flag) {
		flags.add(flag);
		saveFlagsDebounced();
	}

	public void removeFlag(String flag) {
		flags.remove(flag);
		saveFlagsDebounced();
	}

	public void resetAll() {
		unlockNodes.clear();
		flags.clear();
		globalAchievements.clear();
		classAchievements.clear();
		progression.clear();
		initializeDefaultProgression();
		markEquipmentDroptableDirty();
		initializeEquipmentDroptable();
	}

	public AchievementProgress getAchievementProgress(String id) {
		return getGlobalAchievementProgress(id);
	}

	public AchievementProgress getGlobalAchievementProgress(String id) {
		return globalAchievements.computeIfAbsent(id, k -> {
			Achievement ach = AchievementManager.get(k);
			return ach != null ? new AchievementProgress(ach, 0, null) : null;
		});
	}

	public AchievementProgress getClassAchievementProgress(String id, EquipmentClass ec) {
		return classAchievements.computeIfAbsent(ec, k -> new HashMap<>()).computeIfAbsent(id, k -> {
			Achievement ach = AchievementManager.get(k);
			return ach != null ? new AchievementProgress(ach, 0, ec) : null;
		});
	}
	

	
	public ArrayList<String> getBoardLines() {
		Session s = SessionManager.getSession(p);
		if (s == null) return null;
		if (s.getInstance() instanceof FightInstance) {
			if (s.isSpectator(p.getUniqueId())) {
				return ((FightInstance) s.getInstance()).getSpectatorLines();
			}
			PlayerFightData pfd = FightInstance.getUserData(p.getUniqueId());
			if (pfd == null) return null; // Will happen for a second as fight loads
			return pfd.getBoardLines();
		}
		else if (s.getInstance() instanceof NodeSelectInstance) {
			if (s.isSpectator(p.getUniqueId()))
				return s.getSpectatorLines();
			PlayerSessionData psd = s.getParty().get(p.getUniqueId());
			if (psd == null)
				return null;
			return psd.getBoardLines();
		}
		else {
			if (s.isSpectator(p.getUniqueId()))
				return s.getInstance().getSpectatorLines();
			return s.getInstance().getPlayerLines();
		}
	}
}
