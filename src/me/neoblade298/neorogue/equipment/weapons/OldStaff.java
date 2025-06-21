package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class OldStaff extends Equipment {
	private static final String ID = "oldStaff";
	private static final ParticleContainer tick = new ParticleContainer(org.bukkit.Particle.ASH).count(5).spread(0.1, 0.1).speed(0.01);
	private static final SoundContainer hit = new SoundContainer(Sound.BLOCK_CHAIN_PLACE);
	private int bonus;
	
	public OldStaff(boolean isUpgraded) {
		super(
				ID , "Old Staff", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(1, 0, 20, 1.3, DamageType.DARK, Sound.ENTITY_PLAYER_ATTACK_SWEEP)
		);
		bonus = isUpgraded ? 30 : 20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new OldStaffProjectile(data));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}
	
	private class OldStaffProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;

		public OldStaffProjectile(PlayerFightData data) {
			super(1.5, 10, 2);
			this.size(0.2, 0.2);
			this.data = data;
			this.p = data.getPlayer();
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Location loc = hit.getEntity().getLocation();
			OldStaff.hit.play(p, loc);
			applyProjectileOnHit(hit.getEntity(), proj, proj.getMeta(), hitBarrier, true);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			double damage = properties.get(PropertyType.DAMAGE);
			if (data.getMana() < data.getMaxMana() * 0.25) {
				damage += bonus;
			}
			proj.addDamageSlice(new DamageSlice(data, damage, DamageType.DARK));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DEAD_BUSH, "Deals an additional " + DescUtil.yellow(bonus) + " damage when below <white>25%</white> mana.");
	}
}
