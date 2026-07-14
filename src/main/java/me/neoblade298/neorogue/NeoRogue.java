package me.neoblade298.neorogue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Nullable;

import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicReloadedEvent;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.commands.SubcommandManager;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.achievement.AchievementRewardRegistry;
import me.neoblade298.neorogue.commands.CmdAccept;
import me.neoblade298.neorogue.commands.CmdAchievements;
import me.neoblade298.neorogue.commands.CmdAdminAchievement;
import me.neoblade298.neorogue.commands.CmdAdminBoost;
import me.neoblade298.neorogue.commands.CmdAdminBoss;
import me.neoblade298.neorogue.commands.CmdAdminChance;
import me.neoblade298.neorogue.commands.CmdAdminCoins;
import me.neoblade298.neorogue.commands.CmdAdminDamage;
import me.neoblade298.neorogue.commands.CmdAdminDebug;
import me.neoblade298.neorogue.commands.CmdAdminDebugMode;
import me.neoblade298.neorogue.commands.CmdAdminDeserialize;
import me.neoblade298.neorogue.commands.CmdAdminDrop;
import me.neoblade298.neorogue.commands.CmdAdminDropArtifact;
import me.neoblade298.neorogue.commands.CmdAdminEquipment;
import me.neoblade298.neorogue.commands.CmdAdminExp;
import me.neoblade298.neorogue.commands.CmdAdminFlag;
import me.neoblade298.neorogue.commands.CmdAdminGlobalBoost;
import me.neoblade298.neorogue.commands.CmdAdminGod;
import me.neoblade298.neorogue.commands.CmdAdminLevel;
import me.neoblade298.neorogue.commands.CmdAdminMap;
import me.neoblade298.neorogue.commands.CmdAdminMeta;
import me.neoblade298.neorogue.commands.CmdAdminMiniboss;
import me.neoblade298.neorogue.commands.CmdAdminNew;
import me.neoblade298.neorogue.commands.CmdAdminNode;
import me.neoblade298.neorogue.commands.CmdAdminPiece;
import me.neoblade298.neorogue.commands.CmdAdminPieceSettings;
import me.neoblade298.neorogue.commands.CmdAdminPoints;
import me.neoblade298.neorogue.commands.CmdAdminReload;
import me.neoblade298.neorogue.commands.CmdAdminReloadMythic;
import me.neoblade298.neorogue.commands.CmdAdminReset;
import me.neoblade298.neorogue.commands.CmdAdminResetInterstitials;
import me.neoblade298.neorogue.commands.CmdAdminRevokeAchievement;
import me.neoblade298.neorogue.commands.CmdAdminSerialize;
import me.neoblade298.neorogue.commands.CmdAdminSet;
import me.neoblade298.neorogue.commands.CmdAdminSetExp;
import me.neoblade298.neorogue.commands.CmdAdminSetInstance;
import me.neoblade298.neorogue.commands.CmdAdminSetLevel;
import me.neoblade298.neorogue.commands.CmdAdminSetNotoriety;
import me.neoblade298.neorogue.commands.CmdAdminSetPoints;
import me.neoblade298.neorogue.commands.CmdAdminStatus;
import me.neoblade298.neorogue.commands.CmdAdminTest;
import me.neoblade298.neorogue.commands.CmdAdminTestFW;
import me.neoblade298.neorogue.commands.CmdAdminTestHF;
import me.neoblade298.neorogue.commands.CmdAdminTestMW;
import me.neoblade298.neorogue.commands.CmdAdminTrash;
import me.neoblade298.neorogue.commands.CmdAdminUnlock;
import me.neoblade298.neorogue.commands.CmdAdminUnlocks;
import me.neoblade298.neorogue.commands.CmdCargo;
import me.neoblade298.neorogue.commands.CmdDecline;
import me.neoblade298.neorogue.commands.CmdGlossary;
import me.neoblade298.neorogue.commands.CmdHelp;
import me.neoblade298.neorogue.commands.CmdInfo;
import me.neoblade298.neorogue.commands.CmdJoin;
import me.neoblade298.neorogue.commands.CmdKick;
import me.neoblade298.neorogue.commands.CmdLeave;
import me.neoblade298.neorogue.commands.CmdList;
import me.neoblade298.neorogue.commands.CmdLoad;
import me.neoblade298.neorogue.commands.CmdLostCargo;
import me.neoblade298.neorogue.commands.CmdLyticsBalanceVersion;
import me.neoblade298.neorogue.commands.CmdLyticsBosses;
import me.neoblade298.neorogue.commands.CmdLyticsChance;
import me.neoblade298.neorogue.commands.CmdLyticsEquipment;
import me.neoblade298.neorogue.commands.CmdLyticsMinibosses;
import me.neoblade298.neorogue.commands.CmdLyticsMob;
import me.neoblade298.neorogue.commands.CmdLyticsMobs;
import me.neoblade298.neorogue.commands.CmdLyticsPickrate;
import me.neoblade298.neorogue.commands.CmdLyticsView;
import me.neoblade298.neorogue.commands.CmdMenu;
import me.neoblade298.neorogue.commands.CmdNew;
import me.neoblade298.neorogue.commands.CmdSpectate;
import me.neoblade298.neorogue.commands.EquipmentPresets;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.unlock.UnlockRegistry;
import me.neoblade298.neorogue.region.Region;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.analytics.AnalyticsManager;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.fight.Mob;
import me.neoblade298.neorogue.session.fight.mythicbukkit.MythicLoader;
import me.neoblade298.neorogue.session.instances.EditInventoryInstance;
import me.neoblade298.neorogue.session.instances.EditInventoryInstance.NodeMapRenderer;
import me.neoblade298.neorogue.session.instances.NodeSelectInstance;
import me.neoblade298.neorogue.session.reward.RunReward;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class NeoRogue extends JavaPlugin {
	private static NeoRogue inst;
	public static Random gen = new Random();
	public static BukkitAPIHelper mythicApi;
	public static MobManager mythicMobs;
	private static HashSet<String> debugFlags = new HashSet<>();
	
	public static File SCHEMATIC_FOLDER = new File("/home/mlmc/dev/plugins/FastAsyncWorldEdit/schematics");
	
	public static Location spawn;

	public static void main(String args[]) {
		// Use for debug
	}
	
	public void onEnable() {
		Bukkit.getServer().getLogger().info("NeoRogue Enabled");
		inst = this;
		saveResource("achievements.yml", false);
		NeoCore.registerIOComponent(this, new PlayerManager(), "NeoRogue-PlayerManager");
		AnalyticsManager.init();
		RunReward.setupEconomy();
		Bukkit.getPluginManager().registerEvents(new SessionManager(), this);
		Bukkit.getPluginManager().registerEvents(new MythicLoader(), this);
		reload();
		initCommands(); // Must load commands AFTER map pieces due to command suggestion
		new Placeholders().register();

		// Load map renderer for node map
		MapView map = Bukkit.getMap(EditInventoryInstance.MAP_ID);
		while (!map.getRenderers().isEmpty()) {
			MapRenderer rend = map.getRenderers().get(0);
			map.removeRenderer(rend);
		}
		map.addRenderer(new NodeMapRenderer());

		// Strictly for debug usage
		Player p = Bukkit.getPlayer("Ascheladd");
		Player alt = Bukkit.getPlayer("SuaveGentleman");
		Collection<Player> others = new ArrayList<Player>();
		if (alt != null) others.add(alt);
		if (p != null) debugInitialize(p, others, EquipmentClass.MAGE, RegionType.LOW_DISTRICT);
	}
	
	public static void reload() {
		mythicApi = MythicBukkit.inst().getAPIHelper();
		mythicMobs = MythicBukkit.inst().getMobManager();
		Region.initialize();
		Equipment.load();
		UnlockRegistry.reload();
		EquipmentPresets.reload();
		PlayerManager.initializeEquipmentDroptables();
		ChanceSet.load(); // Must load after equipment
		Mob.load(); // Load in mob types
		Map.load(); // Load in map pieces
		AchievementRewardRegistry.reload(); // Load achievement command rewards
		
		// Will need to add multiverse dependency if the world isn't first loaded
		spawn = new Location(Bukkit.getWorld(Region.WORLD_NAME), -250, 65, -250);
	}
	
	public void onDisable() {
		for (Session s : SessionManager.getSessions()) {
			s.cleanup(true);
		}
	    org.bukkit.Bukkit.getServer().getLogger().info("NeoRogue Disabled");
	    super.onDisable();
	}
	
	private void initCommands() {
		SubcommandManager mngr = new SubcommandManager("nr", "neorogue.general", NamedTextColor.DARK_RED, this);
		mngr.register(new CmdMenu("", "Open the main menu", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdHelp("help", "View command list", null, SubcommandRunner.BOTH));
		mngr.register(new CmdNew("new", "Create a new game", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdLoad("load", "Load an existing game", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdLeave("leave", "Leave your session", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdKick("kick", "Kick a player from your party", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdJoin("join", "Request to join a player's lobby", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAccept("accept", "Accept a player's request to join your lobby", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdDecline("decline", "Decline a player's request to join your lobby", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdSpectate("spectate", "Spectate a player's session", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdInfo("info", "View session info", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdList("list", "View a filtered list of equipment", null, SubcommandRunner.BOTH));
		mngr.register(new CmdGlossary("glossary", "View glossary", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAchievements("achievements", "View achievements", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdCargo("cargo", "Manage your cargo", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdLostCargo("lostcargo", "Withdraw unsold cargo from past runs", null, SubcommandRunner.PLAYER_ONLY));
		
		mngr = new SubcommandManager("nradmin", "neorogue.admin", NamedTextColor.DARK_RED, this);
		mngr.register(new CmdAdminReload("reload", "Reloads everything", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminReloadMythic("reloadmythic", "Reloads mythicmobs safely", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminDebug("debug", "Does various tests", null, SubcommandRunner.BOTH));
        mngr.register(new CmdAdminDebugMode("debugmode", "Toggle session debug mode for various tests", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminPiece("piece", "Pastes a map piece at 0,0 for ease of setting up spawners with coords", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminPieceSettings("piecesettings", "Pastes map piece to show how it looks rotated and flipped", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminMap("map", "Generates and pastes a map", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminChance("chance", "Tests a chance event", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminMiniboss("miniboss", "Tests a miniboss fight", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminEquipment("equip", "Gives the player an equipment", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminCoins("coins", "Gives the player coins", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminTrash("trash", "Opens up an admin trash inventory for the player", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminBoss("boss", "Tests a boss fight", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminGod("god", "Maxes out your health, mana, stamina, and ignores cooldowns in a fight", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminSet("set", "Set your stats mid-fight", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminStatus("status", "Add/remove statuses mid-fight, aim at mob to use on them", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminTestHF("testhf", "Loads in a harvest fields game", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminTestFW("testfw", "Loads in a frozen wastes game", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminTestMW("testmw", "Loads in a meadowood game", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminDamage("damage", "Deal damage mid-fight, aim at mob to use on them", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminSerialize("serialize", "Save a player's loadout for debug purposes", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminDeserialize("deserialize", "Loads in a player's loadout for debug purposes", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminNew("new", "Starts a new session with all online players with custom start parameters", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminSetInstance("setinstance", "Sets the current instance", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminNode("node", "Teleport to a specific node", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminTest("test", "Used for testing various things", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminDrop("drop", "Roll equipment from the droptable", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminDropArtifact("dropartifact", "Roll artifacts from the droptable", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminUnlock("unlock", "Manage player unlock nodes", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminUnlocks("unlocks", "List a player's unlock nodes", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminExp("exp", "Add exp to a player", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminBoost("boost", "Grant an exp boost to a player", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminGlobalBoost("globalboost", "Activate a server-wide exp boost", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminSetExp("setexp", "Set a player's exp", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminLevel("level", "Add levels to a player", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminSetLevel("setlevel", "Set a player's level", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminPoints("points", "Add unlock points to a player", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminSetPoints("setpoints", "Set a player's unlock points", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminSetNotoriety("setnotoriety", "Set a player's max notoriety level per class", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminReset("reset", "Reset all progress for a player", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminResetInterstitials("resetinterstitials", "Resets interstitial version-check blocks for initial plots", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminMeta("meta", "Set metadata on held equipment", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminAchievement("achievement", "Grant 1 mastery of an achievement to a player", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminRevokeAchievement("revokeachievement", "Revoke 1 mastery of an achievement from a player", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminFlag("flag", "Add or remove a player flag", null, SubcommandRunner.BOTH));
		mngr.registerCommandList("");

		mngr = new SubcommandManager("nrlytics", "neorogue.admin", NamedTextColor.DARK_RED, this);
		mngr.register(new CmdLyticsBalanceVersion("version", "View or set the balance version reports read from", null, SubcommandRunner.BOTH));
		mngr.register(new CmdLyticsEquipment("equipment", "View a single equipment's effectiveness", null, SubcommandRunner.BOTH));
		mngr.register(new CmdLyticsView("view", "Filterable analytics views (e.g. equipment damage)", null, SubcommandRunner.BOTH));
		mngr.register(new CmdLyticsPickrate("pickrate", "Equipment pickrate leaderboard", null, SubcommandRunner.BOTH));
		mngr.register(new CmdLyticsChance("chance", "Chance event pickrate leaderboard", null, SubcommandRunner.BOTH));
		mngr.register(new CmdLyticsMobs("mobs", "Mob damage leaderboard", null, SubcommandRunner.BOTH));
		mngr.register(new CmdLyticsMinibosses("minibosses", "Miniboss damage leaderboard", null, SubcommandRunner.BOTH));
		mngr.register(new CmdLyticsBosses("bosses", "Boss damage leaderboard", null, SubcommandRunner.BOTH));
		mngr.register(new CmdLyticsMob("mob", "Detailed mob damage and winrate report", null, SubcommandRunner.BOTH));
		mngr.registerCommandList("");
	}
	
	public static NeoRogue inst() {
		return inst;
	}
	
	public static boolean toggleDebugFlag(String flag) {
		if (!debugFlags.add(flag)) {
			debugFlags.remove(flag);
			return false;
		}
		return true;
	}
	
	public static boolean isDebugFlag(String flag) {
		return debugFlags.contains(flag);
	}
	
	public static void debugInitialize(Player host, @Nullable Collection<Player> others, EquipmentClass ec, RegionType regionType) {
		Session s = SessionManager.createSession(host, 1);
		s.generateRegion(regionType);
		s.addPlayer(host.getUniqueId(), ec);
		s.setNodesVisited(regionType == RegionType.FROZEN_WASTES ? 32 : regionType == RegionType.HARVEST_FIELDS ? 16 : 0);
		s.setRegionsCompleted(regionType == RegionType.FROZEN_WASTES ? 2 : regionType == RegionType.HARVEST_FIELDS ? 1 : 0);

		s.setNode(s.getRegion().getNodes()[0][2]);
		for (Player pl : others == null ? Bukkit.getOnlinePlayers() : others) {
			if (pl == host) continue;
			if (SessionManager.getSession(pl) == null) {
				s.addPlayer(pl.getUniqueId(), ec);
				SessionManager.addToSession(pl.getUniqueId(), s);
			}
		}
		
		// Required to have delay otherwise the startup save and auto-save happen simultaneously and conflict
		new BukkitRunnable() {
			public void run() {
				s.setInstance(NodeSelectInstance.create(s));
			}
		}.runTaskLater(NeoRogue.inst(), 1L);
	}

	public static TextDisplay createHologram(Location loc, Component text) {
		TextDisplay td = (TextDisplay) loc.getWorld().spawnEntity(loc, EntityType.TEXT_DISPLAY);
		td.text(text);
		Transformation trans = td.getTransformation();
		trans.getScale().set(1);
		td.setBillboard(Billboard.CENTER);
		td.setTransformation(trans);
		return td;
	}
	
	@EventHandler
	public static void onMythicReload(MythicReloadedEvent e) {
		Bukkit.getLogger().info("[NeoRogue] Reloaded mythicmobs");
		mythicApi = MythicBukkit.inst().getAPIHelper();
		mythicMobs = MythicBukkit.inst().getMobManager();
		Map.reloadMythicMobs();
	}
}
