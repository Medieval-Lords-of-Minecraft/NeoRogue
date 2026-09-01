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

public class VirulentCore extends Equipment {
	private static final String ID = "VirulentCore";
	private static final int POISON_THRESHOLD = 70;
	private final int damagePercent;

	public VirulentCore(boolean isUpgraded) {
		super(ID, "Virulent Core", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.ACCESSORY);
		damagePercent = isUpgraded ? 90 : 60;
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
			if (target == null || !target.hasStatus(StatusType.POISON)
					|| target.getStatus(StatusType.POISON).getStacks() <= POISON_THRESHOLD) return TriggerResult.keep();
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.DIRECT), Buff.multiplier(data,
					damagePercent * 0.01, BuffStatTracker.damageBuffAlly(id + slot, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SLIME_BALL, "Enemies above " + GlossaryTag.POISON.tag(this, POISON_THRESHOLD)
				+ " take " + DescUtil.val(damagePercent + "%") + " additional " + GlossaryTag.DIRECT.tag(this)
				+ " damage from you.");
	}
}