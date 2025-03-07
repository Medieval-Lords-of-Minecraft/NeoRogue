package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class VoidBracelet extends Equipment {
	private static final String ID = "voidBracelet";
	private int inc;
	public VoidBracelet(boolean isUpgraded) {
		super(ID, "Void Bracelet", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY);
		inc = isUpgraded ? 25 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addRift(new Rift(data, p.getLocation(), 200));

		data.addTrigger(id, Trigger.CREATE_RIFT, (pdata, in) -> {
			data.addDamageBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.increase(data, inc, StatTracker.damageBuffAlly(this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_NUGGET, "Every <white>10s</white>, drop a " + GlossaryTag.RIFT.tag(this) + " [<white>5s</white>]. Increase your " +
			GlossaryTag.MAGICAL.tag(this) + " damage by " + DescUtil.yellow(inc) + " for every rift you have created.");
	}
}
