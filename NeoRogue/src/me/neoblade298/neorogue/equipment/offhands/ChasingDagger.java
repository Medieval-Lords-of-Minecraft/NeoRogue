package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Offhand;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ChasingDagger extends Offhand {
	private int hits;
	
	public ChasingDagger(boolean isUpgraded) {
		super("chaser", "Chaser", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, Trigger.RIGHT_CLICK_HIT, (pdata, inputs) -> {
			FightInstance.dealDamage(p, type, damage, target);
			pdata.setoff
			pdata.runBasicAttack(pdata, inputs, this);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(this, Material.STONE_SWORD, null, "Right click to attack. Has its own separate attack cooldown.");
	}
}
