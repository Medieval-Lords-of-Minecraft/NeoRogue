package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;

public class UnstableMagic extends Equipment implements Power {
	private static final String ID = "UnstableMagic";
	private static final TargetProperties TARGETS = TargetProperties.radius(16, false, TargetType.ENEMY);
	private static final ParticleContainer ICE_PARTICLES = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(125, 225, 255), 1F)).count(3).spread(0.06, 0.06).speed(0);
	private static final ParticleContainer LIGHTNING_PARTICLES = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(255, 225, 70), 1F)).count(3).spread(0.06, 0.06).speed(0);
	private static final ParticleContainer DARK_PARTICLES = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(105, 35, 155), 1F)).count(3).spread(0.06, 0.06).speed(0);
	private static final ParticleContainer ICE_ACCENT = new ParticleContainer(Particle.SNOWFLAKE)
			.count(1).spread(0.04, 0.04).speed(0);
	private static final ParticleContainer LIGHTNING_ACCENT = new ParticleContainer(Particle.FIREWORK)
			.count(1).spread(0.04, 0.04).speed(0.01);
	private static final ParticleContainer DARK_ACCENT = new ParticleContainer(Particle.REVERSE_PORTAL)
			.count(1).spread(0.04, 0.04).speed(0.01);
	private static final ParticleContainer ICE_IMPACT = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(125, 225, 255), 1.2F)).count(10).spread(0.1, 0.1).speed(0.01);
	private static final ParticleContainer LIGHTNING_IMPACT = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(255, 225, 70), 1.2F)).count(10).spread(0.1, 0.1).speed(0.01);
	private static final ParticleContainer DARK_IMPACT = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(105, 35, 155), 1.2F)).count(10).spread(0.1, 0.1).speed(0.01);
	private static final ParticleContainer ICE_IMPACT_ACCENT = new ParticleContainer(Particle.SNOWFLAKE)
			.count(6).spread(0.1, 0.1).speed(0.01);
	private static final ParticleContainer LIGHTNING_IMPACT_ACCENT = new ParticleContainer(Particle.FIREWORK)
			.count(6).spread(0.1, 0.1).speed(0.01);
	private static final ParticleContainer DARK_IMPACT_ACCENT = new ParticleContainer(Particle.REVERSE_PORTAL)
			.count(6).spread(0.1, 0.1).speed(0.01);
	private static final SoundContainer ICE_LAUNCH = new SoundContainer(Sound.BLOCK_GLASS_BREAK, 0.45F, 1.7F);
	private static final SoundContainer LIGHTNING_LAUNCH = new SoundContainer(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.45F, 1.65F);
	private static final SoundContainer DARK_LAUNCH = new SoundContainer(Sound.ENTITY_WITHER_SHOOT, 0.45F, 1.3F);
	private static final SoundContainer IMPACT_SOUND = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.4F, 1.35F);
	private int manaRequired, intervalSeconds, damage;

	public UnstableMagic(boolean isUpgraded) {
		super(ID, "Unstable Magic", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		manaRequired = 300;
		intervalSeconds = 4;
		damage = isUpgraded ? 140 : 100;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta spent = new ActionMeta();
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			CastUsableEvent ev = (CastUsableEvent) in;
			if (ev.getInstance().getEquipment().getType() != EquipmentType.ABILITY) return TriggerResult.keep();
			spent.addDouble(ev.getInstance().getManaCost());
			if (spent.getDouble() < manaRequired) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ActionMeta timer = new ActionMeta();
		data.addTrigger(id + "-active", Trigger.PLAYER_TICK, (pdata, in) -> {
			if (timer.addCount(1) < intervalSeconds) return TriggerResult.keep();
			timer.setCount(0);
			Player p = data.getPlayer();
			LivingEntity target = TargetHelper.getNearest(p, TARGETS);
			if (target == null) return TriggerResult.keep();
			int roll = (int) (Math.random() * 3);
			DamageType type = roll == 0 ? DamageType.ICE : roll == 1 ? DamageType.LIGHTNING : DamageType.DARK;
			ParticleContainer particles = type == DamageType.ICE ? ICE_PARTICLES
					: type == DamageType.LIGHTNING ? LIGHTNING_PARTICLES : DARK_PARTICLES;
			Vector direction = target.getLocation().add(0, 0.8, 0).toVector().subtract(p.getEyeLocation().toVector());
			new ProjectileGroup(new UnstableProjectile(data, slot, type, particles)).start(data, p.getEyeLocation(), direction);
			return TriggerResult.keep();
		});
	}

	private class UnstableProjectile extends Projectile {
		private final PlayerFightData data;
		private final int slot;
		private final DamageType type;
		private final ParticleContainer particles;

		private UnstableProjectile(PlayerFightData data, int slot, DamageType type, ParticleContainer particles) {
			super(1, TARGETS.range, 1);
			this.data = data;
			this.slot = slot;
			this.type = type;
			this.particles = particles;
			size(0.5, 0.5);
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			Player p = data.getPlayer();
			particles.play(p, proj.getLocation());
			accent().play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Player p = data.getPlayer();
			impact().play(p, proj.getLocation());
			impactAccent().play(p, proj.getLocation());
			IMPACT_SOUND.play(p, proj.getLocation());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Player p = data.getPlayer();
			launchSound().play(p, p);
			proj.addDamageSlice(new DamageSlice(data, damage, type, DamageStatTracker.of(id + slot, UnstableMagic.this)));
		}

		private ParticleContainer accent() {
			return type == DamageType.ICE ? ICE_ACCENT
					: type == DamageType.LIGHTNING ? LIGHTNING_ACCENT : DARK_ACCENT;
		}

		private ParticleContainer impact() {
			return type == DamageType.ICE ? ICE_IMPACT
					: type == DamageType.LIGHTNING ? LIGHTNING_IMPACT : DARK_IMPACT;
		}

		private ParticleContainer impactAccent() {
			return type == DamageType.ICE ? ICE_IMPACT_ACCENT
					: type == DamageType.LIGHTNING ? LIGHTNING_IMPACT_ACCENT : DARK_IMPACT_ACCENT;
		}

		private SoundContainer launchSound() {
			return type == DamageType.ICE ? ICE_LAUNCH
					: type == DamageType.LIGHTNING ? LIGHTNING_LAUNCH : DARK_LAUNCH;
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_SHARD,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after spending "
				+ DescUtil.val(manaRequired) + " base mana. Every " + DescUtil.val(intervalSeconds + "s")
				+ ", fire a projectile that deals " + DescUtil.val(damage) + " random "
				+ GlossaryTag.ICE.tag(this) + ", " + GlossaryTag.LIGHTNING.tag(this) + ", or "
				+ GlossaryTag.DARK.tag(this) + " damage.");
	}
}
