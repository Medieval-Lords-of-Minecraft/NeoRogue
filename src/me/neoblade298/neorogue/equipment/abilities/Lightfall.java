package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Lightfall extends Equipment {
	private static final String ID = "lightfall";
	private static final TargetProperties tp = TargetProperties.radius(5, false);
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			explode = new ParticleContainer(Particle.FIREWORK).count(50).spread(tp.range / 2, 1).speed(0.01);
	private static final Circle anim = new Circle(3), aoe = new Circle(tp.range);
	private int damage, sanct;
	
	public Lightfall(boolean isUpgraded) {
		super(ID, "Lightfall", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(30, 30, 12, 0, tp.range));
				damage = isUpgraded ? 600 : 400;
				sanct = isUpgraded ? 150 : 100;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.flap.play(p, p);
			p.setVelocity(new Vector(0, 1, 0));
			am.setBool(true);
			data.addTask(new BukkitRunnable() {
				private int count;
				public void run() {
					anim.play(pc, p.getLocation(), LocalAxes.xz(), null);
					if (++count >= 3) {
						cancel();
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 0, 1));

			data.addTask(new BukkitRunnable() {
				public void run() {
					p.setVelocity(new Vector(0, -2, 0));
				}
			}.runTaskLater(NeoRogue.inst(), 7));

			data.addTask(new BukkitRunnable() {
				public void run() {
					am.setBool(false);
				}
			}.runTaskLater(NeoRogue.inst(), 30));

			data.addTask(new BukkitRunnable() {
				int count = 0;
				@SuppressWarnings("deprecation")
				public void run() {
					if (!am.getBool()) {
						cancel();
						return;
					}

					if (p.isOnGround()) {
						activateDamage(p, data, slot);
						am.setBool(false);
					}

					if (++count >= 22) { // Arbitrary basically
						cancel();
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 8, 1));
			return TriggerResult.keep();
		}));

		data.addTrigger(id, Trigger.FALL_DAMAGE, (pdata, in) -> {
			if (am.getBool()) {
				am.setBool(false);
				activateDamage(p, data, slot);
				return TriggerResult.of(false, true);
			}
			return TriggerResult.keep();
		});
	}

	private void activateDamage(Player p, PlayerFightData data, int slot) {
		aoe.play(pc, p.getLocation(), LocalAxes.xz(), null);
		explode.play(p, p);
		Sounds.explode.play(p, p);
		for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
			FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.LIGHT, DamageStatTracker.of(id + slot, this)), ent);
			FightInstance.applyStatus(ent, StatusType.SANCTIFIED, data, sanct, -1);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WHITE_BANNER,
				"On cast, jump into the air before crashing to the ground, dealing " +
				GlossaryTag.LIGHT.tag(this, damage, true) + " damage and applying " + GlossaryTag.SANCTIFIED.tag(this, sanct, true) + 
				" to all nearby enemies.");
	}
}
