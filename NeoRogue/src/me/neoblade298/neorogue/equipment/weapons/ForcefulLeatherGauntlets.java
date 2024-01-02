package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;


import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ForcefulLeatherGauntlets extends Equipment {
	
	public ForcefulLeatherGauntlets(boolean isUpgraded) {
		super("forcefulLeatherGauntlets", "Forceful Leather Gauntlets", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 27 : 22, 2, DamageType.BLUNT, Sound.ENTITY_PLAYER_ATTACK_CRIT));
		item = createItem(Material.LEATHER, null, null);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			meleeWeapon(p, data, inputs, (Damageable) inputs[1]);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER);
	}
}
