package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class RingOfMentalism extends Equipment {
	private static final String ID = "ringOfMentalism";
	private int stacks;
	
	public RingOfMentalism(boolean isUpgraded) {
		super(ID, "Ring Of Mentalism", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ACCESSORY);
		stacks = isUpgraded ? 25 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(ID, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			DealtDamageEvent ev = (DealtDamageEvent) in;
			if (!ev.getMeta().containsType(BuffType.MAGICAL)) return TriggerResult.keep();
			FightInstance.applyStatus(ev.getTarget(), StatusType.INSANITY, data, stacks, -1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_SHARD, "Dealing " + GlossaryTag.MAGICAL.tag(this) + " damage to an enemy applies "
				+ GlossaryTag.INSANITY.tag(this, stacks, true) + " to them.");
	}
}
