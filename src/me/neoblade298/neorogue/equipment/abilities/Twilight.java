package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class Twilight extends Equipment {
	private static final String ID = "Twilight";
	private int duration, evade;
	
	public Twilight(boolean isUpgraded) {
		super(ID, "Twilight", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(25, 20, 0, 0));
		duration = isUpgraded ? 5 : 3;
		evade = isUpgraded ? 2 : 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			StandardPriorityAction inst = new StandardPriorityAction(ID);
			inst.setAction((pdata2, in2) -> {
				PreApplyStatusEvent ev = (PreApplyStatusEvent) in2;
				if (!ev.getStatusId().equals(StatusType.STEALTH.name())) return TriggerResult.keep();
				ev.getDurationBuffList().add(new Buff(data, duration, 0, BuffStatTracker.ignored(this)));
				Player p = data.getPlayer();
				FightInstance.applyStatus(p, StatusType.EVADE, data, evade, 160);
				data.addStamina(10);
				return TriggerResult.keep();
			});
			data.addTrigger(id, Trigger.PRE_RECEIVE_STATUS, inst);

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ECHO_SHARD,
				GlossaryTag.POWER.tag(this) + ". Whenever you receive " + GlossaryTag.STEALTH.tag(this) + ", increase its duration by " + DescUtil.yellow(duration) + ", " +
				"gain " + GlossaryTag.EVADE.tag(this, evade, true) + " [<white>8s</white>], and " + DescUtil.white(10) + " stamina.");
	}
}
