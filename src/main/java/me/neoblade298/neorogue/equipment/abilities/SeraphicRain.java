package me.neoblade298.neorogue.equipment.abilities;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.AmmoEquipmentInstance;
import me.neoblade298.neorogue.equipment.AmmunitionInstance;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
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
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerAttributeController;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class SeraphicRain extends Equipment {
	private static final String ID = "SeraphicRain";
	private static final int PROJECTILES = 8;
	private static final int DURATION = 60;
	private static final double GRAVITY = 0.005;
	private static final TargetProperties TARGETS = TargetProperties.radius(12, false, TargetType.ENEMY);
	private static final ParticleContainer GOLD = new ParticleContainer(Particle.DUST)
			.dustOptions(new Particle.DustOptions(Color.fromRGB(255, 196, 64), 1.15F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer WHITE = new ParticleContainer(Particle.END_ROD)
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer SKY = new ParticleContainer(Particle.DUST)
			.dustOptions(new Particle.DustOptions(Color.fromRGB(110, 205, 255), 0.9F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer LAUNCH_BURST = new ParticleContainer(Particle.FIREWORK)
			.count(18).spread(0.1, 0.1).speed(0.01);
	private static final ParticleContainer HIT_BURST = new ParticleContainer(Particle.FIREWORK)
			.count(10).spread(0.1, 0.1).speed(0.01);
	private static final ParticleAnimation LAUNCH_GOLD, LAUNCH_SKY, WINGS_GOLD, WINGS_WHITE, HIT_GOLD;

	static {
		LAUNCH_GOLD = new ParticleAnimation(GOLD, (loc, tick) -> halo(loc, 0.45 + tick * 0.16, 0.15 + tick * 0.12), 10);
		LAUNCH_SKY = new ParticleAnimation(SKY, (loc, tick) -> halo(loc, 1.8 - tick * 0.12, 1.7 + tick * 0.08), 10);
		WINGS_GOLD = new ParticleAnimation(GOLD, (loc, tick) -> wings(loc, tick, false), DURATION);
		WINGS_WHITE = new ParticleAnimation(WHITE, (loc, tick) -> wings(loc, tick, true), DURATION);
		HIT_GOLD = new ParticleAnimation(GOLD, (loc, tick) -> halo(loc, 0.2 + tick * 0.16, 0.1), 5);
	}

	private int requiredHits;

	private static LinkedList<Location> halo(Location loc, double radius, double height) {
		LinkedList<Location> locations = new LinkedList<>();
		for (int point = 0; point < 16; point++) {
			double angle = Math.PI * 2 * point / 16;
			locations.add(loc.clone().add(Math.cos(angle) * radius, height, Math.sin(angle) * radius));
		}
		return locations;
	}

	private static LinkedList<Location> wings(Location loc, int tick, boolean highlights) {
		LinkedList<Location> locations = new LinkedList<>();
		Vector forward = loc.getDirection().setY(0);
		if (forward.lengthSquared() < 0.001) forward.setZ(1);
		forward.normalize();
		Vector side = forward.clone().crossProduct(new Vector(0, 1, 0)).normalize();
		double flap = Math.sin(tick * 0.38) * 0.18;
		Location center = loc.clone().add(0, 1.15, 0).subtract(forward.clone().multiply(0.15));
		int start = highlights ? 1 : 0;
		for (int wingSide : new int[] {-1, 1}) {
			for (int feather = start; feather < 7; feather += highlights ? 2 : 1) {
				double reach = 0.3 + feather * 0.28;
				Vector offset = side.clone().multiply(wingSide * reach)
						.subtract(forward.clone().multiply(0.08 + feather * 0.16));
				offset.setY(0.38 - feather * 0.07 + flap * (feather / 6.0));
				locations.add(center.clone().add(offset));
				if (!highlights && feather > 1) locations.add(center.clone().add(offset.clone().multiply(0.82)));
			}
		}
		if (!highlights) locations.addAll(halo(loc, 0.75, 0.18 + Math.sin(tick * 0.25) * 0.08));
		return locations;
	}

	public SeraphicRain(boolean isUpgraded) {
		super(ID, "Seraphic Rain", isUpgraded, Rarity.EPIC, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 30, 10, TARGETS.range));
		requiredHits = isUpgraded ? 25 : 30;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta hits = new ActionMeta();
		ItemStack baseIcon = item.clone();
		ItemStack chargedIcon = item.clone().withType(Material.NETHER_STAR);
		SeraphicRainInstance inst = new SeraphicRainInstance(data, sessionEq, slot, es, hits, baseIcon, chargedIcon);

		data.addTrigger(id, bind, inst);
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent event = (DealDamageEvent) in;
			if (!event.getMeta().isBasicAttack() || hits.getCount() >= requiredHits) return TriggerResult.keep();

			int count = hits.addCount(1);
			if (count >= requiredHits) {
				inst.setIcon(chargedIcon);
			} else {
				ItemStack progressIcon = baseIcon.clone();
				progressIcon.setAmount(count);
				inst.setIcon(progressIcon);
			}
			return TriggerResult.keep();
		});
	}

	private class SeraphicRainInstance extends AmmoEquipmentInstance {
		private final ActionMeta hits;

		public SeraphicRainInstance(PlayerFightData data, SessionEquipment sessionEq, int slot, EquipSlot es,
				ActionMeta hits, ItemStack baseIcon, ItemStack chargedIcon) {
			super(data, sessionEq, slot, es);
			this.hits = hits;
			action = (pdata, in) -> {
				hits.setCount(0);
				setIcon(baseIcon);
				beginBarrage(data, slot);
				return TriggerResult.keep();
			};
		}

		@Override
		public boolean canTrigger(Player p, PlayerFightData data, Object in) {
			return hits.getCount() >= requiredHits && super.canTrigger(p, data, in);
		}
	}

	private void beginBarrage(PlayerFightData data, int slot) {
		Player p = data.getPlayer();
		data.getAttributes().applyTimedValue(data, PlayerAttributeController.GRAVITY, Attribute.GRAVITY, GRAVITY,
				DURATION);
		Vector launch = p.getEyeLocation().getDirection().setY(0);
		if (launch.lengthSquared() > 0.001) launch.normalize().multiply(0.25);
		launch.setY(0.85);
		p.setVelocity(launch);
		p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, DURATION / 4, 0, false, false, true));
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, DURATION, 0, false, false, true));
		data.applyStatus(StatusType.INVINCIBLE, data, 1, DURATION, this);
		Location launchLocation = p.getLocation();
		LAUNCH_BURST.play(p, launchLocation.clone().add(0, 0.2, 0));
		Sounds.enchant.play(p, launchLocation);
		Sounds.flap.play(p, launchLocation);
		data.runAnimation(id + "-launch-gold", p, LAUNCH_GOLD, p);
		data.runAnimation(id + "-launch-sky", p, LAUNCH_SKY, p);
		data.runAnimation(id + "-wings-gold", p, WINGS_GOLD, p);
		data.runAnimation(id + "-wings-white", p, WINGS_WHITE, p);

		data.addTask(new BukkitRunnable() {
			@Override
			public void run() {
				data.getPlayer().setGliding(true);
			}
		}.runTaskLater(NeoRogue.inst(), 6L));

		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int shot = 0; shot < PROJECTILES; shot++) {
			long delay = 10L + shot * 5L + random.nextLong(4L);
			data.addTask(new BukkitRunnable() {
				@Override
				public void run() {
					fireProjectile(data, slot);
				}
			}.runTaskLater(NeoRogue.inst(), delay));
		}

		data.addTask(new BukkitRunnable() {
			@Override
			public void run() {
				Player current = data.getPlayer();
				if (current.isGliding()) current.setGliding(false);
			}
		}.runTaskLater(NeoRogue.inst(), DURATION));
	}

	private void fireProjectile(PlayerFightData data, int slot) {
		AmmunitionInstance ammo = data.getAmmoInstance();
		if (ammo == null) return;

		Player p = data.getPlayer();
		List<LivingEntity> targets = TargetHelper.getEntitiesInRadius(p, TARGETS);
		if (targets.isEmpty()) return;

		ThreadLocalRandom random = ThreadLocalRandom.current();
		LivingEntity target = targets.get(random.nextInt(targets.size()));
		Location origin = p.getEyeLocation().add(random.nextDouble(-0.75, 0.75), random.nextDouble(-0.2, 0.65),
				random.nextDouble(-0.75, 0.75));
		Location aim = target.getLocation().add(random.nextDouble(-0.25, 0.25),
				target.getHeight() * random.nextDouble(0.35, 0.85), random.nextDouble(-0.25, 0.25));
		Vector direction = aim.toVector().subtract(origin.toVector());
		ProjectileGroup projectile = new ProjectileGroup(new SeraphicRainProjectile(data, ammo, slot));
		projectile.start(data, origin, direction);
	}

	private class SeraphicRainProjectile extends Projectile {
		private final PlayerFightData data;
		private final AmmunitionInstance ammo;
		private final int slot;

		public SeraphicRainProjectile(PlayerFightData data, AmmunitionInstance ammo, int slot) {
			super(1.5, TARGETS.range, 1);
			this.data = data;
			this.ammo = ammo;
			this.slot = slot;
			size(0.3, 0.3);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			EquipmentProperties ammoProperties = ammo.getProperties();
			proj.getMeta().addDamageSlice(new DamageSlice(data, ammoProperties.get(PropertyType.DAMAGE) * 2,
					ammoProperties.getType(), DamageStatTracker.of(id + slot, ammo.getAmmo())));
			ammo.onStart(proj);
			Player current = data.getPlayer();
			GOLD.play(current, proj.getLocation());
			WHITE.play(current, proj.getLocation());
			Sounds.shoot.play(current, current);
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			Player current = data.getPlayer();
			Location location = proj.getLocation();
			BowProjectile.tick.play(current, location);
			GOLD.play(current, location);
			SKY.play(current, location);
			ammo.onTick(current, proj, interpolation);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			ammo.onHit(proj, meta, hit.getEntity());
			Player current = data.getPlayer();
			Location hitLocation = hit.getEntity().getLocation().add(0, hit.getEntity().getHeight() * 0.5, 0);
			HIT_BURST.play(current, hitLocation);
			data.runAnimation(id + "-hit-" + proj.hashCode(), current, HIT_GOLD, hitLocation);
			Sounds.success.play(current, hitLocation);
		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block block) {
			ammo.onHitBlock(proj, block);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FEATHER,
				"Can only be cast after dealing basic attack damage " + DescUtil.val(requiredHits) + " times. On cast, leap into the air and briefly glide while " +
				GlossaryTag.INVINCIBLE.tag(this) + " " + DescUtil.duration(DURATION / 20) + ", firing " + DescUtil.val(PROJECTILES) +
				" projectiles at random nearby enemies. Projectiles use your equipped ammunition, consume ammunition, and deal " + DescUtil.val("2x") + " its damage.");
	}
}