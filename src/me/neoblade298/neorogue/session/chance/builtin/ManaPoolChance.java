package me.neoblade298.neorogue.session.chance.builtin;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.shared.droptables.DropTable;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.abilities.AgilityTraining;
import me.neoblade298.neorogue.equipment.abilities.BasicDarkArts;
import me.neoblade298.neorogue.equipment.abilities.BasicElementMastery;
import me.neoblade298.neorogue.equipment.abilities.BasicInfusionMastery;
import me.neoblade298.neorogue.equipment.abilities.BasicManaManipulation;
import me.neoblade298.neorogue.equipment.abilities.Dexterity;
import me.neoblade298.neorogue.equipment.abilities.EnduranceTraining;
import me.neoblade298.neorogue.equipment.abilities.Furor;
import me.neoblade298.neorogue.equipment.abilities.KeenSenses;
import me.neoblade298.neorogue.equipment.abilities.Resourcefulness;
import me.neoblade298.neorogue.equipment.artifacts.Anxiety;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;

public class ManaPoolChance extends ChanceSet {
	private static final HashMap<EquipmentClass, DropTable<Equipment>> tables = new HashMap<EquipmentClass, DropTable<Equipment>>();

	static {
		DropTable<Equipment> table = new DropTable<Equipment>();
		table.add(Furor.get(), 1);
		table.add(EnduranceTraining.get(), 1);
		table.add(BasicInfusionMastery.get(), 1);
		tables.put(EquipmentClass.WARRIOR, table);
		table = new DropTable<Equipment>();
		table.add(BasicDarkArts.get(), 1);
		table.add(BasicManaManipulation.get(), 1);
		table.add(Resourcefulness.get(), 1);
		table.add(Dexterity.get(), 1);
		tables.put(EquipmentClass.THIEF, table);
		table = new DropTable<Equipment>();
		table.add(AgilityTraining.get(), 1);
		table.add(KeenSenses.get(), 1);
		table.add(BasicElementMastery.get(), 1);
		tables.put(EquipmentClass.ARCHER, table);
	}

	public ManaPoolChance() {
		super(RegionType.LOW_DISTRICT, Material.BLUE_ICE, "ManaPool", "Mana Pool", true);
		ChanceStage stage = new ChanceStage(this, INIT_ID, "The winding corridors give way to a surprisingly vegetated room. " +
			"In the center is a small pool of crystal clear water. There is an inexplicable force that draws you towards it.");

		stage.addChoice(new ChanceChoice(Material.WATER_BUCKET, "Drink the water",
				"<white>50%</white> chance to acquire a random highly reforgeable Equipment,"
				+ " <white>50%</white> chance to start the next fight with <white>25%</white> reduced damage for <white>20s</white>.",
				(s, inst, data) -> {
					Player p = data.getPlayer();
					if (NeoRogue.gen.nextBoolean()) {
						data.giveEquipment(tables.get(data.getPlayerClass()).get());
						s.broadcast(SharedUtil.color("<yellow>" + p.getName() + "</yellow> drank the water and received equipment!"));
					}
					else {
						s.broadcast(SharedUtil.color("<yellow>" + p.getName() + "</yellow> drank the water and received indigestion! Hilarious."));
						data.giveEquipment(Anxiety.get());
					}
					return null;
				}));

		stage.addChoice(new ChanceChoice(Material.BARRIER, "Don't",
				"Dude, I don't know where that's been.",
				(s, inst, data) -> {
					Player p = data.getPlayer();
					s.broadcast(SharedUtil.color("<yellow>" + p.getName() + "</yellow> decides not to drink the strange floor water."));
					return null;
				}));
	}
}
