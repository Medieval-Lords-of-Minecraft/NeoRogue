package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
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

public class HuntersBow extends Bow {
	private static final String ID = "HuntersBow";
	private static final ParticleContainer tick = new ParticleContainer(Particle.CRIT).count(10).speed(0.01).spread(0.1, 0.1);
	
	public HuntersBow(boolean isUpgraded) {
		super(ID, "Hunter's Bow", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofBow(isUpgraded ? 90 : 60, 1, 0, 12, 0, 1));
		properties.addUpgrades(PropertyType.DAMAGE);
	}

	@Override
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {
		tick.play(p, proj.getLocation());
	}

	@Override
	public void setupReforges() {
		overrideReforgeDrop = true;
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
			if (ev.getEntity().getVelocity().length() < 2.9) return TriggerResult.keep();
			ProjectileGroup proj = new ProjectileGroup(new BowProjectile(data, ev.getEntity().getVelocity(), this, id + slot));
			proj.start(data);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOW, "Can only be fired at max draw.");
	}
}
