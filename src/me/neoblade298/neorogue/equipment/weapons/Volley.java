package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.AmmunitionInstance;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Volley extends Equipment {
	private static final String ID = "volley";
	private static final int[] rotations = new int[] { 0, -15, 15, -30, 30 };
	private int damage;
	
	public Volley(boolean isUpgraded) {
		super(ID, "Volley", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(10, 20, 12, 7));
		properties.addUpgrades(PropertyType.RANGE);
		damage = isUpgraded ? 100 : 75;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (d, inputs) -> {
			data.channel(20);
			data.addTask(new BukkitRunnable() {
				public void run() {
					ProjectileGroup proj = new ProjectileGroup();
					for (int i : rotations) {
						if (data.getAmmoInstance() != null) {
							proj.add(new VolleyProjectile(data, i));
							data.getAmmoInstance().use();
						}
						else {
							break;
						}
					}
					proj.start(data);
				}
			}.runTaskLater(NeoRogue.inst(), 20L));
			return TriggerResult.keep();
		}, Bow.needsAmmo));
	}
	
	private class VolleyProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private AmmunitionInstance ammo;

		// Vector is non-normalized velocity of the vanilla projectile being fired
		public VolleyProjectile(PlayerFightData data, int rotation) {
			super(properties.get(PropertyType.RANGE), 1);
			setBowDefaults();
			this.rotation(rotation);
			this.data = data;
			this.p = data.getPlayer();
			this.ammo = data.getAmmoInstance();
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			BowProjectile.tick.play(p, proj.getLocation());
			ammo.onTick(p, proj, interpolation);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			damageProjectile(hit.getEntity(), proj, hitBarrier);
			ammo.onHit(proj, hit.getEntity());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.shoot.play(p, p);
			DamageMeta dm = proj.getMeta();
			EquipmentProperties ammoProps = ammo.getProperties();
			double dmg = ammoProps.get(PropertyType.DAMAGE) + damage;
			dm.addDamageSlice(new DamageSlice(data, dmg, ammoProps.getType()));
			ammo.onStart(proj, false);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIREWORK_ROCKET, "On cast, " + GlossaryTag.CHARGE.tag(this) + " for <white>1s</white> before firing " +
			"<white>5</white> projectiles in a <white>60 degree</white> cone " +
		 	"that deal " + GlossaryTag.PIERCING.tag(this, damage, true) + " damage using " +
			"your current ammunition.");
	}
}
