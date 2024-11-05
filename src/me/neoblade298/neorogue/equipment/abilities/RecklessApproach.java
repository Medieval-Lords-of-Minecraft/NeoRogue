package me.neoblade298.neorogue.equipment.abilities;

import java.util.ArrayList;
import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class RecklessApproach extends Equipment {
	private static final String ID = "recklessApproach";
	private static final ParticleContainer pc = new ParticleContainer(Particle.EXPLOSION),
			start = new ParticleContainer(Particle.CLOUD);
	private static final TargetProperties hc = TargetProperties.radius(1.5, true, TargetType.ENEMY),
			aoe = TargetProperties.radius(2.5, true, TargetType.ENEMY);
	private int damage, inc, shields, thres;
	
	public RecklessApproach(boolean isUpgraded) {
		super(ID, "RecklessApproach", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(5, 25, 14, 0).add(PropertyType.AREA_OF_EFFECT, 2));
		damage = isUpgraded ? 160 : 120;
		shields = isUpgraded ? 8 : 5;
		inc = isUpgraded ? 20 : 15;
		thres = 6;
		
		pc.count(25).spread(0.5, 0.5);
		start.count(25).spread(0.5, 0);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			Sounds.jump.play(p, p);
			start.play(p, p);
			Vector v = p.getEyeLocation().getDirection();
			if (p.isOnGround()) {
				p.teleport(p.getLocation().add(0, 0.2, 0));
			}
			p.setVelocity(v.setY(0).normalize().setY(0.3));
			data.addBuff(data, ID, true, false, BuffType.GENERAL, inc, 100);
			new RecklessApproachHitbox(p, data, inst);
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);

		double thresSq = thres * thres;
		data.addTrigger(ID, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			if (!inst.getBool()) return TriggerResult.keep();
			DealtDamageEvent ev = (DealtDamageEvent) in;
			if (!ev.getMeta().hasOrigin(DamageOrigin.PROJECTILE)) return TriggerResult.keep();
			if (ev.getMeta().getProjectile().getOrigin().distanceSquared(ev.getTarget().getLocation()) >= thresSq) return TriggerResult.keep();
			return TriggerResult.keep();
		});
	}
	
	private class RecklessApproachHitbox {
		private ArrayList<BukkitTask> tasks = new ArrayList<BukkitTask>();
		
		protected RecklessApproachHitbox(Player p, PlayerFightData data, StandardEquipmentInstance inst) {
			for (long delay = 1; delay <= 10; delay++) {
				final boolean last = delay == 10;
				tasks.add(new BukkitRunnable() {
					public void run() {
						LivingEntity first = TargetHelper.getNearest(p, hc);
						if (last) data.removeCleanupTask(id);
						if (first == null) return;

						LinkedList<LivingEntity> hit = TargetHelper.getEntitiesInRadius(p, aoe);
						Vector v = p.getLocation().subtract(hit.getFirst().getLocation()).toVector();
						v = v.setY(Math.min(0.3, v.getY())).normalize(); // Limit how high a tackle can take you
						p.setVelocity(v.multiply(0.5));
						data.removeCleanupTask(id);
						cancelTasks();
						Sounds.explode.play(p, p);
						for (LivingEntity ent : hit) {
							pc.play(p, p);
							FightInstance.dealDamage(data, DamageType.BLUNT, damage, ent);
						}
						data.addSimpleShield(p.getUniqueId(), shields, 100);
						inst.setBool(true);
						data.addTask(new BukkitRunnable() {
							public void run() {
								inst.setBool(false);
							}
						}.runTaskLater(NeoRogue.inst(), 100));
					}
				}.runTaskLater(NeoRogue.inst(), delay));
			}
			
			data.addCleanupTask(id, () -> { cancelTasks(); });
		}
		
		private void cancelTasks() {
			for (BukkitTask task : tasks) {
				task.cancel();
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_TORCH,
				"On cast, dash forward, stopping at the first enemy hit and dealing <yellow>" + damage + "</yellow> " + GlossaryTag.BLUNT.tag(this) +
				" damage in a small area. If an enemy is hit, gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>5s</white>]. " +
				"Increase projectile damage from at most " + DescUtil.white(thres) + " blocks away by " + DescUtil.yellow(inc) + " [<white>5s</white>].");
	}
}
