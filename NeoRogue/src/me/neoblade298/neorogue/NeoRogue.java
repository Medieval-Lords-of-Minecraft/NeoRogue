package me.neoblade298.neorogue;

import java.io.File;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.commands.SubcommandManager;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.commands.*;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.player.PlayerClass;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.fight.Mob;
import me.neoblade298.neorogue.session.fight.StandardFightInstance;
import me.neoblade298.neorogue.session.fight.mythicbukkit.MythicLoader;
import net.kyori.adventure.text.format.NamedTextColor;

public class NeoRogue extends JavaPlugin {
	private static NeoRogue inst;
	public static Random gen = new Random();
	
	public static File SCHEMATIC_FOLDER = new File("/home/MLMC/Dev/plugins/WorldEdit/schematics");
	
	public static Location spawn;
	
	public void onEnable() {
		Bukkit.getServer().getLogger().info("NeoRogue Enabled");
		inst = this;
		NeoCore.registerIOComponent(this, new PlayerManager(), "NeoRogue-PlayerManager");
		Bukkit.getPluginManager().registerEvents(new SessionManager(), this);
		Bukkit.getPluginManager().registerEvents(new MythicLoader(), this);
		Area.initialize();
		ChanceSet.load();
		Mob.load(); // Load in mob types
		Map.load(); // Load in map pieces
		Equipment.load();
		initCommands(); // Must load commands AFTER map pieces due to command suggestion
		
		// Will need to add multiverse dependency if the world isn't first loaded
		spawn = new Location(Bukkit.getWorld(Area.WORLD_NAME), -250, 65, -250);

		
		// Strictly for debug usage
		debugInitialize();
		
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
		mngr.register(new CmdStart("start", "Start the game with your party", null, SubcommandRunner.BOTH));
		mngr.register(new CmdJoin("join", "Join an existing party", null, SubcommandRunner.BOTH));
		mngr.register(new CmdInfo("info", "View session info", null, SubcommandRunner.BOTH));
		mngr.registerCommandList("");
		
		mngr = new SubcommandManager("nradmin", "neorogue.admin", NamedTextColor.DARK_RED, this);
		mngr.register(new CmdAdminDebug("debug", "Testing", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminTestPiece("testpiece", "Pastes a map piece at 0,0 for ease of setting up spawners with coords", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminTestPieceSettings("testpiecesettings", "Pastes map piece to show how it looks rotated and flipped", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdAdminTestMap("testmap", "Generates and pastes a map", null, SubcommandRunner.PLAYER_ONLY));
		mngr.registerCommandList("");
	}
	
	public static NeoRogue inst() {
		return inst;
	}
	
	private void debugInitialize() {
		Player p = Bukkit.getPlayer("Ascheladd");
		Session s = SessionManager.createSession(p, "test", 1);
		s.generateArea(AreaType.LOW_DISTRICT);
		s.addPlayer(p.getUniqueId(), PlayerClass.WARRIOR);
		s.setInstance(new StandardFightInstance(s.getParty().keySet(), AreaType.LOW_DISTRICT, s.getNodesVisited()));
		// s.setInstance(new ChanceInstance());

		//Map map = Map.generate(AreaType.LOW_DISTRICT, 8);
		//map.instantiate(null, 0, 0);
	}
}
