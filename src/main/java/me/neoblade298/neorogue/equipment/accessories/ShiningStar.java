package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class ShiningStar extends Equipment {
	private static final String ID = "ShiningStar";
	private static final int SANCTIFIED_THRESHOLD = 5;
	private int lightIncrease;

	public ShiningStar(boolean isUpgraded) {
		super(ID, "Shining Star", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		lightIncrease = isUpgraded ? 3 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta stacks = new ActionMeta();
		String buffId = id + slot;
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			if (FightInstance.getFightData(ev.getTarget()).getStatus(StatusType.SANCTIFIED).getStacks()
					< SANCTIFIED_THRESHOLD) return TriggerResult.keep();

			stacks.addCount(1);
			data.addDamageBuff(DamageBuffType.of(DamageCategory.LIGHT),
					Buff.multiplier(data, stacks.getCount() * lightIncrease * 0.01,
							StatTracker.damageBuffAlly(buffId, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR, "Basic attacks against enemies with at least "
				+ GlossaryTag.SANCTIFIED.tag(this, SANCTIFIED_THRESHOLD) + " permanently increase your "
				+ GlossaryTag.LIGHT.tag(this) + " damage by " + DescUtil.val(lightIncrease + "%") + " for the fight.");
	}
}