package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class ManaGuard extends Equipment {
	private static final String ID = "manaGuard";
	private int reduc;

	public ManaGuard(boolean isUpgraded) {
		super(ID, "Mana Guard", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.none());
		reduc = isUpgraded ? 9 : 6;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addManaRegen(-1);
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL),
				Buff.increase(data, reduc, StatTracker.defenseBuffAlly(this)));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD, "Passive. Reduces damage taken by <yellow>" + reduc
				+ "</yellow> but decrease mana regen by <white>1</white>.");
	}
}
