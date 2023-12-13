package me.neoblade298.neorogue.equipment.abilities;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
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
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Tackle extends Ability {
	private ParticleContainer pc = new ParticleContainer(Particle.REDSTONE);
	private static final TargetProperties hc = new TargetProperties(1, true, TargetType.ENEMY);
	
	public Tackle(boolean isUpgraded) {
		super("tackle", "Tackle", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		setBaseProperties(20, 0, 75, 5);
		int strength = isUpgraded ? 20 : 14;
		item = createItem(this, Material.REDSTONE, null,
				"On cast, give yourself <yellow>" + strength + " </yellow>bonus physical damage for <yellow>10</yellow> seconds.");
		
		pc.count(50).offset(0.5, 0.5).dustOptions(new DustOptions(Color.RED, 1F));
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addHotbarTrigger(id, slot, bind, new BattleCryInstance(this, p));
	}
	
	private class BattleCryInstance extends UsableInstance {
		private Player p;
		public BattleCryInstance(Ability a, Player p) {
			super(a);
			this.p = p;
			this.cooldown = a.getCooldown();
		}
		
		@Override
		public TriggerResult run(PlayerFightData data, Object[] inputs) {
			Util.playSound(p, Sound.ENTITY_SHULKER_SHOOT, false);
			Vector v = p.getEyeLocation().toVector();
			p.setVelocity(v.setY(0).normalize().multiply(3).setY(0.5));
			pc.spawn(p);
			data.addBuff(p.getUniqueId(), id, true, false, BuffType.PHYSICAL, isUpgraded ? 20 : 14, 10);
			return TriggerResult.keep();
		}
	}
	
	private class TackleHitChecker {
		private ArrayList<BukkitTask> tasks = new ArrayList<BukkitTask>();
		
		protected TackleHitChecker(Player p) {
			for (long delay = 1; delay <= 10; delay++) {
				tasks.add(new BukkitRunnable() {
					public void run() {
						// check collision and deal damage
						for (LivingEntity ent : TargetHelper.(p, hc)) {
						}
					}
				}.runTaskLater(NeoRogue.inst(), delay));
			}
		}
		
		private void cancelTasks() {
			for (BukkitTask task : tasks) {
				task.cancel();
			}
		}
	}
}
