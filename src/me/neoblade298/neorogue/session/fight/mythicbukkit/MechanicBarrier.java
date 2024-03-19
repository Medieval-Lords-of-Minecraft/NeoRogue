package me.neoblade298.neorogue.session.fight.mythicbukkit;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillManager;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;

public class MechanicBarrier implements ITargetedEntitySkill {
	protected final Skill counterSkill, hitSkill;
	protected final double width, forward, forwardOffset, height, rotateY;
	protected final int duration;
	protected final HashMap<BuffType, Double> buffs = new HashMap<BuffType, Double>();
	protected final String id;
	
	protected static final HashMap<String, UUID> barrierIds = new HashMap<String, UUID>();

    @Override
    public ThreadSafetyLevel getThreadSafetyLevel() {
        return ThreadSafetyLevel.SYNC_ONLY;
    }

	public MechanicBarrier(MythicLineConfig config) {
		width = config.getDouble(new String[] { "w", "width" }, 2);
		height = config.getDouble(new String[] { "h", "height" }, 3);
		forward = config.getDouble(new String[] { "f", "forward" }, 2);
		forwardOffset = config.getDouble(new String[] { "fo", "forwardoffset" }, 2);
		rotateY = config.getDouble(new String[] { "ry", "rotatey" }, 2);
		duration = config.getInteger(new String[] { "d", "duration" }, 40);
		id = config.getString("id");
		
		SkillManager sm = MythicBukkit.inst().getSkillManager();
		Optional<Skill> temp = sm.getSkill(config.getString(new String[] { "cs", "counterskill" }, null));
		if (temp.isPresent()) counterSkill = temp.get();
		else counterSkill = null;
		
		temp = sm.getSkill(config.getString(new String[] { "hs", "hitskill" }, null));
		if (temp.isPresent()) hitSkill = temp.get();
		else hitSkill = null;
		
		for (BuffType type : BuffType.values()) {
			double amt = config.getDouble(type.name(), 0);
			
			if (amt != 0) buffs.put(type, amt);
		}
	}

	@Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
		try {
			Entity owner = target.getBukkitEntity();
			FightData fd = FightInstance.getFightData(owner.getUniqueId());
			if (fd == null) return SkillResult.INVALID_TARGET;
			HashMap<BuffType, Buff> buffs = new HashMap<BuffType, Buff>();
			for (Entry<BuffType, Double> ent : this.buffs.entrySet()) {
				buffs.put(ent.getKey(), new Buff(fd, 0, ent.getValue()));
			}
			LivingEntity ent = (LivingEntity) data.getCaster().getEntity().getBukkitEntity();
			Barrier b = Barrier.centered(ent, width, forward, height, forwardOffset, buffs);
			fd.getInstance().addBarrier(fd, b, duration, false);
			if (id != null) barrierIds.put(id, b.getUniqueId());
			return SkillResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return SkillResult.ERROR;
		}
    }
}
