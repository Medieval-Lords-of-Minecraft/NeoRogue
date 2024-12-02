package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class NullMagicMantle extends Equipment {
	private static final String ID = "nullMagicMantle";
	private int damageReduction;
	
	public NullMagicMantle(boolean isUpgraded) {
		super(ID, "Null Magic Mantle", isUpgraded, Rarity.COMMON, EquipmentClass.CLASSLESS,
				EquipmentType.ARMOR);
		damageReduction = isUpgraded ? 6 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addBuff(data, false, false, DamageCategory.MAGICAL, damageReduction);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RABBIT_HIDE, "Decrease all " + GlossaryTag.MAGICAL.tag(this) + " damage taken by <yellow>" + damageReduction + "</yellow>.");
	}
}
