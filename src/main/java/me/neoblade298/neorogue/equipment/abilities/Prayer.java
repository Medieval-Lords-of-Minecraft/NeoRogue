package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class Prayer extends Equipment implements Power {
	private static final String ID = "Prayer";
	private int heal, thres, inc;
	
	public Prayer(boolean isUpgraded) {
		super(ID, "Prayer", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		heal = isUpgraded ? 15 : 10;
		thres = isUpgraded ? 55 : 80;
		inc = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (!ev.getStatusId().equals(StatusType.SANCTIFIED.name())) return TriggerResult.keep();
			ev.getStacksBuffList().add(Buff.increase(data, inc, BuffStatTracker.of(ID + slot, this, "Sanctified Increased")));
			if (am.addCount(ev.getStacks()) > thres && !am.getBool()) {
				if (activatePower(data, slot, es)) {
					am.setBool(true);
				}
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		Player p = data.getPlayer();
		FightInstance.giveHeal(p, heal, p);
		Sounds.success.play(p, p);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_TORCH,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after applying " + GlossaryTag.SANCTIFIED.tag(this, thres, true) + " stacks. Increase " + GlossaryTag.SANCTIFIED.tag(this) + " application by " + DescUtil.yellow(inc) + ". Applying " + GlossaryTag.SANCTIFIED.tag(this, thres, true) + " heals you for "
				+ DescUtil.yellow(heal) + " once per fight.");
	}
}
