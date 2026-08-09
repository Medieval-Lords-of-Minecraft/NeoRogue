package me.neoblade298.neorogue.equipment.offhands;

import java.util.LinkedList;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.RightClickHitEvent;

public class GoliathGauntlet extends Equipment {
	private static final String ID = "GoliathGauntlet";
	private static final int BASE_DAMAGE = 200, DAMAGE_PER_CONCUSSED = 3, SHIELDS_PER_CONCUSSED = 2;
	private static final int SHIELD_DURATION = 5;
	private static final Circle SHIELD_RING = new Circle(1.3);
	private static final ParticleContainer IMPACT_PARTICLE = new ParticleContainer(Particle.DUST_PLUME)
			.count(1).spread(0.08, 0.08).speed(0.01);
	private static final ParticleContainer IMPACT_DEBRIS = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.DEEPSLATE.createBlockData()).count(12).spread(0.1, 0.1).speed(0.01).offsetY(0.8);
	private static final ParticleContainer SHIELD_PARTICLE = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(235, 205, 105), 1.2F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer SHIELD_GLOW = new ParticleContainer(Particle.FIREWORK)
			.count(8).spread(0.1, 0.1).speed(0.01).offsetY(1);
	private static final ParticleAnimation IMPACT_ANIMATION;

	static {
		IMPACT_ANIMATION = new ParticleAnimation(IMPACT_PARTICLE, (loc, tick) -> {
			LinkedList<Location> locations = new LinkedList<Location>();
			double radius = 0.35 + tick * 0.3;
			for (int i = 0; i < 10; i++) {
				double angle = Math.toRadians(i * 36 + tick * 18);
				locations.add(loc.clone().add(Math.cos(angle) * radius, 0.25 + tick * 0.35,
						Math.sin(angle) * radius));
			}
			return locations;
		}, 3);
	}

	public GoliathGauntlet(boolean isUpgraded) {
		super(ID, "Goliath Gauntlet", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.OFFHAND, EquipmentProperties.custom(10, 0, 6, 0, BASE_DAMAGE, 0, 1,
						DamageType.BLUNT, new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_STRONG)));
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addSlotBasedTrigger(id, slot, Trigger.RIGHT_CLICK_HIT,
				new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			RightClickHitEvent ev = (RightClickHitEvent) in;
			if (ev.getTarget() instanceof Player) return TriggerResult.keep();
			Player p = data.getPlayer();
			int applied = data.getStats().getStatusesApplied().getOrDefault(StatusType.CONCUSSED, 0);
			int totalDamage = BASE_DAMAGE + applied * DAMAGE_PER_CONCUSSED;
			p.swingOffHand();
			Location impactLocation = ev.getTarget().getLocation().clone();
			IMPACT_DEBRIS.play(p, impactLocation);
			data.runAnimation(id + "-impact", p, IMPACT_ANIMATION, impactLocation);
			Sounds.explode.play(p, impactLocation);
			FightInstance.dealDamage(new DamageMeta(data, totalDamage, DamageType.BLUNT,
					DamageStatTracker.of(id + slot, this)).setKnockback(properties.get(PropertyType.KNOCKBACK)),
					ev.getTarget());
			if (applied > 0) {
				data.addSimpleShield(p.getUniqueId(), applied * SHIELDS_PER_CONCUSSED,
						SHIELD_DURATION * 20, this);
				SHIELD_RING.play(SHIELD_PARTICLE, p.getLocation().add(0, 0.15, 0), LocalAxes.xz(), null);
				SHIELD_GLOW.play(p, p);
				Sounds.block.play(p, p);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_HORSE_ARMOR, "Right click an enemy to knock them back and deal "
				+ DescUtil.white(BASE_DAMAGE) + " + " + DescUtil.white(DAMAGE_PER_CONCUSSED) + " per "
				+ GlossaryTag.CONCUSSED.tag(this) + " applied this fight. Gain "
				+ GlossaryTag.SHIELDS.tag(this, SHIELDS_PER_CONCUSSED) + " " + DescUtil.duration(SHIELD_DURATION)
				+ " per stack applied.");
	}
}