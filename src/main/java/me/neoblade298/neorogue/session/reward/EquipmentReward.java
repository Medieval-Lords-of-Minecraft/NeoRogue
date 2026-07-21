package me.neoblade298.neorogue.session.reward;

import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.PlayerSessionData;

public class EquipmentReward implements Reward {
	private SessionEquipment se;
	
	public EquipmentReward(SessionEquipment se) {
		this.se = se;
	}
	
	public EquipmentReward(String str) {
		this.se = SessionEquipment.deserialize(str);
	}

	@Override
	public boolean claim(PlayerSessionData data, int slot, RewardInventory inv) {
		data.getPlayer().playSound(data.getPlayer(), Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
		data.giveEquipment(se);
		return true;
	}

	@Override
	public ItemStack getIcon(PlayerSessionData data) {
		return se.getChoiceItem(data);
	}

	@Override
	public String serialize() {
		return "equipment:" + se.serialize();
	}

}
