package me.neoblade298.neorogue;

import java.io.File;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicReloadedEvent;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.commands.SubcommandManager;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.commands.*;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.session.NodeSelectInstance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.fight.Mob;
import me.neoblade298.neorogue.session.fight.mythicbukkit.MythicLoader;
import net.kyori.adventure.text.format.NamedTextColor;

public class NeoRogue extends JavaPlugin {
	private static NeoRogue inst;
	public static Random gen = new Random();
	public static BukkitAPIHelper mythicApi;
	public static MobManager mythicMobs;
	
	public static File SCHEMATIC_FOLDER = new File("/home/MLMC/Dev/plugins/WorldEdit/schematics");
	
	public static Location spawn;
	
	public void onEnable() {
		Bukkit.getServer().getLogger().info("NeoRogue Enabled");
		inst = this;
		NeoCore.registerIOComponent(this, new PlayerManager(), "NeoRogue-PlayerManager");
		Bukkit.getPluginManager().registerEvents(new SessionManager(), this);
		Bukkit.getPluginManager().registerEvents(new MythicLoader(), this);
		reload();
		initCommands(); // Must load commands AFTER map pieces due to command suggestion

		// Strictly for debug usage
		debugInitialize();
		
	}
	
	public static void reload() {
		mythicApi = MythicBukkit.inst().getAPIHelper();
		mythicMobs = MythicBukkit.inst().getMobManager();
		Area.initialize();
		Equipment.load();
		ChanceSet.load(); // Must load after equipment
		Mob.load(); // Load in mob types
		Map.load(); // Load in map pieces
		
		// Will need to add multiverse dependency if the world isn't first loaded
		spawn = new Location(Bukkit.getWorld(Area.WORLD_NAME), -250, 65, -250);
	}
	
	public void onDisable() {
		for (Session s : SessionManager.getSessions()) {
			s.cleanup();
		}
	    org.bukkit.Bukkit.getServer().getLogger().info("NeoRogue Disabled");
	    super.onDisable();
	}
	
	private void initCommands() {
		SubcommandManager mngr = new SubcommandManager("nr", "neorogue.general", NamedTextColor.DARK_RED, this);
		mngr.register(new CmdNew("new", "Create a new game", null, SubcommandRunner.BOTH));
		mngr.register(new CmdLoad("load", "Load an existing game", null, SubcommandRunner.BOTH));
		mngr.register(new CmdInvite("invite", "Invite a player to your party", null, SubcommandRunner.BOTH));
		mngr.register(new CmdLeave("leave", "Leave your session", null, SubcommandRunner.BOTH));
		mngr.register(new CmdKick("kick", "Kick a player from your party", null, SubcommandRunner.BOTH));
		mngr.register(new CmdJoin("join", "Join an existing party", null, SubcommandRunner.BOTH));
		mngr.register(new CmdInfo("info", "View session info", null, SubcommandRunner.BOTH));
		mngr.registerCommandList("");
		
		mngr = new SubcommandManager("nradmin", "neorogue.admin", NamedTextColor.DARK_RED, this);
		mngr.register(new CmdAdminReload("reload", "Reloads everything", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminReload("reloadmythic", "Reloads mythicmobs safely", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminDebug("debug", "Testing", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminTestPiece("testpiece", "Pastes a map piece at 0,0 for ease of setting up spawners with coords", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminTestPieceSettings("testpiecesettings", "Pastes map piece to show how it looks rotated and flipped", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminTestMap("testmap", "Generates and pastes a map", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminTestChance("testchance", "Tests a chance event", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminTestMiniboss("testminiboss", "Tests a miniboss fight", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminTestBoss("testboss", "Tests a boss fight", null, SubcommandRunner.PLAYER_ONLY));
		mngr.registerCommandList("");
	}
	
	public static NeoRogue inst() {
		return inst;
	}
	
	private void debugInitialize() {
		Player p = Bukkit.getPlayer("Ascheladd");
		if (p == null) return;
		
		Session s = SessionManager.createSession(p, "test", 1);
		s.generateArea(AreaType.LOW_DISTRICT);
		s.addPlayer(p.getUniqueId(), EquipmentClass.WARRIOR);
		SessionManager.addToSession(p.getUniqueId(), s);
		Player alt = Bukkit.getPlayer("SuaveGentleman");
		if (alt != null) {
			s.addPlayer(alt.getUniqueId(), EquipmentClass.WARRIOR);
			SessionManager.addToSession(alt.getUniqueId(), s);
		}
		s.setNode(s.getArea().getNodes()[0][2]);
		s.setInstance(new NodeSelectInstance(s));
		// s.setInstance(new ChanceInstance());

		//Map map = Map.generate(AreaType.LOW_DISTRICT, 8);
		//map.instantiate(null, 0, 0);
	}
	
	@EventHandler
	public static void onMythicReload(MythicReloadedEvent e) {
		Bukkit.getLogger().info("[NeoRogue] Reloaded mythicmobs");
		mythicApi = MythicBukkit.inst().getAPIHelper();
		mythicMobs = MythicBukkit.inst().getMobManager();
		Map.reloadMythicMobs();
	}
}
