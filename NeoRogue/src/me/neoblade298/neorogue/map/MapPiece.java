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
	private MapEntrance[] entrances;
	private MapSpawner[] spawners;
	private ClipboardHolder schematic;
	private AffineTransform transform;
	private int[] rotateOffset, flipOffset;
	
	private int numRotations;
	private boolean flipX, flipY;
	
	public MapPiece(ConfigurationSection cfg) {
		id = cfg.getName();

		// Save schematic
		File file = new File(NeoRogue.SCHEMATIC_FOLDER, cfg.getString("schematic") + ".schem");
		ClipboardFormat format = ClipboardFormats.findByFile(file);
		try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
			schematic = new ClipboardHolder(reader.read());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		shape = new MapShape(cfg.getStringList("shape"));
		
		List<String> entrances = cfg.getStringList("entrances");
		this.entrances = new MapEntrance[entrances.size()];
		int i = 0;
		for (String line : entrances) {
			this.entrances[i++] = new MapEntrance(this, line);
		}
		
		ConfigurationSection sec = cfg.getConfigurationSection("spawners");
		this.spawners = new MapSpawner[sec.getKeys(false).size()];
		i = 0;
		for (String key : sec.getKeys(false)) {
			this.spawners[i++] = new MapSpawner(sec.getConfigurationSection(key), this);
		}
		
		rotateOffset = new int[] {0, 0};
		flipOffset = new int[] {0, 0};
		transform = new AffineTransform();
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
	
	public MapEntrance[] getEntrances() {
		return entrances;
	}
	
	public MapSpawner[] getSpawners() {
		return spawners;
	}
	
	public void rotate(int amount) {
		int origRotations = numRotations;
		numRotations = (numRotations + amount) % 4;
		for (MapEntrance ent : entrances) {
			ent.rotate(amount);
		}
		if (spawners != null) {
			for (MapSpawner spawner : spawners) {
				spawner.rotate(amount);
			}
		}
		schematic.setTransform(transform.rotateY(90 * numRotations));

		for (int i = origRotations; i < origRotations + amount; i++) {
			int j = (i + 1) % 4;
			// +1 because the rotation is a bit off if using bottom left corner
			switch (j) {
			case 1: rotateOffset[0] -= shape.getLength() * 16 + 1;
			break;
			case 2: rotateOffset[1] += shape.getLength() * 16 + 1;
			break;
			case 3: rotateOffset[0] += shape.getLength() * 16 + 1;
			break;
			default: rotateOffset[1] -= shape.getLength() * 16 + 1;
			}
		}
	}
	
	public void flip(boolean xAxis) {
		for (MapEntrance ent : entrances) {
			ent.flip(xAxis);
		}
		if (spawners != null) {
			for (MapSpawner spawner : spawners) {
				spawner.flip(xAxis);
			}
		}
		if ((flipY && !flipX && xAxis) || (flipX && !flipY && !xAxis)) {
			flipX = false;
			flipY = false;
			rotateOffset[0] = 0;
			rotateOffset[1] = 0;
			schematic.setTransform(transform.scale(BukkitAdapter.asVector(new Location(Bukkit.getWorld(Area.WORLD_NAME), 1, 1, 1))));
			rotate(2); // A double flip is just a 180 rotation
			return;
		}
		BlockVector3 direction = BukkitAdapter.asBlockVector(new Location(Bukkit.getWorld(Area.WORLD_NAME), xAxis ? 1 : 0, 0, xAxis ? 0 : 1));
		schematic.setTransform(transform.scale(direction.abs().multiply(-2).add(1, 1, 1).toVector3()));
		
		if (xAxis) {
			flipX = !flipX;
			flipOffset[0] = flipX ? -shape.getLength() * 16 - 1 : 0;
		}
		else {
			flipY = !flipY;
			flipOffset[1] = flipY ? shape.getLength() * 16 + 1 : 0;
		}
	}
	
	public ClipboardHolder getClipboard() {
		return schematic;
	}
	
	public MapPieceSettings[] getRotationOptions(MapEntrance existing, MapEntrance toAttach) {
		MapPieceSettings[] settings = new MapPieceSettings[2];
		rotateToFace(existing, toAttach);
		settings[0] = getSettings(existing, toAttach);
		flipOppositeAxis(toAttach.getFace());
		settings[1] = getSettings(existing, toAttach);
		return settings;
	}
	
	public void rotateToFace(MapEntrance existing, MapEntrance toAttach) {
		int amount = (existing.getFace().getValue() - toAttach.getFace().getValue()) % 4;
		if (amount < 0) amount += 4;
		rotate(amount);
	}
	
	public void flipOppositeAxis(Direction dir) {
		flip(dir == Direction.NORTH || dir == Direction.SOUTH);
	}
	
	public MapPieceSettings getSettings(MapEntrance available, MapEntrance toAttach) {
		return new MapPieceSettings(numRotations, flipX, flipY, available, toAttach);
	}
	
	public void applySettings(MapPieceSettings settings) {
		while (numRotations != settings.getNumRotations()) {
			rotate(1);
		}
		
		if (flipX != settings.isFlipX()) flip(true);
		if (flipY != settings.isFlipY()) flip(false);
	}
	
	public void paste(int x, int y) {
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(Area.world)) {
		    Operation operation = schematic.createPaste(editSession)
		            .to(BlockVector3.at(x + rotateOffset[0] + flipOffset[0] + 16, 64, y + rotateOffset[1] + flipOffset[1])) // Paste by default goes top left, need top right
		            .ignoreAirBlocks(true)
		            .build();
		    // CuboidRegion o = new CuboidRegion(null, null);
		    // Mask mask = new ExistingBlockMask(editSession);
		    try {
				Operations.complete(operation);
			    // editSession.replaceBlocks(o, mask, BukkitAdapter.adapt(Material.AIR.createBlockData()));
			} catch (WorldEditException e) {
				e.printStackTrace();
			}
		}
	}
}
