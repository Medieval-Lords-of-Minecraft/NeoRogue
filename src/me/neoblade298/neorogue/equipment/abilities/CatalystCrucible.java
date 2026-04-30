package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusClass;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class CatalystCrucible extends Equipment {
	private static final String ID = "CatalystCrucible";
	private double mult;
	private int multStr, mana;
	
	public CatalystCrucible(boolean isUpgraded) {
		super(ID, "Catalyst Crucible", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		mult = isUpgraded ? 0.25 : 0.15;
		multStr = (int) (mult * 100);
		mana = isUpgraded ? 6 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		// Add permanent magical damage buff
		data.addDamageBuff(DamageBuffType.of(DamageCategory.MAGICAL), 
			Buff.multiplier(data, mult, BuffStatTracker.damageBuffAlly(id + slot, this)));
		
		// Grant mana when applying negative status
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (ev.getStatusClass() == StatusClass.NEGATIVE) {
				data.addMana(mana);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BREWING_STAND,
				"Passive. Increase all " + GlossaryTag.MAGICAL.tag(this) + " damage by " + 
				DescUtil.yellow(multStr + "%") + ". Whenever you apply a negative status, gain " + 
				DescUtil.yellow(mana) + " mana.");
	}
}
