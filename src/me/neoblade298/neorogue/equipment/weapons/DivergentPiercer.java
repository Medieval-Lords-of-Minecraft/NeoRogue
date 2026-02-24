package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Ammunition;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class DivergentPiercer extends Ammunition {
	private static final String ID = "DivergentPiercer";
	private int bonusDamage;
	private ActionMeta lastHit;

	public DivergentPiercer(boolean isUpgraded) {
		super(ID, "Divergent Piercer", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofAmmunition(isUpgraded ? 40 : 30, 0.1, DamageType.PIERCING));
		bonusDamage = isUpgraded ? 120 : 80;
		properties.addUpgrades(PropertyType.DAMAGE);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		super.initialize(data, bind, es, slot);
		lastHit = new ActionMeta();
	}

	@Override
	public void onHit(ProjectileInstance inst, DamageMeta meta, LivingEntity target) {
		// Check if this is a different enemy from the last one hit
		LivingEntity lastEnemy = lastHit.getEntity();
		if (lastEnemy != null && !target.getUniqueId().equals(lastEnemy.getUniqueId())) {
			// Deal bonus damage when hitting a different enemy
			meta.addDamageSlice(new DamageSlice(inst.getOwner(), bonusDamage, DamageType.PIERCING, 
					DamageStatTracker.of(id, this)));
		}
		
		// Update last hit entity
		lastHit.setEntity(target);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARROW,
				"Deals an additional " + GlossaryTag.PIERCING.tag(this, bonusDamage, true) + 
				" damage if the last enemy hit with this ammunition is different from the current enemy hit.");
	}
}
