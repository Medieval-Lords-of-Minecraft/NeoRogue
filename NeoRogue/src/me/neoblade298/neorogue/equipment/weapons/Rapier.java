package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;


import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class Rapier extends Equipment {
	
	private int shields;
	public Rapier(boolean isUpgraded) {
		super("rapier", "Rapier", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 45 : 30, 1, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_CRIT));
		shields = isUpgraded ? 10 : 7;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			weaponSwingAndDamage(p, data, ev.getTarget());
			data.addShield(p.getUniqueId(), shields, true, 1, 100, 1, 1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SWORD, null, "On hit, grant yourself <yellow>" + shields + "</yellow> shields");
	}
}
