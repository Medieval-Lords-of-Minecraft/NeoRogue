package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
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
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
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
import me.neoblade298.neorogue.session.fight.trigger.event.KillEvent;

public class Requiem extends Equipment implements Power {
	private static final String ID = "Requiem";
	private static final int COOLDOWN_TICKS = 60, ANIMATION_TICKS = 10;
	private static final TargetProperties tp = TargetProperties.radius(4, false, TargetType.ENEMY);
	private static final Circle activationHalo = new Circle(1.4), impactCircle = new Circle(tp.range);
	private static final ParticleContainer goldEdge = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(255, 238, 150), 0.8F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer beamCore = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(255, 248, 205), 0.7F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer beamGlow =
			new ParticleContainer(Particle.END_ROD).count(1).spread(0.04, 0.04).speed(0);
	private static final ParticleContainer activationSpark =
			new ParticleContainer(Particle.END_ROD).count(12).spread(0.3, 0.6).speed(0.01);
	private static final ParticleContainer impactSpark =
			new ParticleContainer(Particle.FIREWORK).count(18).spread(0.7, 0.3).speed(0.01);
	private static final SoundContainer activationSound =
			new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.7F, 1.25F);
	private static final SoundContainer impactSound =
			new SoundContainer(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.6F, 1.2F);
	private int damage, sanctified;

	public Requiem(boolean isUpgraded) {
		super(ID, "Requiem", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		damage = isUpgraded ? 225 : 150;
		sanctified = isUpgraded ? 8 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.KILL, (pdata, in) -> {
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		playActivationFx(data);
		data.addTask(new BukkitRunnable() {
			@Override
			public void run() {
				final long[] nextUsable = { 0L };
				data.addTrigger(id + "-active", Trigger.KILL, (pdata, in) -> {
					long now = System.currentTimeMillis();
					if (now < nextUsable[0]) return TriggerResult.keep();
					nextUsable[0] = now + COOLDOWN_TICKS * 50L;

					KillEvent ev = (KillEvent) in;
					Location impact = ev.getTarget().getLocation().clone();
					animateImpact(data, impact, slot);
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));
	}

	private void animateImpact(PlayerFightData data, Location deathLocation, int slot) {
		Location impact = deathLocation.clone().add(0, 0.05, 0);
		Location sky = impact.clone().add(0, 10, 0);
		impactCircle.play(goldEdge, impact, LocalAxes.xz(), null);
		activationSound.play(data.getPlayer(), impact);

		data.addTask(new BukkitRunnable() {
			private int ticks;

			@Override
			public void run() {
				ticks++;
				Player player = data.getPlayer();
				Location beamEnd = sky.clone().add(0, -10.0 * ticks / ANIMATION_TICKS, 0);
				ParticleUtil.drawLine(player, beamCore, sky, beamEnd, 0.35);
				ParticleUtil.drawLine(player, beamGlow, sky, beamEnd, 0.55);
				if (ticks < ANIMATION_TICKS) return;

				playImpactFx(data, impact);
				for (LivingEntity target : TargetHelper.getEntitiesInRadius(player, impact, tp)) {
					FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.LIGHT,
							DamageStatTracker.of(id + slot, Requiem.this)), target);
					FightInstance.applyStatus(target, StatusType.SANCTIFIED, data, sanctified, -1, Requiem.this);
				}
				cancel();
			}
		}.runTaskTimer(NeoRogue.inst(), 1L, 1L));
	}

	private void playActivationFx(PlayerFightData data) {
		Player player = data.getPlayer();
		Location location = player.getLocation().clone().add(0, 0.1, 0);
		activationHalo.play(goldEdge, location, LocalAxes.xz(), null);
		activationSpark.play(player, location.clone().add(0, 1, 0));
		activationSound.play(player, player);
	}

	private void playImpactFx(PlayerFightData data, Location impact) {
		Player player = data.getPlayer();
		impactCircle.play(goldEdge, impact, LocalAxes.xz(), null);
		impactSpark.play(player, impact.clone().add(0, 0.35, 0));
		impactSound.play(player, impact);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_APPLE,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after killing an enemy. After activation, killing an enemy deals "
				+ GlossaryTag.LIGHT.tag(this, damage) + " damage and applies "
				+ GlossaryTag.SANCTIFIED.tag(this, sanctified) + " to enemies within "
				+ tp.range + " blocks of it after " + DescUtil.white("0.5s") + ". "
				+ DescUtil.white("3s") + " cooldown.");
	}
}