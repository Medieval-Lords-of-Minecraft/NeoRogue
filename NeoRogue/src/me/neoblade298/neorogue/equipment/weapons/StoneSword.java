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

public class StoneSword extends Weapon {
	
	public StoneSword(boolean isUpgraded) {
		super("stoneSword", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);
		display = "Stone Sword";
		damage = !isUpgraded ? 35 : 50;
		type = DamageType.SLASHING;
		attackSpeed = 1;
		item = createItem(Material.STONE_SWORD, null, null);
		reforgeOptions.add("ironSword");
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
