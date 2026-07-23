package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class Untouchable extends Equipment {
	private static final String ID = "Untouchable";
	private int threshold;
	
	public Untouchable(boolean isUpgraded) {
		super(ID, "Untouchable", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(10, 20, 0, 0));
		threshold = isUpgraded ? 3 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	public void setupReforges() {
		addReforge(Vanish.get(), Twilight.get());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			ActionMeta am = new ActionMeta();
			data.addTrigger(id, Trigger.RECEIVE_STATUS, (pdata2, in2) -> {
				ApplyStatusEvent ev = (ApplyStatusEvent) in2;
				if (!ev.isStatus(StatusType.STEALTH)) return TriggerResult.keep();
				am.addCount(ev.getStacks());
				if (am.getCount() >= threshold) {
					Player p = data.getPlayer();
					FightInstance.applyStatus(p, StatusType.EVADE, data, am.getCount() / threshold, -1, this);
					am.setCount(am.getCount() % threshold);
				}
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PHANTOM_MEMBRANE,
				GlossaryTag.POWER.tag(this) + ". Every " + DescUtil.val(threshold) + " stacks of " + GlossaryTag.STEALTH.tag(this) + " you receive, gain " + DescUtil.val(1) + " " + GlossaryTag.EVADE.tag(this) + ".");
	}
}
