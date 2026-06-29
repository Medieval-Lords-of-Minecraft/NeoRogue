package me.neoblade298.neorogue.equipment.abilities;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class DangerClose extends Equipment implements Power {
	private static final String ID = "DangerClose";
	private SessionEquipment sessionEq;
	private double damageIncrease;
	
	public DangerClose(boolean isUpgraded) {
		super(ID, "Danger Close", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		damageIncrease = isUpgraded ? 0.08 : 0.05;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		this.sessionEq = sessionEq;
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.EVADE, (pdata, in) -> {
			if (data.getStamina() < data.getMaxStamina() * 0.5) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < 1) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		String buffId = UUID.randomUUID().toString();
		ActionMeta stacks = new ActionMeta();
		ItemStack icon = item.clone();
		EquipmentInstance inst = new EquipmentInstance(data, sessionEq, slot, es);
		data.addTrigger(id, Trigger.RECEIVE_STATUS, (pdata2, in2) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in2;
			if (!ev.isStatus(StatusType.EVADE)) return TriggerResult.keep();
			
			// Increase stack counter
			stacks.addCount(1);
			icon.setAmount(stacks.getCount());
			inst.setIcon(icon);
			
			// Add permanent damage buff
			data.addDamageBuff(DamageBuffType.of(DamageCategory.PHYSICAL), 
					Buff.multiplier(data, damageIncrease, StatTracker.damageBuffAlly(buffId, this, true)));
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FEATHER,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after evading once while above " + DescUtil.white("50%") + " stamina. Whenever you " + GlossaryTag.EVADE.tag(this) + ", increase your " + 
				GlossaryTag.PHYSICAL.tag(this) + " damage by " + DescUtil.yellow((int)(damageIncrease * 100) + "%") + ".");
	}
}
