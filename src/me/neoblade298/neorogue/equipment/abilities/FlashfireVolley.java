package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.AmmoEquipmentInstance;
import me.neoblade298.neorogue.equipment.AmmunitionInstance;
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
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreLaunchProjectileGroupEvent;

public class FlashfireVolley extends Equipment {
	private static final String ID = "FlashfireVolley";
	private static final TargetProperties tp = TargetProperties.radius(4, false, TargetType.ENEMY);
	private static final ParticleContainer explode = new ParticleContainer(Particle.EXPLOSION).count(50).spread(2, 0.5);
	private static final ParticleContainer fire = new ParticleContainer(Particle.FLAME).count(10).spread(0.5, 0.5);
	private int bluntDamage, fireDamage, burn;
	
	public FlashfireVolley(boolean isUpgraded) {
		super(ID, "Flashfire Volley", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, 3, 0).add(PropertyType.AREA_OF_EFFECT, tp.range));
		
		bluntDamage = isUpgraded ? 150 : 100;
		fireDamage = isUpgraded ? 150 : 100;
		burn = isUpgraded ? 350 : 250;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		FlashfireVolleyInstance inst = new FlashfireVolleyInstance(data, this, slot, es);
		data.addTrigger(id, Trigger.PRE_LAUNCH_PROJECTILE_GROUP, inst);
	}

	private class FlashfireVolleyInstance extends AmmoEquipmentInstance {
		private ActionMeta hitEnemies = new ActionMeta();
		
		public FlashfireVolleyInstance(PlayerFightData data, Equipment equip, int slot, EquipSlot es) {
			super(data, equip, slot, es);
			action = (pdata, in) -> {
				Player p = data.getPlayer();
				Sounds.equip.play(p, p);
				
				// Launch player upward
				p.setVelocity(new Vector(0, 1, 0));
				
				// Deal AoE blunt damage and track hit enemies
				HashSet<LivingEntity> hit = new HashSet<LivingEntity>();
				for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
					DamageMeta dm = new DamageMeta(data, bluntDamage, DamageType.BLUNT, DamageStatTracker.of(id + slot, eq));
					FightInstance.dealDamage(dm, ent);
					hit.add(ent);
				}
				hitEnemies.setObject(hit);
				
				explode.play(p, p.getLocation());
				
				// Fire 3 arrows in quick succession
				for (int i = 0; i < 3; i++) {
					data.addTask(new BukkitRunnable() {
						public void run() {
							if (data.getAmmoInstance() != null) {
								AmmunitionInstance ammo = data.getAmmoInstance();
								ProjectileGroup proj = new ProjectileGroup(new FlashfireVolleyProjectile(data, ammo, slot, hitEnemies));
								proj.start(data);
							}
						}
					}.runTaskLater(NeoRogue.inst(), (i + 1) * 3L)); // 3, 6, 9 tick delays
				}
				
				return TriggerResult.of(false, true);
			};
		}

		@Override
		public boolean canTrigger(Player p, PlayerFightData data, Object in) {
			PreLaunchProjectileGroupEvent ev = (PreLaunchProjectileGroupEvent) in;
			if (!ev.isBasicAttack()) return false;
			// Check if looking down (opposite of PreySeeker's up check)
			if (p.getEyeLocation().getDirection().getY() > -0.9) return false;
			return super.canTrigger(p, data, in);
		}
	}
	
	private class FlashfireVolleyProjectile extends Projectile {
		private static ParticleContainer trail = new ParticleContainer(Particle.FLAME).count(5).spread(0.2, 0.2);
		private Player p;
		private PlayerFightData data;
		private AmmunitionInstance ammo;
		private int slot;
		private ActionMeta hitEnemies;

		public FlashfireVolleyProjectile(PlayerFightData data, AmmunitionInstance ammo, int slot, ActionMeta hitEnemies) {
			super(1, 10, 1);
			this.p = data.getPlayer();
			this.data = data;
			this.ammo = ammo;
			this.slot = slot;
			this.hitEnemies = hitEnemies;
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.shoot.play(p, p);
			DamageMeta dm = proj.getMeta();
			EquipmentProperties ammoProps = ammo.getProperties();
			dm.addDamageSlice(new DamageSlice(data, fireDamage, DamageType.FIRE, DamageStatTracker.of(id + slot, eq)));
			ammo.onStart(proj, false);
			ammo.use();
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			fire.play(p, hit.getEntity().getLocation());
			ammo.onHit(proj, meta, hit.getEntity());
			
			// Check if this enemy was hit by the initial AoE
			@SuppressWarnings("unchecked")
			HashSet<LivingEntity> hitList = (HashSet<LivingEntity>) hitEnemies.getObject();
			if (hitList != null && hitList.contains(hit.getEntity())) {
				FightInstance.applyStatus(hit.getEntity(), StatusType.BURN, data, burn, -1);
			}
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			trail.play(p, proj.getLocation());
			ammo.onTick(p, proj, interpolation);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIRE_CHARGE,
				"Passive. Upon firing a basic attack straight down, cancel the basic attack and launch yourself upward, dealing " +
				DescUtil.yellow(bluntDamage) + " " + GlossaryTag.BLUNT.tag(this) + " damage to nearby enemies. " +
				"Then fire <white>3</white> arrows in quick succession that deal " + DescUtil.yellow(fireDamage) + " " +
				GlossaryTag.FIRE.tag(this) + " damage. If these arrows hit an enemy damaged by the launch, " +
				"apply " + GlossaryTag.BURN.tag(this, burn, true) + ".");
	}
}
