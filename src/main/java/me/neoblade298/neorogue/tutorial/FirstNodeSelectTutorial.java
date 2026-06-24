package me.neoblade298.neorogue.tutorial;

import java.util.EnumSet;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class FirstNodeSelectTutorial implements Tutorial {
	private static final String ID = "first_node_select";

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
		data.addTrigger(ID, SessionTrigger.ENTER_NODE_SELECT, (pdata, in) -> {
			pdata.getData().addFlag(TutorialManager.getTutorialFlag(this));
			Player p = pdata.getPlayer();
			new BukkitRunnable() {
				@Override
				public void run() {
					if (!p.isOnline()) return;
					p.showTitle(Title.title(
							Component.text(""),
							Component.text("Press a wooden button to choose your path!", NamedTextColor.YELLOW)
					));
				}
			}.runTaskLater(NeoRogue.inst(), 100L);
		});
	}
}
