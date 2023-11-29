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

public class WoodenSword extends Weapon {
	
	public WoodenSword(boolean isUpgraded) {
		super("woodenSword", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		display = "Wooden Sword";
		damage = !isUpgraded ? 25 : 35;
		type = DamageType.SLASHING;
		attackSpeed = 1;
		item = createItem(Material.WOODEN_SWORD, null, null);
		reforgeOptions.add("stoneSword");
		reforgeOptions.add("stoneAxe");
		reforgeOptions.add("stoneDagger");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addHotbarTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (inputs) -> {
			FightInstance.dealDamage(p, type, damage, ((Damageable) inputs[1]));
			data.runActions(Trigger.BASIC_ATTACK, inputs);
			return true;
		});
	}
}
