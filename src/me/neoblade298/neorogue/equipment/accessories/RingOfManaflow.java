package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class RingOfManaflow extends Equipment {
	private static final String ID = "RingOfManaflow";
	private int regen;
	
	public RingOfManaflow(boolean isUpgraded) {
		super(ID, "Ring Of Manaflow", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY);
		regen = isUpgraded ? 2 : 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.TOGGLE_CROUCH, (pdata, in) -> {
			PlayerToggleSneakEvent ev = (PlayerToggleSneakEvent) in;
			if (ev.isSneaking()) {
				data.addManaRegen(regen);
			}
			else {
				data.addManaRegen(-regen);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_SHARD, "Your mana regen while crouching is increased by " + DescUtil.yellow(regen) + ".");
	}
}
