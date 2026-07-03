package me.neoblade298.neorogue.equipment.armor;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import java.util.UUID;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class BootsOfSpeed extends Equipment {
	private static final String ID = "BootsOfSpeed";
	private static final int DAMAGE_REDUCTION = 1;
	private double stamina;
	
	public BootsOfSpeed(boolean isUpgraded) {
		super(ID, "Boots of Speed", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ARMOR);
		stamina = isUpgraded ? 1.5 : 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addSprintCost(-stamina);
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL),
				Buff.increase(data, DAMAGE_REDUCTION, StatTracker.defenseBuffAlly(UUID.randomUUID().toString(), this)));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_BOOTS, "Reduces sprint stamina cost by " + DescUtil.yellow(stamina)
				+ ". Reduces damage taken by " + DescUtil.white(DAMAGE_REDUCTION) + ".");
	}
}
