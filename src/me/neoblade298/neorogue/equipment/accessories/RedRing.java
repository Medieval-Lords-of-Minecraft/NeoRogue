package me.neoblade298.neorogue.equipment.accessories;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class RedRing extends Equipment {
	private static final String ID = "redRing";
	private int inc;
	public RedRing(boolean isUpgraded) {
		super(ID, "Red Ring", isUpgraded, Rarity.COMMON, new EquipmentClass[] { EquipmentClass.ARCHER, EquipmentClass.MAGE },
				EquipmentType.ACCESSORY);
		inc = isUpgraded ? 25 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		data.addDamageBuff(DamageBuffType.of(DamageCategory.FIRE), Buff.multiplier(data, inc * 0.01, StatTracker.damageBuffAlly(buffId, this)));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_NUGGET, GlossaryTag.FIRE.tag(this) + " damage is increased by " + DescUtil.yellow(inc + "%") + ".");
	}
}
