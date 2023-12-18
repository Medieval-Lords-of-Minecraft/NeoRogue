package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Weapon;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ForcefulLeatherGauntlets extends Weapon {
	
	public ForcefulLeatherGauntlets(boolean isUpgraded) {
		super("forcefulLeatherGauntlets", "Forceful Leather Gauntlets", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);
		damage = isUpgraded ? 15 : 20;
		type = DamageType.BLUNT;
		attackSpeed = 0.5;
		item = createItem(Material.LEATHER, null, null);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			dealDamage(p, (Damageable) inputs[1]);
			pdata.runBasicAttack(pdata, inputs, this);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER, null, null);
	}
}
