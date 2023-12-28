package me.neoblade298.neorogue.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;

import me.neoblade298.neocore.shared.io.Section;
import me.neoblade298.neorogue.NeoRogue;

public class MapPiece {

	private static HashMap<String, MapPiece> pieces = new HashMap<String, MapPiece>();
	private String id;
	private MapShape shape;
	private HashSet<String> targets;
	protected Coordinates[] entrances, spawns;
	protected HashMap<String, Coordinates> mythicLocations = new HashMap<String, Coordinates>();
	private ArrayList<MapSpawner[]> spawnerSets = new ArrayList<MapSpawner[]>();
	protected Clipboard clipboard;
	
	public MapPiece(Section cfg) {
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
			Coordinates entrance = new Coordinates(this, line);
			Direction invert = entrance.getOriginalDirection().invert();
			entrance.setOriginalDirection(invert);
			entrance.setDirection(invert);
			this.entrances[i++] = entrance;
		}
		
		Section sec = cfg.getSection("spawnersets");
		for (String key : sec.getKeys()) {
			Section spawnerSets = sec.getSection(key);
			MapSpawner[] spawners = new MapSpawner[spawnerSets.getKeys().size()];
			i = 0;
			for (String spawnerKey : spawnerSets.getKeys()) {
				spawners[i++] = new MapSpawner(spawnerSets.getSection(spawnerKey), this);
			}
			this.spawnerSets.add(spawners);
		}
		
		List<String> spawns = cfg.getStringList("spawns");
		this.spawns = new Coordinates[spawns.size()];
		i = 0;
		for (String line : spawns) {
			this.spawns[i] = new Coordinates(this, line, true);
			i++;
		}

		sec = cfg.getSection("mythiclocations");
		if (sec != null) {
			for (String key : sec.getKeys()) {
				mythicLocations.put(key, new Coordinates(this, sec.getString(key), true));
			}
		}
		
		List<String> targets = cfg.getStringList("targets");
		if (targets != null) {
			this.targets = new HashSet<String>();
			this.targets.addAll(targets);
		}
		
		pieces.put(id, this);
	}
	
	public static MapPiece get(String id) {
		return pieces.get(id);
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
	
	public ArrayList<MapSpawner[]> getSpawnerSets() {
		return spawnerSets;
	}
	
	// Pick between the different sets of spawners
	public MapSpawner[] chooseSpawners() {
		return spawnerSets.get(NeoRogue.gen.nextInt(spawnerSets.size()));
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
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MapPiece other = (MapPiece) obj;
		if (id == null) {
			if (other.id != null) return false;
		}
		else if (!id.equals(other.id)) return false;
		return true;
	}
}
