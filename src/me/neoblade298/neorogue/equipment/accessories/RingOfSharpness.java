package me.neoblade298.neorogue.equipment.accessories;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class RingOfSharpness extends Equipment {
	private static final String ID = "RingOfSharpness";
	private int buff;
	
	public RingOfSharpness(boolean isUpgraded) {
		super(ID, "Ring Of Sharpness", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ACCESSORY);
		buff = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		data.addDamageBuff(DamageBuffType.of(DamageCategory.PIERCING), Buff.increase(data, buff * 0.01, StatTracker.damageBuffAlly(buffId, this)));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_SHARD, GlossaryTag.PIERCING.tag(this) + " damage is increased by <yellow>" + buff + "%</yellow>.");
	}
}
