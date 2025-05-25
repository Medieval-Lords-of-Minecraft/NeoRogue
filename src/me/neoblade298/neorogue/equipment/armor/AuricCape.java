package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

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
	private static final String ID = "auricCape";
	private int reduc;

	public AuricCape(boolean isUpgraded) {
		super(ID, "Auric Cape", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ARMOR);
		reduc = isUpgraded ? 8 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (pdata.getMana() > pdata.getMaxMana() / 2) {
				data.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL),
						Buff.increase(data, reduc, BuffStatTracker.defenseBuffAlly(this)), 20);
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
