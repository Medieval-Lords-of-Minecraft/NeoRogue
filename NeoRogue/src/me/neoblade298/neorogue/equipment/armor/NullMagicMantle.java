package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class NullMagicMantle extends Equipment {
	private double damageReduction;
	
	public NullMagicMantle(boolean isUpgraded) {
		super("nullMagicMantle", "Null Magic Mantle", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		damageReduction = isUpgraded ? 3 : 2;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addBuff(p.getUniqueId(), false, false, BuffType.MAGICAL, damageReduction);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RABBIT_HIDE, "Decrease all " + GlossaryTag.MAGICAL.tag(this) + " damage by <yellow>" + damageReduction + "</yellow>.");
	}
}
