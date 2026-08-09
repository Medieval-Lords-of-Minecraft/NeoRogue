package me.neoblade298.neorogue.equipment.accessories;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleShapeMemory;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SeismicShard extends Equipment {
	private static final String ID = "SeismicShard";
	private static final int RADIUS = 5;
	private static final TargetProperties AOE = TargetProperties.radius(RADIUS, true, TargetType.ENEMY);
	private static final Circle INNER_PULSE = new Circle(1.7), MID_PULSE = new Circle(3.4), AREA = new Circle(RADIUS);
	private static final ParticleContainer PULSE = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.DEEPSLATE.createBlockData()).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer AREA_FILL = new ParticleContainer(Particle.DUST_PLUME)
			.count(1).spread(0.1, 0).speed(0);
	private static final ParticleContainer CAST_BURST = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.STONE.createBlockData()).count(12).spread(0.1, 0.1).speed(0.01).offsetY(0.2);
	private static final SoundContainer CAST_SOUND = new SoundContainer(Sound.ENTITY_WARDEN_SONIC_BOOM, 0.7F, 0.6F);
	private static final ParticleAnimation PULSE_ANIMATION;

	static {
		PULSE_ANIMATION = new ParticleAnimation(PULSE, (loc, tick) -> {
			Circle circle = tick == 0 ? INNER_PULSE : tick == 1 ? MID_PULSE : AREA;
			ParticleShapeMemory memory = circle.calculate(loc, LocalAxes.xz());
			return new LinkedList<Location>(memory.getEdges());
		}, 3);
	}
	private int damage;

	public SeismicShard(boolean isUpgraded) {
		super(ID, "Seismic Shard", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR, EquipmentType.ACCESSORY);
		damage = isUpgraded ? 120 : 80;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			Player p = data.getPlayer();
			Location center = p.getLocation().clone();
			AREA.play(PULSE, center, LocalAxes.xz(), AREA_FILL);
			CAST_BURST.play(p, center);
			CAST_SOUND.play(p, center);
			data.runAnimation(id + "-pulse", p, PULSE_ANIMATION, center);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, AOE)) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.EARTHEN,
						DamageStatTracker.of(id + slot, this)), ent);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_SHARD, "Casting an ability deals "
				+ GlossaryTag.EARTHEN.tag(this, damage) + " damage to enemies within " + RADIUS + " blocks.");
	}
}