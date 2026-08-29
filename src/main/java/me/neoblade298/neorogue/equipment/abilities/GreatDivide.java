package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashSet;
import java.util.UUID;

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

public class GreatDivide extends Equipment {
	private static final String ID = "GreatDivide";
	private static final TargetProperties IMPACT = TargetProperties.radius(2, false, TargetType.ENEMY);
	private static final Circle IMPACT_CIRCLE = new Circle(IMPACT.range);
	private static final ParticleContainer AREA_EDGE = new ParticleContainer(Particle.BLOCK)
			.count(1).spread(0, 0).speed(0).blockData(Material.PACKED_MUD.createBlockData());
	private static final ParticleContainer AREA_FILL = new ParticleContainer(Particle.DUST_PLUME)
			.count(1).spread(0.1, 0).speed(0);
	private static final ParticleContainer FISSURE = new ParticleContainer(Particle.BLOCK)
			.count(1).spread(0.03, 0.03).speed(0).blockData(Material.DEEPSLATE.createBlockData());
	private static final ParticleContainer DEBRIS = new ParticleContainer(Particle.BLOCK)
			.count(8).spread(0.1, 0.08).speed(0.01).blockData(Material.DIRT.createBlockData());
	private static final ParticleContainer CAST_DUST = new ParticleContainer(Particle.DUST_PLUME)
			.count(12).spread(0.1, 0.08).speed(0.01).offsetY(0.1);
	private static final SoundContainer CAST_SOUND = new SoundContainer(Sound.BLOCK_DEEPSLATE_BREAK, 0.75F, 0.65F);
	private static final SoundContainer TRAVEL_SOUND = new SoundContainer(Sound.BLOCK_ROOTED_DIRT_BREAK, 0.35F, 0.75F);
	private int chargeTicks, range, damage, concussed;
	private double knockback;

	public GreatDivide(boolean isUpgraded) {
		super(ID, "The Great Divide", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(35, 0, 8, 8, IMPACT.range));
		chargeTicks = 20;
		range = 8;
		damage = isUpgraded ? 200 : 150;
		concussed = isUpgraded ? 5 : 3;
		knockback = 1;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			CAST_DUST.play(p, p.getLocation());
			CAST_SOUND.play(p, p);
			data.charge(chargeTicks).then(() -> launch(data, slot));
			return TriggerResult.keep();
		}));
	}

	private void launch(PlayerFightData data, int slot) {
		Player p = data.getPlayer();
		Location location = p.getLocation().clone();
		Vector direction = p.getEyeLocation().getDirection().setY(0).normalize();
		HashSet<UUID> hit = new HashSet<>();
		CAST_SOUND.play(p, p);
		data.addTask(new BukkitRunnable() {
			private int travelled;
			private Location previous = location.clone();

			@Override
			public void run() {
				if (travelled++ >= range) {
					cancel();
					return;
				}
				location.add(direction);
				Player current = data.getPlayer();
				Location ground = location.clone().add(0, 0.08, 0);
				ParticleUtil.drawLine(current, FISSURE, previous.clone().add(0, 0.08, 0), ground, 0.2);
				IMPACT_CIRCLE.play(AREA_EDGE, ground, LocalAxes.xz(), AREA_FILL);
				DEBRIS.play(current, ground);
				TRAVEL_SOUND.play(current, ground);
				previous = location.clone();
				for (LivingEntity target : TargetHelper.getEntitiesInRadius(current, location, IMPACT)) {
					if (!hit.add(target.getUniqueId())) continue;
					FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.EARTHEN,
							DamageStatTracker.of(id + slot, GreatDivide.this)), target);
					FightInstance.applyStatus(target, StatusType.CONCUSSED, data, concussed, -1, GreatDivide.this);
					FightInstance.knockback(location, target, knockback);
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, 2L));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.MUD_BRICKS,
				DescUtil.charge(this, 0, 1) + ", then launch a ground-hugging projectile that deals "
				+ GlossaryTag.EARTHEN.tag(this, damage) + " damage in a " + DescUtil.val((int) IMPACT.range)
				+ " block area, applies " + GlossaryTag.CONCUSSED.tag(this, concussed)
				+ ", and knocks enemies back. Each enemy can only be hit once.");
	}
}
