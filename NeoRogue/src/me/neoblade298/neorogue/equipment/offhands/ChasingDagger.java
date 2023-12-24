package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;


import me.neoblade298.neorogue.equipment.OffhandWeapon;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ChasingDagger extends OffhandWeapon {
	public ChasingDagger(boolean isUpgraded) {
		super("chaser", "Chaser", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, Trigger.RIGHT_CLICK_HIT, (pdata, inputs) -> {
			this.dealDamage(p, (Damageable) inputs[1]);
			pdata.setOffhandAttackCooldown(this);
			pdata.runBasicAttack(pdata, inputs, this);
			p.swingOffHand();
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SWORD, null, "Right click to basic attack. Has its own separate attack cooldown.");
	}
}
