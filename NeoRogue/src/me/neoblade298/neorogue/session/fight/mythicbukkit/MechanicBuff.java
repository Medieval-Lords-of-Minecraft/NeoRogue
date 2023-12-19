package me.neoblade298.neorogue.session.fight.mythicbukkit;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.buff.BuffType;

public class MechanicBuff implements ITargetedEntitySkill {
	protected final double amount;
	private final int seconds;
	protected final BuffType type;
	protected final boolean damageBuff, multiplier;

    @Override
    public ThreadSafetyLevel getThreadSafetyLevel() {
        return ThreadSafetyLevel.SYNC_ONLY;
    }

	public MechanicBuff(MythicLineConfig config) {
        this.amount = config.getDouble(new String[] { "a", "amount" }, 0);
        this.seconds = config.getInteger(new String[] {"s", "seconds" }, 1);
        this.type = BuffType.valueOf(config.getString(new String[] {"t", "type"}, "BLUNT").toUpperCase());
        this.damageBuff = config.getBoolean(new String[] { "isDamage", "dmg" }, true);
        this.multiplier = config.getBoolean(new String[] { "m", "mult" }, false);
        
	}

	@Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
		try {
			FightData fd = FightInstance.getFightData(target.getBukkitEntity().getUniqueId());
			if (seconds > 0) {
				fd.addBuff(data.getCaster().getEntity().getUniqueId(), "MM", damageBuff, multiplier, type, amount, seconds);
			}
			else {
				fd.addBuff(data.getCaster().getEntity().getUniqueId(), damageBuff, multiplier, type, amount);
			}
			return SkillResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return SkillResult.ERROR;
		}
    }
}
