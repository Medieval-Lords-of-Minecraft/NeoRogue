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
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
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

public class EarthenTackle extends Equipment {
	private static final String ID = "EarthenTackle";
	private static final ParticleContainer pc = new ParticleContainer(Particle.EXPLOSION),
			start = new ParticleContainer(Particle.BLOCK),
			dirt = start.clone().spread(0.5, 0.5);
	private static final TargetProperties hc = TargetProperties.radius(1.5, true, TargetType.ENEMY),
			aoe = TargetProperties.radius(4, true, TargetType.ENEMY);
	private int damage, concussed;
	
	public EarthenTackle(boolean isUpgraded) {
		super(ID, "Earthen Tackle", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 25, 12, 0, hc.range));
		damage = isUpgraded ? 240 : 160;
		concussed = isUpgraded? 15 : 10;
		
		pc.count(25).spread(0.5, 0.5);
		start.count(25).spread(0.5, 0).offsetY(1).blockData(Material.DIRT.createBlockData());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		final EquipmentInstance inst = new EquipmentInstance(data, sessionEq, slot, es);
		inst.setAction((pdata, inputs) -> {
			Player p = data.getPlayer();
			Sounds.jump.play(p, p);
			start.play(p, p);
			data.applyStatus(StatusType.INVINCIBLE, data, 1, 10);
			Vector v = p.getEyeLocation().getDirection();
			if (p.isOnGround()) {
				p.teleport(p.getLocation().add(0, 0.2, 0));
			}
			p.setVelocity(v.setY(0).normalize().setY(0.3));
			new EarthenTackleHitChecker(p, data, inst, this, slot);
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
	}
	
	private class EarthenTackleHitChecker {
		private ArrayList<BukkitTask> tasks = new ArrayList<BukkitTask>();
		
		protected EarthenTackleHitChecker(Player p, PlayerFightData data, EquipmentInstance inst, Equipment eq, int slot) {
			for (long delay = 1; delay <= 10; delay++) {
				final boolean last = delay == 10;
				tasks.add(new BukkitRunnable() {
					public void run() {
						if (last) data.removeCleanupTask(id);
						LivingEntity first = TargetHelper.getNearest(p, hc);
						if (first == null) return;

						LinkedList<LivingEntity> hit = TargetHelper.getEntitiesInRadius(p, aoe);
						Vector v = p.getLocation().subtract(hit.getFirst().getLocation()).toVector();
						v = v.setY(Math.min(0.3, v.getY())).normalize(); // Limit how high a tackle can take you
						p.setVelocity(v.multiply(0.5));
						cancelTasks();
						inst.reduceCooldown(10);
						Sounds.explode.play(p, p);
						for (LivingEntity ent : hit) {
							pc.play(p, ent);
							dirt.play(p, ent);
							FightInstance.dealDamage(data, DamageType.EARTHEN, damage, ent, DamageStatTracker.of(id + slot, eq));
							FightInstance.applyStatus(ent, StatusType.CONCUSSED, p, concussed, -1, EarthenTackle.this);
						}
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
		item = createItem(Material.REDSTONE,
				"On cast, dash forward, stopping at the first enemy hit and dealing " + DescUtil.val(damage) + " " + GlossaryTag.EARTHEN.tag(this) +
				" damage in an area "
						+ "and applies " + GlossaryTag.CONCUSSED.tag(this, concussed) + ", and become invulnerable [" + DescUtil.val("0.5s") + "]. "
						+ "If an enemy is hit, reduce this ability's cooldown by " + DescUtil.val(10) + ".");
	}
}
