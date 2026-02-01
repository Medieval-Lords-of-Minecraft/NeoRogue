package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class HerculeanStrength extends Equipment {
	private static final String ID = "HerculeanStrength";
	private int strength, conc, bers;
	
	public HerculeanStrength(boolean isUpgraded) {
		super(ID, "Herculean Strength", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
				strength = isUpgraded ? 6 : 4;
				bers = isUpgraded ? 5 : 3;
				conc = isUpgraded ? 10 : 7;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.applyStatus(StatusType.BERSERK, data, bers, -1);

		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.STRENGTH)) return TriggerResult.keep();
			ev.getStacksBuffList().add(Buff.increase(data, strength, BuffStatTracker.statusBuff(id + slot, this)));
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			BasicAttackEvent ev = (BasicAttackEvent) in;
			FightInstance.applyStatus(ev.getTarget(), StatusType.CONCUSSED, data, conc, -1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR,
				"Passive. Increase all " + GlossaryTag.STRENGTH.tag(this) + " application by " + DescUtil.yellow(strength) +
				", start fights with " + GlossaryTag.BERSERK.tag(this,bers, true) + ", and apply " + GlossaryTag.CONCUSSED.tag(this, conc, true) +
				" with your basic attacks.");
	}
}
