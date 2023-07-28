package me.neoblade298.neorogue.equipment.offhands;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Offhand;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.Buff;
import me.neoblade298.neorogue.session.fights.BuffType;
import me.neoblade298.neorogue.session.fights.FightData;

public class RicketyShield extends Offhand {
	
	public RicketyShield(boolean isUpgraded) {
		super("ricketyShield", isUpgraded, Rarity.COMMON);
		display = "Rickety Shield";
		item = Offhand.createItem(this, Material.SHIELD, null, "When raised, creates a barrier in front of you of size &e3x3 &7"
				+ "that intercepts projectiles. Projectiles that hit the barrier hit you but have their damage reduced by &e5&7.");
	}

	@Override
	public void initialize(Player p, FightData data, Trigger bind, int hotbar) {
		data.addTrigger(id, Trigger.RAISE_SHIELD, (inputs) -> {
			HashMap<BuffType, Buff> buffs = new HashMap<BuffType, Buff>();
			buffs.put(BuffType.GENERAL, new Buff(p.getUniqueId(), 5, 0));
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
