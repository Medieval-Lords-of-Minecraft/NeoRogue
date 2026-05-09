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
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class Advantage extends Equipment {
	private static final String ID = "Advantage";
	private int shields, thres = 75;
	
	public Advantage(boolean isUpgraded) {
		super(ID, "Advantage", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 0, 0, 0));
		shields = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());
			
			StandardPriorityAction act = new StandardPriorityAction(id);
			data.addTrigger(id, Trigger.APPLY_STATUS, (pdata2, in2) -> {
				Player p = data.getPlayer();
				ApplyStatusEvent ev = (ApplyStatusEvent) in2;
				if (!ev.isStatus(StatusType.INJURY)) return TriggerResult.keep();
				act.addCount(ev.getStacks());
				if (act.getCount() >= thres) {
					data.addPermanentShield(p.getUniqueId(), shields * (act.getCount() / thres));
					act.setCount(act.getCount() % thres);
				}
				return TriggerResult.keep();
			});
			
			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SPYGLASS,
				GlossaryTag.POWER.tag(this) + ". For every " + DescUtil.white(thres) + " stacks of " + GlossaryTag.INJURY.tag(this) + " you apply, " +
				"gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + ".");
	}
}
