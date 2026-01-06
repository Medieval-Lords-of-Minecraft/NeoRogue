package me.neoblade298.neorogue.session.fight.mythicbukkit;

import java.util.Map.Entry;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;

public class MechanicCastAbility implements INoTargetSkill {
    @Override
    public ThreadSafetyLevel getThreadSafetyLevel() {
        return ThreadSafetyLevel.SYNC_ONLY;
    }

	public MechanicCastAbility(MythicLineConfig cfg) {}

	@Override
	public SkillResult cast(SkillMetadata md) {
		try {
			FightData fd = FightInstance.getFightData(md.getCaster().getEntity().getBukkitEntity());
			if (fd.hasStatus(StatusType.ELECTRIFIED)) {
				Status s = fd.getStatus(StatusType.ELECTRIFIED);
				DamageMeta dm = new DamageMeta(s.getSlices().first().getFightData()); // Arbitrarily pick first owner as damage meta owner
				for (Entry<FightData, Integer> slice : fd.getStatus(StatusType.ELECTRIFIED).getSlices().getSliceOwners().entrySet()) {
					dm.addDamageSlice(new DamageSlice(slice.getKey(), slice.getValue() * 0.2, DamageType.ELECTRIFIED, DamageStatTracker.ignored("MythicDamage")));
				}
				FightInstance.dealDamage(dm, fd.getEntity());
			}
			return SkillResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return SkillResult.ERROR;
		}
    }
}
