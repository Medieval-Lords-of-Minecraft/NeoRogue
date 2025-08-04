package me.neoblade298.neorogue.equipment.weapons;


import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class IronSword extends Equipment {
	private static final String ID = "ironSword";
	private int thres;
	
	public IronSword(boolean isUpgraded) {
		super(ID, "Iron Sword", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 80 : 65, 1, 0.4, DamageType.SLASHING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		properties.addUpgrades(PropertyType.DAMAGE);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addPermanentShield(p.getUniqueId(), shields);
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			double damage = properties.get(PropertyType.DAMAGE);
			if (!data.getShields().isEmpty()) {
				damage += shieldsBonus;
			}
			weaponSwingAndDamage(p, data, ev.getTarget(), damage);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SWORD, "Start the fight with " + GlossaryTag.SHIELDS.tag(this, shields, true) + ". "
				+ "Deal an additional <yellow>" + shieldsBonus + "</yellow> damage if you have shields when you attack with this weapon.");
	}
}
