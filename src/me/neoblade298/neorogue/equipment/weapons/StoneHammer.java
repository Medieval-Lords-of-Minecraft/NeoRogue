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

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.abilities.BasicInfusionMastery;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class StoneHammer extends Equipment {
	private static final String ID = "stoneHammer";
	private static final int DISTANCE = 4, RADIUS = 2;
	private static final TargetProperties props = TargetProperties.radius(RADIUS, true, TargetType.ENEMY);
	private static final ParticleContainer swingPart = new ParticleContainer(Particle.CLOUD).count(5).spread(0.1, 0.1),
			edge = new ParticleContainer(Particle.CLOUD).count(1).spread(0, 0),
			fill = new ParticleContainer(Particle.CLOUD).count(1).spread(0.1, 0);
	private static final Circle hitShape = new Circle(RADIUS);
	public static final ParticleAnimation swing;
	
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
		super(ID, "Stone Hammer", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 100 : 70, 0.5, DamageType.BLUNT, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		properties.addUpgrades(PropertyType.DAMAGE);
	}

	@Override
	public void setupReforges() {
		addReforge(BasicInfusionMastery.get(), RighteousHammer.get(), Fracturer.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (pdata, inputs) -> {
			if (!data.canBasicAttack()) return TriggerResult.keep();
			weaponSwing(p, data);
			data.runAnimation(id, p, swing, p);
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
		Sounds.explode.play(p, hit);
		hitShape.play(StoneHammer.edge, hit, LocalAxes.xz(), StoneHammer.fill);
		LinkedList<LivingEntity> enemies = TargetHelper.getEntitiesInRadius(p, hit, props);
		if (enemies.isEmpty()) return;
		boolean first = true;
		Vector v = new Vector(0, 0.5, 0);
		for (LivingEntity ent : enemies) {
			if (first) {
				weaponDamage(p, data, ent);
				first = false;
			}
			else {
				DamageMeta dm = new DamageMeta(data, properties.get(PropertyType.DAMAGE), properties.getType());
				FightInstance.dealDamage(dm, ent);
			}
			FightInstance.knockback(ent, v);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SHOVEL, "Deals damage to enemies in a small circle in front of you. Only the closest enemy"
				+ " to the center of the circle is affected by on-hit effects.");
	}
}
