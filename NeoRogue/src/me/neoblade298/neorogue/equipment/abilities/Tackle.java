package me.neoblade298.neorogue.equipment.abilities;

import java.util.ArrayList;
import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Ability;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.UsableInstance;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Tackle extends Ability {
	private ParticleContainer pc = new ParticleContainer(Particle.EXPLOSION_LARGE),
			start = new ParticleContainer(Particle.CLOUD);
	private static final TargetProperties hc = new TargetProperties(1, true, TargetType.ENEMY),
			aoe = new TargetProperties(2, true, TargetType.ENEMY);
	private int damage;
	
	public Tackle(boolean isUpgraded) {
		super("tackle", "Tackle", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		setBaseProperties(2, 0, 1, 5); // 25 cd, 75 stamina
		damage = isUpgraded ? 300 : 200;
		item = createItem(this, Material.REDSTONE, null,
				"On cast, dash forward, stopping at the first enemy hit and dealing <yellow>" + damage + "</yellow> damage in a small area. "
						+ "If an enemy is hit, reduce this ability's cooldown by <yellow>10</yellow>.");
		
		pc.count(25).spread(0.5, 0.5);
		start.count(25).spread(0.5, 0);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, bind, new TackleInstance(this, p));
	}
	
	private class TackleInstance extends UsableInstance {
		private Player p;
		public TackleInstance(Ability a, Player p) {
			super(a);
			this.p = p;
		}
		
		@Override
		public TriggerResult run(PlayerFightData data, Object[] inputs) {
			Util.playSound(p, Sound.ENTITY_SHULKER_SHOOT, false);
			start.spawn(p);
			double ySpeed = p.getVelocity().getY();
			Vector v = p.getEyeLocation().getDirection();
			p.setVelocity(v.setY(0).normalize().setY(ySpeed < 0 ? ySpeed : 0.5));
			new TackleHitChecker(p, data, this);
			return TriggerResult.keep();
		}
	}
	
	private class TackleHitChecker {
		private ArrayList<BukkitTask> tasks = new ArrayList<BukkitTask>();
		private PlayerFightData data;
		
		protected TackleHitChecker(Player p, PlayerFightData data, TackleInstance inst) {
			this.data = data;
			for (long delay = 1; delay <= 10; delay++) {
				tasks.add(new BukkitRunnable() {
					public void run() {
						LivingEntity first = TargetHelper.getNearest(p, hc);
						if (first == null) return;

						LinkedList<LivingEntity> hit = TargetHelper.getEntitiesInRadius(p, aoe);
						Vector v = p.getLocation().subtract(hit.getFirst().getLocation()).toVector();
						v = v.setY(Math.min(0.3, v.getY())).normalize(); // Limit how high a tackle can take you
						p.setVelocity(v.multiply(0.5));
						cancelTasks();
						inst.reduceCooldown(10);
						Util.playSound(p, Sound.ENTITY_GENERIC_EXPLODE, false);
						pc.spawn(p);
						for (LivingEntity ent : hit) {
							FightInstance.dealDamage(p, DamageType.BLUNT, damage, ent);
						}
					}
				}.runTaskLater(NeoRogue.inst(), delay));
			}
			
			data.addCleanupTask(id, () -> { cancelTasks(); });
		}
		
		private void cancelTasks() {
			data.removeCleanupTask(id);
			for (BukkitTask task : tasks) {
				task.cancel();
			}
		}
	}
}
