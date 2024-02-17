package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class IceWand extends Equipment {
	private static final TargetProperties props = TargetProperties.radius(0.75, true, TargetType.ENEMY);
	private static final PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 60, 1, false, false, false);

	private static ParticleContainer tick;
	
	private int shieldAmount;
	
	static {
		tick = new ParticleContainer(Particle.GLOW);
		tick.count(5).spread(0.1, 0.1).speed(0.01);
	}
	
	public IceWand(boolean isUpgraded) {
		super(
				"iceWand", "Ice Wand", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(6, 0, isUpgraded ? 30 : 20, 0.65, DamageType.ICE, Sound.ITEM_AXE_SCRAPE)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
		shieldAmount = isUpgraded ? 3 : 2;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new IceWandProjectile(p));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data))
				return TriggerResult.keep();
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}
	
	private class IceWandProjectile extends Projectile {
		private Player p;

		public IceWandProjectile(Player p) {
			super(0.4, 8, 3);
			this.size(1, 1);
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
			weaponDamageProjectile(hit.getEntity(), proj);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, hit.getEntity().getLocation(), props)) {
				weaponDamageProjectile(ent, proj);
				ent.addPotionEffect(slow);
				proj.getOwner().addSimpleShield(p.getUniqueId(), shieldAmount, 40);
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
		item = createItem(
				Material.STICK,
				"Shoots an ice missile shattering on hit. All enemies hit are slightly slowed, and for each you gain <yellow>" + shieldAmount + "</yellow> "
						+ GlossaryTag.SHIELDS.tag(this) + " for <white>2</white> seconds."
		);
	}
}
