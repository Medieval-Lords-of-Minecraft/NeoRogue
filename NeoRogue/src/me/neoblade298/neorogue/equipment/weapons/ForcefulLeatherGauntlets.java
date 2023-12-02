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

public class ForcefulLeatherGauntlets extends Weapon {
	
	public ForcefulLeatherGauntlets(boolean isUpgraded) {
		super("forcefulLeatherGauntlets", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);
		display = "Forceful Leather Gauntlets";
		damage = isUpgraded ? 15 : 20;
		type = DamageType.BLUNT;
		attackSpeed = 0.5;
		item = createItem(Material.LEATHER, null, null);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addHotbarTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			FightInstance.dealDamage(p, type, damage, ((Damageable) inputs[1]));
			pdata.runActions(pdata, Trigger.BASIC_ATTACK, inputs);
			return true;
		});
	}
}
