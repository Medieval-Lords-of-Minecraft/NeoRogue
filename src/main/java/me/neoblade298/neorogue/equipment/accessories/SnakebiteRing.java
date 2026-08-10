package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class SnakebiteRing extends Equipment {
	private static final String ID = "SnakebiteRing";
	private double multiplier;
	private int multiplierPercent;

	public SnakebiteRing(boolean isUpgraded) {
		super(ID, "Snakebite Ring", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF, EquipmentType.ACCESSORY);
		multiplierPercent = isUpgraded ? 15 : 10;
		multiplier = multiplierPercent * 0.01;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			if (!ev.getMeta().containsType(DamageCategory.DIRECT)) return TriggerResult.keep();
			FightData target = FightInstance.getFightData(ev.getTarget());
			if (target == null || !target.hasStatus(StatusType.POISON)) return TriggerResult.keep();
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.DIRECT), Buff.multiplier(data, multiplier,
					BuffStatTracker.damageBuffAlly(id + slot, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FERMENTED_SPIDER_EYE, "Deal " + DescUtil.yellow(multiplierPercent + "%")
				+ " increased " + GlossaryTag.DIRECT.tag(this) + " damage to enemies with " + GlossaryTag.POISON.tag(this) + ".");
	}
}