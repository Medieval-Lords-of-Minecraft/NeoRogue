package me.neoblade298.neorogue.equipment.armor;

import java.util.UUID;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
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

public class IcySigil extends Equipment {
	private static final String ID = "IcySigil";
	private int def, thres;
	
	public IcySigil(boolean isUpgraded) {
		super(ID, "Icy Sigil", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ARMOR);
				def = isUpgraded ? 5 : 3;
		thres = 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.increase(data, def, StatTracker.defenseBuffAlly(UUID.randomUUID().toString(), this)));

		String buffId = UUID.randomUUID().toString();
		StandardPriorityAction act = new StandardPriorityAction(id);
		act.setAction((pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.FROST)) return TriggerResult.keep();
			act.addCount(ev.getStacks());
			if (act.getCount() >= thres) {
				data.addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.increase(data, def, StatTracker.defenseBuffAlly(buffId, this)), 100);
				act.setCount(act.getCount() % thres);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(ID, Trigger.APPLY_STATUS, act);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RABBIT_HIDE, "Decrease all " + GlossaryTag.MAGICAL.tag(this) + " damage taken by <yellow>" + def + "</yellow>. " +
				"Double this effect for <white>5s</white> (unstackable) every time you apply " + GlossaryTag.FROST.tag(this, thres, false) + ".");
	}
}
