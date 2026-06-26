package me.neoblade298.neorogue.tutorial;

import java.util.EnumSet;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public interface Tutorial {
	public String getId();
	public EnumSet<TutorialTriggerType> getTriggerTypes();

	public default int getPriority() {
		return 0;
	}
	
	public default void registerSession(Session session, PlayerSessionData data) {
		
	}
	
	public default void registerFight(FightInstance fight, PlayerFightData data) {
		
	}
}
