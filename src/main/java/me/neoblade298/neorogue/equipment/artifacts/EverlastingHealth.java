package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class EverlastingHealth extends Artifact {
	private static final String ID = "EverlastingHealth";
	private static final int DELAY = 10; // seconds before shields are disabled

	public EverlastingHealth() {
		super(ID, "Everlasting Health", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		canDrop = false;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		PriorityAction pa = new PriorityAction(id, (pfd, in) -> {
			return TriggerResult.cancel();
		});
		pa.setPriority(0);

		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			am.addCount(1);
			if (am.getCount() < DELAY) return TriggerResult.keep();
			data.addTrigger(id, Trigger.RECEIVE_SHIELDS, pa);
			return TriggerResult.remove();
		});

		data.addTrigger(id, Trigger.WIN_FIGHT, (pdata, in) -> {
			data.addHealth(data.getMaxHealth() * 0.2, this);
			return TriggerResult.keep();
		});
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {

	}

	@Override
	public void setupItem() {
		item = createItem(Material.HONEY_BOTTLE,
				"Disables your ability to gain "
						+ GlossaryTag.SHIELDS.tag(this) + " after " + DescUtil.white(DELAY + "s") + " of a fight. "
						+ "Heals you for " + DescUtil.white("20%") + " of your max health on winning a fight.");
	}
}
