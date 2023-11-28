package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Offhand;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.BuffType;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class SmallShield extends Offhand {
	private int reduction;
	
	public SmallShield(boolean isUpgraded) {
		super("smallShield", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		display = "Small Shield";
		reduction = isUpgraded ? 5 : 3;
		item = createItem(this, Material.SHIELD, null, "When raised, reduce all damage taken by <yellow>" + reduction + "</yellow>.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		data.addTrigger(id, Trigger.RAISE_SHIELD, (inputs) -> {
			data.addBuff(p.getUniqueId(), false, false, BuffType.PHYSICAL, reduction);
			return false;
		});
		
		data.addTrigger(id, Trigger.LOWER_SHIELD, (inputs) -> {
			data.addBuff(p.getUniqueId(), false, false, BuffType.PHYSICAL, -reduction);
			return false;
		});
	}
}
