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
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Prayer extends Equipment {
	private static final String ID = "Prayer";
	private int heal, thres, inc;
	
	public Prayer(boolean isUpgraded) {
		super(ID, "Prayer", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		heal = isUpgraded ? 15 : 10;
		thres = isUpgraded ? 300 : 400;
		inc = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (!ev.getStatusId().equals(StatusType.SANCTIFIED.name())) return TriggerResult.keep();
			ev.getStacksBuffList().add(Buff.increase(data, inc, BuffStatTracker.of(ID + slot, this, "Sanctified Increased")));
			if (am.addCount(ev.getStacks()) > thres && !am.getBool()) {
				FightInstance.giveHeal(p, heal, p);
				Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));
				Sounds.success.play(p, p);
				am.setBool(true);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_TORCH,
				"Passive. Increase " + GlossaryTag.SANCTIFIED.tag(this) + " application by " + DescUtil.yellow(inc) + ". Applying " + GlossaryTag.SANCTIFIED.tag(this, heal, true) + " heals you for "
				+ DescUtil.yellow(heal) + ".");
	}
}
