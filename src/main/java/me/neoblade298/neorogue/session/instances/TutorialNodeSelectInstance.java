package me.neoblade298.neorogue.session.instances;

import org.bukkit.Location;
import org.bukkit.entity.TextDisplay;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TutorialNodeSelectInstance extends NodeSelectInstance {
	private static final String[] ROW_TIPS = {
		"Press a button to go there!",
		"There's no healing after fights so be careful!\nSprinting also costs stamina!",
		"You can heal or upgrade gear at shrines!",
		"Get ready to fight a miniboss!",
		"One last shrine before the boss!",
		"Good luck on your first boss!"
	};

	public TutorialNodeSelectInstance(Session s) {
		super(s);
	}

	@Override
	protected void createInfoHologram(Location loc) {
		int row = s.getNode().getRow();
		String tip = row < ROW_TIPS.length ? ROW_TIPS[row] : "";
		Component text = Component.text(tip, NamedTextColor.YELLOW);
		TextDisplay holo = NeoRogue.createHologram(loc, text);
		holograms.add(holo);
	}
}
