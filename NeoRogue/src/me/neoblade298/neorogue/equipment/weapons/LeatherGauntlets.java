package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Weapon;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightInstance;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class LeatherGauntlets extends Weapon {
	
	public LeatherGauntlets(boolean isUpgraded) {
		super("leatherGauntlets", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		display = "Leather Gauntlets";
		damage = !isUpgraded ? 10 : 15;
		type = DamageType.BLUNT;
		attackSpeed = 0.5;
		item = createItem(Material.LEATHER, null, null);
		reforgeOptions.add("forcefulLeatherGauntlets");
		reforgeOptions.add("earthenLeatherGauntlets");
		reforgeOptions.add("lightLeatherGauntlets");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		data.addHotbarTrigger(id, hotbar, Trigger.LEFT_CLICK_HIT, (inputs) -> {
			FightInstance.dealDamage(p, type, damage, ((Damageable) inputs[1]));
			data.runActions(Trigger.BASIC_ATTACK, inputs);
			return true;
		});
	}
}
