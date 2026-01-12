package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class FlowState2 extends Equipment {
	private static final String ID = "FlowState2";
	private int thres;
	private double inc, dmgInc;
	
	public FlowState2(boolean isUpgraded) {
		super(ID, "Flow State II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		thres = 30;
		inc = 0.8;
		dmgInc = isUpgraded ? 0.5 : 0.3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
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
				+ "<yellow>" + thres + "</yellow> stamina, further increased by " + DescUtil.yellow(dmgInc)
				+ " if you've dealt damage within <white>2</white> seconds.");
	}
}
