package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class CopperFunnel extends Equipment {
	private static final String ID = "copperFunnel";
	private int reps, reduc = 25;
	
	public CopperFunnel(boolean isUpgraded) {
		super(ID, "Copper Funnel", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.OFFHAND, EquipmentProperties.ofUsable(isUpgraded ? 10 : 15, reduc, cooldown, cooldown));
				reps = isUpgraded ? 3 : 2;
				properties.addUpgrades(PropertyType.MANA_COST);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ItemStack icon = item.clone();
		icon.setAmount(reps);
		ActionMeta am = new ActionMeta();
		am.setCount(reps);
		data.addTrigger(id, bind, (pdata, in) -> {
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.HOPPER,
				"Passive. The first " + DescUtil.yellow(reps) + " you recieve " + GlossaryTag.MAGICAL.tag(this) + " damage, " +
				"reduce it by " + DescUtil.white(reduc) + ".");
	}
}
