package me.neoblade298.neorogue.equipment.accessories;

import java.util.UUID;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class BlightedEarrings extends Equipment {
	private static final String ID = "BlightedEarrings";
	private int inc;

	public BlightedEarrings(boolean isUpgraded) {
		super(ID, "Blighted Earrings", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY);
		inc = isUpgraded ? 50 : 30;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			if (!data.hasStatus(StatusType.CORRUPTION)) return TriggerResult.keep();
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
					Buff.multiplier(data, inc * 0.01, StatTracker.damageBuffAlly(buffId, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLD_NUGGET, "Deal " + DescUtil.yellow(inc + "%") +
				" more damage if you have " + GlossaryTag.CORRUPTION.tag(this) + ".");
	}
}