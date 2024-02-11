package me.neoblade298.neorogue.session.fight.mythicbukkit;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Sound;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillTrigger;
import io.lumine.mythic.api.skills.conditions.ISkillMetaComparisonCondition;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.mechanics.ProjectileMechanic.ProjectileMechanicTracker;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;

public class ConditionHitBarrier implements ISkillMetaComparisonCondition {
	protected Skill skill;

	public ConditionHitBarrier(MythicLineConfig mlc) {
		String skillName = mlc.getString(new String[] { "onHit", "oH" });
		if (skillName != null) {
			skill = MythicBukkit.inst().getSkillManager().getSkill(skillName).get();
		}
	}

	@Override
	public boolean check(SkillMetadata data, AbstractEntity ent) {
		ProjectileMechanicTracker tracker = (ProjectileMechanicTracker) data.getCallingEvent();
		Location loc = BukkitAdapter.adapt(tracker.getCurrentLocation());
		FightData barrierOwner = null;
		for (FightData fd : FightInstance.getUserData().values()) {
			Barrier b = fd.getBarrier();
			checkBarrier(data, b, tracker, loc, barrierOwner);
		}
		for (Barrier b : FightInstance.getUserBarriers().values()) {
			checkBarrier(data, b, tracker, loc);
		}
		return true;
	}
	
	private void checkBarrier(SkillMetadata data, Barrier b, ProjectileMechanicTracker tracker, Location loc) {
		if (b.getOwner() == null) checkBarrier(data, b, tracker, loc, null);
		checkBarrier(data, b, tracker, loc, FightInstance.getFightData(b.getOwner().getUniqueId()));
	}
	
	private void checkBarrier(SkillMetadata data, Barrier b, ProjectileMechanicTracker tracker, Location loc, FightData barrierOwner) {
		if (b == null) return;
		
		if (b.collides(loc)) {
			tracker.projectileEnd();
			tracker.setCancelled();
			loc.getWorld().playSound(loc, Sound.ITEM_SHIELD_BLOCK, 1F, 1F);
		}
		
		if (barrierOwner == null) return;
		
		HashSet<AbstractEntity> targets = new HashSet<AbstractEntity>();
		targets.add(BukkitAdapter.adapt(barrierOwner.getEntity()));
		
		if (skill == null) return;
		skill.execute(SkillTrigger.get("API"), data.getCaster(), data.getTrigger(),
				data.getCaster().getLocation(), targets, null, 1F);
	}
}
