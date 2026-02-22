package me.neoblade298.neorogue.session.fight;

import java.util.LinkedList;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.util.TargetUtil;
import me.neoblade298.neorogue.NeoRogue;

public class TargetHelper {
	public static LivingEntity getNearestInSight(LivingEntity source, TargetProperties props) {
		return TargetUtil.getEntitiesInSight(source, props.range, props.tolerance, new TargetFilter(source, props)).peekFirst();
	}
	
	public static LinkedList<LivingEntity> getEntitiesInSight(LivingEntity source, TargetProperties props) {
		return TargetUtil.getEntitiesInSight(source, props.range, props.tolerance, new TargetFilter(source, props));
	}

	public static LinkedList<LivingEntity> getEntitiesInRadius(LivingEntity source, Location loc, TargetProperties props) {
		return TargetUtil.getEntitiesInRadius(loc, props.range, new TargetFilter(source, props));
	}

	public static LinkedList<LivingEntity> getEntitiesInRadius(LivingEntity source, TargetProperties props) {
		return TargetUtil.getEntitiesInRadius(source, props.range, new TargetFilter(source, props));
	}

	public static LivingEntity getNearest(LivingEntity source, Location loc, TargetProperties props) {
		return getEntitiesInRadius(source, loc, props).peekFirst();
	}

	public static LivingEntity getNearest(LivingEntity source, TargetProperties props) {
		return getEntitiesInRadius(source, props).peekFirst();
	}
	
	public static LinkedList<LivingEntity> getEntitiesInCone(LivingEntity source, TargetProperties props) {
		return TargetUtil.getEntitiesInCone(source, props.arc, props.range, new TargetFilter(source, props));
	}
	
	public static LinkedList<LivingEntity> getEntitiesInCone(LivingEntity source, Vector direction, TargetProperties props) {
		return TargetUtil.getEntitiesInCone(source, source.getLocation(), direction, props.arc, props.range, new TargetFilter(source, props));
	}
	
	public static LinkedList<LivingEntity> getEntitiesInCone(LivingEntity source, Location loc, Vector direction, TargetProperties props) {
		return TargetUtil.getEntitiesInCone(source, loc, direction, props.arc, props.range, new TargetFilter(source, props));
	}
	
	public static Location getSightLocation(LivingEntity source, TargetProperties props) {
		return TargetUtil.getSightLocation(source, props.range, props.stickToGround);
	}
	
	public static Location getSightLocation(Location source, Vector direction, TargetProperties props) {
		return TargetUtil.getSightLocation(source, direction, props.range, props.stickToGround);
	}

	// Source is used to calculate ally and enemy
	public static LinkedList<LivingEntity> getEntitiesInLine(LivingEntity source, Location start, Vector direction, TargetProperties props) {
		return TargetUtil.getEntitiesInLine(start, direction, props.range, props.tolerance, new TargetFilter(source, props));
	}
	
	public static LinkedList<LivingEntity> getEntitiesInLine(LivingEntity source, Location start, Location end, TargetProperties props) {
		Vector v = end.clone().subtract(start).toVector();
		double range = v.length();
		return TargetUtil.getEntitiesInLine(start, v.normalize(), range, props.tolerance, new TargetFilter(source, props));
	}
	
	public static class TargetProperties {
		public double range, tolerance = 4, arc;
		public boolean throughWall, stickToGround, canTargetSource;
		public TargetType type;
		
		private TargetProperties(double range, boolean stickToGround) {
			this.range = range;
			this.stickToGround = stickToGround;
		}
		
		private TargetProperties(double range, double tolerance, TargetType type) {
			this.range = range;
			this.tolerance = tolerance;
			this.type = type;
		}

		private TargetProperties(double range, double tolerance, TargetType type, boolean throughWall) {
			this.range = range;
			this.tolerance = tolerance;
			this.type = type;
			this.throughWall = throughWall;
		}
		
