package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealtDamageEvent;

public class RedRing extends Equipment {
	private static final String ID = "redRing";
	private double inc;
	public RedRing(boolean isUpgraded) {
		super(ID, "Red Ring", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		inc = isUpgraded ? 25 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PRE_DEALT_DAMAGE, (pdata, in) -> {
			PreDealtDamageEvent ev = (PreDealtDamageEvent) in;
			if (!ev.getMeta().containsType(BuffType.FIRE)) return TriggerResult.remove();
			ev.getMeta().addBuff(BuffType.FIRE, new Buff(data, 0, inc), BuffOrigin.NORMAL, true);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_NUGGET, GlossaryTag.FIRE.tag(this) + " damage is increased by " + DescUtil.yellow(inc) + ".");
	}
}
