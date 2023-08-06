package me.neoblade298.neorogue.map;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.session.fights.FightInstance;

public class MapPieceInstance implements Comparable<MapPieceInstance> {
	private MapPiece piece;
	private RotatableCoordinates entrance;
	protected int numRotations;
	private int x, y, z; // In chunk offset
	protected boolean flipX, flipZ;
	private ClipboardHolder schematic;
	private static final int Y_OFFSET = 64;
	private static final int Z_FIGHT_OFFSET = 36;
	private int potential = 100;
	private int[] rotateOffset = new int[] {0, 0},
			flipOffset = new int[] {0, 0};
	
	protected MapPieceInstance(MapPiece piece) {
		this.piece = piece;
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
	public boolean isFlipZ() {
		return flipZ;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (flipX ? 1231 : 1237);
		result = prime * result + (flipZ ? 1231 : 1237);
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
		if (flipZ != other.flipZ) return false;
		if (numRotations != other.numRotations) return false;
		return true;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	public void setRotations(int amount) {
		numRotations = amount % 4;
		MapShape shape = piece.getShape();
		
		int lenOff = (numRotations % 2 == 0 ? shape.getLength() : shape.getHeight()) * 16 - 1;
		int heightOff = (numRotations % 2 == 0 ? shape.getHeight() : shape.getLength()) * 16 - 1;

		if (entrance != null) entrance.setRotations(numRotations);
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
		if ((flipZ && !flipX && xAxis) || (flipX && !flipZ && !xAxis)) {
			flipX = false;
			flipZ = false;
			setRotations((numRotations + 2) % 4); // A double flip is just a 180 rotation
			return;
		}
		
		if (xAxis) {
			flipX = !flipX;
		}
		else {
			flipZ = !flipZ;
		}
		if (entrance != null) {
			entrance.setFlipX(flipX);
			entrance.setFlipZ(flipZ);
		}
	}
	
	private void updateSchematic() {
		AffineTransform transform = new AffineTransform();
		// It is only possible for one of these to be true at a time
		int len = piece.getShape().getLength() * 16 -1;
		if (flipX || flipZ) {
			flipOffset[0] = flipX ? -len - rotateOffset[0] : 0;
			flipOffset[1] = flipZ ? len - rotateOffset[1] : 0;
			BlockVector3 direction = BlockVector3.at(flipX ? 1 : 0, 0, flipZ ? 1 : 0);
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
	
	public void setY(int y) {
		this.y = y;
	}
	
	public void setZ(int z) {
		this.z = z;
	}
	
	public MapPiece getPiece() {
		return piece;
	}
	
	public void rotateToFace(RotatableCoordinates existing, RotatableCoordinates toAttach) {
		int amount = (existing.getDirection().getValue() - toAttach.getOriginalDirection().getValue() + 6) % 4;
		setRotations(amount);
	}
	
	public void flipOppositeAxis() {
		flip(entrance.getDirection() == Direction.EAST || entrance.getDirection() == Direction.WEST);
	}
	
	public int[] calculateOffset(RotatableCoordinates available) {
		entrance.applySettings(this);
		return new int[] { available.getXFacing() - entrance.getX(), available.getY() - entrance.getY(), available.getZFacing() - entrance.getZ() };
	}
	
	public int getPotential() {
		return potential;
	}
	
	public void setPotential(int potential) {
		this.potential = potential;
	}
	
	// Pastes the map piece and sets up its spawners
	public void instantiate(FightInstance inst, int xOff, int zOff) {
		updateSchematic();
		/*
		 * this.x is the chunk coordinates within the fighting area
		 * xOff is the offset of the plot
		 * Z_FIGHT_OFFSET is the offset of where the fighting area is in the plot
		 * x is negative because south is +z and right of south is -x
		 */
		int x = (this.x * 16) + rotateOffset[0] + flipOffset[0] + xOff;
		int z = (this.z * 16) + rotateOffset[1] + flipOffset[1] + zOff + Z_FIGHT_OFFSET;
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(Area.world)) {
		    Operation operation = schematic.createPaste(editSession)
		            .to(BlockVector3.at(-x, Y_OFFSET + this.y, z))
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
	
	public RotatableCoordinates getEntrance() {
		return entrance;
	}

	@Override
	public int compareTo(MapPieceInstance o) {
		int comp = this.potential - o.potential;
		if (comp == 0) {
			return NeoCore.gen.nextBoolean() ? 1 : -1; // Tiebreakers are random
		}
		return comp;
	}
}
