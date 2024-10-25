package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class EnchantedCloak extends Equipment {
	private static final String ID = "enchantedCloak";
	private int reduc, damage;
	
	public EnchantedCloak(boolean isUpgraded) {
		super(ID, "Enchanted Cloak", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ARMOR);
		reduc = isUpgraded ? 8 : 5;
		damage = isUpgraded ? 20 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addBuff(data, false, false, BuffType.MAGICAL, reduc);
		data.addBuff(data, true, false, BuffType.MAGICAL, damage);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_CHESTPLATE, "Decrease all " + GlossaryTag.MAGICAL.tag(this) + " damage taken by <yellow>" + reduc + "</yellow>. " +
			"Increase all " + GlossaryTag.MAGICAL.tag(this) + " damage dealt by <yellow>" + damage + "</yellow>.");
	}
}
