package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
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

public class LifeThief extends Equipment {
	private static final String ID = "LifeThief";
	private int cutoff = 10, heal;
	
	public LifeThief(boolean isUpgraded) {
		super(ID, "Life Thief", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ACCESSORY);
		heal = isUpgraded ? 25 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction inst = new StandardPriorityAction(id);
		inst.setAction((pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.STEALTH)) return TriggerResult.keep();
			inst.addCount(ev.getStacks());
			if (inst.getCount() >= cutoff) {
				Sounds.success.play(p, p);
				Util.msg(p, Component.text("").append(hoverable).append(Component.text(" can be activated", NamedTextColor.GRAY)));
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.APPLY_STATUS, inst);
		
		data.addTrigger(ID, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (inst.getCount() < cutoff) return TriggerResult.keep();
			Sounds.fire.play(p, p);
			data.addHealth(heal);
			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RABBIT_FOOT, "Passive. Upon applying <yellow>" + cutoff + " </yellow>stacks of " + GlossaryTag.STEALTH.tag(this) + ","
				+ " your next basic attack heals you for <yellow>" + heal + "</yellow>. Once per fight.");
	}
}
