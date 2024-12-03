package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.GrantShieldsEvent;

public class MinorShieldingRelic extends Equipment {
	private static final String ID = "minorShieldingRelic";
	private int shields;
	
	public MinorShieldingRelic(boolean isUpgraded) {
		super(ID, "Minor Shielding Relic", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		shields = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RECEIVE_SHIELDS, (pdata, in) -> {
			GrantShieldsEvent ev = (GrantShieldsEvent) in;
			if (ev.isSecondary()) return TriggerResult.keep();
			ev.getBuff().add(Buff.increase(data, shields));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_NUGGET, "Whenever you gain " + GlossaryTag.SHIELDS.tag(this) + ", increase the amount gained by"
				+ " <yellow>" + shields + "</yellow>.");
	}
}
