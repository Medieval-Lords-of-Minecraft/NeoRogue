package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Armor;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.BuffType;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class NullMagicMantle extends Armor {
	private double damageReduction;
	
	public NullMagicMantle(boolean isUpgraded) {
		super("nullMagicMantle", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);
		display = "Null Magic Mantle";
		damageReduction = isUpgraded ? 2 : 1;
		item = createItem(this, Material.RABBIT_HIDE, null, "Decrease all magical damage by <yellow>" + damageReduction + ".");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		data.addBuff(p.getUniqueId(), false, false, BuffType.MAGICAL, damageReduction);
	}
}