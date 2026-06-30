package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class LeatherPauldrons extends Equipment {
	private static final String ID = "LeatherPauldrons";
	private int threshold, shields;

	public LeatherPauldrons(boolean isUpgraded) {
		super(ID, "Leather Pauldrons", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		threshold = isUpgraded ? 5 : 6;
		shields = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		int[] count = {0};
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			count[0]++;
			if (count[0] >= threshold) {
				count[0] = 0;
				Player p = data.getPlayer();
				Sounds.equip.play(p, p);
				data.addPermanentShield(p.getUniqueId(), shields, this);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_CHESTPLATE, "After basic attacking " + DescUtil.yellow(threshold) +
				" times, gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + ".");
	}
}
