package me.neoblade298.neorogue.session.fight.mythicbukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.api.skills.conditions.ISkillCondition;
import io.lumine.mythic.api.skills.targeters.ISkillTargeter;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.bukkit.events.MythicTargeterLoadEvent;

public class MythicLoader implements Listener {

	@EventHandler
	public void onMythicConditionLoad(MythicConditionLoadEvent event) {
		String name = event.getConditionName().toLowerCase();
		ISkillCondition condition = null;
		MythicLineConfig cfg = event.getConfig();
		switch (name) {
		case "hitbarrier":
			condition = new ConditionHitBarrier(cfg);
			break;
		}
		if (condition != null) {
			event.register(condition);
		}
	}

	@EventHandler
	public void onMythicTargeterLoad(MythicTargeterLoadEvent event) {
		String name = event.getTargeterName().toLowerCase();
		ISkillTargeter targeter = null;
		MythicLineConfig cfg = event.getConfig();
		
		switch (name) {
		case "mythiclocation":
			targeter = new TargeterMythicLocation(cfg);
			break;
		case "session":
			targeter = new TargeterSession(cfg);
			break;
		}
		
		if (targeter != null) {
			event.register(targeter);
		}
	}

	@EventHandler
	public void onMythicMechanicLoad(MythicMechanicLoadEvent event) {
		String name = event.getMechanicName().toLowerCase();
		ISkillMechanic mechanic = null;
		MythicLineConfig cfg = event.getConfig();

		switch (name) {
		case "nrdamage":
			mechanic = new MechanicDamage(cfg);
			break;
		case "nrbarrier":
			mechanic = new MechanicBarrier(cfg);
			break;
		case "nrstopbarrier":
			mechanic = new MechanicStopBarrier(cfg);
			break;
		case "nrbuff":
			mechanic = new MechanicBuff(cfg);
			break;
		case "nrstatus":
			mechanic = new MechanicStatus(cfg);
			break;
		case "nrmodifykb":
			mechanic = new MechanicModifyKnockback(cfg);
			break;
		case "nrrefresh":
			mechanic = new MechanicRefresh(cfg);
			break;
		}
		if (mechanic != null) {
			event.register(mechanic);
		}
	}
}
