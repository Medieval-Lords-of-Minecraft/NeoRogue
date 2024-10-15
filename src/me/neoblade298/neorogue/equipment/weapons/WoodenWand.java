package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class WoodenWand extends Equipment {
	private static final String ID = "woodenWand";
	private static final ParticleContainer tick;
	private static final SoundContainer tickSound = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_BREAK),
			hit = new SoundContainer(Sound.BLOCK_CHAIN_PLACE);
	
	static {
		tick = new ParticleContainer(Particle.END_ROD);
		tick.count(4).spread(0.1, 0.1).speed(0.01);
	}
	
	public WoodenWand(boolean isUpgraded) {
		super(
				ID , "Wooden Wand", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(5, 0, isUpgraded ? 50 : 25, isUpgraded ? 1 : 0.5, DamageType.LIGHT, Sound.ENTITY_PLAYER_ATTACK_SWEEP)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
		properties.addUpgrades(PropertyType.ATTACK_SPEED);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new WoodenWandProjectile(p));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data))
				return TriggerResult.keep();
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}
	
	private class WoodenWandProjectile extends Projectile {
		private Player p;

		public WoodenWandProjectile(Player p) {
			super(0.5, 10, 3);
			this.size(0.5, 0.5);
			this.p = p;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
			tickSound.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			weaponDamageProjectile(hit.getEntity(), proj, hitBarrier);
			Location loc = hit.getEntity().getLocation();
			WoodenWand.hit.play(p, loc);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STICK);
	}
}
