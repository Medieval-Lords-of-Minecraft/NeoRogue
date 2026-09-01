package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
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
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ArcaneAegis extends Equipment {
	private static final String ID = "ArcaneAegis";
	private static final int DAMAGE = 60, RANGE = 14, SHIELD_TICKS = 100;
	private static final ParticleContainer TRAIL = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(65, 55, 120), 1F))
			.count(2).spread(0.04, 0.04).speed(0);
	private static final ParticleContainer IMPACT = TRAIL.clone().count(8).spread(0.1, 0.1).speed(0.01);
	private static final ParticleContainer PAYOFF = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(125, 195, 225), 1F))
			.count(10).spread(0.35, 0.6).speed(0.01).offsetY(0.8);
	private static final SoundContainer LAUNCH_SOUND = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.65F, 0.9F);
	private static final SoundContainer PAYOFF_SOUND = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.65F, 1.35F);
	private final int shields, mana;

	public ArcaneAegis(boolean isUpgraded) {
		super(ID, "Arcane Aegis", isUpgraded, Rarity.RARE, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWand(DAMAGE, 0.9, 0, 1, RANGE, DamageType.DARK,
						Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		shields = isUpgraded ? 6 : 4;
		mana = isUpgraded ? 6 : 4;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ProjectileGroup projectile = new ProjectileGroup(new AegisProjectile(data, slot));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (pdata, in) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR)) return TriggerResult.keep();
			Player player = data.getPlayer();
			weaponSwing(player, data);
			data.wandDelaySecs(properties.get(PropertyType.CHARGE_TIME)).then(() -> projectile.start(data));
			return TriggerResult.keep();
		});
	}

	private class AegisProjectile extends Projectile {
		private final PlayerFightData data;
		private final int slot;

		private AegisProjectile(PlayerFightData data, int slot) {
			super(2, RANGE, 1);
			this.data = data;
			this.slot = slot;
		}

		@Override
		public void onStart(ProjectileInstance projectile) {
			projectile.applyWeapon(data, ArcaneAegis.this, slot);
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
			data.addSimpleShield(player.getUniqueId(), shields, SHIELD_TICKS, ArcaneAegis.this);
			data.addMana(mana);
			IMPACT.play(player, hit.getEntity().getLocation().add(0, 1, 0));
			PAYOFF.play(player, player.getLocation());
			PAYOFF_SOUND.play(player, player);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BREEZE_ROD, "Fire a projectile that deals "
				+ GlossaryTag.DARK.tag(this, DAMAGE) + " damage. Hitting an enemy grants "
				+ GlossaryTag.SHIELDS.tag(this, shields) + " " + DescUtil.val("[5s]")
				+ " and " + DescUtil.val(mana) + " mana.");
	}
}