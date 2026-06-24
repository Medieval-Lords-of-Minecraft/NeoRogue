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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Footpads extends Equipment {
	private static final String ID = "Footpads";
	private int stamina, def;
	
	public Footpads(boolean isUpgraded) {
		super(ID, "Footpads", isUpgraded, Rarity.COMMON, new EquipmentClass[] { EquipmentClass.WARRIOR, EquipmentClass.THIEF },
				EquipmentType.ARMOR);
		stamina = isUpgraded ? 15 : 10;
		def = isUpgraded ? 2 : 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.increase(data, def, StatTracker.defenseBuffAlly(UUID.randomUUID().toString(), this)));
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			data.addStamina(stamina);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_BOOTS, "Increase " + GlossaryTag.GENERAL.tag(this) + " defense by " + DescUtil.yellow(def)
				+ ". Receiving damage grants you " + DescUtil.yellow(stamina) + " stamina.");
	}
}
