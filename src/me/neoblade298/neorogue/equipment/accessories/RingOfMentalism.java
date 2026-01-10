package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class RingOfMentalism extends Equipment {
	private static final String ID = "RingOfMentalism";
	private int stacks;
	
	public RingOfMentalism(boolean isUpgraded) {
		super(ID, "Ring Of Mentalism", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ACCESSORY);
		stacks = isUpgraded ? 45 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(ID, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().containsType(DamageCategory.MAGICAL)) return TriggerResult.keep();
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
