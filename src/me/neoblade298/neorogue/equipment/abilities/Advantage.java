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
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Advantage extends Equipment {
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
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta count = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.INJURY)) return TriggerResult.keep();
			count.addCount(1);
			if (count.getCount() < ACTIVATION_THRES) return TriggerResult.keep();

			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			StandardPriorityAction act = new StandardPriorityAction(id);
			data.addTrigger(id, Trigger.APPLY_STATUS, (pdata2, in2) -> {
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

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SPYGLASS,
				GlossaryTag.POWER.tag(this) + ". For every " + DescUtil.white(thres) + " stacks of " + GlossaryTag.INJURY.tag(this) + " you apply, " +
				"gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + ".");
	}
}
