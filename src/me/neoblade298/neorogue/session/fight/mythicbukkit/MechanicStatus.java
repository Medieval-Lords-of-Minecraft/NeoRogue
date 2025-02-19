package me.neoblade298.neorogue.session.fight.mythicbukkit;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.neoblade298.neorogue.session.fight.FightInstance;

public class MechanicStatus implements ITargetedEntitySkill {
	protected final String id;
	protected final boolean asParent;
	protected final int stacks, ticks;

    @Override
    public ThreadSafetyLevel getThreadSafetyLevel() {
        return ThreadSafetyLevel.SYNC_ONLY;
    }

	public MechanicStatus(MythicLineConfig config) {
		this.id = config.getString("id");
        this.stacks = config.getInteger(new String[] { "a", "amount" }, 0);
        this.ticks = config.getInteger(new String[] { "t", "ticks" }, 0);
		this.asParent = config.getBoolean(new String[] { "asParent", "ap" }, false);
	}

	@Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
		try {
			ActiveMob am = MythicBukkit.inst().getMobManager().getMythicMobInstance(data.getCaster().getEntity()); // Currently assumes caster is always mythicmob
			if (asParent && !am.getParent().isPresent()) {
				return SkillResult.CONDITION_FAILED;
			}
			Entity ent = asParent ? am.getParent().get().getBukkitEntity() : data.getCaster().getEntity().getBukkitEntity();
			FightInstance.applyStatus((LivingEntity) target.getBukkitEntity(), id, (LivingEntity) ent, stacks, ticks);
			return SkillResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return SkillResult.ERROR;
		}
    }
}
