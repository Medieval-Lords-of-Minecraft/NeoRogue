package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
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
	private static final String ID = "manaHaze";
	private static final int thres = 60;
	private static TargetProperties tp = TargetProperties.radius(8, true, TargetType.ENEMY);

	public ManaHaze() {
		super(ID, "Mana Haze", Rarity.COMMON, EquipmentClass.CLASSLESS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
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
		public ManaHazeInstance(String id) {
			super(id);
			action = (pdata, in) -> {
				ApplyStatusEvent ev = (ApplyStatusEvent) in;
				if (ev.getStatusClass() != StatusClass.NEGATIVE) return TriggerResult.keep();
				if (ev.isSecondary()) return TriggerResult.keep();
				if (stacks >= thres) {
					LivingEntity trg = TargetHelper.getNearest(ev.getTarget().getEntity(), tp);
					if (trg != null) {
						stacks -= thres;
						FightInstance.applyStatus(trg, ev.getStatus().clone(FightInstance.getFightData(trg)), 10, ev.getTicks(), pdata);
					}
				}
				stacks += ev.getStacks();
				return TriggerResult.keep();
			};
		}
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BREWER_POTTERY_SHERD, 
				"Every " + DescUtil.white(thres) + " stacks of negative statuses you apply, your next negative status applied to a target will also have " +
				"<white>10</white> stacks of it applied to the nearest enemy to the target.");
	}
}
