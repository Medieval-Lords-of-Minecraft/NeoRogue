package me.neoblade298.neorogue.session.chance.builtin;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;

public class LostRelicChance extends ChanceSet {
	private static final HashMap<EquipmentClass, Equipment> items = new HashMap<EquipmentClass, Equipment>();
	
	static {
		items.put(EquipmentClass.ARCHER, Equipment.get("mangledBow", false));
		items.put(EquipmentClass.THIEF, Equipment.get("dullDagger", false));
		items.put(EquipmentClass.WARRIOR, Equipment.get("rustySword", false));
		items.put(EquipmentClass.MAGE, Equipment.get("gnarledWand", false));
	}

	public LostRelicChance() {
		super(AreaType.LOW_DISTRICT, Material.GRAVEL, "LostRelic", "Lost Relic", true);
		ChanceStage stage = new ChanceStage(this, INIT_ID, "You spot an old weapon on the ground. At first glance it seems worthless, "
				+ "but something makes you think it’s worth keeping around.");

		stage.addChoice(new ChanceChoice(Material.COPPER_INGOT, "Take the old weapon",
				"Acquire a <red>cursed item</red> that cannot be unequipped or used, but gain <yellow>50 coins</yellow>.",
				(s, inst, data) -> {
					Player p = data.getPlayer();
					p.getInventory().addItem(items.get(data.getPlayerClass()).getItem());
					data.addCoins(50);
					Util.msgRaw(p, "You pick up the old weapon and go on your way. It's a little heavy.");
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> decided to take the old weapon!", p);
					return null;
				}));
		
		stage.addChoice(new ChanceChoice(Material.LEATHER_BOOTS, "Leave it",
				"Doesn't look worth the extra weight.",
				(s, inst, data) -> {
					Player p = data.getPlayer();
					Util.msgRaw(data.getPlayer(), "Hard pass. It looks ugly too.");
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> decided to skip the old weapon!", p);
					return null;
				}));
	}
}
