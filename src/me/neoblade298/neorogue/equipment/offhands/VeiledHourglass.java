package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class VeiledHourglass extends Equipment {
	private static final String ID = "VeiledHourglass";
	private int cdr;
	
	public VeiledHourglass(boolean isUpgraded) {
		super(ID, "Veiled Hourglass", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.OFFHAND, EquipmentProperties.ofUsable(0, 0, 5, 0));
		cdr = isUpgraded ? 4 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.teleport.play(p, p);
			// Check if player has stealth
			if (data.hasStatus(StatusType.STEALTH)) {
				// If in stealth, reduce all castable cooldowns
				for (EquipmentInstance ei : data.getActiveEquipment().values()) {
					if (ei.getEquipment().getType() == EquipmentType.ABILITY && ei.getCooldownSeconds() > 0) {
						ei.reduceCooldown(cdr);
					}
				}
			}
			else {
				// If not in stealth, gain stealth
				data.applyStatus(StatusType.STEALTH, data, 1, 200); // 10 seconds
			}
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, Trigger.RIGHT_CLICK, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CLOCK,
				"If not in " + GlossaryTag.STEALTH.tag(this) + ", gain <yellow>1</yellow> " + GlossaryTag.STEALTH.tag(this) + " [<white>10s</white>]. "
				+ "If in " + GlossaryTag.STEALTH.tag(this) + ", reduce all castable cooldowns by <yellow>" + cdr + "</yellow>.");
	}
}
