package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class Windplume extends Equipment {
	private static final String ID = "Windplume";
	private static final int RANGE_INCREASE = 2;
	private static final int DAMAGE_INSTANCES = 6;
	private static final int SHIELDS = 3;
	private static final int SHIELD_DURATION_TICKS = 80;

	public Windplume(boolean isUpgraded) {
		super(ID, "Windplume", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER, EquipmentType.ACCESSORY);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			LaunchProjectileGroupEvent event = (LaunchProjectileGroupEvent) in;
			for (ProjectileInstance instance : event.getInstances()) {
				instance.addMaxRange(RANGE_INCREASE);
			}
			return TriggerResult.keep();
		});

		ActionMeta instances = new ActionMeta();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent event = (DealDamageEvent) in;
			if (!event.getMeta().hasOrigin(DamageOrigin.PROJECTILE)) return TriggerResult.keep();
			if (instances.addCount(1) < DAMAGE_INSTANCES) return TriggerResult.keep();

			instances.setCount(0);
			Player player = data.getPlayer();
			data.addSimpleShield(player.getUniqueId(), SHIELDS, SHIELD_DURATION_TICKS, this);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PHANTOM_MEMBRANE,
				"Increase range of all projectiles by " + DescUtil.val(RANGE_INCREASE) + ". Every "
						+ DescUtil.val(DAMAGE_INSTANCES) + " instances of dealing projectile damage, gain "
						+ GlossaryTag.SHIELDS.tag(this, SHIELDS) + " [" + DescUtil.val("4s") + "].");
	}
}