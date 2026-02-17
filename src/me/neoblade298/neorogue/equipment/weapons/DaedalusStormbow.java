package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.IProjectileInstance;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class DaedalusStormbow extends Bow {
	private static final String ID = "DaedalusStormbow";
	private static final int EXTRA_SHOT_DAMAGE_INCREMENT = 5;
	private int threshold;
	
	public DaedalusStormbow(boolean isUpgraded) {
		super(ID, "Daedalus Stormbow", isUpgraded, Rarity.EPIC, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofBow(55, 1, 0, 12, 0, 0));
		threshold = isUpgraded ? 7 : 10;
	}

	@Override
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {
		BowProjectile.tick.play(p, proj.getLocation());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ItemStack icon = item.clone();
		ItemStack chargedIcon = icon.clone().withType(Material.CYAN_STAINED_GLASS);
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		
		// Track projectile damage hits and extra shot damage
		DaedalusStormbowData stormbowData = new DaedalusStormbowData();
		
		// Standard bow shooting with extra shot
		data.addSlotBasedTrigger(id, slot, Trigger.VANILLA_PROJECTILE, (pdata, in) -> {
			if (!canShoot(data)) return TriggerResult.keep();
			useBow(data);

			ProjectileLaunchEvent ev = (ProjectileLaunchEvent) in;
			Vector velocity = ev.getEntity().getVelocity();
			
			ProjectileGroup proj = new ProjectileGroup(new BowProjectile(data, velocity, this, id + slot));
			
			// Add extra shot with rotation
			BowProjectile extraShot = new BowProjectile(data, velocity, this, false, id + slot + "-extra");
			extraShot.setDamageBonus(stormbowData.extraShotDamage);
			extraShot.rotation(15);
			proj.add(extraShot);
			
			proj.start(data);
			return TriggerResult.keep();
		});
		
		// Increase basic attack projectile range by 4
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in;
			if (!ev.isBasicAttack()) return TriggerResult.keep();
			if (!(ev.getInstances().getFirst() instanceof ProjectileInstance)) return TriggerResult.keep();
			for (IProjectileInstance ipi : ev.getInstances()) {
				((ProjectileInstance) ipi).addMaxRange(4);
			}
			return TriggerResult.keep();
		});
		
		// Track projectile damage to increase extra shot damage
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().hasOrigin(DamageOrigin.PROJECTILE)) return TriggerResult.keep();
			
			stormbowData.hitCount++;
			
			if (stormbowData.hitCount >= threshold) {
				stormbowData.hitCount -= threshold;
				stormbowData.extraShotDamage += EXTRA_SHOT_DAMAGE_INCREMENT;
				
				// Update icon to show extra shot damage
				ItemStack currentIcon = chargedIcon.clone();
				currentIcon.setAmount(Math.min(64, stormbowData.extraShotDamage / EXTRA_SHOT_DAMAGE_INCREMENT));
				inst.setIcon(currentIcon);
				
				Player p = data.getPlayer();
				Sounds.fire.play(p, p);
			}
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOW,
				"Passive. Increase basic attack range by <white>4</white> and fire an extra shot on basic attack launch. " +
				"Every " + DescUtil.yellow(threshold) + " times you deal projectile damage, increases the damage your extra shots " +
				"deal by " + DescUtil.yellow(EXTRA_SHOT_DAMAGE_INCREMENT) + ".");
	}
	
	private static class DaedalusStormbowData {
		int hitCount = 0;
		int extraShotDamage = 0;
	}
}
