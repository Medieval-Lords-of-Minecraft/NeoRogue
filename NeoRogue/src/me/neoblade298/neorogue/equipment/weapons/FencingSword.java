package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;


import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Weapon;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class FencingSword extends Weapon {
	private int shields;
	
	public FencingSword(boolean isUpgraded) {
		super("fencingSword", "Fencing Sword", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);
		damage = isUpgraded ? 50 : 40;
		type = DamageType.PIERCING;
		attackSpeed = 1;
		shields = isUpgraded ? 7 : 4;
		addReforgeOption("fencingSword", new String[] {"rapier", "serratedFencingSword"});
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			dealDamage(p, (Damageable) inputs[1]);
			pdata.runBasicAttack(pdata, inputs, this);
			data.addShield(p.getUniqueId(), shields, true, 1, 100, 1, 1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SWORD, null, "On hit, grant yourself <yellow>" + shields + "</yellow> shields");
	}
}
