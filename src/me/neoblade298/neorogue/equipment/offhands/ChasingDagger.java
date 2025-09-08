package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.RightClickHitEvent;

public class ChasingDagger extends Equipment {
	private static final String ID = "chasingDagger";
	public ChasingDagger(boolean isUpgraded) {
		super(ID, "Chasing Dagger", isUpgraded, Rarity.UNCOMMON, new EquipmentClass[] {EquipmentClass.WARRIOR, EquipmentClass.THIEF},
				EquipmentType.OFFHAND, EquipmentProperties.ofWeapon(isUpgraded ? 35 : 25, 1, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		properties.addUpgrades(PropertyType.DAMAGE);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RIGHT_CLICK_HIT, (pdata, inputs) -> {
			RightClickHitEvent ev = (RightClickHitEvent) inputs;
			if (ev.getTarget() instanceof Player) return TriggerResult.keep();
			weaponSwingAndDamage(p, data, ev.getTarget());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SWORD, "Right click to basic attack.");
	}
}
