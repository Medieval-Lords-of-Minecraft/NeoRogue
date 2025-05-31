package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ForceCloak extends Equipment {
	private static final String ID = "forceCloak";

	public ForceCloak(boolean isUpgraded) {
		super(ID, "Force Cloak", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(isUpgraded ? 25 : 35, 0, isUpgraded ? 15 : 20, 0));
		properties.addUpgrades(PropertyType.MANA_COST, PropertyType.COOLDOWN);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(p, p);
			data.applyStatus(StatusType.PROTECT, data, 1, -1);
			data.applyStatus(StatusType.SHELL, data, 1, -1);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD, "On cast, apply " + GlossaryTag.PROTECT.tag(this, 1, false) + " and "
				+ GlossaryTag.SHELL.tag(this, 1, false) + ".");
	}
}
