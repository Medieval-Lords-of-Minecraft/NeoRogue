package me.neoblade298.neorogue.tutorial;

import java.util.EnumSet;

import org.bukkit.entity.Player;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class FirstGlossaryOpenTutorial implements Tutorial {
	private static final String ID = "first_glossary_open";

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
		data.addTrigger(ID, SessionTrigger.VISIT_NODE, (pdata, in) -> {
			if (!TutorialManager.tryActivateSession(this, pdata)) return TriggerResult.keep();
			Player p = pdata.getPlayer();
			p.showTitle(Title.title(
					Component.text("Tip!", NamedTextColor.GREEN),
					Component.text("Right-click equipment to view its glossary!", NamedTextColor.YELLOW)
			));
			return TriggerResult.remove();
		});

		data.addTrigger(ID, SessionTrigger.OPEN_GLOSSARY, (pdata, in) -> {
			if (!TutorialManager.isActivatedSession(this, pdata)) return TriggerResult.keep();
			if (pdata.getData().hasFlag(TutorialManager.getTutorialFlag(this))) return TriggerResult.remove();
			pdata.getData().addFlag(TutorialManager.getTutorialFlag(this));
			Player p = pdata.getPlayer();
			p.showTitle(Title.title(
					Component.text("Nice!", NamedTextColor.GREEN),
					Component.text("You opened the glossary!", NamedTextColor.YELLOW)
			));
			return TriggerResult.remove();
		});
	}
}
