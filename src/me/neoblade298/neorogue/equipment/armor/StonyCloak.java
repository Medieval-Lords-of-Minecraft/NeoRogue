package me.neoblade298.neorogue.equipment.armor;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class StonyCloak extends Equipment {
	private static final String ID = "StonyCloak";
	private int baseReduc = 2, concReduc;
	
	public StonyCloak(boolean isUpgraded) {
		super(ID, "Stony Cloak", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ARMOR);
				concReduc = isUpgraded ? 4 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			ReceiveDamageEvent ev = (ReceiveDamageEvent) in;
			FightData fd = ev.getDamager();
			int reduc = baseReduc + (fd.hasStatus(StatusType.CONCUSSED) ? concReduc : 0);
			ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.increase(data, reduc, BuffStatTracker.defenseBuffAlly(buffId, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RABBIT_HIDE, "Decrease all " + GlossaryTag.GENERAL.tag(this) + " damage taken by <yellow>" + baseReduc + "</yellow>. Further reduce damage by " +
		DescUtil.yellow(concReduc) + " if the enemy is " + GlossaryTag.CONCUSSED.tag(this) + ".");
	}
}
