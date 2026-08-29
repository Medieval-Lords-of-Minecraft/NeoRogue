package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class EventHorizon extends Equipment {
	private static final String ID = "EventHorizon";
	private static final TargetProperties LINE = TargetProperties.line(64, 1.5, TargetType.ENEMY);
	private static final Circle RIFT_RING = new Circle(1.25);
	private static final ParticleContainer RIFT_EDGE = new ParticleContainer(Particle.REVERSE_PORTAL)
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer RIFT_CORE = new ParticleContainer(Particle.SOUL)
			.count(1).spread(0.1, 0).speed(0);
	private static final ParticleContainer LINE_PARTICLES = new ParticleContainer(Particle.REVERSE_PORTAL)
			.count(1).spread(0.04, 0.04).speed(0);
	private static final ParticleContainer LINE_CORE = new ParticleContainer(Particle.SOUL)
			.count(1).spread(0.02, 0.02).speed(0);
	private static final ParticleContainer DETONATION = new ParticleContainer(Particle.REVERSE_PORTAL)
			.count(18).spread(0.1, 0.1).speed(0.01).offsetY(0.5);
	private static final SoundContainer RIFT_OPEN = new SoundContainer(Sound.BLOCK_PORTAL_TRIGGER, 0.55F, 1.35F);
	private static final SoundContainer LINK_SOUND = new SoundContainer(Sound.ENTITY_WARDEN_SONIC_BOOM, 0.65F, 1.65F);
	private int delayTicks, riftDurationTicks, damage;

	public EventHorizon(boolean isUpgraded) {
		super(ID, "Event Horizon", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(35, 5, 10, 0));
		delayTicks = 40;
		riftDurationTicks = 200;
		damage = isUpgraded ? 200 : 150;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Location first = p.getLocation().clone();
			data.addRift(new Rift(data, first, riftDurationTicks, this));
			RIFT_RING.play(RIFT_EDGE, first.clone().add(0, 0.08, 0), LocalAxes.xz(), RIFT_CORE);
			RIFT_OPEN.play(p, first);
			data.addTask(new BukkitRunnable() {
				@Override
				public void run() {
					Player current = data.getPlayer();
					Location second = current.getLocation().clone();
					data.addRift(new Rift(data, second, riftDurationTicks, EventHorizon.this));
					Location firstCore = first.clone().add(0, 0.35, 0);
					Location secondCore = second.clone().add(0, 0.35, 0);
					RIFT_RING.play(RIFT_EDGE, second.clone().add(0, 0.08, 0), LocalAxes.xz(), RIFT_CORE);
					ParticleUtil.drawLine(current, LINE_PARTICLES, firstCore, secondCore, 0.25);
					ParticleUtil.drawLine(current, LINE_CORE, firstCore, secondCore, 0.5);
					DETONATION.play(current, first);
					DETONATION.play(current, second);
					LINK_SOUND.play(current, second);
					for (LivingEntity target : TargetHelper.getEntitiesInLine(current, first, second, LINE)) {
						FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.DARK,
								DamageStatTracker.of(id + slot, EventHorizon.this), DamageOrigin.RIFT), target);
					}
				}
			}.runTaskLater(NeoRogue.inst(), delayTicks));
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ENDER_EYE,
				"Drop a " + GlossaryTag.RIFT.tag(this) + " where you stand, then drop a second "
				+ DescUtil.val("2s") + " later. Enemies between them take "
				+ GlossaryTag.DARK.tag(this, damage) + " " + GlossaryTag.RIFT.tag(this) + " damage.");
	}
}
