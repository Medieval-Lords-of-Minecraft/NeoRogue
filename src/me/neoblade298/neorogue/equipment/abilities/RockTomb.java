package me.neoblade298.neorogue.equipment.abilities;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
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
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class RockTomb extends Equipment {
	private static final String ID = "RockTomb";
	private static final int RADIUS = 4, RANGE = 14, CONCUSSED_THRESHOLD = 30;
	private static final TargetProperties tp = TargetProperties.radius(RADIUS, true);
	private static final Circle circ = new Circle(RADIUS);

	// Circle indicator particles
	private static final ParticleContainer edge = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.STONE.createBlockData())
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer fill = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.COARSE_DIRT.createBlockData())
			.count(1).spread(0.1, 0).speed(0);

	// Impact particles
	private static final ParticleContainer impactStone = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.STONE.createBlockData())
			.count(40).spread(2, 1).speed(0.3);
	private static final ParticleContainer impactDust = new ParticleContainer(Particle.CLOUD)
			.count(25).spread(2, 0.5).speed(0.1);

	// Boulder formation animation particle
	private static final ParticleContainer boulderPart = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.COBBLESTONE.createBlockData())
			.count(1).spread(0.1, 0.1).speed(0);

	// Boulder animation (40 ticks = 2 seconds)
	private static final ParticleAnimation boulderAnim;

	static {
		boulderAnim = new ParticleAnimation(boulderPart, (loc, tick) -> {
			LinkedList<Location> partLocs = new LinkedList<Location>();
			if (tick < 30) {
				// Formation: particles spiral inward and rise
				double progress = tick / 30.0;
				double radius = 4 * (1 - progress);
				double height = 6 * progress;
				int numPoints = 6;
				double angleOffset = tick * 18;
				for (int i = 0; i < numPoints; i++) {
					double angle = Math.toRadians(angleOffset + (360.0 / numPoints) * i);
					double x = radius * Math.cos(angle);
					double z = radius * Math.sin(angle);
					partLocs.add(loc.clone().add(x, height, z));
				}
			} else {
				// Descent: tight cluster slams down
				double descentProgress = (tick - 30) / 10.0;
				double height = 6 * (1 - descentProgress);
				int numPoints = 8;
				for (int i = 0; i < numPoints; i++) {
					double angle = Math.toRadians((360.0 / numPoints) * i + tick * 25);
					double x = 0.5 * Math.cos(angle);
					double z = 0.5 * Math.sin(angle);
					partLocs.add(loc.clone().add(x, height, z));
				}
			}
			return partLocs;
		}, 40);
	}

	private int damage, bonusDamage;

	public RockTomb(boolean isUpgraded) {
		super(ID, "Rock Tomb", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(25, 0, 14, RANGE, RADIUS));
		damage = 300;
		bonusDamage = isUpgraded ? 100 : 50;
		properties.addUpgrades(PropertyType.COOLDOWN);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta stacks = new ActionMeta();
		ItemStack icon = item.clone();
		Equipment eq = this;

		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);

		// Track concussed applications
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.CONCUSSED)) return TriggerResult.keep();
			stacks.addCount(ev.getStacks());
			int displayed = stacks.getCount() / CONCUSSED_THRESHOLD;
			if (displayed > 0) {
				icon.setAmount(Math.min(displayed, 64));
				inst.setIcon(icon);
			}
			return TriggerResult.keep();
		});

		inst.setAction((pdata, in) -> {
			Player p = data.getPlayer();
			Block b = p.getTargetBlockExact(RANGE);
			if (b == null) {
				Sounds.error.play(p, p);
				return TriggerResult.keep();
			}

			Location targetLoc = b.getLocation().add(0, 1, 0);
			circ.play(edge, targetLoc, LocalAxes.xz(), fill);
			data.runAnimation(id, p, boulderAnim, targetLoc);

			data.charge(40).then(new Runnable() {
				public void run() {
					Player p = data.getPlayer();
					Block b = p.getTargetBlockExact(RANGE);
					if (b == null) {
						data.addMana(inst.getLastCastEvent().getManaCost());
						inst.setCooldown(0);
						Sounds.error.play(p, p);
						return;
					}

					Location loc = b.getLocation().add(0, 1, 0);
					impactStone.play(p, loc);
					impactDust.play(p, loc);
					Sounds.explode.play(p, loc);

					int bonus = (stacks.getCount() / CONCUSSED_THRESHOLD) * bonusDamage;
					double totalDamage = damage + bonus;

					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
						FightInstance.dealDamage(new DamageMeta(data, totalDamage, DamageType.EARTHEN,
								DamageStatTracker.of(id + slot, eq)), ent);
					}
				}
			});
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.COBBLESTONE,
				"On cast, " + DescUtil.charge(this, 1, 2) + " before dropping a boulder at the block you aim at, dealing "
				+ GlossaryTag.EARTHEN.tag(this, damage, false) + " damage in a " + DescUtil.white(RADIUS)
				+ " block radius. For every " + DescUtil.white(CONCUSSED_THRESHOLD) + " "
				+ GlossaryTag.CONCUSSED.tag(this) + " applied this fight, increase the damage by "
				+ DescUtil.yellow(bonusDamage) + ".");
	}
}
