package me.neoblade298.neorogue.equipment.abilities;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BreakTheLine extends Equipment {
	private static final String ID = "breakTheLine";
	private int conc, damage = 220;
	private static final ParticleContainer pc = new ParticleContainer(Particle.EXPLOSION_LARGE).count(15).spread(3, 0);
	private static final TargetProperties tp = TargetProperties.radius(4, true, TargetType.ENEMY);
	
	public BreakTheLine(boolean isUpgraded) {
		super(ID, "Break the Line", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 40, 15, tp.range));
		conc = isUpgraded ? 12 : 8;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		BreakTheLineInstance inst = new BreakTheLineInstance(data, this, slot, es);
		data.addTrigger(id, bind, inst);
		data.addTrigger(id, Trigger.FALL_DAMAGE, (pdata, in) -> {
			return TriggerResult.of(false, inst.inAir);
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TNT,
				"On cast, leap in the air and slam down. All nearby enemies will take " + GlossaryTag.EARTHEN.tag(this, damage, false) + ", get knocked back," +
				" given slowness <white>1</white> for <white>3</white> seconds, and get " + GlossaryTag.CONCUSSED.tag(this, conc, true) + ".");
	}
	
	private class BreakTheLineInstance extends EquipmentInstance {
		private boolean inAir;
		public BreakTheLineInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			action = (pdata, inputs) -> {
				Player p = data.getPlayer();
				Sounds.jump.play(p, p);
				p.setVelocity(p.getVelocity().add(new Vector(0, 1, 0)));
				inAir = true;
				
				pdata.addTask(new BukkitRunnable() {
					public void run() {
						p.setVelocity(p.getVelocity().add(new Vector(0, -1, 0)));
					}
				}.runTaskLater(NeoRogue.inst(), 10L));
				
				pdata.addTask(new BukkitRunnable() {
					public void run() {
						Sounds.explode.play(p, p);
						pc.play(p, p);
						LinkedList<LivingEntity> targets = TargetHelper.getEntitiesInRadius(p, tp);
						for (LivingEntity ent : targets) {
							FightInstance.knockback(p, ent, 0.9);
							ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 0));
							FightInstance.dealDamage(new DamageMeta(pdata, damage, DamageType.EARTHEN), ent);
							FightInstance.applyStatus(ent, StatusType.CONCUSSED, p, conc, -1);
						}

					}
				}.runTaskLater(NeoRogue.inst(), 15L));
				
				return TriggerResult.keep();
			};
		}
		
	}
}
