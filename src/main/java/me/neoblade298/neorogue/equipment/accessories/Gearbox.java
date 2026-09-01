package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Trap;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Gearbox extends Equipment {
	private static final String ID = "Gearbox";
	private static final int MAX_TRAPS = 3;
	private double regenPerTrap;

	public Gearbox(boolean isUpgraded) {
		super(ID, "Gearbox", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER, EquipmentType.ACCESSORY);
		regenPerTrap = isUpgraded ? 0.3 : 0.2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta applied = new ActionMeta();
		data.addTrigger(id, Trigger.LAY_TRAP, (pdata, in) -> {
			updateRegen(data, applied, 1);
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.DEACTIVATE_TRAP, (pdata, in) -> {
			updateRegen(data, applied, 0);
			return TriggerResult.keep();
		});
	}

	private void updateRegen(PlayerFightData data, ActionMeta applied, int pendingTraps) {
		long activeTraps = data.getTraps().values().stream().filter(Trap::isActive).count() + pendingTraps;
		double desired = Math.min(MAX_TRAPS, activeTraps) * regenPerTrap;
		data.addStaminaRegen(desired - applied.getDouble());
		applied.setDouble(desired);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.COMPARATOR, "Gain " + DescUtil.val(regenPerTrap)
				+ " stamina regeneration for each active " + GlossaryTag.TRAP.tag(this) + " you own, up to "
				+ DescUtil.val(MAX_TRAPS) + " active traps.");
	}
}