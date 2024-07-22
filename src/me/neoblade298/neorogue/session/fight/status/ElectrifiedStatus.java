package me.neoblade298.neorogue.session.fight.status;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;

public class ElectrifiedStatus extends DecrementStackStatus {
	private static final ParticleContainer tick = new ParticleContainer(Particle.FIREWORKS_SPARK).count(3).speed(0.01).spread(0.1, 0.1);
	private static final SoundContainer hit = new SoundContainer(Sound.ENTITY_ITEM_BREAK);
	private static final TargetProperties tp = TargetProperties.radius(10, false, TargetType.ENEMY);
	private static String id = "ELECTRIFIED";

	// Currently only works on enemies
	public ElectrifiedStatus(FightData target) {
		super(id, target, StatusClass.NEGATIVE);
	}

	
	@Override
	public void onTickAction() {
		// Owner is arbitrarily first slice of damage
		PlayerFightData owner = (PlayerFightData) slices.getSliceOwners().entrySet().iterator().next().getKey();
		ProjectileGroup proj = new ProjectileGroup(new ElectrifiedProjectile(owner.getPlayer()));
		
		LinkedList<LivingEntity> list = TargetHelper.getEntitiesInRadius(data.getEntity(), tp);
		if (list.isEmpty()) return;
		
		Collections.shuffle(list);
		LivingEntity target = list.peekFirst();
		Vector v = target.getLocation().subtract(0, 2, 0).subtract(data.getEntity().getLocation()).toVector();
		proj.start(owner, data.getEntity().getLocation().add(0, 4, 0), v);
	}
	
	private class ElectrifiedProjectile extends Projectile {
		private Player p;

		public ElectrifiedProjectile(Player p) {
			super(0.5, 10, 1);
			this.size(0.5, 0.5);
			this.p = p;
		}

		@Override
		public void onTick(ProjectileInstance proj, boolean interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			Location loc = hit.getEntity().getLocation();
			LivingEntity target = hit.getEntity();
			ElectrifiedStatus.hit.play(p, loc);
			
			DamageMeta dm = new DamageMeta(proj.getOwner());
			dm.isSecondary(true);
			for (Entry<FightData, Integer> ent : slices.getSliceOwners().entrySet()) {
				dm.addDamageSlice(new DamageSlice(ent.getKey(), ent.getValue() * 0.2, DamageType.LIGHTNING, true));
			}
			if (!proj.getBuffs().isEmpty()) {
				dm.addBuffs(proj.getBuffs(), BuffOrigin.PROJECTILE, true);
			}
			if (hitBarrier != null) {
				dm.addBuffs(hitBarrier.getBuffs(), BuffOrigin.BARRIER, false);
			}
			FightInstance.knockback(target,
					proj.getVector().normalize().multiply(0.2));
			FightInstance.dealDamage(dm, target);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			
		}
	}
}
