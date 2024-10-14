package me.neoblade298.neorogue;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicReloadedEvent;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.commands.SubcommandManager;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.commands.CmdAdminBoss;
import me.neoblade298.neorogue.commands.CmdAdminChance;
import me.neoblade298.neorogue.commands.CmdAdminCoins;
import me.neoblade298.neorogue.commands.CmdAdminDebug;
import me.neoblade298.neorogue.commands.CmdAdminEquipment;
import me.neoblade298.neorogue.commands.CmdAdminGod;
import me.neoblade298.neorogue.commands.CmdAdminMap;
import me.neoblade298.neorogue.commands.CmdAdminMiniboss;
import me.neoblade298.neorogue.commands.CmdAdminPiece;
import me.neoblade298.neorogue.commands.CmdAdminPieceSettings;
import me.neoblade298.neorogue.commands.CmdAdminReload;
import me.neoblade298.neorogue.commands.CmdAdminReloadMythic;
import me.neoblade298.neorogue.commands.CmdAdminSet;
import me.neoblade298.neorogue.commands.CmdAdminStatus;
import me.neoblade298.neorogue.commands.CmdAdminTrash;
import me.neoblade298.neorogue.commands.CmdGlossary;
import me.neoblade298.neorogue.commands.CmdInfo;
import me.neoblade298.neorogue.commands.CmdInvite;
import me.neoblade298.neorogue.commands.CmdJoin;
import me.neoblade298.neorogue.commands.CmdKick;
import me.neoblade298.neorogue.commands.CmdLeave;
import me.neoblade298.neorogue.commands.CmdList;
import me.neoblade298.neorogue.commands.CmdLoad;
import me.neoblade298.neorogue.commands.CmdNew;
import me.neoblade298.neorogue.commands.CmdSpectate;
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
	private static final boolean DEBUGGER = true;
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
		new Placeholders().register();

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
		mngr.register(new CmdNew("new", "Create a new game", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdLoad("load", "Load an existing game", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdInvite("invite", "Invite a player to your party", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdLeave("leave", "Leave your session", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdKick("kick", "Kick a player from your party", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdJoin("join", "Join an existing party", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdSpectate("spectate", "Spectate a player's session", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdInfo("info", "View session info", null, SubcommandRunner.PLAYER_ONLY));
		mngr.register(new CmdList("list", "View a filtered list of equipment", null, SubcommandRunner.BOTH));
		mngr.register(new CmdGlossary("glossary", "View glossary", null, SubcommandRunner.PLAYER_ONLY));
		mngr.registerCommandList("");
		
		mngr = new SubcommandManager("nradmin", "neorogue.admin", NamedTextColor.DARK_RED, this);
		mngr.register(new CmdAdminReload("reload", "Reloads everything", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminReloadMythic("reloadmythic", "Reloads mythicmobs safely", null, SubcommandRunner.BOTH));
		mngr.register(new CmdAdminDebug("debug", "Testing", null, SubcommandRunner.BOTH));
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
		mngr.register(new CmdAdminStatus("status", "Add/remove statuses mid-fight", null, SubcommandRunner.PLAYER_ONLY));
		mngr.registerCommandList("");
	}
	
	public static NeoRogue inst() {
		return inst;
	}
	
	private void debugInitialize() {
		if (!DEBUGGER) return;
		Player p = Bukkit.getPlayer("Ascheladd");
		if (p == null) return;
		p.setMaximumNoDamageTicks(0);
		
		Session s = SessionManager.createSession(p, "test", 1);
		s.generateArea(AreaType.LOW_DISTRICT);
		s.addPlayer(p.getUniqueId(), EquipmentClass.ARCHER);
		SessionManager.addToSession(p.getUniqueId(), s);
		Player alt = Bukkit.getPlayer("SuaveGentleman");
		if (alt != null) {
			s.addPlayer(alt.getUniqueId(), EquipmentClass.ARCHER);
			SessionManager.addToSession(alt.getUniqueId(), s);
			alt.setMaximumNoDamageTicks(0);
		}
		s.getParty().get(p.getUniqueId()).addManaRegen(10);
		s.getParty().get(p.getUniqueId()).addStaminaRegen(10);
		s.setNode(s.getArea().getNodes()[0][2]);
		
		
		// Required to have delay otherwise the startup save and auto-save happen simultaneously and conflict
		new BukkitRunnable() {
			public void run() {
				s.setInstance(new NodeSelectInstance(s));
				// s.setInstance(new ChanceInstance());

				//Map map = Map.generate(AreaType.LOW_DISTRICT, 8);
				//map.instantiate(null, 0, 0);
			}
		}.runTaskLater(this, 1L);
	}
	
	public static Hologram createHologram(String name, Location loc, ArrayList<String> lines) {
		try {
			return DHAPI.createHologram(name, loc, lines);
		}
		catch (IllegalArgumentException e) {
			DHAPI.removeHologram(name);
			return DHAPI.createHologram(name, loc, lines);
		}
	}
	
	@EventHandler
	public static void onMythicReload(MythicReloadedEvent e) {
		Bukkit.getLogger().info("[NeoRogue] Reloaded mythicmobs");
		mythicApi = MythicBukkit.inst().getAPIHelper();
		mythicMobs = MythicBukkit.inst().getMobManager();
		Map.reloadMythicMobs();
	}
}
