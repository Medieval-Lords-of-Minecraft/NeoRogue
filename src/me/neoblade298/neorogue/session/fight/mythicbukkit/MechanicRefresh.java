package me.neoblade298.neorogue.session.fight.mythicbukkit;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;

public class MechanicRefresh implements ITargetedEntitySkill {

    @Override
    public ThreadSafetyLevel getThreadSafetyLevel() {
        return ThreadSafetyLevel.EITHER;
    }

	@Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
		try {
			FightData fd = FightInstance.getFightData(target.getBukkitEntity());
			fd.updateDisplayName();
			return SkillResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return SkillResult.ERROR;
		}
    }
}
