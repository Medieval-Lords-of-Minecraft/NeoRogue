package me.neoblade298.neorogue.equipment.armor;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

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

public class IcyArmguard extends Equipment {
	private static final String ID = "IcyArmguard";
	private int damageReduction, shields, thres;
	
	public IcyArmguard(boolean isUpgraded) {
		super(ID, "Icy Armguard", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ARMOR);
		damageReduction = 4;
		shields = isUpgraded ? 3 : 2;
		thres = isUpgraded ? 40 : 60;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.increase(data, damageReduction, StatTracker.defenseBuffAlly(UUID.randomUUID().toString(), this)));

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
		item = createItem(Material.RABBIT_HIDE, "Decrease all " + GlossaryTag.MAGICAL.tag(this) + " damage received by <yellow>" + damageReduction + "</yellow>. " +
				"Also grants " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>5s</white>] for every " +
				GlossaryTag.FROST.tag(this, thres, true) + " you apply.");
	}
}
