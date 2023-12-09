package me.neoblade298.neorogue.equipment.weapons;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Weapon;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class LeatherGauntlets extends Weapon {
	
	public LeatherGauntlets(boolean isUpgraded) {
		super("leatherGauntlets", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		display = "Leather Gauntlets";
		damage = !isUpgraded ? 10 : 15;
		type = DamageType.BLUNT;
		attackSpeed = 0.5;
		item = createItem(Material.LEATHER, null, null);
		reforgeOptions.put("ironSword", Arrays.asList("forcefulLeatherGauntlets", "earthenLeatherGauntlets", "lightLeatherGauntlets"));
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			FightInstance.dealDamage(p, type, damage, ((Damageable) inputs[1]));
			pdata.runActions(pdata, Trigger.BASIC_ATTACK, inputs);
			return TriggerResult.keep();
		});
	}
}
