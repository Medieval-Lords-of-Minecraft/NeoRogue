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
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ManaEater extends Equipment {
	private static final String ID = "ManaEater";
	private static final ParticleContainer tick;
	private static final SoundContainer tickSound = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_BREAK),
			hit = new SoundContainer(Sound.BLOCK_CHAIN_PLACE);
	
	static {
		tick = new ParticleContainer(Particle.SMOKE);
		tick.count(5).spread(0.1, 0.1).speed(0.01);
	}
	
	public ManaEater(boolean isUpgraded) {
		super(
				ID , "Mana Eater", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(3, 0, isUpgraded ? 60 : 45, 1, DamageType.DARK, Sound.ENTITY_PLAYER_ATTACK_SWEEP)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new ManaEaterProjectile(data, this, slot));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}
	
	private class ManaEaterProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private ManaEater eq;
		private int slot;

		public ManaEaterProjectile(PlayerFightData data, ManaEater eq, int slot) {
			super(1.5, 10, 2);
			this.size(0.2, 0.2);
			this.p = data.getPlayer();
			this.data = data;
			this.eq = eq;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Location loc = hit.getEntity().getLocation();
			ManaEater.hit.play(p, loc);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			tickSound.play(p, proj.getLocation());
			proj.applyWeapon(data, eq, slot);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STICK);
	}
}
