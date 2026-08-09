package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class OathOfTheColossus extends Equipment {
	private static final String ID = "OathOfTheColossus";

	public OathOfTheColossus(boolean isUpgraded) {
		super(ID, "Oath of the Colossus", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			if (ev.getMeta().isBasicAttack() || !ev.getMeta().containsType(DamageCategory.DIRECT)) {
				return TriggerResult.keep();
			}
			double shields = data.getShields().getAmount();
			if (shields > 0) ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.DIRECT),
					Buff.increase(data, shields, BuffStatTracker.damageBuffAlly(id + slot, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TOTEM_OF_UNDYING, "Increase non-basic " + GlossaryTag.DIRECT.tag(this)
				+ " damage by your current " + GlossaryTag.SHIELDS.tag(this) + ".");
	}
}