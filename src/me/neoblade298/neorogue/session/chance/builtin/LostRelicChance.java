package me.neoblade298.neorogue.session.chance.builtin;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.equipment.cursed.DullDagger;
import me.neoblade298.neorogue.equipment.cursed.GnarledStaff;
import me.neoblade298.neorogue.equipment.cursed.MangledBow;
import me.neoblade298.neorogue.equipment.cursed.RustySword;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.CustomGlossaryIcon;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;

public class LostRelicChance extends ChanceSet {
	private static final HashMap<EquipmentClass, Equipment> items = new HashMap<EquipmentClass, Equipment>();
	
	static {
		items.put(EquipmentClass.ARCHER, MangledBow.get());
		items.put(EquipmentClass.THIEF, DullDagger.get());
		items.put(EquipmentClass.WARRIOR, RustySword.get());
		items.put(EquipmentClass.MAGE, GnarledStaff.get());
	}

	public LostRelicChance() {
		super(RegionType.LOW_DISTRICT, Material.GRAVEL, "LostRelic", "Lost Relic", true);
		ChanceStage stage = new ChanceStage(this, INIT_ID, "You spot an old weapon on the ground. At first glance it seems worthless, "
				+ "but something makes you think it’s worth keeping around.");

		ChanceChoice ch = new ChanceChoice(Material.COPPER_INGOT, "Take the old weapon",
				"Acquire a <red>cursed accessory</red> that cannot be unequipped or used, but becomes a strong weapon when purified at a shop.",
				"You don't have an accessory slot available",
				(s, inst, pdata) -> {
					int numCurses = pdata.aggregateEquipment((meta) -> { return meta.getEquipment().getType() == EquipmentType.ACCESSORY && meta.getEquipment().isCursed(); }).size();
					if (numCurses >= PlayerSessionData.ACCESSORY_SIZE) return false;
					return true;
				},
				(s, inst, data) -> {
					Player p = data.getPlayer();
					data.unequip(EquipmentType.ACCESSORY);
					data.giveEquipment(items.get(data.getPlayerClass()));
					Util.msgRaw(p, "You pick up the old weapon and go on your way. It's a little heavy.");
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> decided to take the old weapon!", p);
					return null;
				});
		ch.addTag(new CustomGlossaryIcon("mangledBow", MangledBow.get().getItem()));
		ch.addTag(new CustomGlossaryIcon("dullDagger", DullDagger.get().getItem()));
		ch.addTag(new CustomGlossaryIcon("rustySword", RustySword.get().getItem()));
		ch.addTag(new CustomGlossaryIcon("gnarledWand", GnarledStaff.get().getItem()));
		stage.addChoice(ch);
		
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
