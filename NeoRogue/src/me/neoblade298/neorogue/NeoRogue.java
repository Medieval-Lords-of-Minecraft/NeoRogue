package me.neoblade298.neorogue;

import java.io.File;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.internal.HolographicDisplaysAPIProvider;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.area.Node;

public class NeoRogue extends JavaPlugin {
	private static NeoRogue inst;
	
	public static File SCHEMATIC_FOLDER;
	public static HolographicDisplaysAPI holo;
	
	public static void main(String[] args) {
		Area area = new Area(AreaType.ARGENT_PLAZA, 0, 0);
		
		Scanner scan = new Scanner(System.in);
		while (true) {
			area.print();
			System.out.println("Choose pos, lane");
			int pos = scan.nextInt();
			int lane = scan.nextInt();
			if (pos < 0 || lane < 0) break;
			
			Node node = area.getNodes()[pos][lane];
			if (node == null) {
				System.out.println("Null node. Try again.");
				continue;
			}
			
			System.out.println("Destinations: ");
			for (Node dest : node.getDestinations()) {
				System.out.println(dest.getPosition() + "," + dest.getLane());
			}
		}
		System.out.println("Closed.");
		scan.close();
	}
	
	public void onEnable() {
		Bukkit.getServer().getLogger().info("NeoRogue Enabled");
		// Bukkit.getPluginManager().registerEvents(this, this);
		initCommands();
		
		inst = this;
		
		holo = HolographicDisplaysAPIProvider.getImplementation().getHolographicDisplaysAPI(this);
		SCHEMATIC_FOLDER = new File("/home/MLMC/ServerRPG/plugins/WorldEdit/schematics");
		Area area = new Area(AreaType.HARVEST_FIELDS, 0, 0);
		area.generate();
		area.update(area.getNodes()[1][2]);
		
		new BukkitRunnable() {
			int count = 0; // Strictly for debug usage
			public void run() {
				area.tickParticles(Bukkit.getPlayer("Ascheladd"), area.getNodes()[1][2]);
				count++;
				if (count > 20) {
					this.cancel();
				}
			}
		}.runTaskTimer(this, 0L, 20L);
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
}
