package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class FlowState extends Equipment {
	private static final String ID = "FlowState";
	private int thres;
	private double inc;
	
	public FlowState(boolean isUpgraded) {
		super(ID, "Flow State", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		thres = isUpgraded ? 40 : 30;
		inc = isUpgraded ? 0.8 : 0.5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addReforge(Dexterity.get(), Rushdown.get(), FlowState2.get());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(ID, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (data.getStamina() < thres) return TriggerResult.keep();
			data.addStamina(inc);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PRISMARINE_CRYSTALS,
				"Passive. Increase stamina regen by <yellow>" + inc + "</yellow> when above "
				+ "<yellow>" + thres + "</yellow> stamina.");
	}
}
