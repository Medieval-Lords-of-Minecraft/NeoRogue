package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Swiftsunder extends Bow {
	private static final String ID = "Swiftsunder";
	private static final int THRESHOLD = 4;
	private final int strength;

	public Swiftsunder(boolean isUpgraded) {
		super(ID, "Swiftsunder", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofBow(isUpgraded ? 50 : 35, 1, 0, 12, 0, 1.4));
		strength = isUpgraded ? 5 : 3;
	}

	@Override
	public void onTick(Player player, ProjectileInstance proj, int interpolation) {
		BowProjectile.tick.play(player, proj.getLocation());
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta attacks = new ActionMeta();
		data.addSlotBasedTrigger(id, slot, Trigger.VANILLA_PROJECTILE, (pdata, in) -> {
			Vector arrowVelocity = ((ProjectileLaunchEvent) in).getEntity().getVelocity();
			if (!canShoot(data, arrowVelocity)) return TriggerResult.keep();
			useBow(data);

			ProjectileGroup projectile = new ProjectileGroup(new BowProjectile(data, arrowVelocity, this, id + slot));
			if (attacks.addCount(1) >= THRESHOLD) {
				attacks.addCount(-THRESHOLD);
				data.applyStatus(StatusType.STRENGTH, data, strength, -1, this);
				BowProjectile aftershot = new SwiftsunderAftershot(data, arrowVelocity, slot)
						.setDamageBonus(-properties.get(PropertyType.DAMAGE));
				data.addAftershot(new ProjectileGroup(aftershot));
			}
			projectile.start(data);

			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOW, "Every " + DescUtil.val(THRESHOLD) + " basic attacks, gain "
				+ GlossaryTag.STRENGTH.tag(this, strength) + " and fire an "
				+ GlossaryTag.AFTERSHOT.tag(this) + " that deals ammunition damage plus " + GlossaryTag.SLASHING.tag(this) + " damage equal to the target's "
				+ GlossaryTag.REND.tag(this) + ".");
	}

	private class SwiftsunderAftershot extends BowProjectile {
		private final PlayerFightData data;

		public SwiftsunderAftershot(PlayerFightData data, Vector velocity, int slot) {
			super(data, velocity, Swiftsunder.this, id + slot);
			this.data = data;
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			super.onHit(hit, hitBarrier, meta, proj);
			int rend = hit.getStatus(StatusType.REND).getStacks();
			meta.addDamageSlice(new DamageSlice(data, rend, DamageType.SLASHING,
					DamageStatTracker.of(id, Swiftsunder.this)));
		}
	}
}