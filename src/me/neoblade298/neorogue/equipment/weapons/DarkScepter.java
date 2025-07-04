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
import me.neoblade298.neorogue.session.fight.DamageMeta;
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
		tick = new ParticleContainer(Particle.SMOKE);
		tick.count(25).spread(0.1, 0.1).speed(0.01);
	}

	public DarkScepter(boolean isUpgraded) {
		super(
				ID, "Dark Scepter", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(3, 0, isUpgraded ? 80 : 60, 0.5, DamageType.DARK, Sound.ITEM_AXE_SCRAPE)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new DarkRay(data, this, slot));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			weaponSwing(p, data);
			
			RayTraceResult result = p.rayTraceBlocks(hitScanRange);
			if (result != null) {
				double yOff = 0.5;
				Vector spawnVec = result.getHitBlockFace().getDirection();
				if (spawnVec.getY() > 0) {
					yOff = 0;
				}
				else if (spawnVec.getY() < 0) {
					yOff = 1;
				}
				Location spawnLoc = result.getHitBlock().getLocation().add(0.5, yOff, 0.5);
				spawnLoc = spawnLoc.add(spawnVec.multiply(0.75));
				proj.start(data, spawnLoc, spawnVec);
			}
			
			return TriggerResult.keep();
		});
	}

	private class DarkRay extends Projectile {
		private Player p;
		private PlayerFightData data;
		private DarkScepter eq;
		private int slot;
		public DarkRay(PlayerFightData data, DarkScepter eq, int slot) {
			super(0.5, 2, 1);
			this.size(1.25, 1.25).pierce(-1);
			this.ignore(false, true, false);
			this.p = data.getPlayer();
			this.data = data;
			this.eq = eq;
			this.slot = slot;
		}
		
		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}
		
		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {

		}
		
		@Override
		public void onStart(ProjectileInstance proj) {
			proj.applyWeapon(data, eq, slot);
		}
	}
	
	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_HOE, "Dark rays shoot out of the targeted surface.");
	}
}
