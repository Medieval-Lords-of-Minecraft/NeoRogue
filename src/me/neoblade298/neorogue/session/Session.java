package me.neoblade298.neorogue.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
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

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.effects.Audience;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.io.SQLManager;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder.SQLAction;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.area.Node;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.MapViewer;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.fight.FightInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Session {
	private static final boolean endless = false; // TEMP FOR NOW
	
	private Area area;
	private UUID host;
	private HashMap<UUID, PlayerSessionData> party = new HashMap<UUID, PlayerSessionData>();
	private HashMap<UUID, MapViewer> spectators = new HashMap<UUID, MapViewer>();
	private Instance inst;
	private Node curr;
	private SessionStatistics stats;
	private int saveSlot, xOff, zOff, nodesVisited, areasCompleted, potionChance = 25;
	private Plot plot;
	private boolean busy;
	private long nextSuggest = 0L;
	private ArrayList<String> spectatorLines = new ArrayList<String>();
	private double BASE_UPGRADE_CHANCE = 0.3;
	
	// Session coordinates
	public static final int LOBBY_X = 0, LOBBY_Z = 0, LOBBY_WIDTH = 15, AREA_X = 0, AREA_Z = LOBBY_Z + LOBBY_WIDTH,
			AREA_WIDTH = 81, REWARDS_X = 0, REWARDS_Z = AREA_Z + AREA_WIDTH, REWARDS_WIDTH = 19, SHRINE_X = 0,
			SHRINE_Z = REWARDS_Z + REWARDS_WIDTH, SHRINE_WIDTH = 13, SHOP_X = 0, SHOP_Z = SHRINE_Z + SHRINE_WIDTH,
			SHOP_WIDTH = 12, CHANCE_X = 0, CHANCE_Z = SHOP_Z + SHOP_WIDTH, CHANCE_WIDTH = 12, LOSE_X = 0,
			LOSE_Z = CHANCE_Z + CHANCE_WIDTH;
	
	private static Clipboard classSelect, nodeSelect, rewardsRoom, shrine, shop, chance, lose;
	static {
		classSelect = loadClipboard("classselect.schem");
		nodeSelect = loadClipboard("nodeselect.schem");
		rewardsRoom = loadClipboard("rewards.schem");
		shrine = loadClipboard("shrine.schem");
		shop = loadClipboard("shop.schem");
		chance = loadClipboard("chance.schem");
		lose = loadClipboard("graveyard.schem");
	}
	
	private static Clipboard loadClipboard(String schematic) {
		File file = new File(NeoRogue.SCHEMATIC_FOLDER, schematic);
		ClipboardFormat format = ClipboardFormats.findByFile(file);
		try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
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
		Operation operation = new ClipboardHolder(clipboard).createPaste(editSession)
				.to(BlockVector3.at(-(session.getXOff() + xOff + 1), 64 + yOff, session.getZOff() + zOff))
				.ignoreAirBlocks(false).build();
		try {
			Operations.complete(operation);
		} catch (WorldEditException e) {
			e.printStackTrace();
		}
	}

	private static void pasteSchematic(Clipboard clipboard, EditSession editSession, Session session, int zOff) {
		pasteSchematic(clipboard, editSession, session, 0, 0, zOff);
	}

	private static void pasteSchematic(
			Clipboard clipboard, EditSession editSession, Session session, int yOff, int zOff
	) {
		pasteSchematic(clipboard, editSession, session, 0, yOff, zOff);
	}
	
	public Session(Player p, Plot plot, String lobby, int saveSlot) {
		this.saveSlot = saveSlot;
		this.xOff = plot.getXOffset();
		this.zOff = plot.getZOffset();
		host = p.getUniqueId();
		this.plot = plot;
		this.inst = new LobbyInstance(lobby, p, this);
		generateInterstitials();
	}
	
	// Load from existing data
	public Session(Player p, Plot plot, int saveSlot) {
		this.saveSlot = saveSlot;
		this.xOff = plot.getXOffset();
		this.zOff = plot.getZOffset();
		host = p.getUniqueId();
		this.plot = plot;
		
		Session s = this;
		generateInterstitials();
		new BukkitRunnable() {
			@Override
			public void run() {
				Util.msgRaw(p, Component.text("Loading game...", NamedTextColor.GRAY));
				
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
					sessSet.next();
					nodesVisited = sessSet.getInt("nodesVisited");
					int pos = sessSet.getInt("position");
					int lane = sessSet.getInt("lane");
					Instance inst = Instance.deserialize(s, sessSet, party);

					area = new Area(
							AreaType.valueOf(sessSet.getString("areaType")), xOff, zOff, host, saveSlot, s, stmt
					);
					curr = area.getNodes()[pos][lane];
					
					new BukkitRunnable() {
						@Override
						public void run() {
							area.instantiate();
							setInstance(inst);
							s.updateSpectatorLines();
							Util.msgRaw(p, Component.text("Finished loading.", NamedTextColor.GRAY));
						}
					}.runTask(NeoRogue.inst());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
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
		// Generate the lobby and add the host there
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(Area.world)) {
			pasteSchematic(classSelect, editSession, this, Session.LOBBY_Z);
			pasteSchematic(nodeSelect, editSession, this, Session.AREA_Z);
			pasteSchematic(rewardsRoom, editSession, this, Session.REWARDS_Z);
			pasteSchematic(shrine, editSession, this, Session.SHRINE_Z);
			pasteSchematic(shop, editSession, this, Session.SHOP_Z);
			pasteSchematic(chance, editSession, this, Session.CHANCE_Z);
			pasteSchematic(lose, editSession, this, -1, Session.LOSE_Z);
		}
	}
	
	public void save(Statement insert, Statement delete) {
		if (inst instanceof FightInstance || inst instanceof LoseInstance)
			return;
		
		try {
			SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_sessions")
					.addString(host.toString()).addValue(saveSlot).addString(area.getType().name())
					.addValue(curr.getRow()).addValue(curr.getLane()).addValue(nodesVisited).addValue(potionChance)
					.addValue(System.currentTimeMillis()).addString(inst.serialize(party));
			insert.execute(sql.build());
		} catch (SQLException ex) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to save session for host " + host + " to slot " + saveSlot);
			ex.printStackTrace();
		}
		
		// Only save the nodes near the player
		area.saveRelevant(insert, delete);
		
		try {
			delete.execute(
					"DELETE FROM neorogue_playersessiondata WHERE host = '" + host + "' AND slot = " + saveSlot + ";"
			);
			for (PlayerSessionData psd : party.values()) {
				psd.save(insert);
			}
		} catch (SQLException ex) {
			Bukkit.getLogger()
					.warning("[NeoRogue] Failed to save player session data for host " + host + " to slot " + saveSlot);
			ex.printStackTrace();
		}
		
		party.get(host).getData().updateSnapshot(this, saveSlot);
	}
	
	// Players are added this way after a lobby instance starts the game
	public void addPlayers(HashMap<UUID, EquipmentClass> players) {
		for (Entry<UUID, EquipmentClass> ent : players.entrySet()) {
			party.put(ent.getKey(), new PlayerSessionData(ent.getKey(), ent.getValue(), this));
		}
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
		this.spectators.put(p.getUniqueId(), new MapViewer(this, p.getUniqueId()));
		SessionManager.addToSession(p.getUniqueId(), this);
		broadcast("<yellow>" + p.getName() + "</yellow> started spectating!");
		p.setGameMode(GameMode.ADVENTURE);
		p.teleport(inst.spawn);
		p.setInvulnerable(true);
		p.setInvisible(true);
		
		if (inst instanceof EditInventoryInstance) {
			setupSpectatorInventory(p);
		}
		if (inst instanceof NodeSelectInstance) {
			p.setAllowFlight(true);
		}
		if (inst instanceof FightInstance) {
			((FightInstance) inst).addSpectator(p);
			p.setAllowFlight(true);
		}
	}
	
	public void removeSpectator(Player p) {
		broadcast("<yellow>" + p.getName() + " <gray>has stopped spectating!");
		spectators.remove(p.getUniqueId());
		p.setInvisible(false);
		p.setInvulnerable(false);
		p.setAllowFlight(false);
		if (inst instanceof FightInstance) {
			((FightInstance) inst).removeSpectator(p);
		}
		SessionManager.removeFromSession(p.getUniqueId());
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

	public void broadcast(String msg) {
		for (Player p : getOnlinePlayers()) {
			Util.msgRaw(p, NeoCore.miniMessage().deserialize(msg).colorIfAbsent(NamedTextColor.GRAY));
		}
		for (UUID uuid : spectators.keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			Util.msgRaw(p, NeoCore.miniMessage().deserialize(msg).colorIfAbsent(NamedTextColor.GRAY));
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

	public Equipment rollUpgrade(Equipment eq) {
		return rollUpgradeFormula(eq) ? eq.getUpgraded() : eq;
	}

	public ArrayList<Equipment> rollUpgrades(ArrayList<Equipment> drops) {
		for (int i = 0; i < drops.size(); i++) {
			Equipment eq = drops.get(i);
			if (!eq.canUpgrade()) {
				Bukkit.getLogger().warning("Tried to upgrade unupgradeable item: " + drops.get(i).toString());
				continue;
			}
			if (rollUpgradeFormula(eq)) {
				drops.set(i, eq.getUpgraded());
			}
		}
		return drops;
	}

	private boolean rollUpgradeFormula(Equipment eq) {
		return NeoRogue.gen.nextDouble() <= BASE_UPGRADE_CHANCE + (areasCompleted * 0.2) - (eq.getRarity().getValue() * 0.15);
	}
	
	public void setupSpectatorInventory(Player p) {
		p.getInventory().clear();
		if (inst instanceof NodeSelectInstance)
			return;
		ItemStack item = CoreInventory
				.createButton(Material.FILLED_MAP, Component.text("Node Map", NamedTextColor.GOLD));
		MapMeta meta = (MapMeta) item.getItemMeta();
		MapView map = Bukkit.getMap(EditInventoryInstance.MAP_ID);
		meta.setMapView(map);
		item.setItemMeta(meta);
		p.getInventory().setItem(0, item);
	}
	
	// False if set instance fails
	public boolean setInstance(Instance inst) {
		boolean firstLoad = this.inst == null;
		if (!firstLoad) {
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
			this.inst.cleanup();
		}
		this.inst = inst;
		inst.start();
		System.out.println("Started instance " + inst.getClass().getSimpleName());
		for (PlayerSessionData psd : party.values()) {
			psd.trigger(SessionTrigger.VISIT_NODE, null);
			System.out.println("Debug for " + psd.getPlayer().getName());
			for (EquipSlot es : EquipSlot.values()) {
				String line = es + ": ";
				Equipment[] eqs = psd.getEquipment(es);
				for (int i = 0; i < eqs.length; i++) {
					Equipment eq = eqs[i];
					line += i + (eq == null ? "-" : eq.toString()) + " ";
				}
				System.out.println(line);
			}
		}
		
		// Auto-save
		if (firstLoad)
			return true;
		new BukkitRunnable() {
			@Override
			public void run() {
				try (Connection con = SQLManager.getConnection("NeoRogue");
						Statement insert = con.createStatement();
						Statement delete = con.createStatement()) {
					save(insert, delete);
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
	
	public PlayerSessionData getData(UUID uuid) {
		return party.get(uuid);
	}
	
	public Area getArea() {
		return area;
	}
	
	public ArrayList<Player> getOnlinePlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		for (UUID uuid : party.keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			if (p != null)
				players.add(p);
		}
		return players;
	}
	
	public int getNodesVisited() {
		return nodesVisited;
	}
	
	// Can be changed in the future for different scaling setups
	public int getLevel() {
		return nodesVisited - 1;
	}
	
	public void setNodesVisited(int nodesVisited) {
		this.nodesVisited = nodesVisited;
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
	
	public void generateArea(AreaType type) {
		this.area = new Area(type, xOff, zOff, this);
		area.instantiate();
	}
	
	public void generateNextArea() {
		// Erase old nodes
		Node[][] nodes = area.getNodes();
		for (int i = 0; i < nodes.length; i++) {
			for (int j = 0; j < nodes[i].length; j++) {
				Node n = nodes[i][j];
				if (n == null)
					continue;
				Location loc = area.nodeToLocation(n, 0);
				loc.getBlock().setType(Material.AIR); // Remove node block
				// Remove boss stuff
				if (i == 15 && j == 2) {
					loc.add(0, 1, 0);
					loc.getBlock().setType(Material.AIR); // Remove button
					loc.add(0, -1, -1);
					loc.getBlock().setType(Material.AIR); // Remove sign
					loc.add(0, -1, 0);
					loc.getBlock().setType(Material.CRYING_OBSIDIAN); // Remove boss lectern
				}
			}
		}
		
		generateArea(AreaType.getNextArea(area.getType(), endless));
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
	}
	
	public UUID getHost() {
		return host;
	}
	
	public SessionStatistics getStats() {
		return stats;
	}
	
	public int getAreasCompleted() {
		return areasCompleted;
	}
	
	public int getSaveSlot() {
		return saveSlot;
	}
	
	public void cleanup() {
		inst.cleanup();
		
		for (UUID uuid : party.keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			SessionManager.resetPlayer(p);
		}
		
		for (UUID uuid : spectators.keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			SessionManager.resetPlayer(p);
		}
	}
	
	public void leavePlayer(Player p) {
		UUID uuid = p.getUniqueId();
		if (uuid.equals(host)) {
			broadcast("The host has left the party, so the game has ended!");
			SessionManager.endSession(this);
		} else {
			broadcast("<yellow>" + p.getName() + " <gray>has left the party!");
			party.remove(p.getUniqueId());
			SessionManager.resetPlayer(p);
			SessionManager.removeFromSession(p.getUniqueId());
			
			if (inst instanceof FightInstance) {
				((FightInstance) inst).handlePlayerLeaveParty(p);
			}
		}
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
			party.remove(target.getUniqueId());
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
					"DELETE FROM neorogue_sessions WHERE host = '" + host + "' AND slot = " + saveSlot + ";"
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
