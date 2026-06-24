package me.neoblade298.neorogue.tutorial;

import java.util.EnumSet;

import org.bukkit.entity.Player;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class FirstReforgeInventoryTutorial implements Tutorial {
	private static final String ID = "first_reforge_inventory";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public EnumSet<TutorialTriggerType> getTriggerTypes() {
		return EnumSet.of(TutorialTriggerType.SESSION);
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data) {
		data.addTrigger(ID, SessionTrigger.OPEN_SESSION_INVENTORY, (pdata, in) -> {
			if (pdata.getData().hasFlag(TutorialManager.getTutorialFlag(this))) return;
			if (pdata.computeAvailableReforges().isEmpty()) return;
			pdata.getData().addFlag(TutorialManager.getTutorialFlag(this));
			Player p = pdata.getPlayer();
			p.showTitle(Title.title(
					Component.text(""),
					Component.text("Click the anvil to reforge your equipment!", NamedTextColor.YELLOW)
			));
		});
	}
}
