package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;


import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;

public class SmallShield extends Equipment {
	private int reduction;
	
	public SmallShield(boolean isUpgraded) {
		super("smallShield", "Small Shield", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.OFFHAND);
		reduction = isUpgraded ? 5 : 3;
		addReforgeOption("smallShield", "hastyShield", "spikyShield");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, (pdata, inputs) -> {
			if (!p.isHandRaised()) return TriggerResult.keep();
			ReceivedDamageEvent ev = (ReceivedDamageEvent) inputs;
			ev.getMeta().addBuff(BuffType.GENERAL, new Buff(p.getUniqueId(), reduction, 0), BuffOrigin.SHIELD, false);
			p.playSound(p, Sound.ITEM_SHIELD_BLOCK, 1F, 1F);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD, "When raised, reduce all damage taken by <yellow>" + reduction + "</yellow>.");
	}
}
