package me.neoblade298.neorogue.equipment.weapons;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class EnfeeblingWand extends Equipment {
	private static final String ID = "enfeeblingWand";
	private static final ParticleContainer tick;
	private static final SoundContainer tickSound = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_BREAK),
			hit = new SoundContainer(Sound.BLOCK_CHAIN_PLACE);
	private double mult;
	private int multStr;
	
	static {
		tick = new ParticleContainer(Particle.SMOKE);
		tick.count(5).spread(0.1, 0.1).speed(0.01);
	}
	
	public EnfeeblingWand(boolean isUpgraded) {
		super(
				ID , "Enfeebling Wand", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(3, 0, 60, 0.5, DamageType.DARK, Sound.ENTITY_PLAYER_ATTACK_SWEEP)
		);
		mult = isUpgraded ? 0.3 : 0.2;
		multStr = (int) (100 * mult);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new EnfeeblingWandProjectile(data, this, slot));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}
	
	private class EnfeeblingWandProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private String buffId = UUID.randomUUID().toString();
		private EnfeeblingWand eq;
		private int slot;

		public EnfeeblingWandProjectile(PlayerFightData data, EnfeeblingWand eq, int slot) {
			super(1.5, 10, 2);
			this.size(0.2, 0.2);
			this.p = data.getPlayer();
			this.data = data;
			this.eq = eq;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Location loc = hit.getEntity().getLocation();
			EnfeeblingWand.hit.play(p, loc);
			hit.addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.multiplier(data, -mult, BuffStatTracker.defenseDebuffEnemy(buffId, eq, false)), 100);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			tickSound.play(p, proj.getLocation());
			proj.applyWeapon(data, eq, slot);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STICK, "Lowers magical defense of target by " + DescUtil.yellow(multStr + "%") + " on hit for <white>5s</white>. Does not stack.");
	}
}
