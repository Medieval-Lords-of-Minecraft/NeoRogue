package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Firestarter extends Equipment {
	private static final String ID = "Firestarter";
	private static final int EXPLOSION_RADIUS = 6;
	private static final int EXPLOSION_DAMAGE = 120;
	private static final ParticleContainer tick = new ParticleContainer(Particle.FLAME).count(10).spread(0.3, 0.3);
	private static final ParticleContainer explode = new ParticleContainer(Particle.FLAME).count(50).spread(1, 1);
	private static final TargetProperties tp = TargetProperties.radius(EXPLOSION_RADIUS, false, TargetType.ENEMY);
	
	private int burn;
	
	public Firestarter(boolean isUpgraded) {
		super(ID, "Firestarter", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(15, 20, 10, 15));
		burn = isUpgraded ? 50 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new FirestarterProjectile(data, this, slot));
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			data.charge(20);
			data.addTask(new BukkitRunnable() {
				public void run() {
					proj.start(data);
					Sounds.fire.play(p, p);
				}
			}.runTaskLater(NeoRogue.inst(), 20));
			return TriggerResult.keep();
		}));
	}
	
	private class FirestarterProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private Equipment eq;
		private int slot;

		public FirestarterProjectile(PlayerFightData data, Equipment eq, int slot) {
			super(properties.get(PropertyType.RANGE), 3);
			this.size(0.4, 0.4);
			this.pierce(-1);
			this.data = data;
			this.p = data.getPlayer();
			this.eq = eq;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			// Apply burn to hit enemies
			FightInstance.applyStatus(hit.getEntity(), StatusType.BURN, data, burn, -1);
		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			explode(proj.getLocation());
		}
		
		private void explode(Location loc) {
			Sounds.explode.play(p, loc);
			explode.play(p, loc);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
				FightInstance.dealDamage(new DamageMeta(data, EXPLOSION_DAMAGE, DamageType.FIRE, 
						DamageStatTracker.of(ID + slot, eq)), ent);
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIRE_CHARGE,
				"Charge for <white>1s</white> to fire a piercing projectile that applies " + 
				GlossaryTag.BURN.tag(this, burn, true) + " to enemies. Upon hitting a block, deals " +
				GlossaryTag.FIRE.tag(this, EXPLOSION_DAMAGE, false) + " damage to enemies in a " + 
				DescUtil.white(EXPLOSION_RADIUS) + " block radius.");
	}
}
