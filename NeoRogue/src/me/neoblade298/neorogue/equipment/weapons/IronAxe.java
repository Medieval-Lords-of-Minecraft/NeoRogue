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
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class IronAxe extends Weapon {
	
	public IronAxe(boolean isUpgraded) {
		super("ironAxe", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR);
		display = "Iron Axe";
		damage = isUpgraded ? 150 : 225;
		type = DamageType.BLUNT;
		attackSpeed = 2;
		item = createItem(Material.STONE_AXE, null, null);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			Buff b = data.getBuff(true, BuffType.PHYSICAL);
			double strength = b.getIncrease();
			FightInstance.dealDamage(p, type, damage + (strength * 3), ((Damageable) inputs[1]));
			pdata.runActions(pdata, Trigger.BASIC_ATTACK, inputs);
			return TriggerResult.keep();
		});
	}
}
