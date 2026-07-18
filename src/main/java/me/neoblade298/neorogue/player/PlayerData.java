package me.neoblade298.neorogue.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder.SQLAction;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.DropTableSet;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.boost.BoostDurationType;
import me.neoblade298.neorogue.player.boost.ExpBoost;
import me.neoblade298.neorogue.player.boost.ExpBoostType;
import me.neoblade298.neorogue.player.caravan.CaravanUpgrade;
import me.neoblade298.neorogue.player.caravan.CaravanUpgradeRegistry;
import me.neoblade298.neorogue.player.caravan.SellablePackageRegistry;
import me.neoblade298.neorogue.player.unlock.UnlockRegistry;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.instances.NodeSelectInstance;
import me.neoblade298.neorogue.session.reward.RunReward;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class PlayerData {
	private static class ClassProgression {
		int level = 1;
		int exp = 0;
		int points = 0;
		int notorietyMax = 0;
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
	private ArrayList<ExpBoost> expBoosts = new ArrayList<ExpBoost>();
	private DropTableSet<Equipment> equipmentDroptable;
	private DropTableSet<Artifact> artifactDroptable;
	private DropTableSet<Consumable> consumableDroptable;
	private boolean equipmentDroptableDirty;
	private HashMap<String, AchievementProgress> globalAchievements = new HashMap<>();
	private EnumMap<EquipmentClass, HashMap<String, AchievementProgress>> classAchievements = new EnumMap<>(EquipmentClass.class);
	// Append-only finished-run history backing the winrate/winstreak stats (see RunStats).
	private ArrayList<RunStats.RunRecord> runResults = new ArrayList<RunStats.RunRecord>();
	private int slotsAvailable;
	public static final int NOTORIETY_HARD_CAP = 10;
	public static final int DEFAULT_CARGO_CAPACITY = 3000, DEFAULT_CARGO_SLOTS = 5;
	public static final int DEFAULT_FLEET_CAPACITY = 3000, DEFAULT_FLEET_SLOTS = 5;
	// Flags gating the caravan/cargo system, set by caravan upgrades.
	public static final String FLAG_CARGO_ACCESS = "cargo_access", FLAG_CARGO_INSURANCE = "cargo_insurance";
	// Namespaced flag prefixes: owned sellable packages and purchased caravan upgrades are stored as
	// player flags (e.g. "caravan_pkg:ores", "caravan_upgrade:cargo_access").
	public static final String FLAG_PREFIX_PACKAGE = "caravan_pkg:", FLAG_PREFIX_UPGRADE = "caravan_upgrade:";
	private Cargo cargo = new Cargo(DEFAULT_CARGO_CAPACITY, DEFAULT_CARGO_SLOTS);
	// Overflow stash for unsold cargo returned at run end that didn't fit in the main cargo.
	// Shares its capacity/slot limits with the main cargo; withdraw-only in the GUI.
	private Cargo lostCargo = new Cargo(DEFAULT_CARGO_CAPACITY, DEFAULT_CARGO_SLOTS);
	// Persistent caravan scalars, stored on the neorogue_playerdata row.
	private int cargoBaseReward = 0;          // currency awarded per completed region
	private int sellMultiplierBonus = 0;      // % bonus to cargo sell value (effective mult = 1 + bonus/100)
	// Fleet: extra cargo holds beyond the main cargo. fleetSize is the number of extra holds (0 = none);
	// each fleet hold is bounded by fleetCapacity/fleetSlots and auto-sells at midnight America/Los_Angeles.
	private int fleetSize = 0;
	private int fleetCapacity = DEFAULT_FLEET_CAPACITY;
	private int fleetSlots = DEFAULT_FLEET_SLOTS;
	private final ArrayList<FleetHold> fleetHolds = new ArrayList<FleetHold>();
	// Proceeds from auto-sold fleet holds awaiting collection, keyed by material (amount + total value).
	private final LinkedHashMap<Material, PendingFleetSale> pendingFleetSales = new LinkedHashMap<Material, PendingFleetSale>();
	private BukkitTask unlockNodesSaveTask;
	private BukkitTask flagsSaveTask;
	
	// Create new one if one doesn't exist
	public PlayerData(Player p) {
		this.uuid = p.getUniqueId();
		this.p = p;
		this.display = p.getName();

		initializeDefaultProgression();
		if (p.hasPermission("neorogue.admin")) {
			slotsAvailable = 9;
		}
		else if (p.hasPermission("neorogue.slots.5")) {
			slotsAvailable = 5;
		}
		else if (p.hasPermission("neorogue.slots.3")) {
			slotsAvailable = 3;
		}
		else if (p.hasPermission("neorogue.slots.2")) {
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
					savePlayerDataAsync();
					return;
				}
				// Caravan scalars (cargo/fleet limits, base reward, sell multiplier) are no longer stored;
				// they are derived from the player's purchased upgrades in recomputeCaravanState() below.
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
						prog.notorietyMax = classRs.getInt("notoriety_max");
					}
				}
			}

			// Load finished-run history for winrate/winstreak stats.
			try (PreparedStatement runStmt = con.prepareStatement(
					"SELECT ts, playerClass, notoriety, won FROM neorogue_run_results WHERE uuid = ?;")) {
				runStmt.setString(1, uuidStr);
				try (ResultSet runRs = runStmt.executeQuery()) {
					while (runRs.next()) {
						EquipmentClass ec;
						try {
							ec = EquipmentClass.valueOf(runRs.getString("playerClass"));
						} catch (IllegalArgumentException ex) {
							continue;
						}
						runResults.add(new RunStats.RunRecord(runRs.getLong("ts"), ec,
								runRs.getInt("notoriety"), runRs.getInt("won") == 1));
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
			// Derive all caravan effects (cargo/fleet limits, grants, packages) from the purchased upgrades
			// so config changes to caravan.yml apply retroactively. Must run before fleet cargo is loaded so
			// the holds exist.
			recomputeCaravanState();

			// Load exp boosts from separate table
			try (PreparedStatement boostStmt = con.prepareStatement("SELECT * FROM neorogue_expboosts WHERE uuid = ?;")) {
				boostStmt.setString(1, uuidStr);
				try (ResultSet boostRs = boostStmt.executeQuery()) {
					while (boostRs.next()) {
						try {
							ExpBoostType type = ExpBoostType.valueOf(boostRs.getString("type"));
							ExpBoost boost = new ExpBoost(type, boostRs.getLong("remaining"));
							if (!boost.isExpired()) expBoosts.add(boost);
						} catch (IllegalArgumentException ex) {
							// Unknown boost type, skip
						}
					}
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

			// Load cargo contents from the unified neorogue_playercargo table (MAIN/LOST/FLEET/PENDING).
			// Limits live on the playerdata row (read above); fleet holds carry a per-material price
			// snapshot and a fill timestamp used for the midnight auto-sale.
			try (PreparedStatement cargoStmt = con.prepareStatement("SELECT * FROM neorogue_playercargo WHERE uuid = ?;")) {
				cargoStmt.setString(1, uuidStr);
				try (ResultSet cargoRs = cargoStmt.executeQuery()) {
					while (cargoRs.next()) {
						Material mat = Material.getMaterial(cargoRs.getString("material"));
						if (mat == null) continue;
						int amount = cargoRs.getInt("amount");
						String type = cargoRs.getString("type");
						if (type == null) type = "MAIN";
						switch (type) {
						case "LOST":
							lostCargo.load(mat, amount);
							break;
						case "FLEET": {
							FleetHold hold = getFleetHold(cargoRs.getInt("idx"));
							if (hold != null) hold.load(mat, amount, cargoRs.getDouble("price"), cargoRs.getLong("filled_at"));
							else addPendingSale(mat, amount, cargoRs.getDouble("price") * amount); // hold no longer exists (fleet size reduced); bank the value
							break;
						}
						case "PENDING": {
							double value = cargoRs.getDouble("price") * amount;
							PendingFleetSale ps = pendingFleetSales.get(mat);
							if (ps == null) pendingFleetSales.put(mat, new PendingFleetSale(mat, amount, value));
							else { ps.amount += amount; ps.value += value; }
							break;
						}
						default:
							cargo.load(mat, amount);
							break;
						}
					}
				}
			}
			// Resolve any fleet holds that should have auto-sold while the player was offline.
			resolveFleetSales();
			// Owned sellable packages and purchased caravan upgrades are loaded as flags above.
		for (String defaultNode : UnlockRegistry.getDefaultNodes()) {
			unlockNodes.add(defaultNode);
		}

			markEquipmentDroptableDirty();
			initializeEquipmentDroptable();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		savePlayerDataAsync();
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
			savePlayerDataAsync();
		}
	}

	public void initializeEquipmentDroptable() {
		equipmentDroptable = UnlockRegistry.buildEquipmentDroptable(this);
		artifactDroptable = UnlockRegistry.buildArtifactDroptable(this);
		consumableDroptable = UnlockRegistry.buildConsumableDroptable(this);
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

	public DropTableSet<Artifact> getArtifactDroptable() {
		if (artifactDroptable == null || equipmentDroptableDirty) {
			initializeEquipmentDroptable();
		}
		return artifactDroptable;
	}

	public DropTableSet<Consumable> getConsumableDroptable() {
		if (consumableDroptable == null || equipmentDroptableDirty) {
			initializeEquipmentDroptable();
		}
		return consumableDroptable;
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
		if (currentLevel >= 50) return 25000;
		if (currentLevel < 5) return 200 * currentLevel * currentLevel;
		return 2000 * currentLevel - 5000;
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

	// ---- Exp boosts ----

	public List<ExpBoost> getExpBoosts() {
		return expBoosts;
	}

	// Grants a new boost. RUNS types stack their run count if one of the same type
	// already exists; TIME types keep whichever expiry is later.
	public void addExpBoost(ExpBoostType type, long durationInput) {
		ExpBoost existing = null;
		for (ExpBoost b : expBoosts) {
			if (b.getType() == type) {
				existing = b;
				break;
			}
		}
		if (existing == null) {
			expBoosts.add(ExpBoost.create(type, durationInput));
		}
		else if (type.getDurationType() == BoostDurationType.RUNS) {
			existing.setRemaining(existing.getRemaining() + durationInput);
		}
		else {
			existing.setRemaining(Math.max(existing.getRemaining(), System.currentTimeMillis() + durationInput * 1000L));
		}
		saveExpBoostsAsync();
	}

	// Removes any boosts that are no longer active.
	private void pruneExpBoosts() {
		expBoosts.removeIf(ExpBoost::isExpired);
	}

	// Returns the combined exp multiplier from all currently-active boosts, e.g. 1.30
	// for a single +30% boost. Additive stacking across boosts.
	public double getExpBoostMultiplier() {
		pruneExpBoosts();
		double bonus = 0.0;
		for (ExpBoost b : expBoosts) {
			bonus += b.getMultiplier();
		}
		return 1.0 + bonus;
	}

	// Called when a run starts. Captures the current combined multiplier, decrements
	// RUNS boosts by one, prunes expired boosts, and returns the captured multiplier
	// to lock in for that run.
	public double consumeRunExpBoosts() {
		double multiplier = getExpBoostMultiplier();
		boolean changed = false;
		for (ExpBoost b : expBoosts) {
			if (b.getType().getDurationType() == BoostDurationType.RUNS) {
				b.tickRun();
				changed = true;
			}
		}
		pruneExpBoosts();
		if (changed) saveExpBoostsAsync();
		return multiplier;
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
		return progression.entrySet().stream()
				.filter(e -> e.getKey() != null)
				.mapToInt(e -> e.getValue().notorietyMax)
				.sum();
	}

	public int getMaxNotoriety(EquipmentClass ec) {
		if (ec == null) return 0;
		ClassProgression prog = progression.get(ec);
		return prog == null ? 0 : Math.min(prog.notorietyMax, NOTORIETY_HARD_CAP);
	}

	public void increaseNotorietyMax(EquipmentClass ec) {
		if (ec == null) return;
		getOrCreateProgression(ec).notorietyMax++;
		saveClassProgressionAsync();
	}

	public void setNotorietyMax(EquipmentClass ec, int wins) {
		if (ec == null) return;
		getOrCreateProgression(ec).notorietyMax = Math.max(0, wins);
		saveClassProgressionAsync();
	}

	private void savePlayerDataAsync() {
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
					Bukkit.getLogger().warning("[NeoRogue] Failed to save player data for " + uuidStr);
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	public Cargo getCargo() {
		return cargo;
	}

	public Cargo getLostCargo() {
		return lostCargo;
	}

	// Caravan effects are derived from purchased upgrades (see recomputeCaravanState), so the scalar
	// setters/adders below only mutate in-memory state; nothing is persisted here.
	public int getCargoCapacity() {
		return cargo.getCapacity();
	}

	public void setCargoCapacity(int capacity) {
		cargo.setCapacity(capacity);
		lostCargo.setCapacity(capacity);
	}

	public void addCargoCapacity(int amount) {
		cargo.addCapacity(amount);
		lostCargo.addCapacity(amount);
	}

	public int getCargoSlots() {
		return cargo.getSlots();
	}

	public void setCargoSlots(int slots) {
		cargo.setSlots(slots);
		lostCargo.setSlots(slots);
	}

	public void addCargoSlots(int amount) {
		cargo.addSlots(amount);
		lostCargo.addSlots(amount);
	}

	// Recomputes every caravan effect from the player's purchased upgrades, resetting to defaults first
	// so that changes to caravan.yml (e.g. lowering a capacity bonus) apply retroactively. Called on
	// login before the fleet cargo is loaded. Cargo limits, base reward, sell multiplier, fleet
	// configuration, and the access/insurance/package grant flags are all derived here.
	public void recomputeCaravanState() {
		cargo.setCapacity(DEFAULT_CARGO_CAPACITY);
		cargo.setSlots(DEFAULT_CARGO_SLOTS);
		lostCargo.setCapacity(DEFAULT_CARGO_CAPACITY);
		lostCargo.setSlots(DEFAULT_CARGO_SLOTS);
		cargoBaseReward = 0;
		sellMultiplierBonus = 0;
		fleetSize = 0;
		fleetCapacity = DEFAULT_FLEET_CAPACITY;
		fleetSlots = DEFAULT_FLEET_SLOTS;
		// Boolean/package grants are re-derived from the purchased upgrades below.
		flags.remove(FLAG_CARGO_ACCESS);
		flags.remove(FLAG_CARGO_INSURANCE);
		flags.removeIf(f -> f.startsWith(FLAG_PREFIX_PACKAGE));
		for (String upgradeId : getPurchasedUpgrades()) {
			CaravanUpgrade up = CaravanUpgradeRegistry.get(upgradeId);
			if (up != null) up.applyActions(this);
		}
		syncFleetHolds();
	}

	// ----- Fleet holds -----
	// A pending sale of one material from an auto-sold fleet hold, awaiting collection by the player.
	public static class PendingFleetSale {
		public final Material material;
		public int amount;
		public double value;

		public PendingFleetSale(Material material, int amount, double value) {
			this.material = material;
			this.amount = amount;
			this.value = value;
		}
	}

	public int getFleetSize() {
		return fleetSize;
	}

	public int getFleetCapacity() {
		return fleetCapacity;
	}

	public int getFleetSlots() {
		return fleetSlots;
	}

	// Returns the fleet hold at the given 1-based index, or null if out of range.
	public FleetHold getFleetHold(int index) {
		if (index < 1 || index > fleetHolds.size()) return null;
		return fleetHolds.get(index - 1);
	}

	public List<FleetHold> getFleetHolds() {
		return fleetHolds;
	}

	// Reconciles the fleetHolds list with fleetSize. Growing adds empty holds; shrinking dumps the
	// removed holds' contents into pending earnings so items are never lost.
	private void syncFleetHolds() {
		while (fleetHolds.size() < fleetSize) fleetHolds.add(new FleetHold(fleetCapacity, fleetSlots));
		while (fleetHolds.size() > fleetSize) {
			FleetHold removed = fleetHolds.remove(fleetHolds.size() - 1);
			for (Map.Entry<Material, Integer> ent : removed.getCargo().getItems().entrySet()) {
				addPendingSale(ent.getKey(), ent.getValue(), removed.getUnitPrice(ent.getKey()) * ent.getValue());
			}
		}
	}

	public void addFleetSize(int amount) {
		if (amount == 0) return;
		fleetSize = Math.max(0, fleetSize + amount);
		syncFleetHolds();
	}

	public void addFleetCapacity(int amount) {
		fleetCapacity += amount;
		for (FleetHold hold : fleetHolds) hold.setCapacity(fleetCapacity);
	}

	public void addFleetSlots(int amount) {
		fleetSlots += amount;
		for (FleetHold hold : fleetHolds) hold.setSlots(fleetSlots);
	}

	private void addPendingSale(Material mat, int amount, double value) {
		PendingFleetSale ps = pendingFleetSales.get(mat);
		if (ps == null) pendingFleetSales.put(mat, new PendingFleetSale(mat, amount, value));
		else { ps.amount += amount; ps.value += value; }
	}

	// Epoch millis of the most recent midnight in America/Los_Angeles.
	private static long lastLosAngelesMidnightMillis() {
		java.time.ZoneId la = java.time.ZoneId.of("America/Los_Angeles");
		return java.time.ZonedDateTime.now(la).toLocalDate().atStartOfDay(la).toInstant().toEpochMilli();
	}

	// Auto-sells any fleet hold that was filled before the most recent America/Los_Angeles midnight,
	// moving its snapshot value into pending earnings. Returns true if anything was sold.
	public boolean resolveFleetSales() {
		long cutoff = lastLosAngelesMidnightMillis();
		boolean changed = false;
		for (FleetHold hold : fleetHolds) {
			if (hold.isEmpty()) continue;
			long filledAt = hold.getFilledAt();
			if (filledAt <= 0 || filledAt >= cutoff) continue;
			for (Map.Entry<Material, Integer> ent : new java.util.ArrayList<Map.Entry<Material, Integer>>(hold.getCargo().getItems().entrySet())) {
				Material mat = ent.getKey();
				int amt = ent.getValue();
				addPendingSale(mat, amt, hold.getUnitPrice(mat) * amt);
			}
			hold.clear();
			changed = true;
		}
		if (changed) saveCargoAsync();
		return changed;
	}

	public boolean hasPendingFleetSales() {
		return !pendingFleetSales.isEmpty();
	}

	public Collection<PendingFleetSale> getPendingFleetSales() {
		return pendingFleetSales.values();
	}

	public double getPendingFleetEarnings() {
		double total = 0;
		for (PendingFleetSale ps : pendingFleetSales.values()) total += ps.value;
		return total;
	}

	// Deposits all pending fleet earnings to the player's economy balance and clears them. Returns the
	// amount collected (0 if there was nothing to collect).
	public double collectFleetEarnings() {
		double total = getPendingFleetEarnings();
		if (total <= 0) return 0;
		RunReward.deposit(uuid, total);
		pendingFleetSales.clear();
		saveCargoAsync();
		return total;
	}

	// Persists all cargo (main, lost, fleet holds, and pending fleet sales) into the unified table.
	public void saveCargoAsync() {
		final String uuidStr = uuid.toString();
		final HashMap<Material, Integer> mainSnap = new HashMap<>(cargo.getItems());
		final HashMap<Material, Integer> lostSnap = new HashMap<>(lostCargo.getItems());
		final ArrayList<Object[]> fleetRows = new ArrayList<Object[]>();
		for (int i = 0; i < fleetHolds.size(); i++) {
			FleetHold hold = fleetHolds.get(i);
			int idx = i + 1;
			long filledAt = hold.getFilledAt();
			for (Map.Entry<Material, Integer> ent : hold.getCargo().getItems().entrySet()) {
				fleetRows.add(new Object[] { idx, ent.getKey().name(), ent.getValue(), hold.getUnitPrice(ent.getKey()), filledAt });
			}
		}
		final ArrayList<Object[]> pendingRows = new ArrayList<Object[]>();
		for (PendingFleetSale ps : pendingFleetSales.values()) {
			double unit = ps.amount > 0 ? ps.value / ps.amount : 0;
			pendingRows.add(new Object[] { ps.material.name(), ps.amount, unit });
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = NeoCore.getConnection("NeoRogue-PlayerData")) {
					try (PreparedStatement clear = con.prepareStatement(
							"DELETE FROM neorogue_playercargo WHERE uuid = ?;")) {
						clear.setString(1, uuidStr);
						clear.executeUpdate();
					}
					SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_playercargo");
					boolean any = false;
					for (var entry : mainSnap.entrySet()) {
						sql.addValue("uuid", uuidStr).addValue("type", "MAIN").addValue("idx", 0)
								.addValue("material", entry.getKey().name()).addValue("amount", entry.getValue())
								.addValue("price", 0).addValue("filled_at", 0).addRow();
						any = true;
					}
					for (var entry : lostSnap.entrySet()) {
						sql.addValue("uuid", uuidStr).addValue("type", "LOST").addValue("idx", 0)
								.addValue("material", entry.getKey().name()).addValue("amount", entry.getValue())
								.addValue("price", 0).addValue("filled_at", 0).addRow();
						any = true;
					}
					for (Object[] row : fleetRows) {
						sql.addValue("uuid", uuidStr).addValue("type", "FLEET").addValue("idx", (int) row[0])
								.addValue("material", (String) row[1]).addValue("amount", (int) row[2])
								.addValue("price", (double) row[3]).addValue("filled_at", (long) row[4]).addRow();
						any = true;
					}
					for (Object[] row : pendingRows) {
						sql.addValue("uuid", uuidStr).addValue("type", "PENDING").addValue("idx", 0)
								.addValue("material", (String) row[0]).addValue("amount", (int) row[1])
								.addValue("price", (double) row[2]).addValue("filled_at", 0).addRow();
						any = true;
					}
					if (any) {
						try (PreparedStatement ps = sql.build(con)) {
							ps.executeBatch();
						}
					}
				} catch (SQLException ex) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to save cargo for " + uuidStr);
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	// The lost-cargo stash is persisted as part of the unified saveCargoAsync(); kept as an alias so
	// existing callers (run-end cargo return) continue to work.
	public void saveLostCargoAsync() {
		saveCargoAsync();
	}

	// ----- Caravan upgrade state -----
	// Purchased upgrades (namespaced flags) are the sole persisted caravan state. Every derived effect
	// (cargo/fleet limits, base reward, sell multiplier, access/insurance/package grants) is recomputed
	// from them on login via recomputeCaravanState(), so the setters below are in-memory only.

	public int getCargoBaseReward() {
		return cargoBaseReward;
	}

	public void addCargoBaseReward(int amount) {
		cargoBaseReward += amount;
	}

	public int getSellMultiplierBonus() {
		return sellMultiplierBonus;
	}

	// Effective multiplier applied to cargo sale value (1.0 = no bonus).
	public double getSellMultiplier() {
		return 1.0 + (sellMultiplierBonus / 100.0);
	}

	public void addSellMultiplier(int percent) {
		sellMultiplierBonus += percent;
	}

	public java.util.Set<String> getSellablePackages() {
		return flagsWithPrefix(FLAG_PREFIX_PACKAGE);
	}

	public boolean hasSellablePackage(String id) {
		return hasFlag(FLAG_PREFIX_PACKAGE + id);
	}

	public void addSellablePackage(String id) {
		addFlag(FLAG_PREFIX_PACKAGE + id);
	}

	// Whether this player is permitted to deposit the given material into cargo (default package plus
	// any owned sellable packages).
	public boolean canDepositMaterial(Material mat) {
		return SellablePackageRegistry.canDeposit(getSellablePackages(), mat);
	}

	public java.util.Set<String> getPurchasedUpgrades() {
		return flagsWithPrefix(FLAG_PREFIX_UPGRADE);
	}

	public boolean hasPurchasedUpgrade(String id) {
		return hasFlag(FLAG_PREFIX_UPGRADE + id);
	}

	public void addPurchasedUpgrade(String id) {
		addFlag(FLAG_PREFIX_UPGRADE + id);
	}

	// Collects flags starting with the given prefix and returns their suffixes (prefix stripped).
	private java.util.Set<String> flagsWithPrefix(String prefix) {
		java.util.HashSet<String> result = new java.util.HashSet<String>();
		for (String flag : flags) {
			if (flag.startsWith(prefix)) result.add(flag.substring(prefix.length()));
		}
		return result;
	}

	// A read-only view over this player's finished-run history for winrate/winstreak stats.
	public RunStats getRunStats() {
		return new RunStats(runResults);
	}

	// Records one finished run (win or lose) for this player and persists it. Callers should skip
	// runs that shouldn't count toward stats (e.g. tutorial or endless runs).
	public void addRunResult(EquipmentClass playerClass, int notoriety, boolean won) {
		if (playerClass == null) return;
		long ts = System.currentTimeMillis();
		runResults.add(new RunStats.RunRecord(ts, playerClass, notoriety, won));
		persistRunResultAsync(uuid.toString(), ts, playerClass, notoriety, won);
	}

	// Records a run result for a player by uuid whether or not their PlayerData is loaded. Used for
	// events that affect players who may be offline, e.g. deleting an in-progress run (a loss for
	// everyone who was in it). Loaded players also get the record reflected in memory immediately.
	public static void recordRunResult(UUID uuid, EquipmentClass playerClass, int notoriety, boolean won) {
		if (playerClass == null) return;
		PlayerData pd = PlayerManager.getPlayerData(uuid);
		if (pd != null) {
			pd.addRunResult(playerClass, notoriety, won);
		} else {
			persistRunResultAsync(uuid.toString(), System.currentTimeMillis(), playerClass, notoriety, won);
		}
	}

	private static void persistRunResultAsync(String uuidStr, long ts, EquipmentClass playerClass, int notoriety, boolean won) {
		final String classKey = playerClass.name();
		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = NeoCore.getConnection("NeoRogue-PlayerData")) {
					SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.INSERT, "neorogue_run_results");
					sql.addValue("uuid", uuidStr)
							.addValue("ts", ts)
							.addValue("playerClass", classKey)
							.addValue("notoriety", notoriety)
							.addValue("won", won ? 1 : 0)
							.addRow();
					try (PreparedStatement ps = sql.build(con)) {
						ps.executeBatch();
					}
				} catch (SQLException ex) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to save run result for " + uuidStr);
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
			copy.notorietyMax = orig.notorietyMax;
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
								.addValue("notoriety_max", prog.notorietyMax)
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

	private void saveExpBoostsAsync() {
		final String uuidStr = uuid.toString();
		final ArrayList<ExpBoost> snapshot = new ArrayList<>(expBoosts);
		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = NeoCore.getConnection("NeoRogue-PlayerData")) {
					try (PreparedStatement clear = con.prepareStatement(
							"DELETE FROM neorogue_expboosts WHERE uuid = ?;")) {
						clear.setString(1, uuidStr);
						clear.executeUpdate();
					}
					if (snapshot.isEmpty()) return;
					SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_expboosts");
					for (ExpBoost boost : snapshot) {
						sql.addValue("uuid", uuidStr)
								.addValue("type", boost.getType().name())
								.addValue("remaining", boost.getRemaining())
								.addRow();
					}
					try (PreparedStatement ps = sql.build(con)) {
						ps.executeBatch();
					}
				} catch (SQLException ex) {
					Bukkit.getLogger().warning("[NeoRogue] Failed to save exp boosts for " + uuidStr);
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
					.addValue("notoriety_max", prog.notorietyMax)
					.addRow();
		}
		stmts.add(classSql.build(con));

		// Save exp boosts
		PreparedStatement clearBoosts = con.prepareStatement("DELETE FROM neorogue_expboosts WHERE uuid = ?;");
		clearBoosts.setString(1, uuid.toString());
		stmts.add(clearBoosts);

		if (!expBoosts.isEmpty()) {
			SQLInsertBuilder boostSql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_expboosts");
			for (ExpBoost boost : expBoosts) {
				boostSql.addValue("uuid", uuid.toString())
						.addValue("type", boost.getType().name())
						.addValue("remaining", boost.getRemaining())
						.addRow();
			}
			stmts.add(boostSql.build(con));
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

		// Save cargo contents
		PreparedStatement clearCargo = con.prepareStatement("DELETE FROM neorogue_playercargo WHERE uuid = ?;");
		clearCargo.setString(1, uuid.toString());
		stmts.add(clearCargo);

		if (!cargo.getItems().isEmpty()) {
			SQLInsertBuilder cargoSql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_playercargo");
			for (var entry : cargo.getItems().entrySet()) {
				cargoSql.addValue("uuid", uuid.toString())
						.addValue("material", entry.getKey().name())
						.addValue("amount", entry.getValue())
						.addRow();
			}
			stmts.add(cargoSql.build(con));
		}

		// Save cargo limits
		SQLInsertBuilder cargoMetaSql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_playercargo_meta");
		cargoMetaSql.addValue("uuid", uuid.toString())
				.addValue("capacity", cargo.getCapacity())
				.addValue("slots", cargo.getSlots())
				.addRow();
		stmts.add(cargoMetaSql.build(con));
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
