package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
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

public class StrangelySmoothStone extends Equipment {
	private static final String ID = "StrangelySmoothStone";
	private static final double RANGE = 16;
	private static final int CONCUSSED = 3;
	private static final TargetProperties TARGETS = TargetProperties.radius(RANGE, false, TargetType.ENEMY);
	private static final ParticleContainer TRAIL = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.SMOOTH_STONE.createBlockData()).count(1).spread(0.04, 0.04).speed(0);
	private static final ParticleContainer IMPACT = TRAIL.clone().count(8).spread(0.1, 0.1).speed(0.01);
	private static final SoundContainer LAUNCH_SOUND = new SoundContainer(Sound.ENTITY_SNOWBALL_THROW, 0.65F, 0.7F);
	private static final SoundContainer IMPACT_SOUND = new SoundContainer(Sound.BLOCK_STONE_BREAK, 0.75F, 0.85F);
	private final int casts, damage;

	public StrangelySmoothStone(boolean isUpgraded) {
		super(ID, "Strangely Smooth Stone", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY);
		casts = isUpgraded ? 2 : 3;
		damage = isUpgraded ? 60 : 40;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta count = new ActionMeta();
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			if (count.addCount(1) < casts) return TriggerResult.keep();
			count.addCount(-casts);

			Player player = data.getPlayer();
			LivingEntity target = TargetHelper.getNearest(player, TARGETS);
			if (target == null) return TriggerResult.keep();

			ProjectileInstance projectile = new StoneProjectile(data, slot).start(data,
					player.getLocation().add(0, player.isSneaking() ? 1 : 1.4, 0),
					target.getEyeLocation().toVector().subtract(player.getEyeLocation().toVector()));
			projectile.setHomingTarget(target);
			return TriggerResult.keep();
		});
	}

	private class StoneProjectile extends Projectile {
		private final PlayerFightData data;
		private final int slot;

		private StoneProjectile(PlayerFightData data, int slot) {
			super(1, RANGE, 1);
			homing(0.03);
			this.data = data;
			this.slot = slot;
		}

		@Override
		public void onStart(ProjectileInstance projectile) {
			projectile.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.EARTHEN,
					DamageStatTracker.of(id + slot, StrangelySmoothStone.this)));
			Player player = data.getPlayer();
			LAUNCH_SOUND.play(player, player);
		}

		@Override
		public void onTick(ProjectileInstance projectile, int interpolation) {
			TRAIL.play(data.getPlayer(), projectile.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance projectile) {
			if (hitBarrier == null) {
				hit.applyStatus(StatusType.CONCUSSED, data, CONCUSSED, -1, StrangelySmoothStone.this);
				Player player = data.getPlayer();
				IMPACT.play(player, hit.getEntity().getLocation().add(0, 1, 0));
				IMPACT_SOUND.play(player, hit.getEntity());
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SMOOTH_STONE, "Every " + DescUtil.val(casts) + " casts, fire a projectile at the nearest enemy that deals "
				+ GlossaryTag.EARTHEN.tag(this, damage) + " damage and applies "
				+ GlossaryTag.CONCUSSED.tag(this, CONCUSSED) + ".");
	}
}