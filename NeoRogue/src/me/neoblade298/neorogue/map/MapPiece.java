package me.neoblade298.neorogue.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

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
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.session.ClipboardHolder;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.shared.exceptions.NeoIOException;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.Area;

public class MapPiece {
	private String id;
	private MapShape shape;
	private Coordinates[] entrances;
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
