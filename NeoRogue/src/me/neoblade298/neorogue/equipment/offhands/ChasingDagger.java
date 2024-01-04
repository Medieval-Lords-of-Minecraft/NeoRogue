package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ChasingDagger extends Equipment {
	public ChasingDagger(boolean isUpgraded) {
		super("chasingDagger", "Chasing Dagger", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.OFFHAND, EquipmentProperties.ofWeapon(25, 1, DamageType.SLASHING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RIGHT_CLICK_HIT, (pdata, inputs) -> {
			meleeWeapon(p, data, (Damageable) inputs[1]);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SWORD, "Right click to basic attack. Has its own separate attack cooldown.");
	}
}
