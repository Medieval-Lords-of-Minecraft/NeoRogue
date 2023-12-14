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

public class StoneSword extends Weapon {
	
	public StoneSword(boolean isUpgraded) {
		super("stoneSword", "Stone Sword", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);
		damage = !isUpgraded ? 35 : 50;
		type = DamageType.SLASHING;
		attackSpeed = 1;
		item = createItem(Material.STONE_SWORD, null, null);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			FightInstance.dealDamage(p, type, damage, ((Damageable) inputs[1]));
			pdata.runActions(pdata, Trigger.BASIC_ATTACK, inputs);
			return TriggerResult.keep();
		});
	}
}
