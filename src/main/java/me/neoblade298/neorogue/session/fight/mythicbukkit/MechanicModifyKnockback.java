package me.neoblade298.neorogue.session.fight.mythicbukkit;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;

public class MechanicModifyKnockback implements ITargetedEntitySkill {
	protected final double amt;
	protected final String type;

    @Override
    public ThreadSafetyLevel getThreadSafetyLevel() {
        return ThreadSafetyLevel.EITHER;
    }

	public MechanicModifyKnockback(MythicLineConfig cfg) {
        this.type = cfg.getString(new String[] { "type", "t" }, "SET").toUpperCase();
        this.amt = cfg.getDouble(new String[] {"amount", "a"}, 0);
	}

	@Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
		try {
			FightData fd = FightInstance.getFightData(target.getBukkitEntity());
			if (fd == null) return SkillResult.INVALID_TARGET;
			switch (this.type) {
			case "ADD": fd.setKnockbackMultiplier(fd.getKnockbackMultiplier() + amt);
			break;
			case "RESET": fd.setKnockbackMultiplier(fd.getMob() != null ? fd.getMob().getKnockbackMultiplier() : 1);
			break;
			case "MULTIPLY": fd.setKnockbackMultiplier(fd.getKnockbackMultiplier() * amt);
			break;
			default: fd.setKnockbackMultiplier(amt);
			break;
			}
			return SkillResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return SkillResult.ERROR;
		}
    }
}
