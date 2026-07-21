package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Cone;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

// Uncommon mage offhand. On right click, pushes all enemies in a cone away and applies base concussed.
// Any enemy shoved into a wall takes blunt damage and gains additional concussed.
public class ShockwaveTome extends Equipment {
	private static final String ID = "ShockwaveTome";
	private static final int BASE_CONCUSSED = 3;
	private static final double KNOCKBACK = 1.4;
	private static final double WALL_CHECK_DIST = 2.0; // blocks between enemy and a wall to count as "into a wall"
	private static final TargetProperties tp = TargetProperties.cone(70, 5, false, TargetType.ENEMY);
	private static final Cone cone = new Cone(tp.range, tp.arc);
	// Cone body: dense concussive air blasting outward
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD).offsetY(0.5).count(3).spread(0.1, 0.1).speed(0.01);
	// Cone edge: sweeping gust arc to sell the kinetic burst
	private static final ParticleContainer sweep = new ParticleContainer(Particle.SWEEP_ATTACK).offsetY(0.5).count(1).spread(0.1, 0.1);
	// Punchy compressed-air release right in front of the caster on cast (intentional big-impact moment)
	private static final ParticleContainer castBurst = new ParticleContainer(Particle.EXPLOSION).offsetY(0.8).offsetForward(1.5).count(4).spread(0.6, 0.3);
	// Small streak on each shoved enemy showing them being blasted away
	private static final ParticleContainer knockbackTrail = new ParticleContainer(Particle.CLOUD).offsetY(0.5).count(6).spread(0.2, 0.3).speed(0.02);
	// Crunchy wall-slam impact (intentional big-impact moment for the bonus BLUNT hit)
	private static final ParticleContainer wallBurst = new ParticleContainer(Particle.EXPLOSION).offsetY(0.8).count(6).spread(0.4, 0.4);
	private static final ParticleContainer wallDebris = new ParticleContainer(Particle.CRIT).offsetY(0.8).count(14).spread(0.3, 0.4).speed(0.05);
	private static final SoundContainer castSound = new SoundContainer(Sound.ENTITY_WARDEN_SONIC_BOOM, 0.7F);
	private static final SoundContainer wallSound = new SoundContainer(Sound.BLOCK_ANVIL_LAND, 0.8F);

	private int wallDamage, wallConcussed;

	public ShockwaveTome(boolean isUpgraded) {
		super(ID, "Shockwave Tome", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.OFFHAND,
				EquipmentProperties.ofUsable(15, 0, 14, tp.range));
		wallDamage = isUpgraded ? 150 : 100;
		wallConcussed = isUpgraded ? 6 : 4;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.RIGHT_CLICK, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			castSound.play(p, p);
			Sounds.wind.play(p, p);
			cone.play(sweep, p.getLocation(), LocalAxes.usingEyeLocation(p), pc);
			castBurst.play(p, p.getLocation());
			for (LivingEntity ent : TargetHelper.getEntitiesInCone(p, tp)) {
				Vector dir = ent.getLocation().toVector().subtract(p.getLocation().toVector()).setY(0);
				if (dir.lengthSquared() == 0) dir = p.getLocation().getDirection().setY(0);
				dir = dir.normalize();

				boolean intoWall = pushedIntoWall(ent, dir);
				FightInstance.knockback(p, ent, KNOCKBACK);
				FightInstance.applyStatus(ent, StatusType.CONCUSSED, data, BASE_CONCUSSED, -1, this);
				knockbackTrail.play(p, ent.getLocation());

				if (intoWall) {
					FightInstance.dealDamage(new DamageMeta(data, wallDamage, DamageType.BLUNT,
							DamageStatTracker.of(id + slot, this)), ent);
					FightInstance.applyStatus(ent, StatusType.CONCUSSED, data, wallConcussed, -1, this);
					wallBurst.play(p, ent.getLocation());
					wallDebris.play(p, ent.getLocation());
					wallSound.play(p, ent.getLocation());
					Sounds.crit.play(p, ent.getLocation());
				}
			}
			return TriggerResult.keep();
		}));
	}

	// Returns true if a solid block sits within WALL_CHECK_DIST of the enemy along the push direction,
	// meaning the knockback will slam them into a wall.
	private boolean pushedIntoWall(LivingEntity ent, Vector dir) {
		Location base = ent.getLocation();
		double[] heights = { 0.3, 1.0, 1.6 };
		for (double dist = 0.5; dist <= WALL_CHECK_DIST; dist += 0.5) {
			for (double h : heights) {
				Location check = base.clone().add(dir.getX() * dist, h, dir.getZ() * dist);
				if (check.getBlock().getType().isSolid()) return true;
			}
		}
		return false;
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOOK, "On right click, push all enemies in a cone in front of you away and apply "
				+ GlossaryTag.CONCUSSED.tag(this, BASE_CONCUSSED, false) + ". For each enemy pushed into a wall, deal "
				+ GlossaryTag.BLUNT.tag(this, wallDamage, true) + " damage and apply an additional "
				+ GlossaryTag.CONCUSSED.tag(this, wallConcussed, true) + ".");
	}
}
