package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class FireStaff extends Equipment {
	private static final TargetProperties props = TargetProperties.radius(1, true, TargetType.ENEMY);
	
	private static ParticleContainer tick;

	static {
		tick = new ParticleContainer(Particle.FLAME);
		tick.count(5).spread(0.1, 0.1).speed(0.01);
	}

	public FireStaff(boolean isUpgraded) {
		super(
				"fireStaff", "Fire Staff", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(8, 0, isUpgraded ? 40 : 20, 0.35, DamageType.FIRE, Sound.ITEM_AXE_SCRAPE)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new FireStaffProjectile(p));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			weaponSwing(p, data);
			data.addTask(id, new BukkitRunnable() {
				@Override
				public void run() {
					proj.start(data);
				}
			}.runTaskLater(NeoRogue.inst(), 5));
			return TriggerResult.keep();
		});
	}

	private class FireStaffProjectile extends Projectile {
		private Player p;
		
		public FireStaffProjectile(Player p) {
			super(0.5, 15, 2);
			this.size(1, 1).gravity(0.125).initialY(0.65);
			this.p = p;
		}
		
		@Override
		public void onTick(ProjectileInstance proj, boolean interpolation) {
			tick.spawn(proj.getLocation());
		}
		
		@Override
		public void onEnd(ProjectileInstance proj) {

		}
		
		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, proj.getLocation(), props)) {
				weaponDamageProjectile(ent, proj);
			}

			Location loc = hit.getEntity().getLocation();
			Util.playSound(p, loc, Sound.BLOCK_CHAIN_PLACE, 1F, 1F, true);
		}
		
		@Override
		public void onStart(ProjectileInstance proj) {

		}
	}
	
	@Override
	public void setupItem() {
		item = createItem(Material.STICK, "Lobs a small fireball exploding on hit.");
	}
}
