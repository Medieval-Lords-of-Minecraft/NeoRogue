package me.neoblade298.neorogue.equipment.armor;

import java.util.UUID;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class AuricCape extends Equipment {
	private static final String ID = "AuricCape";
	private int reduc;

	public AuricCape(boolean isUpgraded) {
		super(ID, "Auric Cape", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ARMOR);
		reduc = isUpgraded ? 8 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		BuffStatTracker tr = BuffStatTracker.defenseBuffAlly(buffId, this, false);
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (pdata.getMana() > pdata.getMaxMana() / 2) {
				// This buff doesn't have a duration because we can just set it to 0 when mana is below 50%
				data.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL),
						Buff.increase(data, reduc, tr));
			}
			else {
				data.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL),
						Buff.empty(data, tr));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ELYTRA,
				"While above <white>50%</white> mana, reduce all incoming damage by <yellow>" + reduc + "</yellow>.");
	}
}
