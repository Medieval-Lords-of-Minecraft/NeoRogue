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
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ShieldsEvent;

public class StolenFence extends Artifact {
	private static final String ID = "StolenFence";
	private final int applications, shields;

	public StolenFence() {
		super(ID, "Stolen Fence", Rarity.COMMON, EquipmentClass.CLASSLESS);
		applications = 5;
		shields = 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		ActionMeta counter = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_GRANT_SHIELDS, (pdata, in) -> {
			ShieldsEvent event = (ShieldsEvent) in;
			if (event.isSecondary()) return TriggerResult.keep();
			event.getAmountBuff().add(Buff.increase(data, shields, BuffStatTracker.shield(id, this)));
			return counter.addCount(1) >= applications ? TriggerResult.remove() : TriggerResult.keep();
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
		item = createItem(Material.OAK_FENCE, "The first " + DescUtil.val(applications)
				+ " times you apply " + GlossaryTag.SHIELDS.tag(this)
				+ " each fight, increase the amount by " + DescUtil.val(shields) + ".");
	}
}