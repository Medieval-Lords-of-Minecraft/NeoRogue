package me.neoblade298.neorogue.session.fights;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.offhands.Barrier;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.Instance;
import me.neoblade298.neorogue.session.Session;

public class FightInstance implements Instance {
	private HashMap<UUID, FightData> userData = new HashMap<UUID, FightData>();
	private HashMap<UUID, FightData> fightData = new HashMap<UUID, FightData>();
	private HashMap<UUID, BukkitTask> blockTasks = new HashMap<UUID, BukkitTask>();
	
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
	
	public void handleOffhandSwap(PlayerSwapHandItemsEvent e) {
		e.setCancelled(true);
		Player p = e.getPlayer();
		trigger(p.getUniqueId(), p.isSneaking() ? Trigger.SHIFT_SWAP : Trigger.SWAP, null);
	}
	
	public void handleDropItem(PlayerDropItemEvent e) {
		e.setCancelled(true);
		Player p = e.getPlayer();
		trigger(p.getUniqueId(), p.isSneaking() ? Trigger.SHIFT_DROP : Trigger.DROP, null);
	}
	
	public void handleRightClick(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		// Look for non-offhand triggers
		if (e.getHand() == EquipmentSlot.OFF_HAND) {
			trigger(uuid, Trigger.RAISE_SHIELD, null);
			trigger(uuid, Trigger.RIGHT_CLICK, null);
			
			if (blockTasks.containsKey(uuid)) {
				blockTasks.get(uuid).cancel();
			}
			
			blockTasks.put(uuid, new BukkitRunnable() {
				public void run() {
					if (p == null || !p.isBlocking()) {
						this.cancel();
						trigger(uuid, Trigger.LOWER_SHIELD, null);
						blockTasks.remove(uuid);
					}
					else {
						trigger(uuid, Trigger.SHIELD_TICK, null);
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 10L, 10L));
		}
		else {
			double y = p.getEyeLocation().getDirection().normalize().getY();
			if (y > 0.9) {
				trigger(uuid, Trigger.UP_RCLICK, null);
			}
			else if (y < 0.1) {
				trigger(uuid, Trigger.DOWN_RCLICK, null);
			}
			else if (p.isSneaking()) {
				trigger(uuid, Trigger.SHIFT_RCLICK, null);
			}
		}
	}
	
	public void handleLeftClick(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND) return;
		trigger(e.getPlayer().getUniqueId(), Trigger.LEFT_CLICK_NO_HIT, null);
	}
	
	private boolean trigger(UUID uuid, Trigger trigger, Object[] obj) {
		return fightData.get(uuid).runActions(trigger, obj);
	}
	
	public FightData getFightData(UUID uuid) {
		return fightData.get(uuid);
	}
	
	public HashMap<UUID, FightData> getUserData() {
		return userData;
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
		FightData fd = new FightData(this, data);
		fightData.put(uuid, fd);
		userData.put(uuid, fd);
		
		// Setup inventory
		PlayerInventory inv = p.getInventory();
		inv.clear();
		ItemStack[] contents = inv.getContents();
		
		for (int i = 0; i < 8; i++) {
			if (data.getHotbar()[i] == null) continue;
			contents[i] = data.getHotbar()[i].getItem();
		}
		inv.setContents(contents);
		
		if (data.getOffhand() != null) inv.setItemInOffHand(data.getOffhand().getItem());
	}
}
