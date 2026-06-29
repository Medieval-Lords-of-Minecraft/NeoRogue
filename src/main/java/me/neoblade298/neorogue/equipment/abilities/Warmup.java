package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Warmup extends Equipment implements Power {
	private static final String ID = "Warmup";
	private int timer, shields;

	public Warmup(boolean isUpgraded) {
		super(ID, "Warmup", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		timer = isUpgraded ? 7 : 10;
		shields = isUpgraded ? 15 : 10;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		StandardPriorityAction inst = new StandardPriorityAction(ID);
		inst.setCount(timer);
		inst.setAction((pdata, in) -> {
			inst.addCount(-1);
			if (inst.getCount() <= 0) {
				if (activatePower(data, slot, es)) return TriggerResult.remove();
				inst.setCount(1); // reset so it tries again next tick if cancelled
			}
			return TriggerResult.keep();
		});
		data.addTrigger(ID, Trigger.PLAYER_TICK, inst);
		data.addTrigger(ID, Trigger.RECEIVE_HEALTH_DAMAGE, (pdata, in) -> {
			inst.addCount(1);
			if (inst.getCount() < -5) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addStaminaRegen(1);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RED_DYE,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after surviving " + DescUtil.yellow(timer + "s") + " without taking damage. Gain " + DescUtil.white(1) + " stamina regen and " + GlossaryTag.SHIELDS.tag(this, shields, true) + "."
				+ " Taking health damage increases the timer by " + DescUtil.white(1) + ".");
	}
}
