package me.neoblade298.neorogue.equipment.abilities;

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
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.CastType;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;

public class CrateringEarth extends Equipment {
	private static final String ID = "CrateringEarth";
	private static final TargetProperties AREA = TargetProperties.radius(5, false, TargetType.ENEMY);
	private static final Circle AREA_CIRCLE = new Circle(AREA.range);
	private static final Circle CAST_RING = new Circle(1.2), INNER_PULSE = new Circle(2.5);
	private static final ParticleContainer AREA_EDGE = new ParticleContainer(Particle.BLOCK)
			.count(1).spread(0, 0).speed(0).blockData(Material.PACKED_MUD.createBlockData());
	private static final ParticleContainer AREA_FILL = new ParticleContainer(Particle.DUST_PLUME)
			.count(1).spread(0.1, 0).speed(0);
	private static final ParticleContainer CRATER_BURST = new ParticleContainer(Particle.BLOCK)
			.count(24).spread(0.1, 0.1).speed(0.08).blockData(Material.DEEPSLATE.createBlockData());
	private static final ParticleContainer CAST_EDGE = new ParticleContainer(Particle.DUST_PLUME)
			.count(1).spread(0, 0).speed(0);
	private static final SoundContainer CHARGE_SOUND = new SoundContainer(Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.65F, 0.7F);
	private static final SoundContainer IMPACT_SOUND = new SoundContainer(Sound.ENTITY_WARDEN_ATTACK_IMPACT, 0.8F, 0.75F);
	private static final SoundContainer PULSE_SOUND = new SoundContainer(Sound.BLOCK_DEEPSLATE_HIT, 0.3F, 0.65F);
	private int chargeTicks, damage, damagePerConcussed, pulses;

	public CrateringEarth(boolean isUpgraded) {
		super(ID, "Cratering Earth", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(30, 0, 14, 14, AREA.range));
		chargeTicks = 20;
		damage = 100;
		damagePerConcussed = 5;
		pulses = 5;
		properties.setCastType(CastType.POST_TRIGGER);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		EquipmentInstance inst = new EquipmentInstance(data, sessionEq, slot, es);
		inst.setAction((pdata, in) -> {
			Player caster = data.getPlayer();
			CAST_RING.play(CAST_EDGE, caster.getLocation().clone().add(0, 0.08, 0), LocalAxes.xz(), null);
			CHARGE_SOUND.play(caster, caster);
			data.charge(chargeTicks).then(() -> {
				Player p = data.getPlayer();
				Block block = p.getTargetBlockExact((int) properties.get(PropertyType.RANGE));
				CastUsableEvent last = inst.getLastCastEvent();
				if (block == null || !block.getType().isSolid()) {
					data.addMana(last.getManaCost());
					data.addStamina(last.getStaminaCost());
					inst.setCooldown(0);
					Sounds.error.play(p, p);
					return;
				}
				data.runActions(data, Trigger.CAST_USABLE, new CastUsableEvent(inst, CastType.POST_TRIGGER,
						last.getManaCost(), last.getStaminaCost(), last.getCooldown(), in, last.getTags()));
				Location center = block.getLocation().add(0.5, 1, 0.5);
				strike(data, slot, center, damage);
				data.addTask(new BukkitRunnable() {
					private int count;

					@Override
					public void run() {
						if (count++ >= pulses) {
							cancel();
							return;
						}
						Player current = data.getPlayer();
						Location pulseCenter = center.clone().add(0, 0.08, 0);
						INNER_PULSE.play(AREA_EDGE, pulseCenter, LocalAxes.xz(), null);
						AREA_CIRCLE.play(AREA_EDGE, pulseCenter, LocalAxes.xz(), AREA_FILL);
						PULSE_SOUND.play(current, center);
						for (LivingEntity target : TargetHelper.getEntitiesInRadius(current, center, AREA)) {
							FightData fd = FightInstance.getFightData(target);
							if (fd == null || !fd.hasStatus(StatusType.CONCUSSED)) continue;
							int stacks = fd.getStatus(StatusType.CONCUSSED).getStacks();
							FightInstance.dealDamage(new DamageMeta(data, damagePerConcussed * stacks, DamageType.EARTHEN,
									DamageStatTracker.of(id + slot, CrateringEarth.this)), target);
						}
					}
				}.runTaskTimer(NeoRogue.inst(), 20L, 20L));
			});
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
	}

	private void strike(PlayerFightData data, int slot, Location center, int amount) {
		Player p = data.getPlayer();
		Location ground = center.clone().add(0, 0.08, 0);
		AREA_CIRCLE.play(AREA_EDGE, ground, LocalAxes.xz(), AREA_FILL);
		CRATER_BURST.play(p, center);
		IMPACT_SOUND.play(p, center);
		for (LivingEntity target : TargetHelper.getEntitiesInRadius(p, center, AREA)) {
			FightInstance.dealDamage(new DamageMeta(data, amount, DamageType.EARTHEN,
					DamageStatTracker.of(id + slot, this)), target);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PACKED_MUD,
				DescUtil.charge(this, 0, 1) + ", then deal " + GlossaryTag.EARTHEN.tag(this, damage)
				+ " damage in a " + DescUtil.val((int) AREA.range) + " block area at the aimed block. For "
				+ DescUtil.val(pulses + "s") + ", deal " + GlossaryTag.EARTHEN.tag(this, damagePerConcussed)
				+ " damage each second per " + GlossaryTag.CONCUSSED.tag(this) + " stack on each enemy.");
	}
}
