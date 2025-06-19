package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.abilities.Manabending;
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

public class AshenWand extends Equipment {
	private static final String ID = "ashenWand";
	private static final ParticleContainer tick;
	
	static {
		tick = new ParticleContainer(Particle.SMOKE);
		tick.count(5).spread(0.1, 0.1).speed(0.01);
	}
	
	public AshenWand(boolean isUpgraded) {
		super(
				ID , "Ashen Wand", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(2, 0, isUpgraded ? 60 : 50, 1, DamageType.FIRE, Sound.ENTITY_PLAYER_ATTACK_SWEEP).add(PropertyType.RANGE, 10)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
	}

	@Override
	public void setupReforges() {
		addReforge(Manabending.get(), AshenHeadhunter.get(), AshenWand2.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new AshenWandProjectile(data));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			weaponSwing(p, data);
			Location start = p.getLocation().add(0, 1, 0);
			Vector dir = start.getDirection();
			data.addTask(new BukkitRunnable() {
				private boolean first = false;
				public void run() {
					if (first) {
						first = true;
						tick.play(p, start);
					}
					else {
						proj.start(data, start, dir);
						this.cancel();
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 10, 10));
			return TriggerResult.keep();
		});
	}
	
	private class AshenWandProjectile extends Projectile {
		private Player p;

		public AshenWandProjectile(PlayerFightData data) {
			super(1, 10, 2);
			this.size(0.2, 0.2);
			this.p = data.getPlayer();
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Location loc = hit.getEntity().getLocation();
			Sounds.infect.play(p, loc);
			applyProjectileOnHit(hit.getEntity(), proj, hitBarrier, true);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.fire.play(p, p);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STICK, "Projectiles are paused for <white>1s</white> before firing.");
	}
}
