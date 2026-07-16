package me.neoblade298.neorogue.session.fight.mythicbukkit;

import org.bukkit.entity.Player;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.conditions.ISkillMetaComparisonCondition;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class ConditionIsNeoRoguePlayer implements ISkillMetaComparisonCondition {
	protected Skill skill;

	@Override
	public boolean check(SkillMetadata data, AbstractEntity ent) {
		try {
			if (!(ent.getBukkitEntity() instanceof Player)) return false;
			Player p = (Player) ent.getBukkitEntity();
			PlayerFightData pfd = FightInstance.getUserData(p.getUniqueId());
			if (pfd == null) return false;
			return !pfd.isDead();
		}
		catch (IllegalStateException e) {
			e.printStackTrace();
		}
		return false;
	}
}
