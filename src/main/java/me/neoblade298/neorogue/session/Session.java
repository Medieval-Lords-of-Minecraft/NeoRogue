package me.neoblade298.neorogue.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;

import de.tr7zw.nbtapi.NBT;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.effects.Audience;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.io.SQLManager;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder.SQLAction;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.MapViewer;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.boost.GlobalBoostManager;
import me.neoblade298.neorogue.player.inventory.PlayerSessionSpectateInventory;
import me.neoblade298.neorogue.region.Node;
import me.neoblade298.neorogue.region.Region;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.instances.EditInventoryInstance;
import me.neoblade298.neorogue.session.instances.EndRunInstance;
import me.neoblade298.neorogue.session.instances.Instance;
import me.neoblade298.neorogue.session.instances.Instance.PlayerFlags;
import me.neoblade298.neorogue.session.instances.LoadLobbyInstance;
import me.neoblade298.neorogue.session.instances.LobbyInstance;
import me.neoblade298.neorogue.session.instances.LoseInstance;
import me.neoblade298.neorogue.session.instances.NewLobbyInstance;
import me.neoblade298.neorogue.session.settings.NotorietySetting;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;

public class Session {
	
	private Region region;
	private UUID host;
	private String name;
	private HashMap<UUID, PlayerSessionData> party = new HashMap<UUID, PlayerSessionData>();
	private HashMap<UUID, MapViewer> spectators = new HashMap<UUID, MapViewer>();
	private Instance inst;
	private Node curr;
	private int saveSlot, xOff, zOff, nodesVisited, regionsCompleted, potionChance = 25;
	private Plot plot;
	private SessionType sessionType;
	private boolean busy;
	private String lastMiniboss;
	private long nextSuggest = 0L;
	private ArrayList<String> spectatorLines = new ArrayList<String>();
	private double BASE_UPGRADE_CHANCE = 0.3;
	
	// Settings
    private boolean debug;
	private boolean endless;
	// New-lobby only: when true, join requests are auto-accepted instead of needing host approval
	private boolean lobbyOpen = false;
	// Ensures the "lobby is open" join broadcast only fires once per session
	private boolean announcedOpenLobby = false;
	public static double ENEMY_HEALTH_SCALE_PER_LEVEL = 0.03, ENEMY_DAMAGE_SCALE_PER_LEVEL = 0.03,
			COIN_REDUCTION_PER_LEVEL = 0.15, FIGHT_TIME_REDUCTION_PER_LEVEL = 0.05;
	private int notoriety;
	
	// Session coordinates
	public static final int NEW_LOBBY_X = 0, NEW_LOBBY_Z = 0, NEW_LOBBY_WIDTH = 15,
		LOAD_LOBBY_X = 17, LOAD_LOBBY_Z = 0, LOAD_LOBBY_WIDTH = 15,
		AREA_X = 0, AREA_Z = NEW_LOBBY_Z + NEW_LOBBY_WIDTH,
		AREA_WIDTH = 81, REWARDS_X = 0, REWARDS_Z = AREA_Z + AREA_WIDTH, REWARDS_WIDTH = 19, SHRINE_X = 0,
		SHRINE_Z = REWARDS_Z + REWARDS_WIDTH, SHRINE_WIDTH = 13, SHOP_X = 0, SHOP_Z = SHRINE_Z + SHRINE_WIDTH,
		SHOP_WIDTH = 12, CHANCE_X = 0, CHANCE_Z = SHOP_Z + SHOP_WIDTH, CHANCE_WIDTH = 12, LOSE_X = 0,
		LOSE_Z = CHANCE_Z + CHANCE_WIDTH;

	private static final ArrayList<Color> fireworkColors = new ArrayList<Color>();
	
	private static Clipboard newLobby, loadLobby, nodeSelect, rewardsRoom, shrine, shop, chance, lose;
	static {
		// Worldedit schematics
		newLobby = loadClipboard("NRNewLobby.schem");
		loadLobby = loadClipboard("NRLoadLobby.schem");
		nodeSelect = loadClipboard("NRNodeSelect.schem");
		rewardsRoom = loadClipboard("NRRewards.schem");
		shrine = loadClipboard("NRShrine.schem");
		shop = loadClipboard("NRShop.schem");
		chance = loadClipboard("NRChance.schem");
		lose = loadClipboard("NRLose.schem");

		// Firework colors
		fireworkColors.add(Color.RED);
		fireworkColors.add(Color.BLUE);
		fireworkColors.add(Color.GREEN);
		fireworkColors.add(Color.YELLOW);
		fireworkColors.add(Color.PURPLE);
		fireworkColors.add(Color.ORANGE);
	}
	
