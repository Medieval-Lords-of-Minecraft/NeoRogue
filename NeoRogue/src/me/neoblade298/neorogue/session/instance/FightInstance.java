package me.neoblade298.neorogue.session.instance;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import me.neoblade298.neorogue.player.Trigger;

public class FightInstance {
	private HashMap<UUID, FightData> fightData = new HashMap<UUID, FightData>();
	
	
	public void handleOnDamage(EntityDamageByEntityEvent e, boolean playerDamager) {
		Player p = playerDamager ? (Player) e.getDamager() : (Player) e.getEntity();
		UUID uuid = p.getUniqueId();
		
		if (playerDamager) {
			trigger(uuid, Trigger.LEFT_CLICK_HIT, new Object[] {p, e.getEntity()});
		}
		else {
			trigger(uuid, Trigger.RECEIVED_DAMAGE, new Object[] {p, e.getDamager()});
		}
	}
	
	private void trigger(UUID uuid, Trigger trigger, Object[] obj) {
		fightData.get(uuid).runActions(trigger, obj);
	}
}
