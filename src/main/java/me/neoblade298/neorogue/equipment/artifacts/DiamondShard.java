package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
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

public class DiamondShard extends Artifact {
	private static final String ID = "DiamondShard";
	private final int durationSeconds, durationTicks;

	public DiamondShard() {
		super(ID, "Diamond Shard", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		durationSeconds = 3;
		durationTicks = durationSeconds * 20;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.PRE_RECEIVE_SHIELDS, (pdata, in) -> {
			ShieldsEvent event = (ShieldsEvent) in;
			event.getDurationBuff().add(Buff.increase(data, durationTicks, BuffStatTracker.ignored(this)));
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
		item = createItem(Material.DIAMOND,
				"Increase the duration of newly applied " + GlossaryTag.SHIELDS.tag(this) + " by "
				+ DescUtil.val(durationSeconds + "s") + ".");
	}
}