package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Weapon;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightInstance;
import me.neoblade298.neorogue.session.fights.PlayerFightData;
import me.neoblade298.neorogue.session.fights.Shield;

public class Rapier extends Weapon {
	
	private int shields;
	public Rapier(boolean isUpgraded) {
		super("rapier", isUpgraded, Rarity.UNCOMMON);
		display = "Rapier";
		damage = isUpgraded ? 12 : 9;
		type = DamageType.PIERCING;
		attackSpeed = 1;
		shields = isUpgraded ? 20 : 15;
		item = createItem(Material.STONE_SWORD, null, "&7On hit, grant yourself &e" + shields + "&7 shields");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		data.addHotbarTrigger(id, hotbar, Trigger.LEFT_CLICK_HIT, (inputs) -> {
			FightInstance.dealDamage(p, type, damage, ((Damageable) inputs[1]));
			data.getShields().addShield(new Shield(data, p.getUniqueId(), shields, true, 1, 100, 1, 1));
			data.runActions(Trigger.BASIC_ATTACK, inputs);
			return true;
		});
	}
}
