package me.neoblade298.neorogue.session.fight.mythicbukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.api.skills.conditions.ISkillCondition;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import me.neoblade298.neomythicextension.NeoRogueBarrier;
import me.neoblade298.neomythicextension.NeoRogueBuff;
import me.neoblade298.neomythicextension.NeoRogueDamage;
import me.neoblade298.neomythicextension.NeoRogueStopBarrier;
import me.neoblade298.neomythicextension.mechanics.ElementDamage;
import me.neoblade298.neomythicextension.mechanics.FlagMechanic;
import me.neoblade298.neomythicextension.mechanics.GiveAccountTagMechanic;
import me.neoblade298.neomythicextension.mechanics.GiveAttributeMechanic;
import me.neoblade298.neomythicextension.mechanics.GivePartyBossExpMechanic;
import me.neoblade298.neomythicextension.mechanics.GiveStoredItemMechanic;
import me.neoblade298.neomythicextension.mechanics.ModGlobalScore;
import me.neoblade298.neomythicextension.mechanics.ModManaMechanic;
import me.neoblade298.neomythicextension.mechanics.ModScore;
import me.neoblade298.neomythicextension.mechanics.PluginMessageMechanic;
import me.neoblade298.neomythicextension.mechanics.ReduceThreatMechanic;
import me.neoblade298.neomythicextension.mechanics.RemoveFlagMechanic;
import me.neoblade298.neomythicextension.mechanics.ResearchKillsMechanic;
import me.neoblade298.neomythicextension.mechanics.ResearchPointsBossMechanic;
import me.neoblade298.neomythicextension.mechanics.ResearchPointsChanceMechanic;
import me.neoblade298.neomythicextension.mechanics.ResearchPointsMechanic;
import me.neoblade298.neomythicextension.mechanics.ScaleChestMechanic;
import me.neoblade298.neomythicextension.mechanics.ScaleExpMechanic;
import me.neoblade298.neomythicextension.mechanics.ScaleGoldMechanic;
import me.neoblade298.neomythicextension.mechanics.ScaleHealMechanic;
import me.neoblade298.neomythicextension.mechanics.ScaleToLevelMechanic;
import me.neoblade298.neomythicextension.mechanics.TauntMechanic;
import me.neoblade298.neomythicextension.mechanics.WarnMechanic;

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
		}
		if (mechanic != null) {
			event.register(mechanic);
		}
	}
}
