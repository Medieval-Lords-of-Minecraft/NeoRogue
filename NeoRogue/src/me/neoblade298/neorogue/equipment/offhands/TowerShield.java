package me.neoblade298.neorogue.equipment.offhands;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Offhand;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.Buff;
import me.neoblade298.neorogue.session.fights.BuffType;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class TowerShield extends Offhand {
	private int reduction;
	
	public TowerShield(boolean isUpgraded) {
		super("towerShield", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		display = "Tower Shield";
		reduction = isUpgraded ? 6 : 4;
		item = createItem(this, Material.SHIELD, null, "When raised, creates a barrier in front of you of size <yellow>3x3</yellow> "
				+ "that intercepts projectiles. Projectiles that hit the barrier hit you but have their damage reduced by <yellow>" + reduction + "</yellow>.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, Trigger.RAISE_SHIELD, (inputs) -> {
			HashMap<BuffType, Buff> buffs = new HashMap<BuffType, Buff>();
			buffs.put(BuffType.GENERAL, new Buff(p.getUniqueId(), reduction, 0));
			data.setBarrier(new Barrier(p, 2, 3, 3, 0, buffs, false));
			return true;
		});

		data.addTrigger(id, Trigger.SHIELD_TICK, (inputs) -> {
			data.getBarrier().tick();
			return true;
		});
		
		data.addTrigger(id, Trigger.LOWER_SHIELD, (inputs) -> {
			data.setBarrier(null);
			return true;
		});
	}
}
