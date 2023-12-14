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

public class Rapier extends Weapon {
	
	private int shields;
	public Rapier(boolean isUpgraded) {
		super("rapier", "Rapier", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);
		damage = isUpgraded ? 90 : 60;
		type = DamageType.PIERCING;
		attackSpeed = 1;
		shields = isUpgraded ? 15 : 10;
		item = createItem(Material.STONE_SWORD, null, "&7On hit, grant yourself <yellow>" + shields + "</yellow> shields");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			FightInstance.dealDamage(p, type, damage, ((Damageable) inputs[1]));
			data.addShield(p.getUniqueId(), shields, true, 1, 100, 1, 1);
			pdata.runActions(pdata, Trigger.BASIC_ATTACK, inputs);
			return TriggerResult.keep();
		});
	}
}
