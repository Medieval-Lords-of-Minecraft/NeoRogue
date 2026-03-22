package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.GrantShieldsEvent;

public class ForgemastersMark extends Artifact {
	private static final String ID = "ForgemastersMark";
	private double buff = 2;

	public ForgemastersMark() {
		super(ID, "Forgemaster's Mark", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.GRANT_SHIELDS, (pdata, in) -> {
			GrantShieldsEvent ev = (GrantShieldsEvent) in;
			if (ev.isSecondary()) return TriggerResult.remove();
			ev.getAmountBuff().add(Buff.multiplier(data, buff, BuffStatTracker.ignored(this)));
			ev.getDurationBuff().add(Buff.multiplier(data, buff, BuffStatTracker.ignored(this)));
			return TriggerResult.remove();
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
		item = createItem(Material.ANVIL, "The first time you apply "
				+ GlossaryTag.SHIELDS.tag(this) + " in a fight, increase the amount and duration by <white>" + (int) (buff * 100) + "%</white>.");
	}
}
