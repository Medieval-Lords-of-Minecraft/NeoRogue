package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
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
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SoulBattery extends Equipment {
	private static final String ID = "SoulBattery";
	private static final ParticleContainer lightningCore = new ParticleContainer(Particle.END_ROD)
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer lightningGlow = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(100, 180, 255), 1.2F))
			.count(1).spread(0.05, 0).speed(0);
	private static final ParticleContainer circleEdge = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(100, 180, 255), 0.8F))
			.count(1).spread(0, 0).speed(0);
	private static final TargetProperties boltAoe = TargetProperties.radius(3, false, TargetType.ENEMY);
	private static final int SHIELD_DURATION = 160; // 8s
	private static final int STRIKE_DELAY = 40; // 2s
	private static final int STRIKE_COUNT = 3;
	private static final int STRIKE_RADIUS = 5;
	private static final Circle circ = new Circle(STRIKE_RADIUS);
	private int threshold, damage;

	public SoulBattery(boolean isUpgraded) {
		super(ID, "Soul Battery", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 12, 0));
		threshold = isUpgraded ? 15 : 10;
		damage = isUpgraded ? 225 : 150;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			int totalElectrified = data.getStats().getStatusesApplied().getOrDefault(StatusType.ELECTRIFIED, 0);
			int shields = totalElectrified / threshold;
			if (shields > 0) {
				data.addSimpleShield(p.getUniqueId(), shields, SHIELD_DURATION);
			}

			Location center = p.getLocation().clone();
			circ.play(circleEdge, center, LocalAxes.xz(), null);
			data.addTask(new BukkitRunnable() {
				@Override
				public void run() {
					Player p2 = data.getPlayer();
					for (int i = 0; i < STRIKE_COUNT; i++) {
						double angle = Math.random() * 2 * Math.PI;
						double distance = Math.random() * STRIKE_RADIUS;
						double x = Math.cos(angle) * distance;
						double z = Math.sin(angle) * distance;
						Location loc = center.clone().add(x, 0, z);
						ParticleUtil.drawLine(p2, lightningCore, loc.clone().add(0, 5, 0), loc, 0.3);
						ParticleUtil.drawLine(p2, lightningGlow, loc.clone().add(0, 5, 0), loc, 0.25);
						Sounds.thunder.play(p2, loc);
						for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p2, loc, boltAoe)) {
							FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.LIGHTNING,
									DamageStatTracker.of(id + slot, eq)), ent);
						}
					}
				}
			}.runTaskLater(NeoRogue.inst(), STRIKE_DELAY));
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RESPAWN_ANCHOR,
				"On cast, gain " + GlossaryTag.SHIELDS.tag(this, 1, true) + " [<white>8s</white>] for every <yellow>" + threshold +
				"</yellow> " + GlossaryTag.ELECTRIFIED.tag(this) + " you applied this fight. After <white>2s</white>, " +
				"drop <white>3</white> bolts of lightning at random in a <white>5</white> block radius, each dealing " +
				GlossaryTag.LIGHTNING.tag(this, damage, true) + " in a <white>3</white> block radius.");
	}
}
