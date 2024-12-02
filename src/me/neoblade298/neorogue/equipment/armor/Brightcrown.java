package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;

public class Brightcrown extends Equipment {
	private static final String ID = "brightcrown";
	private int def, sanct;
	
	public Brightcrown(boolean isUpgraded) {
		super(ID, "Brightcrown", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		def = isUpgraded ? 5 : 3;
		sanct = isUpgraded ? 35 : 25;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addBuff(data, false, false, DamageCategory.MAGICAL, def);
		data.addTrigger(ID, Trigger.RECEIVED_DAMAGE, (pdata, in) -> {
			ReceivedDamageEvent ev = (ReceivedDamageEvent) in;
			if (!ev.getMeta().containsType(DamageCategory.MAGICAL)) return TriggerResult.keep();
			ev.getDamager().applyStatus(StatusType.SANCTIFIED, data, sanct, -1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_HELMET, "Decrease " + GlossaryTag.MAGICAL.tag(this) + " damage by <yellow>" + def + "</yellow>. Taking " + GlossaryTag.MAGICAL.tag(this)
				+ " damage applies " + GlossaryTag.SANCTIFIED.tag(this, sanct, true) + " to the damager.");
	}
}
