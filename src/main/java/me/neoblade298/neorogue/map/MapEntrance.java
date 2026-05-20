package me.neoblade298.neorogue.map;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MapEntrance {
	private final Coordinates entrance;
	private final Coordinates facing;
	private final Set<String> tags;
	private final Set<String> requiredTags;
	private final Set<String> allowedTags;

	public MapEntrance(MapPiece piece, String line) {
		String[] parsed = line.split(",");
		Direction direction = Direction.getFromCharacter(parsed[3].charAt(0)).invert();
		double xlen = piece.getShape().getBaseLength() - 1;
		double zlen = piece.getShape().getBaseHeight() - 1;

		entrance = new Coordinates(Double.parseDouble(parsed[0]), Double.parseDouble(parsed[1]), Double.parseDouble(parsed[2]),
				xlen, zlen, direction, direction);

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

		tags = parsed.length >= 5 ? parseTags(parsed[4]) : Collections.emptySet();
		requiredTags = parsed.length >= 6 ? parseTags(parsed[5]) : Collections.emptySet();
		allowedTags = parsed.length >= 7 ? parseTags(parsed[6]) : Collections.emptySet();
	}

	private MapEntrance(Coordinates entrance, Coordinates facing, Set<String> tags, Set<String> requiredTags, Set<String> allowedTags) {
		this.entrance = entrance;
		this.facing = facing;
		this.tags = tags;
		this.requiredTags = requiredTags;
		this.allowedTags = allowedTags;
	}

	private static Set<String> parseTags(String input) {
		if (input == null || input.isEmpty()) return Collections.emptySet();
		String[] parts = input.split("\\+");
		Set<String> set = new HashSet<>();
		for (String part : parts) {
			String trimmed = part.trim();
			if (!trimmed.isEmpty()) set.add(trimmed);
		}
		return set.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(set);
	}

	@Override
	public MapEntrance clone() {
		return new MapEntrance(entrance.clone(), facing.clone(), tags, requiredTags, allowedTags);
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

	public boolean tagsCompatible(MapEntrance other) {
		// If neither side has any filtering, always compatible
		boolean thisFilters = !requiredTags.isEmpty() || !allowedTags.isEmpty();
		boolean otherFilters = !other.requiredTags.isEmpty() || !other.allowedTags.isEmpty();

		// Check that the other entrance has all of this entrance's required tags
		if (!other.tags.containsAll(requiredTags)) return false;
		// Check that this entrance has all of the other entrance's required tags
		if (!tags.containsAll(other.requiredTags)) return false;

		// If this entrance filters, all of other's tags must be in required ∪ allowed
		if (thisFilters) {
			for (String tag : other.tags) {
				if (!requiredTags.contains(tag) && !allowedTags.contains(tag)) return false;
			}
		}
		// If other entrance filters, all of this entrance's tags must be in other's required ∪ allowed
		if (otherFilters) {
			for (String tag : tags) {
				if (!other.requiredTags.contains(tag) && !other.allowedTags.contains(tag)) return false;
			}
		}

		return true;
	}

	public boolean canConnect(MapEntrance other) {
		return getX() == other.getXFacing()
				&& getY() == other.getYFacing()
				&& getZ() == other.getZFacing()
				&& getXFacing() == other.getX()
				&& getYFacing() == other.getY()
				&& getZFacing() == other.getZ()
				&& getDirection().equals(other.getDirection().invert())
				&& tagsCompatible(other);
	}
}
