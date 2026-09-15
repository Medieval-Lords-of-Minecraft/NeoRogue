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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BladeFragment extends Artifact {
	private static final String ID = "BladeFragment";
	private final int attacksRequired, strength;

	public BladeFragment() {
		super(ID, "Blade Fragment", Rarity.COMMON, EquipmentClass.WARRIOR);
		attacksRequired = 12;
		strength = 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		ActionMeta attacks = new ActionMeta();
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			if (attacks.addCount(1) >= attacksRequired) {
				attacks.addCount(-attacksRequired);
				data.applyStatus(StatusType.STRENGTH, data, strength, -1, this);
			}
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
		item = createItem(Material.IRON_NUGGET,
				"After every " + DescUtil.val(attacksRequired) + " basic attacks, gain "
				+ GlossaryTag.STRENGTH.tag(this, strength) + " for the fight.");
	}
}