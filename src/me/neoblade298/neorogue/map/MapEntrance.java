package me.neoblade298.neorogue.map;

public class MapEntrance {
	private final Coordinates entrance;
	private final Coordinates facing;

	public MapEntrance(MapPiece piece, String line) {
		String[] parsed = line.split(",");
		Direction direction = Direction.getFromCharacter(parsed[parsed.length - 1].charAt(0)).invert();
		double xlen = piece.getShape().getBaseLength() - 1;
		double zlen = piece.getShape().getBaseHeight() - 1;

		entrance = new Coordinates(Double.parseDouble(parsed[0]), Double.parseDouble(parsed[1]), Double.parseDouble(parsed[2]),
				xlen, zlen, direction, direction);
		if (parsed.length >= 7) {
			facing = new Coordinates(Double.parseDouble(parsed[3]), Double.parseDouble(parsed[4]), Double.parseDouble(parsed[5]),
					xlen, zlen, direction, direction);
		}
		else {
			double x = entrance.getX();
			double y = entrance.getY();
			double z = entrance.getZ();
			switch (direction) {
			case EAST:
				x += 1;
				break;
			case WEST:
				x -= 1;
				break;
			case NORTH:
				z += 1;
				break;
			case SOUTH:
				z -= 1;
				break;
			}
			facing = new Coordinates(x, y, z, xlen, zlen, direction, direction);
		}
	}

	private MapEntrance(Coordinates entrance, Coordinates facing) {
		this.entrance = entrance;
		this.facing = facing;
	}

	@Override
	public MapEntrance clone() {
		return new MapEntrance(entrance.clone(), facing.clone());
	}

	public MapEntrance applySettings(MapPieceInstance settings) {
		entrance.applySettings(settings);
		facing.applySettings(settings);
		return this;
	}

	public Coordinates getEntrance() {
		return entrance;
	}

	public Coordinates getFacing() {
		return facing;
	}

	public double getX() {
		return entrance.getX();
	}

	public double getY() {
		return entrance.getY();
	}

	public double getZ() {
		return entrance.getZ();
	}

	public double getXFacing() {
		return facing.getX();
	}

	public double getYFacing() {
		return facing.getY();
	}

	public double getZFacing() {
		return facing.getZ();
	}

	public Direction getOriginalDirection() {
		return entrance.getOriginalDirection();
	}

	public Direction getDirection() {
		return entrance.getDirection();
	}

	public void setRotations(int amount) {
		entrance.setRotations(amount);
		facing.setRotations(amount);
	}

	public void setFlip(boolean flipX, boolean flipZ) {
		entrance.setFlip(flipX, flipZ);
		facing.setFlip(flipX, flipZ);
	}

	public boolean canConnect(MapEntrance other) {
		return getX() == other.getXFacing()
				&& getY() == other.getYFacing()
				&& getZ() == other.getZFacing()
				&& getXFacing() == other.getX()
				&& getYFacing() == other.getY()
				&& getZFacing() == other.getZ()
				&& getDirection().equals(other.getDirection().invert());
	}
}
