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
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SoulBattery extends Equipment {
	private static final String ID = "SoulBattery";
	private static final ParticleContainer lightningCore = new ParticleContainer(Particle.END_ROD)
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer lightningGlow = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(100, 180, 255), 1.2F)).offsetY(0.5)
			.count(1).spread(0.05, 0).speed(0);
	private static final ParticleContainer circleEdge = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(100, 180, 255), 0.8F)).offsetY(0.5)
			.count(1).spread(0, 0).speed(0);
	private static final TargetProperties boltAoe = TargetProperties.radius(3, false, TargetType.ENEMY);
	private static final int SHIELD_DURATION = 160; // 8s
	private static final int STRIKE_DELAY = 40; // 2s
	private static final int STRIKE_RADIUS = 5;
	private static final Circle circ = new Circle(STRIKE_RADIUS);
	private static final Circle boltCirc = new Circle(3);
	private int threshold, damage;

	public SoulBattery(boolean isUpgraded) {
		super(ID, "Soul Battery", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(24, 4, 5, 0));
		threshold = isUpgraded ? 7 : 10;
		damage = isUpgraded ? 190 : 125;
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
					// Bolt directly in front of the player
					Location front = p2.getLocation().clone().add(p2.getLocation().getDirection().setY(0).normalize().multiply(2));
					strikeBolt(p2, data, front, eq, slot);
					// 2 random bolts
					for (int i = 0; i < 2; i++) {
						double angle = Math.random() * 2 * Math.PI;
						double distance = Math.random() * STRIKE_RADIUS;
						double x = Math.cos(angle) * distance;
						double z = Math.sin(angle) * distance;
						Location loc = center.clone().add(x, 0, z);
						strikeBolt(p2, data, loc, eq, slot);
					}
				}
			}.runTaskLater(NeoRogue.inst(), STRIKE_DELAY));
			return TriggerResult.keep();
		}));
	}

	private void strikeBolt(Player p, PlayerFightData data, Location loc, Equipment eq, int slot) {
		// Jagged lightning bolt from 5 blocks above down to loc
		int segments = 5;
		Location top = loc.clone().add(0, 5, 0);
		Location prev = top.clone();
		for (int i = 1; i <= segments; i++) {
			Location next;
			if (i == segments) {
				next = loc.clone();
			} else {
				double t = (double) i / segments;
				double offsetX = (Math.random() - 0.5) * 0.8;
				double offsetZ = (Math.random() - 0.5) * 0.8;
				next = loc.clone().add(offsetX, 5 * (1 - t), offsetZ);
			}
			ParticleUtil.drawLine(p, lightningCore, prev, next, 0.3);
			ParticleUtil.drawLine(p, lightningGlow, prev, next, 0.25);
			prev = next;
		}
		// AoE radius indicator
		boltCirc.play(circleEdge, loc, LocalAxes.xz(), null);
		Sounds.thunder.play(p, loc);
		for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, boltAoe)) {
			FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.LIGHTNING,
					DamageStatTracker.of(id + slot, eq)), ent);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RESPAWN_ANCHOR,
				"On cast, gain " + GlossaryTag.SHIELDS.tag(this, 1, false) + " [<white>8s</white>] for every " + DescUtil.yellow(threshold) +
				" " + GlossaryTag.ELECTRIFIED.tag(this) + " you applied this fight. After " + DescUtil.white("2s") + ", " +
				"strike a bolt of lightning " + DescUtil.white(2) + " blocks in front of you and drop " + DescUtil.white(2) + " bolts at random in a " + DescUtil.white(5) + " block radius, each dealing " +
				GlossaryTag.LIGHTNING.tag(this, damage, true) + " in a " + DescUtil.white(3) + " block radius.");
	}
}
