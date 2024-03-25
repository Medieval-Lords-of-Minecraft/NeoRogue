package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class RingOfChaos extends Equipment {
	private static final String ID = "ringOfChaos";
	private int buffPercent;
	private DamageType lastDamageType;
	
	public RingOfChaos(boolean isUpgraded) {
		super(ID, "Ring Of Chaos", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ACCESSORY);
		buffPercent = isUpgraded ? 25 : 12;
		lastDamageType = null;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			DealtDamageEvent ev = (DealtDamageEvent) in;
			// todo: get dmg dealt and dmg type thank you neo
			int dmgDealt = -1;
			DamageType type = null;
			
			if (lastDamageType == null || lastDamageType != type) {
				ev.getMeta().addDamageSlice(new DamageSlice(data, dmgDealt * buffPercent / 100.0, type));
			}
			lastDamageType = type;
			
			return TriggerResult.keep();
		});
	}
	
	@Override
	public void setupItem() {
		item = createItem(
				Material.GOLD_NUGGET,
				"Whenever you deal magic damage, if it's a different type as the last time you dealt damage, buff this damage by <yellow>"
						+ buffPercent + "%</yellow>."
		);
	}
}
