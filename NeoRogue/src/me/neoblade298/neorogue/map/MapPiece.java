package me.neoblade298.neorogue.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import me.neoblade298.neorogue.NeoRogue;

public class MapPiece {
	private String id;
	private MapShape shape;
	private HashSet<String> targets;
	protected Coordinates[] entrances, spawns;
	private MapSpawner[] spawners;
	protected Clipboard clipboard;
	
	public MapPiece(ConfigurationSection cfg) {
		id = cfg.getName();

		// Save schematic
		File file = new File(NeoRogue.SCHEMATIC_FOLDER, cfg.getString("schematic") + ".schem");
		ClipboardFormat format = ClipboardFormats.findByFile(file);
		try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
			clipboard = reader.read();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		shape = new MapShape(cfg.getStringList("shape"));
		
		List<String> entrances = cfg.getStringList("entrances");
		this.entrances = new Coordinates[entrances.size()];
		int i = 0;
		for (String line : entrances) {
			this.entrances[i++] = new Coordinates(this, line);
		}
		
		ConfigurationSection sec = cfg.getConfigurationSection("spawners");
		this.spawners = new MapSpawner[sec.getKeys(false).size()];
		i = 0;
		for (String key : sec.getKeys(false)) {
			this.spawners[i++] = new MapSpawner(sec.getConfigurationSection(key), this);
		}
		
		List<String> spawns = cfg.getStringList("spawns");
		this.spawns = new Coordinates[spawns.size()];
		i = 0;
		for (String line : spawns) {
			this.spawns[i++] = new Coordinates(this, line, true);
		}
		
		List<String> targets = cfg.getStringList("targets");
		if (targets != null) {
			this.targets = new HashSet<String>();
			this.targets.addAll(targets);
		}
	}
	
	public HashSet<String> getTargets() {
		return targets;
	}
	
	public int getNumEntrances() {
		return entrances.length;
	}
	
	public MapShape getShape() {
		return shape;
	}
	
	public String getId() {
		return id;
	}
	
	public Coordinates[] getEntrances() {
		return entrances;
	}
	
	public MapSpawner[] getSpawners() {
		return spawners;
	}
	
	public MapPieceInstance[] getRotationOptions(Coordinates existing, Coordinates toAttach) {
		MapPieceInstance[] settings = new MapPieceInstance[] {
				getInstance(existing, toAttach),
				getInstance(existing, toAttach)
		};
		settings[0].rotateToFace(existing, toAttach);
		settings[1].rotateToFace(existing, toAttach);
		settings[1].flipOppositeAxis();
		return settings;
	}
	
	public MapPieceInstance getInstance(Coordinates available, Coordinates toAttach) {
		return new MapPieceInstance(this, available, toAttach);
	}
	
	public MapPieceInstance getInstance() {
		return new MapPieceInstance(this);
	}
}
