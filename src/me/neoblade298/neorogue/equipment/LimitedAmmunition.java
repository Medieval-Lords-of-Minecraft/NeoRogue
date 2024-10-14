package me.neoblade298.neorogue.equipment;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public abstract class LimitedAmmunition extends Ammunition {
	protected int uses;
	public LimitedAmmunition(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec, EquipmentType type, EquipmentProperties props, int uses) {
		super(id, display, isUpgraded, rarity, ec, type, props);
		this.uses = uses;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ItemStack item = p.getInventory().getItem(slot);
		item.setAmount(uses);
		AmmunitionInstance inst = new AmmunitionInstance(data, this, slot);
		if (data.getAmmoInstance() == null) {
			equip(p, data, inst);
		}

		data.addTrigger(id, bind, (pdata, in) -> {
			if (uses <= 0) return TriggerResult.remove();
			equip(p, data, inst);
			return TriggerResult.keep();
		});
	}
}
