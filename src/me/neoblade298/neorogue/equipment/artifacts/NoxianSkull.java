package me.neoblade298.neorogue.equipment.artifacts;

import java.util.HashMap;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusClass;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class NoxianSkull extends Artifact {
	private static final String ID = "NoxianSkull";

	public NoxianSkull() {
		super(ID, "Noxian Skull", Rarity.COMMON, EquipmentClass.CLASSLESS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(ID, Trigger.PRE_APPLY_STATUS, new NoxianSkullInstance(this, ID));
	}
	
	private class NoxianSkullInstance extends PriorityAction {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		
		public NoxianSkullInstance(Equipment eq, String id) {
			super(id);
			
			action = (pdata, in) -> {
				PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
				if (ev.getStatusClass() == StatusClass.NEGATIVE) {
					int total = map.getOrDefault(ev.getStatusId(), 0) + ev.getStacks();
					int extra = total / 6;
					total = total % 6;
					map.put(ev.getStatusId(), total);
					ev.getStacksBuffList().add(Buff.increase(pdata, extra, BuffStatTracker.ignored(eq)));
				}
				return TriggerResult.keep();
			};
		}
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WITHER_SKELETON_SKULL, 
				"For every <white>6</white> stacks of any negative status you apply, apply <white>1</white> additional stack.");
	}
}
