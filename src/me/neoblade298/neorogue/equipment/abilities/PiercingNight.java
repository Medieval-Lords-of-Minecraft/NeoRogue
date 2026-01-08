package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
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
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class PiercingNight extends Equipment {
	private static final String ID = "PiercingNight";
	private int damage, lineDamage, insanity;
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST)
		.dustOptions(new DustOptions(Color.fromRGB(75, 0, 130), 1F));
	private static final ParticleContainer lineParticle = new ParticleContainer(Particle.DUST)
		.dustOptions(new DustOptions(Color.fromRGB(138, 43, 226), 1.2F))
		.count(5).spread(0.2, 0.2);
	private static final TargetProperties lineProps = TargetProperties.line(8, 2, TargetType.ENEMY);
	
	public PiercingNight(boolean isUpgraded) {
		super(ID, "Piercing Night", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(25, 10, 8, 8));
		damage = isUpgraded ? 300 : 200;
		lineDamage = isUpgraded ? 250 : 175;
		insanity = isUpgraded ? 100 : 75;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String statusName = p.getName() + "-piercingnight";
		ProjectileGroup proj = new ProjectileGroup();
		boolean[] anyHit = {false}; // Track if any projectile hit
		
		// Create 3 projectiles in a cone spread
		for (int angle : new int[] { -15, 0, 15 }) {
			proj.add(new PiercingNightProjectile(data, angle, this, slot, statusName, anyHit));
		}
		
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			anyHit[0] = false;
			Sounds.attackSweep.play(p, p);
			proj.start(data);
			
			// After projectiles hit, check if we hit anything and charge if so
			data.addTask(new BukkitRunnable() {
				public void run() {
					if (anyHit[0]) {
						data.charge(20).then(new Runnable() {
							public void run() {
								// Deal line damage after charge
								Vector direction = p.getEyeLocation().getDirection().setY(0).normalize();
								org.bukkit.Location start = p.getLocation().add(0, 1, 0);
								org.bukkit.Location end = start.clone().add(direction.clone().multiply(lineProps.range));
								
								ParticleUtil.drawLine(p, lineParticle, start, end, 0.3);
								Sounds.extinguish.play(p, p);
								
								for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, start, end, lineProps)) {
									FightData fd = FightInstance.getFightData(ent);
									
									// Deal line damage
									FightInstance.dealDamage(new DamageMeta(data, lineDamage, DamageType.DARK, 
										DamageStatTracker.of(id + slot, PiercingNight.this)), ent);
									
									// Check if enemy was hit by both projectile and line
									if (fd.hasStatus(statusName)) {
										// Hit by both - apply triple insanity (2x additional)
										FightInstance.applyStatus(ent, StatusType.INSANITY, data, insanity * 2, -1);
										
										// Remove the mark
										Status s = Status.createByGenericType(GenericStatusType.BASIC, statusName, fd, true);
										fd.applyStatus(s, data, -1, -1);
									}
									else {
										// Only hit by line - apply normal insanity
										FightInstance.applyStatus(ent, StatusType.INSANITY, data, insanity, -1);
									}
								}
							}
						});
					}
				}
			}.runTaskLater(NeoRogue.inst(), 10L));
			
			return TriggerResult.keep();
		}));
	}
	
	private class PiercingNightProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private Equipment eq;
		private int slot;
		private String statusName;
		private boolean[] anyHit;

		public PiercingNightProjectile(PlayerFightData data, int angleOffset, Equipment eq, int slot, 
				String statusName, boolean[] anyHit) {
			super(1.5, properties.get(PropertyType.RANGE), 1);
			this.rotation(angleOffset);
			this.size(0.3, 0.3);
			this.data = data;
			this.p = data.getPlayer();
			this.eq = eq;
			this.slot = slot;
			this.statusName = statusName;
			this.anyHit = anyHit;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			anyHit[0] = true;
			LivingEntity target = hit.getEntity();
			FightData fd = FightInstance.getFightData(target);
			
			// Apply insanity and mark the enemy
			FightInstance.applyStatus(target, StatusType.INSANITY, data, insanity, -1);
			
			// Mark with custom status for tracking double-hit
			if (!fd.hasStatus(statusName)) {
				Status s = Status.createByGenericType(GenericStatusType.BASIC, statusName, fd, true);
				fd.applyStatus(s, data, 1, 60); // 3 seconds duration
			}
			
			Sounds.extinguish.play(p, target.getLocation());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.DARK, 
				DamageStatTracker.of(id + slot, eq)));
		}
	}

	@Override
	public void setupReforges() {
		addSelfReforge(Blackspike.get(), Obfuscation.get());
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLACKSTONE,
			"On cast, throw <white>3</white> projectiles in a cone that each deal " + 
			GlossaryTag.DARK.tag(this, damage, true) + " damage and apply " + 
			GlossaryTag.INSANITY.tag(this, insanity, true) + ". If an enemy is hit, " +
			"<white>charge 1s</white> and deal " + GlossaryTag.DARK.tag(this, lineDamage, true) + 
			" damage in a line in front of you and apply " + GlossaryTag.INSANITY.tag(this, insanity, true) + 
			". Enemies hit by both have their " + GlossaryTag.INSANITY.tag(this) + " tripled.");
	}
}
