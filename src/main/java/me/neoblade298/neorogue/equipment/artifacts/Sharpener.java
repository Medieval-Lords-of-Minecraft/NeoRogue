package me.neoblade298.neorogue.equipment.artifacts;

import java.util.UUID;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class Sharpener extends Artifact {
	private static final String ID = "Sharpener";
	private final double damageIncrease;

	public Sharpener() {
		super(ID, "Sharpener", Rarity.UNCOMMON, EquipmentClass.THIEF);
		damageIncrease = 0.01;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		String buffId = UUID.randomUUID().toString();
		data.addTrigger(id, Trigger.RECEIVE_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (!event.isStatus(StatusType.STEALTH) || event.getStacks() <= 0) return TriggerResult.keep();
			data.addDamageBuff(DamageBuffType.of(DamageCategory.DIRECT),
					Buff.multiplier(data, damageIncrease, StatTracker.damageBuffAlly(buffId, this, true)));
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
		item = createItem(Material.FLINT,
				"Whenever you gain " + GlossaryTag.STEALTH.tag(this) + ", increase your "
				+ GlossaryTag.DIRECT.tag(this) + " damage by " + DescUtil.val("1%") + " for the fight. Stacks.");
	}
}