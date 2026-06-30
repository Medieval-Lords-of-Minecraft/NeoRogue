package me.neoblade298.neorogue.equipment.offhands;

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

public class Buckler extends Equipment {
	private static final String ID = "Buckler";
	private int threshold, shields;

	public Buckler(boolean isUpgraded) {
		super(ID, "Buckler", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.OFFHAND);
		threshold = isUpgraded ? 4 : 5;
		shields = isUpgraded ? 6 : 4;
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
				data.addSimpleShield(p.getUniqueId(), shields, 60, this);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD, "Every " + DescUtil.yellow(threshold) + " basic attacks, gain " +
				GlossaryTag.SHIELDS.tag(this, shields, true) + " " + DescUtil.duration(3, false) + ".");
	}
}
