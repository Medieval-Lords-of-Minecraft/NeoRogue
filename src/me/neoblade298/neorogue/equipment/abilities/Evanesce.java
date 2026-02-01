package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.EvadeEvent;

public class Evanesce extends Equipment {
	private static final String ID = "Evanesce";
	private int damage, stealthDuration;
	private double damageBuff;
	
	public Evanesce(boolean isUpgraded) {
		super(ID, "Evanesce", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		damage = isUpgraded ? 150 : 100;
		stealthDuration = isUpgraded ? 200 : 120;
		damageBuff = isUpgraded ? 0.3 : 0.2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.EVADE, (pdata, in) -> {
			EvadeEvent ev = (EvadeEvent) in;
			
			// Get the damager entity from the DamageMeta
			if (ev.getDamageMeta() == null || ev.getDamageMeta().getOwner() == null) {
				return TriggerResult.keep();
			}
			
			Player p = data.getPlayer();
			LivingEntity damager = ev.getDamageMeta().getOwner().getEntity();
			Location playerLoc = p.getLocation();
			Location damagerLoc = damager.getLocation();

			// Calculate dash direction away from the enemy
			Vector awayFromEnemy = playerLoc.toVector().subtract(damagerLoc.toVector()).normalize();

			// Dash away from the enemy
			data.dash(awayFromEnemy);
			
			// Deal damage to the attacker
			FightInstance.dealDamage(pdata, DamageType.PIERCING, damage, damager, 
					DamageStatTracker.of(id + slot, this));
			
			// Apply stealth
			FightInstance.applyStatus(p, StatusType.STEALTH, data, 1, stealthDuration);
			
			// Delayed damage buff (1 second = 20 ticks later)
			data.addTask(new BukkitRunnable() {
				public void run() {
					data.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
						new Buff(data, 0, damageBuff, StatTracker.damageBuffAlly(id, Evanesce.this)),
						100); // 5 seconds
				}
			}.runTaskLater(NeoRogue.inst(), 20L));
			
			Sounds.attackSweep.play(p, p);
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ECHO_SHARD,
				"Passive. Upon " + GlossaryTag.EVADE.tag(this) + ", deal " + 
				GlossaryTag.PIERCING.tag(this, damage, true) + " damage to the attacker and " + 
				GlossaryTag.DASH.tag(this) + " away from them. Gain " +
				GlossaryTag.STEALTH.tag(this, 1, false) + " [" + DescUtil.yellow(
                                stealthDuration / 20) + "]. " +
				"After <white>1s</white>, gain <yellow>" + (int)(damageBuff * 100) + "%</yellow> increased " +
				GlossaryTag.GENERAL.tag(this) + " damage for <white>5s</white>.");
	}
}
