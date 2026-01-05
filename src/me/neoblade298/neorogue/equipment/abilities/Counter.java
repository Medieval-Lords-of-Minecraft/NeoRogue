package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreEvadeEvent;

public class Counter extends Equipment {
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
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.EVADE, (pdata, in) -> {
			PreEvadeEvent ev = (PreEvadeEvent) in;
			
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
			FightInstance.dealDamage(pdata, DamageType.PIERCING, damage, damager, 
					DamageStatTracker.of(id + slot, this));
			Sounds.attackSweep.play(p, p);
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WIND_CHARGE,
				"Passive. Upon " + GlossaryTag.EVADE.tag(this) + ", deal " + 
				GlossaryTag.PIERCING.tag(this, damage, true) + " damage to the attacker and " + 
				GlossaryTag.DASH.tag(this) + " away from them.");
	}
}
