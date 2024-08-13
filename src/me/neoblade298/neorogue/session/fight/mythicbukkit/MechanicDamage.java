package me.neoblade298.neorogue.session.fight.mythicbukkit;

import java.util.HashSet;

import org.bukkit.entity.LivingEntity;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.SkillTrigger;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;

public class MechanicDamage implements ITargetedEntitySkill {
	protected final int amount;
	protected final DamageType type;
	protected final boolean hitBarrier;
	protected Skill successSkill, failSkill;

    @Override
    public ThreadSafetyLevel getThreadSafetyLevel() {
        return ThreadSafetyLevel.SYNC_ONLY;
    }

	public MechanicDamage(MythicLineConfig cfg) {
        this.amount = cfg.getInteger(new String[] { "a", "amount" }, 0);
        this.type = DamageType.valueOf(cfg.getString(new String[] {"t", "type"}, "BLUNT").toUpperCase());
        this.hitBarrier = cfg.getBoolean(new String[] { "hb", "hitbarrier" }, false);

		String skillName = cfg.getString(new String[] { "onSuccess", "oS" });
		if (skillName != null) {
			successSkill = MythicBukkit.inst().getSkillManager().getSkill(skillName).get();
		}
		
		skillName = cfg.getString(new String[] { "onFail", "oF" });
		if (skillName != null) {
			failSkill = MythicBukkit.inst().getSkillManager().getSkill(skillName).get();
		}
	}

	@Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
		try {
			double level = data.getCaster().getLevel();
			final double fAmount = amount * (1 + (level / 10));
			FightData fd = FightInstance.getFightData(data.getCaster().getEntity().getUniqueId());
			DamageMeta meta = new DamageMeta(fd, fAmount, type);
			meta.setHitBarrier(hitBarrier);
			double dealt = FightInstance.dealDamage(meta, (LivingEntity) target.getBukkitEntity());
			

			HashSet<AbstractEntity> targets = new HashSet<AbstractEntity>();
			targets.add(target);
			
			Skill skill;
			if (dealt > 0) {
				skill = successSkill;
			}
			else {
				skill = failSkill;
			}
			if (skill != null) skill.execute(SkillTrigger.get("API"), data.getCaster(), data.getTrigger(),
					data.getCaster().getLocation(), targets, null, 1F);
			return SkillResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return SkillResult.ERROR;
		}
    }
}
