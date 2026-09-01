package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreCastUsableEvent;

public class GryphonClaw extends Equipment {
	private static final String ID = "GryphonClaw";
	private int shieldsPerFocus, cooldownReduction;
	private int focusThreshold = 5;

	public GryphonClaw(boolean isUpgraded) {
		super(ID, "Gryphon Claw", isUpgraded, Rarity.EPIC, EquipmentClass.ARCHER, EquipmentType.ACCESSORY,
				EquipmentProperties.none());
		shieldsPerFocus = isUpgraded ? 6 : 4;
		cooldownReduction = isUpgraded ? 30 : 20;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (!event.isStatus(StatusType.FOCUS) || event.getStacks() <= 0) return TriggerResult.keep();
			Player player = data.getPlayer();
			data.addPermanentShield(player.getUniqueId(), shieldsPerFocus * event.getStacks(), this);
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.PRE_CAST_USABLE, (pdata, in) -> {
			if (data.getStatus(StatusType.FOCUS).getStacks() <= focusThreshold) return TriggerResult.keep();
			PreCastUsableEvent event = (PreCastUsableEvent) in;
			if (event.getInstance().getEquipment().getType() != EquipmentType.ABILITY) return TriggerResult.keep();
			event.addBuff(PropertyType.COOLDOWN, id + slot, Buff.multiplier(data, cooldownReduction * 0.01,
					BuffStatTracker.of(id + slot, this, PropertyType.COOLDOWN.getDisplay() + " reduced")));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RABBIT_FOOT, "Gain " + GlossaryTag.SHIELDS.tag(this, shieldsPerFocus)
				+ " per " + GlossaryTag.FOCUS.tag(this) + " gained. While above " + DescUtil.val(focusThreshold)
				+ " Focus, reduce ability cooldowns by " + DescUtil.val(cooldownReduction + "%") + ".");
	}
}