package me.neoblade298.neorogue.equipment;

import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public abstract class HotbarCompatibleInstance extends EquipmentInstance {
	protected double staminaCost, manaCost;
	
	public HotbarCompatibleInstance(HotbarCompatible h) {
		super(h);
		this.manaCost = h.manaCost;
		this.staminaCost = h.staminaCost;
	}
	
	@Override
	public TriggerResult trigger(PlayerFightData data, Object[] inputs) {
		data.addMana(-manaCost);
		data.addStamina(-staminaCost);
		return super.trigger(data, inputs);
	}
	
	@Override
	public boolean canTrigger(Player p, PlayerFightData data) {
		if (super.canTrigger(p, data)) {
			if (data.getMana() <= manaCost) {
				Util.displayError(data.getPlayer(), "Not enough mana!");
				return false;
			}
			
			if (data.getStamina() <= staminaCost) {
				Util.displayError(data.getPlayer(), "Not enough stamina!");
				return false;
			}
			
			return true;
		}
		return false;
	}
}
