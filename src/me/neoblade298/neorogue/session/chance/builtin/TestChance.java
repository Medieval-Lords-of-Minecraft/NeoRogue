package me.neoblade298.neorogue.session.chance.builtin;

import org.bukkit.Material;

import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.session.Instance;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;

public class TestChance extends ChanceSet {

	public TestChance(Instance next) {
		super(AreaType.OUTER_ADMIRATIO, Material.GRAVEL, "Tester", "Tester");
		ChanceStage stage = new ChanceStage(this, INIT_ID, "Click the choice to continue");

		stage.addChoice(new ChanceChoice(Material.DIAMOND_SWORD, "Next",
				"Click to continue.",
				(s, inst, data) -> {
					inst.setNextInstance(next);
					return null;
				}));
	}
}
