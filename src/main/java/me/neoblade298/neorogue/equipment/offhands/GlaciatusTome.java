package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class GlaciatusTome extends Equipment {
	private static final String ID = "GlaciatusTome";
	private static final int RANGE = 12, SLOWNESS_LEVEL = 2, SLOWNESS_DURATION = 2;
	private static final int FROST_PER_SHIELD = 3, SHIELD_DURATION = 6;
	private static final double PROJECTILE_SPEED = 0.6, PROJECTILE_HEIGHT = 0.35;
	private static final ParticleContainer CAST = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.PACKED_ICE.createBlockData()).count(5).spread(0.1, 0.05).speed(0.01);
	private static final ParticleContainer TRAIL = new ParticleContainer(Particle.SNOWFLAKE).count(2)
			.spread(0.05, 0.05).speed(0);
	private static final ParticleContainer HIT = new ParticleContainer(Particle.SNOWFLAKE).count(8)
			.spread(0.1, 0.1).offsetY(0.8).speed(0.01);
	private static final ParticleContainer SHIELD_GAIN = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(170, 235, 255), 1F)).count(6)
			.spread(0.3, 0.45).offsetY(1).speed(0.01);
	private int frost;

	public GlaciatusTome(boolean isUpgraded) {
		super(ID, "Glaciatus Tome", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.OFFHAND, EquipmentProperties.none());
		frost = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.LEFT_CLICK, (pdata, in) -> {
			Player player = data.getPlayer();
			Vector direction = player.getLocation().getDirection().setY(0);
			if (direction.lengthSquared() == 0) return TriggerResult.keep();
			new ProjectileGroup(new GlaciatusProjectile(data)).start(data,
					player.getLocation().add(0, PROJECTILE_HEIGHT, 0), direction.normalize());
			CAST.play(player, player.getLocation().add(0, PROJECTILE_HEIGHT, 0));
			Sounds.wind.play(player, player);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOOK, "On left click, fire a ground-hugging projectile. Enemies hit receive "
				+ GlossaryTag.FROST.tag(this, frost) + " and "
				+ DescUtil.potion("Slowness", SLOWNESS_LEVEL, SLOWNESS_DURATION) + ". For every "
				+ GlossaryTag.FROST.tag(this, FROST_PER_SHIELD) + " on each enemy after the hit, gain "
				+ GlossaryTag.SHIELDS.tag(this, 1) + " [" + DescUtil.white(SHIELD_DURATION + "s") + "].");
	}

	private class GlaciatusProjectile extends Projectile {
		private final PlayerFightData data;

		private GlaciatusProjectile(PlayerFightData data) {
			super(PROJECTILE_SPEED, RANGE, 1);
			gravity(0);
			pierce(-1);
			this.data = data;
		}

		@Override
		public void onTick(ProjectileInstance projectile, int interpolation) {
			TRAIL.play(data.getPlayer(), projectile.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance projectile) {
			Player player = data.getPlayer();
			HIT.play(player, hit.getEntity());
			Sounds.glass.play(player, hit.getEntity());
			FightInstance.applyStatus(hit.getEntity(), StatusType.FROST, data, frost, -1, GlaciatusTome.this);
			hit.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
					SLOWNESS_DURATION * 20, SLOWNESS_LEVEL - 1));
			int shields = hit.getStatus(StatusType.FROST).getStacks() / FROST_PER_SHIELD;
			if (shields > 0) {
				data.addSimpleShield(player.getUniqueId(), shields, SHIELD_DURATION * 20, GlaciatusTome.this);
				SHIELD_GAIN.play(player, player);
			}
		}
	}
}