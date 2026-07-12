package me.neoblade298.neorogue.equipment.weapons;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BeamStaff extends Equipment {
	private static final String ID = "BeamStaff";
	private static final StatusType[] STATUS_POOL = { StatusType.BURN, StatusType.INSANITY, StatusType.CONCUSSED, StatusType.FROST };
	private static final TargetProperties tp = TargetProperties.radius(10, false),
		aoe = TargetProperties.radius(2, false);
	private static final ParticleContainer beamPart = new ParticleContainer(Particle.END_ROD).count(1).spread(0, 0).speed(0),
			spiralPart = new ParticleContainer(Particle.FIREWORK).count(1).spread(0, 0).speed(0.01),
			burst = new ParticleContainer(Particle.FIREWORK).count(40).spread(1.2, 0.4).speed(0.08),
			ringEdge = new ParticleContainer(Particle.END_ROD).count(1).spread(0, 0).speed(0),
			ringFill = new ParticleContainer(Particle.FIREWORK).count(1).spread(0.1, 0).speed(0.01);
	private static final Circle circ = new Circle(aoe.range);
	private int numStatuses;
	private static final ParticleAnimation beamAnim, spiralAnim;
	
	static {
		// Beam head streaks downward from high above onto the target over the cast delay
		beamAnim = new ParticleAnimation(beamPart, (loc, tick) -> {
			LinkedList<Location> locs = new LinkedList<Location>();
			double headY = 8.0 * (9 - tick) / 9.0;
			for (double y = headY; y <= headY + 2.5; y += 0.4) {
				locs.add(loc.clone().add(0, y, 0));
			}
			return locs;
		}, 10);
		
		// Double helix of sparks wrapping the descending beam
		spiralAnim = new ParticleAnimation(spiralPart, (loc, tick) -> {
			LinkedList<Location> locs = new LinkedList<Location>();
			double headY = 8.0 * (9 - tick) / 9.0;
			double radius = 0.7;
			for (double y = headY; y <= headY + 2.5; y += 0.5) {
				double angle = Math.toRadians(tick * 72) + y * 1.4;
				locs.add(loc.clone().add(Math.cos(angle) * radius, y, Math.sin(angle) * radius));
				locs.add(loc.clone().add(Math.cos(angle + Math.PI) * radius, y, Math.sin(angle + Math.PI) * radius));
			}
			return locs;
		}, 10);
	}

	public BeamStaff(boolean isUpgraded) {
		super(
				ID , "Beam Staff", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(3, 0, 70, 0.8, DamageType.LIGHT, Sound.ENTITY_PLAYER_ATTACK_SWEEP)
				.add(PropertyType.RANGE, tp.range).add(PropertyType.AREA_OF_EFFECT, aoe.range)
		);
		numStatuses = isUpgraded ? 4 : 2;
		canDrop = false;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			Player p = data.getPlayer();
			Block block = p.getTargetBlockExact((int) properties.get(PropertyType.RANGE));
			if (block == null) {
				return TriggerResult.keep();
			}
			Location loc = block.getLocation().add(0.5, 1, 0.5);
			weaponSwing(p, data);
			beamAnim.play(p, loc);
			spiralAnim.play(p, loc);
			// Telegraph the impact area on the ground while the beam descends
			data.addTask(new BukkitRunnable() {
				private int count = 0;
				public void run() {
					circ.play(ringEdge, loc, LocalAxes.xz(), null);
					if (++count >= 5) cancel();
				}
			}.runTaskTimer(NeoRogue.inst(), 0, 2));
			data.addTask(new BukkitRunnable() {
				public void run() {
					Sounds.explode.play(p, loc);
					burst.play(p, loc);
					circ.play(ringEdge, loc, LocalAxes.xz(), ringFill);
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, aoe)) {
						weaponDamage(p, data, ent);
						List<StatusType> pool = new ArrayList<>(Arrays.asList(STATUS_POOL));
						Collections.shuffle(pool);
						for (int i = 0; i < numStatuses; i++) {
							FightInstance.applyStatus(ent, pool.get(i), data, 1, -1, BeamStaff.this);
						}
					}
				}
			}.runTaskLater(NeoRogue.inst(), 10));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.END_ROD, "Fires a beam down onto the block you aim at after a brief delay, dealing " + 
		"damage to all enemies in a small radius. Applies " +
		DescUtil.yellow(numStatuses + "") + " random stacks of " + GlossaryTag.BURN.tag(this) + ", " +
		GlossaryTag.INSANITY.tag(this) + ", " + GlossaryTag.CONCUSSED.tag(this) + ", or " +
		GlossaryTag.FROST.tag(this) + ".");
	}
}
