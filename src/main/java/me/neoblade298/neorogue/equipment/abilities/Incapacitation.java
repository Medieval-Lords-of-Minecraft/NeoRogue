package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
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
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class Incapacitation extends Equipment {
	private static final String ID = "Incapacitation";
	private static final int BASE_DAMAGE = 80;
	private static final int REND_THRESHOLD = 12;
	private static final int STAMINA_GAIN = 10;
	private static final int RANGE = 12;
	private static final ParticleContainer ARROW_TRAIL = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(205, 215, 225), 0.85F))
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer EMPOWERED_ARROW_TRAIL = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(235, 70, 35), 1.15F))
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer IMPACT = new ParticleContainer(Particle.CRIT)
			.count(7).spread(0.1, 0.1).offsetY(1).speed(0.01);
	private static final ParticleContainer EMPOWERED_IMPACT = new ParticleContainer(Particle.FIREWORK)
			.count(5).spread(0.08, 0.08).offsetY(1).speed(0.01);
	private final int bonusDamage, strength;

	public Incapacitation(boolean isUpgraded) {
		super(ID, "Incapacitation", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 20, 6, RANGE));
		bonusDamage = isUpgraded ? 80 : 40;
		strength = isUpgraded ? 2 : 1;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta rendApplied = new ActionMeta();

		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (event.isStatus(StatusType.REND) && event.getStacks() > 0) {
				rendApplied.addCount(event.getStacks());
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			boolean empowered = rendApplied.getCount() >= REND_THRESHOLD;
			rendApplied.setCount(0);
			if (empowered) {
				data.addStamina(STAMINA_GAIN);
				data.applyStatus(StatusType.STRENGTH, data, strength, -1, this);
				Sounds.success.play(data.getPlayer(), data.getPlayer());
			}

			new ProjectileGroup(new IncapacitationProjectile(data, slot, empowered)).start(data);
			return TriggerResult.keep();
		}));
	}

	private class IncapacitationProjectile extends Projectile {
		private final PlayerFightData data;
		private final int slot;
		private final boolean empowered;

		public IncapacitationProjectile(PlayerFightData data, int slot, boolean empowered) {
			super(properties.get(PropertyType.RANGE), 1);
			setBowDefaults();
			this.data = data;
			this.slot = slot;
			this.empowered = empowered;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			Player player = data.getPlayer();
			Location tip = proj.getLocation();
			Vector trailDirection = proj.getVelocity().clone();
			if (trailDirection.lengthSquared() > 0) {
				Location tail = tip.clone().subtract(trailDirection.normalize().multiply(0.8));
				ParticleUtil.drawLine(player, empowered ? EMPOWERED_ARROW_TRAIL : ARROW_TRAIL,
						tail, tip, 0.2);
			}
			BowProjectile.tick.play(player, tip);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Player player = data.getPlayer();
			IMPACT.play(player, hit.getEntity());
			if (empowered) EMPOWERED_IMPACT.play(player, hit.getEntity());
			Sounds.crit.play(player, hit.getEntity());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.shoot.play(data.getPlayer(), data.getPlayer());
			int damage = BASE_DAMAGE + (empowered ? bonusDamage : 0);
			proj.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.PIERCING,
					DamageStatTracker.of(id + slot, Incapacitation.this)));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SPECTRAL_ARROW,
				"On cast, fire an arrow that deals " + GlossaryTag.PIERCING.tag(this, BASE_DAMAGE)
				+ " damage. If you applied at least " + GlossaryTag.REND.tag(this, REND_THRESHOLD)
				+ " since the previous cast, gain " + DescUtil.white(STAMINA_GAIN) + " stamina, deal an additional "
				+ GlossaryTag.PIERCING.tag(this, bonusDamage) + " damage, and gain "
				+ GlossaryTag.STRENGTH.tag(this, strength) + ". Rend progress resets on every cast.");
	}
}
