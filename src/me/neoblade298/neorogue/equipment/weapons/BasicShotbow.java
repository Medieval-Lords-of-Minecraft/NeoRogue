package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BasicShotbow extends Bow {
	private static final String ID = "basicShotbow";
	
	public BasicShotbow(boolean isUpgraded) {
		super(ID, "Basic Shotbow", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofBow(30, isUpgraded ? 1.5 : 1, 0, 5));
		properties.addUpgrades(PropertyType.ATTACK_SPEED);
	}

	@Override
	public void onTick(Player p, ProjectileInstance proj, boolean interpolation) {
		BowProjectile.tick.play(p, proj.getLocation());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction act = new StandardPriorityAction(id);
		act.setAction((pdata, in) -> {
			if (!canShoot(data)) return TriggerResult.keep();
			ProjectileLaunchEvent ev = (ProjectileLaunchEvent) in;
			Vector v = ev.getEntity().getVelocity();
			ProjectileGroup proj = new ProjectileGroup(new BowProjectile(data, v, this));
			act.addCount(1);
			if (act.getCount() >= 3) {
				act.setCount(0);
				proj.add(new BowProjectile(data, v.clone().rotateAroundY(Math.toRadians(-15)), this));
				proj.add(new BowProjectile(data, v.clone().rotateAroundY(Math.toRadians(15)), this));
			}
			proj.start(data);
			return TriggerResult.keep();
		});
		data.addSlotBasedTrigger(id, slot, Trigger.VANILLA_PROJECTILE, act);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOW,
		"Every third shot, fire two additional arrows in a <white>30</white> degree cone.");
	}
}
