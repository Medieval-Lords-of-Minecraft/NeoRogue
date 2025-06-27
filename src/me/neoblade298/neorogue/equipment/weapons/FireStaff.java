package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class FireStaff extends Equipment {
	private static final String ID = "fireStaff";
	private static final TargetProperties props = TargetProperties.radius(1.2, true, TargetType.ENEMY);
	private static final ParticleContainer exp = new ParticleContainer(Particle.EXPLOSION);
	
	private static final ParticleContainer tick;

	static {
		tick = new ParticleContainer(Particle.FLAME);
		tick.count(5).spread(0.1, 0.1).speed(0.01);
	}

	public FireStaff(boolean isUpgraded) {
		super(
				ID, "Fire Staff", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(8, 0, isUpgraded ? 80 : 60, 0.5, DamageType.FIRE, Sound.ENTITY_BLAZE_SHOOT)
				.add(PropertyType.AREA_OF_EFFECT, props.range)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new FireStaffProjectile(data, this));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}

	private class FireStaffProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private FireStaff eq;

		public FireStaffProjectile(PlayerFightData data, FireStaff eq) {
			super(0.5, 15, 2);
			this.size(1, 1).gravity(0.0125).initialY(0.55);
			this.p = data.getPlayer();
			this.data = data;
			this.eq = eq;
		}
		
		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			dealDamageArea(proj, hit.getEntity().getLocation(), null);
		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			dealDamageArea(proj, b.getLocation(), null);
		}
		
		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.fire.play(p, p);
		}

		private void dealDamageArea(ProjectileInstance proj, Location loc, Barrier hitBarrier) {
			while (loc.getBlock().getType().isAir()) {
				loc.add(0, -1, 0);
			}
			Sounds.explode.play(p, loc);
			exp.play(p, loc);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, props)) {
				DamageMeta dm = new DamageMeta(data, eq, true);
				dm.setSource(loc);
				FightInstance.dealDamage(dm, ent);
				// Make sure this happens BEFORE projectile tick is resolved, since damagemeta is resolved in there first and we need to change Location source
			}
		}
	}
	
	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_ROD, "Lobs a small fireball exploding when hitting enemies or blocks.");
	}
}
