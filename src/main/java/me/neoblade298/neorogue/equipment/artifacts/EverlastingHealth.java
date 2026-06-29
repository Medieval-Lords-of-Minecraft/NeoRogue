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
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			am.addCount(1);
			if (am.getCount() < DELAY) return TriggerResult.keep();
			data.addTrigger(id, Trigger.RECEIVE_SHIELDS, (pd, ev) -> {
				return TriggerResult.of(false, true);
			});
			return TriggerResult.remove();
		});
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		data.healPercent(0.20);
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {

	}

	@Override
	public void setupItem() {
		item = createItem(Material.HONEY_BOTTLE,
				"On acquire, heals you for " + DescUtil.white("20%") + " of your max health. Disables your ability to gain "
						+ GlossaryTag.SHIELDS.tag(this) + " after " + DescUtil.white(DELAY + "s") + " of a fight.");
	}
}
