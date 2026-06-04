package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
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

public class ChosenOfTheLight extends Equipment implements Power {
	private static final String ID = "ChosenOfTheLight";
	private double mult;
	private int heal, multStr;
	
	public ChosenOfTheLight(boolean isUpgraded) {
		super(ID, "Chosen of the Light", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		heal = 1;
		mult = isUpgraded ? 0.25 : 0.15;
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

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-active", Trigger.APPLY_STATUS, (pdata2, in2) -> {
					ApplyStatusEvent ev2 = (ApplyStatusEvent) in2;
					if (!ev2.isStatus(StatusType.SANCTIFIED)) return TriggerResult.keep();
					data.addHealth(heal);
					data.addDamageBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.multiplier(data, mult, BuffStatTracker.damageBuffAlly(id + slot, ChosenOfTheLight.this, true)), 200);
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));
	}

	@Override
	public void setupItem() {
	item = createItem(Material.IRON_ORE,
			GlossaryTag.POWER.tag(this) + ". Activates after applying " + GlossaryTag.SANCTIFIED.tag(this) + " " + DescUtil.white(ACTIVATION_THRES) + " times. Whenever you apply " + GlossaryTag.SANCTIFIED.tag(this) + ", heal for " +
			DescUtil.white(heal) + " and increase your " + GlossaryTag.MAGICAL.tag(this) + " damage by " +
				DescUtil.yellow(multStr + "%") + " [<white>10s</white>], stackable.");
	}
}
