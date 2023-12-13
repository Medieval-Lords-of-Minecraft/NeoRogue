package me.neoblade298.neorogue.session.fight;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.bukkit.MythicBukkit;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import me.neoblade298.neocore.bukkit.util.TargetUtil;

public class TargetHelper {
	
	public static LivingEntity getNearestInSight(LivingEntity source, double tolerance, TargetProperties props) {
		return TargetUtil.getEntitiesInSight(source, props.range, tolerance, new TargetFilter(source, props)).peekFirst();
	}
	
	public static List<LivingEntity> getEntitiesInSight(LivingEntity source, TargetProperties props) {
		return TargetUtil.getEntitiesInSight(source, props.range, props.tolerance, new TargetFilter(source, props));
	}

	public static List<LivingEntity> getEntitiesInRadius(LivingEntity source, TargetProperties props) {
		return source.getNearbyEntities(props.range, props.range, props.range).stream()
			.filter(trg -> trg instanceof LivingEntity)
			.map(trg -> (LivingEntity) trg)
			.filter(new TargetFilter(source, props)).collect(Collectors.toCollection(LinkedList::new));
	}
	
	public static class TargetProperties {
		public double range, tolerance = 4, arc;
		public boolean throughWall;
		public TargetType type;
		
		public TargetProperties(double range, boolean throughWall, TargetType type) {
			this.range = range;
			this.throughWall = throughWall;
			this.type = type;
		}
		
		public TargetProperties(double arc, double range, boolean throughWall, TargetType type) {
			this.range = range;
			this.throughWall = throughWall;
			this.type = type;
		}
		
		public TargetProperties tolerance(double tolerance) {
			this.tolerance = tolerance;
			return this;
		}
	}
	
	public static enum TargetType {
		ALLY, ENEMY, BOTH;
	}
	
	private static class TargetFilter implements Predicate<LivingEntity> {
		private LivingEntity src;
		private TargetProperties props;
		private BukkitAPIHelper api = MythicBukkit.inst().getAPIHelper();
		public TargetFilter(LivingEntity src, TargetProperties props) {
			this.src = src;
			this.props = props;
		}

		@Override
		public boolean test(LivingEntity trg) {
			if (!isValidTarget(src, trg, props.throughWall)) return false;
			switch (props.type) {
			case ENEMY: return api.isMythicMob(trg);
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
		return rt.getHitBlock() != null;
	}

	static boolean isValidTarget(final LivingEntity source, final LivingEntity target,
			boolean throughWall) {
		return target != source
				&& (throughWall || !isObstructed(source.getEyeLocation(), target.getEyeLocation()))
				&& target.getType() != EntityType.ARMOR_STAND;
	}
}
