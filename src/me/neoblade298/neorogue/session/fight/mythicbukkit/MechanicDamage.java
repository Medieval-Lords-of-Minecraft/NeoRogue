package me.neoblade298.neorogue.session.fight.mythicbukkit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

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
import io.lumine.mythic.core.mobs.ActiveMob;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;

public class MechanicDamage implements ITargetedEntitySkill {
	protected final boolean hitBarrier, asParent;
	protected final HashMap<DamageType, Double> damage = new HashMap<DamageType, Double>();
	protected Skill successSkill, failSkill;

    @Override
    public ThreadSafetyLevel getThreadSafetyLevel() {
        return ThreadSafetyLevel.SYNC_ONLY;
    }

	public MechanicDamage(MythicLineConfig cfg) {
		for (DamageType type : DamageType.values()) {
			double amt = cfg.getDouble(type.name(), 0);
			if (amt > 0) {
				damage.put(type, amt);
			}
		}
        this.hitBarrier = cfg.getBoolean(new String[] { "hb", "hitbarrier" }, false);
		this.asParent = cfg.getBoolean(new String[] { "asParent", "ap" }, false);

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
			final double mult = 1 + (level / 10);
			ActiveMob am = MythicBukkit.inst().getMobManager().getMythicMobInstance(data.getCaster().getEntity()); // Currently assumes caster is always mythicmob
			if (asParent && !am.getParent().isPresent()) {
				return SkillResult.CONDITION_FAILED;
			}
			FightData fd = asParent ? FightInstance.getFightData(am.getParent().get().getBukkitEntity()) : FightInstance.getFightData(data.getCaster().getEntity().getUniqueId());
			DamageMeta meta = new DamageMeta(fd);
			for (Entry<DamageType, Double> ent : damage.entrySet()) {
				meta.addDamageSlice(new DamageSlice(fd, ent.getValue() * mult, ent.getKey()));
			}
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
