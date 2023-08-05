package me.neoblade298.neorogue.map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;

import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.session.fights.FightInstance;

public class MapPieceInstance {
	private MapPiece piece;
	private RotatableCoordinates entrance;
	protected int numRotations;
	private int x, z; // In chunk offset
	protected boolean flipX, flipY;
	private ClipboardHolder schematic;
	
	private int[] rotateOffset = new int[] {0, 0},
			flipOffset = new int[] {0, 0};
	
	protected MapPieceInstance(MapPiece piece) {
		schematic = new ClipboardHolder(piece.clipboard);
	}
	
	protected MapPieceInstance(MapPiece piece, RotatableCoordinates available, RotatableCoordinates toAttach) {
		this(piece);
		this.entrance = toAttach.clone();
	}
	
	public int getNumRotations() {
		return numRotations;
	}
	public boolean isFlipX() {
		return flipX;
	}
	public boolean isFlipY() {
		return flipY;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (flipX ? 1231 : 1237);
		result = prime * result + (flipY ? 1231 : 1237);
		result = prime * result + numRotations;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MapPieceInstance other = (MapPieceInstance) obj;
		if (flipX != other.flipX) return false;
		if (flipY != other.flipY) return false;
		if (numRotations != other.numRotations) return false;
		return true;
	}
	
	public int getX() {
		return x;
	}
	
	public int getZ() {
		return z;
	}
	
	public void rotate(int amount) {
		numRotations = (numRotations + amount) % 4;
		MapShape shape = piece.getShape();
		
		int lenOff = (numRotations % 2 == 0 ? shape.getLength() : shape.getHeight()) * 16 - 1;
		int heightOff = (numRotations % 2 == 0 ? shape.getHeight() : shape.getLength()) * 16 - 1;

		switch (numRotations) {
		case 0: rotateOffset[0] = 0;
		rotateOffset[1] = 0;
		break;
		case 1: rotateOffset[1] = heightOff;
		rotateOffset[0] = 0;
		break;
		case 2: rotateOffset[0] = -lenOff;
		rotateOffset[1] = heightOff;
		break;
		case 3: rotateOffset[0] = -lenOff;
		rotateOffset[1] = 0;
		break;
		}
	}
	
	public void flip(boolean xAxis) {
		if ((flipY && !flipX && xAxis) || (flipX && !flipY && !xAxis)) {
			flipX = false;
			flipY = false;
			rotate(2); // A double flip is just a 180 rotation
			return;
		}
		
		if (xAxis) {
			flipX = !flipX;
		}
		else {
			flipY = !flipY;
		}
	}
	
	private void updateSchematic() {
		AffineTransform transform = new AffineTransform();
		// It is only possible for one of these to be true at a time
		int len = piece.getShape().getLength() * 16 -1;
		if (flipX || flipY) {
			flipOffset[0] = flipX ? -len - rotateOffset[0] : 0;
			flipOffset[1] = flipY ? len - rotateOffset[1] : 0;
			BlockVector3 direction = BukkitAdapter.asBlockVector(new Location(Bukkit.getWorld(Area.WORLD_NAME), flipX ? 1 : 0, 0, flipY ? 1 : 0));
			transform = transform.scale(direction.abs().multiply(-2).add(1, 1, 1).toVector3());
			schematic.setTransform(transform);
		}
		else {
			flipOffset[0] = 0;
			flipOffset[1] = 0;
		}
		transform = transform.rotateY(numRotations * -90);
		schematic.setTransform(transform);
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setZ(int z) {
		this.z = z;
	}
	
	public MapPiece getPiece() {
		return piece;
	}
	
	public void rotateToFace(RotatableCoordinates existing, RotatableCoordinates toAttach) {
		int amount = (existing.getDirection().getValue() - toAttach.getDirection().getValue()) % 4;
		if (amount < 0) amount += 4;
		rotate(amount);
	}
	
	public void flipOppositeAxis() {
		flip(entrance.getDirection() == Direction.NORTH || entrance.getDirection() == Direction.SOUTH);
	}
	
	public int[] calculateOffset(RotatableCoordinates available) {
		entrance.applySettings(this);
		return new int[] { available.getX() - entrance.getX(), available.getZ() - entrance.getZ() };
	}
	
	// Pastes the map piece and sets up its spawners
	public void instantiate(FightInstance inst) {
		updateSchematic();
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(Area.world)) {
		    Operation operation = schematic.createPaste(editSession)
		            .to(BlockVector3.at((x * 16) + rotateOffset[0] + flipOffset[0], 64,
		            		(z * 16) + rotateOffset[1] + flipOffset[1])) // Paste by default goes top left, need top right
		            .ignoreAirBlocks(true)
		            .build();
		    System.out.println("===");
		    System.out.println(rotateOffset[0] + " " + rotateOffset[1]);
		    System.out.println(flipOffset[0] + " " + flipOffset[1]);
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
