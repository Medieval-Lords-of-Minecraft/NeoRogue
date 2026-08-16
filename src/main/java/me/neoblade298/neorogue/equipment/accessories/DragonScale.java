package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
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
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class DragonScale extends Equipment {
	private static final String ID = "DragonScale";
	private static final int RANGE = 12;
	private static final ParticleContainer TRAIL = new ParticleContainer(Particle.FLAME)
			.count(2).spread(0.05, 0.05).speed(0.01);
	private static final ParticleContainer EMBER = new ParticleContainer(Particle.LAVA)
			.count(1).spread(0.04, 0.04).speed(0);
	private static final ParticleContainer LAUNCH = new ParticleContainer(Particle.FLAME)
			.count(10).spread(0.1, 0.1).speed(0.01);
	private static final ParticleContainer IMPACT = new ParticleContainer(Particle.FLAME)
			.count(18).spread(0.1, 0.1).speed(0.01);
	private int threshold, damage = 40;

	public DragonScale(boolean isUpgraded) {
		super(ID, "Dragon Scale", isUpgraded, Rarity.EPIC, EquipmentClass.ARCHER, EquipmentType.ACCESSORY,
				EquipmentProperties.none());
		threshold = isUpgraded ? 12 : 15;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta burn = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (!event.isStatus(StatusType.BURN) || event.getStacks() <= 0) return TriggerResult.keep();
			burn.addCount(event.getStacks());
			while (burn.getCount() >= threshold) {
				burn.addCount(-threshold);
				data.launchAftershot(new ProjectileGroup(new DragonShot(data, slot)));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DRAGON_BREATH, "Every " + GlossaryTag.BURN.tag(this, threshold)
				+ " applied fires an " + GlossaryTag.AFTERSHOT.tag(this) + " dealing "
				+ GlossaryTag.FIRE.tag(this, damage) + " damage.");
	}

	private class DragonShot extends Projectile {
		private final PlayerFightData data;
		private final int slot;

		private DragonShot(PlayerFightData data, int slot) {
			super(RANGE, 1);
			this.data = data;
			this.slot = slot;
			setBowDefaults();
		}

		@Override
		public void onTick(ProjectileInstance projectile, int interpolation) {
			Player player = data.getPlayer();
			TRAIL.play(player, projectile.getLocation());
			if (interpolation % 2 == 0) EMBER.play(player, projectile.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance projectile) {
			Player player = data.getPlayer();
			IMPACT.play(player, projectile.getLocation());
			Sounds.fire.play(player, projectile.getLocation());
		}

		@Override
		public void onStart(ProjectileInstance projectile) {
			projectile.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.FIRE,
					DamageStatTracker.of(id + slot, DragonScale.this)));
			Player player = data.getPlayer();
			LAUNCH.play(player, projectile.getLocation());
			Sounds.fire.play(player, projectile.getLocation());
		}
	}
}