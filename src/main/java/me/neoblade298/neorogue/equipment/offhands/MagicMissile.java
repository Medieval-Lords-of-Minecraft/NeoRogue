package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class MagicMissile extends Equipment {
	private static final String ID = "MagicMissile";
	private static final int RANGE = 12;
	private static final ParticleContainer TRAIL = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(95, 45, 145), 0.9F))
			.count(2).spread(0.04, 0.04).speed(0);
	private static final ParticleContainer IMPACT = TRAIL.clone().count(10).spread(0.1, 0.1).speed(0.01);
	private static final SoundContainer LAUNCH_SOUND = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.65F, 1.45F);
	private static final SoundContainer IMPACT_SOUND = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6F, 0.8F);
	private final int damage;

	public MagicMissile(boolean isUpgraded) {
		super(ID, "Magic Missile", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.OFFHAND,
				EquipmentProperties.ofUsable(5, 5, 4, RANGE));
		damage = isUpgraded ? 45 : 30;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ProjectileGroup missiles = new ProjectileGroup(new MissileProjectile(data, slot));
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player player = data.getPlayer();
			missiles.start(data, player.getLocation().add(0, player.isSneaking() ? 1 : 1.4, 0),
					player.getEyeLocation().getDirection());
			return TriggerResult.keep();
		}));
	}

	private class MissileProjectile extends Projectile {
		private final PlayerFightData data;
		private final int slot;

		private MissileProjectile(PlayerFightData data, int slot) {
			super(1.5, RANGE, 1);
			this.data = data;
			this.slot = slot;
		}

		@Override
		public void onStart(ProjectileInstance projectile) {
			projectile.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.DARK,
					DamageStatTracker.of(id + slot, MagicMissile.this)));
			Player player = data.getPlayer();
			LAUNCH_SOUND.play(player, player);
		}

		@Override
		public void onTick(ProjectileInstance projectile, int interpolation) {
			TRAIL.play(data.getPlayer(), projectile.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance projectile) {
			if (hitBarrier != null) return;
			Player player = data.getPlayer();
			IMPACT.play(player, hit.getEntity().getLocation().add(0, 1, 0));
			IMPACT_SOUND.play(player, hit.getEntity());
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIRE_CHARGE, "Fire a fast projectile that deals "
				+ GlossaryTag.DARK.tag(this, damage) + " damage.");
	}
}