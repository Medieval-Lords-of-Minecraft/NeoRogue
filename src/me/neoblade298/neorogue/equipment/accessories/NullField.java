package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedHealthDamageEvent;

public class NullField extends Equipment {
	private static final String ID = "nullField";
	private int grantPercent;

	public NullField(boolean isUpgraded) {
		super(ID, "Null Field", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ACCESSORY);
		grantPercent = isUpgraded ? 300 : 200;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RECEIVED_HEALTH_DAMAGE, (pdata, in) -> {
			ReceivedHealthDamageEvent ev = (ReceivedHealthDamageEvent) in;
			
			double dmg = 0;
			for (DamageSlice slice : ev.getMeta().getSlices()) {
				if (slice.getPostBuffType().containsBuffType(BuffType.MAGICAL)) {
					dmg += slice.getDamage();
				}
			}

			data.addMana(dmg * grantPercent / 100.0);

			return TriggerResult.keep();
		});
	}
	
	@Override
	public void setupItem() {
		item = createItem(
				Material.ENDER_PEARL,
				"Taking magic damage to health grants <yellow>" + grantPercent + "%</yellow> as mana."
		);
	}
}
