package me.neoblade298.neorogue.equipment.weapons;

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

public class WoodenSword extends Weapon {
	
	public WoodenSword(boolean isUpgraded) {
		super("woodenSword", "Wooden Sword", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		damage = !isUpgraded ? 25 : 35;
		type = DamageType.SLASHING;
		attackSpeed = 1;
		item = createItem(Material.WOODEN_SWORD, null, null);
		addReforgeOption("woodenSword", new String[] {"stoneSword", "stoneAxe", "stoneDagger"});
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
