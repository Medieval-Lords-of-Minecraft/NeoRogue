package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BasicBow extends Bow {
	private static final String ID = "basicBow";
	
	public BasicBow(boolean isUpgraded) {
		super(ID, "Basic Bow", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofBow(isUpgraded ? 45 : 35, 1, 0, 12, 0, 1.2));
		properties.addUpgrades(PropertyType.DAMAGE);
	}

	@Override
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {
		BowProjectile.tick.play(p, proj.getLocation());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.VANILLA_PROJECTILE, (pdata, in) -> {
			if (!canShoot(data)) return TriggerResult.keep();
			useBow(data);

			ProjectileLaunchEvent ev = (ProjectileLaunchEvent) in;
			ProjectileGroup proj = new ProjectileGroup(new BowProjectile(data, ev.getEntity().getVelocity(), this));
			proj.start(data);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOW);
	}
}
