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
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class AshenHeadhunter extends Equipment {
	private static final String ID = "AshenHeadhunter";
	private static final ParticleContainer pc;
	private int burn;
	
	static {
		pc = new ParticleContainer(Particle.SMOKE);
		pc.count(5).spread(0.1, 0.1).speed(0.01);
	}
	
	public AshenHeadhunter(boolean isUpgraded) {
		super(
				ID , "Ashen Headhunter", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(4, 0, isUpgraded ? 140 : 100, 0.5, DamageType.FIRE, Sound.ENTITY_PLAYER_ATTACK_SWEEP).add(PropertyType.RANGE, 10)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
		burn = isUpgraded ? 30 : 20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new AshenHeadhunterProjectile(data, this, slot));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			weaponSwing(p, data);
			Location start = p.getLocation().add(0, 1, 0);
			Vector dir = start.getDirection();
			data.addTask(new BukkitRunnable() {
				private int tick = 0;

				public void run() {
					if (++tick < 3) {
						pc.play(p, start);
					} else {
						proj.start(data, start, dir);
						this.cancel();
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 0, 10));
			return TriggerResult.keep();
		});
	}
	
	private class AshenHeadhunterProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private AshenHeadhunter eq;
		private int slot;

		public AshenHeadhunterProjectile(PlayerFightData data, AshenHeadhunter eq, int slot) {
			super(2, 10, 2);
			this.size(0.2, 0.2);
			this.data = data;
			this.p = data.getPlayer();
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Location loc = hit.getEntity().getLocation();
			Sounds.infect.play(p, loc);
			hit.applyStatus(StatusType.BURN, data, burn, -1);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.fire.play(p, p);
			proj.applyWeapon(data, eq, slot);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STICK, "Projectiles are paused for <white>1s</white> before firing and apply " + GlossaryTag.BURN.tag(this, burn, true) + ".");
	}
}