	private static Clipboard loadClipboard(String schematic) {
		File file = new File(NeoRogue.SCHEMATIC_FOLDER, schematic);
		ClipboardFormat format = ClipboardFormats.findByFile(file);
		try (FileInputStream fis = new FileInputStream(file);
			ClipboardReader reader = format.getReader(fis)) {
			return reader.read();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void pasteSchematic(
			Clipboard clipboard, EditSession editSession, Session session, int xOff, int yOff, int zOff
	) {
		ClipboardHolder holder = new ClipboardHolder(clipboard);
		Operation operation = holder.createPaste(editSession)
				.to(BlockVector3.at(-(session.getXOff() + xOff + 1), 64 + yOff, session.getZOff() + zOff))
				.ignoreAirBlocks(false).build();
		try {
			Operations.complete(operation);
		} catch (WorldEditException e) {
			e.printStackTrace();
		}
		holder.close();
	}

	private static void pasteSchematic(Clipboard clipboard, EditSession editSession, Session session, int zOff) {
		pasteSchematic(clipboard, editSession, session, 0, 0, zOff);
	}

	public Session(Player p, Plot plot, int saveSlot, boolean isNew, SessionType sessionType) {
		this.saveSlot = saveSlot;
		this.xOff = plot.getXOffset();
		this.zOff = plot.getZOffset();
		host = p.getUniqueId();
		name = p.getName() + "'s game";
		this.plot = plot;
		this.sessionType = sessionType;
		this.inst = isNew ? new NewLobbyInstance(p, this) : new LoadLobbyInstance(p, this);
		generateInterstitials();

		if (!isNew) {
			load(saveSlot, host, xOff, zOff, (LoadLobbyInstance) this.inst);
		}
	}
	
	// Load from existing data
	private void load(int saveSlot, UUID host, int xOff, int zOff, LoadLobbyInstance lobby) {
		Session s = this;
		busy = true;

		// Safety net: if load takes longer than 10 seconds, force-end the session
		new BukkitRunnable() {
			@Override
			public void run() {
				if (busy) {
					Bukkit.getLogger().warning("[NeoRogue] Session load timed out for host " + host + " slot " + saveSlot + ". Forcing end.");
					busy = false;
					SessionManager.endSession(s);
				}
			}
		}.runTaskLater(NeoRogue.inst(), 200L); // 10 seconds = 200 ticks

		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = SQLManager.getConnection("NeoRogue"); Statement stmt = con.createStatement()) {
					ResultSet partySet = stmt.executeQuery(
							"SELECT * FROM neorogue_playersessiondata WHERE host = '" + host + "' AND slot = "
									+ saveSlot + ";"
					);
					while (partySet.next()) {
						UUID uuid = UUID.fromString(partySet.getString("uuid"));
						party.put(uuid, new PlayerSessionData(uuid, s, partySet));
					}
					
					ResultSet sessSet = stmt.executeQuery(
							"SELECT * FROM neorogue_sessions WHERE host = '" + host + "' AND slot = " + saveSlot + ";"
					);
					if (!sessSet.next()) {
						throw new SQLException("No session data found for host " + host + " slot " + saveSlot);
					}
					sessionType = getSessionTypeFromRow(sessSet);
					nodesVisited = sessSet.getInt("nodesVisited");
					int pos = sessSet.getInt("position");
					int lane = sessSet.getInt("lane");
					potionChance = sessSet.getInt("potionChance");
					regionsCompleted = sessSet.getInt("regionsCompleted");

					lastMiniboss = sessSet.getString("lastMiniboss");

					// settings
					endless = sessSet.getBoolean("endless");
					notoriety = sessSet.getInt("notoriety");

					// Read instanceData before Region construction closes the ResultSet
					String instanceData = sessSet.getString("instanceData");
					RegionType regionType = RegionType.valueOf(sessSet.getString("regionType"));

					region = new Region(
							regionType, xOff, zOff, host, saveSlot, s, stmt
					);
					curr = region.getNodes()[pos][lane];

					Instance inst = Instance.deserialize(s, instanceData, party);

					// Complete load on main thread for thread safety
					Bukkit.getScheduler().runTask(NeoRogue.inst(), () -> {
						busy = false;
						lobby.completeLoad(inst);
					});
				} catch (Exception e) {
					e.printStackTrace();
					Bukkit.getLogger().warning("[NeoRogue] Failed to load session for host " + host + " slot " + saveSlot);
					busy = false;
					// End session on main thread
					Bukkit.getScheduler().runTask(NeoRogue.inst(), () -> {
						Player p = Bukkit.getPlayer(host);
						if (p != null) {
							Util.displayError(p, "Failed to load your saved game. The session has been ended.");
						}
						SessionManager.endSession(s);
					});
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}

	private SessionType getSessionTypeFromRow(ResultSet sessSet) {
		try {
			return SessionType.fromStorage(sessSet.getString("sessionType"));
		} catch (SQLException ex) {
			return SessionType.STANDARD;
		}
	}
	
	public ArrayList<String> getSpectatorLines() {
		return spectatorLines;
	}

	public MapViewer getSpectator(UUID uuid) {
		return spectators.get(uuid);
	}
	
	public void updateSpectatorLines() {
		spectatorLines = new ArrayList<String>(9);
		for (PlayerSessionData psd : party.values()) {
			spectatorLines.add("§e" + psd.getData().getDisplay() + "§7: §f" + Math.round(psd.getHealth()) + "§c♥");
		}
	}
	
	private void generateInterstitials() {
	Location loc = new Location(Bukkit.getWorld(Region.WORLD_NAME), -(xOff + 1), 62, zOff);
		Material versionCheck = Material.RED_WOOL; // Change this when interstitials change to regen them
		
		if (loc.getBlock().getType() != versionCheck) {
			Bukkit.getLogger().info("[NeoRogue] Generating interstitials for host " + Bukkit.getPlayer(host).getName());
			loc.getBlock().setType(versionCheck);
			// Generate the lobby and add the host there
			try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(Region.world).build()) {
				pasteSchematic(newLobby, editSession, this, Session.NEW_LOBBY_Z);
				pasteSchematic(loadLobby, editSession, this, LOAD_LOBBY_X, 0, Session.LOAD_LOBBY_Z);
				pasteSchematic(nodeSelect, editSession, this, Session.AREA_Z);
				pasteSchematic(rewardsRoom, editSession, this, Session.REWARDS_Z);
				pasteSchematic(shrine, editSession, this, Session.SHRINE_Z);
				pasteSchematic(shop, editSession, this, Session.SHOP_Z);
				pasteSchematic(chance, editSession, this, Session.CHANCE_Z);
				pasteSchematic(lose, editSession, this, 0, -1, Session.LOSE_Z);
			}
		}
		else {
			Bukkit.getLogger().info("[NeoRogue] Interstitials for host " + Bukkit.getPlayer(host).getName() + " are up to date");
		}
	}
	
	public void save(Connection con) {
		if (inst instanceof FightInstance || inst instanceof LoseInstance || inst instanceof LobbyInstance)
			return;
		
		try {
			SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_sessions")
					.addValue("host", host.toString())
					.addValue("slot", saveSlot)
					.addValue("regionType", region.getType().name())
					.addValue("position", curr.getRow())
					.addValue("lane", curr.getLane())
					.addValue("nodesVisited", nodesVisited)
					.addValue("regionsCompleted", regionsCompleted)
					.addValue("potionChance", potionChance)
					.addValue("notoriety", notoriety)
					.addValue("endless", endless ? 1 : 0)
					.addValue("lastSaved", System.currentTimeMillis())
					.addValue("instanceData", inst.serialize(party))
					.addValue("sessionType", sessionType.name())
					.addValue("lastMiniboss", lastMiniboss);
			PreparedStatement ps = sql.build(con);
			ps.executeBatch();
			ps.close();
		} catch (SQLException ex) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to save session for host " + host + " to slot " + saveSlot);
			ex.printStackTrace();
		}
		
		// Only save the nodes near the player and the boss
		region.saveRelevant(con);
		
		try (Statement delete = con.createStatement()) {
			delete.execute(
					"DELETE FROM neorogue_playersessiondata WHERE host = '" + host + "' AND slot = " + saveSlot + ";"
			);
			for (PlayerSessionData psd : party.values()) {
				psd.save(con);
			}
		} catch (SQLException ex) {
			Bukkit.getLogger()
					.warning("[NeoRogue] Failed to save player session data for host " + host + " to slot " + saveSlot);
			ex.printStackTrace();
		}
		
		party.get(host).getData().updateSnapshot(this, saveSlot);
	}

