package me.neoblade298.neorogue.session.instances;

import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.reward.RunReward;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class WinInstance extends EndRunInstance {
	public WinInstance(Session s) {
		super(s);
	}

	@Override
	protected boolean isWin() {
		return true;
	}

	@Override
	protected void onRunEnd() {
		int sessionNotoriety = s.getNotoriety();
		for (PlayerSessionData psd : s.getParty().values()) {
			PlayerData pd = PlayerManager.getPlayerData(psd.getUniqueId());
			if (pd != null && sessionNotoriety >= pd.getMaxNotoriety(psd.getPlayerClass())) {
				pd.increaseNotorietyMax(psd.getPlayerClass());
			}
		}

		for (PlayerSessionData data : s.getParty().values()) {
			data.trigger(SessionTrigger.WIN_RUN, null);
			data.trigger(SessionTrigger.FINISH_RUN, true);
		}
		RunReward.payout(s, true);
	}

	@Override
	protected Component getResultMessage() {
		return Component.text("Congratulations! You won!", NamedTextColor.GREEN);
	}
}
