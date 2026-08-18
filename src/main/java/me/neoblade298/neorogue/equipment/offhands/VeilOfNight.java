package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class VeilOfNight extends Equipment {
	private static final String ID = "VeilOfNight";
	private static final TargetProperties tp = TargetProperties.radius(6, false, TargetType.ENEMY);
	private int duration;

	public VeilOfNight(boolean isUpgraded) {
		super(ID, "Veil of Night", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF,
				EquipmentType.OFFHAND, EquipmentProperties.ofUsable(15, 0, 10, tp.range));
		duration = isUpgraded ? 15 : 10;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		EquipmentInstance inst = new EquipmentInstance(data, sessionEq, slot, es);
		inst.setAction((pdata, in) -> {
			Player p = data.getPlayer();
			int totalInsanity = 0;
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
				FightData fd = FightInstance.getFightData(ent);
				if (fd.hasStatus(StatusType.INSANITY)) {
					totalInsanity += fd.getStatus(StatusType.INSANITY).getStacks();
				}
			}

			if (totalInsanity > 0) {
				data.addSimpleShield(p.getUniqueId(), totalInsanity, duration * 20, this);
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.RIGHT_CLICK, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ECHO_SHARD,
				"On cast, gain " + GlossaryTag.SHIELDS.tag(this) + " equal to the total "
				+ GlossaryTag.INSANITY.tag(this) + " stacks of enemies in range "
				+ DescUtil.duration(duration, true) + ".");
	}
}