package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class RingOfAttrition extends Equipment {
	private static final String ID = "RingOfAttrition";
	private int damage;

	public RingOfAttrition(boolean isUpgraded) {
		super(ID, "Ring of Attrition", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF, EquipmentType.ACCESSORY);
		damage = isUpgraded ? 30 : 20;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.INSANITY)) return TriggerResult.keep();
			FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.DARK,
					DamageStatTracker.of(id + slot, this)), ev.getTarget().getEntity());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ENDER_EYE, "Applying " + GlossaryTag.INSANITY.tag(this) + " also deals "
				+ GlossaryTag.DARK.tag(this, damage) + " damage.");
	}
}