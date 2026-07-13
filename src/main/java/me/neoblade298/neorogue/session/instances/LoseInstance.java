package me.neoblade298.neorogue.session.instances;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.reward.RunReward;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class LoseInstance extends EndRunInstance {
	public LoseInstance(Session s) {
		super(s);
	}

	@Override
	protected boolean isWin() {
		return false;
	}

	@Override
	protected void onRunEnd() {
		for (PlayerSessionData data : s.getParty().values()) {
			data.trigger(SessionTrigger.FINISH_RUN, false);
		}
		RunReward.payout(s, false);
	}

	@Override
	protected Component getResultMessage() {
		return Component.text("You lost!", NamedTextColor.RED);
	}
}
