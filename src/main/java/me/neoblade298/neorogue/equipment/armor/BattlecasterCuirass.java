package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BattlecasterCuirass extends Equipment {
	private static final String ID = "BattlecasterCuirass";
	private static final double REGEN = 0.5;
	private int shields;

	public BattlecasterCuirass(boolean isUpgraded) {
		super(ID, "Battlecaster Cuirass", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		shields = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addManaRegen(REGEN);
		data.addStaminaRegen(REGEN);
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			Player player = data.getPlayer();
			data.addSimpleShield(player.getUniqueId(), shields, 100, this);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_CHESTPLATE, "Increase mana and stamina regen by " + DescUtil.white(REGEN)
				+ ". Casting an ability grants " + GlossaryTag.SHIELDS.tag(this, shields) + " ["
				+ DescUtil.white("5s") + "].");
	}
}
