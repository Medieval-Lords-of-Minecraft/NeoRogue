package me.neoblade298.neorogue.session.fight.mythicbukkit;

import org.bukkit.entity.LivingEntity;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import me.neoblade298.neorogue.session.fight.FightInstance;

public class MechanicStatus implements ITargetedEntitySkill {
	protected final String id;
	protected final int stacks, seconds;

    @Override
    public ThreadSafetyLevel getThreadSafetyLevel() {
        return ThreadSafetyLevel.SYNC_ONLY;
    }

	public MechanicStatus(MythicLineConfig config) {
		this.id = config.getString("id");
        this.stacks = config.getInteger(new String[] { "a", "amount" }, 0);
        this.seconds = config.getInteger(new String[] { "s", "seconds" }, 0);
	}

	@Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
		try {
			FightInstance.applyStatus((LivingEntity) target.getBukkitEntity(), id, (LivingEntity) data.getCaster().getEntity().getBukkitEntity(), stacks, seconds);
			return SkillResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return SkillResult.ERROR;
		}
    }
}
