package me.neoblade298.neorogue.equipment.abilities;

import java.util.ArrayList;
import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
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

public class Bulldoze extends Equipment {
	private ParticleContainer pc = new ParticleContainer(Particle.EXPLOSION_LARGE),
			start = new ParticleContainer(Particle.CLOUD),
			wake = new ParticleContainer(Particle.EXPLOSION_NORMAL);
	private static final TargetProperties hc = TargetProperties.radius(2, true, TargetType.ENEMY);
	private int damage;
	
	public Bulldoze(boolean isUpgraded) {
		super("bulldoze", "Bulldoze", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 20, 20, 0));
		damage = isUpgraded ? 250 : 200;
		
		pc.count(25).spread(0.5, 0.5);
		start.count(25).spread(0.5, 0);
		wake.count(25).spread(0.5, 0).offsetForward(2);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EquipmentInstance inst = new EquipmentInstance(p, this, slot, es);
		inst.setAction((pdata, inputs) -> {
			Util.playSound(p, Sound.ENTITY_ENDER_DRAGON_AMBIENT, false);
			start.spawn(p);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0));
			new BulldozeHitChecker(p, data);
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
	}
	
	private class BulldozeHitChecker {
		private ArrayList<BukkitTask> tasks = new ArrayList<BukkitTask>();
		private PlayerFightData data;
		
		protected BulldozeHitChecker(Player p, PlayerFightData data) {
			this.data = data;
			for (long delay = 2; delay <= 60; delay+= 2) {
				boolean spawnParticle = delay % 4 == 0;
				tasks.add(new BukkitRunnable() {
					public void run() {
						if (spawnParticle) wake.spawn(p);
						
						LinkedList<LivingEntity> hit = TargetHelper.getEntitiesInRadius(p, hc);
						if (hit.size() == 0) return;

						Util.playSound(p, Sound.ENTITY_GENERIC_EXPLODE, false);
						for (LivingEntity ent : hit) {
							pc.spawn(ent);
							FightInstance.dealDamage(data, DamageType.BLUNT, damage + (data.getShields() != null ? data.getShields().getAmount() : 0), ent);
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
		item = createItem(Material.IRON_CHESTPLATE,
				"On cast, gain speed for 3 seconds, dealing <yellow>" + damage + "</yellow> " + GlossaryTag.BLUNT.tag(this) + " damage plus any "
						+ GlossaryTag.SHIELDS.tag(this) + " you have to enemies you touch and knock them back, once per enemy.");
	}
}
