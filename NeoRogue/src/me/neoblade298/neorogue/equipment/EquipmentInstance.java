package me.neoblade298.neorogue.equipment;

import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public abstract class EquipmentInstance implements TriggerAction {
	private Equipment eq;
	private long lastUsed = 0L;
	protected int cooldown = 0;
	
	public EquipmentInstance(Equipment eq) {
		this.eq = eq;
	}
	
	@Override
	public TriggerResult trigger(PlayerFightData data, Object[] inputs) {
		lastUsed = System.currentTimeMillis();
		return run(data, inputs);
	}
	public abstract TriggerResult run(PlayerFightData data, Object[] inputs);
	
	public boolean canTrigger(Player p, PlayerFightData data) {
		if (lastUsed + (cooldown * 1000) < System.currentTimeMillis()) {
			sendCooldownMessage(p);
			return false;
		}
		return true;
	}
	
	public String getDisplay() {
		return eq.display;
	}
	
	public void sendCooldownMessage(Player p) {
		int cooldownOver = (int) ((lastUsed / 1000) + cooldown);
		int now = (int) (System.currentTimeMillis() / 1000);
		Util.msgRaw(p, NeoCore.miniMessage().deserialize("<yellow>" + eq.display + " <red>cooldown: </red>" + (cooldownOver - now) + "s"));
	}
	
	public Equipment getEquipment() {
		return eq;
	}
}
