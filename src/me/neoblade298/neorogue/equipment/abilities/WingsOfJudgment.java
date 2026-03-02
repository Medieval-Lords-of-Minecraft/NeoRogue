package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.AmmunitionInstance;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
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
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class WingsOfJudgment extends Equipment {
	private static final String ID = "WingsOfJudgment";
	private static final int ARROW_COUNT = 8;
	private static final TargetProperties tp = TargetProperties.radius(15, false, TargetType.ENEMY);
	private int damage;
	
	public WingsOfJudgment(boolean isUpgraded) {
		super(ID, "Wings of Judgment", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 0, 15, tp.range));
		damage = isUpgraded ? 100 : 50;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			
			// Charge for 1 second
			data.charge(20).then(new Runnable() {
				public void run() {
					Player p = data.getPlayer();
					
					// Check if player has ammunition
					if (data.getAmmoInstance() == null) {
						return;
					}
					
					// Fire 8 arrows in a wing pattern (4 per side, spreading behind and upward)
					int half = ARROW_COUNT / 2;
					double[] rotations = new double[half]; // Rotation offsets for one side
					double[] arcs = new double[half];      // Arc (upward tilt) for each arrow
					for (int i = 0; i < half; i++) {
						// Wings spread from sides to behind the player
						rotations[i] = 100 + i * 20.0; // 100, 120, 140, 160 degrees
						// Outer feathers arc higher
						arcs[i] = 0.3 + i * 0.25;      // 0.3, 0.55, 0.8, 1.05
					}

					for (int i = 0; i < ARROW_COUNT; i++) {
						int idx = i % half;
						// Negative rotation = left wing, positive = right wing
						double rot = (i < half) ? -rotations[idx] : rotations[idx];
						double arc = arcs[idx];

						WingsOfJudgmentProjectile proj = new WingsOfJudgmentProjectile(data, WingsOfJudgment.this, id + slot);
						proj.arc(arc);
						proj.rotation(rot);
						ProjectileGroup group = new ProjectileGroup(proj);
						group.start(data);
					}
					
					Sounds.shoot.play(p, p);
				}
			});
			
			return TriggerResult.keep();
		}));
	}

	private class WingsOfJudgmentProjectile extends Projectile {
		private PlayerFightData data;
		private AmmunitionInstance ammo;
		private Equipment eq;
		private String id;

		public WingsOfJudgmentProjectile(PlayerFightData data, Equipment eq, String id) {
			super(1, tp.range, 1);
			this.setBowDefaults();
			this.homing(0.03);
			this.gravity(0.05);
			this.data = data;
			this.ammo = data.getAmmoInstance();
			this.eq = eq;
			this.id = id;
			ammo.use();
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			Player p = data.getPlayer();
			BowProjectile.tick.play(p, proj.getLocation());
			ammo.onTick(p, proj, interpolation);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			ammo.onHit(proj, meta, hit.getEntity());
		}
		
		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			ammo.onHitBlock(proj, b);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Player p = data.getPlayer();
			Sounds.shoot.play(p, p);
			DamageMeta dm = proj.getMeta();
			EquipmentProperties ammoProps = ammo.getProperties();
			
			// Add ammunition damage
			double ammoDmg = ammoProps.get(PropertyType.DAMAGE);
			dm.addDamageSlice(new DamageSlice(data, ammoDmg, ammoProps.getType(), DamageStatTracker.of(id, ammo.getAmmo())));
			
			// Add base ability damage
			dm.addDamageSlice(new DamageSlice(data, damage, DamageType.PIERCING, DamageStatTracker.of(id, eq)));
			
			ammo.onStart(proj, false);
			
			// Set homing target to nearest enemy
			LivingEntity nearest = TargetHelper.getNearest(p, tp);
			if (nearest != null) {
				proj.setHomingTarget(nearest);
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FEATHER,
				"On cast, " + DescUtil.charge(this, 1, 1) + ". Afterwards, fire <white>" + ARROW_COUNT + 
				"</white> arrows upward in a wing pattern that home towards the nearest enemy. " +
				"Uses your equipped ammunition and deals " + GlossaryTag.PIERCING.tag(this, damage, true) + 
				" base damage.");
	}
}
