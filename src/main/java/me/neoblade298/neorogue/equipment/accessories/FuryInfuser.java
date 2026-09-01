package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class FuryInfuser extends Equipment {
	private static final String ID = "FuryInfuser";
	private static final int DAMAGE_INCREASE = 30;
	private int threshold;

	public FuryInfuser(boolean isUpgraded) {
		super(ID, "Fury Infuser", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		threshold = isUpgraded ? 7 : 10;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		String buffId = id + slot;
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			if (data.getStatus(StatusType.BERSERK).getStacks() < threshold) return TriggerResult.keep();

			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.EARTHEN),
					Buff.multiplier(data, DAMAGE_INCREASE * 0.01,
							BuffStatTracker.damageBuffAlly(buffId, this)));
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.BLUNT),
					Buff.multiplier(data, DAMAGE_INCREASE * 0.01,
							BuffStatTracker.damageBuffAlly(buffId, this)));
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.LIGHT),
					Buff.multiplier(data, DAMAGE_INCREASE * 0.01,
							BuffStatTracker.damageBuffAlly(buffId, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIRE_CHARGE, "While you have at least "
				+ GlossaryTag.BERSERK.tag(this, threshold) + ", increase " + GlossaryTag.EARTHEN.tag(this)
				+ ", " + GlossaryTag.BLUNT.tag(this) + ", and " + GlossaryTag.LIGHT.tag(this)
				+ " damage by " + DescUtil.val(DAMAGE_INCREASE + "%") + ".");
	}
}