	// Captures each online party member's current fight health (capped at their session max). Used
	// to persist a lowered HP when a session ends mid-fight, since fights otherwise don't save.
	public HashMap<UUID, Double> captureLiveHealth() {
		HashMap<UUID, Double> out = new HashMap<UUID, Double>();
		for (PlayerSessionData psd : party.values()) {
			Player pl = psd.getPlayer();
			if (pl == null) continue;
			out.put(psd.getUniqueId(), (double) Math.round(Math.min(psd.getMaxHealth(), pl.getHealth())));
		}
		return out;
	}

	// Lowers each party member's saved health to their captured fight health, but only when it's
	// below the previously saved value. Runs async when a session ends during a fight.
	public void saveHealthAfterFightEnd(Connection con, HashMap<UUID, Double> capturedHp) {
		for (Entry<UUID, Double> ent : capturedHp.entrySet()) {
			try {
				double liveHp = ent.getValue();
				double savedHp;
				try (PreparedStatement sel = con.prepareStatement(
						"SELECT health FROM neorogue_playersessiondata WHERE host = ? AND slot = ? AND uuid = ?;")) {
					sel.setString(1, host.toString());
					sel.setInt(2, saveSlot);
					sel.setString(3, ent.getKey().toString());
					try (ResultSet rs = sel.executeQuery()) {
						if (!rs.next()) continue; // no saved row for this player
						savedHp = rs.getDouble("health");
					}
				}
				if (liveHp >= savedHp) continue; // only ever lower the saved health
				try (PreparedStatement upd = con.prepareStatement(
						"UPDATE neorogue_playersessiondata SET health = ? WHERE host = ? AND slot = ? AND uuid = ?;")) {
					upd.setDouble(1, liveHp);
					upd.setString(2, host.toString());
					upd.setInt(3, saveSlot);
					upd.setString(4, ent.getKey().toString());
					upd.executeUpdate();
				}
			} catch (SQLException ex) {
				Bukkit.getLogger().warning("[NeoRogue] Failed to save mid-fight health for " + ent.getKey());
				ex.printStackTrace();
			}
		}
	}
	
	// Players are added this way after a lobby instance starts the game
	public void addPlayers(HashMap<UUID, EquipmentClass> players, java.util.Set<UUID> commitCargo) {
		for (Entry<UUID, EquipmentClass> ent : players.entrySet()) {
			PlayerSessionData psd = new PlayerSessionData(ent.getKey(), ent.getValue(), this);
			if (commitCargo.contains(ent.getKey())) {
				moveCargoIntoRun(psd);
			}
			party.put(ent.getKey(), psd);
		}
	}

	// On a new run, a player's persistent cargo is emptied into the run so it can be sold as regions
	// are completed. Any unsold remainder is returned at the end of the run.
	private void moveCargoIntoRun(PlayerSessionData psd) {
		PlayerData pd = psd.getData();
		if (pd == null) return;
		me.neoblade298.neorogue.player.Cargo cargo = pd.getCargo();
		if (cargo.getItems().isEmpty()) return;
		for (Entry<Material, Integer> ent : new HashMap<Material, Integer>(cargo.getItems()).entrySet()) {
			psd.loadRunCargo(ent.getKey(), ent.getValue());
		}
		cargo.clear();
		pd.saveCargoAsync();
	}
	
	// Used for debug purposes only
	public void addPlayer(UUID uuid, EquipmentClass pc) {
		party.put(uuid, new PlayerSessionData(uuid, pc, this));
	}
	
	public void setBusy(boolean busy) {
		this.busy = busy;
	}
	
	public boolean isBusy() {
		return busy;
	}
	
	public void addSpectator(Player p) {
		MapViewer viewer = new MapViewer(this, p.getUniqueId());
		viewer.snapToCurrentNode();
		this.spectators.put(p.getUniqueId(), viewer);
		SessionManager.addToSession(p.getUniqueId(), this);
		broadcast("<yellow>" + p.getName() + "</yellow> started spectating!");
		p.setGameMode(GameMode.ADVENTURE);
		p.teleport(inst.getSpawn());
		inst.getSpectatorFlags().applyFlags(p);
		SessionManager.hidePlayerFromAll(p);
		inst.handleSpectatorJoin(p);
		setupSpectatorInventory(p);
	}
	
