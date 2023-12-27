package me.neoblade298.neorogue.equipment.weapons;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.particles.Circle;
import me.neoblade298.neocore.bukkit.particles.LocalAxes;
import me.neoblade298.neocore.bukkit.particles.ParticleAnimation;
import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class StoneHammer extends Equipment {
	private static final int DISTANCE = 4, RADIUS = 2;
	private static final TargetProperties props = new TargetProperties(RADIUS, true, TargetType.ENEMY);
	private static final ParticleContainer swingPart = new ParticleContainer(Particle.CLOUD).count(5).spread(0.1, 0.1),
			edge = new ParticleContainer(Particle.CLOUD).count(1).spread(0, 0),
			fill = new ParticleContainer(Particle.CLOUD).count(1).spread(0.1, 0);
	private static final Circle hitShape = new Circle(RADIUS);
	private static final ParticleAnimation swing;
	
	static {
		swing = new ParticleAnimation(swingPart, (loc, tick) -> {
			LinkedList<Location> partLocs = new LinkedList<Location>();
			double rotation = 2 + Math.min(4, tick) * 8;
			if (tick >= 5) rotation += (tick - 5) * 14;
			Vector cross = loc.getDirection().setY(0).rotateAroundY(Math.toRadians(90)).normalize();
			Vector v = new Vector(0,DISTANCE,0).rotateAroundAxis(cross, Math.toRadians(rotation));
			partLocs.add(loc.add(v));
			return partLocs;
		}, 10);
	}
	
	public StoneHammer(boolean isUpgraded) {
		super("stoneHammer", "Stone Hammer", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 45 : 30, 0.5, DamageType.BLUNT, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (pdata, inputs) -> {
			swingWeapon(p, data);
			data.runAnimation(id, swing, p);
			data.addTask(id, new BukkitRunnable() {
				public void run() {
					hitArea(p, data);
				}
			}.runTaskLater(NeoRogue.inst(), 10L));
			return TriggerResult.keep();
		});
	}
	
	private void hitArea(Player p, PlayerFightData data) {
		Location hit = p.getLocation().add(p.getLocation().getDirection().setY(0).normalize().multiply(DISTANCE));
		Util.playSound(p, Sound.ENTITY_GENERIC_EXPLODE, false);
		hitShape.draw(StoneHammer.edge, hit, LocalAxes.xz(), StoneHammer.fill);
		LinkedList<LivingEntity> enemies = TargetHelper.getEntitiesInRadius(p, hit, props);
		if (enemies.isEmpty()) return;
		for (LivingEntity ent : enemies) {
			damageWithWeapon(p, data, null, ent);
			Vector v = ent.getVelocity();
			ent.setVelocity(v.setY(v.getY() + 0.5));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SHOVEL, null, "Deals damage to enemies in a small circle in front of you. Only the closest enemy"
				+ " to the center of the circle is affected by on-hit effects.");
	}
}
