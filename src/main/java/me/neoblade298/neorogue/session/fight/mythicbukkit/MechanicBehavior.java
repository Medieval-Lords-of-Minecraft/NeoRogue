package me.neoblade298.neorogue.session.fight.mythicbukkit;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Particle;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.MobAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class MechanicBehavior implements ITargetedEntitySkill {
	private static HashMap<String, TriggerActionPackage> behaviors = new HashMap<String, TriggerActionPackage>();
	private static final ParticleContainer bkpc = new ParticleContainer(Particle.FLAME).count(10).speed(0.1).offsetY(1);
	protected final String id;

    @Override
    public ThreadSafetyLevel getThreadSafetyLevel() {
        return ThreadSafetyLevel.SYNC_ONLY;
    }

	public MechanicBehavior(MythicLineConfig config) {
		this.id = config.getString("id");
	}

	@Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
		try {
			UUID uuid = target.getBukkitEntity().getUniqueId();
			FightData fd = FightInstance.getFightData(uuid);
			if (fd == null) {
				Bukkit.getLogger().warning("[NeoRogue] No fight data found for entity targeted with behavior " + id);
				return SkillResult.INVALID_TARGET;
			}

			TriggerActionPackage tap = behaviors.get(id);
			if (tap == null) {
				Bukkit.getLogger().warning("[NeoRogue] No mob behavior found for id " + id);
				return SkillResult.INVALID_CONFIG;
			}
			fd.addMobTrigger(tap.trigger, tap.action);
			return SkillResult.SUCCESS;
		} catch (Exception e) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to add mob behavior " + id);
			e.printStackTrace();
			return SkillResult.ERROR;
		}
    }

	private static class TriggerActionPackage {
		public Trigger trigger;
		public MobAction action;

		public TriggerActionPackage(Trigger trigger, MobAction action) {
			this.trigger = trigger;
			this.action = action;
		}
	}

	static {
		behaviors.put("BanditKing", packageBanditKing());
	}

	private static TriggerActionPackage packageBanditKing() {
		Trigger trigger = Trigger.PRE_RECEIVE_STATUS;
		MobAction action = (data, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (ev.isSecondary()) return TriggerResult.keep();
			if (NeoRogue.gen.nextBoolean())	return TriggerResult.keep();
			data.applyStatus(StatusType.STRENGTH, data, 1, -1, null, true);
			Sounds.fire.play(data.getEntity());
			bkpc.play(data.getEntity());
			ev.getStacksBuffList().add(Buff.multiplier(data, -0.5, BuffStatTracker.ignored("BanditKing")));
			return TriggerResult.keep();
		};
		return new TriggerActionPackage(trigger, action);
	}
}
