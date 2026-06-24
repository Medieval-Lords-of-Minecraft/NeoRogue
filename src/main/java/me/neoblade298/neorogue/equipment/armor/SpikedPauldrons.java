package me.neoblade298.neorogue.equipment.armor;
import java.util.UUID;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class SpikedPauldrons extends Equipment {
	private static final String ID = "SpikedPauldrons";
	private int thorns, damageReduction = 1;
	
	public SpikedPauldrons(boolean isUpgraded) {
		super(ID, "Spiked Pauldrons", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		thorns = isUpgraded ? 30 : 20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.applyStatus(StatusType.THORNS, data, thorns, -1);
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.increase(data, damageReduction, StatTracker.defenseBuffAlly(UUID.randomUUID().toString(), this)));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARMOR_STAND, "Reduce all damage taken by " + DescUtil.yellow(damageReduction) + ". Start every fight with " + DescUtil.yellow(thorns) + " " + GlossaryTag.THORNS.tag(this) + ".");
	}
}
