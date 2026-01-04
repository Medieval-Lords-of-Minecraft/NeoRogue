package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class WeaponEnchantmentDarkness extends Equipment {
	private static final String ID = "WeaponEnchantmentDarkness";
	private int damage;
	private static final ParticleContainer slash = new ParticleContainer(Particle.SWEEP_ATTACK);
	private static final ParticleContainer trail = new ParticleContainer(Particle.SOUL);
	private ProjectileGroup projs;
	
	public WeaponEnchantmentDarkness(boolean isUpgraded) {
		super(ID, "Weapon Enchantment: Darkness", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		
		damage = isUpgraded ? 300 : 200;
		slash.count(3).spread(0, 0).speed(0);
		trail.count(15).spread(0.3, 0.3).speed(0.02);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		projs = new ProjectileGroup(new DarknessSlashProjectile(slot, this));
		
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			am.addCount(1);
			if (am.getCount() >= 3) {
				am.addCount(-3);
				Sounds.flap.play(p, p);
				slash.play(p, p);
				trail.play(p, p);
				projs.start(data);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.OBSIDIAN,
				"Passive. Every <white>3rd</white> basic attack launches a slash projectile that deals "
						+ "<yellow>" + damage + " </yellow>" + GlossaryTag.DARK.tag(this) + " damage and pierces.");
	}
	
	private class DarknessSlashProjectile extends Projectile {
		private Equipment eq;
		private int slot;
		
		public DarknessSlashProjectile(int slot, Equipment eq) {
			super(0.5, 8, 2);
			this.size(1.5, 1.5);
			this.pierce(-1);
			this.eq = eq;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			Player p = (Player) proj.getOwner().getEntity();
			slash.play(p, proj.getLocation());
			trail.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.getMeta().addDamageSlice(new DamageSlice(proj.getOwner(), damage, DamageType.DARK, DamageStatTracker.of(id + slot, eq)));
		}
	}
}
