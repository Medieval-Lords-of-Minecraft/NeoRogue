package me.neoblade298.neorogue.session.fight.mythicbukkit;

import java.util.HashSet;
import java.util.Set;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class MechanicCooldown implements ITargetedEntitySkill {
	protected final double seconds;
	protected final Set<String> equipIds;

	@Override
	public ThreadSafetyLevel getThreadSafetyLevel() {
		return ThreadSafetyLevel.SYNC_ONLY;
	}

	public MechanicCooldown(MythicLineConfig cfg) {
		this.seconds = cfg.getDouble(new String[] { "s", "seconds" }, 0);
		String idStr = cfg.getString(new String[] { "id" }, "").trim();
		if (idStr.isEmpty()) {
			this.equipIds = null;
		} else {
			this.equipIds = new HashSet<>();
			for (String part : idStr.split(",")) {
				String trimmed = part.trim();
				if (!trimmed.isEmpty()) {
					this.equipIds.add(trimmed);
				}
			}
		}
	}

	@Override
	public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
		try {
			PlayerFightData pfd = FightInstance.getUserData(target.getBukkitEntity().getUniqueId());
			if (pfd == null) return SkillResult.INVALID_TARGET;

			for (EquipmentInstance ei : pfd.getActiveEquipment().values()) {
				if (equipIds == null) {
					// Apply to all instances currently on cooldown
					if (ei.getCooldownSeconds() > 0) {
						ei.reduceCooldown(seconds);
					}
				} else {
					// Apply only to the specified equipment IDs
					if (equipIds.contains(ei.getEquipment().getId())) {
						ei.reduceCooldown(seconds);
					}
				}
			}
			return SkillResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return SkillResult.ERROR;
		}
	}
}
