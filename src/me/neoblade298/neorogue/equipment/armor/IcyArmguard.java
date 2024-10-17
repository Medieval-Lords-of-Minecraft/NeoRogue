package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class IcyArmguard extends Equipment {
	private static final String ID = "icyArmguard";
	private int damageReduction, shields, thres;
	
	public IcyArmguard(boolean isUpgraded) {
		super(ID, "Icy Armguard", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ARMOR);
		damageReduction = 3;
		shields = isUpgraded ? 5 : 3;
		thres = isUpgraded ? 5 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addBuff(data, false, false, BuffType.PHYSICAL, damageReduction);

		StandardPriorityAction act = new StandardPriorityAction(id);
		act.setAction((pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.FROST)) return TriggerResult.keep();
			act.addCount(ev.getStacks());
			if (act.getCount() >= thres) {
				data.addSimpleShield(p.getUniqueId(), shields * act.getCount() / thres, 100);
				act.setCount(act.getCount() % thres);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(ID, Trigger.APPLY_STATUS, act);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RABBIT_HIDE, "Decrease all " + GlossaryTag.PHYSICAL.tag(this) + " damage by <yellow>" + damageReduction + "</yellow>. " +
				"Also grants " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>5s</white>] for every " +
				GlossaryTag.FROST.tag(this, thres, true) + " you apply.");
	}
}
