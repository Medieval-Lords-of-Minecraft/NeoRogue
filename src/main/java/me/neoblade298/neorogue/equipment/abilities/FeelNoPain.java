package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class FeelNoPain extends Equipment implements Power {
	private static final String ID = "FeelNoPain";
	private double reduc;
	private int reducString;
	private static final int THRES = 10, COUNT = 4, CUTOFF = THRES * COUNT;
	
	public FeelNoPain(boolean isUpgraded) {
		super(ID, "Feel No Pain", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		reduc = isUpgraded ? 0.07 : 0.04;
		reducString = (int) (reduc * 100);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.RECEIVE_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.BERSERK)) return TriggerResult.keep();
			am.addCount(1);
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.RECEIVE_DAMAGE, (pdata, in) -> {
			am.setBool(true);
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (am.getCount() < 3 || !am.getBool()) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata2, in2) -> {
			ReceiveDamageEvent ev = (ReceiveDamageEvent) in2;
			int stacks = data.getStatus(StatusType.BERSERK).getStacks();
			int ct = Math.min(COUNT, stacks / THRES);
			ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL),
					Buff.multiplier(data, ct * reduc, null));
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.IRON_HELMET,
				GlossaryTag.POWER.tag(this) + ". Activates after receiving " + DescUtil.white(3) + " " + GlossaryTag.BERSERK.tag(this) + " stacks and taking damage. Gain " + DescUtil.yellow(reducString + "%") + " damage reduction for every " + DescUtil.white(THRES) + " stacks of " + GlossaryTag.BERSERK.tag(this) + ", up to " +
				DescUtil.white(CUTOFF) + " stacks.");
	}
}
