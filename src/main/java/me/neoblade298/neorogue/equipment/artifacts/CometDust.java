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
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class CometDust extends Artifact {
	private static final String ID = "CometDust";
	private final int castsRequired, riftDurationTicks;

	public CometDust() {
		super(ID, "Comet Dust", Rarity.UNCOMMON, EquipmentClass.MAGE);
		castsRequired = 8;
		riftDurationTicks = 200;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		ActionMeta casts = new ActionMeta();
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			if (casts.addCount(1) >= castsRequired) {
				casts.addCount(-castsRequired);
				data.addRift(new Rift(data, data.getPlayer().getLocation(), riftDurationTicks, this));
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
		item = createItem(Material.AMETHYST_SHARD,
				"Every " + DescUtil.val(castsRequired) + " ability casts, drop a "
				+ GlossaryTag.RIFT.tag(this) + " [" + DescUtil.val("10s") + "].");
	}
}