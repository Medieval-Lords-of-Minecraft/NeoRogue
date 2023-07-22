package me.neoblade298.neorogue.session.fights;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.Instance;
import me.neoblade298.neorogue.session.Session;

public class FightInstance implements Instance {
	private HashMap<UUID, FightData> fightData = new HashMap<UUID, FightData>();
	
	// This will only ever handle basic left click
	public void handleDamage(EntityDamageByEntityEvent e, boolean playerDamager) {
		Player p = playerDamager ? (Player) e.getDamager() : (Player) e.getEntity();
		UUID uuid = p.getUniqueId();
		e.setCancelled(true);
		if (p.getAttackCooldown() < 0.9F) return;
		
		if (playerDamager) {
			trigger(uuid, Trigger.LEFT_CLICK_HIT, new Object[] {p, e.getEntity()});
		}
		else {
			trigger(uuid, Trigger.RECEIVED_DAMAGE, new Object[] {p, e.getDamager()});
		}
	}
	
	public void handleHotbarSwap(PlayerItemHeldEvent e) {
		// Only cancel swap if something is bound to the trigger
		e.setCancelled(trigger(e.getPlayer().getUniqueId(), Trigger.getFromHotbarSlot(e.getNewSlot()), null));
	}
	
	private boolean trigger(UUID uuid, Trigger trigger, Object[] obj) {
		return fightData.get(uuid).runActions(trigger, obj);
	}
	
	public FightData getFightData(UUID uuid) {
		return fightData.get(uuid);
	}
	
	public void dealDamage(Damageable damager, DamageType type, double amount, Damageable... targets) {
		UUID uuid = damager.getUniqueId();
		if (!fightData.containsKey(uuid)) {
			// If no data found, just do the regular base damage
			Bukkit.getLogger().warning("[NeoRogue] Failed to find fight data for " + damager.getName());
		}
		else {
			FightData data = fightData.get(uuid);
			double multiplier = 1;
			System.out.println(type);
			for (BuffType buffType : type.getBuffTypes()) {
				Buff b = data.getBuff(true, buffType);
				amount += b.getIncrease();
				multiplier += b.getMultiplier();
			}
			amount *= multiplier;
		}

		for (Damageable target : targets) {
			receiveDamage(damager, type, amount, target);
		}
	}
	
	public void receiveDamage(Damageable damager, DamageType type, double amount, Damageable target) {
		UUID uuid = damager.getUniqueId();
		if (fightData.containsKey(uuid)) {
			// If no data found, just do the regular base damage
			target.damage(amount);
		}
		else {
			FightData data = fightData.get(uuid);
			double multiplier = 1;
			for (BuffType buffType : type.getBuffTypes()) {
				Buff b = data.getBuff(true, buffType);
				amount -= b.getIncrease();
				multiplier -= b.getMultiplier();
			}
			amount *= multiplier;
		}
	}

	@Override
	public void start(Session s) {
		s.setInstance(this);

		for (Player p : s.getOnlinePlayers()) {
			setup(p, s.getData(p.getUniqueId()));
		}
	}
	
	private void setup(Player p, PlayerSessionData data) {
		UUID uuid = p.getUniqueId();
		fightData.put(uuid, new FightData(this, data));
		
		// Setup inventory
		PlayerInventory inv = p.getInventory();
		inv.clear();
		ItemStack[] contents = inv.getContents();
		
		for (int i = 0; i < 8; i++) {
			if (data.getHotbar()[i] == null) continue;
			contents[i] = data.getHotbar()[i].getItem();
		}
		inv.setContents(contents);
	}
}
