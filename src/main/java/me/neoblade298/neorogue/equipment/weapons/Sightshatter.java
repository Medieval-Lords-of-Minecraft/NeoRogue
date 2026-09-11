package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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

public class Sightshatter extends Bow {
	private static final String ID = "Sightshatter";
	private int injury, slownessDuration;

	public Sightshatter(boolean isUpgraded) {
		super(ID, "Sightshatter", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofBow(isUpgraded ? 100 : 80, 1, 0, 12, 0, 1.2));
		injury = isUpgraded ? 8 : 6;
		slownessDuration = 60;
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

			ProjectileGroup proj = new ProjectileGroup(new SightshatterProjectile(data, arrowVelocity, id + slot));
			proj.start(data);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOW,
				"Can only be fired at " + DescUtil.val("max draw") + ". Basic attacks apply "
				+ GlossaryTag.INJURY.tag(this, injury) + " and "
				+ DescUtil.potion("Slowness", 0, slownessDuration / 20) + " on hit.");
	}

	private class SightshatterProjectile extends BowProjectile {
		public SightshatterProjectile(PlayerFightData data, Vector velocity, String id) {
			super(data, velocity, Sightshatter.this, id);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			super.onHit(hit, hitBarrier, meta, proj);
			hit.applyStatus(StatusType.INJURY, proj.getOwner(), injury, -1, Sightshatter.this);
			hit.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slownessDuration, 0));
		}
	}
}