		private TargetProperties(double range, boolean throughWall, TargetType type) {
			this.range = range;
			this.throughWall = throughWall;
			this.type = type;
		}
		
		private TargetProperties(double arc, double range, boolean throughWall, TargetType type) {
			this.arc = arc;
			this.range = range;
			this.throughWall = throughWall;
			this.type = type;
		}
		
		public TargetProperties tolerance(double tolerance) {
			this.tolerance = tolerance;
			return this;
		}
		
		public static TargetProperties radius(double range, boolean throughWall) {
			return new TargetProperties(range, throughWall, TargetType.ENEMY);
		}
		
		public static TargetProperties radius(double range, boolean throughWall, TargetType type) {
			return new TargetProperties(range, throughWall, type);
		}
		
		public static TargetProperties line(double range, double tolerance, TargetType type) {
			return new TargetProperties(range, tolerance, type);
		}

		public static TargetProperties line(double range, double tolerance, boolean throughWall, TargetType type) {
			return new TargetProperties(range, tolerance, type, throughWall);
		}
		
		public static TargetProperties cone(double arc, double range, boolean throughWall, TargetType type) {
			return new TargetProperties(arc, range, throughWall, type);
		}
		
		public static TargetProperties block(double range, boolean stickToGround) {
			return new TargetProperties(range, stickToGround);
		}

		public TargetProperties canTargetSource(boolean canTargetSource) {
			this.canTargetSource = canTargetSource;
			return this;
		}
	}
	
	public static enum TargetType {
		ALLY, ENEMY, BOTH;
	}
	
	private static class TargetFilter implements Predicate<LivingEntity> {
		private LivingEntity src;
		private TargetProperties props;
		public TargetFilter(LivingEntity src, TargetProperties props) {
			this.src = src;
			this.props = props;
		}

		@Override
		public boolean test(LivingEntity trg) {
			if (!isValidTarget(src, trg, props)) return false;
			if (trg == src) return false;
			switch (props.type) {
			case ENEMY: return NeoRogue.mythicApi.isMythicMob(trg);
			case ALLY: return trg instanceof Player;
			default: return true;
			}
		}
	}

	// Checks if there are blocks between two locations
	public static boolean isObstructed(Location loc1, Location loc2) {
		if (loc1.getWorld() != loc2.getWorld()) return false;
		if (loc1.getX() == loc2.getX() && loc1.getY() == loc2.getY() && loc1.getZ() == loc2.getZ()) {
			return false;
		}

		RayTraceResult rt = loc1.getWorld().rayTraceBlocks(loc1, loc2.toVector().subtract(loc1.toVector()), loc1.distance(loc2));
		return rt != null && rt.getHitBlock() != null;
	}

	private static boolean isValidTarget(final LivingEntity source, final LivingEntity target,
			TargetProperties props) {

		// Check for if target type matches
		if (props.type == TargetType.ENEMY) {
			if (!checkValidEnemy(source, target, props)) return false;
		}
		else if (props.type == TargetType.ALLY) {
			if (!checkValidAlly(source, target, props)) return false;
		}
		else {
			if (!(target instanceof Player) && !checkValidEnemy(source, target, props)) return false;
			if (target instanceof Player && !checkValidAlly(source, target, props)) return false;
		}
		return (props.throughWall ||
						!isObstructed(source.getEyeLocation(), target.getEyeLocation()) ||
						!isObstructed(source.getEyeLocation(), target.getLocation()));
	}

	private static boolean checkValidEnemy(final LivingEntity source, final LivingEntity target,
		TargetProperties props) {
		if (!NeoRogue.mythicApi.isMythicMob(target))
			return false;
		if (Mob.get(NeoRogue.mythicApi.getMythicMobInstance(target).getType().getInternalName()) == null)
			return false;
		return true;
	}

	private static boolean checkValidAlly(final LivingEntity source, final LivingEntity target,
			TargetProperties props) {
		if (!(target instanceof Player))
			return false;
		if (target == source && !props.canTargetSource)
			return false;
		return true;
	}
}
