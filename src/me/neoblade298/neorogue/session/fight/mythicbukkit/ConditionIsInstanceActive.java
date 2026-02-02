package me.neoblade298.neorogue.session.fight.mythicbukkit;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.conditions.ISkillMetaComparisonCondition;
import io.lumine.mythic.bukkit.BukkitAdapter;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;

public class ConditionIsInstanceActive implements ISkillMetaComparisonCondition {

	public ConditionIsInstanceActive(MythicLineConfig mlc) {
		
	}

	@Override
	public boolean check(SkillMetadata data, AbstractEntity ent) {
		FightData fd = FightInstance.getFightData(BukkitAdapter.adapt(ent).getUniqueId());
		if (fd == null) return false;
		if (fd.getInstance() == null) return false;
		return fd.getInstance().isActive();
	}
}
