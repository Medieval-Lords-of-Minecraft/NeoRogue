package me.neoblade298.neorogue.session.chance.builtin;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerClass;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;

public class LostRelicChance extends ChanceSet {
	private static final HashMap<PlayerClass, Equipment> items = new HashMap<PlayerClass, Equipment>();
	
	static {
		items.put(PlayerClass.ARCHER, Equipment.get("mangledBow", false));
		items.put(PlayerClass.THIEF, Equipment.get("dullDagger", false));
		items.put(PlayerClass.WARRIOR, Equipment.get("rustySword", false));
		items.put(PlayerClass.MAGE, Equipment.get("gnarledWand", false));
	}

	public LostRelicChance() {
		super(AreaType.LOW_DISTRICT, Material.GRAVEL, "LostRelic", "Lost Relic", true);
		ChanceStage stage = new ChanceStage(this, INIT_ID, "You spot an old weapon on the ground. At first glance it seems worthless, "
				+ "but something makes you think it’s worth keeping around.");

		stage.addChoice(new ChanceChoice(Material.COPPER_INGOT, "Take the old weapon",
				"Acquire a <red>cursed item</red> that cannot be unequipped or used.",
				(s, inst, data) -> {
					Player p = data.getPlayer();
					p.getInventory().addItem(items.get(data.getPlayerClass()).getItem());
					Util.msg(p, "You pick up the old weapon and go on your way. It's a little heavy.");
					return null;
				}));
		
		stage.addChoice(new ChanceChoice(Material.LEATHER_BOOTS, "Leave it",
				"Doesn't look worth the extra weight.",
				(s, inst, unused) -> {
					return null;
				}));
	}
}
