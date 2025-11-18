package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class Furor extends Equipment {
	private static final String ID = "Furor";
	private int strength = 5, berserk;
	
	public Furor(boolean isUpgraded) {
		super(ID, "Furor", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
				berserk = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.applyStatus(StatusType.STRENGTH, data, strength, -1);
		data.applyStatus(StatusType.BERSERK, data, strength, -1);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT_AND_STEEL,
				"Passive. Start fights with " + GlossaryTag.STRENGTH.tag(this, strength, false) + " and " + GlossaryTag.BERSERK.tag(this, berserk, true) + ".");
	}
}
