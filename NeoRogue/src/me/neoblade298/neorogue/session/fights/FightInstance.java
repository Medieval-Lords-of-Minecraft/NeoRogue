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

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.offhands.Barrier;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.Instance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class FightInstance implements Instance {
	private static HashMap<UUID, FightData> userData = new HashMap<UUID, FightData>();
	private static HashMap<UUID, FightData> fightData = new HashMap<UUID, FightData>();
	private static HashMap<UUID, BukkitTask> blockTasks = new HashMap<UUID, BukkitTask>();
	
	private Session s;
	
	public FightInstance(Session s) {
		this.s = s;
	}
	
	// This will only ever handle basic left click
	public static void handleDamage(EntityDamageByEntityEvent e, boolean playerDamager) {
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
	
	public static void handleHotbarSwap(PlayerItemHeldEvent e) {
		// Only cancel swap if something is bound to the trigger
		e.setCancelled(trigger(e.getPlayer().getUniqueId(), Trigger.getFromHotbarSlot(e.getNewSlot()), null));
	}
	
	public static void handleOffhandSwap(PlayerSwapHandItemsEvent e) {
		e.setCancelled(true);
		Player p = e.getPlayer();
		trigger(p.getUniqueId(), p.isSneaking() ? Trigger.SHIFT_SWAP : Trigger.SWAP, null);
	}
	
	public static void handleDropItem(PlayerDropItemEvent e) {
		e.setCancelled(true);
		Player p = e.getPlayer();
		trigger(p.getUniqueId(), p.isSneaking() ? Trigger.SHIFT_DROP : Trigger.DROP, null);
	}
	
	public static void handleRightClick(PlayerInteractEvent e) {
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
	
	public static void handleLeftClick(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND) return;
		trigger(e.getPlayer().getUniqueId(), Trigger.LEFT_CLICK_NO_HIT, null);
	}
	
	public static void handleMythicDespawn(MythicMobDespawnEvent e) {
		fightData.remove(e.getEntity().getUniqueId());
	}
	
	public static void handleMythicDeath(MythicMobDeathEvent e) {
		fightData.remove(e.getEntity().getUniqueId());
	}
	
	private static boolean trigger(UUID uuid, Trigger trigger, Object[] obj) {
		return fightData.get(uuid).runActions(trigger, obj);
	}
	
	public static FightData getFightData(UUID uuid) {
		return fightData.get(uuid);
	}
	
	public static HashMap<UUID, FightData> getUserData() {
		return userData;
	}
	
	public static void dealBarrieredDamage(Damageable damager, DamageType type, double amount, Damageable... targets) {
		dealDamage(damager, type, amount, true, targets);
	}
	
	public static void dealDamage(Damageable damager, DamageType type, double amount, Damageable... targets) {
		dealDamage(damager, type, amount, false, targets);
	}
	
	public static void dealDamage(Damageable damager, DamageType type, double amount, boolean hitBarrier, Damageable... targets) {
		UUID uuid = damager.getUniqueId();
		if (!fightData.containsKey(uuid)) {
			// If no data found, just do the regular base damage
			Bukkit.getLogger().warning("[NeoRogue] Failed to find fight data for (attacker) " + damager.getName());
		}
		else {
			FightData data = fightData.get(uuid);
			double multiplier = 1;
			for (BuffType buffType : type.getBuffTypes()) {
				Buff b = data.getBuff(true, buffType);
				if (b == null) continue;
				
				amount += b.getIncrease();
				multiplier += b.getMultiplier();
			}
			amount *= multiplier;
		}

		for (Damageable target : targets) {
			receiveDamage(damager, type, amount, true, target);
		}
	}
	
	public static void receiveDamage(Damageable damager, DamageType type, double amount, boolean hitBarrier, Damageable target) {
		UUID uuid = target.getUniqueId();
		if (!fightData.containsKey(uuid)) {
			// If no data found, just do the regular base damage
			Bukkit.getLogger().warning("[NeoRogue] Failed to find fight data for (defender) " + target.getName());
			target.damage(amount);
		}
		else {
			FightData data = fightData.get(uuid);
			
			// First reduce damage from barriers
			if (hitBarrier && data.getBarrier() != null) {
				amount = data.getBarrier().applyDefenseBuffs(amount, type);
			}

			// Next calculate damage to shields
			if (!data.getShields().isEmpty()) {
				ShieldHolder shields = data.getShields();
				amount = Math.max(0, shields.useShields(amount));
				new BukkitRunnable() {
					public void run() {
						shields.update();
					}
				}.runTask(NeoRogue.inst());
			}
			if (amount <= 0) {
				target.damage(0.1);
				return;
			}
			
			// Finally calculate hp damage
			double multiplier = 1;
			for (BuffType buffType : type.getBuffTypes()) {
				Buff b = data.getBuff(false, buffType);
				if (b == null) continue; 
				
				amount -= b.getIncrease();
				multiplier -= b.getMultiplier();
			}
			amount *= multiplier;
			target.damage(amount);
		}
	}

	@Override
	public void start(Session s) {
		s.setInstance(this);

		for (Player p : s.getOnlinePlayers()) {
			setup(p, s.getData(p.getUniqueId()));
		}
	}
	
	public static void putFightData(UUID uuid, FightData data) {
		fightData.put(uuid, data);
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
	
	public void cleanup() {
		for (UUID uuid : s.getParty().keySet()) {
			userData.remove(uuid).cleanup();
			fightData.remove(uuid).cleanup();
		}
	}
}
