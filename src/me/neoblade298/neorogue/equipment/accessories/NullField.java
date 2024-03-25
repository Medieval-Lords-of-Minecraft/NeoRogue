package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;

public class NullField extends Equipment {
	private static final String ID = "nullField";
	private int grantPercent;
	
	public NullField(boolean isUpgraded) {
		super(ID, "Null Field", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ACCESSORY);
		grantPercent = isUpgraded ? 200 : 100;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, (pdata, in) -> {
			ReceivedDamageEvent ev = (ReceivedDamageEvent) in;
			DamageMeta meta = ev.getMeta();
			
			// todo since neo is a bitch and STILL hasnt ADDED IT: get the DAMAGE RECEIVED AMOUNT here
			double dmgReceived = -1;
			data.addMana(dmgReceived * grantPercent / 100.0);
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.ENDER_PEARL, "Taking magic damage grants <yellow>" + grantPercent + "</yellow>% as mana."
		);
	}
}
