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
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Tackle extends Equipment {
	private ParticleContainer pc = new ParticleContainer(Particle.EXPLOSION_LARGE),
			start = new ParticleContainer(Particle.CLOUD);
	private static final TargetProperties hc = TargetProperties.radius(1.5, true, TargetType.ENEMY),
			aoe = TargetProperties.radius(2, true, TargetType.ENEMY);
	private int damage;
	
	public Tackle(boolean isUpgraded) {
		super("tackle", "Tackle", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 10, 20, 0));
		damage = isUpgraded ? 150 : 100;
		
		pc.count(25).spread(0.5, 0.5);
		start.count(25).spread(0.5, 0);
		
		addReforgeOption("tackle", "bulldoze", "earthenTackle");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EquipmentInstance inst = new EquipmentInstance(p, this, slot, es);
		inst.setAction((pdata, in) -> {
			Util.playSound(p, Sound.ENTITY_SHULKER_SHOOT, false);
			start.spawn(p);
			Vector v = p.getEyeLocation().getDirection();
			if (p.isOnGround()) {
				p.teleport(p.getLocation().add(0, 0.2, 0));
			}
			p.setVelocity(v.setY(0).normalize().setY(0.3));
			new TackleHitChecker(p, data, inst);
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
	}
	
	private class TackleHitChecker {
		private ArrayList<BukkitTask> tasks = new ArrayList<BukkitTask>();
		private PlayerFightData data;
		
		protected TackleHitChecker(Player p, PlayerFightData data, EquipmentInstance inst) {
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
						for (LivingEntity ent : hit) {
							pc.spawn(p);
							FightInstance.dealDamage(data, DamageType.BLUNT, damage, ent);
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
		item = createItem(Material.REDSTONE, new String[] { "<gold>Area of Effect: <white>2" },
				"On cast, dash forward, stopping at the first enemy hit and dealing <yellow>" + damage + "</yellow> " + GlossaryTag.BLUNT.tag(this) +
				" damage in a small area. "
						+ "If an enemy is hit, reduce this ability's cooldown by <white>10</white>.");
	}
}
