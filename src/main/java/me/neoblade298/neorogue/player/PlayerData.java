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
	
	// Create new one if one doesn't exist
	public PlayerData(Player p) {
		this.uuid = p.getUniqueId();
		this.p = p;
		this.display = p.getName();
		
		progression.put(null, new ClassProgression());
		progression.put(EquipmentClass.WARRIOR, new ClassProgression());
		progression.put(EquipmentClass.THIEF, new ClassProgression());
		progression.put(EquipmentClass.MAGE, new ClassProgression());
		progression.put(EquipmentClass.ARCHER, new ClassProgression());
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
				if (!base.next()) return;
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
						prog.level = classRs.getInt("level");
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
						Achievement ach = AchievementManager.get(achId);
						if (ach == null) continue;
						if (scope == null || scope.equals("GLOBAL")) {
							globalAchievements.put(achId, new AchievementProgress(ach, prog, null));
						} else {
							try {
								EquipmentClass ec = EquipmentClass.valueOf(scope);
								classAchievements.computeIfAbsent(ec, k -> new HashMap<>())
										.put(achId, new AchievementProgress(ach, prog, ec));
							} catch (IllegalArgumentException ex) {
								// Unknown scope, treat as global
								globalAchievements.put(achId, new AchievementProgress(ach, prog, null));
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
	}
	
	public Player getPlayer() {
		if (p == null) {
			p = Bukkit.getPlayer(uuid);
		}
		return p;
	}
	
	public void updatePlayer() {
		p = Bukkit.getPlayer(uuid);
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
		return progression.get(null).level;
	}
	
	public int getExp() {
		return progression.get(null).exp;
	}
	
	public int getSlots() {
		return slotsAvailable;
	}

	public int getLevel(EquipmentClass ec) {
		ClassProgression prog = progression.get(ec);
		return prog != null ? prog.level : 1;
	}

	public int getExp(EquipmentClass ec) {
		ClassProgression prog = progression.get(ec);
		return prog != null ? prog.exp : 0;
	}

	public int getPoints(EquipmentClass ec) {
		ClassProgression prog = progression.get(ec);
		return prog != null ? prog.points : 0;
	}

	public void setLevel(EquipmentClass ec, int value) {
		ClassProgression prog = progression.get(ec);
		if (prog != null) prog.level = value;
	}

	public void setExp(EquipmentClass ec, int value) {
		ClassProgression prog = progression.get(ec);
		if (prog != null) prog.exp = value;
	}

	public void setPoints(EquipmentClass ec, int value) {
		ClassProgression prog = progression.get(ec);
		if (prog != null) prog.points = value;
	}

	public void addPoints(EquipmentClass ec, int amount) {
		ClassProgression prog = progression.get(ec);
		if (prog != null) prog.points += amount;
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
		addPoints(ec, 1);
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
	
	public void save(Connection con, List<PreparedStatement> stmts) throws Exception {
		// Only saves player data and ascension tree, session saving is handled elsewhere
		SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_playerdata")
				.addValue("uuid", uuid.toString())
				.addValue("display", display);
		stmts.add(sql.build(con));

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

		PreparedStatement clearUnlocks = con.prepareStatement("DELETE FROM neorogue_unlocknodes WHERE uuid = ?;");
		clearUnlocks.setString(1, uuid.toString());
		stmts.add(clearUnlocks);

		if (!unlockNodes.isEmpty()) {
			SQLInsertBuilder unlockSql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_unlocknodes");
			for (String nodeId : unlockNodes) {
				unlockSql.addValue("uuid", uuid.toString())
						.addValue("node", nodeId)
						.addRow();
			}
			stmts.add(unlockSql.build(con));
		}

		PreparedStatement clearFlags = con.prepareStatement("DELETE FROM neorogue_playerflags WHERE uuid = ?;");
		clearFlags.setString(1, uuid.toString());
		stmts.add(clearFlags);

		if (!flags.isEmpty()) {
			SQLInsertBuilder flagsSql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_playerflags");
			for (String flag : flags) {
				flagsSql.addValue("uuid", uuid.toString())
						.addValue("flag", flag)
						.addRow();
			}
			stmts.add(flagsSql.build(con));
		}

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
		}
		return removed;
	}

	public boolean hasFlag(String flag) {
		return flags.contains(flag);
	}

	public void addFlag(String flag) {
		flags.add(flag);
	}

	public void removeFlag(String flag) {
		flags.remove(flag);
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
