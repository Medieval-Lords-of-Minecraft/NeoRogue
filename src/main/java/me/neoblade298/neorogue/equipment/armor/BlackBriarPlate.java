package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class BlackBriarPlate extends Equipment {
	private static final String ID = "BlackBriarPlate";
	private static final int PHYSICAL_REDUCTION = 2, THORNS_INCREASE = 100;
	private int thorns;

	public BlackBriarPlate(boolean isUpgraded) {
		super(ID, "Black Briar Plate", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		thorns = isUpgraded ? 150 : 100;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.PHYSICAL), Buff.increase(data, PHYSICAL_REDUCTION,
				StatTracker.defenseBuffAlly(id + slot, this)));
		data.applyStatus(StatusType.THORNS, data, thorns, -1, this);
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			if (!ev.getMeta().containsType(DamageType.THORNS)) return TriggerResult.keep();

			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.DIRECT),
					Buff.multiplier(data, THORNS_INCREASE * 0.01,
							BuffStatTracker.damageBuffAlly(id + slot, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_CHESTPLATE, "Reduce " + GlossaryTag.PHYSICAL.tag(this)
				+ " damage taken by " + DescUtil.white(PHYSICAL_REDUCTION) + ". Start fights with "
				+ GlossaryTag.THORNS.tag(this, thorns) + ". Increase " + GlossaryTag.THORNS.tag(this)
				+ " damage by " + DescUtil.white(THORNS_INCREASE + "%") + ".");
	}
}