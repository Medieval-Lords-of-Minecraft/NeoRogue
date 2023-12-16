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

public class Bulldoze extends Ability {
	private ParticleContainer pc = new ParticleContainer(Particle.EXPLOSION_LARGE),
			start = new ParticleContainer(Particle.CLOUD);
	private static final TargetProperties hc = new TargetProperties(2, true, TargetType.ENEMY);
	private int damage;
	
	public Bulldoze(boolean isUpgraded) {
		super("bulldoze", "Bulldoze", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		setBaseProperties(5, 0, 1, 5); // 25 cd, 75 stamina
		damage = isUpgraded ? 300 : 200;
		
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
			Util.playSound(p, Sound.ENTITY_ENDER_DRAGON_AMBIENT, false);
			start.spawn(p);
			new BulldozeHitChecker(p, data, this);
			return TriggerResult.keep();
		}
	}
	
	private class BulldozeHitChecker {
		private ArrayList<BukkitTask> tasks = new ArrayList<BukkitTask>();
		private PlayerFightData data;
		
		protected BulldozeHitChecker(Player p, PlayerFightData data, TackleInstance inst) {
			this.data = data;
			for (long delay = 2; delay <= 60; delay+= 2) {
				tasks.add(new BukkitRunnable() {
					public void run() {
						LinkedList<LivingEntity> hit = TargetHelper.getEntitiesInRadius(p, hc);
						if (hit.size() == 0) return;

						Util.playSound(p, Sound.ENTITY_GENERIC_EXPLODE, false);
						for (LivingEntity ent : hit) {
							pc.spawn(ent);
							FightInstance.dealDamage(p, DamageType.BLUNT, damage, ent);
							FightInstance.knockback(p, ent, 2);
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

	@Override
	public void setupItem() {
		item = createItem(this, Material.REDSTONE, null,
				"On cast, gain speed for 3 seconds, dealing <yellow>" + damage + "</yellow> damage to enemies you touch"
						+ " and knock them back, once per enemy.");
	}
}
