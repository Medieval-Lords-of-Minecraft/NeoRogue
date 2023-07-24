package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.Rectangle;
import me.neoblade298.neorogue.equipment.Offhand;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Weapon;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.FightInstance;

public class RicketyShield extends Offhand {
	
	public RicketyShield(boolean isUpgraded) {
		super("ricketyShield", isUpgraded, Rarity.COMMON);
		display = "Rickety Shield";
		item = Offhand.createItem(this, Material.SHIELD, null, null);
	}

	@Override
	public void initialize(Player p, FightData data, FightInstance inst, Trigger bind) {
		data.addTrigger(id, Trigger.RAISE_SHIELD, (inputs) -> {
			data.setBarrier(new Barrier(p, 0, 0, 0));
			return true;
		});
	}
}