	public void removeSpectator(Player p) {
		broadcast("<yellow>" + p.getName() + "</yellow> stopped spectating!");
		inst.handleSpectatorLeave(p);
		spectators.remove(p.getUniqueId());
		p.getInventory().clear();
		PlayerFlags.applyDefaults(p);
		SessionManager.showPlayerToAll(p);
		SessionManager.removeFromSession(p.getUniqueId());
		SessionManager.giveMenuCompass(p);
		p.teleport(NeoRogue.spawn);
	}
	
	public HashMap<UUID, MapViewer> getSpectators() {
		return spectators;
	}
	
	public boolean isSpectator(UUID uuid) {
		return spectators.containsKey(uuid);
	}

	public void broadcastError(String msg) {
		for (Player p : getOnlinePlayers()) {
			Util.msgRaw(p, NeoCore.miniMessage().deserialize(msg).colorIfAbsent(NamedTextColor.RED));
			Sounds.error.play(p, p, Audience.ORIGIN);
		}
		
		for (UUID uuid : spectators.keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			Util.msgRaw(p, NeoCore.miniMessage().deserialize(msg).colorIfAbsent(NamedTextColor.RED));
			Sounds.error.play(p, p, Audience.ORIGIN);
		}
	}
	
	public void setSuggestCooldown() {
		this.nextSuggest = System.currentTimeMillis() + 1000L;
	}
	
	public boolean canSuggest() {
		return System.currentTimeMillis() > nextSuggest;
	}

	public void broadcastOthers(String msg, Player ignore) {
		for (Player p : getOnlinePlayers()) {
			if (p == ignore)
				continue;
			Util.msgRaw(p, NeoCore.miniMessage().deserialize(msg).colorIfAbsent(NamedTextColor.GRAY));
		}

		for (UUID uuid : spectators.keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			Util.msgRaw(p, NeoCore.miniMessage().deserialize(msg).colorIfAbsent(NamedTextColor.GRAY));
		}
	}

	public void broadcastOthers(Component msg, Player ignore) {
		for (Player p : getOnlinePlayers()) {
			if (p == ignore)
				continue;
			Util.msgRaw(p, msg);
		}
		for (UUID uuid : spectators.keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			Util.msgRaw(p, msg);
		}
	}

