package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;
import me.neoblade298.neocore.bukkit.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Fade extends Equipment {
	private static final String ID = "Fade";
	private int duration, stealthDur;
	
	public Fade(boolean isUpgraded) {
		super(ID, "Fade", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		duration = 2;
		stealthDur = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.RECEIVE_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.STEALTH)) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < 2) return TriggerResult.keep();
			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msgRaw(p, Component.text("").append(hoverable).append(Component.text(" was activated", NamedTextColor.GRAY)));

			data.addTrigger(id, Trigger.PRE_RECEIVE_STATUS, (pdata2, in2) -> {
				PreApplyStatusEvent ev2 = (PreApplyStatusEvent) in2;
				if (!ev2.getStatusId().equals(StatusType.STEALTH.name())) return TriggerResult.keep();
				ev2.getDurationBuffList().add(new Buff(data, duration, 0, BuffStatTracker.ignored(this)));
				return TriggerResult.keep();
			});

			StandardPriorityAction inst = new StandardPriorityAction(ID);
			inst.setAction((pdata3, in3) -> {
				data.applyStatus(StatusType.STEALTH, data, 1, stealthDur * 20);
				return TriggerResult.keep();
			});
			data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, inst);

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_TORCH,
				GlossaryTag.POWER.tag(this) + ". Activates after receiving " + GlossaryTag.STEALTH.tag(this) + " " + DescUtil.white(2) + " times. Whenever you receive " + GlossaryTag.STEALTH.tag(this) + ", increase its duration by " + DescUtil.white(duration + "s") + "."
				+ " Basic attacks additionally grant you " + GlossaryTag.STEALTH.tag(this, 1, false) + " " + DescUtil.duration(stealthDur, true) + ".");
	}
}
