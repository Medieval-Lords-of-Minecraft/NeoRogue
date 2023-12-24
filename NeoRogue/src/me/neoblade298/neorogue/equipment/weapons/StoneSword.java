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

public class StoneSword extends Weapon {
	
	public StoneSword(boolean isUpgraded) {
		super("stoneSword", "Stone Sword", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);
		damage = !isUpgraded ? 35 : 50;
		type = DamageType.SLASHING;
		attackSpeed = 1;
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
		item = createItem(Material.STONE_SWORD, null, null);
	}
}
