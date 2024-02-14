package me.neoblade298.neorogue.session.fight;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.LinkedList;
import java.util.function.Predicate;
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

	public static LivingEntity getNearest(LivingEntity source, TargetProperties props) {
		return getEntitiesInRadius(source, props).peekFirst();
	}
	
	public static LinkedList<LivingEntity> getEntitiesInCone(LivingEntity source, TargetProperties props) {
		return TargetUtil.getEntitiesInCone(source, props.arc, props.range, new TargetFilter(source, props));
	}
	
	public static class TargetProperties {
		public double range, tolerance = 4, arc;
		public boolean throughWall, stickToGround;
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
		
		public static TargetProperties radius(double range, boolean throughWall, TargetType type) {
			return new TargetProperties(range, throughWall, type);
		}
		
		public static TargetProperties line(double range, double tolerance, TargetType type) {
			return new TargetProperties(range, tolerance, type);
		}
		
		public static TargetProperties cone(double arc, double range, boolean throughWall, TargetType type) {
			return new TargetProperties(arc, range, throughWall, type);
		}
		
		public static TargetProperties block(double range, boolean stickToGround) {
			return new TargetProperties(range, stickToGround);
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
			if (!isValidTarget(src, trg, props.throughWall)) return false;
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

	static boolean isValidTarget(final LivingEntity source, final LivingEntity target,
			boolean throughWall) {
		return target != source
				
				&& (throughWall ||
						!isObstructed(source.getEyeLocation(), target.getEyeLocation()) ||
						!isObstructed(source.getEyeLocation(), target.getLocation()))
				
				&& target.getType() != EntityType.ARMOR_STAND;
	}
}
