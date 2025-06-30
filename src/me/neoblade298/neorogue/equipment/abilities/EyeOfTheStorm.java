package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
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

public class EyeOfTheStorm extends Equipment {
	private static final String ID = "eyeOfTheStorm";
	private int damage, elec;
	private static final TargetProperties tp = TargetProperties.radius(4, false);
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANTED_HIT);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_STRAY_DEATH);
	private static final Circle circ = new Circle(tp.range);

	public EyeOfTheStorm(boolean isUpgraded) {
		super(ID, "Eye Of The Storm", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(30, 0, 16, 0, tp.range));
		damage = isUpgraded ? 120 : 80;
		elec = isUpgraded ? 60 : 40;
		pc.count(10).spread(0.5, 0.5).speed(0.2);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(p, p);
			data.charge(20).then(new Runnable() {
				public void run() {
					data.addTask(new BukkitRunnable() {
						int count = 0;

						public void run() {
							circ.play(pc, p.getLocation(), LocalAxes.xz(), null);
							circ.play(pc, p.getLocation().add(0, 0.2, 0), LocalAxes.xz(), null);
							sc.play(p.getLocation());
							for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
								FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.LIGHTNING, DamageStatTracker.of(id + slot, eq)), ent);
								FightInstance.applyStatus(ent, StatusType.ELECTRIFIED, data, elec, -1);
							}
							if (++count >= 3) {
								cancel();
								return;
							}
						}
					}.runTaskTimer(NeoRogue.inst(), 20, 20));
				}
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ENDER_PEARL,
				"On cast, " + DescUtil.charge(this, 1, 1) + " before dealing "
						+ GlossaryTag.LIGHTNING.tag(this, damage, true) + " damage and applying "
						+ GlossaryTag.ELECTRIFIED.tag(this, elec, true)
						+ " to enemies near you <white>3x</white> over <white>3s</white>.");
	}
}
