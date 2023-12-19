package me.neoblade298.neorogue.session.fight.mythicbukkit;

import org.bukkit.entity.Damageable;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;

public class MechanicDamage implements ITargetedEntitySkill {
	protected final int amount;
	protected final DamageType type;
	protected final boolean hitBarrier;

    @Override
    public ThreadSafetyLevel getThreadSafetyLevel() {
        return ThreadSafetyLevel.SYNC_ONLY;
    }

	public MechanicDamage(MythicLineConfig config) {
        this.amount = config.getInteger(new String[] { "a", "amount" }, 0);
        this.type = DamageType.valueOf(config.getString(new String[] {"t", "type"}, "BLUNT").toUpperCase());
        this.hitBarrier = config.getBoolean(new String[] { "hb", "hitbarrier" }, false);
	}

	@Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
		try {
			double level = data.getCaster().getLevel();
			final double fAmount = level > 5 ? amount * (level / 5) : amount;
			DamageMeta meta = new DamageMeta(fAmount, type);
			meta.setHitBarrier(hitBarrier);
			FightInstance.dealDamage((Damageable) data.getCaster().getEntity().getBukkitEntity(), meta, (Damageable) target.getBukkitEntity());
			return SkillResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return SkillResult.ERROR;
		}
    }
}
