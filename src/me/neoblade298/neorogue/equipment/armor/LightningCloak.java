package me.neoblade298.neorogue.equipment.armor;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class LightningCloak extends Equipment {
	private static final String ID = "LightningCloak";
	private int base, threshold, def;
	
	public LightningCloak(boolean isUpgraded) {
		super(ID, "Lightning Cloak", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ARMOR);
		base = isUpgraded ? 250 : 150;
		threshold = isUpgraded ? 600 : 900;
		def = 12;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		data.addTrigger(ID, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			ReceiveDamageEvent ev = (ReceiveDamageEvent) in;
			ev.getDamager().applyStatus(StatusType.ELECTRIFIED, data, base, -1);
			return TriggerResult.keep();
		});
		
		StandardPriorityAction inst = new StandardPriorityAction(ID);
		data.addTrigger(ID, Trigger.APPLY_STATUS, (pdata, in) -> {
			Player p = data.getPlayer();
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.ELECTRIFIED)) return TriggerResult.keep();
			inst.addCount(ev.getStacks());
			if (inst.getCount() >= threshold) {
				data.addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.increase(data, def, StatTracker.defenseBuffAlly(buffId, this)));
				Util.msg(p, this.hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));
				return TriggerResult.remove();
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_BOOTS, "Whenever you receive damage, apply " + GlossaryTag.ELECTRIFIED.tag(this, base, true) + ". "
				+ "Upon applying " + GlossaryTag.ELECTRIFIED.tag(this, threshold, true) + ", gain <yellow>"
				+ def + "</yellow> " + GlossaryTag.MAGICAL.tag(this) + " defense.");
	}
}
