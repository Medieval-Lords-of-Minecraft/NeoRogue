package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Ammunition;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;

public class BarbedRocket extends Ammunition {
	private static final String ID = "barbedRocket";
	
	public BarbedRocket(boolean isUpgraded) {
		super(ID, "Barbed Rocket", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofAmmunition(isUpgraded ? 20 : 10, 0.2, DamageType.PIERCING));
		properties.addUpgrades(PropertyType.DAMAGE);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void onHit(ProjectileInstance inst, DamageMeta meta, LivingEntity target) {
		Vector v = inst.getVelocity().normalize().multiply(properties.get(PropertyType.KNOCKBACK));
		inst.getOwner().addTask(new BukkitRunnable() {
			public void run() {
				if (target != null && target.isValid()) {
					FightInstance.knockback(target, v);
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 10, 10));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARROW, "Knocks the enemy back two additional times over <white>1s</white> on hit.");
	}
}
