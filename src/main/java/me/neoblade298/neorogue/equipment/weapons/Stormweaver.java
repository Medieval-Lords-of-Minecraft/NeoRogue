package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class Stormweaver extends Equipment {
	private static final String ID = "Stormweaver";
	private static final int RANGE = 5, ELECTRIFIED = 2;
	private static final ParticleContainer TRAIL = new ParticleContainer(Particle.FIREWORK)
			.count(2).spread(0.05, 0.05).speed(0.005);
	private static final ParticleContainer IMPACT = new ParticleContainer(Particle.FIREWORK)
			.count(12).spread(0.1, 0.1).speed(0.01);

	public Stormweaver(boolean isUpgraded) {
		super(ID, "Stormweaver", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.WEAPON,
				EquipmentProperties.ofRangedWeapon(60, 1, 0.2, RANGE, DamageType.PIERCING,
						Sound.ENTITY_PLAYER_ATTACK_SWEEP));
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ProjectileGroup projectile = new ProjectileGroup(new StormweaverProjectile(data, slot));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_NO_HIT, (pdata, in) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR)) return TriggerResult.keep();
			weaponSwing(data.getPlayer(), data);
			projectile.start(data);
			return TriggerResult.keep();
		});
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, in) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			FightData target = FightInstance.getFightData(ev.getTarget());
			int bonusDamage = target != null && target.hasStatus(StatusType.ELECTRIFIED)
					? target.getStatus(StatusType.ELECTRIFIED).getStacks() : 0;
			weaponSwingAndDamage(data.getPlayer(), data, ev.getTarget(),
					properties.get(PropertyType.DAMAGE) + bonusDamage);
			return TriggerResult.keep();
		});
	}

	private class StormweaverProjectile extends Projectile {
		private final PlayerFightData data;
		private final int slot;

		private StormweaverProjectile(PlayerFightData data, int slot) {
			super(0.5, RANGE, 1);
			this.size(0.4, 0.4);
			this.data = data;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance projectile, int interpolation) {
			TRAIL.play(data.getPlayer(), projectile.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier barrier, DamageMeta meta, ProjectileInstance projectile) {
			IMPACT.play(data.getPlayer(), projectile.getLocation());
			hit.applyStatus(StatusType.ELECTRIFIED, data, ELECTRIFIED, -1, Stormweaver.this);
		}

		@Override
		public void onStart(ProjectileInstance projectile) {
			projectile.applyWeapon(data, Stormweaver.this, slot);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_SWORD, "Left-clicking the air fires a projectile up to "
				+ DescUtil.white(RANGE) + " blocks that applies " + GlossaryTag.ELECTRIFIED.tag(this, ELECTRIFIED)
				+ ". Basic attacks against an enemy deal additional " + GlossaryTag.PIERCING.tag(this)
				+ " damage equal to its Electrified stacks.");
	}
}