package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class LeatherHood extends Equipment {
	private static final String ID = "LeatherHood";
	private int dur, shields;
	
	public LeatherHood(boolean isUpgraded) {
		super(ID, "Leather Hood", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ARMOR);
		dur = isUpgraded ? 15 : 10;
		shields = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.applyStatus(StatusType.STEALTH, data, 1, dur);
		data.addSimpleShield(data.getPlayer().getUniqueId(), shields, 200);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_HELMET, "Start every fight with " + GlossaryTag.STEALTH.tag(this, 1, false)
		+ " [<yellow>" + dur +"s</yellow>] and " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>10s</white>].");
		
		LeatherArmorMeta dye = (LeatherArmorMeta) item.getItemMeta();
		dye.setColor(Color.BLACK);
		item.setItemMeta(dye);
	}
}
