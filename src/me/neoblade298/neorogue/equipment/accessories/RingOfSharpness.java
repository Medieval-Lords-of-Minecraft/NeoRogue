package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class RingOfSharpness extends Equipment {
	private static final String ID = "ringOfSharpness";
	private int buff;
	
	public RingOfSharpness(boolean isUpgraded) {
		super(ID, "Ring Of Sharpness", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ACCESSORY);
		buff = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addBuff(data, true, true, BuffType.PIERCING, buff * 0.01);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_SHARD, GlossaryTag.PIERCING.tag(this) + " damage is increased by <yellow>" + buff + "%</yellow>.");
	}
}
