package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
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

public class Infinity extends Equipment {
	private static final String ID = "Infinity";
	private double stackMultiplier;

	public Infinity(boolean isUpgraded) {
		super(ID, "Infinity", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY, EquipmentProperties.none());
		stackMultiplier = isUpgraded ? 1.5 : 1.0;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		String buffId = id + slot;
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			int protect = data.hasStatus(StatusType.PROTECT) ? data.getStatus(StatusType.PROTECT).getStacks() : 0;
			int shell = data.hasStatus(StatusType.SHELL) ? data.getStatus(StatusType.SHELL).getStacks() : 0;
			double multiplier = (protect + shell) * stackMultiplier / 100.0;
			if (multiplier > 0) {
				((PreDealDamageEvent) in).getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.DIRECT),
						Buff.multiplier(data, multiplier, StatTracker.damageBuffAlly(buffId, this)));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.END_CRYSTAL, GlossaryTag.PASSIVE.tag(this) + ". Increase direct damage by "
				+ DescUtil.val(stackMultiplier) + "% per combined stack of " + GlossaryTag.PROTECT.tag(this)
				+ " and " + GlossaryTag.SHELL.tag(this) + ".");
	}
}