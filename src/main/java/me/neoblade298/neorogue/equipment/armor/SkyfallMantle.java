package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class SkyfallMantle extends Equipment {
	private static final String ID = "SkyfallMantle";
	private int threshold, statusStacks = 1;

	public SkyfallMantle(boolean isUpgraded) {
		super(ID, "Skyfall Mantle", isUpgraded, Rarity.EPIC, EquipmentClass.ARCHER, EquipmentType.ARMOR);
		threshold = isUpgraded ? 8 : 10;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta shots = new ActionMeta();
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			LaunchProjectileGroupEvent event = (LaunchProjectileGroupEvent) in;
			if (!event.isAftershot()) return TriggerResult.keep();
			shots.addCount(event.getInstances().size());
			while (shots.getCount() >= threshold) {
				shots.addCount(-threshold);
				data.applyStatus(StatusType.PROTECT, data, statusStacks, -1, this);
				data.applyStatus(StatusType.SHELL, data, statusStacks, -1, this);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ELYTRA, "For every " + DescUtil.val(threshold) + " "
				+ GlossaryTag.AFTERSHOT.tagPlural(this) + " fired, gain "
				+ GlossaryTag.PROTECT.tag(this, statusStacks) + " and " + GlossaryTag.SHELL.tag(this, statusStacks) + ".");
	}
}