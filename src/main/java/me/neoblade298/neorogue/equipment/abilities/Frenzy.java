package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.WeaponSwingEvent;

public class Frenzy extends Equipment implements Power {
	private static final String ID = "Frenzy";
	private int atkSpeed;
	private static final int CUTOFF = 5;
	
	public Frenzy(boolean isUpgraded) {
		super(ID, "Frenzy", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		atkSpeed = isUpgraded ? 10 : 7;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addReforge(Bloodlust.get(), BloodFrenzy.get());
	}

	private static final int ACTIVATION_THRES = 8;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (data.getStatus(StatusType.BERSERK).getStacks() < 5) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < ACTIVATION_THRES) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTrigger(id, Trigger.WEAPON_SWING, (pdata2, in2) -> {
			int mult = Math.min(4, data.getStatus(StatusType.BERSERK).getStacks() / CUTOFF);
			WeaponSwingEvent ev = (WeaponSwingEvent) in2;
			ev.getAttackSpeedBuffList().add(new Buff(data, 0, mult * atkSpeed * 0.01, BuffStatTracker.ignored(this)));
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.TIPPED_ARROW,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after basic attacking " + DescUtil.white(ACTIVATION_THRES) + " times with " + DescUtil.white(5) + "+ " + GlossaryTag.BERSERK.tag(this) + ". For every " + GlossaryTag.BERSERK.tag(this, CUTOFF, false) + " you have, up to " + DescUtil.white(20) + ", increase your attack speed by"
				+ " " + DescUtil.yellow(atkSpeed + "%") + ".");
	}
}
