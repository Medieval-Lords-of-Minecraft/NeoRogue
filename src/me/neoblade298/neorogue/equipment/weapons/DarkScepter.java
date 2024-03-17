package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class DarkScepter extends Equipment {
	private static final String ID = "darkScepter";
	private static final int hitScanRange = 12;
	private static final ParticleContainer tick;

	static {
		tick = new ParticleContainer(Particle.GLOW);
		tick.count(5).spread(0.1, 0.1).speed(0.01);
	}

	public DarkScepter(boolean isUpgraded) {
		super(
				ID, "Dark Scepter", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(6, 0, isUpgraded ? 50 : 25, 0.35, DamageType.DARK, Sound.ITEM_AXE_SCRAPE)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new DarkRay(p));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data))
				return TriggerResult.keep();
			weaponSwing(p, data);
			
			RayTraceResult result = p.rayTraceBlocks(hitScanRange);
			if (result != null) {
				Location spawnLoc = result.getHitBlock().getLocation().add(0.5, -0.5, 0.5);
				Vector spawnVec = result.getHitBlockFace().getDirection();
				spawnLoc = spawnLoc.add(spawnVec.multiply(0.75));
				proj.start(data, spawnLoc, spawnVec);
			}
			
			return TriggerResult.keep();
		});
	}

	private class DarkRay extends Projectile {
		private Player p;
		public DarkRay(Player p) {
			super(0.5, 2, 1);
			this.size(1.25, 1.25).pierce();
			this.p = p;
		}
		
		@Override
		public void onTick(ProjectileInstance proj, boolean interpolation) {
			tick.play(p, proj.getLocation());
		}
		
		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			weaponDamageProjectile(hit.getEntity(), proj, hitBarrier);
		}
		
		@Override
		public void onStart(ProjectileInstance proj) {
		}
	}
	
	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_HOE, "Dark rays shoot out of the targeted surface.");
	}
}
