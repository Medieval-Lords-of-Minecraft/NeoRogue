package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class Advantage extends Equipment implements Power {
	private static final String ID = "Advantage";
	private int shields, thres = 15;
	
	public Advantage(boolean isUpgraded) {
		super(ID, "Advantage", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		shields = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	private static final int ACTIVATION_THRES = 3;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta count = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.INJURY)) return TriggerResult.keep();
			count.addCount(1);
			if (count.getCount() < ACTIVATION_THRES) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		StandardPriorityAction act = new StandardPriorityAction(id);
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-active", Trigger.APPLY_STATUS, (pdata2, in2) -> {
					Player p2 = data.getPlayer();
					ApplyStatusEvent ev2 = (ApplyStatusEvent) in2;
					if (!ev2.isStatus(StatusType.INJURY)) return TriggerResult.keep();
					act.addCount(ev2.getStacks());
					if (act.getCount() >= thres) {
						data.addPermanentShield(p2.getUniqueId(), shields * (act.getCount() / thres));
						act.setCount(act.getCount() % thres);
					}
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SPYGLASS,
				GlossaryTag.POWER.tag(this) + ". Activates after applying " + GlossaryTag.INJURY.tag(this) + " " + DescUtil.white(3) + " times. For every " + DescUtil.white(thres) + " stacks of " + GlossaryTag.INJURY.tag(this) + " you apply, " +
				"gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + ".");
	}
}
