package me.neoblade298.neorogue.equipment.offhands;

import java.util.UUID;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class MirrorBracer extends Equipment {
	private static final String ID = "MirrorBracer";
	private int reflect, mr;

	public MirrorBracer(boolean isUpgraded) {
		super(ID, "Mirror Bracer", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.OFFHAND);
		reflect = isUpgraded ? 100 : 60;
		mr = isUpgraded ? 3 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		data.applyStatus(StatusType.REFLECT, data, reflect, -1);
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL),
				Buff.increase(data, mr, BuffStatTracker.defenseBuffAlly(buffId, this)));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER, "Passive. Start fights with " + GlossaryTag.REFLECT.tag(this, reflect, true)
				+ ". " + "Also reduces magic damage taken by " + DescUtil.yellow(mr) + ".");
	}
}
