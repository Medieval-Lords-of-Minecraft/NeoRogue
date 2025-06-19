package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class AshenWand2 extends Equipment {
	private static final String ID = "ashenWand2";
	private static final ParticleContainer tick;
	
	static {
		tick = new ParticleContainer(Particle.SMOKE);
		tick.count(5).spread(0.1, 0.1).speed(0.01);
	}
	
	public AshenWand2(boolean isUpgraded) {
		super(
				ID , "Ashen Wand II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(1, 0, 65, isUpgraded ? 1.3 : 1, DamageType.FIRE, Sound.ENTITY_PLAYER_ATTACK_SWEEP).add(PropertyType.RANGE, 10)
		);
		properties.addUpgrades(PropertyType.ATTACK_SPEED);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new AshenWandProjectile(data));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}
	
	private class AshenWandProjectile extends Projectile {
		private Player p;

		public AshenWandProjectile(PlayerFightData data) {
			super(1, 10, 2);
			this.size(0.2, 0.2);
			this.p = data.getPlayer();
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Location loc = hit.getEntity().getLocation();
			Sounds.infect.play(p, loc);
			applyProjectileOnHit(hit.getEntity(), proj, hitBarrier, true);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.fire.play(p, p);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STICK, "Projectiles are paused for <white>1s</white> before firing.");
	}
}
