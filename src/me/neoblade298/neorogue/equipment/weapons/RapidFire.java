package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.AmmunitionInstance;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class RapidFire extends Equipment {
	private static final String ID = "rapidFire";
	private int thres, damage;
	private static final int MAX = 6;
	
	public RapidFire(boolean isUpgraded) {
		super(ID, "Rapid Fire", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(20, 5, 13, 8));
		properties.addUpgrades(PropertyType.RANGE);
		thres = isUpgraded ? 7 : 10;
		damage = 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardEquipmentInstance inst = new StandardEquipmentInstance(p, this, slot, es);
		inst.setAction((pdata, in) -> {
			ProjectileGroup proj = new ProjectileGroup(new RapidFireProjectile(data));
			for (int i = 0; i * thres <= inst.getCount() && i < MAX; i++) {
				data.addTask(new BukkitRunnable() {
					public void run() {
						if (data.getAmmoInstance() != null) proj.start(data);
					}
				}.runTaskLater(NeoRogue.inst(), i * 5));
			}
			return TriggerResult.keep();
		});
		inst.setCondition(Bow.needsAmmo);
		data.addTrigger(id, bind, inst);

		ItemStack icon = item.clone();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.REND)) return TriggerResult.keep();
			inst.addCount(ev.getStacks());
			icon.setAmount(Math.min(MAX, (inst.getCount() / thres) + 1));
			data.setIcon(slot, icon);
			if (inst.getCount() >= MAX * thres) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}
	
	private class RapidFireProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private AmmunitionInstance ammo;

		// Vector is non-normalized velocity of the vanilla projectile being fired
		public RapidFireProjectile(PlayerFightData data) {
			super(properties.get(PropertyType.RANGE), 1);
			this.gravity(0.03);
			this.size(1, 1);
			this.data = data;
			this.p = data.getPlayer();
			this.ammo = data.getAmmoInstance();

			blocksPerTick(2);
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			BowProjectile.tick.play(p, proj.getLocation());
			ammo.onTick(p, proj, interpolation);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			damageProjectile(hit.getEntity(), proj, hitBarrier);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.shoot.play(p, p);
			DamageMeta dm = proj.getMeta();
			EquipmentProperties ammoProps = ammo.getProperties();
			double dmg = ammoProps.get(PropertyType.DAMAGE) + damage;
			dm.addDamageSlice(new DamageSlice(data, dmg, ammoProps.getType()));
			ammo.onStart(proj);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIREWORK_ROCKET, "On cast, fire a projectile for every " + GlossaryTag.REND.tag(this, thres, true) + " you've applied +1, " +
			"up to <white>6</white>. Each projectile deals " + DescUtil.white(damage) + " damage using your current ammunition.");
	}
}
