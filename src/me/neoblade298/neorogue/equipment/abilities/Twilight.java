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
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Twilight extends Equipment {
	private static final String ID = "Twilight";
	private int duration, evade;
	
	public Twilight(boolean isUpgraded) {
		super(ID, "Twilight", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		duration = isUpgraded ? 4 : 2;
		evade = isUpgraded ? 2 : 1;
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
			if (data.getStamina() < data.getMaxStamina() * 0.5) return TriggerResult.keep();
			if (data.getMana() < data.getMaxMana() * 0.5) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < 1) return TriggerResult.keep();
			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			StandardPriorityAction inst = new StandardPriorityAction(ID);
			inst.setAction((pdata2, in2) -> {
				PreApplyStatusEvent ev2 = (PreApplyStatusEvent) in2;
				if (!ev2.getStatusId().equals(StatusType.STEALTH.name())) return TriggerResult.keep();
				ev2.getDurationBuffList().add(new Buff(data, duration, 0, BuffStatTracker.ignored(this)));
				Player p2 = data.getPlayer();
				FightInstance.applyStatus(p2, StatusType.EVADE, data, evade, 160);
				data.addStamina(8);
				return TriggerResult.keep();
			});
			data.addTrigger(id, Trigger.PRE_RECEIVE_STATUS, inst);

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ECHO_SHARD,
				GlossaryTag.POWER.tag(this) + ". Activates after receiving " + GlossaryTag.STEALTH.tag(this) + " once while above " + DescUtil.white("50%") + " mana and stamina. Whenever you receive " + GlossaryTag.STEALTH.tag(this) + ", increase its duration by " + DescUtil.yellow(duration) + ", " +
				"gain " + GlossaryTag.EVADE.tag(this, evade, true) + " [<white>8s</white>], and " + DescUtil.white(8) + " stamina.");
	}
}
