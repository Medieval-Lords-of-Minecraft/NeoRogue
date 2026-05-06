package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Voltaics extends Equipment {
	private static final String ID = "Voltaics";
	private static final int TICK_INTERVAL = 5;
	private static final int BASE_ELECTRIFIED_STACKS = 3;
	private static final int STACKS_INCREASE_PER_HIT = 2;
	private static final int RANGE = 16;
	private static final TargetProperties tp = TargetProperties.line(RANGE, 2, TargetType.ENEMY);

	private int damage, stackThreshold;

	private static final ParticleContainer boltCore = new ParticleContainer(Particle.END_ROD)
			.count(3).spread(0.05, 0.05).speed(0);
	private static final ParticleContainer boltSpark = new ParticleContainer(Particle.ELECTRIC_SPARK)
			.count(2).spread(0.1, 0.1).speed(0.01);
	private static final ParticleContainer impactFlash = new ParticleContainer(Particle.FIREWORK)
			.count(12).spread(0.4, 0.4).speed(0.1);
	private static final ParticleContainer impactSpark = new ParticleContainer(Particle.ELECTRIC_SPARK)
			.count(20).spread(0.5, 0.5).speed(0.15);
	private static final SoundContainer fireSound = new SoundContainer(Sound.ENTITY_BREEZE_WIND_BURST, 0.8F, 1.6F);
	private static final SoundContainer impactSound = new SoundContainer(Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.9F, 1.4F);

	public Voltaics(boolean isUpgraded) {
		super(ID, "Voltaics", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(isUpgraded ? 50 : 70, 0, 0, RANGE));
		properties.addUpgrades(PropertyType.MANA_COST);
		damage = isUpgraded ? 120 : 80;
		stackThreshold = isUpgraded ? 15 : 20;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta tickCounter = new ActionMeta();
		ActionMeta totalElectrifiedApplied = new ActionMeta();

		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);

			data.addTrigger(id, Trigger.PLAYER_TICK, (pdata2, in2) -> {
				tickCounter.addCount(1);
				if (tickCounter.getCount() >= TICK_INTERVAL) {
					tickCounter.setCount(0);

					Player p2 = data.getPlayer();
					LivingEntity target = TargetHelper.getNearestInSight(p2, tp);
					if (target != null) {
						Location origin = p2.getLocation().add(0, 1.5, 0);
						Vector dir = target.getLocation().add(0, 1, 0).subtract(origin).toVector().normalize();
						ProjectileGroup proj = new ProjectileGroup(new LightningBoltProjectile(data, slot, totalElectrifiedApplied));
						proj.start(data, origin, dir);
					}
				}
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	private class LightningBoltProjectile extends Projectile {
		private final PlayerFightData data;
		private final int slot;
		private final ActionMeta totalElectrifiedApplied;

		public LightningBoltProjectile(PlayerFightData data, int slot, ActionMeta totalElectrifiedApplied) {
			super(3.0, RANGE, 1);
			this.size(0.3, 0.3);
			this.data = data;
			this.slot = slot;
			this.totalElectrifiedApplied = totalElectrifiedApplied;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			Player p = data.getPlayer();
			boltCore.play(p, proj.getLocation());
			boltSpark.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Player p = data.getPlayer();
			Location loc = proj.getLocation();
			impactFlash.play(p, loc);
			impactSpark.play(p, loc);
			impactSound.play(p, loc);

			// Apply electrified with increasing stacks
			int stacks = BASE_ELECTRIFIED_STACKS + (totalElectrifiedApplied.getCount() / stackThreshold * STACKS_INCREASE_PER_HIT);
			hit.applyStatus(StatusType.ELECTRIFIED, data, stacks, -1);
			totalElectrifiedApplied.addCount(stacks);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Player p = data.getPlayer();
			fireSound.play(p, p);
			Sounds.thunder.play(p, p);
			proj.addDamageSlice(new DamageSlice(data, damage, DamageType.LIGHTNING, DamageStatTracker.of(ID + slot, Voltaics.this)));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_SHARD,
				"Castable once. Every <white>" + TICK_INTERVAL + "s</white>, fire a bolt of lightning at the nearest enemy in sight that deals " +
				GlossaryTag.LIGHTNING.tag(this, damage, true) + " and applies " + GlossaryTag.ELECTRIFIED.tag(this, BASE_ELECTRIFIED_STACKS, false) +
				". The number of " + GlossaryTag.ELECTRIFIED.tag(this) + " stacks applied increases by <white>" + STACKS_INCREASE_PER_HIT +
				"</white> for every " + DescUtil.yellow(stackThreshold) + " " + GlossaryTag.ELECTRIFIED.tag(this) + " applied.");
	}
}
