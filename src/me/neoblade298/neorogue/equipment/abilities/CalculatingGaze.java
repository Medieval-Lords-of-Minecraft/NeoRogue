package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class CalculatingGaze extends Equipment {
	private static final String ID = "calculatingGaze";
	private int shields;
	private double regen;
	private static final int THRES = 20;
	
	public CalculatingGaze(boolean isUpgraded) {
		super(ID, "Calculating Gaze", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
				shields = 5;
				regen = isUpgraded ? 0.5 : 0.3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addPermanentShield(p.getUniqueId(), shields);
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (data.getMana() > THRES) data.addMana(regen);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ENDER_EYE,
				"Passive. Gain " + GlossaryTag.SHIELDS.tag(this, shields, false) + " at the start of a fight. " +
				"Increase mana regen by " + DescUtil.yellow(regen) + " when above " + DescUtil.white(THRES) + " mana.");
	}
}
