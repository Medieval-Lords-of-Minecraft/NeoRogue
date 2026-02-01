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

public class RighteousHammer extends Equipment {
	private static final String ID = "RighteousHammer";
	private static final int DISTANCE = 4, RADIUS = 2;
	private static final TargetProperties props = TargetProperties.radius(RADIUS, true, TargetType.ENEMY);
	private static final ParticleContainer swingPart = new ParticleContainer(Particle.CLOUD).count(5).spread(0.1, 0.1),
			edge = new ParticleContainer(Particle.CLOUD).count(1).spread(0, 0),
			fill = new ParticleContainer(Particle.CLOUD).count(1).spread(0.1, 0);
	private static final Circle hitShape = new Circle(RADIUS);
	private static final ParticleAnimation swing;
	
	private int sanct;
	
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
	
	public RighteousHammer(boolean isUpgraded) {
		super(ID, "Righteous Hammer", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(110, 0.5, DamageType.BLUNT, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		sanct = isUpgraded ? 18 : 12;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (pdata, inputs) -> {
			Player p = data.getPlayer();
			weaponSwing(p, data);
			data.runAnimation(id, p, swing, p);
			data.addTask(new BukkitRunnable() {
				public void run() {
					hitArea(p, data, slot);
				}
			}.runTaskLater(NeoRogue.inst(), 10L));
			return TriggerResult.keep();
		});
	}
	
	private void hitArea(Player p, PlayerFightData data, int slot) {
		Location hit = p.getLocation().add(p.getLocation().getDirection().setY(0).normalize().multiply(DISTANCE));
		Sounds.explode.play(p, hit);
		hitShape.play(RighteousHammer.edge, hit, LocalAxes.xz(), RighteousHammer.fill);
		LinkedList<LivingEntity> enemies = TargetHelper.getEntitiesInRadius(p, hit, props);
		if (enemies.isEmpty()) return;
		boolean first = true;
		for (LivingEntity ent : enemies) {
			FightInstance.applyStatus(ent, StatusType.SANCTIFIED, p, sanct, -1);
			if (first) {
				weaponDamage(p, data, ent);
				Vector v = ent.getVelocity();
				FightInstance.knockback(ent, v.setY(v.getY() + 0.5));
				first = false;
			}
			else {
				DamageMeta dm = new DamageMeta(data, properties.get(PropertyType.DAMAGE), properties.getType(), DamageStatTracker.of(id + slot, this));
				FightInstance.dealDamage(dm, ent);
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_SHOVEL, "Deals damage to enemies in a small circle in front of you. Only the closest enemy"
				+ " to the center of the circle is affected by on-hit effects. Apply " + GlossaryTag.SANCTIFIED.tag(this, sanct, true) + " to all enemies hit.");
	}
}
