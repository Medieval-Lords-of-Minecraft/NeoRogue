package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Weapon;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.FightInstance;

public class WoodenSword extends Weapon {
	
	public WoodenSword(boolean isUpgraded) {
		super("woodenSword", isUpgraded, Rarity.COMMON);
		display = "Wooden Sword";
		damage = isUpgraded ? 3.5 : 4;
		attackSpeed = 1;
		item = Weapon.createItem(this, Material.WOODEN_SWORD, null, null);
	}

	@Override
	public void initialize(Player p, FightData data, FightInstance inst, Trigger bind) {
		data.addTrigger(id, Trigger.LEFT_CLICK_HIT, (inputs) -> {
			inst.dealDamage(p, DamageType.SLASHING, damage, ((Damageable) inputs[1]));
			return true;
		});
	}
}
