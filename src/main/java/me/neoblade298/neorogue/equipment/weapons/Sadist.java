package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Sadist extends Bow {
	private static final String ID = "Sadist";
	private int injuryThreshold, healing;

	public Sadist(boolean isUpgraded) {
		super(ID, "Sadist", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofBow(70, 1, 0, 12, 0, 1.2));
		injuryThreshold = isUpgraded ? 15 : 20;
		healing = 1;
	}

	@Override
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {
		BowProjectile.tick.play(p, proj.getLocation());
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addSlotBasedTrigger(id, slot, Trigger.VANILLA_PROJECTILE, (pdata, in) -> {
			ProjectileLaunchEvent ev = (ProjectileLaunchEvent) in;
			Vector arrowVelocity = ev.getEntity().getVelocity();
			if (!canShoot(data, arrowVelocity) || arrowVelocity.length() < 2.9) return TriggerResult.keep();
			useBow(data);

			ProjectileGroup proj = new ProjectileGroup(new SadistProjectile(data, arrowVelocity, id + slot));
			proj.start(data);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOW,
				"Can only be fired at " + DescUtil.val("max draw") + ". Basic attacks that hit enemies with at least "
				+ GlossaryTag.INJURY.tag(this, injuryThreshold) + " heal you for " + DescUtil.val(healing) + ".");
	}

	private class SadistProjectile extends BowProjectile {
		public SadistProjectile(PlayerFightData data, Vector velocity, String id) {
			super(data, velocity, Sadist.this, id);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			super.onHit(hit, hitBarrier, meta, proj);
			if (hitBarrier == null && hit.getStatus(StatusType.INJURY).getStacks() >= injuryThreshold) {
				proj.getOwner().addHealth(healing, Sadist.this);
			}
		}
	}
}