package me.neoblade298.neorogue.equipment.armor;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class IronCuirass extends Equipment {
	private static final String ID = "ironCuirass";
	private int damageReduction;
	
	public IronCuirass(boolean isUpgraded) {
		super(ID, "Iron Cuirass", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		damageReduction = isUpgraded ? 7 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.PHYSICAL), Buff.increase(data, damageReduction, StatTracker.defenseBuffAlly(UUID.randomUUID().toString(), this)));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_CHESTPLATE, "Decrease all " + GlossaryTag.PHYSICAL.tag(this) + " damage by <yellow>" + damageReduction + "</yellow>.");
	}
}
