package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class MirageTalisman extends Equipment {
	private static final String ID = "MirageTalisman";
	private static final int SHIELD_DURATION = 100;
	private final int shields;

	public MirageTalisman(boolean isUpgraded) {
		super(ID, "Mirage Talisman", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.ACCESSORY);
		shields = isUpgraded ? 3 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.DASH, (pdata, in) -> {
			grantShields(data);
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.EVADE, (pdata, in) -> {
			grantShields(data);
			return TriggerResult.keep();
		});
	}

	private void grantShields(PlayerFightData data) {
		data.addSimpleShield(data.getPlayer().getUniqueId(), shields, SHIELD_DURATION, this);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_SHARD, GlossaryTag.DASH.tag(this) + "ing or evading grants "
				+ GlossaryTag.SHIELDS.tag(this, shields) + " " + DescUtil.duration(SHIELD_DURATION / 20) + ".");
	}
}