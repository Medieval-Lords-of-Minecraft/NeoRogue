package me.neoblade298.neorogue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;

import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.internal.HolographicDisplaysAPIProvider;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.commands.SubcommandManager;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.commands.CmdAdminDebug;
import me.neoblade298.neorogue.map.Direction;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.map.MapPiece;
import me.neoblade298.neorogue.map.MapPieceInstance;
import me.neoblade298.neorogue.map.MapShape;
import me.neoblade298.neorogue.map.Coordinates;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.fights.FightInstance;
import net.md_5.bungee.api.ChatColor;

public class NeoRogue extends JavaPlugin {
	private static NeoRogue inst;
	
	public static File SCHEMATIC_FOLDER = new File("/home/MLMC/DevServers/ServerDev/plugins/WorldEdit/schematics");
	public static HolographicDisplaysAPI holo;
	
	public static void main(String[] args) {
		boolean[][] shape = { {true, true}, {false, true}};
		ArrayList<String> list = new ArrayList<String>();
		list.add("XX");
		list.add("_X");
		MapShape ms = new MapShape(list);
		ms.setRotations(1);
		ms.setFlip(true, false);
		ms.display();
	}
	
	public void onEnable() {
		Bukkit.getServer().getLogger().info("NeoRogue Enabled");
		NeoCore.registerIOComponent(this, new PlayerManager(), "NeoRogue-PlayerManager");
		Bukkit.getPluginManager().registerEvents(new SessionManager(), this);
		initCommands();
		Area.initialize();
		
		inst = this;

		Map.load(); // Load in map pieces
		
		holo = HolographicDisplaysAPIProvider.getImplementation().getHolographicDisplaysAPI(this);
		
		// Strictly for debug usage
		debugInitialize();
	}
	
	public void onDisable() {
		holo.deleteHolograms();
	    org.bukkit.Bukkit.getServer().getLogger().info("NeoRogue Disabled");
	    super.onDisable();
	}
	
	private void initCommands() {
		SubcommandManager mngr = new SubcommandManager("nradmin", "neorogue.admin", ChatColor.DARK_RED, this);
		mngr.register(new CmdAdminDebug("debug", "Testing", null, SubcommandRunner.BOTH));
	}
	
	public static NeoRogue inst() {
		return inst;
	}
	
	private void debugInitialize() {
		//Player p = Bukkit.getPlayer("Ascheladd");
		//Session s = SessionManager.createSession(p);
		//s.setInstance(new FightInstance(s));
		//s.getInstance().start(s);

		Map map = Map.generate(AreaType.LOW_DISTRICT, 4);
		map.instantiate(null, 0, 0);
		LinkedList<MapPiece> pieces = Map.getPieces(AreaType.LOW_DISTRICT);
		//MapPieceInstance inst = pieces.get(0).getInstance();
		//inst.setRotations(1);
		//inst.instantiate(null, 0, 0);
		
		/*
		new BukkitRunnable() {
			int count = 0; 
			public void run() {
				count++;
				if (count > 20) {
					this.cancel();
				}
				if (p == null) return;

				// area.tickParticles(p, area.getNodes()[1][2]);
			}
		}.runTaskTimer(this, 0L, 20L);
		*/
	}
}
