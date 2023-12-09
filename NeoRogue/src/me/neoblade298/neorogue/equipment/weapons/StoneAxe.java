package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Weapon;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.Buff;
import me.neoblade298.neorogue.session.fights.BuffType;
import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightInstance;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class StoneAxe extends Weapon {
	
	public StoneAxe(boolean isUpgraded) {
		super("stoneAxe", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);
		display = "Stone Axe";
		damage = !isUpgraded ? 70 : 115;
		type = DamageType.BLUNT;
		attackSpeed = 2;
		item = createItem(Material.STONE_AXE, null, null);
		reforgeOptions.add("ironAxe");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			Buff b = data.getBuff(true, BuffType.PHYSICAL);
			double strength = b.getIncrease();
			FightInstance.dealDamage(p, type, damage + (strength * 2), ((Damageable) inputs[1]));
			pdata.runActions(pdata, Trigger.BASIC_ATTACK, inputs);
			return true;
		});
	}
}
