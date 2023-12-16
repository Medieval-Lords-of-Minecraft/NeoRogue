package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Armor;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class NullMagicMantle extends Armor {
	private double damageReduction;
	
	public NullMagicMantle(boolean isUpgraded) {
		super("nullMagicMantle", "Null Magic Mantle", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);
		damageReduction = isUpgraded ? 2 : 1;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addBuff(p.getUniqueId(), false, false, BuffType.MAGICAL, damageReduction);
	}

	@Override
	public void setupItem() {
		item = createItem(this, Material.RABBIT_HIDE, null, "Decrease all magical damage by <yellow>" + damageReduction + ".");
	}
}
