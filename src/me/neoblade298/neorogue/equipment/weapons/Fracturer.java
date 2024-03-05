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
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Fracturer extends Equipment {
	private static final int DISTANCE = 4, RADIUS = 2;
	private static final TargetProperties props = TargetProperties.radius(RADIUS, true, TargetType.ENEMY),
			line = TargetProperties.line(5, 2, TargetType.ENEMY);
	private static final ParticleContainer swingPart = new ParticleContainer(Particle.CLOUD).count(5).spread(0.1, 0.1),
			edge = new ParticleContainer(Particle.CLOUD).count(1).spread(0, 0),
			fill = new ParticleContainer(Particle.CLOUD).count(1).spread(0.1, 0),
			linePart = new ParticleContainer(Particle.BLOCK_CRACK).count(5).spread(0.1, 0.1).blockData(Material.DIRT.createBlockData());
	private static final Circle hitShape = new Circle(RADIUS);
	private static final ParticleAnimation swing;
	private int earth, concussed;
	
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
	
	public Fracturer(boolean isUpgraded) {
		super("fracturer", "Fracturer", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 130 : 100, 0.5, DamageType.BLUNT, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		properties.addUpgrades(PropertyType.DAMAGE);
		earth = isUpgraded ? 60 : 40;
		concussed = isUpgraded ? 18 : 12;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, new FracturerInstance(id));
	}
	
	
	private class FracturerInstance extends PriorityAction {
		private int count = 0;
		public FracturerInstance(String id) {
			super(id);
			action = (data, in) -> {
				Player p = data.getPlayer();
				weaponSwing(p, data);
				data.runAnimation(id, p, swing, p);
				data.addTask(id, new BukkitRunnable() {
					public void run() {
						hitArea(p, data);
					}
				}.runTaskLater(NeoRogue.inst(), 10L));
				return TriggerResult.keep();
			};
		}
		
		private void hitArea(Player p, PlayerFightData data) {
			Location hit = p.getLocation().add(p.getLocation().getDirection().setY(0).normalize().multiply(DISTANCE));
			Sounds.explode.play(p, hit);
			hitShape.play(Fracturer.edge, hit, LocalAxes.xz(), Fracturer.fill);
			LinkedList<LivingEntity> enemies = TargetHelper.getEntitiesInRadius(p, hit, props);
			if (!enemies.isEmpty()) {
				boolean first = true;
				for (LivingEntity ent : enemies) {
					if (first) {
						weaponDamage(p, data, ent);
						Vector v = ent.getVelocity();
						ent.setVelocity(v.setY(v.getY() + 0.5));
						first = false;
					}
					else {
						DamageMeta dm = new DamageMeta(data, properties.get(PropertyType.DAMAGE), properties.getType());
						FightInstance.dealDamage(dm, ent);
					}
				}
			}
			if (++count == 3) {
				count = 0;
				// Test to see if this works between blocks
				Location start = p.getLocation().add(0, 0.5, 0);
				ParticleUtil.drawLine(p, linePart, start, hit, 0.25);
				LinkedList<LivingEntity> lineHit =  TargetHelper.getEntitiesInLine(start, hit, line);
				FightInstance.dealDamage(new DamageMeta(data, earth, DamageType.EARTHEN), lineHit);
				for (LivingEntity ent : lineHit) {
					FightInstance.getFightData(ent.getUniqueId()).applyStatus(StatusType.CONCUSSED, p.getUniqueId(), concussed, -1);
				}
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_SHOVEL, "Deals damage to enemies in a small circle in front of you. Only the closest enemy"
				+ " to the center of the circle is affected by on-hit effects. Every third hit, deal " + GlossaryTag.EARTHEN.tag(this, earth, true) +
				" to all enemies between you and the center of the circle and apply " + GlossaryTag.CONCUSSED.tag(this, concussed, true)
				+ ".");
	}
}
