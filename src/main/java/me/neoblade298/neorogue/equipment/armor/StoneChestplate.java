package me.neoblade298.neorogue.equipment.armor;

import java.util.UUID;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class StoneChestplate extends Equipment {
	private static final String ID = "StoneChestplate";
	private static final int PHYSICAL_REDUCTION = 2;
	private int concussedReduction;

	public StoneChestplate(boolean isUpgraded) {
		super(ID, "Stone Chestplate", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		concussedReduction = isUpgraded ? 30 : 20;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.PHYSICAL), Buff.increase(data, PHYSICAL_REDUCTION,
				StatTracker.defenseBuffAlly(UUID.randomUUID().toString(), this)));
		String buffId = UUID.randomUUID().toString();
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			ReceiveDamageEvent ev = (ReceiveDamageEvent) in;
			FightData damager = ev.getDamager();
			if (!damager.hasStatus(StatusType.CONCUSSED)) return TriggerResult.keep();
			ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.DIRECT), Buff.multiplier(data,
					concussedReduction * 0.01, StatTracker.defenseBuffAlly(buffId, this, false)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CHAINMAIL_CHESTPLATE,
				"Reduce " + GlossaryTag.PHYSICAL.tag(this) + " damage taken by " + DescUtil.val(PHYSICAL_REDUCTION)
						+ ". " + GlossaryTag.DIRECT.tag(this) + " damage dealt by " + GlossaryTag.CONCUSSED.tag(this)
						+ " enemies is reduced by " + DescUtil.val(concussedReduction + "%") + ".");
	}
}
