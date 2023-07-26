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

public class Rapier extends Weapon {
	
	public Rapier(boolean isUpgraded) {
		super("rapier", isUpgraded, Rarity.UNCOMMON);
		display = "Rapier";
		damage = isUpgraded ? 12 : 9;
		attackSpeed = 1;
		int shields = isUpgraded ? 15 : 15;
		item = Weapon.createItem(this, Material.STONE_SWORD, null, "&7On hit, grant yourself &e" + shields + "&7 shields");
	}

	@Override
	public void initialize(Player p, FightData data, Trigger bind) {
		data.addTrigger(id, Trigger.LEFT_CLICK_HIT, (inputs) -> {
			FightInstance.dealDamage(p, type, damage, ((Damageable) inputs[1]));
			data.runActions(Trigger.BASIC_ATTACK, inputs);
			return true;
		});
	}
}
