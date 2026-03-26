package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class PredatorDrive extends Equipment {
	private static final String ID = "PredatorDrive";
	private int threshold;
	
	public PredatorDrive(boolean isUpgraded) {
		super(ID, "Predator Drive", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
			EquipmentType.ABILITY, EquipmentProperties.none());
		threshold = isUpgraded ? 2 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ItemStack icon = item.clone();
		ItemStack charged = item.clone().withType(Material.GLOWSTONE_DUST);
		ActionMeta am = new ActionMeta();
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			BasicAttackEvent ev = (BasicAttackEvent) in;
			Player p = data.getPlayer();
			
			// Only track projectile-based basic attacks
			if (ev.getProjectile() != null) {
				double distance = ev.getProjectile().getOrigin().distance(ev.getTarget().getLocation());
				
				// Check if hit was within 5 blocks
				if (distance <= 5) {
					am.addCount(1);
					
					// Reduce cooldowns when threshold is reached
					if (am.getCount() >= threshold) {
						am.setCount(0);
						icon.setAmount(1);
						inst.setIcon(icon);
						for (EquipmentInstance eqi : data.getActiveEquipment().values()) {
							eqi.addCooldown(-1);
						}
						Sounds.success.play(p, p);
					} else {
						// Update icon count
						int count = am.getCount();
						if (count >= threshold - 1) {
							// Show charged version
							charged.setAmount(count);
							inst.setIcon(charged);
						} else {
							icon.setAmount(count);
							inst.setIcon(icon);
						}
					}
				}
			}
			
			return TriggerResult.keep();
		});
	}
	
	@Override
	public void setupItem() {
		item = createItem(Material.SPECTRAL_ARROW,
			"Passive. Every " + DescUtil.yellow(threshold) + " basic attacks" + 
			" that hit an enemy within <white>5</white> blocks reduce all cooldowns by <white>1s</white>.");
	}
}
