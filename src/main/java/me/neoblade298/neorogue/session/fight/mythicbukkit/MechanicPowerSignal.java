package me.neoblade298.neorogue.session.fight.mythicbukkit;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import io.lumine.mythic.bukkit.BukkitAdapter;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class MechanicPowerSignal implements ITargetedEntitySkill {
	private final double seconds;

	@Override
	public ThreadSafetyLevel getThreadSafetyLevel() {
		return ThreadSafetyLevel.SYNC_ONLY;
	}

	public MechanicPowerSignal(MythicLineConfig config) {
		this.seconds = config.getDouble(new String[] { "s", "seconds" }, 5.0);
	}

	@Override
	public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
		try {
			if (!(target.getBukkitEntity() instanceof Player)) return SkillResult.CONDITION_FAILED;
			Player p = (Player) target.getBukkitEntity();
			PlayerFightData pdata = FightInstance.getUserData(p.getUniqueId());
			if (pdata == null) return SkillResult.CONDITION_FAILED;

			LivingEntity caster = (LivingEntity) data.getCaster().getEntity().getBukkitEntity();
			long startTime = System.currentTimeMillis();
			long durationMs = (long) (seconds * 1000);
			String id = "nrpowersignal_" + caster.getUniqueId();

			PriorityAction action = new PriorityAction(id, (pfd, in) -> {
				if (System.currentTimeMillis() - startTime > durationMs) {
					return TriggerResult.remove();
				}
				if (NeoRogue.mythicApi.isMythicMob(caster)) {
					NeoRogue.mythicApi.getMythicMobInstance(caster)
							.signalMob(BukkitAdapter.adapt(pfd.getPlayer()), "ACTIVATE_POWER");
				}
				return TriggerResult.keep();
			});
			action.setPriority(1);
			pdata.addTrigger(id, Trigger.PRE_ACTIVATE_POWER, action);
			return SkillResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return SkillResult.ERROR;
		}
	}
}
