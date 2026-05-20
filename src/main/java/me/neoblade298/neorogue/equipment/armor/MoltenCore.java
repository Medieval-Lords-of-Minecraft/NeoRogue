package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class MoltenCore extends Equipment {
	private static final String ID = "MoltenCore";
	private int shields;

	public MoltenCore(boolean isUpgraded) {
		super(ID, "Molten Core", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ARMOR);
		shields = isUpgraded ? 8 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.CORRUPTION)) return TriggerResult.keep();
			Player p = data.getPlayer();
			data.addSimpleShield(p.getUniqueId(), shields, 120);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.MAGMA_CREAM, "Every time you apply " + GlossaryTag.CORRUPTION.tag(this)
				+ ", gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [" + DescUtil.white("6s") + "].");
	}
}