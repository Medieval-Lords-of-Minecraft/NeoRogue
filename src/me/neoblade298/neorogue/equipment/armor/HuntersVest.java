package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;

public class HuntersVest extends Equipment {
	private static final String ID = "huntersBoots";
	private int reduc;
	
	public HuntersVest(boolean isUpgraded) {
		super(ID, "Hunter's Vest", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ARMOR);
		reduc = isUpgraded ? 4 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, (pdata, in) -> {
			ReceivedDamageEvent ev = (ReceivedDamageEvent) in;
			if (!data.hasStatus(StatusType.FOCUS)) return TriggerResult.keep();
			if (!ev.getMeta().containsType(BuffType.PHYSICAL)) return TriggerResult.keep();
			ev.getMeta().addBuff(BuffType.PHYSICAL, new Buff(data, reduc * data.getStatus(StatusType.FOCUS).getStacks(), 0), BuffOrigin.NORMAL, false);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_CHESTPLATE, "Physical damage taken is reduced by " + DescUtil.yellow(reduc) + " multiplied by stacks of " +
			GlossaryTag.FOCUS.tag(this) + ", up to <white>3</white> stacks.");
	}
}
