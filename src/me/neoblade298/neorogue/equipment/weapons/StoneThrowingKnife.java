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

public class StoneThrowingKnife extends Equipment {
	private static final String ID = "stoneThrowingKnife";
	
	private static final ParticleContainer tick = new ParticleContainer(Particle.CRIT).count(1);
	private static final SoundContainer hit = new SoundContainer(Sound.ENTITY_ITEM_BREAK);
	
	public StoneThrowingKnife(boolean isUpgraded) {
		super(ID, "Stone Throwing Knife", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofRangedWeapon(20, 1.25, 0, isUpgraded ? 5 : 3, DamageType.SLASHING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		properties.addUpgrades(PropertyType.RANGE);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new StoneThrowingKnifeProjectile(data, this, slot));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data)) return TriggerResult.keep();
			if (!data.canBasicAttack()) return TriggerResult.keep();
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}
	
	private class StoneThrowingKnifeProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private StoneThrowingKnife eq;
		private int slot;

		public StoneThrowingKnifeProjectile(PlayerFightData data, StoneThrowingKnife eq, int slot) {
			super(0.5, isUpgraded ? 5 : 3, 1);
			this.size(0.5, 0.5);
			this.data = data;
			this.eq = eq;
			this.p = data.getPlayer();
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Location loc = hit.getEntity().getLocation();
			StoneThrowingKnife.hit.play(p, loc);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.applyWeapon(data, eq, slot);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SWORD, "Throwable.");
	}
}
