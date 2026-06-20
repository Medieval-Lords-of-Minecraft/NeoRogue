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
import me.neoblade298.neorogue.session.settings.NotorietySetting;

public class MechanicStatus implements ITargetedEntitySkill {
	protected final String id;
	protected final boolean asParent, scales;
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
		this.scales = config.getBoolean(new String[] { "scales", "s" }, true);
	}

	@Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
		try {
			ActiveMob am = MythicBukkit.inst().getMobManager().getMythicMobInstance(data.getCaster().getEntity()); // Currently assumes caster is always mythicmob
			if (asParent && !am.getParent().isPresent()) {
				return SkillResult.CONDITION_FAILED;
			}
			Entity ent = asParent ? am.getParent().get().getBukkitEntity() : data.getCaster().getEntity().getBukkitEntity();
			int finalStacks = stacks;
			if (scales) {
				double level = data.getCaster().getLevel();
				FightData fd = asParent ? FightInstance.getFightData(am.getParent().get().getBukkitEntity())
						: FightInstance.getFightData(data.getCaster().getEntity().getUniqueId());
				boolean increaseDamageNotoriety = NotorietySetting.INCREASE_DAMAGE.isActive(fd.getInstance().getSession());
				double mult = (1 + (level * 0.05)) * (increaseDamageNotoriety ? NotorietySetting.INCREASE_DAMAGE_MULTIPLIER : 1.0);
				finalStacks = (int) (stacks * mult);
			}
			FightInstance.applyStatus((LivingEntity) target.getBukkitEntity(), id, (LivingEntity) ent, finalStacks, ticks);
			return SkillResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return SkillResult.ERROR;
		}
    }
}
