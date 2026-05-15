package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ChosenOfTheLight extends Equipment {
	private static final String ID = "ChosenOfTheLight";
	private double mult;
	private int heal, multStr;
	
	public ChosenOfTheLight(boolean isUpgraded) {
		super(ID, "Chosen of the Light", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		heal = 1;
		mult = isUpgraded ? 0.3 : 0.2;
		multStr = (int) (mult * 100);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	private static final int ACTIVATION_THRES = 3;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.SANCTIFIED)) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < ACTIVATION_THRES) return TriggerResult.keep();

			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			data.addTrigger(id, Trigger.APPLY_STATUS, (pdata2, in2) -> {
				ApplyStatusEvent ev2 = (ApplyStatusEvent) in2;
				if (!ev2.isStatus(StatusType.SANCTIFIED)) return TriggerResult.keep();
				data.addHealth(heal);
				data.addDamageBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.multiplier(data, mult, BuffStatTracker.damageBuffAlly(id + slot, this, true)), 200);
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_ORE,
				GlossaryTag.POWER.tag(this) + ". Whenever you apply " + GlossaryTag.SANCTIFIED.tag(this) + ", heal for " +
				DescUtil.white(heal) + " and increase your " + GlossaryTag.MAGICAL.tag(this) + " damage by " +
				DescUtil.yellow(multStr + "%") + " [<white>10s</white>], stackable.");
	}
}
