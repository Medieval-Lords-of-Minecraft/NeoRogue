package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.EvadeEvent;

public class Counter extends Equipment implements Power {
	private static final String ID = "Counter";
	private int damage;
	
	public Counter(boolean isUpgraded) {
		super(ID, "Counter", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		damage = isUpgraded ? 150 : 100;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addReforge(WeaponEnchantmentElectrified.get(), ThunderclapAndFlash.get());
		addReforge(WeaponEnchantmentDarkness.get(), AbyssalCarve.get());
		addReforge(Obfuscation.get(), Evanesce.get());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.EVADE, (pdata, in) -> {
			if (data.getStamina() < data.getMaxStamina() * 0.5) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < 1) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-active", Trigger.EVADE, (pdata2, in2) -> {
					Player p = data.getPlayer();
					EvadeEvent ev = (EvadeEvent) in2;
					
					// Get the damager entity from the DamageMeta
					if (ev.getDamageMeta() == null || ev.getDamageMeta().getOwner() == null) {
						return TriggerResult.keep();
					}
					
					LivingEntity damager = ev.getDamageMeta().getOwner().getEntity();
					Location playerLoc = p.getLocation();
					Location damagerLoc = damager.getLocation();

					// Calculate dash direction away from the enemy
					Vector awayFromEnemy = playerLoc.toVector().subtract(damagerLoc.toVector()).normalize();

					// Dash away from the enemy
					data.dash(awayFromEnemy);
					
					// Deal damage to the attacker
					FightInstance.dealDamage(pdata2, DamageType.PIERCING, damage, damager, 
							DamageStatTracker.of(id + slot, Counter.this));
					Sounds.attackSweep.play(p, p);
					
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WIND_CHARGE,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after evading once while above " + DescUtil.val("50%") + " stamina. Upon " + GlossaryTag.EVADE.tag(this) + ", deal " + 
				GlossaryTag.PIERCING.tag(this, damage) + " damage to the attacker and " + 
				GlossaryTag.DASH.tag(this) + " away from them.");
	}
}
