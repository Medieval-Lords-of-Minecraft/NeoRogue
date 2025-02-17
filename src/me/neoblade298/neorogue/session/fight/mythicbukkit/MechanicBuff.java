package me.neoblade298.neorogue.session.fight.mythicbukkit;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;

public class MechanicBuff implements ITargetedEntitySkill {
	private final int seconds;
	protected final DamageCategory type;
	protected final boolean damageBuff;
	protected final double increase, multiplier;

    @Override
    public ThreadSafetyLevel getThreadSafetyLevel() {
        return ThreadSafetyLevel.SYNC_ONLY;
    }

	public MechanicBuff(MythicLineConfig config) {
        this.seconds = config.getInteger(new String[] {"s", "seconds" }, 1);
        this.type = DamageCategory.valueOf(config.getString(new String[] {"t", "type"}, "BLUNT").toUpperCase());
        this.damageBuff = config.getBoolean(new String[] { "isDamage", "dmg" }, true);
        this.increase = config.getDouble(new String[] { "i", "increase" }, 0);
        this.multiplier = config.getDouble(new String[] { "m", "mult" }, 0);
        
	}

	@Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
		try {
			FightData fd = FightInstance.getFightData(target.getBukkitEntity().getUniqueId());
			FightData fdCaster = FightInstance.getFightData(data.getCaster().getEntity().getUniqueId());
			if (seconds > 0) {
				if (damageBuff) {
					fd.addDamageBuff(DamageBuffType.of(type), new Buff(fdCaster, increase, multiplier, BuffStatTracker.ignored("MythicMobsDamageBuff")), seconds);
				}
				else {
					fd.addDefenseBuff(DamageBuffType.of(type), new Buff(fdCaster, increase, multiplier, BuffStatTracker.ignored("MythicMobsDefenseBuff")), seconds);
				}
			}
			else {
				if (damageBuff) {
					fd.addDamageBuff(DamageBuffType.of(type), new Buff(fdCaster, increase, multiplier, BuffStatTracker.ignored("MythicMobsDamageBuff")));
				}
				else {
					fd.addDefenseBuff(DamageBuffType.of(type), new Buff(fdCaster, increase, multiplier, BuffStatTracker.ignored("MythicMobsDefenseBuff")));
				}
			}
			return SkillResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return SkillResult.ERROR;
		}
    }
}
