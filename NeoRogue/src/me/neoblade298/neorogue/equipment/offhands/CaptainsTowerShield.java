package me.neoblade298.neorogue.equipment.offhands;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;


import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class CaptainsTowerShield extends Equipment {
	private int reduction;
	
	public CaptainsTowerShield(boolean isUpgraded) {
		super("captainsTowerShield", "Captain's Tower Shield", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.OFFHAND);
		reduction = isUpgraded ? 10 : 7;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RAISE_SHIELD, (pdata, inputs) -> {
			HashMap<BuffType, Buff> buffs = new HashMap<BuffType, Buff>();
			buffs.put(BuffType.GENERAL, new Buff(p.getUniqueId(), reduction, 0));
			data.setBarrier(Barrier.centered(p, 4, 2, 2, 0, buffs));
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.SHIELD_TICK, (pdata, inputs) -> {
			data.getBarrier().tick();
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, Trigger.LOWER_SHIELD, (pdata, inputs) -> {
			data.setBarrier(null);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD, "When raised, creates a barrier in front of you of size <yellow>4x3</yellow> "
				+ "that intercepts projectiles. Projectiles that hit the barrier hit you but have their damage reduced by <yellow>"
				+ reduction + "</yellow>.");
	}
}
