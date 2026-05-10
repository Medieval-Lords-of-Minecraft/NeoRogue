package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class ChosenOfTheLight extends Equipment {
	private static final String ID = "ChosenOfTheLight";
	private double mult;
	private int heal, multStr;
	
	public ChosenOfTheLight(boolean isUpgraded) {
		super(ID, "Chosen of the Light", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 20, 0, 0));
		heal = 1;
		mult = isUpgraded ? 0.3 : 0.2;
		multStr = (int) (mult * 100);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			data.addTrigger(id, Trigger.APPLY_STATUS, (pdata2, in2) -> {
				ApplyStatusEvent ev = (ApplyStatusEvent) in2;
				if (!ev.isStatus(StatusType.SANCTIFIED)) return TriggerResult.keep();
				data.addHealth(heal);
				data.addDamageBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.multiplier(data, mult, BuffStatTracker.damageBuffAlly(id + slot, this, true)), 200);
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_ORE,
				GlossaryTag.POWER.tag(this) + ". Whenever you apply " + GlossaryTag.SANCTIFIED.tag(this) + ", heal for " +
				DescUtil.white(heal) + " and increase your " + GlossaryTag.MAGICAL.tag(this) + " damage by " +
				DescUtil.yellow(multStr + "%") + " [<white>10s</white>], stackable.");
	}
}
