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

public class MultiCrossbow extends Bow {
	private static final String ID = "MultiCrossbow";
	
	public MultiCrossbow(boolean isUpgraded) {
		super(ID, "Multi-Crossbow", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofBow(isUpgraded ? 60 : 50, 1, 0, 6, 0, 2));
		properties.addUpgrades(PropertyType.DAMAGE);
	}

	@Override
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {
		BowProjectile.tick.play(p, proj.getLocation());
	}

	@Override
	public void setupReforges() {
		
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.VANILLA_PROJECTILE, (pdata, in) -> {
			if (!canShootCrossbow(data)) return TriggerResult.keep();
			useBow(data);
			ProjectileLaunchEvent ev = (ProjectileLaunchEvent) in;
			ProjectileGroup proj = new ProjectileGroup();
			int limit = data.getAmmoInstance().getRemaining();
			int count = 0;
			if (limit == -1 || ++count < limit) {
				proj.add(new BowProjectile(data, ev.getEntity().getVelocity(), this, id + slot));
			}
			for (double y : new double[] { -0.1, 0.1 }) {
				for (double rotate : new double[] { 10, -10 }) {
					if (limit != -1 && ++count >= limit) break;
					proj.add(new BowProjectile(data, ev.getEntity().getVelocity(), this, id + slot).initialY(y).rotation(rotate));
				}
				if (limit != -1 && count >= limit) break;
			}
			proj.start(data);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CROSSBOW,
			"Additionally fires <white>4</white> projectiles in a circle around the cursor.");
	}
}