	public void launchFireworks() {
		new BukkitRunnable() {
			int count = 0;
			public void run() {
				launchFirework();
				if (++count >= 2) {
					this.cancel();
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, 10L);
	}
	
	private void launchFirework() {
		for (Player p : getOnlinePlayers()) {
			World w = p.getWorld();
			Location loc = p.getLocation().clone().add(NeoCore.gen.nextDouble(-1, 1), 
					NeoCore.gen.nextDouble(-1, 1), NeoCore.gen.nextDouble(-1, 1));
			Firework firework = w.spawn(loc, Firework.class);
			FireworkMeta meta = firework.getFireworkMeta();
			Color color = fireworkColors.get(NeoRogue.gen.nextInt(fireworkColors.size()));
			meta.addEffect(FireworkEffect.builder().withColor(color).with(FireworkEffect.Type.BALL).build());
			meta.setPower(1);
			firework.setFireworkMeta(meta);
		}
	}

	public void broadcast(String msg) {
		if (inst instanceof LobbyInstance) {
			((LobbyInstance) inst).broadcast(msg);
			return;
		}

		for (Player p : getOnlinePlayers()) {
			Util.msgRaw(p, NeoCore.miniMessage().deserialize(msg).colorIfAbsent(NamedTextColor.GRAY));
		}
		for (UUID uuid : spectators.keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			Util.msgRaw(p, NeoCore.miniMessage().deserialize(msg).colorIfAbsent(NamedTextColor.GRAY));
		}
	}

	public void broadcastTitle(Title title) {
		for (Player p : getOnlinePlayers()) {
			p.showTitle(title);
		}
		for (UUID uuid : spectators.keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			p.showTitle(title);
		}
	}

	public void broadcast(Component msg) {
		for (Player p : getOnlinePlayers()) {
			Util.msgRaw(p, msg.colorIfAbsent(NamedTextColor.GRAY));
		}
		for (UUID uuid : spectators.keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			Util.msgRaw(p, msg.colorIfAbsent(NamedTextColor.GRAY));
		}
	}
	
	public void broadcastSound(Sound s) {
		for (Player p : getOnlinePlayers()) {
			p.playSound(p, s, 1F, 1F);
		}

		for (UUID uuid : spectators.keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			p.playSound(p, s, 1F, 1F);
		}
	}
	
	public HashMap<UUID, PlayerSessionData> getParty() {
		return party;
	}
	
	public Instance getInstance() {
		return inst;
	}
	
	public boolean isEveryoneOnline() {
		for (UUID uuid : party.keySet()) {
			if (Bukkit.getPlayer(uuid) == null) {
				broadcast("<red>You can't move on until every member in your party is online!");
				return false;
			}
		}
		return true;
	}
	
	public int getPotionChance() {
		return potionChance;
	}
	
	public void addPotionChance(int amount) {
		this.potionChance = Math.max(0, Math.min(100, potionChance + amount));
	}

	public boolean rollPotionChance(int incrementOnFail) {
		if (NeoRogue.gen.nextInt(100) < potionChance) {
			addPotionChance(-25);
			return true;
		} else {
			addPotionChance(incrementOnFail);
			return false;
		}
	}

	public Equipment rollUpgrade(Equipment eq, double bonusChance) {
		return rollUpgradeFormula(eq, bonusChance) ? eq.getUpgraded() : eq;
	}

	public SessionEquipment rollUpgrade(SessionEquipment se, double bonusChance) {
		return rollUpgradeFormula(se.getEquipment(), bonusChance) ? se.upgrade() : se;
	}

	public void rollUpgrades(ArrayList<SessionEquipment> drops, double bonusChance) {
		for (int i = 0; i < drops.size(); i++) {
			SessionEquipment se = drops.get(i);
			Equipment eq = se.getEquipment();
			if (!eq.canUpgrade()) {
				Bukkit.getLogger().warning("Tried to upgrade unupgradeable item: " + eq.toString());
				continue;
			}
			if (rollUpgradeFormula(eq, bonusChance)) {
				drops.set(i, se.upgrade());
			}
		}
	}

	private boolean rollUpgradeFormula(Equipment eq, double bonusChance) {
		double chance = BASE_UPGRADE_CHANCE + bonusChance + (regionsCompleted * 0.2) - (eq.getRarity().getValue() * 0.15);
		if (NotorietySetting.LOWER_UPGRADE_CHANCE.isActive(this)) {
			chance *= 0.5;
		}
		return NeoRogue.gen.nextDouble() <= chance;
	}
	
	public void setupSpectatorInventory(Player p) {
		if (p == null) return;
		PlayerInventory pinv = p.getInventory();
		pinv.clear();

		// Node map (slot 0) whenever a region is loaded
		if (region != null) {
			ItemStack item = CoreInventory
					.createButton(Material.FILLED_MAP, Component.text("Node Map", NamedTextColor.GOLD));
			MapMeta meta = (MapMeta) item.getItemMeta();
			meta.setMapView(Bukkit.getMap(EditInventoryInstance.MAP_ID));
			item.setItemMeta(meta);
			pinv.setItem(0, item);
		}

		// During fights or inventory editing, a head per party member for teleporting / viewing their inventory
		if (inst instanceof FightInstance || inst instanceof EditInventoryInstance) {
			List<PlayerSessionData> members = new ArrayList<PlayerSessionData>(party.values());
			int firstSlot = 1, lastSlot = 7, available = lastSlot - firstSlot + 1;
			int center = firstSlot + (available - 1) / 2;
			int count = Math.min(members.size(), available);
			for (int i = 0; i < count; i++) {
				int slot;
				if (count <= 4) {
					// Centered layout with a 1-slot gap between heads
					// 1:{4} 2:{3,5} 3:{2,4,6} 4:{1,3,5,7}
					slot = center - (count - 1) + 2 * i;
				} else {
					// More heads than the centered layout can hold: spread to fill the width
					slot = firstSlot + (int) Math.round((double) i * (available - 1) / (count - 1));
				}
				pinv.setItem(slot, createSpectatorHead(members.get(i)));
			}
		}

		// Barrier (slot 8) to stop spectating
		pinv.setItem(8, createLeaveBarrier());
	}

	private ItemStack createSpectatorHead(PlayerSessionData data) {
		ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setOwningPlayer(data.getPlayer() != null ? data.getPlayer() : Bukkit.getOfflinePlayer(data.getUniqueId()));
		meta.displayName(Component.text(data.getData().getDisplay(), NamedTextColor.YELLOW)
				.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
		meta.lore(List.of(
				Component.text("Left click to teleport", NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
				Component.text("Right click to view inventory", NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
		skull.setItemMeta(meta);
		String spectateTarget = data.getUniqueId().toString();
		NBT.modify(skull, nbt -> { nbt.setString("spectateTarget", spectateTarget); });
		return skull;
	}

	private ItemStack createLeaveBarrier() {
		ItemStack barrier = new ItemStack(Material.BARRIER);
		ItemMeta meta = barrier.getItemMeta();
		meta.displayName(Component.text("Leave Session", NamedTextColor.RED)
				.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
		meta.lore(List.of(Component.text("Click to stop spectating", NamedTextColor.GRAY)
				.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
		barrier.setItemMeta(meta);
		return barrier;
	}

	// Routes a spectator's world interaction based on the held hotbar item. Recognized spectator
	// tools (map/leave barrier/party heads) take priority over block clicks so they still work when
	// the spectator happens to be aiming at a block. Unrecognized items and bare block clicks (e.g.
	// lecterns) fall through to the instance's own spectator handler.
	public void handleSpectatorInteract(Player p, PlayerInteractEvent e) {
		if (handleSpectatorItem(p, e.getItem(), e.getAction().isLeftClick(), e.getAction().isRightClick())) return;
		inst.handleSpectatorInteractEvent(e);
	}

	// Lets the spectator hotbar tools also work when clicked inside the player's own inventory (e.g.
	// with the inventory open), not just via world interaction. Deferred a tick so actions that open
	// inventories or teleport don't fight the in-progress InventoryClickEvent.
	public void handleSpectatorInventoryClick(Player p, InventoryClickEvent e) {
		ItemStack item = e.getCurrentItem();
		if (item == null || item.getType().isAir()) return;
		final boolean left = e.isLeftClick(), right = e.isRightClick();
		final ItemStack clicked = item;
		new BukkitRunnable() {
			public void run() {
				handleSpectatorItem(p, clicked, left, right);
			}
		}.runTask(NeoRogue.inst());
	}

	// Shared dispatch for the spectator hotbar tools, usable from both world interaction and inventory
	// clicks. Returns true if the item was a recognized spectator tool (and was handled).
	private boolean handleSpectatorItem(Player p, ItemStack item, boolean isLeftClick, boolean isRightClick) {
		Material type = item != null ? item.getType() : null;

		if (type == Material.FILLED_MAP) {
			MapViewer viewer = spectators.get(p.getUniqueId());
			if (viewer != null) {
				if (isLeftClick) viewer.scrollMapDown();
				else if (isRightClick) viewer.scrollMapUp();
			}
			return true;
		}
		if (type == Material.BARRIER) {
			// Either click leaves; spectators shouldn't have to hunt for the right mouse button
			removeSpectator(p);
			return true;
		}
		if (type == Material.PLAYER_HEAD && (inst instanceof FightInstance || inst instanceof EditInventoryInstance)) {
			String spectateTarget = NBT.get(item, nbt -> { return nbt.hasTag("spectateTarget") ? nbt.getString("spectateTarget") : null; });
			if (spectateTarget != null) {
				PlayerSessionData data = party.get(UUID.fromString(spectateTarget));
				if (data == null) return true;
				if (isLeftClick) {
					// Downed players are hidden, so teleport to their corpse if they have one
					Location dest = inst instanceof FightInstance
							? ((FightInstance) inst).getCorpseLocation(data.getUniqueId())
							: null;
					if (dest == null) {
						Player target = data.getPlayer();
						if (target != null && target.isOnline()) dest = target.getLocation();
					}
					if (dest != null) {
						p.teleport(dest);
						Sounds.teleport.play(p, p, Audience.ORIGIN);
					}
				}
				else if (isRightClick) {
					new PlayerSessionSpectateInventory(data, p);
				}
				return true;
			}
		}

		return false;
	}
	
	// False if set instance fails
	public boolean setInstance(Instance next) {
		if (next == null) {
			Bukkit.getLogger().severe("[NeoRogue] Attempted to setInstance with null! Ignoring.");
			return false;
		}
		boolean firstLoad = this.inst == null;
		if (!firstLoad) {
			if (!canSetInstance(next)) {
				return false;
			}
			this.inst.unloadChunks();
			this.inst.cleanup(false);
		}
		this.inst = next;
		next.start();
		Bukkit.getLogger()
				.info("Started instance " + next.getClass().getSimpleName() + ", visited nodes " + nodesVisited + ", regions completed " + regionsCompleted);
		for (PlayerSessionData psd : party.values()) {
			Player p = psd.getPlayer();
			if (p == null) continue;
			psd.trigger(SessionTrigger.VISIT_NODE, null);
			Bukkit.getLogger().info("Serialization for " + p.getName());
			Bukkit.getLogger().info(psd.serialize());
			Bukkit.getLogger().info("Abilities: " + psd.getAbilitiesEquipped() + " / " + psd.getMaxAbilities());
			Bukkit.getLogger().info("Accessories: " + psd.getAccessoriesEquipped() + " / " + psd.getAccessorySlots());
			Bukkit.getLogger().info("Armor: " + psd.getArmorEquipped() + " / " + psd.getArmorSlots());
		}
		
		// Don't save because autosave happened
		if (firstLoad)
			return true;

		// Don't save because the session is already ended
		if (inst instanceof EndRunInstance)
			return true;

		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = SQLManager.getConnection("NeoRogue")) {
					save(con);
				} catch (SQLException ex) {
					Bukkit.getLogger().warning(
							"[NeoRogue] Failed to acquire connection to save session hosted by " + host + " to slot "
									+ saveSlot
					);
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
		return true;
	}

	public boolean canSetInstance(Instance next) {
		if (!(this.inst instanceof FightInstance)) {
			for (UUID uuid : party.keySet()) {
				if (Bukkit.getPlayer(uuid) == null) {
					broadcast("<red>You can't move on until every member in your party is online!");
					return false;
				}
			}
		}

		// Save player's storage
		if (this.inst instanceof EditInventoryInstance && !EditInventoryInstance.isValid(this))
			return false;

		return true;
	}
	
	public PlayerSessionData getData(UUID uuid) {
		return party.get(uuid);
	}
	
	public void setRegion(Region region) {
		this.region = region;
	}

	public Region getRegion() {
		return region;
	}
	
	public ArrayList<Player> getOnlinePlayers() {
		if (inst instanceof LobbyInstance) {
			return ((LobbyInstance) inst).getOnlinePlayers();
		}

		ArrayList<Player> players = new ArrayList<Player>();
		for (UUID uuid : party.keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			if (p != null)
				players.add(p);
		}
		return players;
	}

	// This includes spectators and players
	public ArrayList<Player> getChatParticipants() {
		if (inst instanceof LobbyInstance) {
			return ((LobbyInstance) inst).getChatParticipants();
		}

		ArrayList<Player> participants = new ArrayList<Player>();
		for (UUID uuid : party.keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			if (p != null)
				participants.add(p);
		}
		for (UUID uuid : spectators.keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			if (p != null)
				participants.add(p);
		}
		return participants;
	}
	
	public int getNodesVisited() {
		return nodesVisited;
	}
	
	// Can be changed in the future for different scaling setups
	public int getLevel() {
		return nodesVisited - 1;
	}

	// Standardized enemy damage scaling by session level (nodes visited) plus the increase-damage
	// notoriety modifier: +20% per level beyond the first, multiplied by the notoriety damage bonus
	// when active. Mob levels are set to getLevel() on spawn, so this is equivalent to reading a
	// MythicMob caster's level.
	public double getMobDamageMultiplier() {
		double mult = 1 + ((getLevel() - 1) * 0.2);
		if (NotorietySetting.INCREASE_DAMAGE.isActive(this)) {
			mult *= NotorietySetting.INCREASE_DAMAGE_MULTIPLIER;
		}
		return mult;
	}
	
	public void setNodesVisited(int nodesVisited) {
		this.nodesVisited = nodesVisited;
	}

	public String getLastMiniboss() {
		return lastMiniboss;
	}

	public void setLastMiniboss(String lastMiniboss) {
		this.lastMiniboss = lastMiniboss;
	}

	public void setRegionsCompleted(int regionsCompleted) {
		this.regionsCompleted = regionsCompleted;
	}

	public void incrementRegionsCompleted() {
		this.regionsCompleted++;
	}

	// False if the session is "for-fun", disables competitive aspects like winrates,
	// balancing, rewards, and achievements.
	public boolean isCompetitive() {
		return !endless;
	}
	
	public int getXOff() {
		return xOff;
	}
	
	public int getZOff() {
		return zOff;
	}
	
	public Plot getPlot() {
		return plot;
	}

	public void setEndless(boolean endless) {
		this.endless = endless;
	}

	public boolean isEndless() {
		return endless;
	}

	public void setLobbyOpen(boolean lobbyOpen) {
		this.lobbyOpen = lobbyOpen;
		// The first time the lobby is opened, broadcast a clickable button so others can join
		if (lobbyOpen && !announcedOpenLobby) {
			announcedOpenLobby = true;
			broadcastLobbyInvite(true);
		}
	}

	// Announces this lobby to the server with a clickable button. open = true means it auto-accepts
	// (Click to Join); open = false is a freshly created lobby that requires requests (Click to Request to Join).
	public void broadcastLobbyInvite(boolean open) {
		Player hostPlayer = Bukkit.getPlayer(host);
		if (hostPlayer == null) return;
		String hostName = hostPlayer.getName();
		Component lead = open ? Component.text("'s lobby is now open! ", NamedTextColor.GRAY)
				: Component.text(" created a new lobby! ", NamedTextColor.GRAY);
		String label = open ? "Click to Join" : "Click to Request to Join";
		String hover = (open ? "Click to join " : "Click to request to join ") + hostName + "'s lobby";
		Component msg = Component.text(hostName, NamedTextColor.YELLOW)
				.append(lead)
				.append(Component.text("[" + label + "]", NamedTextColor.GOLD)
						.decorate(TextDecoration.BOLD)
						.hoverEvent(HoverEvent.showText(
								Component.text(hover, NamedTextColor.GRAY)))
						.clickEvent(ClickEvent.runCommand("/nr join " + hostName)));
		for (Player online : Bukkit.getOnlinePlayers()) {
			// Skip players already in a session; they can't join anyway
			if (SessionManager.getSession(online) != null) continue;
			online.sendMessage(msg);
		}
	}

	public boolean isLobbyOpen() {
		return lobbyOpen;
	}
    
    public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isDebug() {
		return debug;
	}

	public SessionType getSessionType() {
		return sessionType;
	}

	public void setSessionType(SessionType sessionType) {
		this.sessionType = sessionType;
	}

	public int getNotoriety() {
		return notoriety;
	}

	public void updateAllBoards() {
		for (PlayerSessionData psd : party.values()) {
			psd.updateBoardLines();
		}
	}

	public int getMaxNotoriety() {
		PlayerSessionData hostData = party.get(host);
		EquipmentClass ec = hostData != null ? hostData.getPlayerClass() : null;
		return PlayerManager.getPlayerData(host).getMaxNotoriety(ec);
	}

	public int getMaxNotoriety(EquipmentClass ec) {
		return PlayerManager.getPlayerData(host).getMaxNotoriety(ec);
	}
	
	public void addNotoriety(int amount) {
		notoriety += amount;
	}

	public void setNotoriety(int amount) {
		notoriety = Math.max(0, Math.min(amount, 10));
	}

	public double getRegionXpMultiplier() {
		if (region == null) return 1.0;
		switch (region.getType()) {
		case LOW_DISTRICT:
		case LOW_DISTRICT_DEBUG:
			return 1.0;
		case HARVEST_FIELDS:
		case HARVEST_FIELDS_DEBUG:
			return 1.5;
		case FROZEN_WASTES:
		case FROZEN_WASTES_DEBUG:
			return 2.5;
		default:
			return 1.0;
		}
	}

	public double getNotorietyXpMultiplier() {
		return 1.0 + notoriety * 0.05;
	}

	public int getNotorietyXpBonusPercent() {
		return notoriety * 5;
	}

	// Each notoriety level grants a 2% bonus to run money earnings
	public double getNotorietyMoneyMultiplier() {
		return 1.0 + notoriety * 0.02;
	}

	public int getNotorietyMoneyBonusPercent() {
		return notoriety * 2;
	}

	public void awardXp(int baseXp) {
		if (!isCompetitive()) return;
		int finalXp = (int) Math.round(baseXp * getRegionXpMultiplier() * getNotorietyXpMultiplier());
		for (Entry<UUID, PlayerSessionData> entry : party.entrySet()) {
			PlayerData pdata = PlayerManager.getPlayerData(entry.getKey());
			if (pdata == null) continue;
			EquipmentClass ec = entry.getValue().getPlayerClass();
			int playerXp = (int) Math.round(finalXp * entry.getValue().getRunExpBoostMultiplier());
			pdata.addExp(ec, playerXp);
			pdata.saveAchievementsAsync();
		}
	}

	// Captures and consumes each party member's exp boosts for this run. TIME boosts
	// contribute while active; RUNS boosts are decremented by one and applied to this run.
	// Server-wide global boosts are added additively on top of each member's personal boosts.
	public void applyExpBoosts() {
		if (sessionType == SessionType.TUTORIAL) return;
		double globalBonus = GlobalBoostManager.getGlobalBoostBonus();
		for (Entry<UUID, PlayerSessionData> entry : party.entrySet()) {
			PlayerData pdata = PlayerManager.getPlayerData(entry.getKey());
			if (pdata == null) continue;
			entry.getValue().setRunExpBoostMultiplier(pdata.consumeRunExpBoosts() + globalBonus);
		}
	}
	
	public void generateRegion(RegionType type) {
		this.region = new Region(type, xOff, zOff, this);
		region.instantiate();
	}

	public void generateRegion() {
		generateRegion(sessionType.getInitialRegion());
	}

	private static void clearLecternBook(Block block) {
		if (!(block.getState(false) instanceof org.bukkit.block.Lectern lectern)) {
			return;
		}
		lectern.getInventory().clear();
	}
	
	public void generateNextRegion() {
		// Erase old nodes
		Node[][] nodes = region.getNodes();
		int bossRow = region.getBossRow();
		for (int i = 0; i < nodes.length; i++) {
			for (int j = 0; j < nodes[i].length; j++) {
				Node n = nodes[i][j];
				if (n == null)
					continue;
				Location loc = region.nodeToLocation(n, 0);
				loc.getBlock().setType(Material.AIR); // Remove node block
				// Remove boss stuff
				if (i == bossRow && j == Region.CENTER_LANE) {
					loc.add(0, 1, 0);
					loc.getBlock().setType(Material.AIR); // Remove button
					loc.add(0, -1, -1);
					loc.getBlock().setType(Material.AIR); // Remove sign
					loc.add(0, -1, 0);
					Block lecternBlock = loc.getBlock();
					clearLecternBook(lecternBlock);
					lecternBlock.setType(Material.CRYING_OBSIDIAN); // Remove boss lectern
				}
			}
		}
		
		for (PlayerSessionData psd : party.values()) {
			psd.getSessionStats().markRegionStart();
		}
		generateRegion(RegionType.getNextRegion(region.getType(), endless));
	}
	
	public Node getNode() {
		return curr;
	}
	
	public void setNode(Node node) {
		this.curr = node;
	}
	
	public void visitNode(Node node) {
		for (PlayerSessionData psd : party.values()) {
			psd.setMapPosition(node.getRow());
			psd.setShouldMapRender(true);
		}
		for (MapViewer viewer : spectators.values()) {
			viewer.setMapPosition(node.getRow());
			viewer.setShouldMapRender(true);
		}
		this.curr = node;
		nodesVisited++;
		awardXp(20);
	}
	
	public String getName() {
		return name;
	}

	public UUID getHost() {
		return host;
	}
	
	public int getRegionsCompleted() {
		return regionsCompleted;
	}

	public int getBaseDropValue() {
		return regionsCompleted * 2;
	}
	
	public int getSaveSlot() {
		return saveSlot;
	}
	
	public void cleanup(boolean pluginDisable) {
		inst.unloadChunks();
		inst.cleanup(pluginDisable);

		// Remove blocks from node select
		// Doesn't happen if it's still lobby instance
		if (region != null) {
			region.cleanupAll();
		}

		for (Entry<UUID, PlayerSessionData> entry : party.entrySet()) {
			Player p = Bukkit.getPlayer(entry.getKey());
			entry.getValue().cleanup();
			SessionManager.resetPlayer(p);
		}
		
		for (UUID uuid : spectators.keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			SessionManager.resetPlayer(p);
		}
	}
	
	// Any party member leaving an active run ends the game for the whole party.
	public void leavePlayer(Player p) {
		broadcast("<yellow>" + p.getName() + " <gray>has left the party, so the game has ended!");
		SessionManager.endSession(this);
	}
	
	// Standardized entry point for a player leaving this session via /nr leave or the leave button
	// in the session inventory. Handles busy/loading state, spectators, and lobby vs in-game dispatch.
	// Returns true if the leave was carried out, false if it was rejected (e.g. session is loading).
	public boolean tryLeave(Player p) {
		if (isBusy()) {
			Util.displayError(p, "You can't do that while the session is loading!");
			return false;
		}
		if (isSpectator(p.getUniqueId())) {
			removeSpectator(p);
			return true;
		}
		if (inst instanceof LobbyInstance) {
			((LobbyInstance) inst).leavePlayer(p);
		}
		else {
			leavePlayer(p);
		}
		return true;
	}
	
	public void kickPlayer(Player p, OfflinePlayer target) {
		UUID uuid = p.getUniqueId();
		if (!uuid.equals(host)) {
			Util.displayError(p, "Only the host may kick players");
		} else {
			if (!party.containsKey(target.getUniqueId())) {
				Util.displayError(p, "That player isn't in your party!");
				return;
			}
			broadcast("<yellow>" + target.getName() + " <gray>was kicked from the party!");
			PlayerSessionData psd = party.remove(target.getUniqueId());
			psd.cleanup();

			if (target instanceof Player) {
				Player targetPlayer = (Player) target;
				SessionManager.resetPlayer(targetPlayer);
				inst.handlePlayerLeaveParty(targetPlayer);
			}
			SessionManager.removeFromSession(target.getUniqueId());
		}
	}
	
	public void deleteSave() {
		try (Connection con = SQLManager.getConnection("NeoRogue"); Statement stmt = con.createStatement()) {
			stmt.executeUpdate(
					"DELETE FROM neorogue_playersessiondata WHERE host = '" + host + "' AND slot = " + saveSlot + ";"
			);
			stmt.executeUpdate(
					"DELETE FROM neorogue_sessions WHERE host = '" + host + "' AND slot = " + saveSlot + ";"
			);
			stmt.executeUpdate(
					"DELETE FROM neorogue_sessioncargo WHERE host = '" + host + "' AND slot = " + saveSlot + ";"
			);
			stmt.executeUpdate(
					"DELETE FROM neorogue_sessioncargosold WHERE host = '" + host + "' AND slot = " + saveSlot + ";"
			);
		} catch (SQLException ex) {
			Bukkit.getLogger().warning(
					"[NeoRogue] Failed to acquire connection to delete session hosted by " + host + " from slot "
							+ saveSlot
			);
			ex.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return plot.toString();
	}
}
