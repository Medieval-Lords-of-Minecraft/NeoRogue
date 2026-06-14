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
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class PredatorDrive extends Equipment implements Power {
	private static final String ID = "PredatorDrive";
	private SessionEquipment sessionEq;
	private int threshold;
	
	public PredatorDrive(boolean isUpgraded) {
		super(ID, "Predator Drive", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
			EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		threshold = isUpgraded ? 3 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		this.sessionEq = sessionEq;
		ActionMeta castCount = new ActionMeta();
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			if (castCount.addCount(1) < 4) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ItemStack icon = item.clone();
		ItemStack charged = item.clone().withType(Material.GLOWSTONE_DUST);
		ActionMeta am = new ActionMeta();
		EquipmentInstance inst = new EquipmentInstance(data, sessionEq, slot, es);
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata2, in2) -> {
			BasicAttackEvent ev = (BasicAttackEvent) in2;
			Player p2 = data.getPlayer();
			
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
						Sounds.success.play(p2, p2);
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
			GlossaryTag.POWER.tag(this) + ". Activates after casting " + DescUtil.white(4) + " abilities. Every " + DescUtil.yellow(threshold) + " basic attacks" + 
			" that hit an enemy within " + DescUtil.white(5) + " blocks reduce all cooldowns by " + DescUtil.white("1s") + ".");
	}
}
