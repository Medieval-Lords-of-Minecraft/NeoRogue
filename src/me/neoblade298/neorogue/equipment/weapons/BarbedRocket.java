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
				EquipmentProperties.ofAmmunition(isUpgraded ? 20 : 10, 0.6, DamageType.PIERCING));
		properties.addUpgrades(PropertyType.DAMAGE);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void onHit(ProjectileInstance inst, DamageMeta meta, LivingEntity target) {
		Vector v = inst.getVelocity().setY(0).normalize().setY(0.2).multiply(properties.get(PropertyType.KNOCKBACK));
		inst.getOwner().addTask(new BukkitRunnable() {
			private int count = 0;
			public void run() {
				if (target != null && target.isValid()) {
					FightInstance.knockback(target, v);
				}
				if (++count >= 2) {
					this.cancel();
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 20, 20));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARROW, "Knocks the enemy back two additional times over <white>2s</white> on hit.");
	}
}
