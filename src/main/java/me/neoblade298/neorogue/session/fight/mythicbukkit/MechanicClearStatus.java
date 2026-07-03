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
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;

public class MechanicClearStatus implements ITargetedEntitySkill {
	protected final String id;
	protected final boolean asParent;

	@Override
	public ThreadSafetyLevel getThreadSafetyLevel() {
		return ThreadSafetyLevel.SYNC_ONLY;
	}

	public MechanicClearStatus(MythicLineConfig config) {
		this.id = config.getString("id");
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

			FightData fd = FightInstance.getFightData(target.getUniqueId());
			if (fd == null) return SkillResult.CONDITION_FAILED;

			int current = fd.getStatus(id).getStacks();
			if (current <= 0) return SkillResult.CONDITION_FAILED;

			// Clear the status by applying the negative of the current stacks
			FightInstance.applyStatus((LivingEntity) target.getBukkitEntity(), id, (LivingEntity) ent, -current, 0);
			return SkillResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return SkillResult.ERROR;
		}
	}
}
