package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusClass;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class ManaHaze extends Artifact {
	private static final String ID = "ManaHaze";
	private static final int thres = 3;
	private static TargetProperties tp = TargetProperties.radius(8, true, TargetType.ENEMY);

	public ManaHaze() {
		super(ID, "Mana Haze", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(ID, Trigger.APPLY_STATUS, new ManaHazeInstance(id));
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}
	
	private class ManaHazeInstance extends PriorityAction {
		private int stacks = 0;
		private ActionMeta am = new ActionMeta();
		public ManaHazeInstance(String id) {
			super(id);
			action = (pdata, in) -> {
				ApplyStatusEvent ev = (ApplyStatusEvent) in;
				if (ev.getStatusClass() != StatusClass.NEGATIVE) return TriggerResult.keep();
				if (ev.isSecondary()) return TriggerResult.keep();
				if (stacks >= thres && System.currentTimeMillis() >= am.getTime()) {
					LivingEntity trg = TargetHelper.getNearest(ev.getTarget().getEntity(), tp);
					if (trg != null) {
						stacks -= thres;
						am.setTime(System.currentTimeMillis() + 1000); // 1 second cooldown
						FightInstance.applyStatus(trg, ev.getStatus().clone(FightInstance.getFightData(trg)), ev.getStatus().getStacks() / 2, ev.getTicks(), pdata);
					}
				}
				stacks++;
				return TriggerResult.keep();
			};
		}
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BREWER_POTTERY_SHERD, 
				"Every " + DescUtil.white(thres) + " times you apply a negative status, apply half of it to the nearest enemy. <white>1s</white> cooldown.");
	}
}
