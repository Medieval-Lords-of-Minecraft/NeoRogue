package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
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

public class DrainWand extends Equipment {
	private static final String ID = "iceWand";
	private static final TargetProperties props = TargetProperties.radius(0.75, true, TargetType.ENEMY);
	
	private static final ParticleContainer tick;
	private static final SoundContainer sc = new SoundContainer(Sound.BLOCK_CHAIN_PLACE);

	private int shieldAmount;

	static {
		tick = new ParticleContainer(Particle.SMOKE);
		tick.count(5).spread(0.1, 0.1).speed(0.01);
	}

	public DrainWand(boolean isUpgraded) {
		super(
				ID, "Drain Wand", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(2, 0, isUpgraded ? 45 : 35, 1, DamageType.DARK, Sound.ITEM_AXE_SCRAPE)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
		shieldAmount = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new DrainWandProjectile(data));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}

	private class DrainWandProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		
		public DrainWandProjectile(PlayerFightData data) {
			super(0.8, 10, 3);
			this.size(1, 1);
			this.data = data;
			this.p = data.getPlayer();
		}
		
		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}
		
		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(hit.getEntity(), props)) {
				data.addSimpleShield(p.getUniqueId(), shieldAmount, 40);
				FightInstance.dealDamage(new DamageMeta(hit, properties.get(PropertyType.DAMAGE), DamageType.DARK), ent);
			}

			Location loc = hit.getEntity().getLocation();
			sc.play(p, loc);
		}
		
		@Override
		public void onStart(ProjectileInstance proj) {
			proj.applyProperties(data, properties);
		}
	}
	
	@Override
	public void setupItem() {
		item = createItem(
				Material.STICK,
				"Gain <yellow>" + shieldAmount + "</yellow> "
						+ GlossaryTag.SHIELDS.tag(this) + " [<white>2s</white>] on hit."
		);
	}
}
