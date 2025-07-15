package me.neoblade298.neorogue.equipment.armor;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class NullifyingCloak extends Equipment {
	private static final String ID = "nullifyingCloak";
	private int def;
	public NullifyingCloak(boolean isUpgraded) {
		super(ID, "Nullifying Cloak", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ARMOR);
		def = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			ReceiveDamageEvent ev = (ReceiveDamageEvent) in;
			int rifts = data.getRifts().size();
			ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.increase(data, rifts * def, StatTracker.defenseBuffAlly(buffId, this, false)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER, GlossaryTag.GENERAL.tag(this) + " damage is decreased by " + DescUtil.yellow(def) + " for every " + GlossaryTag.RIFT.tag(this) + " you have active, " +
		"up to <white>3</white> rifts.");
	}
}
