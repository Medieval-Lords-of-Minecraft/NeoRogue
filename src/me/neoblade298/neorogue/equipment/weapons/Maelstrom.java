package me.neoblade298.neorogue.equipment.weapons;

import java.util.HashSet;
import java.util.LinkedList;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
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
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class Maelstrom extends Equipment {
	private static final String ID = "Maelstrom";
	private static final int RANGE = 14, BOLT_RANGE = 8;
	private static final TargetProperties boltTp = TargetProperties.radius(BOLT_RANGE, false, TargetType.ENEMY);
	private static final ParticleContainer projPart = new ParticleContainer(Particle.ELECTRIC_SPARK)
			.count(3).spread(0.1, 0.1).speed(0);
	private static final ParticleContainer boltPart = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(100, 180, 255), 1F))
			.count(1).spread(0.05, 0.05).speed(0);
	private static final SoundContainer boltSound = new SoundContainer(Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5F);

	public Maelstrom(boolean isUpgraded) {
		super(ID, "Maelstrom", isUpgraded, Rarity.LEGENDARY, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWand(isUpgraded ? 85 : 65, 0.9, 0, 0.8, RANGE, DamageType.LIGHTNING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		properties.addUpgrades(PropertyType.DAMAGE);
	}

	@Override
	public void setupReforges() {
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta armed = new ActionMeta();

		// Arm weapon when player applies electrified
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.ELECTRIFIED)) return TriggerResult.keep();
			armed.setBool(true);
			return TriggerResult.keep();
		});

		// Fire projectile when armed
		ProjectileGroup proj = new ProjectileGroup(new MaelstromProjectile(data, this, slot, armed));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (pdata, in) -> {
			if (!armed.getBool()) return TriggerResult.keep();
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR)) return TriggerResult.keep();
			Player p = data.getPlayer();
			weaponSwing(p, data);
			armed.setBool(false);
			data.chargeSecs(properties.get(PropertyType.CHARGE_TIME)).then(() -> proj.start(data));
			return TriggerResult.keep();
		});
	}

	private void chainBolt(Player p, PlayerFightData data, LivingEntity source, int slot, HashSet<LivingEntity> hit) {
		hit.add(source);
		Location sourceLoc = source.getLocation().add(0, 1, 0);

		// Find nearest enemy to source, excluding already-hit entities
		LinkedList<LivingEntity> nearby = TargetHelper.getEntitiesInRadius(p, sourceLoc, boltTp);
		LivingEntity next = null;
		for (LivingEntity ent : nearby) {
			if (!hit.contains(ent)) {
				next = ent;
				break;
			}
		}
		if (next == null) return;

		// Draw bolt and deal damage
		Location targetLoc = next.getLocation().add(0, 1, 0);
		ParticleUtil.drawLine(p, boltPart, sourceLoc, targetLoc, 0.3);
		boltSound.play(p, targetLoc);
		weaponDamage(p, data, next);

		// Chain further if target is electrified
		FightData fd = FightInstance.getFightData(next);
		if (fd != null && fd.hasStatus(StatusType.ELECTRIFIED)) {
			chainBolt(p, data, next, slot, hit);
		}
	}

	private class MaelstromProjectile extends Projectile {
		private final PlayerFightData data;
		private final Maelstrom eq;
		private final int slot;
		private final ActionMeta armed;

		public MaelstromProjectile(PlayerFightData data, Maelstrom eq, int slot, ActionMeta armed) {
			super(2, RANGE, 1);
			this.size(0.4, 0.4);
			this.data = data;
			this.eq = eq;
			this.slot = slot;
			this.armed = armed;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			projPart.play(data.getPlayer(), proj.getLocation());
		}

		@Override
		public void onHit(FightData hitTarget, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Player p = data.getPlayer();
			LivingEntity target = hitTarget.getEntity();
			Sounds.thunder.play(p, target);

			// Chain bolt if target is electrified
			if (hitTarget.hasStatus(StatusType.ELECTRIFIED)) {
				HashSet<LivingEntity> hit = new HashSet<>();
				chainBolt(p, data, target, slot, hit);
			}
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.applyWeapon(data, eq, slot);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TRIDENT, "Can only be used after applying " + GlossaryTag.ELECTRIFIED.tag(this)
				+ ". Must reapply after each use. Basic attacks that hit an " + GlossaryTag.ELECTRIFIED.tag(this)
				+ " enemy fire a bolt to the nearest enemy within " + DescUtil.white(BOLT_RANGE)
				+ " blocks, dealing " + GlossaryTag.LIGHTNING.tag(this, (int) properties.get(PropertyType.DAMAGE), true)
				+ " damage. The bolt chains further if the new target is also "
				+ GlossaryTag.ELECTRIFIED.tag(this) + ". Each bolt counts as a basic attack.");
	}
}
