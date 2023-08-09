package me.neoblade298.neorogue;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.internal.HolographicDisplaysAPIProvider;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.map.Direction;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.map.MapPiece;
import me.neoblade298.neorogue.map.MapShape;
import me.neoblade298.neorogue.map.Coordinates;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.fights.FightInstance;

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
		// SubcommandManager mngr = new SubcommandManager("disenchant", "disenchant.use", ChatColor.RED, this);
		// mngr.register(new CmdDisenchant("disenchant", "Disenchants the item", null, SubcommandRunner.PLAYER_ONLY));
	}
	
	public static NeoRogue inst() {
		return inst;
	}
	
	private void debugInitialize() {
		//Player p = Bukkit.getPlayer("Ascheladd");
		//Session s = SessionManager.createSession(p);
		//s.setInstance(new FightInstance(s));
		//s.getInstance().start(s);

		Map.generate(AreaType.LOW_DISTRICT, 4);
		
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
