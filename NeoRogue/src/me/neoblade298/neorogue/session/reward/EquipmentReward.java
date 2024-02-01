package me.neoblade298.neorogue.session.reward;

import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;

public class EquipmentReward implements Reward {
	private Equipment eq;
	
	public EquipmentReward(Equipment eq) {
		this.eq = eq;
	}
	
	public EquipmentReward(String str) {
		this.eq = Equipment.deserialize(str);
	}

	@Override
	public boolean claim(PlayerSessionData data, int slot, RewardInventory inv) {
		data.getPlayer().playSound(data.getPlayer(), Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
		data.giveEquipment(eq);
		return true;
	}

	@Override
	public ItemStack getIcon() {
		return eq.getItem();
	}

	@Override
	public String serialize() {
		return "equipment:" + eq.serialize();
	}

}
