package me.neoblade298.neorogue.tutorial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class TutorialManager {
	private static final List<Tutorial> tutorials = List.of(
			new FirstAbilityCastTutorial(),
			new FirstNodeSelectTutorial(),
			new FirstReforgeInventoryTutorial(),
			new FirstGlossaryOpenTutorial()
	);
	private static final EnumMap<TutorialTriggerType, List<Tutorial>> tutorialsByTrigger = new EnumMap<TutorialTriggerType, List<Tutorial>>(
			TutorialTriggerType.class
	);
	private static final Map<PlayerSessionData, Set<String>> registeredSessionTutorials = Collections.synchronizedMap(
			new WeakHashMap<>()
	);
	private static final Map<PlayerFightData, Set<String>> registeredFightTutorials = Collections.synchronizedMap(
			new WeakHashMap<>()
	);
	private static final Map<PlayerSessionData, Tutorial> activeSessionTutorial = Collections.synchronizedMap(
			new WeakHashMap<>()
	);
	private static final Map<PlayerFightData, Tutorial> activeFightTutorial = Collections.synchronizedMap(
			new WeakHashMap<>()
	);
	
	static {
		for (TutorialTriggerType triggerType : TutorialTriggerType.values()) {
			tutorialsByTrigger.put(triggerType, new ArrayList<>());
		}
		
		for (Tutorial tutorial : tutorials) {
			for (TutorialTriggerType triggerType : tutorial.getTriggerTypes()) {
				tutorialsByTrigger.get(triggerType).add(tutorial);
			}
		}
		
		for (TutorialTriggerType triggerType : TutorialTriggerType.values()) {
			tutorialsByTrigger.put(triggerType, Collections.unmodifiableList(tutorialsByTrigger.get(triggerType)));
		}
	}
	
	private TutorialManager() {
		
	}
	
	public static List<Tutorial> getTutorials(TutorialTriggerType triggerType) {
		return tutorialsByTrigger.getOrDefault(triggerType, List.of());
	}
	
	public static String getTutorialFlag(Tutorial tutorial) {
		return "tutorial_" + tutorial.getId();
	}

	public static void registerSessionTutorials(Session session, PlayerSessionData data) {
		for (Tutorial tutorial : getTutorials(TutorialTriggerType.SESSION)) {
			if (!tryRegister(registeredSessionTutorials, data, tutorial.getId())) continue;
			PlayerData pdata = data.getData();
			if (pdata != null && pdata.hasFlag(getTutorialFlag(tutorial))) continue;
			tutorial.registerSession(session, data);
		}
	}
	
	public static void registerFightTutorials(FightInstance fight, PlayerFightData data) {
		for (Tutorial tutorial : getTutorials(TutorialTriggerType.FIGHT)) {
			if (!tryRegister(registeredFightTutorials, data, tutorial.getId())) continue;
			PlayerData pdata = data.getSessionData().getData();
			if (pdata != null && pdata.hasFlag(getTutorialFlag(tutorial))) continue;
			tutorial.registerFight(fight, data);
		}
	}
	
	private static <T> boolean tryRegister(Map<T, Set<String>> registrations, T key, String tutorialId) {
		synchronized (registrations) {
			Set<String> registered = registrations.get(key);
			if (registered == null) {
				registered = new HashSet<>();
				registrations.put(key, registered);
			}
			return registered.add(tutorialId);
		}
	}
	
	public static boolean tryActivateSession(Tutorial tutorial, PlayerSessionData data) {
		synchronized (activeSessionTutorial) {
			Tutorial active = activeSessionTutorial.get(data);
			if (active != null) {
				PlayerData pdata = data.getData();
				if (pdata == null || !pdata.hasFlag(getTutorialFlag(active))) {
					return false;
				}
			}
			activeSessionTutorial.put(data, tutorial);
			return true;
		}
	}
	
	public static boolean tryActivateFight(Tutorial tutorial, PlayerFightData data) {
		synchronized (activeFightTutorial) {
			Tutorial active = activeFightTutorial.get(data);
			if (active != null) {
				PlayerData pdata = data.getSessionData().getData();
				if (pdata == null || !pdata.hasFlag(getTutorialFlag(active))) {
					return false;
				}
			}
			activeFightTutorial.put(data, tutorial);
			return true;
		}
	}
}
