package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class SilentSteps2 extends Equipment implements Power {
	private static final String ID = "SilentSteps2";
	private int duration, damage;
	
	public SilentSteps2(boolean isUpgraded) {
		super(ID, "Silent Steps II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		duration = 3;
		damage = isUpgraded ? 30 : 25;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.RECEIVE_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.STEALTH)) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < 2) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTrigger(id, Trigger.PRE_RECEIVE_STATUS, (pdata2, in2) -> {
			PreApplyStatusEvent ev2 = (PreApplyStatusEvent) in2;
			if (!ev2.getStatusId().equals(StatusType.STEALTH.name())) return TriggerResult.keep();
			ev2.getDurationBuffList().add(new Buff(data, duration, 0, BuffStatTracker.ignored(this)));
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata3, in3) -> {
			if (!pdata3.hasStatus(StatusType.STEALTH)) return TriggerResult.keep();
			PreDealDamageEvent ev3 = (PreDealDamageEvent) in3;
			ev3.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
					new Buff(pdata3, damage, 0, StatTracker.damageBuffAlly(id + slot, this)));
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_BOOTS,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after receiving " + GlossaryTag.STEALTH.tag(this) + " twice. Whenever you receive " + GlossaryTag.STEALTH.tag(this) + ", increase its duration by " + DescUtil.white(duration)
						+ " and its stacks by " + DescUtil.white(1) + "." +
						" Damage dealt is increased by " + DescUtil.yellow(damage) + " if you have " + GlossaryTag.STEALTH.tag(this) +".");
	}
}
