package me.neoblade298.neorogue.equipment.abilities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Bulldoze extends Equipment {
	private static final String ID = "Bulldoze";
	private static final ParticleContainer pc = new ParticleContainer(Particle.EXPLOSION),
			start = new ParticleContainer(Particle.CLOUD),
			wake = new ParticleContainer(Particle.EXPLOSION);
	private static final TargetProperties hc = TargetProperties.radius(2, true, TargetType.ENEMY);
	private int damage;
	
	public Bulldoze(boolean isUpgraded) {
		super(ID, "Bulldoze", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 25, 20, 0));
		damage = isUpgraded ? 130 : 100;
		
		pc.count(25).spread(0.5, 0.5);
		start.count(25).spread(0.5, 0);
		wake.count(25).spread(0.5, 0).offsetForward(2);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, inputs) -> {
			Player p = data.getPlayer();
			Sounds.roar.play(p, p);
			start.play(p, p);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0));
			new BulldozeHitChecker(p, data, this, slot);
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
	}
	
	private class BulldozeHitChecker {
		private ArrayList<BukkitTask> tasks = new ArrayList<BukkitTask>();
		private HashSet<UUID> hitList = new HashSet<UUID>();
		
		protected BulldozeHitChecker(Player p, PlayerFightData data, Equipment eq, int slot) {
			for (long delay = 2; delay <= 60; delay+= 2) {
				boolean spawnParticle = delay % 4 == 0;
				tasks.add(new BukkitRunnable() {
					public void run() {
						if (spawnParticle) {
							wake.play(p, p);
							Sounds.explode.play(p, p);
						}
						
						LinkedList<LivingEntity> hit = TargetHelper.getEntitiesInRadius(p, hc);
						if (hit.size() == 0) return;

						for (LivingEntity ent : hit) {
							if (hitList.contains(ent.getUniqueId())) continue;
							hitList.add(ent.getUniqueId());
							pc.play(p, ent);
							FightInstance.dealDamage(data, DamageType.BLUNT, damage + data.getShields().getAmount(), ent, DamageStatTracker.of(id + slot, eq));
							FightInstance.knockback(p, ent, 2);
						}
					}
				}.runTaskLater(NeoRogue.inst(), delay));
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
