package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.equipment.weapons.Flametongue;
import me.neoblade298.neorogue.equipment.weapons.Harpoon;
import me.neoblade298.neorogue.equipment.weapons.RighteousFlame;
import me.neoblade298.neorogue.equipment.weapons.RighteousLance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class WeaponEnchantmentHoly extends Equipment {
	private static final String ID = "weaponEnchantmentHoly";
	private ProjectileGroup projs;
	private int damage, sanct;
	private static final int RANGE = 8;
	private static final SoundContainer sc = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_BREAK),
			scHit = new SoundContainer(Sound.BLOCK_GLASS_BREAK);
	private static final ParticleContainer tick = new ParticleContainer(Particle.FIREWORK).count(5).speed(0.02);
	
	public WeaponEnchantmentHoly(boolean isUpgraded) {
		super(ID, "Weapon Enchantment: Holy", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 3, RANGE));
		damage = isUpgraded ? 60 : 40;
		sanct = isUpgraded ? 50 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addReforge(Flametongue.get(), RighteousFlame.get());
		addReforge(Harpoon.get(), RighteousLance.get());
		addReforge(Windcutter.get(), LightPulse.get());
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		projs = new ProjectileGroup(new WeaponEnchantmentHolyProjectile(this, slot));
		data.addTrigger(id, Trigger.LEFT_CLICK, new WeaponEnchantmentHolyInstance(id));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR,
				"Passive. Your left clicks fire a projectile that deals " + GlossaryTag.LIGHT.tag(this, damage, true)
				+ " damage and applies " + GlossaryTag.SANCTIFIED.tag(this, sanct, true) + ".");
	}
	
	private class WeaponEnchantmentHolyInstance extends PriorityAction {
		private long nextCastTime = 0L;
		public WeaponEnchantmentHolyInstance(String id) {
			super(id);
			action = (pdata, in) -> {
				if (System.currentTimeMillis() > nextCastTime) {
					nextCastTime = System.currentTimeMillis() + 3000L;
					projs.start(pdata);
				}
				return TriggerResult.keep();
			};
		}
	}
	
	private class WeaponEnchantmentHolyProjectile extends Projectile {
		private Equipment eq;
		private int slot;
		public WeaponEnchantmentHolyProjectile(Equipment eq, int slot) {
			super(0.5, RANGE, 2);
			this.eq = eq;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			Player p = (Player) proj.getOwner().getEntity();
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			FightInstance.getFightData(hit.getEntity()).applyStatus(StatusType.SANCTIFIED, proj.getOwner(), sanct, -1);
			scHit.play((Player) proj.getOwner().getEntity(), hit.getEntity());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			sc.play((Player) proj.getOwner().getEntity(), proj.getOwner().getEntity());
			proj.getMeta().addDamageSlice(new DamageSlice(proj.getOwner(), damage, DamageType.LIGHT, DamageStatTracker.of(id + slot, eq)));
		}
	}
}
