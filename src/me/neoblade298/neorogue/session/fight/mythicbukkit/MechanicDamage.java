package me.neoblade298.neorogue.session.fight.mythicbukkit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
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
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;

public class MechanicDamage implements ITargetedEntitySkill {
	protected final boolean hitBarrier, asParent, debug;
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
		this.debug = cfg.getBoolean("debug", false);

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
			double level = data.getCaster().getLevel(); // From Session#getLevel()

			// Currently assumes caster is always a mythicmob
			ActiveMob am = MythicBukkit.inst().getMobManager().getMythicMobInstance(data.getCaster().getEntity());
			if (asParent && !am.getParent().isPresent()) {
				if (debug) Bukkit.getLogger().info("[NeoRogue] Nrdamage failed, tried to deal damage as parent of mob, parent doesn't exist");
				return SkillResult.CONDITION_FAILED;
			}
			FightData fd = asParent ? FightInstance.getFightData(am.getParent().get().getBukkitEntity())
					: FightInstance.getFightData(data.getCaster().getEntity().getUniqueId());
			DamageMeta meta = new DamageMeta(fd);
			final double mult = 1 + (level * (0.05 + fd.getInstance().getSession().getEnemyDamageScale()));
			for (Entry<DamageType, Double> ent : damage.entrySet()) {
				meta.addDamageSlice(new DamageSlice(fd, ent.getValue() * mult, ent.getKey(), DamageStatTracker.ignored("MythicDamage1")));
			}
			meta.setHitBarrier(hitBarrier);
			double dealt = FightInstance.dealDamage(meta, (LivingEntity) target.getBukkitEntity(), data);

			HashSet<AbstractEntity> targets = new HashSet<AbstractEntity>();
			targets.add(target);

			Skill skill;
			if (dealt > 0) {
				skill = successSkill;
				if (debug) Bukkit.getLogger().info("[NeoRogue] Nrdamage dealt " + dealt + " damage to " + target.getBukkitEntity().getName());
			} else {
				skill = failSkill;
				if (debug) Bukkit.getLogger().info("[NeoRogue] Nrdamage dealt 0 damage to " + target.getBukkitEntity().getName());
			}
			if (skill != null)
				skill.execute(SkillTrigger.get("API"), data.getCaster(), data.getTrigger(),
						data.getCaster().getLocation(), targets, null, 1F);

			if (fd.hasStatus(StatusType.ELECTRIFIED)) {
				Status s = fd.getStatus(StatusType.ELECTRIFIED);
				DamageMeta dm = new DamageMeta(s.getSlices().first().getFightData()); // Arbitrarily pick first owner as
																						// damage meta owner
				for (Entry<FightData, Integer> slice : fd.getStatus(StatusType.ELECTRIFIED).getSlices().getSliceOwners()
						.entrySet()) {
					dm.addDamageSlice(new DamageSlice(slice.getKey(), slice.getValue() * 0.2, DamageType.ELECTRIFIED,
							DamageStatTracker.ignored("MythicDamage2")));
				}
			}
			return SkillResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return SkillResult.ERROR;
		}
	}
}
