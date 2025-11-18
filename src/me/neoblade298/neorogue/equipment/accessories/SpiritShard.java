package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class SpiritShard extends Equipment {
	private static final String ID = "SpiritShard";
	private double stam, mana;

	public SpiritShard(boolean isUpgraded) {
		super(ID, "Spirit Shard", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ACCESSORY);
		stam = 0.2;
		mana = isUpgraded ? 1.8 : 1.2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addManaRegen(mana);
		data.addStamina(-stam);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHT_BLUE_BANNER, "Decrease your stamina by " + DescUtil.white(stam)
				+ " and increases your mana regen by " + DescUtil.yellow(mana) + ".");
	}
}
